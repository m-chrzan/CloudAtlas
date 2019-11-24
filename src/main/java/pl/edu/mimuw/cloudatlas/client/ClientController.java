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
    private static final int MAX_ENTRIES = 10;

    ClientController() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            this.api = (Api) registry.lookup("Api");
        } catch (Exception e) {
            System.err.println("Client exception:");
            e.printStackTrace();
        }

        this.attributes = new LinkedHashMap<ValueTime, AttributesMap>() {
            protected boolean removeEldestEntry(Map.Entry<ValueTime, AttributesMap> eldest) {
                return size() > MAX_ENTRIES;
            }
        };
        this.currentZoneName = "/uw/violet07";
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

    private List<Value> parseCollectionAttributeValue(List values, ArrayList<String> types) throws Exception {
        List<Value> resultValue = new ArrayList<Value>();
        String currentTypeString = types.get(1);
        AttributeInput attributeInput = new AttributeInput();
        ArrayList<String> newTypes = new ArrayList<>(types.subList(1, types.size()));
        Gson gson = new Gson();

        for (int i = 0; i < values.size(); i++) {
            if (currentTypeString.equals("List")) {
                resultValue.add(parseListAttributeValue((List) values.get(i), newTypes));
            } else if (currentTypeString.equals("Set")) {
                resultValue.add(parseSetAttributeValue((List) values.get(i), newTypes));
            } else {
                attributeInput.setAttributeType(currentTypeString);
                attributeInput.setValueString(gson.toJson(values.get(i)));
                resultValue.add(parseAttributeValue(attributeInput));
            }
        }

        return resultValue;
    }

    private Value parseListAttributeValue(List values, ArrayList<String> types) throws Exception {
        List<Value> listResultValue = parseCollectionAttributeValue(values, types);
        ArrayList<Value> resultValue = new ArrayList<>(listResultValue);

        return new ValueList(resultValue, resultValue.iterator().next().getType());
    }

    private Value parseSetAttributeValue(List values, ArrayList<String> types) throws Exception {
        List<Value> listResultValue = parseCollectionAttributeValue(values, types);
        HashSet<Value> resultValue = new HashSet<>(listResultValue);

        return new ValueSet(resultValue, resultValue.iterator().next().getType());
    }

    private Value parseAttributeValue(AttributeInput attributeObject) throws Exception {
        Gson gson = new Gson();
        Value attributeValue = null;

        switch (attributeObject.getAttributeType()) {
            case "Boolean":
                if (attributeObject.getValueString().toLowerCase().equals("true")) {
                    attributeValue = new ValueBoolean(true);
                } else if (attributeObject.getValueString().toLowerCase().equals("false")) {
                    attributeValue = new ValueBoolean(false);
                } else {
                    String errMsg = "Incorrect boolean value: " + attributeObject.getValueString();
                    throw new UnsupportedOperationException(errMsg);
                }
                break;
            case "Double":
                attributeValue = new ValueDouble(Double.parseDouble(attributeObject.getValueString()));
                break;
            case "Int":
                Double tempDouble = Double.parseDouble(attributeObject.getValueString());
                attributeValue = new ValueInt(tempDouble.longValue());
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
            case "List":
                List parsedListValue = gson.fromJson(attributeObject.getValueString(), List.class);
                ArrayList<String> parsedListTypes = new ArrayList<>(Arrays.asList(
                        attributeObject.getAttributeComplexType().replaceAll("\\s","").split(",")));
                attributeValue = parseListAttributeValue(parsedListValue, parsedListTypes);
                break;
            case "Set":
                List parsedSetValue = gson.fromJson(attributeObject.getValueString(), List.class);
                ArrayList<String> parsedSetTypes = new ArrayList<>(Arrays.asList(
                        attributeObject.getAttributeComplexType(). replaceAll("\\s","").split(",")));
                attributeValue = parseSetAttributeValue(parsedSetValue, parsedSetTypes);
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
    }

    private ArrayList getAllAttributeValues(AttributesMap attribs, Boolean justNumerical) {
        ArrayList valuesList = new ArrayList<>();
        Value val;

        for (Map.Entry<Attribute, Value> entry : attribs) {
            val = entry.getValue();
            // casting to ValueDouble and ValueInt caused some errors
            // and gson turns all numerical values into doubles anyway
            if (justNumerical && isValueNumerical(val)) {
                valuesList.add(Double.parseDouble(val.toString()));
            } else if (!justNumerical) {
                valuesList.add(val.toString());
            }
        }

        return valuesList;
    }

    private boolean isValueNumerical(Value val) {
        Type valType = val.getType();

        if (TypePrimitive.DOUBLE.isCompatible(valType) || TypePrimitive.INTEGER.isCompatible(valType)) {
            return true;
        } else {
            return false;
        }
    }

    private AttributesMap getLastAttributesMap() {
        ArrayList<Map.Entry<ValueTime, AttributesMap>> attribsMap = new ArrayList<>(this.attributes.entrySet());
        return attribsMap.get(attribsMap.size() - 1).getValue();
    }

    private ArrayList<String> getAttributesColumnNames(Boolean justNumerical) {
        ArrayList<String> chartValueNames = new ArrayList<>();
        AttributesMap lastAttribMap = getLastAttributesMap();

        for (Map.Entry<Attribute, Value> e : lastAttribMap) {
            if (!justNumerical || isValueNumerical(e.getValue())) {
                chartValueNames.add(e.getKey().getName());
            }
        }
        chartValueNames.add(0, "Timestamp");
        return chartValueNames;
    }

    // data format compatible with Google Charts Table and Google Line Chart input
    // but it's a generic 2d array table representation
    // https://developers.google.com/chart/interactive/docs/gallery/table
    private ArrayList<ArrayList> getValuesTable(Boolean justNumerical) {
        ArrayList valueRow;
        ArrayList<ArrayList> allValues = new ArrayList<>();
        ArrayList<String> valueNames = getAttributesColumnNames(justNumerical);

        for (Map.Entry<ValueTime, AttributesMap> attribMapEntry : this.attributes.entrySet()) {
            valueRow = getAllAttributeValues(attribMapEntry.getValue(), justNumerical);
            while (valueRow.size() < valueNames.size() - 1) {
                valueRow.add(null);
            }
            valueRow.add(0, attribMapEntry.getKey().toString().substring(11, 19));
            allValues.add(valueRow);
        }

        // optional trimming of table length
        // if (allValues.size() > 10) {
        //     allValues =  new ArrayList<ArrayList>(allValues.subList(allValues.size() - 11, allValues.size() - 1));
        // }
        allValues.add(0, valueNames);
        return allValues;
    }

    private String processAttribValues(ArrayList<ArrayList> valuesTable) {
        String jsonAttributes = "";
        Gson gson = new Gson();
        jsonAttributes = gson.toJson(valuesTable);
        // System.out.println(jsonAttributes);
        return jsonAttributes;
    }

    @GetMapping("/attribNumValues")
    @ResponseBody
    public String attribNumValuesApi() {
        return processAttribValues(getValuesTable(true));
    }

    @GetMapping("/attribAllValues")
    @ResponseBody
    public String attribAllValuesApi() {
        return processAttribValues(getValuesTable(false));
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
