package pl.edu.mimuw.cloudatlas.client;

import com.google.gson.Gson;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import pl.edu.mimuw.cloudatlas.api.Api;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueContact;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
        boolean success = true;

        try {
            this.api.installQuery(queryObject.getName(), queryObject.getValue());
        } catch (Exception e) {
            success = false;
            System.err.println("Client exception:");
            e.printStackTrace();
        }

        if (success) {
            model.addAttribute(
                    "homeMessage",
                    "Query submitted successfully");
        } else {
            model.addAttribute(
                    "homeMessage",
                    "Query submission failed with a remote exception");
        }

        return "home";
    }

    @GetMapping("/contacts")
    public String contactPage(Model model) {
        model.addAttribute("contactsObject" , new ContactsString());
        return "contactsForm";
    }

    private Set<ValueContact> parseContactsString(ContactsString contactsInput) throws Exception {
        Gson gson = new Gson();
        Map<String, ArrayList> contactStrings = gson.fromJson(contactsInput.getString(), Map.class);
        Set<ValueContact> contactObjects = new HashSet<ValueContact>();
        ArrayList<Double> cAddr;
        byte[] inetArray = new byte[4];

        for (Map.Entry<String, ArrayList> cursor : contactStrings.entrySet()) {
            cAddr = cursor.getValue(); // gson always reads numerical values as doubles
            for (int i = 0; i < 4; i++) {
                inetArray[i] = (byte) cAddr.get(i).doubleValue();
            }
            contactObjects.add(new ValueContact(
                    new PathName(cursor.getKey()),
                    InetAddress.getByAddress(inetArray))
            );
        }

        return contactObjects;
    }

    @PostMapping("/contacts")
    public String contactPage(@ModelAttribute ContactsString contactsObject, Model model) {
        boolean success = true;
        Set<ValueContact> contactObjects;

        try {
            contactObjects = parseContactsString(contactsObject);
            this.api.setFallbackContacts(contactObjects);
        } catch (Exception e) {
            success = false;
            System.err.println("Client exception:");
            e.printStackTrace();
        }

        if (success) {
            model.addAttribute("homeMessage", "Contact list submitted successfully");
        } else {
            model.addAttribute("homeMessage", "Contact list submission failed");
        }

        return "home";
    }

    @GetMapping("/attribs")
    public String attribPage(Model model) {
        model.addAttribute("attributeObject", new Attribute());
        return "attribForm";
    }

    @PostMapping("/attribs")
    public String attribPage(@ModelAttribute Attribute attributeObject, Model model) {
        return "attribForm";
    }

    @GetMapping("/values")
    public String valuesPage(Model model) {
        return "attribChart";
    }
}
