package be.occam.lti.ultra.teams.infrastructure.microsoft;

import com.microsoft.graph.models.*;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class GraphClient {

    protected final GraphServiceClient graphServiceClient;
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public GraphClient(GraphServiceClient graphServiceClient) {
        this.graphServiceClient = graphServiceClient;
    }

    public OnlineMeeting createMeeting(String organizer, String subject) {

        OnlineMeeting onlineMeeting = new OnlineMeeting();
        onlineMeeting.setSubject(subject);
        onlineMeeting.setStartDateTime(OffsetDateTime.parse("2024-12-15T10:00:00Z"));
        LobbyBypassSettings lobbyBypassSettings = new LobbyBypassSettings();
        lobbyBypassSettings.setScope(LobbyBypassScope.Invited);
        onlineMeeting.setLobbyBypassSettings(lobbyBypassSettings);
        User user = this.graphServiceClient.usersWithUserPrincipalName(organizer).get();
        OnlineMeeting createdMeeting = this.graphServiceClient.users().byUserId(user.getId())
                .onlineMeetings()
                .post(onlineMeeting);
        log.info("User [{}]; created online meeting [{}]", user.getId(), createdMeeting);
        return createdMeeting;

    }

    public OnlineMeeting getMeeting(String organizer, String id) {
        User user = this.graphServiceClient.usersWithUserPrincipalName(organizer).get();
        OnlineMeeting meeting = this.graphServiceClient.users().byUserId(user.getId())
                .onlineMeetings()
                .byOnlineMeetingId(id).get();
        log.info("User [{}]; got online meeting with id [{}] and subject [{}]", user.getId(), meeting.getId(), meeting.getSubject() );
        return meeting;
    }

    public OnlineMeeting addParticipant(String organizer, String meetingId, String participant) {
        User oUser = this.graphServiceClient.usersWithUserPrincipalName(organizer).get();
        User pUser = this.graphServiceClient.usersWithUserPrincipalName(participant).get();
        OnlineMeeting existing  = this.getMeeting(organizer,meetingId);
        MeetingParticipants existingParticipants = existing.getParticipants();
        MeetingParticipantInfo participantInfo = new MeetingParticipantInfo();
        participantInfo.setUpn(participant);
        participantInfo.setRole(OnlineMeetingRole.Attendee);
        existingParticipants.getAttendees().add(participantInfo);
        OnlineMeeting patchMeeting = new OnlineMeeting();
        patchMeeting.setParticipants(existingParticipants);
        return this.graphServiceClient.users().byUserId(oUser.getId())
                .onlineMeetings()
                .byOnlineMeetingId(meetingId)
                .patch(patchMeeting);

    }

}
