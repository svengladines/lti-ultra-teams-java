package be.occam.lti.ultra.teams.web.controller;

import be.occam.lti.ultra.teams.domain.service.LTIService;
import be.occam.lti.ultra.teams.domain.service.MeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {

    public static final String RESOURCE_PATH = "/pages/{area}/{page}.html";

    protected final LTIService ltiService;
    protected final MeetingService meetingService;

    @Autowired
    public PageController(LTIService ltiService, MeetingService meetingService) {
        this.ltiService = ltiService;
        this.meetingService = meetingService;
    }

    @GetMapping(value = RESOURCE_PATH)
    public String get(
            @PathVariable("area") String area,
            @PathVariable("page") String page
    ) {
        return "%s/%s".formatted(area,page);
    }
}
