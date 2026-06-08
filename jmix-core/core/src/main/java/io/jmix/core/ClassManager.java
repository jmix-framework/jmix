/*
 * Copyright 2020 Haulmont.
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

package io.jmix.core;

import io.jmix.core.impl.JavaClassLoader;
import org.springframework.stereotype.Component;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Loads classes taking into account hot-deploy feature.
 */
@Component("core_ClassManager")
public class ClassManager {

    @Autowired
    protected JavaClassLoader javaClassLoader;

    /**
     * Finds class by name and loads if found
     *
     * @param className fully qualified class name
     * @return class or null if not found
     */
    @Nullable
    public Class<?> findClass(String className) {
        try {
            return javaClassLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Loads a class by name
     *
     * @param className fully qualified class name
     * @return class
     * @throws IllegalStateException if the class is not found
     */
    public Class<?> loadClass(String className) {
        try {
            return javaClassLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Unable to load class", e);
        }
    }

    /**
     * Loads a generated class from bytecode and replaces the cached class with the same name if it exists.
     *
     * @param className fully qualified class name
     * @param bytes     bytecode
     * @return loaded class
     */
    public Class<?> loadGeneratedClass(String className, byte[] bytes) {
        return javaClassLoader.loadGeneratedClass(className, bytes);
    }

    /**
     * Loads generated classes from bytecode and replaces cached classes with the same names if they exist.
     *
     * @param generatedClasses bytecode by fully qualified class name
     * @return loaded classes by fully qualified class name
     */
    public Map<String, Class<?>> loadGeneratedClasses(Map<String, byte[]> generatedClasses) {
        return javaClassLoader.loadGeneratedClasses(generatedClasses);
    }

    /**
     * Remove compiled class from cache
     *
     * @return true if class removed from cache
     */
    public boolean removeClass(String className) {
        return javaClassLoader.removeClass(className);
    }

    /**
     * Reloads class by name
     *
     * @param className fully qualified class name
     * @return class or null if not found
     */
    public Class<?> reloadClass(String className) {
        javaClassLoader.removeClass(className);
        return findClass(className);
    }

    /**
     * Clears compiled classes cache
     */
    public void clearCache() {
        javaClassLoader.clearCache();
    }

    public JavaClassLoader getJavaClassLoader() {
        return javaClassLoader;
    }
}
