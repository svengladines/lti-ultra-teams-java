package be.occam.lti.ultra.teams.web.dto;

public class MeetingDTO {
    protected  String id;
    protected String subject;
    protected String organizer;
    protected String joinUrl;
    protected String jwt;

    public String getSubject() {
        return subject;
    }

    public MeetingDTO setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public String getOrganizer() {
        return organizer;
    }

    public MeetingDTO setOrganizer(String organizer) {
        this.organizer = organizer;
        return this;
    }

    public String getJwt() {
        return jwt;
    }

    public MeetingDTO setJwt(String jwt) {
        this.jwt = jwt;
        return this;
    }

    public String getJoinUrl() {
        return joinUrl;
    }

    public MeetingDTO setJoinUrl(String joinUrl) {
        this.joinUrl = joinUrl;
        return this;
    }

    public String getId() {
        return id;
    }

    public MeetingDTO setId(String id) {
        this.id = id;
        return this;
    }
}
