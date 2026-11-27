package ch.noveit.ai.charly.factory.test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

/**
 * Prüft, dass Fehlerantworten dem RFC 9457 Problem Details Format entsprechen.
 */
@SpringBootTest
class ProductProblemDetailsTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    // --- 400-Fehler: Content-Type und Struktur ---

    @Test
    void badRequestHasProblemJsonContentType() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"price\":9.99}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("application/problem+json")));
    }

    @Test
    void badRequestContainsProblemDetailsType() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"price\":9.99}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").exists());
    }

    @Test
    void badRequestContainsProblemDetailsTitle() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"price\":9.99}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    void badRequestContainsProblemDetailsStatus() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"price\":9.99}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void badRequestContainsProblemDetailsDetail() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"price\":9.99}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void badRequestContainsProblemDetailsInstance() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"price\":9.99}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.instance").exists());
    }

    // --- 400-Fehler: betroffene Felder bei ungültigem Namen ---

    @Test
    void badRequestForNullNameListsNameField() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\":9.99}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.properties.name").exists());
    }

    @Test
    void badRequestForBlankNameListsNameField() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  \",\"price\":9.99}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.properties.name").exists());
    }

    @Test
    void badRequestForEmptyNameListsNameField() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"price\":9.99}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.properties.name").exists());
    }

    @Test
    void badRequestForLongNameListsNameField() throws Exception {
        String longName = "a".repeat(101);
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + longName + "\",\"price\":9.99}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.properties.name").exists());
    }

    // --- 400-Fehler: betroffene Felder bei ungültigem Preis ---

    @Test
    void badRequestForZeroPriceListsPriceField() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Kaffee\",\"price\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.properties.price").exists());
    }

    @Test
    void badRequestForNegativePriceListsPriceField() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Kaffee\",\"price\":-5.0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.properties.price").exists());
    }

    // --- 404-Fehler: Content-Type ---

    @Test
    void notFoundHasProblemJsonContentType() throws Exception {
        mockMvc.perform(get("/products/999999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("application/problem+json")));
    }

    @Test
    void notFoundContainsProblemDetailsType() throws Exception {
        mockMvc.perform(get("/products/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").exists());
    }

    @Test
    void notFoundContainsProblemDetailsTitle() throws Exception {
        mockMvc.perform(get("/products/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    void notFoundContainsProblemDetailsStatus() throws Exception {
        mockMvc.perform(get("/products/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void notFoundContainsProblemDetailsDetail() throws Exception {
        mockMvc.perform(get("/products/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void notFoundContainsProblemDetailsInstance() throws Exception {
        mockMvc.perform(get("/products/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.instance").exists());
    }
}
