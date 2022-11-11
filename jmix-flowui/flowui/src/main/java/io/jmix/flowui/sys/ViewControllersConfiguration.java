/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.sys;

import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.flowui.view.ViewController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Configuration that performs ClassPath scanning of {@link ViewController}s and provides {@link ViewControllerDefinition}.
 */
public class ViewControllersConfiguration extends AbstractScanConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ViewControllersConfiguration.class);

    protected ApplicationContext applicationContext;
    protected MetadataReaderFactory metadataReaderFactory;

    protected List<String> basePackages = Collections.emptyList();
    protected List<ViewControllerDefinition> explicitDefinitions = Collections.emptyList();

    @Autowired
    public ViewControllersConfiguration(ApplicationContext applicationContext,
                                        AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        this.applicationContext = applicationContext;
        this.metadataReaderFactory = metadataReaderFactory;
    }

    public List<String> getBasePackages() {
        return basePackages;
    }

    public void setBasePackages(List<String> basePackages) {
        checkNotNullArgument(basePackages);

        this.basePackages = basePackages;
    }

    public List<ViewControllerDefinition> getExplicitDefinitions() {
        return explicitDefinitions;
    }

    public void setExplicitDefinitions(List<ViewControllerDefinition> explicitDefinitions) {
        checkNotNullArgument(explicitDefinitions);

        this.explicitDefinitions = explicitDefinitions;
    }

    public List<ViewControllerDefinition> getViewControllers() {
        log.trace("Scanning packages {}", basePackages);

        Stream<ViewControllerDefinition> scannedControllersStream = basePackages.stream()
                .flatMap(this::scanPackage)
                .filter(this::isCandidateViewController)
                .map(this::extractControllerDefinition);

        return Stream.concat(scannedControllersStream, explicitDefinitions.stream())
                .collect(Collectors.toList());
    }

    protected ViewControllerDefinition extractControllerDefinition(MetadataReader metadataReader) {
        ViewControllerMeta viewControllerMeta = new ViewControllerMeta(metadataReaderFactory, metadataReader);

        return new ViewControllerDefinition(viewControllerMeta.getControllerId(),
                viewControllerMeta.getControllerClass(), viewControllerMeta.getResource());
    }

    protected boolean isCandidateViewController(MetadataReader metadataReader) {
        return metadataReader.getClassMetadata().isConcrete()
                && metadataReader.getAnnotationMetadata().hasAnnotation(ViewController.class.getName());
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
