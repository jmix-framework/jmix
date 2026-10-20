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
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import io.jmix.flowui.theme.bundler.ThemeCssBundleException;
import io.jmix.flowui.theme.bundler.ThemeCssBundler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThemeCssBundlerTest {

    @TempDir
    Path tempDir;

    Path root;

    @BeforeEach
    void setUp() {
        root = tempDir.resolve("themes");
    }

    @Test
    void bundle_inlinesImportsDepthFirstInPlace() {
        write("t/entry.css", "@import './a.css';\n@import \"./b.css\";\n.entry {}\n");
        write("t/a.css", "@import '../shared/c.css';\n.a {}\n");
        write("t/b.css", ".b {}\n");
        write("shared/c.css", ".c {}\n");

        String css = ThemeCssBundler.bundle(root, "t/entry.css");

        assertFalse(css.contains("@import"));
        assertInOrder(css, "/* t/a.css */", "/* shared/c.css */", ".c {}", ".a {}", "/* t/b.css */", ".b {}",
                ".entry {}");
    }

    @Test
    void bundle_acceptsUrlImportForm() {
        write("t/entry.css", "@import url('./a.css');\n@import url(./b.css);\n");
        write("t/a.css", ".a {}\n");
        write("t/b.css", ".b {}\n");

        assertInOrder(ThemeCssBundler.bundle(root, "t/entry.css"), "/* t/a.css */", ".a {}", "/* t/b.css */", ".b {}");
    }

    @Test
    void bundle_acceptsCaseInsensitiveImportWithoutSpace() {
        write("t/entry.css", "@IMPORT './a.css';\n@import\"./b.css\";\n");
        write("t/a.css", ".a {}\n");
        write("t/b.css", ".b {}\n");

        String css = ThemeCssBundler.bundle(root, "t/entry.css");

        assertFalse(css.toLowerCase().contains("@import"));
        assertInOrder(css, "/* t/a.css */", ".a {}", "/* t/b.css */", ".b {}");
    }

    @Test
    void bundle_inlinesRepeatedImportAtEachPosition() {
        write("t/entry.css", "@import './a.css';\n@import './b.css';\n@import './a.css';\n");
        write("t/a.css", ".a {}\n");
        write("t/b.css", ".b {}\n");

        assertInOrder(ThemeCssBundler.bundle(root, "t/entry.css"), "/* t/a.css */", "/* t/b.css */", "/* t/a.css */");
    }

    @Test
    void bundle_ignoresImportsInComments() {
        write("t/entry.css", "/* @import './missing.css'; */\n@import './a.css';\n");
        write("t/a.css", ".a {}\n");

        String css = ThemeCssBundler.bundle(root, "t/entry.css");

        assertTrue(css.contains("/* @import './missing.css'; */"));
        assertTrue(css.contains(".a {}"));
    }

    @Test
    void bundle_doesNotTreatCommentMarkerInStringAsComment() {
        write("t/entry.css", "@import './sub/a.css';\n");
        write("t/sub/a.css", ".a::before { content: \"/*\"; }\n.b { background: url('img.png'); }\n");

        assertTrue(ThemeCssBundler.bundle(root, "t/entry.css").contains("url('sub/img.png')"));
    }

    @Test
    void bundle_stripsLicenseHeaderOfInlinedFilesOnly() {
        String header = "/*\n * Copyright 2026 Haulmont.\n * Licensed under the Apache License, Version 2.0\n */\n";
        write("t/entry.css", header + "@import './a.css';\n");
        write("t/a.css", header + ".a {}\n");

        String css = ThemeCssBundler.bundle(root, "t/entry.css");

        assertTrue(css.startsWith("/*\n * Copyright 2026 Haulmont."));
        assertEquals(1, css.split("Licensed under the Apache License", -1).length - 1);
    }

    @Test
    void bundle_keepsLeadingCommentThatIsNotTheLicenseHeader() {
        write("t/entry.css", "@import './a.css';\n");
        write("t/a.css", "/** @license Copyright (c) Vaadin Ltd. */\n.a {}\n");

        assertInOrder(ThemeCssBundler.bundle(root, "t/entry.css"), "/* t/a.css */",
                "/** @license Copyright (c) Vaadin Ltd. */", ".a {}");
    }

    @Test
    void bundle_rewritesRelativeUrlsToEntryDirectory() {
        write("t/entry.css", "@import '../shared/icons/a.css';\n");
        write("shared/icons/a.css", """
                @font-face { src: url("Font.woff") format("woff"), url('Font.eot?#iefix'); }
                .x { background: url(./img/x.png); }
                .d { background: url(data:font/woff;base64,AAAA); mask: url("data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg'/>"); }
                .k { background: url(/abs.png), url(https://example.com/y.png), url(#frag); }
                """);

        String css = ThemeCssBundler.bundle(root, "t/entry.css");

        assertTrue(css.contains("url(\"../shared/icons/Font.woff\")"));
        assertTrue(css.contains("url('../shared/icons/Font.eot?#iefix')"));
        assertTrue(css.contains("url(../shared/icons/img/x.png)"));
        assertTrue(css.contains("url(data:font/woff;base64,AAAA)"));
        assertTrue(css.contains("url(\"data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg'/>\")"));
        assertTrue(css.contains("url(/abs.png), url(https://example.com/y.png), url(#frag)"));
    }

    @Test
    void bundle_stripsByteOrderMark() {
        write("t/entry.css", "@import './a.css';\n");
        write("t/a.css", "﻿.a {}\n");

        String css = ThemeCssBundler.bundle(root, "t/entry.css");

        assertFalse(css.contains("﻿"));
        assertTrue(css.contains(".a {}"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"@import './a.css' screen;", "@import './a.css' layer(base);",
            "@import './a.css' supports(display: grid);", "@import '/abs.css';",
            "@import 'https://example.com/a.css';", "@import './a.css?v=1';", "@import a.css;", "@charset \"UTF-8\";"})
    void bundle_failsOnUnsupportedStatement(String statement) {
        write("t/entry.css", "\n" + statement + "\n");
        write("t/a.css", ".a {}\n");

        assertMessageStartsWith("t/entry.css:2: ", () -> ThemeCssBundler.bundle(root, "t/entry.css"));
    }

    @Test
    void bundle_failsOnMissingImport() {
        write("t/entry.css", "@import './a.css';\n");
        write("t/a.css", "\n@import './missing.css';\n");

        assertMessageStartsWith("t/a.css:2: ", () -> ThemeCssBundler.bundle(root, "t/entry.css"));
    }

    @Test
    void bundle_failsOnImportOutsideThemesRoot() throws IOException {
        Files.writeString(tempDir.resolve("outside.css"), ".o {}\n");
        write("t/entry.css", "@import '../../outside.css';\n");

        assertMessageStartsWith("t/entry.css:1: ", () -> ThemeCssBundler.bundle(root, "t/entry.css"));
    }

    @Test
    void bundle_failsOnCycle() {
        write("t/entry.css", "@import './a.css';\n");
        write("t/a.css", "@import './b.css';\n");
        write("t/b.css", "@import './a.css';\n");

        assertMessageStartsWith("t/b.css:1: ", () -> ThemeCssBundler.bundle(root, "t/entry.css"));
    }

    @Test
    void bundle_failsOnImportAfterRule() {
        write("t/entry.css", ".x {}\n@import './a.css';\n");
        write("t/a.css", ".a {}\n");

        assertMessageStartsWith("t/entry.css:2: ", () -> ThemeCssBundler.bundle(root, "t/entry.css"));
    }

    @Test
    void bundle_failsOnUnterminatedComment() {
        write("t/entry.css", "@import './a.css';\n@import './b.css';\n");
        write("t/a.css", ".a { color: red; }\n/* never closed\n");
        write("t/b.css", ".b {}\n");

        assertMessageStartsWith("t/a.css:2: ", () -> ThemeCssBundler.bundle(root, "t/entry.css"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"\n.a { color: red;\n", "\n.a { width: calc(1px + 2px; }\n", "\n.a[part='x' {}\n",
            "\n.a { color: red; }}\n"})
    void bundle_failsOnUnbalancedBrackets(String content) {
        write("t/entry.css", "@import './a.css';\n@import './b.css';\n");
        write("t/a.css", content);
        write("t/b.css", ".b {}\n");

        assertMessageStartsWith("t/a.css:2: ", () -> ThemeCssBundler.bundle(root, "t/entry.css"));
    }

    @Test
    void bundle_failsOnInvalidUtf8WithFileAndLine() throws IOException {
        write("t/entry.css", "@import './a.css';\n");
        Files.createDirectories(root.resolve("t"));
        // Line 2 holds a Latin-1 'é' (0xE9), which is not valid UTF-8.
        Files.write(root.resolve("t/a.css"), new byte[]{'.', 'a', ' ', '{', '}', '\n', '/', '*', ' ', (byte) 0xE9,
                ' ', '*', '/', '\n'});

        assertMessageStartsWith("t/a.css:2: ", () -> ThemeCssBundler.bundle(root, "t/entry.css"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"image-set(\"img/x.png\" 1x)", "-webkit-image-set('img/x.png' 1x)",
            "image-set(url(img/a.png) 1x, \"img/b.png\" 2x)"})
    void bundle_failsOnStringUrlInImageSet(String value) {
        write("t/entry.css", "@import './sub/a.css';\n");
        write("t/sub/a.css", "\n.e { background: " + value + "; }\n");

        assertMessageStartsWith("t/sub/a.css:2: ", () -> ThemeCssBundler.bundle(root, "t/entry.css"));
    }

    @Test
    void bundle_rewritesUrlsInImageSetAndKeepsTypeStrings() {
        write("t/entry.css", "@import './sub/a.css';\n");
        write("t/sub/a.css",
                ".e { background: image-set(url(img/a.avif) type(\"image/avif\"), url('img/a.png') 1x); }\n");

        assertTrue(ThemeCssBundler.bundle(root, "t/entry.css")
                .contains("image-set(url(sub/img/a.avif) type(\"image/avif\"), url('sub/img/a.png') 1x)"));
    }

    @Test
    void main_writesEachEntryAndRemovesStaleOutput() throws IOException {
        write("t/entry.css", "@import './a.css';\n");
        write("t/a.css", ".a {}\n");
        write("u/entry.css", ".u {}\n");
        Path out = Files.createDirectories(tempDir.resolve("out"));
        Files.writeString(out.resolve("stale.css"), "");

        ThemeCssBundler.main(new String[]{root.toString(), out.toString(), "t/entry.css", "u/entry.css"});

        assertTrue(Files.readString(out.resolve("t/entry.css")).contains(".a {}"));
        assertTrue(Files.exists(out.resolve("u/entry.css")));
        assertFalse(Files.exists(out.resolve("stale.css")));
    }

    void write(String path, String content) {
        try {
            Path file = root.resolve(path);
            Files.createDirectories(file.getParent());
            Files.writeString(file, content);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    void assertInOrder(String text, String... parts) {
        int prev = -1;
        for (String part : parts) {
            int idx = text.indexOf(part, prev + 1);
            assertTrue(idx >= 0, "'" + part + "' not found after index " + prev + " in:\n" + text);
            prev = idx;
        }
    }

    void assertMessageStartsWith(String prefix, Executable call) {
        ThemeCssBundleException e = assertThrows(ThemeCssBundleException.class, call);
        assertTrue(e.getMessage().startsWith(prefix), "Unexpected message: " + e.getMessage());
    }
}
