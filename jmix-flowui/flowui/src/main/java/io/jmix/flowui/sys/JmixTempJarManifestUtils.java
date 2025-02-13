package io.jmix.flowui.sys;

import org.apache.commons.lang3.SystemUtils;
import org.springframework.core.io.UrlResource;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;

/**
 * Workaround on Windows OS if you build project using gradle with long class path.
 * <p>
 * Also see issue and comments:
 * <a href="https://github.com/jmix-framework/jmix/issues/1571">jmix-framework/jmix#1571</a>
 */
public class JmixTempJarManifestUtils {

    public static boolean isGradleTempJarClassPathUsed() {
        String[] paths = StringUtils.delimitedListToStringArray(
                System.getProperty("java.class.path"),
                System.getProperty("path.separator"));

        return SystemUtils.IS_OS_WINDOWS && paths.length == 1;
    }

    public static String getClassPath() {
        String rawClassPath = getRawClassPath();

        // PathMatchingResourcePatternResolver#addClassPathManifestEntries()
        // applies string without spaces. "file:/" occurrences should be
        // replaced by "path.separator" property.
        String classPath = rawClassPath
                .replaceAll("\\s", "")
                .replaceAll("file:/", System.getProperty("path.separator"))
                .trim();

        // After string modification it lefts ";" char before the class path.
        // We should delete it.
        classPath = classPath.substring(classPath.indexOf(System.getProperty("path.separator")) + 1);

        return classPath;
    }

    protected static String getRawClassPath() {
        String[] paths = StringUtils.delimitedListToStringArray(
                System.getProperty("java.class.path"), System.getProperty("path.separator"));

        String filePath = new File(paths[0]).getAbsolutePath();
        int prefixIndex = filePath.indexOf(':');
        if (prefixIndex == 1) {
            // Possibly "c:" drive prefix on Windows, to be upper-cased for proper duplicate detection
            filePath = StringUtils.capitalize(filePath);
        }

        UrlResource urlResource;

        try {
            urlResource = new UrlResource(
                    ResourceUtils.JAR_URL_PREFIX +
                            ResourceUtils.FILE_URL_PREFIX + filePath +
                            ResourceUtils.JAR_URL_SEPARATOR + "META-INF/MANIFEST.MF");
        } catch (MalformedURLException e) {
            throw new RuntimeException("Cannot find temporary jar file", e);
        }

        try (InputStreamReader reader = new InputStreamReader(urlResource.getInputStream(), StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            StringBuilder sb = new StringBuilder();

            while (bufferedReader.ready()) {
                sb.append(bufferedReader.readLine());
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException("Cannot read MANIFEST.MF from temporary jar file", e);
        }
    }
}
