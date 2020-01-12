package pl.edu.mimuw.cloudatlas.fetcher;

import pl.edu.mimuw.cloudatlas.api.Api;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import pl.edu.mimuw.cloudatlas.model.*;

public class Fetcher {
    private static String host;
    private static int port;

    private static final List<String> fetcherAttributeNames = Arrays.asList(
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

    private static final List<Type.PrimaryType> fetcherAttributeTypes = Arrays.asList(
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
        Registry registry = LocateRegistry.getRegistry(host, port);
        api = (Api) registry.lookup("Api");
        System.out.println("Fetcher runs with registry");
    }

    private static void initializePythonProcess() throws IOException {
        String pythonScript = Fetcher.class.getResource("data_fetcher.py").getFile();
        String pythonCmd = "/usr/bin/python3 " + pythonScript;
        System.out.println("Run cmd: " + pythonCmd);
        pythonProcess = Runtime.getRuntime().exec(pythonCmd);
    }

    private static ArrayList deserializeAttribs(String serializedAttribs) {
        Gson g = new Gson();
        return g.fromJson(serializedAttribs, ArrayList.class);
    }

    // https://jj09.net/interprocess-communication-python-java/
    private static void fetchData(String zonePath) {
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
                            zonePath,
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

    private static void parseArgs(String[] args) {
        System.out.println("args length: " + args.length);
        if (args.length < 2) {
            port = 1099;
        } else {
            port = Integer.parseInt(args[1]);
        }

        if (args.length < 1) {
            host = "localhost";
        } else {
            host = args[0];
        }
    }

    public static void main(String[] args) {
        String zonePath = System.getProperty("zone_path");
        parseArgs(args);
        fetchData(zonePath);
    }
}
