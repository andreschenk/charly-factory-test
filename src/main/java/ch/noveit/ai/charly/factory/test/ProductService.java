package ch.noveit.ai.charly.factory.test;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service für die Produktverwaltung. Hält alle Produkte im Speicher.
 */
@Service
public class ProductService {

    private final List<Product> products = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    /**
     * Legt ein neues Produkt an und weist eine eindeutige ID zu.
     *
     * @param name  Name des Produkts
     * @param price Preis des Produkts
     * @return das neu angelegte Produkt mit vergebener ID
     */
    public Product create(String name, double price) {
        long id = idGenerator.incrementAndGet();
        Product product = new Product(id, name, price);
        products.add(product);
        return product;
    }

    /**
     * Liefert alle Produkte.
     *
     * @return unmodifizierbare Liste aller Produkte
     */
    public List<Product> findAll() {
        return Collections.unmodifiableList(products);
    }

    /**
     * Liefert alle Produkte, deren Name den Suchtext (case-insensitive) enthält.
     *
     * @param searchText Suchtext oder null für keine Filterung
     * @return gefilterte Liste aller Produkte
     */
    public List<Product> findByNameContainingIgnoreCase(String searchText) {
        if (searchText == null || searchText.isBlank()) {
            return findAll();
        }
        String lower = searchText.toLowerCase();
        return products.stream()
                .filter(p -> p.name().toLowerCase().contains(lower))
                .toList();
    }

    /**
     * Liefert das Produkt mit der gegebenen ID oder null, wenn nicht gefunden.
     *
     * @param id ID des gesuchten Produkts
     * @return das Produkt oder null
     */
    public Product findById(long id) {
        return products.stream()
                .filter(p -> p.id() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * DTO für ein Produkt.
     */
    public record Product(long id, String name, double price) {
    }
}
