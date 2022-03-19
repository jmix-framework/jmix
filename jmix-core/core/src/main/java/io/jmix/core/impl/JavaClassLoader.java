/*
 * Copyright 2019 Haulmont.
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

package io.jmix.core.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import io.jmix.core.CoreProperties;
import io.jmix.core.TimeSource;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component("core_JavaClassLoader")
public class JavaClassLoader extends URLClassLoader {

    private static final Logger log = LoggerFactory.getLogger(JavaClassLoader.class);

    protected final Set<String> rootDirs;

    protected final Map<String, TimestampClass> loaded = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<String, Lock> locks = new ConcurrentHashMap<>();

    protected final ProxyClassLoader proxyClassLoader;
    protected final Map<String, ClassFilesProvider> classFilesProviders;

    @Autowired
    protected TimeSource timeSource;
    @Autowired
    protected SpringBeanLoader springBeanLoader;
    @Autowired
    protected MeterRegistry meterRegistry;

    @Autowired
    public JavaClassLoader(CoreProperties coreProperties) {
        super(new URL[0], Thread.currentThread().getContextClassLoader());

        this.proxyClassLoader = new ProxyClassLoader(Thread.currentThread().getContextClassLoader(), loaded);
        this.rootDirs = Sets.newHashSet(coreProperties.getConfDir()); //getRootPaths(); ToDo: multiple root paths
        this.classFilesProviders = new HashMap<>();
        for (String dir : this.rootDirs) {
            this.classFilesProviders.put(dir, new ClassFilesProvider(dir));
        }
    }

    //Please use this constructor only in tests
    JavaClassLoader(ClassLoader parent, String rootDir, Set<String> rootDirs, SpringBeanLoader springBeanLoader) {
        super(new URL[0], parent);

        Preconditions.checkNotNull(rootDir);

        this.proxyClassLoader = new ProxyClassLoader(parent, loaded);
        this.springBeanLoader = springBeanLoader;
        this.rootDirs = rootDirs;
        this.classFilesProviders = new HashMap<>();
        for (String dir : this.rootDirs) {
            this.classFilesProviders.put(dir, new ClassFilesProvider(dir));
        }
    }

    public void clearCache() {
        loaded.clear();
    }

    @Override
    public Class loadClass(final String fullClassName, boolean resolve) throws ClassNotFoundException {
        String containerClassName = StringUtils.substringBefore(fullClassName, "$");

        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            lock(containerClassName);
            Class clazz;

            //first check if there is a ".class" file in the root directories
            for (ClassFilesProvider classFilesProvider : classFilesProviders.values()) {
                File classFile = classFilesProvider.getClassFile(containerClassName);
                if (classFile.exists()) {
                    return loadClassFromClassFile(fullClassName, containerClassName, classFile);
                }
            }

            //default class loading
            clazz = super.loadClass(fullClassName, resolve);
            return clazz;
        } finally {
            unlock(containerClassName);
            sample.stop(meterRegistry.timer("jmix.JavaClassLoader.loadClass"));
        }
    }

    protected Class loadClassFromClassFile(String fullClassName, String containerClassName, File classFile) {
        TimestampClass timestampClass = loaded.get(containerClassName);
        if (timestampClass != null && !FileUtils.isFileNewer(classFile, timestampClass.timestamp)) {
            return timestampClass.clazz;
        }
        Map<String, Class> loadedClasses = new HashMap<>();
        Map<String, String> modifiedClassFiles = new HashMap<>();
        Map<String, FileClassLoader> fileClassLoaders = new HashMap<>();
        for (String dir : rootDirs) {
            Set<String> modifiedClassFilesForDir = collectModifiedClassFiles(dir);
            fileClassLoaders.put(dir, new FileClassLoader(proxyClassLoader, dir, modifiedClassFiles.keySet()));
            for (String fqn : modifiedClassFilesForDir)
                modifiedClassFiles.put(fqn, dir);
        }
        for (Map.Entry<String, String> entry : modifiedClassFiles.entrySet()) {
            String fqn = entry.getKey();
            FileClassLoader fileClassLoader = fileClassLoaders.get(entry.getValue());
            Class clazz;
            try {
                clazz = fileClassLoader.loadClass(fqn);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Class not found", e);
            }
            loadedClasses.put(fqn, clazz);
            loaded.put(fqn, new TimestampClass(clazz, getCurrentTimestamp()));
        }
        springBeanLoader.updateContext(loadedClasses.values());
        return loadedClasses.get(fullClassName);
    }

    /**
     * Collects class files that were modified or not loaded yet from the directory
     */
    protected Set<String> collectModifiedClassFiles(String rootDir) {
        Set<String> result = new HashSet<>();
        Path root = Paths.get(rootDir);
        try {
            Files.walk(root)
                    .forEach(path -> {
                        if (Files.isDirectory(path) || !path.toString().endsWith(".class")) {
                            return;
                        }
                        String fqn = root.relativize(path).toString();
                        fqn = fqn.substring(0, fqn.length() - 6).replace(File.separator, ".");
                        TimestampClass timeStampClass = getTimestampClass(fqn);
                        if (timeStampClass == null || FileUtils.isFileNewer(path.toFile(), timeStampClass.timestamp)) {
                            result.add(fqn);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("Error on traversing the directory " + rootDir, e);
        }
        return result;
    }

    /**
     * Class loader is used for building class instances from ".class" files. Loading from file happens only for classes with FQN from the {@code
     * modifiedFQNs} collection passed to the constructor. Loading of all other classes is delegated to parent class loaders.
     */
    protected static class FileClassLoader extends ClassLoader {

        private final Map<String, Class> loadedClasses = new HashMap<>();
        private String rootDir;
        private Set<String> modifiedFQNs;

        /**
         * @param parent       parent class loader
         * @param rootDir      a root directory
         * @param modifiedFQNs a set of classes fully qualified names that should be loaded by this class loader
         */
        FileClassLoader(ClassLoader parent, String rootDir, Set<String> modifiedFQNs) {
            super(parent);
            this.rootDir = rootDir;
            this.modifiedFQNs = modifiedFQNs;
        }

        @Override
        protected Class<?> loadClass(String fqn, boolean resolve) throws ClassNotFoundException {
            Class clazz = loadedClasses.get(fqn);
            if (clazz != null) return clazz;
            if (modifiedFQNs.contains(fqn)) {
                Path pathToClassFile = fqnToPath(fqn);
                if (Files.exists(pathToClassFile)) {
                    try {
                        byte[] bytes = Files.readAllBytes(pathToClassFile);
                        clazz = defineClass(fqn, bytes, 0, bytes.length);
                        loadedClasses.put(fqn, clazz);
                        log.debug("Class {} loaded from directory {}", fqn, rootDir);
                    } catch (IOException e) {
                        throw new RuntimeException("Error on reading file content", e);
                    }
                }
            }
            return super.loadClass(fqn, resolve);
        }

        private Path fqnToPath(String fqn) {
            String[] packageNameParts = fqn.split("\\.");
            packageNameParts[packageNameParts.length - 1] = packageNameParts[packageNameParts.length - 1] + ".class";
            return Paths.get(rootDir, packageNameParts);
        }
    }

    public boolean removeClass(String className) {
        TimestampClass removed = loaded.remove(className);
        if (removed != null) {
            for (String dependent : removed.dependent) {
                removeClass(dependent);
            }
        }
        return removed != null;
    }

    public boolean isLoadedClass(String className) {
        return loaded.containsKey(className);
    }

    public Collection<String> getClassDependencies(String className) {
        TimestampClass timestampClass = loaded.get(className);
        if (timestampClass != null) {
            return timestampClass.dependencies;
        }
        return Collections.emptyList();
    }

    public Collection<String> getClassDependent(String className) {
        TimestampClass timestampClass = loaded.get(className);
        if (timestampClass != null) {
            return timestampClass.dependent;
        }
        return Collections.emptyList();
    }

    @Override
    public URL findResource(String name) {
        if (name.startsWith("/"))
            name = name.substring(1);
        for (String rootDir : rootDirs) {
            File file = new File(rootDir, name);
            if (file.exists()) {
                try {
                    return file.toURI().toURL();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    @Override
    public URL getResource(String name) {
        URL resource = findResource(name);
        if (resource != null)
            return resource;
        else
            return super.getResource(name);
    }

    protected Set<String> getRootPaths() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        URL[] urls;
        Set<String> result = new HashSet<>();
        if (contextClassLoader instanceof URLClassLoader) {
            urls = ((URLClassLoader) contextClassLoader).getURLs();
            for (URL url : urls) {
                String urlString = url.toString();
                if (urlString.startsWith("file:") && urlString.endsWith("/")) {
                    result.add(url.getPath().replaceFirst("^/(.:/)", "$1"));
                }
            }
        } else {
            String[] paths;
            paths = ManagementFactory.getRuntimeMXBean().getClassPath().split(File.pathSeparator);
            for (String path : paths) {
                File file = new File(path);
                if (file.exists() && file.isDirectory()) {
                    result.add(path);
                }
            }
        }

        return result;
    }

    protected Date getCurrentTimestamp() {
        return timeSource.currentTimestamp();
    }

    TimestampClass getTimestampClass(String name) {
        return loaded.get(name);
    }

    private void unlock(String name) {
        locks.get(name).unlock();
    }

    private void lock(String name) {//not sure it's right, but we can not use synchronization here
        locks.putIfAbsent(name, new ReentrantLock());
        locks.get(name).lock();
    }
}
