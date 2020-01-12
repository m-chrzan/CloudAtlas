package pl.edu.mimuw.cloudatlas.agent;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;
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

import pl.edu.mimuw.cloudatlas.api.Api;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueDouble;
import pl.edu.mimuw.cloudatlas.model.ValueInt;
import pl.edu.mimuw.cloudatlas.model.ValueList;
import pl.edu.mimuw.cloudatlas.model.ValueNull;
import pl.edu.mimuw.cloudatlas.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.model.ValueString;
import pl.edu.mimuw.cloudatlas.model.ValueTime;

@Ignore
public class AgentIntegrationTest {
    private static Process registryProcess;
    private static Process agentProcess;

    private static final long queriesInterval = 100;

    private static Registry registry;
    private static Api api;

    @BeforeClass
    public static void bindApi() throws Exception {
        registryProcess = Runtime.getRuntime().exec("./scripts/registry");
        Thread.sleep(5000);
        agentProcess = Runtime.getRuntime().exec("./gradlew runAgent -Dhostname=localhost -DfreshnessPeriod=10000000 -DqueryPeriod=100");
        Thread.sleep(5000);

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
        // assertEquals(new ValueString(0l), rootAttributes.get("level"));
        assertEquals(ValueNull.getInstance(), rootAttributes.get("name"));
    }

    @Test
    public void testIntermediateGetZoneAttributeValue() throws Exception {
        AttributesMap attributes = api.getZoneAttributeValues("/uw");
        // assertEquals(new ValueInt(1l), attributes.get("level"));
        assertEquals(new ValueString("uw"), attributes.get("name"));
    }

    @Test
    public void testLeafGetZoneAttributeValue() throws Exception {
        AttributesMap attributes = api.getZoneAttributeValues("/pjwstk/whatever01");
        assertEquals(new ValueInt(2l), attributes.get("level"));
        assertEquals(new ValueString("whatever01"), attributes.get("name"));
        assertEquals(new ValueString("/pjwstk/whatever01"), attributes.get("owner"));
        long timestamp = ((ValueTime) attributes.get("timestamp")).getValue();
        assertTrue(timestamp <= System.currentTimeMillis());
        assertEquals(new ValueInt(1l), attributes.get("cardinality"));
        assertEquals(new ValueTime("2012/10/18 07:03:00.000"), attributes.get("creation"));
        assertEquals(new ValueDouble(0.1), attributes.get("cpu_usage"));
        assertEquals(new ValueInt(7l), attributes.get("num_cores"));
        assertEquals(new ValueInt(215l), attributes.get("num_processes"));

        List<Value> phpModules = new ArrayList<Value>();
        phpModules.add(new ValueString("rewrite"));
        assertEquals(new ValueList(phpModules, TypePrimitive.STRING), attributes.get("php_modules"));
    }

    @Test
    public void testInstallQuery() throws Exception {
        String name = "&query";
        String queryCode = "SELECT 1 AS one";
        api.installQuery(name, queryCode);
        // TODO: test something here
    }

    @Test
    public void testInstallQueryRuns() throws Exception {
        String name = "&query";
        String queryCode = "SELECT 1 AS one";
        api.installQuery(name, queryCode);

        Thread.sleep(queriesInterval * 2);
        AttributesMap attributes = api.getZoneAttributeValues("/pjwstk");
        assertEquals(new ValueInt(1l), attributes.getOrNull("one"));
    }

    @Test
    public void testUninstallQuery() throws Exception {
        String name = "&query";
        String queryCode = "SELECT 1 AS one";
        api.installQuery(name, queryCode);
        api.uninstallQuery(name);
        AttributesMap attributes = api.getZoneAttributeValues("/pjwstk");
        assertNull(attributes.getOrNull(name));
        // TODO: test this correctly
    }

    @Test
    public void testSetAttributeValueChange() throws Exception {
        Value numProcesses = new ValueInt(42l);
        api.setAttributeValue("/uw/khaki13", "num_processes", numProcesses);
        AttributesMap attributes = api.getZoneAttributeValues("/uw/khaki13");
        assertEquals(numProcesses, attributes.get("num_processes"));
    }

    @Test
    public void testSetAttributeValueAdd() throws Exception {
        Value numProcesses = new ValueInt(42l);
        api.setAttributeValue("/uw/khaki13", "an_attribute", numProcesses);
        AttributesMap attributes = api.getZoneAttributeValues("/uw/khaki13");
        assertEquals(numProcesses, attributes.get("an_attribute"));
    }
}
