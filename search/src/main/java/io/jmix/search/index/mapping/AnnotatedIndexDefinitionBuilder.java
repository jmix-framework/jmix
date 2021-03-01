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

import io.jmix.core.InstanceNameProvider;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.search.index.IndexDefinition;
import io.jmix.search.index.annotation.JmixEntitySearchIndex;
import io.jmix.search.utils.PropertyTools;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component("search_AnnotatedIndexDefinitionBuilder")
public class AnnotatedIndexDefinitionBuilder {

    private static final Logger log = LoggerFactory.getLogger(AnnotatedIndexDefinitionBuilder.class);

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected MappingFieldAnnotationProcessorsRegistry mappingFieldAnnotationProcessorsRegistry;
    @Autowired
    protected PropertyTools propertyTools;
    @Autowired
    protected FieldMappingStrategyProvider fieldMappingStrategyProvider;
    @Autowired
    protected InstanceNameProvider instanceNameProvider;

    public IndexDefinition createIndexDefinition(String className) {
        log.debug("Create Index Definition for class {}", className);

        Class<?> indexDefClass = resolveClass(className);

        JmixEntitySearchIndex indexAnnotation = indexDefClass.getAnnotation(JmixEntitySearchIndex.class);
        Class<?> entityJavaClass = indexAnnotation.entity();
        MetaClass entityMetaClass = metadata.findClass(entityJavaClass);
        if(entityMetaClass == null) {
            throw new RuntimeException("MetaClass for '" + entityJavaClass + "' not found");
        }
        String indexName = createIndexName(indexAnnotation, entityMetaClass);
        log.debug("Index name for entity {}: {}", entityMetaClass, indexName);

        IndexMappingConfig indexMappingConfig = createIndexMappingConfig(entityMetaClass, indexDefClass);
        Set<Class<?>> affectedEntityClasses = getAffectedEntityClasses(indexMappingConfig);

        log.debug("Definition class {}. Affected entity classes = {}", className, affectedEntityClasses);

        return new IndexDefinition(entityMetaClass.getName(), entityJavaClass, indexName, indexMappingConfig, affectedEntityClasses);
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

        if(methods.length > 0) {
            List<Annotation> fieldAnnotations = new ArrayList<>();
            Method methodWithDefinitionImplementation = null;
            for (Method method : methods) {
                if (isDefinitionImplementationMethod(method)) {
                    if (methodWithDefinitionImplementation == null) {
                        methodWithDefinitionImplementation = method;
                    } else {
                        throw new RuntimeException("There can be only one method with body in Index Definition interface '" + indexDefClass + "'");
                    }
                } else {
                    fieldAnnotations.addAll(Arrays.asList(method.getAnnotations()));
                }
            }

            IndexMappingConfigTemplate indexMappingConfigTemplate;
            if (methodWithDefinitionImplementation == null) {
                List<IndexMappingConfigTemplateItem> items = fieldAnnotations.stream()
                        .map(annotation -> processAnnotation(annotation, entityMetaClass))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

                indexMappingConfigTemplate = new IndexMappingConfigTemplate();
                indexMappingConfigTemplate.setItems(items);
            } else {
                try {
                    indexMappingConfigTemplate = (IndexMappingConfigTemplate) methodWithDefinitionImplementation.invoke(null);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("Failed to call method '" + methodWithDefinitionImplementation + "'", e);
                }
            }
            Map<String, MappingFieldDescriptor> fieldDescriptors = processIndexMappingConfigTemplate(entityMetaClass, indexMappingConfigTemplate);
            indexMappingConfig = new IndexMappingConfig(entityMetaClass, fieldDescriptors);
        } else {
            indexMappingConfig = new IndexMappingConfig(entityMetaClass, Collections.emptyMap());
        }
        return indexMappingConfig;
    }

    protected boolean isDefinitionImplementationMethod(Method method) {
        return Modifier.isStatic(method.getModifiers())
                && IndexMappingConfigTemplate.class.equals(method.getReturnType())
                && method.getParameterCount() == 0;
    }

    protected Optional<IndexMappingConfigTemplateItem> processAnnotation(Annotation annotation, MetaClass entityMetaClass) {
        Class<? extends Annotation> aClass = annotation.annotationType();
        Optional<FieldAnnotationProcessor<? extends Annotation>> processor = mappingFieldAnnotationProcessorsRegistry.getProcessorForAnnotationClass(aClass);
        return processor.map(fieldAnnotationProcessor -> fieldAnnotationProcessor.process(entityMetaClass, annotation));
    }

    protected Set<Class<?>> getAffectedEntityClasses(IndexMappingConfig indexMappingConfig) {
        Set<Class<?>> affectedClasses = indexMappingConfig.getFields().values().stream()
                .flatMap(d -> Arrays.stream(d.getMetaPropertyPath().getMetaProperties()))
                .filter(p -> p.getRange().isClass())
                .map(p -> p.getRange().asClass().getJavaClass())
                .collect(Collectors.toSet());

        affectedClasses.add(indexMappingConfig.getEntityMetaClass().getJavaClass());
        return affectedClasses;
    }

