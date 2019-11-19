package pl.edu.mimuw.cloudatlas.client;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/*
should enable reading attribute values stored by the agent
installing and
uninstalling queries, and
setting fallback contacts.

Apart from providing forms for queries and fallback contacts,
and presenting the information fetched from the agent in a textual form (with automatic refreshment),
plotting the attributes with numeric values as real-time graphs.
*/

@RestController
public class Controller {

    @GetMapping("/")
    public String homePage(Model model) {
        return "home";
    }
}
