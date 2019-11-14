package pl.edu.mimuw.cloudatlas.agent;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.Runtime;
import java.lang.Process;
import java.lang.Thread;

import java.io.InputStream;

import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.math.BigDecimal;

public class AgentTest {
    private static Process registryProcess;
    private static Process agentProcess;

    @BeforeClass
    public static void bindApi() throws Exception {
        registryProcess = Runtime.getRuntime().exec("./scripts/registry");
        Thread.sleep(1000);
        agentProcess = Runtime.getRuntime().exec("./gradlew runAgent");
        Thread.sleep(1000);
    }

    @AfterClass
    public static void killProcesses() throws Exception {
        registryProcess.destroy();
        agentProcess.destroy();
    }

    @Test
    public void testPing() {
		try {
			Registry registry = LocateRegistry.getRegistry("localhost");
			Api api = (Api) registry.lookup("Api");
			int res = api.ping(10);
            assertEquals(11, res);
		} catch (Exception e) {
			System.err.println("FibonacciClient exception:");
			e.printStackTrace();
            assertTrue(false);
		}
    }
}
