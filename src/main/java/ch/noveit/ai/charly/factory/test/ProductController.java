package ch.noveit.ai.charly.factory.test;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller für Produkt-Endpunkte.
 */
@RestController
@RequestMapping("/products")
public class ProductController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Legt ein neues Produkt an und liefert es mit Status 201 zurück.
     *
     * @param request Objekt mit Name und Preis
     * @return das erstellte Produkt
     */
    @PostMapping
    public ResponseEntity<ProductService.Product> create(@RequestBody CreateProductRequest request) {
        validate(request);
        ProductService.Product product = productService.create(request.getName(), request.getPrice());
        return ResponseEntity.status(201).body(product);
    }

    /**
     * Prüft die Eingabedaten auf Gültigkeit. Wirft bei Fehlern eine 400-Exception.
     *
     * @param request anzulegendes Produkt
     * @throws org.springframework.web.server.ResponseStatusException bei ungültigen Daten (400)
     */
    private void validate(CreateProductRequest request) {
        if (request.getName() == null || request.getName().isBlank() || request.getName().length() > 100) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Name ist Pflicht (1–100 Zeichen)");
        }
        if (request.getPrice() <= 0) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Preis muss grösser als 0 sein");
        }
    }

    /**
     * Liefert Produkte mit optionaler Suche und Sortierung. Bei expliziter Paginierung (page/size-Parameter) wird
     * eine paginierte Antwort zurückgegeben; andernfalls ein einfaches Array für Abwärtskompatibilität.
     *
     * @param q      Suchtext für Produktnamen (case-insensitive, Teiltext)
     * @param sort   Sortierfeld: "name" oder "price"; ohne Angabe unveränderte Reihenfolge
     * @param order  "asc" (Standard) oder "desc" für absteigende Reihenfolge
     * @param page   Seitennummer (ab 0), Standard 0
     * @param size   Seitengrösse, Standard 20, maximal 100; bei ungültigem Wert 400
     * @return paginierte Antwort mit den gefundenen Produkten oder einfaches Array
     */
    @GetMapping
    public Object findAll(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        // Produkte filtern (optional nach Name)
        List<ProductService.Product> filtered = productService.findByNameContainingIgnoreCase(q);

        // Optional sortieren
        if (sort != null && !sort.isBlank()) {
            Comparator<ProductService.Product> comparator;
            switch (sort) {
                case "name" -> comparator = Comparator.comparing(ProductService.Product::name);
                case "price" -> comparator = Comparator.comparingDouble(ProductService.Product::price);
                default -> throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        "Unbekanntes Sortierfeld: " + sort);
            }
            if ("desc".equalsIgnoreCase(order)) {
                comparator = comparator.reversed();
            }
            filtered = filtered.stream().sorted(comparator).toList();
        }

        // Wenn page und/oder size explizit angegeben wurden, paginierte Antwort liefern
        if (page != null || size != null) {
            int p = Optional.ofNullable(page).orElse(DEFAULT_PAGE);
            int s = Optional.ofNullable(size).orElse(DEFAULT_SIZE);

            // Grösse validieren: nur positive Werte erlaubt, maximal 100
            if (s <= 0 || s > MAX_SIZE) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        "Seitengrösse muss zwischen 1 und " + MAX_SIZE + " sein");
            }

            // Seite validieren: nur nicht-negative Werte erlaubt
            if (p < 0) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        "Seitennummer muss grösser oder gleich 0 sein");
            }

            // Paginierung anwenden
            long totalItems = filtered.size();
            int totalPages = (int) Math.ceil((double) totalItems / s);
            int fromIndex = p * s;
            int toIndex = (int) Math.min((long) fromIndex + s, totalItems);

            List<ProductService.Product> items;
            if (fromIndex < totalItems) {
                items = filtered.subList(fromIndex, toIndex);
            } else {
                items = List.of();
            }

            return new ProductPage(items, p, s, totalItems, totalPages);
        }

        // Keine Paginierung: einfaches Array für Abwärtskompatibilität
        return filtered;
    }

    /**
     * Liefert das Produkt mit der gegebenen ID.
     *
     * @param id ID des gesuchten Produkts
     * @return das Produkt
     * @throws org.springframework.web.server.ResponseStatusException bei nicht gefundener ID (404)
     */
    @GetMapping("/{id}")
    public ProductService.Product findById(@PathVariable long id) {
        ProductService.Product product = productService.findById(id);
        if (product == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Produkt nicht gefunden: " + id);
        }
        return product;
    }

    /**
     * Paginierte Antwortstruktur.
     */
    record ProductPage(List<ProductService.Product> items, int page, int size, long totalItems, int totalPages) {
    }

    /**
     * Eingabe-DTO für das Anlegen eines Produkts.
     */
    static class CreateProductRequest {

        private String name;
        private double price;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }
    }
}
