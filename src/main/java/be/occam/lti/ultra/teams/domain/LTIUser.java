package be.occam.lti.ultra.teams.domain;

import com.nimbusds.oauth2.sdk.id.Subject;
import com.nimbusds.oauth2.sdk.token.Token;

public record LTIUser(Subject userId, Token oneTimeSessionToken) {
}