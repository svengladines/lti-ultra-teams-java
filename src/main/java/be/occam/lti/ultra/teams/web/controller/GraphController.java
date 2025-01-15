package be.occam.lti.ultra.teams.web.controller;

import org.springframework.stereotype.Controller;

@Controller
public class GraphController {
    public static final String PATH = "/graphed";

    public String graphed() {
        return "redirect:ok";
    }
}
