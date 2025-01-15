package be.occam.lti.ultra.teams.domain.service;

import be.occam.lti.ultra.teams.config.SystemProperties;
import be.occam.lti.ultra.teams.domain.LTIUser;
import be.occam.lti.ultra.teams.domain.TeamsMeeting;
import be.occam.lti.ultra.teams.infrastructure.microsoft.GraphClient;
import com.azure.core.util.UrlBuilder;
import com.microsoft.graph.models.OnlineMeeting;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;

@Service
public class MeetingService {

    protected final GraphClient graphClient;
    protected final LTIService ltiService;
    protected final SystemProperties systemProperties;

    @Autowired
    public MeetingService(GraphClient graphClient, LTIService ltiService, SystemProperties systemProperties) {
        this.graphClient = graphClient;
        this.ltiService = ltiService;
        this.systemProperties = systemProperties;
    }

    public TeamsMeeting create(LTIUser organizer, String subject, HttpServletRequest httpServletRequest) {
        TeamsMeeting meeting = onlineToTeamsMeeting(this.graphClient.createMeeting(organizer.email(), subject));
        meeting.url(meetingURL(meeting));
        return meeting;
    }

    public TeamsMeeting get(String id) {
        return null;
    }

    protected TeamsMeeting onlineToTeamsMeeting(OnlineMeeting onlineMeeting) {
        return new TeamsMeeting()
                .id(onlineMeeting.getExternalId())
                .joinURL(onlineMeeting.getJoinWebUrl())
                .subject(onlineMeeting.getSubject());
    }

    protected URL meetingURL(TeamsMeeting teamsMeeting) {
        try {
            /* TODO: use real url, not join url
            return UrlBuilder.parse("%s/meetings/%s"
                    .formatted(this.systemProperties.baseURL(), teamsMeeting.id())).toUrl();
             */
            return UrlBuilder.parse(teamsMeeting.joinURL()).toUrl();
        }
        catch(MalformedURLException e) {
            // TODO, better exception handling
            throw new RuntimeException(e);
        }
    }

}
