package edu.tcu.cs.projectpulse.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Forwards all non-API, non-static routes to index.html so Vue Router
 * can handle client-side navigation (HTML5 history mode).
 */
@Controller
public class SpaController {

    @RequestMapping(value = {
            "/{path:[^\\.]*}",
            "/{path:[^\\.]*}/{subpath:[^\\.]*}"
    })
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
