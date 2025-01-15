package be.occam.lti.ultra.teams.web.controller;

import be.occam.lti.ultra.teams.domain.service.JWKSetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class JWKSetController {

    protected final JWKSetService jwkSetService;

    public JWKSetController(JWKSetService jwkSetService) {
        this.jwkSetService = jwkSetService;
    }

    @GetMapping(value = "/jwks", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> get() {
        // we cannot use JWKSet entities, as they do not produce the correct JSON
        return new ResponseEntity<>(jwkSetService.get().toString(), HttpStatus.OK);
    }

}
