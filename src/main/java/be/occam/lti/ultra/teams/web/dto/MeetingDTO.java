package be.occam.lti.ultra.teams.web.dto;

public class MeetingDTO {
    protected String subject;
    protected String userEmail;

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
}
