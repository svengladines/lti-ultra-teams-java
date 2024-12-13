package be.occam.lti.ultra.teams.domain.service;

import be.occam.lti.ultra.teams.domain.LTIUser;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.id.Subject;
import com.nimbusds.oauth2.sdk.token.Token;
import com.nimbusds.oauth2.sdk.token.TypelessToken;
import com.nimbusds.openid.connect.sdk.*;
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator;
import jakarta.servlet.http.HttpServletRequest;
import org.jsoup.Connection.KeyVal;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.nimbusds.openid.connect.sdk.OIDCScopeValue.OPENID;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class LTIService {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final IDTokenValidator ltiIdTokenValidator;
    @Value("${occam.lti.ultra.target.meeting}")
    protected URI redirectUri;
    @Value("https://${occam.lti.ultra.host}/learn/api/public/v1/oauth2/authorizationcode")
    protected URI oauthAuthorizationUri;
    protected final ClientID ltiClientId;

    @Value("${occam.lti.ultra.authorization-host}/api/v1/gateway/oidcauth")
    private URI oauthOidcInitUri;

    public LTIService(
                       @Value("${occam.lti.ultra.issuer}") Issuer issuer,
                       @Value("${occam.lti.ultra.client-id}") ClientID clientId,
                       @Value("${occam.lti.ultra.authorization-host}/.well-known/jwks.json") URL jwkSetUrl) {
        this.ltiClientId = clientId;
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
        if (
            !ltiIdTokenValidator.getExpectedIssuer().equals(issuer) ||
                !ltiIdTokenValidator.getClientID().equals(clientId) ||
                !this.redirectUri.equals(redirectUri)) {
            throw new ResponseStatusException(BAD_REQUEST);
        }

        /* move to launch ...
        final LTIUser ltiUser = ltiLogin(loginHint, ltiMessageHint);
        PreAuthenticatedAuthenticationToken authentication = new PreAuthenticatedAuthenticationToken(ltiUser.userId(), ltiUser.oneTimeSessionToken());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
         */
        URI redirectURI = new DefaultUriBuilderFactory()
                .builder()
                .scheme(this.oauthOidcInitUri.getScheme())
                .host(this.oauthOidcInitUri.getHost())
                .path(this.oauthOidcInitUri.getPath())
                .queryParam("scope", "openid")
                .queryParam("response_type", "id_token")
                .queryParam("client_id", clientId.getValue())
                .queryParam("redirect_uri", UriUtils.encodeQueryParam(this.redirectUri.toString(),Charset.defaultCharset()))
                // TODO - store login hint for later verification
                .queryParam("login_hint", loginHint)
                .queryParam("lti_message_hint", ltiMessageHint)
                .queryParam("response_mode", "form_post")
                // TODO - store nonce for later verification
                .queryParam("nonce", UUID.randomUUID().toString().replace("-",""))
                .queryParam("prompt", "none")
                .build();
        return redirectURI;
    }

    protected AuthorizationCode getOauthCode(Token oneTimeSessionToken) {
        final State expectedState = new State();
        // The initial oauth authentication request.
        // Normally the browser executes this and the user is prompted to give permission.
        // However the 'permission prompt' is disabled for this oauth application in ultra
        // and the 'one_time_session_token' allows us to start the request in the backend.
        AuthorizationRequest authorizationRequest = new AuthorizationRequest.Builder(ResponseType.CODE, ltiClientId)
                .endpointURI(oauthAuthorizationUri)
                .redirectionURI(redirectUri)
                .state(expectedState)
                .customParameter("one_time_session_token", oneTimeSessionToken.getValue())
                .build();

        try {
            // Self-posting form that blackboard uses...
            String locationHeader = Jsoup
                    .parse(authorizationRequest.toHTTPRequest().send().getBody())
                    .expectForm("#bltiLaunchForm")
                    .submit()
                    .followRedirects(false)
                    .execute()
                    .header("location");

            if(locationHeader == null) {
                throw new RuntimeException("Could not start oauth flow");
            }
            AuthenticationResponse authenticationResponse = AuthenticationResponseParser.parse(URI.create(locationHeader));
            if(!expectedState.equals(authenticationResponse.getState())) {
                throw new RuntimeException();
            }
            return authenticationResponse.toSuccessResponse().getAuthorizationCode();
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }


    private LTIUser manualLtiLogin(String loginHint, String ltiMessageHint) {

        final State expectedState = new State();
        final Nonce expectedNonce = new Nonce();

        try {

            // The initial (and only) lti authentication request
            // Normally the browser executes this in an iframe, but without cookies we cannot verify the state or nonce parameter.
            // 'login_hint' and 'lti_message_hint) effectively act as one-time-session-token
            AuthenticationRequest authenticationRequest = new AuthenticationRequest.Builder(ResponseType.IDTOKEN, new Scope(OPENID), this.ltiClientId, redirectUri)
                    .endpointURI(oauthOidcInitUri)
                    .responseMode(ResponseMode.FORM_POST)
                    .prompt(Prompt.Type.NONE)
                    .state(expectedState)
                    .nonce(expectedNonce)
                    .customParameter("login_hint", loginHint)
                    .customParameter("lti_message_hint", ltiMessageHint)
                    .build();

            // Self-posting form that blackboard uses...
            String body = authenticationRequest.toHTTPRequest().send().getBody();
            logger.info("received HTML document: {}", body);
            Document doc = Jsoup.parse(body);
            Map<String, String> formParams = doc
                    .expectForm("#bltiLaunchForm")
                    .formData()
                    .stream()
                    .collect(Collectors.toMap(KeyVal::key, KeyVal::value));

            JWT idToken = JWTParser.parse(formParams.get("id_token"));
            State state = State.parse(formParams.get("state"));

            if (!expectedState.equals(state)) {
                throw new RuntimeException();
            }

            JWTClaimsSet claimsSet = validateToken(idToken, expectedNonce);

            return new LTIUser(
                    new Subject(claimsSet.getJSONObjectClaim("https://purl.imsglobal.org/spec/lti/claim/lis").get("person_sourcedid").toString()),
                    new TypelessToken(claimsSet.getStringClaim("https://blackboard.com/lti/claim/one_time_session_token"))
            );
        }
        catch(Exception e) {
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
