package pl.edu.mimuw.cloudatlas.agent;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.hasItems;


import java.lang.Runtime;
import java.lang.Process;
import java.lang.Thread;

import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.math.BigDecimal;

import pl.edu.mimuw.cloudatlas.api.Api;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueDouble;
import pl.edu.mimuw.cloudatlas.model.ValueInt;
import pl.edu.mimuw.cloudatlas.model.ValueList;
import pl.edu.mimuw.cloudatlas.model.ValueNull;
import pl.edu.mimuw.cloudatlas.model.ValueString;
import pl.edu.mimuw.cloudatlas.model.ValueTime;

public class AgentTest {
    private static Process registryProcess;
    private static Process agentProcess;

    private static Registry registry;
    private static Api api;

    @BeforeClass
    public static void bindApi() throws Exception {
        registryProcess = Runtime.getRuntime().exec("./scripts/registry");
        Thread.sleep(10000);
        agentProcess = Runtime.getRuntime().exec("./gradlew runAgent");
        Thread.sleep(10000);

        registry = LocateRegistry.getRegistry("localhost");
        api = (Api) registry.lookup("Api");
    }

    @AfterClass
    public static void killProcesses() throws Exception {
        try {
            registryProcess.destroy();
            agentProcess.destroy();
        } catch (Exception e) {
            System.out.println("Caught exception: " + e);
        }
    }

    @Test
    public void testGetZoneSet() throws Exception {
        Set<String> set = api.getZoneSet();
        assertEquals(8, set.size());
        assertThat(set, hasItems("/"));
        assertThat(set, hasItems("/uw"));
        assertThat(set, hasItems("/uw/violet07", "/uw/khaki31", "/uw/khaki13"));
        assertThat(set, hasItems("/pjwstk"));
        assertThat(set, hasItems("/pjwstk/whatever01", "/pjwstk/whatever02"));
    }

    @Test
    public void testRootGetZoneAttributeValue() throws Exception {
        AttributesMap rootAttributes = api.getZoneAttributeValues("/");
        assertEquals(new ValueInt(0l), rootAttributes.get("level"));
        assertEquals(ValueNull.getInstance(), rootAttributes.get("name"));
    }

    @Test
    public void testIntermediateGetZoneAttributeValue() throws Exception {
        AttributesMap attributes = api.getZoneAttributeValues("/uw");
        assertEquals(new ValueInt(1l), attributes.get("level"));
        assertEquals(new ValueString("uw"), attributes.get("name"));
    }

    @Test
    public void testLeafGetZoneAttributeValue() throws Exception {
        AttributesMap attributes = api.getZoneAttributeValues("/pjwstk/whatever01");
        assertEquals(new ValueInt(2l), attributes.get("level"));
        assertEquals(new ValueString("whatever01"), attributes.get("name"));
        assertEquals(new ValueString("/pjwstk/whatever01"), attributes.get("owner"));
        assertEquals(new ValueTime("2012/11/09 21:12:00.000"), attributes.get("timestamp"));
        assertEquals(new ValueInt(1l), attributes.get("cardinality"));
        assertEquals(new ValueTime("2012/10/18 07:03:00.000"), attributes.get("creation"));
        assertEquals(new ValueDouble(0.1), attributes.get("cpu_usage"));
        assertEquals(new ValueInt(7l), attributes.get("num_cores"));
        assertEquals(new ValueInt(215l), attributes.get("num_processes"));

        List<Value> phpModules = new ArrayList<Value>();
        phpModules.add(new ValueString("rewrite"));
        assertEquals(new ValueList(phpModules, TypePrimitive.STRING), attributes.get("php_modules"));

    }
}
