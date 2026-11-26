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
     * Liefert alle Produkte, optional sortiert nach Name oder Preis.
     *
     * @param sort  Sortierfeld: "name" oder "price"; ohne Angabe unveränderte Reihenfolge
     * @param order "asc" (Standard) oder "desc" für absteigende Reihenfolge
     * @return sortierte Liste aller Produkte
     */
    @GetMapping
    public List<ProductService.Product> findAll(
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "asc") String order) {

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
            return productService.findAll().stream()
                    .sorted(comparator)
                    .toList();
        }
        return productService.findAll();
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
