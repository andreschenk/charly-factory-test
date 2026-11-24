package ch.noveit.ai.charly.factory.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Einstiegspunkt des Testprojekts. Wird über Issues schrittweise zu einer Spring-Boot-REST-Applikation ausgebaut.
 */
@SpringBootApplication
public class App {

    static final String NAME = "charly-factory-test";
    static final String VERSION = "0.1.0-SNAPSHOT";

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    static String name() {
        return NAME;
    }

    static String version() {
        return VERSION;
    }
}
