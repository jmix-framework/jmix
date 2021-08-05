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
import io.jmix.core.impl.method.ContextArgumentResolverComposite;
import io.jmix.core.impl.method.MethodArgumentsProvider;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.IndexSettingsConfigurationContext;
import io.jmix.search.index.IndexSettingsConfigurer;
import io.jmix.search.index.annotation.FieldMappingAnnotation;
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
import org.elasticsearch.common.settings.Settings;
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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides functionality to process index definition interfaces marked with {@link JmixEntitySearchIndex}
 */
@Component("search_AnnotatedIndexDefinitionProcessor")
public class AnnotatedIndexDefinitionProcessor {

    private static final Logger log = LoggerFactory.getLogger(AnnotatedIndexDefinitionProcessor.class);

    protected final Metadata metadata;
    protected final MetadataTools metadataTools;
    protected final MappingFieldAnnotationProcessorsRegistry mappingFieldAnnotationProcessorsRegistry;
    protected final PropertyTools propertyTools;
    protected final FieldMappingStrategyProvider fieldMappingStrategyProvider;
    protected final InstanceNameProvider instanceNameProvider;
    protected final PropertyValueExtractorProvider propertyValueExtractorProvider;
    protected final SearchProperties searchProperties;
    protected final List<IndexSettingsConfigurer> indexSettingsConfigurers;
    protected final MethodArgumentsProvider methodArgumentsProvider;

    @Autowired
    public AnnotatedIndexDefinitionProcessor(Metadata metadata,
                                             MetadataTools metadataTools,
                                             MappingFieldAnnotationProcessorsRegistry mappingFieldAnnotationProcessorsRegistry,
                                             PropertyTools propertyTools,
                                             FieldMappingStrategyProvider fieldMappingStrategyProvider,
                                             InstanceNameProvider instanceNameProvider,
                                             PropertyValueExtractorProvider propertyValueExtractorProvider,
                                             SearchProperties searchProperties,
                                             List<IndexSettingsConfigurer> indexSettingsConfigurers,
                                             ContextArgumentResolverComposite resolvers) {
        this.metadata = metadata;
        this.metadataTools = metadataTools;
        this.mappingFieldAnnotationProcessorsRegistry = mappingFieldAnnotationProcessorsRegistry;
        this.propertyTools = propertyTools;
        this.fieldMappingStrategyProvider = fieldMappingStrategyProvider;
        this.instanceNameProvider = instanceNameProvider;
        this.propertyValueExtractorProvider = propertyValueExtractorProvider;
        this.searchProperties = searchProperties;
        this.indexSettingsConfigurers = indexSettingsConfigurers;
        this.methodArgumentsProvider = new MethodArgumentsProvider(resolvers);
    }

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
        ParsedIndexDefinition indexDef = parseIndexDefinition(indexDefClass);

        String indexName = createIndexName(indexDef);
        log.debug("Index name for entity {}: {}", indexDef.getMetaClass(), indexName);

        IndexMappingConfiguration indexMappingConfiguration = createIndexMappingConfig(indexDef);
        Set<Class<?>> affectedEntityClasses = getAffectedEntityClasses(indexMappingConfiguration);
        log.debug("Index Definition class {}. Affected entity classes = {}", className, affectedEntityClasses);

        Settings settings = configureIndexSettings(indexDef.getEntityClass());

        Predicate<Object> indexablePredicate = createIndexablePredicate(indexDef);

