package pl.edu.mimuw.cloudatlas.agent;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.hasItems;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import pl.edu.mimuw.cloudatlas.api.Api;
import pl.edu.mimuw.cloudatlas.interpreter.Main;
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
import pl.edu.mimuw.cloudatlas.model.ZMI;

public class ApiImplementationTests {
    private ApiImplementation api;

    @Before
    public void initializeApi() throws Exception {
        ZMI root = Main.createTestHierarchy2();
        api = new ApiImplementation(root);
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
        assertAttributeInZmiEquals(name, new ValueQuery(queryCode), "/");
        assertAttributeInZmiEquals(name, new ValueQuery(queryCode), "/uw");
        assertAttributeInZmiEquals(name, new ValueQuery(queryCode), "/pjwstk");
    }

    @Test
    public void testInstallQueryRuns() throws Exception {
        api.installQuery("&query", "SELECT 1 AS one");
        assertAttributeInZmiEquals("one", new ValueInt(1l), "/");
        assertAttributeInZmiEquals("one", new ValueInt(1l), "/uw");
        assertAttributeInZmiEquals("one", new ValueInt(1l), "/pjwstk");
    }

    @Test
    public void testInstallQueryRuns2() throws Exception {
        api.installQuery("&query", "SELECT sum(num_processes) AS num_processes");
        assertAttributeInZmiEquals("num_processes", new ValueInt(362l), "/uw");
        assertAttributeInZmiEquals("num_processes", new ValueInt(437l), "/pjwstk");
        assertAttributeInZmiEquals("num_processes", new ValueInt(799l), "/");
    }

    @Test
    public void testInstallQueryWithInvalidNameFails() throws Exception {
        String name = "query";
        String queryCode = "SELECT 1 AS one";
        try {
            api.installQuery(name, queryCode);
            assertTrue("should have thrown", false);
        } catch (Exception e) {
            assertEquals("Invalid query identifier", e.getMessage());
        }
    }

    public void assertAttributeInZmiEquals(String attribute, Value expected, String zmiPath) throws Exception {
        AttributesMap attributes = api.getZoneAttributeValues(zmiPath);
        assertEquals(expected, attributes.get(attribute));
    }

    @Test
    public void testUninstallQuery() throws Exception {
        String name = "&query";
        String queryCode = "SELECT 1 AS one";
        api.installQuery(name, queryCode);
        api.uninstallQuery(name);
        AttributesMap attributes = api.getZoneAttributeValues("/pjwstk");
        assertNull(attributes.getOrNull(name));
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

    @Test
    public void testSetAttributeValueRunsQueries() throws Exception {
        api.installQuery("&query", "SELECT sum(num_processes) AS num_processes");
        Value numProcesses = new ValueInt(42l);
        api.setAttributeValue("/uw/khaki13", "num_processes", numProcesses);
        assertAttributeInZmiEquals("num_processes", new ValueInt(297l), "/uw");
        assertAttributeInZmiEquals("num_processes", new ValueInt(437l), "/pjwstk");
        assertAttributeInZmiEquals("num_processes", new ValueInt(734l), "/");
    }
}
