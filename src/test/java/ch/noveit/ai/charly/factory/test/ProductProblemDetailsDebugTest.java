package ch.noveit.ai.charly.factory.test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Debug-Test, um die tatsächliche JSON-Antwortstruktur zu prüfen.
 */
@SpringBootTest
class ProductProblemDetailsDebugTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void debugBadRequestResponse() throws Exception {
        String response = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"price\":9.99}"))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertTrue(response.contains("\"type\""), "Response sollte 'type' enthalten: " + response);
        assertTrue(response.contains("\"title\""), "Response sollte 'title' enthalten: " + response);
        assertTrue(response.contains("\"status\""), "Response sollte 'status' enthalten: " + response);
        assertTrue(response.contains("\"detail\""), "Response sollte 'detail' enthalten: " + response);
        assertTrue(response.contains("\"instance\""), "Response sollte 'instance' enthalten: " + response);
        assertTrue(response.contains("\"properties\""), "Response sollte 'properties' enthalten: " + response);
    }

    @Test
    void debugNotFoundResponse() throws Exception {
        String response = mockMvc.perform(get("/products/999999"))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        assertTrue(response.contains("\"type\""), "Response sollte 'type' enthalten: " + response);
        assertTrue(response.contains("\"title\""), "Response sollte 'title' enthalten: " + response);
        assertTrue(response.contains("\"status\""), "Response sollte 'status' enthalten: " + response);
        assertTrue(response.contains("\"detail\""), "Response sollte 'detail' enthalten: " + response);
        assertTrue(response.contains("\"instance\""), "Response sollte 'instance' enthalten: " + response);
    }
}