        return new IndexConfiguration(
                indexDef.getMetaClass().getName(),
                indexDef.getEntityClass(),
                indexName,
                indexMappingConfiguration,
                settings,
                affectedEntityClasses,
                indexablePredicate
        );
    }

    protected Class<?> resolveClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot find Index Definition class: " + className);
        }
    }

    protected ParsedIndexDefinition parseIndexDefinition(Class<?> indexDefinitionClass) {
        ParsedIndexDefinition result = new ParsedIndexDefinition(indexDefinitionClass);

        JmixEntitySearchIndex indexAnnotation = indexDefinitionClass.getAnnotation(JmixEntitySearchIndex.class);
        Class<?> entityClass = indexAnnotation.entity();
        MetaClass metaClass = metadata.findClass(entityClass);
        if (metaClass == null) {
            throw new RuntimeException("MetaClass for '" + entityClass + "' not found");
        }
        result.setEntityClass(entityClass);
        result.setMetaClass(metaClass);
        result.setIndexName(indexAnnotation.indexName());

        Method[] methods = indexDefinitionClass.getDeclaredMethods();
        for (Method method : methods) {
            if (isIndexablePredicateMethod(method)) {
                result.addIndexablePredicateMethod(method);
            } else if (isMappingDefinitionImplementationMethod(method)) {
                if (result.getMappingDefinitionImplementationMethod() == null) {
                    result.setMappingDefinitionImplementationMethod(method);
                } else {
                    throw new RuntimeException("There can be only one mapping method with body in Index Definition interface '" + indexDefinitionClass + "'");
                }
            } else {
                Set<Annotation> annotations = MergedAnnotations.from(method).stream()
                        .map(MergedAnnotation::synthesize)
                        .filter(this::isFieldMappingAnnotation)
                        .collect(Collectors.toSet());
                result.addFieldAnnotations(annotations);
            }
        }

        return result;
    }

    protected String createIndexName(ParsedIndexDefinition parsedIndexDefinition) {
        String indexName;
        if (StringUtils.isNotEmpty(parsedIndexDefinition.getIndexName())) {
            indexName = parsedIndexDefinition.getIndexName().toLowerCase();
        } else {
            indexName = searchProperties.getSearchIndexNamePrefix() + parsedIndexDefinition.getMetaClass().getName();
        }
        return indexName.toLowerCase();
    }

    protected IndexMappingConfiguration createIndexMappingConfig(ParsedIndexDefinition parsedIndexDefinition) {
        IndexMappingConfiguration indexMappingConfiguration;

        DisplayedNameDescriptor displayedNameDescriptor = createDisplayedNameDescriptor(parsedIndexDefinition.getMetaClass());
        MappingDefinition mappingDefinition;
        Method mappingDefinitionImplementationMethod = parsedIndexDefinition.getMappingDefinitionImplementationMethod();
        if (mappingDefinitionImplementationMethod == null) {
            MappingDefinitionBuilder builder = MappingDefinition.builder();
            parsedIndexDefinition.getFieldAnnotations().forEach(annotation -> processAnnotation(
                    builder, annotation, parsedIndexDefinition.getMetaClass()
            ));
            mappingDefinition = builder.buildMappingDefinition();
        } else {
            try {
                mappingDefinition = callMethod(parsedIndexDefinition.getIndexDefinitionClass(), mappingDefinitionImplementationMethod);
            } catch (Exception e) {
                throw new RuntimeException("Failed to call method '" + mappingDefinitionImplementationMethod + "'", e);
            }
        }
        Map<String, MappingFieldDescriptor> fieldDescriptors = processMappingDefinition(
                parsedIndexDefinition.getMetaClass(), mappingDefinition
        );
        indexMappingConfiguration = new IndexMappingConfiguration(
                parsedIndexDefinition.getMetaClass(), fieldDescriptors, displayedNameDescriptor
        );
        return indexMappingConfiguration;
    }

    protected boolean isIndexablePredicateMethod(Method method) {
        return method.isDefault()
                && method.isAnnotationPresent(io.jmix.search.index.annotation.IndexablePredicate.class);
    }

    protected boolean isMappingDefinitionImplementationMethod(Method method) {
        return method.isDefault()
                && MappingDefinition.class.equals(method.getReturnType());
    }

    protected boolean isFieldMappingAnnotation(Annotation annotation) {
        return annotation.annotationType().isAnnotationPresent(FieldMappingAnnotation.class);
    }

    protected Predicate<Object> createIndexablePredicate(ParsedIndexDefinition parsedIndexDefinition) {
        List<Predicate<Object>> predicates = new ArrayList<>();
        for (Method method : parsedIndexDefinition.getIndexablePredicateMethods()) {
            Class<?> returnType = method.getReturnType();
            if (!Predicate.class.isAssignableFrom(returnType)) {
                throw new RuntimeException("Indexable predicate method should return Predicate object");
            }
            Predicate<Object> predicate;
            try {
                predicate = callMethod(parsedIndexDefinition.getIndexDefinitionClass(), method);
                predicates.add(predicate);
            } catch (Exception e) {
                throw new RuntimeException("Cannot evaluate indexable predicate", e);
            }
        }
        return predicates.stream().reduce(Predicate::and).orElseGet(() -> (obj) -> true);
    }

    protected <T> T callMethod(Class<?> ownerClass, Method method) throws Exception {
        Object proxyObject = null;
        if (!Modifier.isStatic(method.getModifiers())) {
            proxyObject = createProxy(ownerClass);
        }
        Object[] methodArgumentValues = methodArgumentsProvider.getMethodArgumentValues(method);
        //noinspection unchecked
        return (T) method.invoke(proxyObject, methodArgumentValues);
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

    protected Settings configureIndexSettings(Class<?> entityClass) {
        IndexSettingsConfigurationContext context = new IndexSettingsConfigurationContext();
        indexSettingsConfigurers.forEach(configurer -> configurer.configure(context));

        Settings commonSettings = context.getCommonSettingsBuilder().build();
        Settings entitySettings = context.getEntitySettingsBuilder(entityClass).build();
        return Settings.builder()
                .put(commonSettings)
                .put(entitySettings)
                .build();
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

    private static class ParsedIndexDefinition {
        private final Class<?> indexDefinitionClass;
        private Class<?> entityClass;
        private MetaClass metaClass;
        private String indexName;
        private final List<Annotation> fieldAnnotations = new ArrayList<>();
        private final List<Method> indexablePredicateMethods = new ArrayList<>();
        private Method mappingDefinitionImplementationMethod = null;

        private ParsedIndexDefinition(Class<?> indexDefinitionClass) {
            this.indexDefinitionClass = indexDefinitionClass;
        }

        private Class<?> getIndexDefinitionClass() {
            return indexDefinitionClass;
        }

        private Class<?> getEntityClass() {
            return entityClass;
        }

        private void setEntityClass(Class<?> entityClass) {
            this.entityClass = entityClass;
        }

        public MetaClass getMetaClass() {
            return metaClass;
        }

        public void setMetaClass(MetaClass metaClass) {
            this.metaClass = metaClass;
        }

        private String getIndexName() {
            return indexName;
        }

        private void setIndexName(String indexName) {
            this.indexName = indexName;
        }

        private List<Annotation> getFieldAnnotations() {
            return fieldAnnotations;
        }

        private void addFieldAnnotations(Collection<Annotation> fieldAnnotations) {
            this.fieldAnnotations.addAll(fieldAnnotations);
        }

        private List<Method> getIndexablePredicateMethods() {
            return indexablePredicateMethods;
        }

        private void addIndexablePredicateMethod(Method indexablePredicateMethod) {
            this.indexablePredicateMethods.add(indexablePredicateMethod);
        }

        @Nullable
        private Method getMappingDefinitionImplementationMethod() {
            return mappingDefinitionImplementationMethod;
        }

        private void setMappingDefinitionImplementationMethod(@Nullable Method mappingDefinitionImplementationMethod) {
            this.mappingDefinitionImplementationMethod = mappingDefinitionImplementationMethod;
        }
    }
}
