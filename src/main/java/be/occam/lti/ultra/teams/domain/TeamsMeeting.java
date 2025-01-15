package be.occam.lti.ultra.teams.domain;

import java.net.URL;

public class TeamsMeeting {

    protected String id;
    protected String subject;
    protected String joinURL;
    protected URL url;

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

    public URL url() {
        return url;
    }

    public TeamsMeeting url(URL url) {
        this.url = url;
        return this;
    }
}
