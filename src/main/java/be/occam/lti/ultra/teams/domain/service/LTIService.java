package be.occam.lti.ultra.teams.domain.service;

import be.occam.lti.ultra.teams.config.SystemProperties;
import be.occam.lti.ultra.teams.domain.LTIContentItem;
import be.occam.lti.ultra.teams.domain.LTIUser;
import com.azure.core.util.UrlBuilder;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.id.Subject;
import com.nimbusds.oauth2.sdk.token.TypelessToken;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.session.DefaultWebSessionManager;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class LTIService {

    protected final String SESSION_ATTRIBUTE_NONCE = "ltiNonce";
    protected final String SESSION_ATTRIBUTE_JWT = "ltiToken";

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final IDTokenValidator ltiIdTokenValidator;
    @Value("${occam.lti.ultra.teams.system.redirects}")
    protected URI[] redirects;
    @Value("https://${occam.lti.ultra.host}/learn/api/public/v1/oauth2/authorizationcode")
    protected URI oauthAuthorizationUri;
    protected final ClientID ltiClientId;
    protected final String ltiClientSecret;
    protected final JWKSetService jwkSetService;
    protected final SystemProperties systemProperties;

    @Value("${occam.lti.ultra.authorization-host}/api/v1/gateway/oidcauth")
    private URI oauthOidcInitUri;

    public LTIService(
            @Value("${occam.lti.ultra.issuer}") Issuer issuer,
            @Value("${spring.security.oauth2.client.registration.ultra.client-id}") ClientID clientId,
            @Value("${spring.security.oauth2.client.registration.ultra.client-secret}") String ltiClientSecret,
            @Value("${spring.security.oauth2.client.provider.ultra.jwk-set-uri}") URL jwkSetUrl, JWKSetService jwkSetService, SystemProperties systemProperties) {
        this.ltiClientId = clientId;
        this.ltiClientSecret = ltiClientSecret;
        this.jwkSetService = jwkSetService;
        this.systemProperties = systemProperties;
        this.ltiIdTokenValidator = new IDTokenValidator(
                issuer,
                this.ltiClientId,
                JWSAlgorithm.RS256,
                jwkSetUrl
        );;
    }

    public URI thirdPartyLogin(
            Issuer issuer,
            URI redirectUri,
            ClientID clientId,
            String loginHint,
            String ltiMessageHint,
            HttpServletRequest httpRequest) {
        if (!ltiIdTokenValidator.getExpectedIssuer().equals(issuer)) {
                logger.warn("Expected issuer [{}], actual [{}]", ltiIdTokenValidator.getExpectedIssuer(), issuer);
                throw new ResponseStatusException(BAD_REQUEST);
        }
        else if(!ltiIdTokenValidator.getClientID().equals(clientId)) {
            logger.warn("Expected client id [{}], actual [{}]", ltiIdTokenValidator.getClientID(), clientId);
            throw new ResponseStatusException(BAD_REQUEST);
        }
        else if (!Arrays.stream(this.redirects).anyMatch(r -> r.equals(redirectUri))) {
            logger.warn("Expected redirect uris {}, actual [{}]", this.redirects, redirectUri);
            throw new ResponseStatusException(BAD_REQUEST);
        }

        String nonce = UUID.randomUUID().toString().replace("-","");
        HttpSession session = httpRequest.getSession(true);
        // TODO: make multi-tab-safe (session shared between tabs)
        session.setAttribute(SESSION_ATTRIBUTE_NONCE, nonce);
        URI redirectURI = new DefaultUriBuilderFactory()
                .builder()
                .scheme(this.oauthOidcInitUri.getScheme())
                .host(this.oauthOidcInitUri.getHost())
                .path(this.oauthOidcInitUri.getPath())
                .queryParam("scope", "openid")
                .queryParam("response_type", "id_token")
                .queryParam("client_id", clientId.getValue())
                .queryParam("redirect_uri", UriUtils.encodeQueryParam(redirectUri.toString(),Charset.defaultCharset()))
                // TODO - store login hint for later verification
                .queryParam("login_hint", loginHint)
                .queryParam("lti_message_hint", ltiMessageHint)
                .queryParam("response_mode", "form_post")
                .queryParam("nonce", nonce)
                .queryParam("prompt", "none")
                .build();

        logger.info("redirect uri = [{}]", redirectURI.toString());
        return redirectURI;
    }

    public LTIUser authenticated(String idTokenString, String stateString, HttpServletRequest httpRequest) {
        try {
            JWT idToken = JWTParser.parse(idTokenString);
            State state = State.parse(stateString);
            HttpSession session = httpRequest.getSession(true);
            new DefaultWebSessionManager().getSessionIdResolver();
            // TODO: make multi-tab-safe (session shared between tabs, should use unique attribute name)
            String nonce = (String) session.getAttribute(SESSION_ATTRIBUTE_NONCE);
            JWTClaimsSet jwtClaims = this.validateToken(idToken,new Nonce(nonce));
            Map<String, Object> claims = jwtClaims.getClaims();
            Map<String,Object> lisClaims = (Map) claims.get("https://purl.imsglobal.org/spec/lti/claim/lis");
            logger.info("lis claim is [{}]", lisClaims);
            String userId = (String) lisClaims.get("person_sourcedid");
            String email = (String) claims.get("email");
            String oneTimeSessionId = (String) claims.get("https://blackboard.com/lti/claim/one_time_session_token");
            LTIUser ltiUser = new LTIUser(new Subject(userId), new TypelessToken(oneTimeSessionId), email);
            PreAuthenticatedAuthenticationToken authentication = new PreAuthenticatedAuthenticationToken(userId, oneTimeSessionId);
            authentication.setAuthenticated(true);
            authentication.setDetails(ltiUser);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
            session.setAttribute(SESSION_ATTRIBUTE_JWT,idToken);
            return ltiUser;
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public JWT deepLinkingResponseToken(String title, URL url, HttpServletRequest httpRequest) {
        try {
            HttpSession session = httpRequest.getSession(false);
            JWT deepLinkingRequestToken = (JWT) session.getAttribute(SESSION_ATTRIBUTE_JWT);
            JWSHeader header = new JWSHeader
                    .Builder(JWSAlgorithm.RS256)
                    .type(JOSEObjectType.JWT)
                    .build();
            JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                    .issuer(deepLinkingRequestToken.getJWTClaimsSet().getIssuer())
                    .audience(deepLinkingRequestToken.getJWTClaimsSet().getAudience())
                    .expirationTime(new Date(Instant.now().plus(Duration.ofMinutes(5L)).toEpochMilli()))
                    .issueTime(new Date(Instant.now().plus(Duration.ofMinutes(5L)).toEpochMilli()))
                    .claim("nonce",deepLinkingRequestToken.getJWTClaimsSet().getClaim("nonce"))
                    .claim("https://purl.imsglobal.org/spec/lti/claim/deployment_id", deepLinkingRequestToken.getJWTClaimsSet().getClaim("https://purl.imsglobal.org/spec/lti/claim/deployment_id"))
                    .claim("https://purl.imsglobal.org/spec/lti/claim/message_type","LtiDeepLinkingResponse")
                    .claim("https://purl.imsglobal.org/spec/lti/claim/version","1.3.0")
                    .claim("https://purl.imsglobal.org/spec/lti-dl/claim/content_items", List.of(
                        new LTIContentItem("ltiResourceLink", title, url)
                    ))
                    .build();
            SignedJWT deepLinkingResponseToken =  new SignedJWT(header,jwtClaimsSet);
            JWSSigner signer = new RSASSASigner(this.jwkSetService.privateKey());
            try {
                deepLinkingResponseToken.sign(signer);
            } catch (JOSEException e) {
                throw new RuntimeException(e);
            }

            return deepLinkingResponseToken;
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public URL deepLinkingResponseURL(HttpServletRequest httpRequest) {
        try {
            HttpSession session = httpRequest.getSession(false);
            JWT deepLinkingRequestToken = (JWT) session.getAttribute(SESSION_ATTRIBUTE_JWT);
            Map<String,Object> deepLinkingClaims = (Map) deepLinkingRequestToken.getJWTClaimsSet().getClaim("https://purl.imsglobal.org/spec/lti-dl/claim/deep_linking_settings");
            return UrlBuilder.parse((String) deepLinkingClaims.get("deep_link_return_url")).toUrl();
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private JWTClaimsSet validateToken(JWT jwt, Nonce expectedNonce) throws BadJOSEException, JOSEException, ParseException {
        if(jwt == null) {
            throw new RuntimeException("Jwt token cannot be null");
        }
        if(expectedNonce == null) {
            throw new RuntimeException("Nonce cannot be null");
        }
        return ltiIdTokenValidator.validate(jwt, expectedNonce).toJWTClaimsSet();
    }
}
