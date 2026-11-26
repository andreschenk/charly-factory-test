package ch.noveit.ai.charly.factory.test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class ProductSortingTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ProductService productService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        // Produkte-Liste zurücksetzen für unabhängige Tests
        Field productsField = ProductService.class.getDeclaredField("products");
        productsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<ProductService.Product> products = (List<ProductService.Product>) productsField.get(productService);
        products.clear();
    }

    @Test
    void sortByNameAscending() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Zucker\",\"price\":5.0}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Apfel\",\"price\":3.0}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Bananen\",\"price\":2.0}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/products?sort=name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Apfel"))
                .andExpect(jsonPath("$[1].name").value("Bananen"))
                .andExpect(jsonPath("$[2].name").value("Zucker"));
    }

    @Test
    void sortByPriceAscending() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Zucker\",\"price\":5.0}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Apfel\",\"price\":1.0}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Bananen\",\"price\":3.0}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/products?sort=price"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].price").value(1.0))
                .andExpect(jsonPath("$[1].price").value(3.0))
                .andExpect(jsonPath("$[2].price").value(5.0));
    }

    @Test
    void sortByNameDescending() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Zucker\",\"price\":5.0}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Apfel\",\"price\":3.0}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Bananen\",\"price\":2.0}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/products?sort=name&order=desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Zucker"))
                .andExpect(jsonPath("$[1].name").value("Bananen"))
                .andExpect(jsonPath("$[2].name").value("Apfel"));
    }

    @Test
    void sortByPriceDescending() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Zucker\",\"price\":5.0}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Apfel\",\"price\":1.0}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Bananen\",\"price\":3.0}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/products?sort=price&order=desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].price").value(5.0))
                .andExpect(jsonPath("$[1].price").value(3.0))
                .andExpect(jsonPath("$[2].price").value(1.0));
    }

    @Test
    void noSortParameterReturnsUnchangedOrder() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Erstes\",\"price\":10.0}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Zweites\",\"price\":20.0}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Erstes"))
                .andExpect(jsonPath("$[1].name").value("Zweites"));
    }

    @Test
    void unknownSortFieldReturns400() throws Exception {
        mockMvc.perform(get("/products?sort=unknown"))
                .andExpect(status().isBadRequest());
    }
}
