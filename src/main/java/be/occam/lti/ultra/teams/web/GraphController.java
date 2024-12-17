package be.occam.lti.ultra.teams.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GraphController {
    public static final String PATH = "/graphed";

    public String graphed() {
        return "redirect:ok";
    }
}
