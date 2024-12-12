package be.occam.lti.ultra.teams.web;

import be.occam.lti.ultra.teams.domain.service.LTIService;
import be.occam.lti.ultra.teams.domain.service.MeetingService;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.nio.charset.Charset;
import java.security.Principal;

@Controller
public class MeetingController {

    public static final String PATH = "/meeting";
    public static final String PATH_LOCAL = "/meetingLocal";

    protected final MeetingService meetingService;

    @Autowired
    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @PostMapping(value = PATH)
    public ResponseEntity<String> launch() {
        MultiValueMap<String,String> headers = new HttpHeaders();
        headers.add("Location", "/meetingLocal");
        return new ResponseEntity<>(headers,HttpStatus.TEMPORARY_REDIRECT);
    }

    @PostMapping(value = PATH_LOCAL)
    public ResponseEntity<String> launchLocal(
            @RequestParam("id_token") String idToken,
            @RequestParam("state") String state,
            Principal principal) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
