package be.occam.lti.ultra.teams.infrastructure.microsoft;

import com.microsoft.graph.models.OnlineMeeting;
import com.microsoft.graph.models.User;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class GraphClient {

    protected final GraphServiceClient graphServiceClient;

    @Autowired
    public GraphClient(GraphServiceClient graphServiceClient) {
        this.graphServiceClient = graphServiceClient;
    }

    public void createMeeting(String organizer, String subject) {

        OnlineMeeting onlineMeeting = new OnlineMeeting();
        onlineMeeting.setSubject(subject);
        onlineMeeting.setStartDateTime(OffsetDateTime.parse("2024-12-15T10:00:00Z"));

        User user = this.graphServiceClient.usersWithUserPrincipalName(organizer).get();

        OnlineMeeting createdMeeting = this.graphServiceClient.users().byUserId(user.getId())
                .onlineMeetings()
                .post(onlineMeeting);
    }

}
