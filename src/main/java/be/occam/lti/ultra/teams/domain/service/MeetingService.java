package be.occam.lti.ultra.teams.domain.service;

import be.occam.lti.ultra.teams.infrastructure.microsoft.GraphClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MeetingService {

    protected final GraphClient graphClient;

    @Autowired
    public MeetingService(GraphClient graphClient) {
        this.graphClient = graphClient;
    }

    public void createMeeting(String organizer, String subject) {
        this.graphClient.createMeeting(organizer, subject);
    }

}
