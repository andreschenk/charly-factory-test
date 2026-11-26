package ch.noveit.ai.charly.factory.test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class ProductSortingTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void sortByNameReturnsProductsAscending() throws Exception {
        createProduct("Zucker", 3.0);
        createProduct("Apfel", 1.5);
        createProduct("Bananen", 2.0);

        List<String> names = getAllNames("/products?sort=name");
        assertRelativeOrder(names, List.of("Apfel", "Bananen", "Zucker"));
    }

    @Test
    void sortByPriceReturnsProductsAscending() throws Exception {
        createProduct("Teuer", 50.0);
        createProduct("Günstig", 5.0);
        createProduct("Mittel", 25.0);

        List<Double> prices = getAllPrices("/products?sort=price");
        assertRelativeOrder(prices, List.of(5.0, 25.0, 50.0));
    }

    @Test
    void sortByNameDescendingReturnsProductsReversed() throws Exception {
        createProduct("Zucker", 3.0);
        createProduct("Apfel", 1.5);
        createProduct("Bananen", 2.0);

        List<String> names = getAllNames("/products?sort=name&order=desc");
        assertRelativeOrder(names, List.of("Zucker", "Bananen", "Apfel"));
    }

    @Test
    void sortByPriceDescendingReturnsProductsReversed() throws Exception {
        createProduct("Teuer", 50.0);
        createProduct("Günstig", 5.0);
        createProduct("Mittel", 25.0);

        List<Double> prices = getAllPrices("/products?sort=price&order=desc");
        assertRelativeOrder(prices, List.of(50.0, 25.0, 5.0));
    }

    @Test
    void unknownSortFieldReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/products?sort=unknown"))
                .andExpect(status().isBadRequest());
    }

    private void createProduct(String name, double price) throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"price\":" + price + "}"))
                .andExpect(status().isCreated());
    }

    private List<String> getAllNames(String url) throws Exception {
        MvcResult result = mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        String json = result.getResponse().getContentAsString();
        // Extrahiere alle "name"-Werte aus dem JSON-Array
        List<String> names = new ArrayList<>();
        int idx = 0;
        while ((idx = json.indexOf("\"name\"", idx)) >= 0) {
            int colonIdx = json.indexOf(":", idx);
            int quoteStart = json.indexOf("\"", colonIdx + 1);
            int quoteEnd = json.indexOf("\"", quoteStart + 1);
            if (quoteStart > 0 && quoteEnd > quoteStart) {
                names.add(json.substring(quoteStart + 1, quoteEnd));
            }
            idx = quoteEnd;
        }
        return names;
    }

    private List<Double> getAllPrices(String url) throws Exception {
        MvcResult result = mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        String json = result.getResponse().getContentAsString();
        // Extrahiere alle "price"-Werte aus dem JSON-Array
        List<Double> prices = new ArrayList<>();
        int idx = 0;
        while ((idx = json.indexOf("\"price\"", idx)) >= 0) {
            int colonIdx = json.indexOf(":", idx);
            int numStart = colonIdx + 1;
            while (numStart < json.length() && Character.isWhitespace(json.charAt(numStart))) {
                numStart++;
            }
            int numEnd = numStart;
            while (numEnd < json.length() && (Character.isDigit(json.charAt(numEnd)) || json.charAt(numEnd) == '.' || json.charAt(numEnd) == '-')) {
                numEnd++;
            }
            if (numEnd > numStart) {
                prices.add(Double.parseDouble(json.substring(numStart, numEnd)));
            }
            idx = numEnd;
        }
        return prices;
    }

    /**
     * Prüft, dass die erwarteten Werte in der gegebenen Reihenfolge als Teilsequenz
     * in der tatsächlichen Liste erscheinen. So können andere Produkte aus vorherigen
     * Tests vorhanden sein, ohne die Sortierung zu verfälschen.
     */
    private <T> void assertRelativeOrder(List<T> actual, List<T> expected) {
        int expectedIdx = 0;
        for (T item : actual) {
            if (expectedIdx < expected.size() && expected.get(expectedIdx).equals(item)) {
                expectedIdx++;
            }
        }
        if (expectedIdx != expected.size()) {
            throw new AssertionError("Erwartete Reihenfolge " + expected + " nicht in " + actual + " gefunden.");
        }
    }
}
