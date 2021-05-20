package io.jmix.graphql.spqr;

import graphql.schema.*;
import io.jmix.graphql.configuration.JmixConfigurationException;
import io.jmix.graphql.schema.GenericSchemaGenerator;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.graphql.*;
import io.leangen.graphql.execution.GlobalEnvironment;
import io.leangen.graphql.execution.ResolverInterceptor;
import io.leangen.graphql.execution.ResolverInterceptorFactory;
import io.leangen.graphql.execution.ResolverInterceptorFactoryParams;
import io.leangen.graphql.generator.BuildContext;
import io.leangen.graphql.generator.JavaDeprecationMappingConfig;
import io.leangen.graphql.generator.OperationSourceRegistry;
import io.leangen.graphql.generator.RelayMappingConfig;
import io.leangen.graphql.generator.mapping.*;
import io.leangen.graphql.generator.mapping.SchemaTransformer;
import io.leangen.graphql.generator.mapping.common.IdAdapter;
import io.leangen.graphql.generator.mapping.strategy.*;
import io.leangen.graphql.metadata.exceptions.TypeMappingException;
import io.leangen.graphql.metadata.messages.DelegatingMessageBundle;
import io.leangen.graphql.metadata.strategy.InclusionStrategy;
import io.leangen.graphql.metadata.strategy.query.*;
import io.leangen.graphql.metadata.strategy.type.DefaultTypeInfoGenerator;
import io.leangen.graphql.metadata.strategy.type.DefaultTypeTransformer;
import io.leangen.graphql.metadata.strategy.type.TypeInfoGenerator;
import io.leangen.graphql.metadata.strategy.type.TypeTransformer;
import io.leangen.graphql.metadata.strategy.value.InputFieldBuilder;
import io.leangen.graphql.metadata.strategy.value.ScalarDeserializationStrategy;
import io.leangen.graphql.metadata.strategy.value.ValueMapper;
import io.leangen.graphql.metadata.strategy.value.ValueMapperFactory;
import io.leangen.graphql.module.Module;
import io.leangen.graphql.util.ClassUtils;
import io.leangen.graphql.util.GraphQLUtils;
import io.leangen.graphql.util.Urls;
import io.leangen.graphql.util.Utils;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * We ought to make a copy of io.leangen.graphql.GraphQLSchemaGenerator in case of private access modifiers of methods ond
 * fields which need to be customized.
 */
public class BaseSpqrSchemaGenerator extends GraphQLSchemaGenerator {

