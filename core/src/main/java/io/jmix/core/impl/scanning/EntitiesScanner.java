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
import io.jmix.core.metamodel.annotations.MetaClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.jmix.core.commons.util.Preconditions.checkNotNullArgument;

@Component("jmix_EntitiesScanner")
public class EntitiesScanner extends AbstractClasspathScanner {

    private static final Logger log = LoggerFactory.getLogger(EntitiesScanner.class);

    protected MetadataReaderFactory metadataReaderFactory;

    protected List<String> basePackages = Collections.emptyList();
    protected List<String> explicitDefinitions = Collections.emptyList();

    @Inject
    protected ApplicationContext applicationContext;

    @Inject
    public void setMetadataReaderFactory(AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        this.metadataReaderFactory = metadataReaderFactory;
    }

    @Inject
    public void setJmixComponents(JmixModules jmixModules) {
        basePackages = jmixModules.getComponents().stream()
                .map(JmixModuleDescriptor::getId)
                .collect(Collectors.toList());
    }

    public List<String> getBasePackages() {
        return basePackages;
    }

    public void setBasePackages(List<String> basePackages) {
        checkNotNullArgument(basePackages);

        this.basePackages = basePackages;
    }

    public List<String> getExplicitDefinitions() {
        return explicitDefinitions;
    }

    public void setExplicitDefinitions(List<String> explicitDefinitions) {
        checkNotNullArgument(explicitDefinitions);

        this.explicitDefinitions = explicitDefinitions;
    }

    public List<String> getEntityClassNames() {
        log.trace("Scanning packages {}", basePackages);

        Stream<String> scannedActionsStream = basePackages.stream()
                .flatMap(this::scanPackage)
                .filter(this::isCandidateEntity)
                .map(metadataReader -> metadataReader.getClassMetadata().getClassName());

        return Stream.concat(scannedActionsStream, explicitDefinitions.stream())
                .collect(Collectors.toList());
    }

    protected boolean isCandidateEntity(MetadataReader metadataReader) {
        return (metadataReader.getAnnotationMetadata().hasAnnotation(Entity.class.getName())
                || metadataReader.getAnnotationMetadata().hasAnnotation(MetaClass.class.getName()))
                || metadataReader.getAnnotationMetadata().hasAnnotation(Embeddable.class.getName());
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