    protected Map<String, MappingFieldDescriptor> processIndexMappingConfigTemplate(MetaClass metaClass, IndexMappingConfigTemplate template) {
        return template.getItems().stream()
                .map(item -> processIndexMappingConfigTemplateItem(metaClass, item))
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(MappingFieldDescriptor::getIndexPropertyFullName, Function.identity(), (v1, v2) -> {
                    int order1 = v1.getOrder();
                    int order2 = v2.getOrder();
                    if (order1 == order2) {
                        throw new RuntimeException("Conflicted mapping fields: '" + v1.getIndexPropertyFullName() + "' and '" + v2.getIndexPropertyFullName() + "'");
                    }
                    return order1 < order2 ? v2 : v1;
                }));
    }

    protected List<MappingFieldDescriptor> processIndexMappingConfigTemplateItem(MetaClass metaClass, IndexMappingConfigTemplateItem template) {
        Map<String, MetaPropertyPath> effectiveProperties = resolveEffectiveProperties(
                metaClass, template.getIncludedProperties(), template.getExcludedProperties()
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
        Arrays.stream(includes)
                .filter(StringUtils::isNotBlank)
                .forEach(included -> {
                    Map<String, MetaPropertyPath> propertyPaths = propertyTools.findPropertyPaths(rootEntityMetaClass, included);
                    Map<String, MetaPropertyPath> expandedPropertyPaths = expandEmbeddedProperties(rootEntityMetaClass, propertyPaths);
                    effectiveProperties.putAll(expandedPropertyPaths);
                });

        Arrays.stream(excludes)
                .filter(StringUtils::isNotBlank)
                .flatMap(excluded -> {
                    Map<String, MetaPropertyPath> propertyPaths = propertyTools.findPropertyPaths(rootEntityMetaClass, excluded);
                    Map<String, MetaPropertyPath> expandedPropertyPaths = expandEmbeddedProperties(rootEntityMetaClass, propertyPaths);
                    return expandedPropertyPaths.keySet().stream();
                })
                .forEach(effectiveProperties::remove);

        return effectiveProperties;
    }

    protected Map<String, MetaPropertyPath> expandEmbeddedProperties(MetaClass rootEntityMetaClass, Map<String, MetaPropertyPath> propertyPaths) {
        return propertyPaths.entrySet().stream()
                .flatMap(entry -> {
                    String propertyFullName = entry.getKey();
                    MetaPropertyPath propertyPath = entry.getValue();
                    if (metadataTools.isEmbedded(propertyPath.getMetaProperty())) {
                        log.trace("Property '{}' is embedded. Expand", propertyFullName);
                        Map<String, MetaPropertyPath> expandedEmbeddedProperties = propertyTools.findPropertyPaths(rootEntityMetaClass, propertyFullName + ".*");
                        log.trace("Property '{}' was expanded to {}", propertyFullName, expandedEmbeddedProperties.values());
                        Map<String, MetaPropertyPath> result = expandEmbeddedProperties(rootEntityMetaClass, expandedEmbeddedProperties);
                        return result.entrySet().stream();
                    } else {
                        log.trace("Property '{}' is not embedded", propertyFullName);
                        return Stream.of(entry);
                    }
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    protected Optional<MappingFieldDescriptor> createMappingFieldDescriptor(MetaPropertyPath propertyPath, IndexMappingConfigTemplateItem template) {
        FieldMappingStrategy fieldMappingStrategy = resolveFieldMappingStrategy(template.getFieldMappingStrategyClass());
        if(fieldMappingStrategy.isSupported(propertyPath)) {
            List<MetaPropertyPath> instanceNameRelatedProperties;
            if(propertyPath.getRange().isClass()) {
                Collection<MetaProperty> instanceNameRelatedLocalProperties = instanceNameProvider.getInstanceNameRelatedProperties(
                        propertyPath.getRange().asClass(), true
                );
                MetaProperty[] metaProperties = propertyPath.getMetaProperties();

                instanceNameRelatedProperties = instanceNameRelatedLocalProperties.stream()
                        .map(instanceNameRelatedProperty -> {
                            MetaProperty[] extendedPropertyArray = Arrays.copyOf(metaProperties, metaProperties.length + 1);
                            extendedPropertyArray[extendedPropertyArray.length - 1] = instanceNameRelatedProperty;
                            return new MetaPropertyPath(propertyPath.getMetaClass(), extendedPropertyArray);
                        })
                        .collect(Collectors.toList());

                log.debug("Properties related to Instance Name ({}): {}", propertyPath, instanceNameRelatedProperties);
            } else {
                instanceNameRelatedProperties = Collections.emptyList();
            }

            MappingFieldDescriptor fieldDescriptor = new MappingFieldDescriptor();
            fieldDescriptor.setEntityPropertyFullName(propertyPath.toPathString());
            fieldDescriptor.setIndexPropertyFullName(propertyPath.toPathString());
            fieldDescriptor.setMetaPropertyPath(propertyPath);
            fieldDescriptor.setStandalone(false); //todo implement standalone properties
            fieldDescriptor.setOrder(fieldMappingStrategy.getOrder());
            fieldDescriptor.setValueMapper(fieldMappingStrategy.getValueMapper(propertyPath));
            fieldDescriptor.setInstanceNameRelatedProperties(instanceNameRelatedProperties);

            FieldConfiguration fieldConfiguration = fieldMappingStrategy.createFieldConfiguration(propertyPath, template.getParameters());
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
