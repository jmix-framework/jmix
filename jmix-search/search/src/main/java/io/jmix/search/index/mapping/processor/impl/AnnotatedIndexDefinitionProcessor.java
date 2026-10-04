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

package io.jmix.search.index.mapping.processor.impl;

import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.search.exception.IndexDefinitionRejectedException;
import io.jmix.search.index.impl.IndexLayout;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.core.impl.method.ContextArgumentResolverComposite;
import io.jmix.core.impl.method.MethodArgumentsProvider;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.IndexNameGenerator;
import io.jmix.search.index.annotation.*;
import io.jmix.search.index.mapping.*;
import io.jmix.search.index.mapping.MappingDefinition.MappingDefinitionBuilder;
import io.jmix.search.index.mapping.fieldmapper.impl.TextFieldMapper;
import io.jmix.search.index.mapping.processor.AttributesGroupProcessor;
import io.jmix.search.index.mapping.processor.FieldAnnotationProcessor;
import io.jmix.search.index.mapping.processor.MappingFieldAnnotationProcessorsRegistry;
import io.jmix.search.index.mapping.propertyvalue.PropertyValueExtractorProvider;
import io.jmix.search.index.mapping.propertyvalue.impl.DisplayedNameValueExtractor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

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

import static io.jmix.search.index.IndexConfigurationFormatter.format;

/**
 * Provides functionality to process index definition interfaces marked with {@link JmixEntitySearchIndex}
 */
@Component("search_AnnotatedIndexDefinitionProcessor")
public class AnnotatedIndexDefinitionProcessor {

    private static final Logger log = LoggerFactory.getLogger(AnnotatedIndexDefinitionProcessor.class);
    private static final String MAPPINGS_CONFLICT_ERROR_MESSAGE = "Conflicted mapping fields: '%s' and '%s'. Specify the different values of order for them.";

    protected final Metadata metadata;
    protected final MappingFieldAnnotationProcessorsRegistry mappingFieldAnnotationProcessorsRegistry;
    protected final PropertyValueExtractorProvider propertyValueExtractorProvider;
    protected final SearchProperties searchProperties;
    protected final MethodArgumentsProvider methodArgumentsProvider;
    protected final List<AttributesGroupProcessor<? extends AttributesGroupConfiguration>> attributesGroupProcessors;
    protected final InstanceNameRelatedPropertiesResolver instanceNameRelatedPropertiesResolver;
    protected final MetadataTools metadataTools;
    protected final IndexNameGenerator indexNameGenerator;

    @Autowired
    protected IndexLayout indexLayout;

    @Autowired
    public AnnotatedIndexDefinitionProcessor(Metadata metadata,
                                             MappingFieldAnnotationProcessorsRegistry mappingFieldAnnotationProcessorsRegistry,
                                             PropertyValueExtractorProvider propertyValueExtractorProvider,
                                             SearchProperties searchProperties,
                                             ContextArgumentResolverComposite resolvers,
                                             List<AttributesGroupProcessor<?>> attributesGroupProcessors,
                                             InstanceNameRelatedPropertiesResolver instanceNameRelatedPropertiesResolver,
                                             MetadataTools metadataTools,
                                             IndexNameGenerator indexNameGenerator) {
        this.metadata = metadata;
        this.mappingFieldAnnotationProcessorsRegistry = mappingFieldAnnotationProcessorsRegistry;
        this.propertyValueExtractorProvider = propertyValueExtractorProvider;
        this.searchProperties = searchProperties;
        this.methodArgumentsProvider = new MethodArgumentsProvider(resolvers);
        this.attributesGroupProcessors = attributesGroupProcessors;
        this.instanceNameRelatedPropertiesResolver = instanceNameRelatedPropertiesResolver;
        this.metadataTools = metadataTools;
        this.indexNameGenerator = indexNameGenerator;
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

        IndexMappingConfiguration indexMappingConfiguration = createIndexMappingConfig(indexDef);
        Set<Class<?>> affectedEntityClasses = getAffectedEntityClasses(indexMappingConfiguration);
        log.debug("Index Definition class {}. Affected entity classes = {}", className, affectedEntityClasses);

        Predicate<Object> indexablePredicate = createIndexablePredicate(indexDef);

        IndexConfiguration indexConfiguration = new IndexConfiguration(
                indexDef.getMetaClass().getName(),
                indexDef.getEntityClass(),
                indexMappingConfiguration,
                affectedEntityClasses,
                indexablePredicate,
                indexDef.getExtendedSearchSettings(),
                metadataTools.isTenantAware(indexDef.getMetaClass()),
                resolveIndexNamePattern(indexDefClass, indexDef)
        );

        log.debug("Index configuration: {}", format(indexConfiguration));

        return indexConfiguration;
    }

