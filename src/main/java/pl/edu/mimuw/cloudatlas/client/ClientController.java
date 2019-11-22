package pl.edu.mimuw.cloudatlas.client;

import com.google.gson.Gson;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import pl.edu.mimuw.cloudatlas.api.Api;
import pl.edu.mimuw.cloudatlas.model.*;

import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

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

    private Map<ValueTime, AttributesMap> attributes;
    private String currentZoneName;

    ClientController() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            this.api = (Api) registry.lookup("Api");
        } catch (Exception e) {
            System.err.println("Client exception:");
            e.printStackTrace();
        }

        this.attributes = new LinkedHashMap<ValueTime, AttributesMap>();
        this.currentZoneName = "/";
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
        model.addAttribute("contactsObject" , new DataStringInput());
        return "contactsForm";
    }

    private Set<ValueContact> parseContactsString(DataStringInput contactsInput) throws Exception {
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
    public String contactPage(@ModelAttribute DataStringInput contactsObject, Model model) {
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
        model.addAttribute("attributeObject", new AttributeInput());
        return "attribForm";
    }

    private Value parseAttributeValue(AttributeInput attributeObject) throws Exception {
        Value attributeValue = null;

        switch (attributeObject.getAttributeType()) {
            case "Boolean":
                attributeValue = attributeObject.getValueString().toLowerCase().equals("true") ?
                        new ValueBoolean(true) :
                        new ValueBoolean(false);
                break;
            case "Double":
                attributeValue = new ValueDouble(Double.parseDouble(attributeObject.getValueString()));
                break;
            case "Int":
                attributeValue = new ValueInt(Long.parseLong(attributeObject.getValueString()));
                break;
            case "String":
                attributeValue = new ValueString(attributeObject.getValueString());
                break;
            case "Time":
                attributeValue = new ValueTime(Long.parseLong(attributeObject.getValueString()));
                break;
            case "Duration":
                attributeValue = new ValueDuration(attributeObject.getValueString());
                break;
            case "Contact":
                DataStringInput contactsString = new DataStringInput();
                contactsString.setString(attributeObject.getValueString());
                attributeValue = parseContactsString(contactsString).iterator().next();
                break;
            case "Query":
                attributeValue = new ValueQuery(attributeObject.getValueString());
                break;
            default:
                String errMsg = "Value type not supported: " + attributeObject.getAttributeType();
                throw new UnsupportedOperationException(errMsg);
        }

        return attributeValue;
    }

    @PostMapping("/attribs")
    public String attribPage(@ModelAttribute AttributeInput attributeObject, Model model) {
        boolean success = true;
        Value attributeValue;

        try {
            attributeValue = parseAttributeValue(attributeObject);
            api.setAttributeValue(
                    attributeObject.getZoneName(),
                    attributeObject.getAttributeName(),
                    attributeValue);
        } catch (Exception e) {
            success = false;
            System.err.println("Client exception:");
            e.printStackTrace();
        }

        if (success) {
            model.addAttribute("homeMessage", "Attribute submitted successfully");
        } else {
            model.addAttribute("homeMessage", "Attribute submission failed");
        }

        return "home";
    }

    private String getAvailableZonesString() {
        boolean success = true;
        Set<String> availableZones;
        String availableZonesString = "";

        try {
            availableZones = api.getZoneSet();
            availableZonesString = availableZones.toString().substring(1, availableZones.toString().length() - 1);
        } catch (Exception e) {
            success = false;
            System.err.println("Client exception:");
            e.printStackTrace();
        }

        if (success) {
            return "Available zones are: " + availableZonesString;
        } else {
            return "No zones available, error occured during fetch";
        }
    }

    @GetMapping("/values")
    public String valuesPage(Model model) {
        model.addAttribute("availableZones", getAvailableZonesString());
        model.addAttribute("currentZone", "Current zone: " + this.currentZoneName);
        model.addAttribute("zoneName", new DataStringInput());
        return "attribChart";
    }

    @Scheduled(fixedRate = 5000)
    private void fetchAttributeData() {
        AttributesMap attribData;
        ValueTime currentTime;

        try {
            if (!this.currentZoneName.isEmpty()) {
                attribData = api.getZoneAttributeValues(this.currentZoneName);
                currentTime = new ValueTime(System.currentTimeMillis());
                this.attributes.put(currentTime, attribData);
            }
        } catch (Exception e) {
            System.err.println("Client exception:");
            e.printStackTrace();
        }

        Iterator<ValueTime> it = this.attributes.keySet().iterator();
        while (it.hasNext() && this.attributes.size() > 50) {
            it.next();
            it.remove();
        }
    }

    private ArrayList getNumericalAttributeValue(AttributesMap attribs) {
        Value val;
        Type valType;
        ArrayList valuesList = new ArrayList<>();

        for (Map.Entry<Attribute, Value> entry : attribs) {
            val = entry.getValue();
            valType = val.getType();
            if (TypePrimitive.DOUBLE.isCompatible(valType)) {
                valuesList.add(Double.parseDouble(val.toString()));
            } else if (TypePrimitive.INTEGER.isCompatible(valType)) {
                valuesList.add(Long.parseLong(val.toString()));
            } else if (TypePrimitive.TIME.isCompatible(valType)) {
                valuesList.add(Long.parseLong(val.toString()));
            } else if (TypePrimitive.DURATION.isCompatible(valType)) {
                valuesList.add(Long.parseLong(val.convertTo(TypePrimitive.INTEGER).toString()));
            }
        }
        return valuesList;
    }

    private boolean isValueNumerical(Value val) {
        Type valType = val.getType();

        if (TypePrimitive.DOUBLE.isCompatible(valType) ||
                TypePrimitive.INTEGER.isCompatible(valType) ||
                TypePrimitive.TIME.isCompatible(valType) ||
                TypePrimitive.DURATION.isCompatible(valType)) {
            return true;
        } else {
            return false;
        }
    }

    private AttributesMap getLastAttributesMap() {
        ArrayList<Map.Entry<ValueTime, AttributesMap>> attribsMap = new ArrayList<>(this.attributes.entrySet());
        return attribsMap.get(attribsMap.size() - 1).getValue();
    }

    private ArrayList<String> getNumericalColumnNames() {
        ArrayList<String> chartValueNames = new ArrayList<>();
        AttributesMap lastAttribMap = getLastAttributesMap();

        for (Map.Entry<Attribute, Value> e : lastAttribMap) {
            if (isValueNumerical(e.getValue())) {
                chartValueNames.add(e.getKey().getName());
            }
        }
        chartValueNames.add(0, "Timestamp");
        return chartValueNames;
    }

    // data format compatible with Google Line Chart
    // https://developers.google.com/chart/interactive/docs/gallery/linechart
    private ArrayList<ArrayList> getNumericalValuesTable() {
        ArrayList<ArrayList> chartValues = new ArrayList<>();
        ArrayList<String> chartValueNames = getNumericalColumnNames();
        ArrayList chartValueColumn;

        System.out.println(this.attributes);
        for (Map.Entry<ValueTime, AttributesMap> attribsMap : this.attributes.entrySet()) {
            chartValueColumn = getNumericalAttributeValue(attribsMap.getValue());
            chartValueColumn.add(0, attribsMap.getKey().toString().substring(11, 19));
            while (chartValueColumn.size() < chartValueNames.size()) {
                chartValueColumn.add(null);
            }
            chartValues.add(chartValueColumn);
        }

        chartValues.add(0, chartValueNames);
        return chartValues;
    }

    private String processAttribNumValues() {
        String jsonAttributes = "";
        Gson gson = new Gson();
        ArrayList<ArrayList> chartValues = getNumericalValuesTable();
        jsonAttributes = gson.toJson(chartValues);
        System.out.println(jsonAttributes);
        return jsonAttributes;
    }

    @GetMapping("/attribNumValues")
    @ResponseBody
    public String attribNumValuesApi() {
        return processAttribNumValues();
    }

    @PostMapping("/values")
    public String valuesPage(@ModelAttribute DataStringInput zoneName, Model model) {
        this.currentZoneName = zoneName.getString();
        this.attributes.clear();
        model.addAttribute("currentZone", "Current zone: " + this.currentZoneName);
        model.addAttribute("availableZones", getAvailableZonesString());
        model.addAttribute("zoneName", new DataStringInput());
        return "attribChart";
    }
}
