package pl.edu.mimuw.cloudatlas.fetcher;

import pl.edu.mimuw.cloudatlas.api.Api;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import pl.edu.mimuw.cloudatlas.model.*;

public class Fetcher {
    private static final Map<String, Type.PrimaryType> fetcherAttributes = Map.ofEntries(
            Map.entry("cpu_load", Type.PrimaryType.DOUBLE),
            Map.entry("free_disk", Type.PrimaryType.INT),
            Map.entry("total_disk", Type.PrimaryType.INT),
            Map.entry("free_ram", Type.PrimaryType.INT),
            Map.entry("total_ram", Type.PrimaryType.INT),
            Map.entry("free_swap", Type.PrimaryType.INT),
            Map.entry("total_swap", Type.PrimaryType.INT),
            Map.entry("num_processes", Type.PrimaryType.INT),
            Map.entry("num_cores", Type.PrimaryType.INT),
            Map.entry("kernel_ver", Type.PrimaryType.STRING),
            Map.entry("logged_users", Type.PrimaryType.INT),
            Map.entry("dns_names", Type.PrimaryType.LIST)
    );

    private static final List<String> fetcherAttributeNames = List.of(
            "avg_load",
            "free_disk",
            "total_disk",
            "free_ram",
            "total_ram",
            "free_swap",
            "total_swap",
            "num_processes",
            "num_cores",
            "kernel_ver",
            "logged_users",
            "dns_names"
    );

    private static final List<Type.PrimaryType> fetcherAttributeTypes = List.of(
            Type.PrimaryType.DOUBLE,
            Type.PrimaryType.INT,
            Type.PrimaryType.INT,
            Type.PrimaryType.INT,
            Type.PrimaryType.INT,
            Type.PrimaryType.INT,
            Type.PrimaryType.INT,
            Type.PrimaryType.INT,
            Type.PrimaryType.INT,
            Type.PrimaryType.STRING,
            Type.PrimaryType.INT,
            Type.PrimaryType.LIST
    );

    private static Api api;
    private static Process pythonProcess;

    private static Value packAttributeValue(Object rawValue, Type.PrimaryType valueType) {
        Value val = null;
        ArrayList<Value> contacts = new ArrayList<Value>();

        if (valueType.equals(Type.PrimaryType.STRING)) {
            val = new ValueString((String) rawValue);
        } else if (valueType.equals(Type.PrimaryType.INT)) {
            val = new ValueInt(((Double) rawValue).longValue());
        } else if (valueType.equals(Type.PrimaryType.DOUBLE)) {
            val = new ValueDouble((Double) rawValue);
        } else if (valueType.equals(Type.PrimaryType.LIST)) {
            for (Object c : (ArrayList) rawValue) {
                contacts.add(new ValueString((String) c));
            }
            val = new ValueList(contacts, TypePrimitive.STRING);
        } else {
            throw new UnsupportedOperationException();
        }

        return val;
    }
    
    private static void initializeApiStub() throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry("localhost");
        api = (Api) registry.lookup("Api");
        System.out.println("Fetcher runs with registry");
    }

    private static void initializePythonProcess() throws IOException {
        String pythonScript = Fetcher.class.getResource("data_fetcher.py").getFile();
        String pythonCmd = "/usr/bin/python3 " + pythonScript;
        System.out.println("Run cmd: " + pythonCmd);
        pythonProcess = Runtime.getRuntime().exec(pythonCmd);
        System.out.println("Fetcher pid " + ProcessHandle.current().pid());
        System.out.println("Python process running with PID: " + pythonProcess.pid());
    }

    private static ArrayList deserializeAttribs(String serializedAttribs) {
        Gson g = new Gson();
        return g.fromJson(serializedAttribs, ArrayList.class);
    }

    // https://jj09.net/interprocess-communication-python-java/
    private static void fetchData() {
        BufferedReader bufferRead;
        ArrayList deserializedAttribs;
        String jsonAttribs;

        System.out.println(System.getProperty("user.dir"));

        try {
            initializeApiStub();
            initializePythonProcess();

            bufferRead = new BufferedReader( new InputStreamReader(pythonProcess.getInputStream()));

            while((jsonAttribs = bufferRead.readLine()) != null) {
                System.out.println(jsonAttribs);
                System.out.flush();
                deserializedAttribs = deserializeAttribs(jsonAttribs);
                for (int i = 0; i < fetcherAttributeNames.size(); i++) {
                    api.setAttributeValue(
                            "/",
                            fetcherAttributeNames.get(i),
                            packAttributeValue(
                                    deserializedAttribs.get(i),
                                    fetcherAttributeTypes.get(i)));
                }
            }

            bufferRead.close();

        } catch (Exception e) {
            System.err.println("Fetcher exception:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        fetchData();
    }
}
