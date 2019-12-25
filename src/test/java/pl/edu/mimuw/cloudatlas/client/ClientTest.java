package pl.edu.mimuw.cloudatlas.client;

import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import pl.edu.mimuw.cloudatlas.api.Api;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ClientTest {
    private static Process registryProcess;
    private static Process agentProcess;
    private static Process clientProcess;

    private static Registry registry;
    private static Api api;

    @BeforeClass
    public static void bindApi() throws Exception {
        registryProcess = Runtime.getRuntime().exec("./scripts/registry");
        Thread.sleep(10000);
        agentProcess = Runtime.getRuntime().exec("./gradlew runAgent -Dhostname=localhost");
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

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void homeMessageCheck() throws Exception {
        Thread.sleep(100);
        String response = this.restTemplate.getForObject("http://localhost:" + port + "/", String.class);
        assertThat(response, CoreMatchers.containsString("Welcome to CloudaAtlas client interface"));
    }

    @Test
    public void attributeValuesMessageCheck() throws Exception {
        Thread.sleep(100);
        String response = this.restTemplate.getForObject("http://localhost:" + port + "/values", String.class);
        assertThat(response, CoreMatchers.containsString("Attribute values"));
    }
}
