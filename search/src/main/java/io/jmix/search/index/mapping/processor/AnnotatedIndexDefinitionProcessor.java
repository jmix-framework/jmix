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

package io.jmix.search.index.mapping.processor;

import io.jmix.core.InstanceNameProvider;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.annotation.JmixEntitySearchIndex;
import io.jmix.search.index.mapping.DisplayedNameDescriptor;
import io.jmix.search.index.mapping.IndexMappingConfiguration;
import io.jmix.search.index.mapping.MappingFieldDescriptor;
import io.jmix.search.index.mapping.processor.MappingDefinition.MappingDefinitionBuilder;
import io.jmix.search.index.mapping.processor.MappingDefinition.MappingDefinitionElement;
import io.jmix.search.index.mapping.strategy.*;
import io.jmix.search.utils.PropertyTools;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides functionality to process index definition interfaces marked with {@link JmixEntitySearchIndex}
 */
@Component("search_AnnotatedIndexDefinitionProcessor")
public class AnnotatedIndexDefinitionProcessor {

    private static final Logger log = LoggerFactory.getLogger(AnnotatedIndexDefinitionProcessor.class);

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
    @Autowired
    protected PropertyValueExtractorProvider propertyValueExtractorProvider;
    @Autowired
    protected SearchProperties searchProperties;

    /**
     * Processes index definition interface marked with {@link JmixEntitySearchIndex} annotation
     * and creates {@link IndexConfiguration} based on it.
     *
     * @param className full name of index definition interface
     * @return {@link IndexConfiguration}
     */
    public IndexConfiguration createIndexConfiguration(String className) {
        log.debug("Create Index Definition for class {}", className);

        Class<?> indexDefClass = resolveClass(className);

        JmixEntitySearchIndex indexAnnotation = indexDefClass.getAnnotation(JmixEntitySearchIndex.class);
        Class<?> entityJavaClass = indexAnnotation.entity();
        MetaClass entityMetaClass = metadata.findClass(entityJavaClass);
        if (entityMetaClass == null) {
            throw new RuntimeException("MetaClass for '" + entityJavaClass + "' not found");
        }
        String indexName = createIndexName(entityMetaClass);
        log.debug("Index name for entity {}: {}", entityMetaClass, indexName);

        IndexMappingConfiguration indexMappingConfiguration = createIndexMappingConfig(entityMetaClass, indexDefClass);
        Set<Class<?>> affectedEntityClasses = getAffectedEntityClasses(indexMappingConfiguration);

        log.debug("Definition class {}. Affected entity classes = {}", className, affectedEntityClasses);

        return new IndexConfiguration(entityMetaClass.getName(), entityJavaClass, indexName, indexMappingConfiguration, affectedEntityClasses);
    }

