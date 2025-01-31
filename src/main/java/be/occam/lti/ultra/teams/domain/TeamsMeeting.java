package be.occam.lti.ultra.teams.domain;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TeamsMeeting {

    protected String id;
    protected String organizer;
    protected String subject;
    protected String joinURL;
    protected URL url;
    final protected List<String> participants;

    public TeamsMeeting() {
        this.participants = new ArrayList<>();
    }

    public String id() {
        return id;
    }

    public TeamsMeeting id(String id) {
        this.id = id;
        return this;
    }

    public String joinURL() {
        return joinURL;
    }

    public TeamsMeeting joinURL(String joinURL) {
        this.joinURL = joinURL;
        return this;
    }

    public String subject() {
        return subject;
    }

    public TeamsMeeting subject(String subject) {
        this.subject = subject;
        return this;
    }

    public String organizer() {
        return organizer;
    }

    public TeamsMeeting organizer(String organizer) {
        this.organizer = organizer;
        return this;
    }

    public URL url() {
        return url;
    }

    public TeamsMeeting url(URL url) {
        this.url = url;
        return this;
    }

    public List<String> participants() {
        return participants;
    }
}
