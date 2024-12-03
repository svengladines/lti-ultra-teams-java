package be.occam.lti.ultra.teams.domain.service;

import be.occam.lti.ultra.teams.domain.LTIUser;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.id.Subject;
import com.nimbusds.oauth2.sdk.token.Token;
import com.nimbusds.oauth2.sdk.token.TypelessToken;
import com.nimbusds.openid.connect.sdk.*;
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.jsoup.Jsoup;
import org.jsoup.Connection.KeyVal;
import com.nimbusds.jwt.JWTParser;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

import static com.nimbusds.openid.connect.sdk.OIDCScopeValue.OPENID;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class LTIService {

    protected final IDTokenValidator ltiIdTokenValidator;

    @Value("https://foo.bar/launch")
    protected URI redirectUri;

    @Value("https://${ultra.host}/learn/api/public/v1/oauth2/authorizationcode")
    protected URI oauthAuthorizationUri;

    @Value("${occam.lti.dev-bb}/api/v1/gateway/oidcauth")
    private URI ltiLaunchUri;

    @Value("${occam.client.learn.client-id}")
    protected ClientID oauthId;

    @Value("${occam.lti.application-id}")
    private ClientID ltiId;

    public LTIService(IDTokenValidator ltiIdTokenValidator) {
        this.ltiIdTokenValidator = ltiIdTokenValidator;
    }

    public void thirdPartyLogin(
            Issuer issuer,
            URI redirectUri,
            ClientID clientId,
            String loginHint,
            String ltiMessageHint) {
        if (
            !ltiIdTokenValidator.getExpectedIssuer().equals(issuer) ||
                !ltiIdTokenValidator.getClientID().equals(clientId) ||
                !this.redirectUri.equals(redirectUri)) {
            throw new ResponseStatusException(BAD_REQUEST);
        }

        final LTIUser ltiUser = ltiLogin(loginHint, ltiMessageHint);
        final AuthorizationCode oauthCode = getOauthCode(ltiUser.oneTimeSessionToken());
    }

    protected AuthorizationCode getOauthCode(Token oneTimeSessionToken) {
        final State expectedState = new State();
        // The initial oauth authentication request.
        // Normally the browser executes this and the user is prompted to give permission.
        // However the 'permission prompt' is disabled for this oauth application in ultra
        // and the 'one_time_session_token' allows us to start the request in the backend.
        AuthorizationRequest authorizationRequest = new AuthorizationRequest.Builder(ResponseType.CODE, oauthId)
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

    private LTIUser ltiLogin(String loginHint, String ltiMessageHint) {

        final State expectedState = new State();
        final Nonce expectedNonce = new Nonce();

        try {

            // The initial (and only) lti authentication request
            // Normally the browser executes this in an iframe, but without cookies we cannot verify the state or nonce parameter.
            // 'login_hint' and 'lti_message_hint) effectively act as one-time-session-token
            AuthenticationRequest authenticationRequest = new AuthenticationRequest.Builder(ResponseType.IDTOKEN, new Scope(OPENID), ltiId, redirectUri)
                    .endpointURI(ltiLaunchUri)
                    .responseMode(ResponseMode.FORM_POST)
                    .prompt(Prompt.Type.NONE)
                    .state(expectedState)
                    .nonce(expectedNonce)
                    .customParameter("login_hint", loginHint)
                    .customParameter("lti_message_hint", ltiMessageHint)
                    .build();

            // Self-posting form that blackboard uses...
            Map<String, String> formParams = Jsoup
                    .parse(authenticationRequest.toHTTPRequest().send().getBody())
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
                    new Subject(claimsSet.getJSONObjectClaim("https://purl.imsglobal.org/spec/lti/claim/custom").get("blackboard_userid").toString()),
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