    protected final OperationSourceRegistry operationSourceRegistry = new OperationSourceRegistry();
    protected final List<ExtensionProvider<GeneratorConfiguration, TypeMapper>> typeMapperProviders = new ArrayList<>();
    protected final List<ExtensionProvider<GeneratorConfiguration, SchemaTransformer>> schemaTransformerProviders = new ArrayList<>();
    protected final List<ExtensionProvider<GeneratorConfiguration, InputConverter>> inputConverterProviders = new ArrayList<>();
    protected final List<ExtensionProvider<GeneratorConfiguration, OutputConverter>> outputConverterProviders = new ArrayList<>();
    protected final List<ExtensionProvider<GeneratorConfiguration, ArgumentInjector>> argumentInjectorProviders = new ArrayList<>();
    protected final List<ExtensionProvider<ExtendedGeneratorConfiguration, InputFieldBuilder>> inputFieldBuilderProviders = new ArrayList<>();
    protected final List<ExtensionProvider<GeneratorConfiguration, ResolverBuilder>> resolverBuilderProviders = new ArrayList<>();
    protected final List<ExtensionProvider<GeneratorConfiguration, ResolverBuilder>> nestedResolverBuilderProviders = new ArrayList<>();
    protected final List<ExtensionProvider<GeneratorConfiguration, Module>> moduleProviders = new ArrayList<>();
    protected final List<ExtensionProvider<GeneratorConfiguration, ResolverInterceptorFactory>> interceptorFactoryProviders = new ArrayList<>();
    protected final List<ExtensionProvider<GeneratorConfiguration, Comparator<AnnotatedType>>> typeComparatorProviders = new ArrayList<>();
    protected final Collection<GraphQLSchemaProcessor> processors = new HashSet<>();
    protected final RelayMappingConfig relayMappingConfig = new RelayMappingConfig();
    protected final Map<String, GraphQLDirective> additionalDirectives = new HashMap<>();
    protected final List<AnnotatedType> additionalDirectiveTypes = new ArrayList<>();
    protected final GraphQLCodeRegistry.Builder codeRegistry = GraphQLCodeRegistry.newCodeRegistry();
    protected final Map<String, GraphQLNamedType> additionalTypes = new HashMap<>();
    protected final String queryRoot = "Query";
    protected final String mutationRoot = "Mutation";
    protected final String subscriptionRoot = "Subscription";
    protected final String queryRootDescription = "Query root";
    protected final String mutationRootDescription = "Mutation root";
    protected final String subscriptionRootDescription = "Subscription root";
    protected InterfaceMappingStrategy interfaceStrategy = new AnnotatedInterfaceStrategy();
    protected ScalarDeserializationStrategy scalarStrategy;
    protected AbstractInputHandler abstractInputHandler = new NoOpAbstractInputHandler();
    protected OperationBuilder operationBuilder = new DefaultOperationBuilder(DefaultOperationBuilder.TypeInference.NONE);
    protected DirectiveBuilder directiveBuilder = new AnnotatedDirectiveBuilder();
    protected ValueMapperFactory valueMapperFactory;
    protected InclusionStrategy inclusionStrategy;
    protected ImplementationDiscoveryStrategy implDiscoveryStrategy = new DefaultImplementationDiscoveryStrategy();
    protected TypeInfoGenerator typeInfoGenerator = new DefaultTypeInfoGenerator();
    protected TypeTransformer typeTransformer = new DefaultTypeTransformer(false, false);
    protected GlobalEnvironment environment;
    protected String[] basePackages = Utils.emptyArray();
    protected DelegatingMessageBundle messageBundle = new DelegatingMessageBundle();
    protected List<TypeMapper> typeMappers;
    protected List<SchemaTransformer> transformers;
    protected Comparator<AnnotatedType> typeComparator;
    protected List<InputFieldBuilder> inputFieldBuilders;
    protected ResolverInterceptorFactory interceptorFactory;
    protected JavaDeprecationMappingConfig javaDeprecationConfig = new JavaDeprecationMappingConfig(true, "Deprecated");

    protected final GenericSchemaGenerator genericGenerator;

    public BaseSpqrSchemaGenerator(GenericSchemaGenerator genericGenerator) {
        this.genericGenerator = genericGenerator;
    }

    public BaseSpqrSchemaGenerator withBasePackages(String... basePackages) {
        this.basePackages = Utils.emptyIfNull(basePackages);
        return this;
    }

    public BaseSpqrSchemaGenerator withDataFetchers(GraphQLCodeRegistry dataFetchers) {
        this.codeRegistry.dataFetchers(dataFetchers);
        return this;
    }

    public GraphQLSchemaGenerator withTypeMappers(TypeMapper... typeMappers) {
        return withTypeMappers((conf, current) -> current.insertAfterOrAppend(IdAdapter.class, typeMappers));
    }

    public GraphQLSchemaGenerator withTypeMappersPrepended(TypeMapper... typeMappers) {
        this.typeMapperProviders.add(0, (conf, current) -> current.insertAfterOrAppend(IdAdapter.class, typeMappers));
        return this;
    }

    public GraphQLSchemaGenerator withTypeMappersPrepended(ExtensionProvider<GeneratorConfiguration, TypeMapper> provider) {
        this.typeMapperProviders.add(0, provider);
        return this;
    }

