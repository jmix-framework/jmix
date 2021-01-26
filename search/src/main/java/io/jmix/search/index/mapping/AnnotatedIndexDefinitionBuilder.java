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

package io.jmix.search.index.mapping;

import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.data.impl.EntityListenerManager;
import io.jmix.search.index.EntityTracker;
import io.jmix.search.index.IndexDefinition;
import io.jmix.search.index.annotation.JmixEntitySearchIndex;
import io.jmix.search.utils.PropertyTools;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component("search_AnnotatedIndexDefinitionBuilder")
public class AnnotatedIndexDefinitionBuilder {

    private static final Logger log = LoggerFactory.getLogger(AnnotatedIndexDefinitionBuilder.class);

    @Autowired
    private Metadata metadata;
    @Autowired
    protected MappingFieldAnnotationProcessorsRegistry mappingFieldAnnotationProcessorsRegistry;
    @Autowired
    protected EntityListenerManager entityListenerManager;
    @Autowired
    protected PropertyTools propertyTools;
    @Autowired
    protected FieldMappingStrategyProvider fieldMappingStrategyProvider;

    public IndexDefinition createIndexDefinition(String className) {
        log.info("[IVGA] Create Index Definition for class {}", className);

        Class<?> indexDefClass = resolveClass(className);

        JmixEntitySearchIndex indexAnnotation = indexDefClass.getAnnotation(JmixEntitySearchIndex.class);
        Class<?> entityJavaClass = indexAnnotation.entity();
        MetaClass entityMetaClass = metadata.findClass(entityJavaClass);
        if(entityMetaClass == null) {
            throw new RuntimeException("[IVGA] MetaClass for '" + className + "' not found");
        }
        String indexName = createIndexName(indexAnnotation, entityMetaClass);
        log.info("[IVGA] Index name for class {}: {}", className, indexName);

        IndexMappingConfig indexMappingConfig = createIndexMappingConfig(entityMetaClass, indexDefClass);
        Set<Class<?>> affectedEntityClasses = getAffectedEntityClasses(indexMappingConfig);

        log.info("[IVGA] Definition class {}. Affected entity classes = {}", className, affectedEntityClasses);
        affectedEntityClasses.forEach(entityClass -> entityListenerManager.addListener(entityClass, EntityTracker.NAME));

        return new IndexDefinition(entityJavaClass, indexName, indexMappingConfig, affectedEntityClasses);
    }

