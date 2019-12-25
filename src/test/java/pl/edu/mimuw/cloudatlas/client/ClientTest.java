package pl.edu.mimuw.cloudatlas.client;

import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import pl.edu.mimuw.cloudatlas.api.Api;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
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
    private MockMvc mvc;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void homeMessageCheck() throws Exception {
        String response = this.restTemplate.getForObject("http://localhost:" + port + "/", String.class);
        assertThat(response, CoreMatchers.containsString("Welcome to CloudaAtlas client interface"));
    }

    @Test
    public void attributeValuesMessageCheck() throws Exception {
        String response = this.restTemplate.getForObject("http://localhost:" + port + "/values", String.class);
        assertThat(response, CoreMatchers.containsString("Attribute values"));
    }

    @Test
    public void queryInstallationCheck() throws Exception {
        this.mvc.perform(post("/installQuery")
                        .param("name", "&sampleQuery")
                        .param("value", "SELECT 1 AS one"))
                .andExpect(status().isOk()).andExpect(content()
                .contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(CoreMatchers.containsString("Query installed successfully")));
    }

    @Test
    public void queryUninstallationCheck() throws Exception {
        this.mvc.perform(post("/installQuery")
                .param("name", "&sampleQuery")
                .param("value", "SELECT 1 AS one"))
                .andExpect(status().isOk()).andExpect(content()
                .contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(CoreMatchers.containsString("Query installed successfully")));

        this.mvc.perform(post("/uninstallQuery")
                .param("name", "&sampleQuery"))
                .andExpect(status().isOk()).andExpect(content()
                .contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(CoreMatchers.containsString("Query uninstalled successfully")));
    }

    @Test
    public void attributeInstallationCheck() throws Exception {
        this.mvc.perform(post("/attribs")
                .param("zoneName", "/")
                .param("attributeName", "a")
                .param("attributeType", "Int")
                .param("valueString", "1"))
                .andExpect(status().isOk()).andExpect(content()
                .contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(CoreMatchers.containsString("Attribute submitted successfully")));
    }

    @Test
    public void complexAttributeInstallationCheck() throws Exception {
        this.mvc.perform(post("/attribs")
                .param("zoneName", "/")
                .param("attributeName", "a")
                .param("attributeType", "List")
                .param("attributeComplexType", "List, Set, Int")
                .param("valueString", "[[1]]"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(CoreMatchers.containsString("Attribute submitted successfully")));
    }

    @Test
    public void numericalRESTApiCheck() throws Exception {
        Thread.sleep(10000);
        this.mvc.perform(get("/attribNumValues")
                .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string(CoreMatchers.containsString("num_processes")));
    }

    @Test
    public void allValuesRESTApiCheck() throws Exception {
        Thread.sleep(10000);
        mvc.perform(get("/attribAllValues")
                .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string(CoreMatchers.containsString("contacts")));
    }
}
