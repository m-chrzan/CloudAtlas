package pl.edu.mimuw.cloudatlas.fetcher;

import pl.edu.mimuw.cloudatlas.api.Api;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

public class Fetcher {

//    private static String processAttribs(String jsonAttribs) {
//        Serializer serializer = new Serializer();
//        return
//    }

    // https://jj09.net/interprocess-communication-python-java/
    private static void fetchData() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            Api stub = (Api) registry.lookup("Api");
            System.out.println("Fetcher runs with registry"); // TODO

            String pythonScript = Fetcher.class.getResource("data_fetcher.py").getFile();
            String pythonCmd = "/usr/bin/python3 " + pythonScript;
            System.out.println("cmd: " + pythonCmd);
            Process p = Runtime.getRuntime().exec(pythonCmd);
            BufferedReader bufferRead = new BufferedReader( new InputStreamReader(p.getInputStream()));
            BufferedReader errorRead = new BufferedReader( new InputStreamReader(p.getErrorStream()));


            System.out.println("Gonna read some attribs");
            String jsonAttribs = bufferRead.readLine();
            String serializedAttribs;

            System.out.println("Read some attribs");
            System.out.println(jsonAttribs);
            System.out.println("Got some attribs");

            ArrayList aa = deserializeAttribs(jsonAttribs);
            System.out.println(aa);

            // TODO different condition
            while(!jsonAttribs.equals("x")) {
                System.out.println(jsonAttribs);
                System.out.flush();
                serializedAttribs = "1";
                // stub.setAttributeValue(serializedAttribs);
                jsonAttribs = bufferRead.readLine();
            }

            bufferRead.close();

        } catch(IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Fetcher exception:");
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        fetchData();
    }

    public static ArrayList deserializeAttribs(String serializedAttribs) {
        Gson g = new Gson();

        return g.fromJson(serializedAttribs, ArrayList.class);
    }
}