    public GraphQLSchemaGenerator withTypeMappers(ExtensionProvider<GeneratorConfiguration, TypeMapper> provider) {
        this.typeMapperProviders.add(provider);
        return this;
    }

    @Deprecated
    public GraphQLSchemaGenerator withAdditionalTypes(Collection<GraphQLType> additionalTypes) {
        return withAdditionalTypes(additionalTypes, new NoOpCodeRegistryBuilder());
    }

    public GraphQLSchemaGenerator withAdditionalTypes(Collection<? extends GraphQLType> additionalTypes, GraphQLCodeRegistry codeRegistry) {
        return withAdditionalTypes(additionalTypes, new CodeRegistryMerger(codeRegistry));
    }

    public GraphQLSchemaGenerator withAdditionalTypes(Collection<? extends GraphQLType> additionalTypes, CodeRegistryBuilder codeRegistryUpdater) {
        additionalTypes.forEach(type -> merge(type, this.additionalTypes, codeRegistryUpdater, this.codeRegistry));
        return this;
    }

    public BaseSpqrSchemaGenerator withOperationsFromBean(Supplier<Object> serviceSupplier, Type beanType, ResolverBuilder... builders) {
        return withOperationsFromBean(serviceSupplier, GenericTypeReflector.annotate(checkType(beanType)), ClassUtils.getRawType(beanType), builders);
    }

    /**
     * Same as {@link #withOperationsFromBean(Supplier, Type, ResolverBuilder...)}, except that an {@link AnnotatedType}
     * is used as the static type of the instances provided by {@code serviceSupplier}.
     * Needed when type annotations such as {@link io.leangen.graphql.annotations.GraphQLNonNull} not directly declared on the class should be captured.
     */
    public BaseSpqrSchemaGenerator withOperationsFromBean(Supplier<Object> serviceSupplier, AnnotatedType beanType, ResolverBuilder... builders) {
        return withOperationsFromBean(serviceSupplier, beanType, ClassUtils.getRawType(beanType.getType()),  builders);
    }

    /**
     * Same as {@link #withOperationsFromBean(Supplier, Type, ResolverBuilder...)}, but the actual runtime type of
     * the instances provided by {@code serviceSupplier} will be used to choose the method to invoke at runtime.
     * This is the absolute safest approach to registering beans, and is needed when the instances are proxied
     * by a container (e.g. Spring, CDI or others) and can _not_ be cast to {@code beanType} at runtime.
     *
     * @param serviceSupplier The supplier that will be used to obtain an instance on which the exposed methods
     *                        will be invoked when resolving queries/mutations/subscriptions.
     * @param beanType Static type of instances provided by {@code serviceSupplier}.
     *                 Use {@link io.leangen.geantyref.TypeToken} to get a {@link Type} literal
     *                 or {@link io.leangen.geantyref.TypeFactory} to create it dynamically.
     * @param exposedType Runtime type of the instances provided by {@code serviceSupplier},
     *                    not necessarily possible to cast to {@code beanType}
     * @param builders Custom strategy to use when analyzing {@code beanType}
     *
     * @return This {@link GraphQLSchemaGenerator} instance, to allow method chaining
     */
    public BaseSpqrSchemaGenerator withOperationsFromBean(Supplier<Object> serviceSupplier, Type beanType, Class<?> exposedType, ResolverBuilder... builders) {
        return withOperationsFromBean(serviceSupplier, GenericTypeReflector.annotate(checkType(beanType)), exposedType, builders);
    }

