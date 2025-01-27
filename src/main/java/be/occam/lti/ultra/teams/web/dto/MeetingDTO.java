package be.occam.lti.ultra.teams.web.dto;

public class MeetingDTO {
    protected String subject;
    protected String userEmail;
    protected String joinUrl;
    protected String jwt;

    public String getSubject() {
        return subject;
    }

    public MeetingDTO setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public MeetingDTO setUserEmail(String userEmail) {
        this.userEmail = userEmail;
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
}
