/*
 * Copyright 2026 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package theme_bundler;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks the bundled theme entries on the classpath against expectations computed from the theme sources.
 */
class BundledThemesTest {

    static final Path SOURCE_ROOT = Path.of("src/main/resources/META-INF/resources/themes").toAbsolutePath();

    static final Pattern COMMENT = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
    static final Pattern IMPORT = Pattern.compile("@import\\s+(?:url\\(\\s*)?['\"]?([^'\")\\s;]+)");
    static final Pattern URL = Pattern.compile("url\\(\\s*(?:\"([^\"]*)\"|'([^']*)'|([^)'\"\\s]*))\\s*\\)");
    static final Pattern MARKER = Pattern.compile("^/\\* (\\S+\\.css) \\*/$", Pattern.MULTILINE);
    static final Pattern SCHEME = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+.-]*:");

    @ParameterizedTest
    @CsvSource({"jmix-lumo/jmix-lumo.css,184", "jmix-aura/jmix-aura.css,147"})
    void bundledEntry_inlinesSourceTreeDepthFirst(String entry, int inlinedFiles) {
        List<String> expected = sourceImportOrder(entry);

        assertEquals(inlinedFiles, expected.size());
        assertEquals(expected, markers(classpathCss(entry)));
        assertFalse(classpathCss(entry).contains("@import"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"jmix-lumo/jmix-lumo.css", "jmix-aura/jmix-aura.css"})
    void bundledEntry_urlsPointToSameTargets(String entry) {
        assertEquals(sorted(sourceUrlTargets(entry)), sorted(bundleUrlTargets(entry)));
    }

    @Test
    void lumoBundle_keepsInjectedComponentStyles() {
        assertTrue(classpathCss("jmix-lumo/jmix-lumo.css").contains("@media lumo_components_jmix-code-editor"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Vaadin-Icons.woff", "Vaadin-Icons.ttf", "Vaadin-Icons.eot"})
    void vaadinIconsFont_isOnClasspath(String file) {
        assertNotNull(getClass().getResource("/META-INF/resources/themes/shared/src/icons/vaadin-icons/" + file));
    }

    @Test
    void nonEntryFiles_stayUnbundled() {
        assertTrue(classpathCss("jmix-lumo/components/jmix-code-editor.css")
                .contains("@import '../src/components/jmix-code-editor.css';"));
        assertTrue(classpathCss("jmix-lumo/styles.css").contains("@import 'jmix-lumo.css';"));
    }

    String classpathCss(String path) {
        try (InputStream stream = getClass().getResourceAsStream("/META-INF/resources/themes/" + path)) {
            assertNotNull(stream, "Not on the classpath: " + path);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    List<String> sourceImportOrder(String entry) {
        List<String> order = new ArrayList<>();
        collectImports(SOURCE_ROOT.resolve(entry), order);
        return order;
    }

    void collectImports(Path file, List<String> order) {
        Matcher matcher = IMPORT.matcher(sourceCss(file));
        while (matcher.find()) {
            Path target = file.getParent().resolve(matcher.group(1)).normalize();
            order.add(relativePath(target));
            collectImports(target, order);
        }
    }

    List<String> markers(String css) {
        List<String> markers = new ArrayList<>();
        Matcher matcher = MARKER.matcher(css);
        while (matcher.find()) {
            markers.add(matcher.group(1));
        }
        return markers;
    }

    List<String> sourceUrlTargets(String entry) {
        List<String> targets = new ArrayList<>();
        Path entryFile = SOURCE_ROOT.resolve(entry);
        collectUrlTargets(entryFile, sourceCss(entryFile), targets);
        for (String path : sourceImportOrder(entry)) {
            Path file = SOURCE_ROOT.resolve(path);
            collectUrlTargets(file, sourceCss(file), targets);
        }
        return targets;
    }

    List<String> bundleUrlTargets(String entry) {
        List<String> targets = new ArrayList<>();
        collectUrlTargets(SOURCE_ROOT.resolve(entry), COMMENT.matcher(classpathCss(entry)).replaceAll(""), targets);
        return targets;
    }

    /**
     * Collects each relative, non-{@code data:} {@code url()} of the CSS resolved against the given file, without
     * its {@code ?…}/{@code #…} suffix.
     */
    void collectUrlTargets(Path file, String css, List<String> targets) {
        Matcher matcher = URL.matcher(css);
        while (matcher.find()) {
            String url = matcher.group(1) != null ? matcher.group(1)
                    : matcher.group(2) != null ? matcher.group(2) : matcher.group(3);
            if (url.isEmpty() || url.startsWith("/") || url.startsWith("#") || SCHEME.matcher(url).find()) {
                continue;
            }
            String path = url.split("[?#]", 2)[0];
            targets.add(relativePath(file.getParent().resolve(path).normalize()));
        }
    }

    String sourceCss(Path file) {
        try {
            return COMMENT.matcher(Files.readString(file)).replaceAll("");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    String relativePath(Path file) {
        return SOURCE_ROOT.relativize(file).toString().replace('\\', '/');
    }

    List<String> sorted(List<String> list) {
        List<String> copy = new ArrayList<>(list);
        Collections.sort(copy);
        return copy;
    }
}
