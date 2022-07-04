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

package io.jmix.flowui.sys;

import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.flowui.view.UiController;
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
 * Configuration that performs ClassPath scanning of {@link UiController}s and provides {@link UiControllerDefinition}.
 */
public class UiControllersConfiguration extends AbstractScanConfiguration {

    private static final Logger log = LoggerFactory.getLogger(UiControllersConfiguration.class);

    protected ApplicationContext applicationContext;
    protected MetadataReaderFactory metadataReaderFactory;

    protected List<String> basePackages = Collections.emptyList();
    protected List<UiControllerDefinition> explicitDefinitions = Collections.emptyList();

    @Autowired
    public UiControllersConfiguration(ApplicationContext applicationContext,
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

    public List<UiControllerDefinition> getExplicitDefinitions() {
        return explicitDefinitions;
    }

    public void setExplicitDefinitions(List<UiControllerDefinition> explicitDefinitions) {
        checkNotNullArgument(explicitDefinitions);

        this.explicitDefinitions = explicitDefinitions;
    }

    public List<UiControllerDefinition> getUiControllers() {
        log.trace("Scanning packages {}", basePackages);

        Stream<UiControllerDefinition> scannedControllersStream = basePackages.stream()
                .flatMap(this::scanPackage)
                .filter(this::isCandidateUiController)
                .map(this::extractControllerDefinition);

        return Stream.concat(scannedControllersStream, explicitDefinitions.stream())
                .collect(Collectors.toList());
    }

    protected UiControllerDefinition extractControllerDefinition(MetadataReader metadataReader) {
        UiControllerMeta uiControllerMeta = new UiControllerMeta(metadataReaderFactory, metadataReader);

        return new UiControllerDefinition(uiControllerMeta.getControllerId(),
                uiControllerMeta.getControllerClass(), uiControllerMeta.getResource());
    }

    protected boolean isCandidateUiController(MetadataReader metadataReader) {
        return metadataReader.getClassMetadata().isConcrete()
                && metadataReader.getAnnotationMetadata().hasAnnotation(UiController.class.getName());
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
