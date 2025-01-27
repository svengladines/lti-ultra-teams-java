package be.occam.lti.ultra.teams.domain.service;

import be.occam.lti.ultra.teams.config.SystemProperties;
import be.occam.lti.ultra.teams.domain.LTIUser;
import be.occam.lti.ultra.teams.domain.TeamsMeeting;
import be.occam.lti.ultra.teams.infrastructure.microsoft.GraphClient;
import com.azure.core.util.UrlBuilder;
import com.microsoft.graph.models.OnlineMeeting;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;

@Service
public class MeetingService {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final GraphClient graphClient;
    protected final LTIService ltiService;
    protected final SystemProperties systemProperties;

    @Autowired
    public MeetingService(GraphClient graphClient, LTIService ltiService, SystemProperties systemProperties) {
        this.graphClient = graphClient;
        this.ltiService = ltiService;
        this.systemProperties = systemProperties;
    }

    public TeamsMeeting create(String organizerEmail, String subject, HttpServletRequest httpServletRequest) {
        logger.info("User [{}]; create teams meeting with subject [{}]...", organizerEmail, subject);
        TeamsMeeting meeting = onlineToTeamsMeeting(this.graphClient.createMeeting(organizerEmail, subject));
        meeting.url(meetingURL(meeting));
        logger.info("User [{}]; ... teams meeting created with  id [{}], subject [{}] and  join url [{}]", organizerEmail, meeting.id(), meeting.subject(), meeting.joinURL());
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
            return UrlBuilder.parse("%s/api/meetings/%s.html"
                    .formatted(this.systemProperties.baseURL(), teamsMeeting.id())).toUrl();
            // joinURL is interpreted as link to Teams Classes.... return UrlBuilder.parse(teamsMeeting.joinURL()).toUrl();
        }
        catch(MalformedURLException e) {
            // TODO, better exception handling
            throw new RuntimeException(e);
        }
    }

}
