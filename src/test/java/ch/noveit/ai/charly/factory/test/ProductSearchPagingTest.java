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
class ProductSearchPagingTest {

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

    // --- Suche (q-Parameter) ---

    @Test
    void searchByNameCaseInsensitive() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Kaffee\",\"price\":9.99}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"KAFFEE-MILCH\",\"price\":1.50}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Tee\",\"price\":4.50}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/products?q=kaffee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Kaffee"))
                .andExpect(jsonPath("$[1].name").value("KAFFEE-MILCH"));
    }

    @Test
    void searchByNamePartialMatch() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Schwarzer Kaffee\",\"price\":7.0}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Weisser Kaffee\",\"price\":6.0}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Schokoladenmilch\",\"price\":2.50}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/products?q=kaff"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Schwarzer Kaffee"))
                .andExpect(jsonPath("$[1].name").value("Weisser Kaffee"));
    }

    @Test
    void searchByNameNoMatchReturnsEmptyList() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Kaffee\",\"price\":9.99}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/products?q=\"Bier\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void searchByNameNoMatchReturnsEmptyArray() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Kaffee\",\"price\":9.99}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/products?q=Bier"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void searchWithoutQReturnsAllProducts() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Kaffee\",\"price\":9.99}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Tee\",\"price\":4.50}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // --- Paginierung ---

    @Test
    void pageAndSizeReturnPaginatedResponse() throws Exception {
        for (int i = 1; i <= 5; i++) {
            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Produkt " + i + "\",\"price\":" + i + ".0}"))
                    .andExpect(status().isCreated());
        }

        // Seite 0, Grösse 2
        mockMvc.perform(get("/products?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalItems").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void secondPageReturnsCorrectItems() throws Exception {
        for (int i = 1; i <= 5; i++) {
            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Produkt " + i + "\",\"price\":" + i + ".0}"))
                    .andExpect(status().isCreated());
        }

        // Seite 1, Grösse 2
        mockMvc.perform(get("/products?page=1&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalItems").value(5));
    }

    @Test
    void pageBeyondTotalReturnsEmptyItems() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ein Produkt\",\"price\":9.99}"))
                .andExpect(status().isCreated());

        // Seite 10, Grösse 20 – mehr als vorhanden
        mockMvc.perform(get("/products?page=10&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.page").value(10))
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void defaultPageSizeIsTwenty() throws Exception {
        for (int i = 1; i <= 25; i++) {
            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Produkt " + i + "\",\"price\":" + i + ".0}"))
                    .andExpect(status().isCreated());
        }

        // Nur page=0 explizit, size soll Default 20 sein
        mockMvc.perform(get("/products?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(20))
                .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    void sizeOverMaxReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Produkt\",\"price\":1.0}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/products?page=0&size=101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sizeZeroReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Produkt\",\"price\":1.0}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/products?page=0&size=0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void negativePageReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Produkt\",\"price\":1.0}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/products?page=-1&size=10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void negativeSizeReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Produkt\",\"price\":1.0}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/products?page=0&size=-5"))
                .andExpect(status().isBadRequest());
    }

    // --- Kombination von Suche, Paginierung und Sortierung ---

    @Test
    void searchAndPageCombine() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Kaffee\",\"price\":9.99}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"KAFFEE-MILCH\",\"price\":1.50}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Tee\",\"price\":4.50}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/products?q=kaffee&page=0&size=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.totalItems").value(2));
    }

    @Test
    void searchAndSortCombine() throws Exception {
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

        mockMvc.perform(get("/products?q=apfel&sort=name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Apfel"));
    }

    @Test
    void searchSortAndPageCombine() throws Exception {
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

        mockMvc.perform(get("/products?q=apfel&sort=name&page=0&size=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.totalItems").value(1));
    }
}