    protected Class<?> resolveClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot find Index Definition class: " + className);
        }
    }

    protected String createIndexName(MetaClass entityMetaClass) {
        return searchProperties.getSearchIndexNamePrefix() + entityMetaClass.getName().toLowerCase();
    }

    protected IndexMappingConfiguration createIndexMappingConfig(MetaClass entityMetaClass, Class<?> indexDefClass) {
        Method[] methods = indexDefClass.getDeclaredMethods();
        IndexMappingConfiguration indexMappingConfiguration;

        DisplayedNameDescriptor displayedNameDescriptor = createDisplayedNameDescriptor(entityMetaClass);
        if (methods.length > 0) {
            List<Annotation> fieldAnnotations = new ArrayList<>();
            Method methodWithMappingDefinitionImplementation = null;
            for (Method method : methods) {
                if (isMappingDefinitionImplementationMethod(method)) {
                    if (methodWithMappingDefinitionImplementation == null) {
                        methodWithMappingDefinitionImplementation = method;
                    } else {
                        throw new RuntimeException("There can be only one method with body in Index Definition interface '" + indexDefClass + "'");
                    }
                } else {
                    Set<Annotation> annotations = MergedAnnotations.from(method).stream()
                            .map(MergedAnnotation::synthesize)
                            .collect(Collectors.toSet());
                    fieldAnnotations.addAll(annotations);
                }
            }

            MappingDefinition mappingDefinition;
            if (methodWithMappingDefinitionImplementation == null) {
                MappingDefinitionBuilder builder = MappingDefinition.builder();
                fieldAnnotations.forEach(annotation -> processAnnotation(builder, annotation, entityMetaClass));
                mappingDefinition = builder.buildMappingDefinition();
            } else {
                try {
                    Object proxy = createProxy(indexDefClass);
                    mappingDefinition = (MappingDefinition) methodWithMappingDefinitionImplementation.invoke(proxy);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("Failed to call method '" + methodWithMappingDefinitionImplementation + "'", e);
                }
            }
            Map<String, MappingFieldDescriptor> fieldDescriptors = processMappingDefinition(entityMetaClass, mappingDefinition);
            indexMappingConfiguration = new IndexMappingConfiguration(entityMetaClass, fieldDescriptors, displayedNameDescriptor);
        } else {
            indexMappingConfiguration = new IndexMappingConfiguration(entityMetaClass, Collections.emptyMap(), displayedNameDescriptor);
        }
        return indexMappingConfiguration;
    }

    protected boolean isMappingDefinitionImplementationMethod(Method method) {
        return method.isDefault()
                && MappingDefinition.class.equals(method.getReturnType())
                && method.getParameterCount() == 0;
    }

    protected Object createProxy(Class<?> ownerClass) {
        return ownerClass.isInterface()
                ? createInterfaceProxyInstance(ownerClass)
                : createClassProxyInstance(ownerClass);
    }

    protected Object createInterfaceProxyInstance(Class<?> ownerClass) {
        ClassLoader classLoader = ownerClass.getClassLoader();
        return Proxy.newProxyInstance(classLoader, new Class[]{ownerClass},
                (proxy, method, args) -> invokeProxyMethod(ownerClass, proxy, method, args));
    }

    protected Object createClassProxyInstance(Class<?> ownerClass) {
        try {
            return ReflectionHelper.newInstance(ownerClass);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(String.format("Cannot create Index Definition [%s] proxy", ownerClass), e);
        }
    }

    @Nullable
    protected Object invokeProxyMethod(Class<?> ownerClass, Object proxy, Method method, Object[] args) {
        if (method.isDefault()) {
            try {
                if (SystemUtils.IS_JAVA_1_8) {
                    Constructor<MethodHandles.Lookup> lookupConstructor =
                            MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, Integer.TYPE);
                    if (!lookupConstructor.isAccessible()) {
                        lookupConstructor.setAccessible(true);
                    }
                    return lookupConstructor.newInstance(ownerClass, MethodHandles.Lookup.PRIVATE)
                            .unreflectSpecial(method, ownerClass)
                            .bindTo(proxy)
                            .invokeWithArguments(args);
                } else {
                    return MethodHandles.lookup()
                            .findSpecial(
                                    ownerClass,
                                    method.getName(),
                                    MethodType.methodType(method.getReturnType(),
                                            method.getParameterTypes()),
                                    ownerClass
                            )
                            .bindTo(proxy)
                            .invokeWithArguments(args);
                }
            } catch (Throwable throwable) {
                throw new RuntimeException("Error invoking default method of Index Definition interface", throwable);
            }
        } else {
            return null;
        }
    }

    protected void processAnnotation(MappingDefinitionBuilder builder, Annotation annotation, MetaClass entityMetaClass) {
        Class<? extends Annotation> aClass = annotation.annotationType();
        Optional<FieldAnnotationProcessor<? extends Annotation>> processor = mappingFieldAnnotationProcessorsRegistry.getProcessorForAnnotationClass(aClass);
        processor.ifPresent(fieldAnnotationProcessor -> fieldAnnotationProcessor.process(builder, entityMetaClass, annotation));
    }

    protected Set<Class<?>> getAffectedEntityClasses(IndexMappingConfiguration indexMappingConfiguration) {
        Set<Class<?>> affectedClasses = indexMappingConfiguration.getFields().values().stream()
                .flatMap(d -> Arrays.stream(d.getMetaPropertyPath().getMetaProperties()))
                .filter(p -> p.getRange().isClass())
                .map(p -> p.getRange().asClass().getJavaClass())
                .collect(Collectors.toSet());

        affectedClasses.add(indexMappingConfiguration.getEntityMetaClass().getJavaClass());
        return affectedClasses;
    }

    protected Map<String, MappingFieldDescriptor> processMappingDefinition(MetaClass metaClass, MappingDefinition mappingDefinition) {
        return mappingDefinition.getElements().stream()
                .map(item -> processMappingDefinitionElement(metaClass, item))
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

    protected List<MappingFieldDescriptor> processMappingDefinitionElement(MetaClass metaClass, MappingDefinitionElement element) {
        Map<String, MetaPropertyPath> effectiveProperties = resolveEffectiveProperties(
                metaClass, element.getIncludedProperties(), element.getExcludedProperties()
        );

        return effectiveProperties.values().stream()
                .map(propertyPath -> createMappingFieldDescriptor(propertyPath, element))
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
                    Map<String, MetaPropertyPath> propertyPaths = propertyTools.findPropertiesByPath(rootEntityMetaClass, included);
                    Map<String, MetaPropertyPath> expandedPropertyPaths = expandEmbeddedProperties(rootEntityMetaClass, propertyPaths);
                    effectiveProperties.putAll(expandedPropertyPaths);
                });

        Arrays.stream(excludes)
                .filter(StringUtils::isNotBlank)
                .flatMap(excluded -> {
                    Map<String, MetaPropertyPath> propertyPaths = propertyTools.findPropertiesByPath(rootEntityMetaClass, excluded);
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
                        Map<String, MetaPropertyPath> expandedEmbeddedProperties = propertyTools.findPropertiesByPath(rootEntityMetaClass, propertyFullName + ".*");
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

    protected Optional<MappingFieldDescriptor> createMappingFieldDescriptor(MetaPropertyPath propertyPath, MappingDefinitionElement element) {
        FieldMappingStrategy fieldMappingStrategy = resolveFieldMappingStrategy(element.getFieldMappingStrategyClass());
        if (fieldMappingStrategy.isSupported(propertyPath)) {
            List<MetaPropertyPath> instanceNameRelatedProperties;
            if (propertyPath.getRange().isClass()) {
                instanceNameRelatedProperties = resolveInstanceNameRelatedProperties(propertyPath.getRange().asClass(), propertyPath);
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
            fieldDescriptor.setPropertyValueExtractor(fieldMappingStrategy.getPropertyValueExtractor(propertyPath));
            fieldDescriptor.setInstanceNameRelatedProperties(instanceNameRelatedProperties);
            fieldDescriptor.setParameters(element.getParameters());

            FieldConfiguration fieldConfiguration = fieldMappingStrategy.createFieldConfiguration(propertyPath, element.getParameters());
            fieldDescriptor.setFieldConfiguration(fieldConfiguration);

            return Optional.of(fieldDescriptor);
        } else {
            return Optional.empty();
        }
    }

    protected DisplayedNameDescriptor createDisplayedNameDescriptor(MetaClass metaClass) {
        DisplayedNameDescriptor displayedNameDescriptor = new DisplayedNameDescriptor();
        FieldConfiguration fieldConfiguration = new NativeFieldConfiguration(
                new TextFieldMapper().createJsonConfiguration(Collections.emptyMap())
        );
        displayedNameDescriptor.setFieldConfiguration(fieldConfiguration);

        List<MetaPropertyPath> instanceNameRelatedProperties = resolveInstanceNameRelatedProperties(metaClass, null);
        displayedNameDescriptor.setInstanceNameRelatedProperties(instanceNameRelatedProperties);
        displayedNameDescriptor.setValueExtractor(propertyValueExtractorProvider.getPropertyValueExtractor(DisplayedNameValueExtractor.class));

        return displayedNameDescriptor;
    }

    protected List<MetaPropertyPath> resolveInstanceNameRelatedProperties(MetaClass metaClass, @Nullable MetaPropertyPath rootPropertyPath) {
        MetaProperty[] rootProperties = rootPropertyPath == null ? null : rootPropertyPath.getMetaProperties();
        return instanceNameProvider.getInstanceNameRelatedProperties(metaClass, true)
                .stream()
                .map(property -> {
                    if (rootProperties == null) {
                        return new MetaPropertyPath(metaClass, property);
                    } else {
                        MetaProperty[] extendedPropertiesArray = Arrays.copyOf(rootProperties, rootProperties.length + 1);
                        extendedPropertiesArray[extendedPropertiesArray.length - 1] = property;
                        return new MetaPropertyPath(rootPropertyPath.getMetaClass(), extendedPropertiesArray);
                    }

                })
                .collect(Collectors.toList());
    }

    protected FieldMappingStrategy resolveFieldMappingStrategy(Class<? extends FieldMappingStrategy> strategyClass) {
        return fieldMappingStrategyProvider.getFieldMappingStrategyByClass(strategyClass);
    }
}
