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

package io.jmix.core.impl.scanning;

import io.jmix.core.JmixModuleDescriptor;
import io.jmix.core.JmixModules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Scans classpath of all Jmix modules used in the application and detects classes according to
 * {@link ClasspathScanCandidateDetector} beans registered in the Spring context.
 * <p>
 * Detected class names are stored and available through the {@link #getClassNames(Class)} method. This method
 * accepts a {@code ClasspathScanCandidateDetector} type and returns names of classes selected by this detector.
 */
@Component("core_JmixModulesClasspathScanner")
public class JmixModulesClasspathScanner extends AbstractClasspathScanner {

    private static final Logger log = LoggerFactory.getLogger(JmixModulesClasspathScanner.class);

    protected MetadataReaderFactory metadataReaderFactory;

    protected List<String> basePackages = Collections.emptyList();

    protected Map<Class<? extends ClasspathScanCandidateDetector>, Set<String>> detectedClasses = new HashMap<>();

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected List<ClasspathScanCandidateDetector> candidateDetectors;

    @Autowired
    public void setMetadataReaderFactory(AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        this.metadataReaderFactory = metadataReaderFactory;
    }

    @Autowired
    public void setJmixComponents(JmixModules jmixModules) {
        basePackages = jmixModules.getAll().stream()
                .map(JmixModuleDescriptor::getBasePackage)
                .collect(Collectors.toList());
    }

    @PostConstruct
    protected void init() {
        log.trace("Scanning packages {} using detectors {}", basePackages, candidateDetectors);
        long startTime = System.currentTimeMillis();

        basePackages.stream()
                .flatMap(this::scanPackage)
                .forEach(metadataReader -> {
                    for (ClasspathScanCandidateDetector detector : candidateDetectors) {
                        if (detector.isCandidate(metadataReader)) {
                            Set<String> classNames = detectedClasses.computeIfAbsent(
                                    detector.getClass(), aClass -> new HashSet<>());
                            classNames.add(metadataReader.getClassMetadata().getClassName());
                        }
                    }
                });

        log.info("Classpath scan completed in {} ms", System.currentTimeMillis() - startTime);
    }

    /**
     * Returns the set of class names selected by a detector of the given type.
     */
    public Set<String> getClassNames(Class<? extends ClasspathScanCandidateDetector> detectorType) {
        return detectedClasses.getOrDefault(detectorType, new HashSet<>());
    }

    /**
     * Refreshes the set of class names corresponding to a detector passed.
     */
    public void refreshClassNames(ClasspathScanCandidateDetector detector) {
        basePackages.stream()
                .flatMap(this::scanPackage)
                .forEach(metadataReader -> {
                    if (detector.isCandidate(metadataReader)) {
                        Set<String> classNames = detectedClasses.computeIfAbsent(
                                detector.getClass(), aClass -> new HashSet<>());
                        classNames.add(metadataReader.getClassMetadata().getClassName());
                    }
                });
    }

    @Override
    protected MetadataReaderFactory getMetadataReaderFactory() {
        return metadataReaderFactory;
    }

    @Override
    protected ResourceLoader getResourceLoader() {
        return applicationContext;
    }

    @Override
    protected Environment getEnvironment() {
        return applicationContext.getEnvironment();
    }
}
