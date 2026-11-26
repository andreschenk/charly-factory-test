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

@SpringBootTest
class ProductValidationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void createProductWithNullNameReturns400() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\":9.99}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProductWithBlankNameReturns400() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  \",\"price\":9.99}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProductWithEmptyNameReturns400() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"price\":9.99}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProductWithNameOver100CharsReturns400() throws Exception {
        String longName = "a".repeat(101);
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + longName + "\",\"price\":9.99}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProductWithValid100CharNameReturns201() throws Exception {
        String name = "a".repeat(100);
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"price\":9.99}"))
                .andExpect(status().isCreated());
    }

    @Test
    void createProductWithZeroPriceReturns400() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Kaffee\",\"price\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProductWithNegativePriceReturns400() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Kaffee\",\"price\":-5.0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectedProductDoesNotAppearInGetProducts() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"price\":9.99}"))
                .andExpect(status().isBadRequest());

        String response = mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        if (response.contains("\"name\":\"\"")) {
            throw new AssertionError("Ein abgewiesenes Produkt mit leerem Name erscheint in GET /products: " + response);
        }
    }
}