    /**
     * Creates an index configuration for an entity that has no annotated Java definition.
     *
     * @param definition contributed definition
     * @return index configuration
     */
    public IndexConfiguration createIndexConfiguration(ContributedIndexDefinition definition) {
        MetaClass metaClass = metadata.getClass(definition.getEntityName());
        String source = String.format("Contributed index definition of entity '%s'", metaClass.getName());
        ExtendedSearchSettings extendedSearchSettings = definition.getExtendedSearchSettings();
        Map<String, MappingFieldDescriptor> fields = processMappingDefinition(
                metaClass, definition.getMappingDefinition(), extendedSearchSettings);
        IndexMappingConfiguration mapping = new IndexMappingConfiguration(
                metaClass, fields, createDisplayedNameDescriptor(metaClass));
        return new IndexConfiguration(
                metaClass.getName(),
                metaClass.getJavaClass(),
                mapping,
                getAffectedEntityClasses(mapping),
                definition.getIndexablePredicate(),
                extendedSearchSettings,
                metadataTools.isTenantAware(metaClass),
                resolveIndexNamePattern(definition.getIndexName(), source, metaClass));
    }

    /**
     * Adds the fields of a contributed definition to an existing configuration. Fields already present
     * in the existing configuration win over contributed ones with the same name. The index name, the extended
     * search settings and the indexable predicate of the existing configuration are kept.
     *
     * @param base       configuration built from the annotated Java definition
     * @param definition contributed definition for the same entity
     * @return a new index configuration with the merged fields
     */
    public IndexConfiguration appendContributedFields(IndexConfiguration base, ContributedIndexDefinition definition) {
        if (definition.getIndexName() != null) {
            log.warn("Index name pattern '{}' contributed for entity '{}' is dropped, the entity is already named " +
                            "by '{}'", definition.getIndexName(), definition.getEntityName(),
                    base.getIndexNamePattern());
        }
        IndexMappingConfiguration baseMapping = base.getMapping();
        MetaClass metaClass = baseMapping.getEntityMetaClass();
        Map<String, MappingFieldDescriptor> fields = new HashMap<>(baseMapping.getFields());
        processMappingDefinition(metaClass, definition.getMappingDefinition(), base.getExtendedSearchSettings())
                .forEach(fields::putIfAbsent);
        IndexMappingConfiguration mapping = new IndexMappingConfiguration(
                metaClass, fields, baseMapping.getDisplayedNameDescriptor());
        return new IndexConfiguration(
                base.getEntityName(),
                base.getEntityClass(),
                mapping,
                getAffectedEntityClasses(mapping),
                base.getIndexablePredicate(),
                base.getExtendedSearchSettings(),
                base.isTenantAware(),
                base.getIndexNamePattern());
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

        ExtendedSearchSettings extendedSearchSettings = createExtendedSearchSettings(indexDefinitionClass);
        result.setExtendedSearchSettings(extendedSearchSettings);

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

    protected ExtendedSearchSettings createExtendedSearchSettings(Class<?> indexDefinitionClass) {
        ExtendedSearch extendedSearchAnnotation = indexDefinitionClass.getAnnotation(ExtendedSearch.class);
        ExtendedSearchSettings extendedSearchSettings;
        if (extendedSearchAnnotation != null) {
            extendedSearchSettings = ExtendedSearchSettings.builder()
                    .setEdgeNGramMin(searchProperties.getMinPrefixLength())
                    .setEdgeNGramMax(searchProperties.getMaxPrefixLength())
                    .setEnabled(extendedSearchAnnotation.enabled())
                    .setTokenizer(extendedSearchAnnotation.tokenizer())
                    .setAdditionalFilters(extendedSearchAnnotation.additionalFilters())
                    .build();
        } else {
            extendedSearchSettings = ExtendedSearchSettings.empty();
        }
        return extendedSearchSettings;
    }

    protected IndexMappingConfiguration createIndexMappingConfig(ParsedIndexDefinition parsedIndexDefinition) {
        IndexMappingConfiguration indexMappingConfiguration;

        MetaClass parsedIndexDefinitionMetaClass = parsedIndexDefinition.getMetaClass();
        DisplayedNameDescriptor displayedNameDescriptor = createDisplayedNameDescriptor(parsedIndexDefinitionMetaClass);
        ExtendedSearchSettings extendedSearchSettings = parsedIndexDefinition.getExtendedSearchSettings();
        MappingDefinition mappingDefinition;
        Method mappingDefinitionImplementationMethod = parsedIndexDefinition.getMappingDefinitionImplementationMethod();
        if (mappingDefinitionImplementationMethod == null) {
            MappingDefinitionBuilder builder = MappingDefinition.builder();
            parsedIndexDefinition.getFieldAnnotations().forEach(annotation -> processAnnotation(
                    builder, annotation, parsedIndexDefinitionMetaClass
            ));
            mappingDefinition = builder.build();
        } else {
            Class<?> returnType = mappingDefinitionImplementationMethod.getReturnType();
            if (!MappingDefinition.class.equals(returnType)) {
                throw new RuntimeException("Method with manual mapping building should return MappingDefinition");
            }
            try {
                mappingDefinition = callMethod(parsedIndexDefinition.getIndexDefinitionClass(), mappingDefinitionImplementationMethod);
            } catch (Exception e) {
                throw new RuntimeException("Failed to call method '" + mappingDefinitionImplementationMethod + "'", e);
            }
        }
        Map<String, MappingFieldDescriptor> fieldDescriptors = processMappingDefinition(
                parsedIndexDefinitionMetaClass, mappingDefinition, extendedSearchSettings
        );
        indexMappingConfiguration = new IndexMappingConfiguration(
                parsedIndexDefinitionMetaClass, fieldDescriptors, displayedNameDescriptor
        );
        return indexMappingConfiguration;
    }

    protected boolean isIndexablePredicateMethod(Method method) {
        return method.isDefault()
               && method.isAnnotationPresent(IndexablePredicate.class);
    }

    protected boolean isMappingDefinitionImplementationMethod(Method method) {
        return method.isDefault()
               && method.isAnnotationPresent(ManualMappingDefinition.class);
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

    /**
     * Takes the index name pattern an entity declares for itself and checks it right away, while the index
     * definition interface is at hand and can be named in the error message.
     * <p>
     * A blank value means the entity declares nothing and the application-wide pattern applies.
     */
    @Nullable
    protected String resolveIndexNamePattern(Class<?> indexDefinitionClass, ParsedIndexDefinition indexDef) {
        return resolveIndexNamePattern(indexDef.getIndexName(),
                String.format("Index definition %s of entity '%s'",
                        indexDefinitionClass.getSimpleName(), indexDef.getMetaClass().getName()),
                indexDef.getMetaClass());
    }

    @Nullable
    protected String resolveIndexNamePattern(@Nullable String pattern, String source, MetaClass metaClass) {
        if (StringUtils.isBlank(pattern)) {
            return null;
        }
        indexNameGenerator.validateEntityIndexNamePattern(pattern, source, indexLayout.isSplitByTenants(metaClass));
        return pattern;
    }

    /**
     * Fails if an entity stored in a single index shared by all tenants maps the data of a tenant-aware entity.
     * <p>
     * A mapped value is copied into the document, so the data of one tenant would end up in an index that every
     * tenant searches. Row-level security of the Multitenancy add-on doesn't help here: it constrains the queries
     * to the database, while the value has already been copied to the search engine.
     * <p>
     * The opposite direction is safe: an entity split by tenants that maps a shared one merely duplicates data
     * that everyone is allowed to see into the index of every tenant. Being split is what matters, not carrying
     * a tenant attribute - an entity whose attribute buys it no separate index is on the shared side here.
     * <p>
     * Called over an assembled configuration rather than inside the methods that build one: a definition is built
     * from an annotation, contributed whole, or contributed on top of an existing one, and a field reaching the
     * data of a tenant is equally forbidden whichever way it got there.
     */
    public void checkNoTenantDataInSharedIndex(String source, IndexMappingConfiguration mappingConfiguration) {
        if (!indexLayout.isSplitByTenantsEnabled()) {
            return;
        }
        MetaClass rootMetaClass = mappingConfiguration.getEntityMetaClass();
        if (indexLayout.isSplitByTenants(rootMetaClass)) {
            return;
        }

        mappingConfiguration.getFields().values().forEach(field ->
                checkPropertyPath(source, rootMetaClass,
                        field.getMetaPropertyPath(), field.getEntityPropertyFullName()));

        mappingConfiguration.getDisplayedNameDescriptor().getInstanceNameRelatedProperties().forEach(path ->
                checkPropertyPath(source, rootMetaClass, path, path.toPathString()));
    }

    protected void checkPropertyPath(String source,
                                     MetaClass rootMetaClass,
                                     MetaPropertyPath propertyPath,
                                     String propertyDescription) {
        for (MetaProperty property : propertyPath.getMetaProperties()) {
            if (!property.getRange().isClass()) {
                continue;
            }
            MetaClass referencedMetaClass = property.getRange().asClass();
            if (metadataTools.isTenantAware(referencedMetaClass)) {
                // The source names itself - "Index definition of entity 'Catalog'" - so it opens the sentence
                // instead of sitting behind a second "its index definition", which read twice over.
                throw new IndexDefinitionRejectedException(String.format(
                        "%s maps property '%s' of tenant-aware entity '%s', but '%s' is stored in a single index"
                                + " shared by all tenants. The data of one tenant would become searchable by the"
                                + " others. Either drop the mapped property or make '%s' tenant-aware.",
                        source, propertyDescription, referencedMetaClass.getName(),
                        rootMetaClass.getName(), rootMetaClass.getName()));
            }
        }
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

    protected Map<String, MappingFieldDescriptor> processMappingDefinition(MetaClass metaClass,
                                                                           MappingDefinition mappingDefinition,
                                                                           ExtendedSearchSettings extendedSearchSettings) {
        List<MappingFieldDescriptor> allMappings = new ArrayList<>();

        attributesGroupProcessors.forEach(processor -> {
            List<? extends AttributesGroupConfiguration> mappingConfigurations = mappingDefinition.getMappingConfigurations(processor.getConfigurationClass());

            List<MappingFieldDescriptor> mappings = mappingConfigurations.stream()
                    .map(item -> processor.processAttributesGroup(metaClass, item, extendedSearchSettings))
                    .flatMap(Collection::stream)
                    .toList();
            allMappings.addAll(mappings);
        });
        return allMappings.stream()
                .collect(Collectors.toMap(MappingFieldDescriptor::getIndexPropertyFullName, Function.identity(), (v1, v2) -> {
                    int order1 = v1.getOrder();
                    int order2 = v2.getOrder();
                    if (order1 == order2) {
                        throw new RuntimeException(String.format(MAPPINGS_CONFLICT_ERROR_MESSAGE, v1.getIndexPropertyFullName(), v2.getIndexPropertyFullName()));
                    }
                    return order1 < order2 ? v2 : v1;
                }));
    }

    protected DisplayedNameDescriptor createDisplayedNameDescriptor(MetaClass metaClass) {
        DisplayedNameDescriptor displayedNameDescriptor = new DisplayedNameDescriptor();
        FieldConfiguration fieldConfiguration = FieldConfiguration.create(
                new TextFieldMapper().createJsonConfiguration(Collections.emptyMap())
        );
        displayedNameDescriptor.setFieldConfiguration(fieldConfiguration);

        List<MetaPropertyPath> instanceNameRelatedProperties = instanceNameRelatedPropertiesResolver.resolveInstanceNameRelatedProperties(metaClass, null);
        displayedNameDescriptor.setInstanceNameRelatedProperties(instanceNameRelatedProperties);
        displayedNameDescriptor.setValueExtractor(propertyValueExtractorProvider.getPropertyValueExtractor(DisplayedNameValueExtractor.class));

        return displayedNameDescriptor;
    }

    protected static class ParsedIndexDefinition {
        private final Class<?> indexDefinitionClass;
        private Class<?> entityClass;
        private MetaClass metaClass;
        private String indexName;
        private final List<Annotation> fieldAnnotations = new ArrayList<>();
        private final List<Method> indexablePredicateMethods = new ArrayList<>();
        private Method mappingDefinitionImplementationMethod = null;
        private ExtendedSearchSettings extendedSearchSettings;

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

        private ExtendedSearchSettings getExtendedSearchSettings() {
            return extendedSearchSettings;
        }

        private void setExtendedSearchSettings(@Nullable ExtendedSearchSettings extendedSearchSettings) {
            this.extendedSearchSettings = extendedSearchSettings;
        }
    }
}
