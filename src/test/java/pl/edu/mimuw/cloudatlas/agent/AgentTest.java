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

import java.util.Set;

import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.math.BigDecimal;

import pl.edu.mimuw.cloudatlas.api.Api;

public class AgentTest {
    private static Process registryProcess;
    private static Process agentProcess;

    @BeforeClass
    public static void bindApi() throws Exception {
        registryProcess = Runtime.getRuntime().exec("./scripts/registry");
        Thread.sleep(2000);
        agentProcess = Runtime.getRuntime().exec("./gradlew runAgent");
        Thread.sleep(5000);
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
    public void testGetZoneSet() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            Api api = (Api) registry.lookup("Api");
            Set<String> set = api.getZoneSet();
            assertEquals(8, set.size());
            assertThat(set, hasItems("/"));
            assertThat(set, hasItems("/uw"));
            assertThat(set, hasItems("/uw/violet07", "/uw/khaki31", "/uw/khaki13"));
            assertThat(set, hasItems("/pjwstk"));
            assertThat(set, hasItems("/pjwstk/whatever01", "/pjwstk/whatever02"));
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }
}
