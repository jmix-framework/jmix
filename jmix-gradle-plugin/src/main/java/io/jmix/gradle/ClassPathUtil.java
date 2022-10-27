/*
 * Copyright (c) 2008-2020 Haulmont.
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

package io.jmix.gradle;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ClassPathUtil {

    public static void createClassPathFile(File classPathFile, List<File> classPath) {
        try (PrintWriter writer = new PrintWriter(classPathFile)) {
            for (File file : classPath) {
                writer.println(file.getAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write classpath to temporary file", e);
        }
    }

    public static void createFormattedClassPathFile(File classPathFile, List<File> classPath) {
        try (PrintWriter writer = new PrintWriter(classPathFile)) {
            writer.print("-cp ");
            for (int i = 0; i < classPath.size(); i++) {
                File file = classPath.get(i);
                writer.print(file.getAbsolutePath() + ";");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write classpath to temporary file", e);
        }
    }

    public static List<File> getCommandLineClassPath() {
        ClassLoader classLoader = ClassPathCommandLine.class.getClassLoader();
        if (classLoader instanceof URLClassLoader) {
            URL[] urls = ((URLClassLoader) (ClassPathCommandLine.class.getClassLoader())).getURLs();
            return Arrays.stream(urls)
                    .map(url -> new File(url.getPath()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public static List<String> getExtendedCommandLineAgs(String classPathFile, String mainClass, List<String> args) {
        List<String> extendedArgs = new ArrayList<>();

        extendedArgs.add(classPathFile);
        extendedArgs.add(mainClass);
        extendedArgs.addAll(args);

        return extendedArgs;
    }
}
