package be.occam.lti.ultra.teams.domain.service;

import be.occam.lti.ultra.teams.config.SystemProperties;
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
import java.util.Optional;

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

    public Optional<TeamsMeeting> create(String organizerEmail, String subject, HttpServletRequest httpServletRequest) {
        logger.info("User [{}]; create teams meeting with subject [{}]...", organizerEmail, subject);
        Optional<TeamsMeeting> meeting = map(this.graphClient.createMeeting(organizerEmail, subject));
        meeting.ifPresent(m -> {
            m.organizer(organizerEmail);
            m.url(meetingURL(m));
            logger.info("User [{}]; ... teams meeting created with  id [{}], subject [{}] and join url [{}]", organizerEmail, m.id(), m.subject(), m.joinURL());
        });
        return meeting;
    }

    public Optional<TeamsMeeting> get(String organizer, String id) {
        return map(this.graphClient.getMeeting(organizer,id)).map(m -> m.organizer(organizer));
    }

    protected Optional<TeamsMeeting> map(OnlineMeeting onlineMeeting) {
        if (onlineMeeting == null ) {
            return Optional.empty();
        }
        else {
            return Optional.of(new TeamsMeeting()
                    .id(onlineMeeting.getId())
                    .joinURL(onlineMeeting.getJoinWebUrl())
                    .subject(onlineMeeting.getSubject()));
        }
    }

    protected URL meetingURL(TeamsMeeting teamsMeeting) {
        try {
            return UrlBuilder.parse("%s/api/meetings/%s/%s.html"
                    .formatted(this.systemProperties.baseURL(), teamsMeeting.organizer(), teamsMeeting.id())).toUrl();
            // joinURL is interpreted as link to Teams Classes.... return UrlBuilder.parse(teamsMeeting.joinURL()).toUrl();
        }
        catch(MalformedURLException e) {
            // TODO, better exception handling
            throw new RuntimeException(e);
        }
    }

}
