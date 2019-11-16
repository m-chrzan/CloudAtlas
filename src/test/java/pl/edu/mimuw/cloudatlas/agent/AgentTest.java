package pl.edu.mimuw.cloudatlas.agent;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

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
        Thread.sleep(10000);
        agentProcess = Runtime.getRuntime().exec("./gradlew runAgent");
        Thread.sleep(10000);
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
            assertEquals(null, set);
		} catch (Exception e) {
			e.printStackTrace();
            assertTrue(false);
		}
    }
}
