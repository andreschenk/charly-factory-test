package ch.noveit.ai.charly.factory.test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class ProductControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void createProductReturns201() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Kaffee\",\"price\":9.99}"))
                .andExpect(status().isCreated());
    }

    @Test
    void createProductReturnsProductWithName() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Kaffee\",\"price\":9.99}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Kaffee"));
    }

    @Test
    void createProductReturnsProductWithPrice() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Kaffee\",\"price\":9.99}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.price").value(9.99));
    }

    @Test
    void createProductReturnsProductWithId() throws Exception {
        String response = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Kaffee\",\"price\":9.99}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long id = extractId(response);
        if (id <= 0) {
            throw new AssertionError("Produktantwort enthält keine gültige ID: " + response);
        }
    }

    @Test
    void findAllProductsReturnsJsonList() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void findByIdReturnsProduct() throws Exception {
        String createResponse = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Tee\",\"price\":4.50}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long id = extractId(createResponse);

        mockMvc.perform(get("/products/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tee"))
                .andExpect(jsonPath("$.price").value(4.50));
    }

    @Test
    void findByIdReturnsNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get("/products/999999"))
                .andExpect(status().isNotFound());
    }

    private long extractId(String json) throws IOException {
        int idx = json.indexOf("\"id\"");
        if (idx < 0) {
            throw new IOException("Keine ID in der Antwort: " + json);
        }
        int colonIdx = json.indexOf(":", idx);
        int numStart = colonIdx + 1;
        while (numStart < json.length() && Character.isWhitespace(json.charAt(numStart))) {
            numStart++;
        }
        int numEnd = numStart;
        while (numEnd < json.length() && (Character.isDigit(json.charAt(numEnd)) || json.charAt(numEnd) == '.')) {
            numEnd++;
        }
        return Long.parseLong(json.substring(numStart, numEnd));
    }
}
