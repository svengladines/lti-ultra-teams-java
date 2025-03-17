package be.occam.lti.ultra.teams.web.controller;

import be.occam.lti.ultra.teams.domain.service.LTIService;
import be.occam.lti.ultra.teams.domain.service.MeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
//@Profile("local")
public class LocalController {

    protected final LTIService ltiService;
    protected final MeetingService meetingService;

    @Autowired
    public LocalController(LTIService ltiService, MeetingService meetingService) {
        this.ltiService = ltiService;
        this.meetingService = meetingService;
    }

    @GetMapping(value = "/local/meeting/create")
    public String get(
            @RequestParam("organizer") String organizer,
            Model model) {
        model.addAttribute("organizer", organizer);
        model.addAttribute("jwt", "xxx");
        return "/meeting/create";
    }
}
