package be.occam.lti.ultra.teams.domain.service;

import be.occam.lti.ultra.teams.domain.LTIUser;
import be.occam.lti.ultra.teams.infrastructure.microsoft.GraphClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
public class MeetingService {

    protected final GraphClient graphClient;
    protected final LTIService ltiService;

    @Autowired
    public MeetingService(GraphClient graphClient, LTIService ltiService) {
        this.graphClient = graphClient;
        this.ltiService = ltiService;
    }

    public String createMeeting(PreAuthenticatedAuthenticationToken organizer, String subject) {
        LTIUser ltiUser = (LTIUser) organizer.getDetails();
        return this.graphClient.createMeeting(ltiUser.email(), subject);
    }

}
