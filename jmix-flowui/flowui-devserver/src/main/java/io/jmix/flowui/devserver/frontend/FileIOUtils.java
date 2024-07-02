/*
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.jmix.flowui.devserver.frontend;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileIOUtils {

    private FileIOUtils() {
    }

    public static boolean writeIfChanged(File file, List<String> content)
            throws IOException {
        return writeIfChanged(file, String.join("\n", content));
    }

    public static boolean writeIfChanged(File file, String content)
            throws IOException {
        String existingFileContent = getExistingFileContent(file);
        if (content.equals(existingFileContent)) {
            log().debug("skipping writing to file '{}' because content matches",
                    file);
            return false;
        } else {
        log().debug("writing to file '{}' because content does not match",
                file);

        FileUtils.forceMkdirParent(file);
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        return true;
    }
    }

    private static Logger log() {
        return LoggerFactory.getLogger(FileIOUtils.class);
    }

    private static String getExistingFileContent(File file) throws IOException {
        return !file.exists() ? null : FileUtils.readFileToString(file, StandardCharsets.UTF_8);
    }

    public static File getProjectFolderFromClasspath() {
        try {
            URL url = FileIOUtils.class.getClassLoader().getResource(".");
            if (url != null && url.getProtocol().equals("file")) {
                return getProjectFolderFromClasspath(url);
            }
        } catch (Exception var1) {
            Exception e = var1;
            log().warn("Unable to determine project folder using classpath", e);
        }
        return null;

    }

    static File getProjectFolderFromClasspath(URL rootFolder)
            throws URISyntaxException {
        Path path = Path.of(rootFolder.toURI());
        return path.endsWith(Path.of("target", "classes")) ? path.getParent().getParent().toFile() : null;
    }

    public static boolean isProbablyTemporaryFile(File file) {
        return file.getName().endsWith("~");
    }

    public static List<Path> getFilesByPattern(Path baseDir, String pattern) throws IOException {
        if (baseDir != null && baseDir.toFile().exists()) {
            if (pattern == null || pattern.isBlank()) {
                pattern = "*";
            }

            final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
            final List<Path> matchingPaths = new ArrayList();
            Files.walkFileTree(baseDir, new SimpleFileVisitor<Path>() {
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (matcher.matches(file)) {
                        matchingPaths.add(file);
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
            return matchingPaths;
        } else {
            throw new IllegalArgumentException("Base directory is empty or doesn't exist: " + baseDir);
        }
    }

    public static boolean compareIgnoringIndentationAndEOL(String content1, String content2, BiPredicate<String, String> compareFn) {
        return compareFn.test(replaceIndentationAndEOL(content1), replaceIndentationAndEOL(content2));
    }

    public static boolean compareIgnoringIndentationEOLAndWhiteSpace(String content1, String content2, BiPredicate<String, String> compareFn) {
        return compareFn.test(replaceWhiteSpace(replaceIndentationAndEOL(content1)), replaceWhiteSpace(replaceIndentationAndEOL(content2)));
    }

    private static String replaceIndentationAndEOL(String text) {
        return text.replace("\r\n", "\n").replaceFirst("\n$", "").replaceAll("(?m)^(\\s)+", "");
    }

    private static String replaceWhiteSpace(String text) {
        String character;
        for(Iterator var1 = Stream.of("{", "}", ":", "'", "[", "]").toList().iterator(); var1.hasNext(); text = replaceWhiteSpaceAround(text, character)) {
            character = (String)var1.next();
        }

        return text;
    }

    private static String replaceWhiteSpaceAround(String text, String character) {
        return text.replaceAll(String.format("(\\s)*\\%s", character), character).replaceAll(String.format("\\%s(\\s)*", character), character);
    }
}
