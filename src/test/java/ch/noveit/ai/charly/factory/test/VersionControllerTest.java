package ch.noveit.ai.charly.factory.test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
class VersionControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void versionEndpointReturnsOk() throws Exception {
        mockMvc.perform(get("/version"))
                .andExpect(status().isOk());
    }

    @Test
    void versionEndpointReturnsJson() throws Exception {
        mockMvc.perform(get("/version"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void versionEndpointReturnsNameField() throws Exception {
        mockMvc.perform(get("/version"))
                .andExpect(jsonPath("$.name").value("charly-factory-test"));
    }

    @Test
    void versionEndpointReturnsVersionField() throws Exception {
        mockMvc.perform(get("/version"))
                .andExpect(jsonPath("$.version").value("0.1.0-SNAPSHOT"));
    }
}
