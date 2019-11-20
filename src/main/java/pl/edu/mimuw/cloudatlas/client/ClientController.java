package pl.edu.mimuw.cloudatlas.client;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import pl.edu.mimuw.cloudatlas.api.Api;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/*
should enable reading attribute values stored by the agent
installing and
uninstalling queries, and
setting fallback contacts.

Apart from providing forms for queries and fallback contacts,
and presenting the information fetched from the agent in a textual form (with automatic refreshment),
plotting the attributes with numeric values as real-time graphs.
*/

@Controller
public class ClientController {
    private Api api;

    ClientController() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            this.api = (Api) registry.lookup("Api");
        } catch (Exception e) {
            System.err.println("Client exception:");
            e.printStackTrace();
        }
    }

    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("homeMessage", "Welcome to CloudaAtlas client interface");
        return "home";
    }

    @GetMapping("/query")
    public String queryPage(Model model) {
        model.addAttribute("queryObject", new Query());
        return "queryForm";
    }

    @PostMapping("/query")
    public String submitQuery(@ModelAttribute Query queryObject, Model model)  {
        try {
            this.api.installQuery(queryObject.getName(), queryObject.getValue());
        } catch (Exception e) {
            System.err.println("Client exception:");
            e.printStackTrace();
        }

        model.addAttribute("homeMessage", "Query submitted successfully");
        return "home";
    }

    @GetMapping("/contacts")
    public String contactPage(Model model) {
        return "contactsForm";
    }

    @PostMapping("/contacts")
    public String contactPage(@ModelAttribute String contactsObject, Model model) {
        model.addAttribute("homeMessage", "Contact list submitted successfully");
        return "home";
    }

    @GetMapping("/attribs")
    public String attribPage(Model model) {
        return "attribChart";
    }
}
