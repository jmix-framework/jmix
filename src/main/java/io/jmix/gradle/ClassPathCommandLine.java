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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class ClassPathCommandLine {

    public static void main(String[] args) {
        List<String> classPath = readAndRemoveFile(args[0]);

        ClassLoader classLoader = buildClassLoader(classPath);
        Thread.currentThread().setContextClassLoader(classLoader);

        System.setProperty("java.class.path", StringUtils.join(classPath, ";"));

        //System.setProperty("java.class.path",
        //        StringUtils.join(classPath, SystemUtils.IS_OS_WINDOWS ? ";" : ":"));

        invokeOriginalMainClass(classLoader, args[1], getOriginalArgs(args));
    }

    private static void invokeOriginalMainClass(ClassLoader classLoader, String mainClass, String[] args) {
        try {
            Class<?> cls = classLoader.loadClass(mainClass);
            Method mainMethod = cls.getMethod("main", String[].class);
            mainMethod.invoke(null, new Object[]{args});
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unable to run Main-Class:" + mainClass, e);
        }
    }

    private static String[] getOriginalArgs(String[] args) {
        if (args != null) {
            return ArrayUtils.subarray(args, 2, args.length);
        }
        return new String[0];
    }

    private static ClassLoader buildClassLoader(List<String> classPath) {
        List<URL> urls = new ArrayList<>();
        try {
            for (String str : classPath) {
                urls.add(new File(str).toURI().toURL());
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Unable to build classpath:", e);
        }
        return new URLClassLoader(urls.toArray(new URL[0]), null);
    }

    private static List<String> readAndRemoveFile(String path) {
        List<String> lines;
        try {
            lines = FileUtils.readLines(new File(path), Charset.defaultCharset());
            FileUtils.deleteQuietly(new File(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lines;
    }
}