    protected Class<?> resolveClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot find Index Definition class: " + className);
        }
    }

    protected String createIndexName(JmixEntitySearchIndex indexAnnotation, MetaClass entityMetaClass) {
        return StringUtils.isNotEmpty(indexAnnotation.indexName())
                ? indexAnnotation.indexName().toLowerCase()
                : createIndexName(entityMetaClass);
    }

    protected String createIndexName(MetaClass entityMetaClass) {
        return entityMetaClass.getName().toLowerCase() + "_search_index"; //todo naming
    }

    protected IndexMappingConfig createIndexMappingConfig(MetaClass entityMetaClass, Class<?> indexDefClass) {
        Method[] methods = indexDefClass.getDeclaredMethods();
        IndexMappingConfig indexMappingConfig;

        if(methods.length > 0) { //todo handle multiple methods?
            Method method = methods[0];
            log.info("[IVGA] Check method '{}'", method.getName());
            if(method.isDefault() || IndexMappingConfig.class.equals(method.getReturnType())) {
                indexMappingConfig = new IndexMappingConfig(entityMetaClass, Collections.emptyMap()); //todo call method
            } else {
                Map<String, MappingFieldDescriptor> fieldDescriptors = Arrays.stream(method.getAnnotations())
                        .map(annotation -> {
                            Class<? extends Annotation> aClass = annotation.annotationType();
                            Optional<FieldAnnotationProcessor<? extends Annotation>> processor = mappingFieldAnnotationProcessorsRegistry.getProcessorForAnnotationClass(aClass);
                            return processor.map(fieldAnnotationProcessor -> fieldAnnotationProcessor.process(entityMetaClass, annotation));
                        }).filter(Optional::isPresent)
                        .map(Optional::get)
                        .flatMap(descriptor -> processIndexMappingConfigTemplate(descriptor).stream())
                        .collect(Collectors.toMap(MappingFieldDescriptor::getIndexPropertyFullName, Function.identity(), (v1, v2) -> {
                            int order1 = v1.getOrder();
                            int order2 = v2.getOrder();
                            if (order1 == order2) {
                                throw new RuntimeException("Conflicted mapping fields: '" + v1.getIndexPropertyFullName() + "' and '" + v2.getIndexPropertyFullName() + "'");
                            }
                            return order1 < order2 ? v2 : v1;
                        }));

                indexMappingConfig = new IndexMappingConfig(entityMetaClass, fieldDescriptors);
            }
        } else {
            indexMappingConfig = new IndexMappingConfig(entityMetaClass, Collections.emptyMap());
        }
        return indexMappingConfig;
    }

    private Set<Class<?>> getAffectedEntityClasses(IndexMappingConfig indexMappingConfig) {
        Set<Class<?>> affectedClasses = indexMappingConfig.getFields().values().stream()
                .flatMap(d -> Arrays.stream(d.getMetaPropertyPath().getMetaProperties()))
                .filter(p -> p.getRange().isClass())
                .map(p -> p.getRange().asClass().getJavaClass())
                .collect(Collectors.toSet());

        affectedClasses.add(indexMappingConfig.getEntityMetaClass().getJavaClass());
        return affectedClasses;
    }

    protected List<MappingFieldDescriptor> processIndexMappingConfigTemplate(IndexMappingConfigTemplate template) {
        Map<String, MetaPropertyPath> effectiveProperties = resolveEffectiveProperties(
                template.getRootEntityMetaClass(), template.getIncludedProperties(), template.getExcludedProperties()
        );

        return effectiveProperties.values().stream()
                .map(propertyPath -> createMappingFieldDescriptor(propertyPath, template))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    protected Map<String, MetaPropertyPath> resolveEffectiveProperties(MetaClass rootEntityMetaClass,
                                                                       String[] includes,
                                                                       String[] excludes) {
        Map<String, MetaPropertyPath> effectiveProperties = new HashMap<>();

        Arrays.stream(includes).forEach(included -> {
            Map<String, MetaPropertyPath> propertyPaths = propertyTools.findPropertyPaths(rootEntityMetaClass, included);
            effectiveProperties.putAll(propertyPaths);
        });

        Arrays.stream(excludes)
                .filter(StringUtils::isNotBlank)
                .flatMap(excluded -> propertyTools.findPropertyPaths(rootEntityMetaClass, excluded).keySet().stream())
                .forEach(effectiveProperties::remove);

        return effectiveProperties;
    }

    protected Optional<MappingFieldDescriptor> createMappingFieldDescriptor(MetaPropertyPath propertyPath, IndexMappingConfigTemplate descriptor) {
        FieldMappingStrategy fieldMappingStrategy = resolveFieldMappingStrategy(descriptor.getFieldMappingStrategyClass());
        if(fieldMappingStrategy.isSupported(propertyPath)) {
            MappingFieldDescriptor fieldDescriptor = new MappingFieldDescriptor();
            fieldDescriptor.setEntityPropertyFullName(propertyPath.toPathString());
            fieldDescriptor.setIndexPropertyFullName(propertyPath.toPathString());
            fieldDescriptor.setMetaPropertyPath(propertyPath);
            fieldDescriptor.setRootEntityMetaClass(descriptor.getRootEntityMetaClass());
            fieldDescriptor.setStandalone(false);
            fieldDescriptor.setOrder(fieldMappingStrategy.getOrder());
            fieldDescriptor.setValueMapper(fieldMappingStrategy.getValueMapper(propertyPath));

            FieldConfiguration fieldConfiguration = fieldMappingStrategy.createFieldConfiguration(propertyPath, descriptor.getParameters());
            fieldDescriptor.setFieldConfiguration(fieldConfiguration);

            return Optional.of(fieldDescriptor);
        } else {
            return Optional.empty();
        }
    }

    protected FieldMappingStrategy resolveFieldMappingStrategy(Class<? extends FieldMappingStrategy> strategyClass) {
        return fieldMappingStrategyProvider.getFieldMappingStrategyByClass(strategyClass);
    }
}
