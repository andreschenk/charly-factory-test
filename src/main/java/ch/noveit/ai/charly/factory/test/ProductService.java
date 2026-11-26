package ch.noveit.ai.charly.factory.test;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
     * @ Liste aller Produkte
     */
    public List<Product> findAll() {
        return Collections.unmodifiableList(products);
    }

    /**
     * Liefert alle Produkte, optional sortiert nach einem Feld und einer Richtung.
     *
     * @param sort  Sortierfeld: "name" oder "price"; bei unbekanntem Wert wird null zurückgegeben
     * @param order "asc" für aufsteigend, "desc" für absteigend; Standard ist "asc"
     * @return sortierte Liste aller Produkte oder unveränderte Liste bei unbekanntem Sortierfeld
     */
    public List<Product> findAll(String sort, String order) {
        if (!"name".equals(sort) && !"price".equals(sort)) {
            return Collections.unmodifiableList(products);
        }

        Comparator<Product> comparator;
        if ("name".equals(sort)) {
            comparator = Comparator.comparing(Product::name);
        } else {
            comparator = Comparator.comparingDouble(Product::price);
        }

        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }

        return products.stream()
                .sorted(comparator)
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
