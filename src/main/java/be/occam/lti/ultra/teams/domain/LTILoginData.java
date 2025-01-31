package be.occam.lti.ultra.teams.domain;

import java.net.URI;

public record LTILoginData(String state, String nonce, URI redirectUri) {
}
