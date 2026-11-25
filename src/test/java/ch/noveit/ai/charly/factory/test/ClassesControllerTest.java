package ch.noveit.ai.charly.factory.test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class ClassesControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void classesEndpointReturnsOk() throws Exception {
        mockMvc.perform(get("/classes"))
                .andExpect(status().isOk());
    }

    @Test
    void classesEndpointReturnsMarkdownContent() throws Exception {
        mockMvc.perform(get("/classes"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN));
    }

    @Test
    void classesEndpointContainsMarkdownHeader() throws Exception {
        String response = mockMvc.perform(get("/classes"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        if (response.contains("# Java-Klassen im Projekt")) {
            // Header vorhanden – Test bestanden
        } else {
            throw new AssertionError("Markdown-Header '# Java-Klassen im Projekt' nicht gefunden in: " + response);
        }
    }

    @Test
    void classesEndpointListsProductiveClasses() throws Exception {
        String response = mockMvc.perform(get("/classes"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Mindestens die bekannten produktiven Klassen sollten enthalten sein
        boolean hasApp = response.contains("`App`");
        boolean hasClassesController = response.contains("`ClassesController`");
        boolean hasHealthController = response.contains("`HealthController`");
        boolean hasVersionController = response.contains("`VersionController`");

        if (!hasApp || !hasClassesController || !hasHealthController || !hasVersionController) {
            throw new AssertionError("Nicht alle produktiven Klassen gefunden. Antwort: " + response);
        }
    }

    @Test
    void classesEndpointDoesNotListTestClasses() throws Exception {
        String response = mockMvc.perform(get("/classes"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Testklassen sollten nicht enthalten sein
        if (response.contains("AppTest") || response.contains("HealthControllerTest")
                || response.contains("VersionControllerTest") || response.contains("SpringContextTest")) {
            throw new AssertionError("Testklassen sollten nicht im Markdown enthalten sein. Antwort: " + response);
        }
    }

    @Test
    void classesEndpointReturnsNotFoundForUnknownPath() throws Exception {
        mockMvc.perform(get("/unknown"))
                .andExpect(status().isNotFound());
    }
}