    /**
     * Same as {@link #withOperationsFromBean(Supplier, Type, Class, ResolverBuilder...)}, except that an {@link AnnotatedType}
     * is used as the static type of the instances provided by {@code serviceSupplier}.
     * Needed when type annotations such as {@link io.leangen.graphql.annotations.GraphQLNonNull} not directly declared on the class should be captured.
     */
    public BaseSpqrSchemaGenerator withOperationsFromBean(Supplier<Object> serviceSupplier, AnnotatedType beanType, Class<?> exposedType, ResolverBuilder... builders) {
        checkType(beanType);
        this.operationSourceRegistry.registerOperationSource(serviceSupplier, beanType, exposedType, Utils.asList(builders));
        return this;
    }

    public BaseSpqrSchemaGenerator withResolverBuilders(ResolverBuilder... resolverBuilders) {
        return withResolverBuilders((config, defaults) -> Arrays.asList(resolverBuilders));
    }

    public BaseSpqrSchemaGenerator withResolverBuilders(ExtensionProvider<GeneratorConfiguration, ResolverBuilder> provider) {
        this.resolverBuilderProviders.add(provider);
        return this;
    }

    private boolean isRealType(GraphQLNamedType type) {
        // Reject introspection types
        return !(GraphQLUtils.isIntrospectionType(type)
                // Reject quasi-types
                || type instanceof GraphQLTypeReference
                || type instanceof GraphQLArgument
                || type instanceof GraphQLDirective
                // Reject root types
                || type.getName().equals(messageBundle.interpolate(queryRoot))
                || type.getName().equals(messageBundle.interpolate(mutationRoot))
                || type.getName().equals(messageBundle.interpolate(subscriptionRoot)));
    }

    protected Type checkType(Type type) {
        if (type == null) {
            throw TypeMappingException.unknownType();
        }
        Class<?> clazz = ClassUtils.getRawType(type);
        if (ClassUtils.isProxy(clazz)) {
            throw new TypeMappingException("The registered object of type " + clazz.getName() +
                    " appears to be a dynamically generated proxy, so its type can not be reliably determined." +
                    " Provide the type explicitly when registering the bean." +
                    " For details and solutions see " + Urls.Errors.DYNAMIC_PROXIES);
        }
        if (ClassUtils.isMissingTypeParameters(type)) {
            throw new TypeMappingException("The registered object is of generic type " + type.getTypeName() + "." +
                    " Provide the full type explicitly when registering the bean." +
                    " For details and solutions see " + Urls.Errors.TOP_LEVEL_GENERICS);
        }
        return type;
    }

    protected void checkType(AnnotatedType type) {
        if (type == null) {
            throw TypeMappingException.unknownType();
        }
        checkType(type.getType());
    }

    protected void checkForEmptyOrDuplicates(String extensionType, List<?> extensions) {
        if (extensions.isEmpty()) {
            throw new JmixConfigurationException("No " + extensionType + "SimpleFieldValidation registered");
        }
        checkForDuplicates(extensionType, extensions);
    }

    protected <E> void checkForDuplicates(String extensionType, List<E> extensions) {
        Set<E> seen = new HashSet<>();
        extensions.forEach(element -> {
            if (!seen.add(element)) {
                throw new JmixConfigurationException("Duplicate " + extensionType + " of type " + element.getClass().getName() + " registered");
            }
        });
    }

    protected void applyProcessors(GraphQLSchema.Builder builder, BuildContext buildContext) {
        for (GraphQLSchemaProcessor processor : processors) {
            processor.process(builder, buildContext);
        }
    }

