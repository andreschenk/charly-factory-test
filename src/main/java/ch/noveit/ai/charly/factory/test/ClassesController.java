package ch.noveit.ai.charly.factory.test;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller für den Endpunkt zur Auflistung aller produktiven Java-Klassen.
 */
@RestController
public class ClassesController {

    private static final String BASE_PACKAGE = "ch.noveit.ai.charly.factory.test";

    /**
     * Gibt alle produktiven Java-Klassen im Basis-Paket als Markdown-Liste zurück.
     *
     * @return Markdown-Text mit den Klassennamen
     */
    @GetMapping("/classes")
    public String listClasses() throws IOException {
        List<String> classNames = findClasses(BASE_PACKAGE);
        return buildMarkdown(classNames);
    }

    /**
     * Findet alle Klassen im angegebenen Paket über den Classpath.
     *
     * @param packageName das Java-Paket (z. B. "ch.noveit.ai.charly.factory.test")
     * @return Liste der einfachen Klassennamen
     */
    private List<String> findClasses(String packageName) throws IOException {
        List<String> classNames = new ArrayList<>();
        CachingMetadataReaderFactory factory = new CachingMetadataReaderFactory();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        // Suche nach allen .class-Dateien im Paket
        String packageSearchPath = "classpath*:" + packageName.replace('.', '/') + "/**/*.class";

        Resource[] resources = resolver.getResources(packageSearchPath);

        for (Resource resource : resources) {
            if (resource.isReadable()) {
                MetadataReader reader = factory.getMetadataReader(resource);
                String className = reader.getClassMetadata().getClassName();

                // Nur Klassen aus dem Basis-Paket, keine Unterpakete und keine inneren Klassen
                if (className.startsWith(packageName + ".")
                        && !className.contains("$")
                        && !className.endsWith("Test")) {
                    classNames.add(className.substring(className.lastIndexOf('.') + 1));
                }
            }
        }

        classNames.sort(String::compareTo);
        return classNames;
    }

    /**
     * Baut einen Markdown-Text aus der Liste der Klassennamen.
     *
     * @param classNames Liste der einfachen Klassennamen
     * @return Markdown-Formatierter Text
     */
    private String buildMarkdown(List<String> classNames) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Java-Klassen im Projekt\n\n");
        sb.append("Auflistung aller produktiven Java-Klassen:\n\n");

        for (String className : classNames) {
            sb.append("- `").append(className).append("`\n");
        }

        return sb.toString();
    }
}
