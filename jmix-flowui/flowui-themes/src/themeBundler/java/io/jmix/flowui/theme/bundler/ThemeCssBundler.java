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

package io.jmix.flowui.theme.bundler;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Build-time tool that turns a theme entry stylesheet into a single stylesheet by inlining its whole
 * {@code @import} tree, so that a content hash of the entry covers every file of the theme.
 * <p>
 * Each import is replaced, depth-first and in place, by a marker comment with the imported file's path relative to
 * the themes root followed by the bundled content of that file. Relative {@code url()} references are rewritten to
 * resolve against the entry's directory. Imports with conditions, non-relative imports, missing targets, targets
 * outside the themes root, cycles, {@code @charset}, imports that follow other rules, unterminated comments,
 * unbalanced brackets and string URLs in {@code image-set()} fail the bundling.
 */
public final class ThemeCssBundler {

    private static final String LICENSE_TEXT = "Licensed under the Apache License";
    private static final Pattern SCHEME = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+.-]*:");

    private final Path themesRoot;
    private final Path entryDir;
    private final Deque<Path> stack = new ArrayDeque<>();

    private ThemeCssBundler(Path themesRoot, Path entryDir) {
        this.themesRoot = themesRoot;
        this.entryDir = entryDir;
    }

    /**
     * Bundles one theme entry stylesheet.
     *
     * @param themesRoot the themes root directory
     * @param entry      the entry path relative to the themes root, {@code /}-separated
     * @return the bundled CSS
     * @throws ThemeCssBundleException if the entry or a file it imports cannot be bundled
     */
    public static String bundle(Path themesRoot, String entry) {
        Path root = themesRoot.toAbsolutePath().normalize();
        Path entryFile = root.resolve(entry).normalize();
        return new ThemeCssBundler(root, entryFile.getParent()).process(entryFile, false);
    }

    /**
     * Bundles theme entries into an output directory, replacing its previous content.
     *
     * @param args the themes root, the output directory and the entry paths relative to the themes root
     * @throws IOException if reading or writing a file fails
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            throw new IllegalArgumentException("Usage: ThemeCssBundler <themes root> <output dir> <entry>...");
        }
        Path themesRoot = Path.of(args[0]);
        Path outputDir = Files.createDirectories(Path.of(args[1]));
        deleteContent(outputDir);

        for (int i = 2; i < args.length; i++) {
            String entry = args[i];
            String css = bundle(themesRoot, entry);
            checkNoImportLeft(entry, css);

            Path output = outputDir.resolve(entry);
            Files.createDirectories(output.getParent());
            Files.writeString(output, css, StandardCharsets.UTF_8);
        }
    }

    private String process(Path file, boolean inlined) {
        String text = read(file);
        if (text.startsWith("﻿")) {
            text = text.substring(1);
        }
        int start = inlined ? licenseHeaderEnd(text) : 0;

        stack.push(file);
        StringBuilder out = new StringBuilder();
        Deque<Integer> openBrackets = new ArrayDeque<>();
        int copied = start;
        boolean ruleSeen = false;
        int i = start;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (text.startsWith("/*", i)) {
                int end = text.indexOf("*/", i + 2);
                if (end < 0) {
                    throw error(file, text, i, "unterminated comment");
                }
                i = end + 2;
            } else if (c == '"' || c == '\'') {
                checkNotImageSetUrl(file, text, i, openBrackets);
                i = stringEnd(text, i);
                ruleSeen = true;
            } else if (c == '\\') {
                i += 2;
                ruleSeen = true;
            } else if (isKeyword(text, i, "@import")) {
                if (ruleSeen) {
                    throw error(file, text, i, "@import after other rules is not supported");
                }
                Import statement = parseImport(file, text, i);
                out.append(text, copied, i).append(inline(file, text, i, statement.path()));
                copied = statement.end();
                i = statement.end();
            } else if (isKeyword(text, i, "@charset")) {
                throw error(file, text, i, "@charset is not supported");
            } else if (isKeyword(text, i, "url(") && (i == 0 || !isNameChar(text.charAt(i - 1)))) {
                int[] value = urlValue(text, i);
                out.append(text, copied, value[0]).append(rewriteUrl(file, text.substring(value[0], value[1])));
                copied = value[1];
                i = value[2];
                ruleSeen = true;
            } else {
                trackBracket(file, text, i, openBrackets);
                ruleSeen |= !Character.isWhitespace(c);
                i++;
            }
        }
        // An unclosed block would swallow every file inlined after this one, unlike a separate stylesheet.
        if (!openBrackets.isEmpty()) {
            throw error(file, text, openBrackets.peek(), "unclosed '" + text.charAt(openBrackets.peek()) + "'");
        }
        out.append(text, copied, text.length());
        stack.pop();
        return out.toString();
    }

    /**
     * Pushes the position of an opening bracket at {@code pos} and pops it for the matching closing bracket.
     */
    private void trackBracket(Path file, String text, int pos, Deque<Integer> openBrackets) {
        int opening = "{([".indexOf(text.charAt(pos));
        int closing = "})]".indexOf(text.charAt(pos));
        if (opening >= 0) {
            openBrackets.push(pos);
        } else if (closing >= 0) {
            if (openBrackets.isEmpty() || "{([".indexOf(text.charAt(openBrackets.peek())) != closing) {
                throw error(file, text, pos, "unbalanced '" + text.charAt(pos) + "'");
            }
            openBrackets.pop();
        }
    }

    /**
     * Fails on a string directly inside {@code image-set()}: it is a URL relative to the stylesheet, which the
     * bundler does not rewrite.
     */
    private void checkNotImageSetUrl(Path file, String text, int pos, Deque<Integer> openBrackets) {
        Integer bracket = openBrackets.peek();
        if (bracket != null && text.charAt(bracket) == '('
                && text.regionMatches(true, bracket - "image-set".length(), "image-set", 0, "image-set".length())) {
            throw error(file, text, pos, "a string URL in image-set() is not supported, use url()");
        }
    }

    private String inline(Path file, String text, int pos, String path) {
        Path target = file.getParent().resolve(path).normalize();
        if (!target.startsWith(themesRoot)) {
            throw error(file, text, pos, "import target is outside the themes root: " + path);
        }
        if (!Files.isRegularFile(target)) {
            throw error(file, text, pos, "import target not found: " + path);
        }
        if (stack.contains(target)) {
            throw error(file, text, pos, "import cycle: " + path);
        }
        String content = process(target, true);
        return "/* " + relativePath(target) + " */\n" + content + (content.endsWith("\n") ? "" : "\n");
    }

    /**
     * Parses and validates the {@code @import} statement at {@code pos}.
     */
    private Import parseImport(Path file, String text, int pos) {
        int i = skipWhitespace(text, pos + "@import".length());
        boolean urlForm = isKeyword(text, i, "url(");
        if (urlForm) {
            i = skipWhitespace(text, i + "url(".length());
        }
        String path;
        if (i < text.length() && (text.charAt(i) == '"' || text.charAt(i) == '\'')) {
            int end = stringEnd(text, i);
            if (end - i < 2 || text.charAt(end - 1) != text.charAt(i)) {
                throw error(file, text, pos, "unterminated @import path");
            }
            path = text.substring(i + 1, end - 1);
            i = end;
        } else if (urlForm) {
            int end = i;
            while (end < text.length() && text.charAt(end) != ')' && !Character.isWhitespace(text.charAt(end))) {
                end++;
            }
            path = text.substring(i, end);
            i = end;
        } else {
            throw error(file, text, pos, "unsupported @import form");
        }
        if (urlForm) {
            i = skipWhitespace(text, i);
            if (i >= text.length() || text.charAt(i) != ')') {
                throw error(file, text, pos, "unsupported @import form");
            }
            i++;
        }

        i = skipWhitespace(text, i);
        if (i >= text.length() || text.charAt(i) != ';') {
            throw error(file, text, pos, "@import with media, supports() or layer() conditions is not supported");
        }
        if (path.isEmpty() || path.startsWith("/") || SCHEME.matcher(path).find()) {
            throw error(file, text, pos, "only relative @import paths are supported: " + path);
        }
        if (path.indexOf('?') >= 0 || path.indexOf('#') >= 0) {
            throw error(file, text, pos, "@import path with a query or fragment is not supported: " + path);
        }
        return new Import(path, i + 1);
    }

    /**
     * Returns the start and end of the value of the {@code url()} at {@code pos}, without quotes, and the position
     * after the closing parenthesis.
     */
    private static int[] urlValue(String text, int pos) {
        int i = skipWhitespace(text, pos + "url(".length());
        if (i < text.length() && (text.charAt(i) == '"' || text.charAt(i) == '\'')) {
            int end = stringEnd(text, i);
            int valueEnd = end - i >= 2 && text.charAt(end - 1) == text.charAt(i) ? end - 1 : end;
            int close = skipWhitespace(text, end);
            return new int[]{i + 1, valueEnd, close < text.length() && text.charAt(close) == ')' ? close + 1 : end};
        }
        int end = i;
        while (end < text.length() && text.charAt(end) != ')') {
            end += text.charAt(end) == '\\' ? 2 : 1;
        }
        end = Math.min(end, text.length());
        int valueEnd = end;
        while (valueEnd > i && Character.isWhitespace(text.charAt(valueEnd - 1))) {
            valueEnd--;
        }
        return new int[]{i, valueEnd, Math.min(end + 1, text.length())};
    }

    private String rewriteUrl(Path file, String url) {
        if (url.isEmpty() || url.startsWith("/") || url.startsWith("#") || SCHEME.matcher(url).find()) {
            return url;
        }
        int suffixStart = url.length();
        for (int i = 0; i < url.length(); i++) {
            if (url.charAt(i) == '?' || url.charAt(i) == '#') {
                suffixStart = i;
                break;
            }
        }
        String path = url.substring(0, suffixStart);
        Path target = path.isEmpty() ? file : file.getParent().resolve(path).normalize();
        return toUrlPath(entryDir.relativize(target)) + url.substring(suffixStart);
    }

    private ThemeCssBundleException error(Path file, String text, int pos, String message) {
        int line = 1;
        for (int i = 0; i < pos; i++) {
            if (text.charAt(i) == '\n') {
                line++;
            }
        }
        return new ThemeCssBundleException(relativePath(file), line, message);
    }

    private String relativePath(Path file) {
        return toUrlPath(themesRoot.relativize(file));
    }

    private static void checkNoImportLeft(String entry, String css) {
        int i = 0;
        while (i < css.length()) {
            if (css.startsWith("/*", i)) {
                i = commentEnd(css, i);
            } else if (css.charAt(i) == '"' || css.charAt(i) == '\'') {
                i = stringEnd(css, i);
            } else if (css.charAt(i) == '\\') {
                i += 2;
            } else if (isKeyword(css, i, "@import")) {
                int line = (int) css.substring(0, i).chars().filter(c -> c == '\n').count() + 1;
                throw new ThemeCssBundleException(entry, line, "@import left in the bundle");
            } else {
                i++;
            }
        }
    }

    /**
     * Returns the position after the leading license comment and the whitespace that follows it, or {@code 0} if
     * the text does not start with such a comment.
     */
    private static int licenseHeaderEnd(String text) {
        int i = skipWhitespace(text, 0);
        if (text.startsWith("/*", i)) {
            int end = text.indexOf("*/", i + 2);
            if (end >= 0 && text.substring(i, end).contains(LICENSE_TEXT)) {
                return skipWhitespace(text, end + 2);
            }
        }
        return 0;
    }

    private static int commentEnd(String text, int pos) {
        int end = text.indexOf("*/", pos + 2);
        return end < 0 ? text.length() : end + 2;
    }

    /**
     * Returns the position after the string literal that starts at {@code pos}. An unterminated string ends at the
     * line break, as in CSS.
     */
    private static int stringEnd(String text, int pos) {
        char quote = text.charAt(pos);
        int i = pos + 1;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '\\') {
                i += 2;
            } else if (c == quote) {
                return i + 1;
            } else if (c == '\n') {
                return i;
            } else {
                i++;
            }
        }
        return text.length();
    }

    private static boolean isKeyword(String text, int pos, String keyword) {
        if (!text.regionMatches(true, pos, keyword, 0, keyword.length())) {
            return false;
        }
        int next = pos + keyword.length();
        return keyword.endsWith("(") || next >= text.length() || !isNameChar(text.charAt(next));
    }

    private static boolean isNameChar(char c) {
        return Character.isLetterOrDigit(c) || c == '-' || c == '_';
    }

    private static int skipWhitespace(String text, int pos) {
        int i = pos;
        while (i < text.length() && Character.isWhitespace(text.charAt(i))) {
            i++;
        }
        return i;
    }

    private static String toUrlPath(Path path) {
        return Stream.iterate(0, i -> i < path.getNameCount(), i -> i + 1)
                .map(i -> path.getName(i).toString())
                .collect(Collectors.joining("/"));
    }

    /**
     * Reads the file as UTF-8, failing with the file and line of the first invalid byte sequence.
     */
    private String read(Path file) {
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        ByteBuffer input = ByteBuffer.wrap(bytes);
        CharBuffer output = CharBuffer.allocate(bytes.length);
        if (StandardCharsets.UTF_8.newDecoder().decode(input, output, true).isError()) {
            int line = 1;
            for (int i = 0; i < input.position(); i++) {
                if (bytes[i] == '\n') {
                    line++;
                }
            }
            throw new ThemeCssBundleException(relativePath(file), line, "not valid UTF-8");
        }
        return output.flip().toString();
    }

    private static void deleteContent(Path dir) throws IOException {
        List<Path> paths;
        try (Stream<Path> walk = Files.walk(dir)) {
            paths = walk.filter(path -> !path.equals(dir))
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList());
        }
        for (Path path : paths) {
            Files.delete(path);
        }
    }

    /**
     * An {@code @import} statement: the imported path and the position after the statement's {@code ;}.
     */
    private record Import(String path, int end) {
    }
}