    private void merge(GraphQLType type, Map<String, GraphQLNamedType> additionalTypes, CodeRegistryBuilder updater, GraphQLCodeRegistry.Builder builder) {
        GraphQLNamedType namedType = GraphQLUtils.unwrap(type);
        if (!isRealType(namedType)) {
            return;
        }
        if (additionalTypes.containsKey(namedType.getName())) {
            if (additionalTypes.get(namedType.getName()).equals(namedType)) {
                return;
            }
            throw new JmixConfigurationException("Type name collision: multiple registered additional types are named '" + namedType.getName() + "'");
        }
        additionalTypes.put(namedType.getName(), namedType);

        if (namedType instanceof GraphQLInterfaceType) {
            TypeResolver typeResolver = updater.getTypeResolver((GraphQLInterfaceType) namedType);
            if (typeResolver != null) {
                builder.typeResolverIfAbsent((GraphQLInterfaceType) namedType, typeResolver);
            }
        }
        if (namedType instanceof GraphQLUnionType) {
            TypeResolver typeResolver = updater.getTypeResolver((GraphQLUnionType) namedType);
            if (typeResolver != null) {
                builder.typeResolverIfAbsent((GraphQLUnionType) namedType, typeResolver);
            }
        }
        if (namedType instanceof GraphQLFieldsContainer) {
            GraphQLFieldsContainer fieldsContainer = (GraphQLFieldsContainer) namedType;
            fieldsContainer.getFieldDefinitions().forEach(fieldDef -> {
                DataFetcher<?> dataFetcher = updater.getDataFetcher(fieldsContainer, fieldDef);
                if (dataFetcher != null) {
                    builder.dataFetcherIfAbsent(FieldCoordinates.coordinates(fieldsContainer, fieldDef), dataFetcher);
                }
                merge(fieldDef.getType(), additionalTypes, updater, builder);

                fieldDef.getArguments().forEach(arg -> merge(arg.getType(), additionalTypes, updater, builder));
            });
        }
        if (namedType instanceof GraphQLInputFieldsContainer) {
            ((GraphQLInputFieldsContainer) namedType).getFieldDefinitions()
                    .forEach(fieldDef -> merge(fieldDef.getType(), additionalTypes, updater, builder));
        }
    }

    private static class CodeRegistryMerger implements CodeRegistryBuilder {

        private final GraphQLCodeRegistry codeRegistry;

        public CodeRegistryMerger(GraphQLCodeRegistry codeRegistry) {
            this.codeRegistry = codeRegistry;
        }

        @Override
        public TypeResolver getTypeResolver(GraphQLInterfaceType interfaceType) {
            return codeRegistry.getTypeResolver(interfaceType);
        }

        @Override
        public TypeResolver getTypeResolver(GraphQLUnionType unionType) {
            return codeRegistry.getTypeResolver(unionType);
        }

        @Override
        public DataFetcher<?> getDataFetcher(GraphQLFieldsContainer parentType, GraphQLFieldDefinition fieldDef) {
            return codeRegistry.getDataFetcher(parentType, fieldDef);
        }
    }

    protected static class DelegatingResolverInterceptorFactory implements ResolverInterceptorFactory {

        protected final List<ResolverInterceptorFactory> delegates;

        protected DelegatingResolverInterceptorFactory(List<ResolverInterceptorFactory> delegates) {
            this.delegates = delegates;
        }

        @Override
        public List<ResolverInterceptor> getInterceptors(ResolverInterceptorFactoryParams params) {
            return delegates.stream()
                    .flatMap(delegate -> delegate.getInterceptors(params).stream())
                    .collect(Collectors.toList());
        }
    }

    protected static class MemoizedValueMapperFactory implements ValueMapperFactory {

        protected final ValueMapper defaultValueMapper;
        protected final ValueMapperFactory delegate;

        public MemoizedValueMapperFactory(GlobalEnvironment environment, ValueMapperFactory delegate) {
            this.defaultValueMapper = delegate.getValueMapper(Collections.emptyMap(), environment);
            this.delegate = delegate;
        }

        @Override
        public ValueMapper getValueMapper(Map<Class, List<Class<?>>> concreteSubTypes, GlobalEnvironment environment) {
            if (concreteSubTypes.isEmpty() || concreteSubTypes.values().stream().allMatch(List::isEmpty)) {
                return this.defaultValueMapper;
            }
            return delegate.getValueMapper(concreteSubTypes, environment);
        }
    }

    private static class NoOpCodeRegistryBuilder implements CodeRegistryBuilder {}

}
