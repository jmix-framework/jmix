package io.jmix.graphql.spqr;

import graphql.relay.Relay;
import graphql.schema.*;
import io.jmix.graphql.schema.GenericSchemaGenerator;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeFactory;
import io.leangen.graphql.*;
import io.leangen.graphql.execution.GlobalEnvironment;
import io.leangen.graphql.execution.ResolverInterceptorFactory;
import io.leangen.graphql.generator.*;
import io.leangen.graphql.generator.mapping.SchemaTransformer;
import io.leangen.graphql.generator.mapping.*;
import io.leangen.graphql.generator.mapping.common.*;
import io.leangen.graphql.generator.mapping.core.CompletableFutureAdapter;
import io.leangen.graphql.generator.mapping.core.DataFetcherResultMapper;
import io.leangen.graphql.generator.mapping.core.PublisherAdapter;
import io.leangen.graphql.metadata.strategy.DefaultInclusionStrategy;
import io.leangen.graphql.metadata.strategy.query.AnnotatedResolverBuilder;
import io.leangen.graphql.metadata.strategy.query.BeanResolverBuilder;
import io.leangen.graphql.metadata.strategy.query.ResolverBuilder;
import io.leangen.graphql.metadata.strategy.value.*;
import io.leangen.graphql.module.Module;
import io.leangen.graphql.util.Defaults;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.*;

import static graphql.schema.GraphQLObjectType.newObject;

public class SpqrSchemaGenerator extends BaseSpqrSchemaGenerator {

    protected final GenericSchemaGenerator genericGenerator;

    @Autowired
    private SpqrCustomSchemeRegistry schemeRegistry;

    public SpqrSchemaGenerator(GenericSchemaGenerator genericGenerator) {
        this.genericGenerator = genericGenerator;
    }

    public SpqrSchemaGenerator withDataFetchers(GraphQLCodeRegistry dataFetchers) {
        this.codeRegistry.dataFetchers(dataFetchers);
        return this;
    }

    public GraphQLSchema generate() {
        init();

        final String queryRootName = messageBundle.interpolate(queryRoot);
        final String mutationRootName = messageBundle.interpolate(mutationRoot);
        final String subscriptionRootName = messageBundle.interpolate(subscriptionRoot);

        BuildContext buildContext = new BuildContext(
                basePackages, environment, new OperationRegistry(operationSourceRegistry, operationBuilder, inclusionStrategy,
                typeTransformer, basePackages, environment), new TypeMapperRegistry(typeMappers),
                new SchemaTransformerRegistry(transformers), valueMapperFactory, typeInfoGenerator, messageBundle, interfaceStrategy,
                scalarStrategy, typeTransformer, abstractInputHandler, new DelegatingInputFieldBuilder(inputFieldBuilders),
                interceptorFactory, directiveBuilder, inclusionStrategy, relayMappingConfig, additionalTypes.values(),
                additionalDirectiveTypes, typeComparator, implDiscoveryStrategy, codeRegistry);
        OperationMapper operationMapper = new OperationMapper(queryRootName, mutationRootName, subscriptionRootName, buildContext);

        schemeRegistry.addOperations(operationMapper.getQueries());
        schemeRegistry.addOperations(operationMapper.getMutations());

        GraphQLSchema.Builder schemaBuilder = GraphQLSchema.newSchema();
        GraphQLObjectType.Builder fields = newObject()
                .name(queryRootName)
                .description(messageBundle.interpolate(queryRootDescription))
                .fields(operationMapper.getQueries())
                .fields(genericGenerator.generateQueryFields());
        schemaBuilder.query(fields.build());

        List<GraphQLFieldDefinition> mutations = operationMapper.getMutations();
        schemaBuilder.mutation(newObject()
                .name(mutationRootName)
                .description(messageBundle.interpolate(mutationRootDescription))
                .fields(mutations)
                .fields(genericGenerator.generateMutationFields())
                .build());

        List<GraphQLFieldDefinition> subscriptions = operationMapper.getSubscriptions();
        if (!subscriptions.isEmpty()) {
            schemaBuilder.subscription(newObject()
                    .name(subscriptionRootName)
                    .description(messageBundle.interpolate(subscriptionRootDescription))
                    .fields(subscriptions)
                    .build());
        }

        Set<GraphQLType> additional = new HashSet<>(additionalTypes.values());
        additional.addAll(buildContext.typeRegistry.getDiscoveredTypes());
        schemaBuilder.additionalTypes(additional);

        schemaBuilder.additionalDirectives(new HashSet<>(additionalDirectives.values()));
        schemaBuilder.additionalDirectives(new HashSet<>(operationMapper.getDirectives()));

        schemaBuilder.codeRegistry(buildContext.codeRegistry.build());

        applyProcessors(schemaBuilder, buildContext);
        buildContext.executePostBuildHooks();
        return schemaBuilder.build();
    }

    protected void init() {
        GeneratorConfiguration configuration = new SpqrGeneratorConfiguration(interfaceStrategy, scalarStrategy, typeTransformer, basePackages, javaDeprecationConfig);

        //Modules must go first to get a chance to change other settings
        List<Module> modules = Defaults.modules();
        for (ExtensionProvider<GeneratorConfiguration, Module> provider : moduleProviders) {
            modules = provider.getExtensions(configuration, new ExtensionList<>(modules));
        }
        checkForDuplicates("modules", modules);
//        modules.forEach(module -> module.setUp(() -> this));

//        if (operationSourceRegistry.isEmpty()) {
//            throw new IllegalStateException("At least one top-level operation source must be registered");
//        }

        if (inclusionStrategy == null) {
            inclusionStrategy = new DefaultInclusionStrategy(basePackages);
        }
        ValueMapperFactory internalValueMapperFactory = valueMapperFactory != null
                ? valueMapperFactory
                : Defaults.valueMapperFactory(typeInfoGenerator);
        if (scalarStrategy == null) {
            if (internalValueMapperFactory instanceof ScalarDeserializationStrategy) {
                scalarStrategy = (ScalarDeserializationStrategy) internalValueMapperFactory;
            } else {
                scalarStrategy = (ScalarDeserializationStrategy) Defaults.valueMapperFactory(typeInfoGenerator);
            }
        }

        List<ResolverBuilder> resolverBuilders = Collections.singletonList(new AnnotatedResolverBuilder());
        for (ExtensionProvider<GeneratorConfiguration, ResolverBuilder> provider : resolverBuilderProviders) {
            resolverBuilders = provider.getExtensions(configuration, new ExtensionList<>(resolverBuilders));
        }
        checkForEmptyOrDuplicates("resolver builders", resolverBuilders);
        operationSourceRegistry.registerGlobalResolverBuilders(resolverBuilders);

        List<ResolverBuilder> nestedResolverBuilders = Arrays.asList(
                new AnnotatedResolverBuilder(),
                new BeanResolverBuilder(basePackages).withJavaDeprecation(javaDeprecationConfig));
        for (ExtensionProvider<GeneratorConfiguration, ResolverBuilder> provider : nestedResolverBuilderProviders) {
            nestedResolverBuilders = provider.getExtensions(configuration, new ExtensionList<>(nestedResolverBuilders));
        }
        checkForEmptyOrDuplicates("nested resolver builders", nestedResolverBuilders);
        operationSourceRegistry.registerGlobalNestedResolverBuilders(nestedResolverBuilders);

        ObjectTypeMapper objectTypeMapper = new ObjectTypeMapper();
        PublisherAdapter publisherAdapter = new PublisherAdapter();
        EnumMapper enumMapper = new EnumMapper(javaDeprecationConfig);
        typeMappers = Arrays.asList(
                new NonNullMapper(), new IdAdapter(), new ScalarMapper(), new CompletableFutureAdapter<>(),
                publisherAdapter, new AnnotationMapper(), new OptionalIntAdapter(), new OptionalLongAdapter(), new OptionalDoubleAdapter(),
                enumMapper, new ArrayAdapter(), new UnionTypeMapper(), new UnionInlineMapper(),
                new StreamToCollectionTypeAdapter(), new DataFetcherResultMapper<>(), new VoidToBooleanTypeAdapter(),
                new ListMapper(), new IterableAdapter<>(), new PageMapper(), new OptionalAdapter(), new EnumMapToObjectTypeAdapter(enumMapper),
                new ObjectScalarMapper(), new InterfaceMapper(interfaceStrategy, objectTypeMapper), objectTypeMapper);
        for (ExtensionProvider<GeneratorConfiguration, TypeMapper> provider : typeMapperProviders) {
            typeMappers = provider.getExtensions(configuration, new ExtensionList<>(typeMappers));
        }
        checkForEmptyOrDuplicates("type mappers", typeMappers);

        transformers = Arrays.asList(new NonNullMapper(), publisherAdapter);
        for (ExtensionProvider<GeneratorConfiguration, SchemaTransformer> provider : schemaTransformerProviders) {
            transformers = provider.getExtensions(configuration, new ExtensionList<>(transformers));
        }
        checkForEmptyOrDuplicates("schema transformers", transformers);

        List<OutputConverter> outputConverters = Arrays.asList(
                new IdAdapter(), new ArrayAdapter(), new CollectionOutputConverter(), new CompletableFutureAdapter<>(),
                new OptionalIntAdapter(), new OptionalLongAdapter(), new OptionalDoubleAdapter(), new OptionalAdapter(),
                new StreamToCollectionTypeAdapter(), publisherAdapter);
        for (ExtensionProvider<GeneratorConfiguration, OutputConverter> provider : outputConverterProviders) {
            outputConverters = provider.getExtensions(configuration, new ExtensionList<>(outputConverters));
        }
        checkForDuplicates("output converters", outputConverters);

        List<InputConverter> inputConverters = Arrays.asList(new CompletableFutureAdapter<>(),
                new StreamToCollectionTypeAdapter(), new IterableAdapter<>(), new EnumMapToObjectTypeAdapter(enumMapper));
        for (ExtensionProvider<GeneratorConfiguration, InputConverter> provider : inputConverterProviders) {
            inputConverters = provider.getExtensions(configuration, new ExtensionList<>(inputConverters));
        }
        checkForDuplicates("input converters", inputConverters);

        List<ArgumentInjector> argumentInjectors = Arrays.asList(
                new IdAdapter(), new RootContextInjector(), new ContextInjector(),
                new EnvironmentInjector(), new DirectiveValueDeserializer(), new InputValueDeserializer());
        for (ExtensionProvider<GeneratorConfiguration, ArgumentInjector> provider : argumentInjectorProviders) {
            argumentInjectors = provider.getExtensions(configuration, new ExtensionList<>(argumentInjectors));
        }
        checkForDuplicates("argument injectors", argumentInjectors);

        List<ResolverInterceptorFactory> interceptorFactories = Collections.singletonList(new VoidToBooleanTypeAdapter());
        for (ExtensionProvider<GeneratorConfiguration, ResolverInterceptorFactory> provider : this.interceptorFactoryProviders) {
            interceptorFactories = provider.getExtensions(configuration, new ExtensionList<>(interceptorFactories));
        }
        interceptorFactory = new DelegatingResolverInterceptorFactory(interceptorFactories);

        environment = new GlobalEnvironment(messageBundle, new Relay(), new TypeRegistry(additionalTypes.values()),
                new ConverterRegistry(inputConverters, outputConverters), new ArgumentInjectorRegistry(argumentInjectors),
                typeTransformer, inclusionStrategy, typeInfoGenerator);
        ExtendedGeneratorConfiguration extendedConfig = new SpqrExtendedGeneratorConfiguration(configuration, environment);
        valueMapperFactory = new MemoizedValueMapperFactory(environment, internalValueMapperFactory);
        ValueMapper def = valueMapperFactory.getValueMapper(Collections.emptyMap(), environment);

        InputFieldBuilder defaultInputFieldBuilder;
        if (def instanceof InputFieldBuilder) {
            defaultInputFieldBuilder = (InputFieldBuilder) def;
        } else {
            defaultInputFieldBuilder = (InputFieldBuilder) Defaults.valueMapperFactory(typeInfoGenerator).getValueMapper(Collections.emptyMap(), environment);
        }
        inputFieldBuilders = Arrays.asList(new AnnotationInputFieldBuilder(), defaultInputFieldBuilder);
        for (ExtensionProvider<ExtendedGeneratorConfiguration, InputFieldBuilder> provider : this.inputFieldBuilderProviders) {
            inputFieldBuilders = provider.getExtensions(extendedConfig, new ExtensionList<>(inputFieldBuilders));
        }
        checkForEmptyOrDuplicates("input field builders", inputFieldBuilders);

        List<Comparator<AnnotatedType>> typeComparators = new ArrayList<>();
        //Only consider leangen annotations except @GraphQLNonNull
        typeComparators.add(new IgnoredAnnotationsTypeComparator().include("io.leangen").exclude(io.leangen.graphql.annotations.GraphQLNonNull.class));
        Type annotatedTypeComparator = TypeFactory.parameterizedClass(Comparator.class, AnnotatedType.class);
        for (TypeMapper mapper : typeMappers) {
            if (GenericTypeReflector.isSuperType(annotatedTypeComparator, mapper.getClass())) {
                //noinspection unchecked
                typeComparators.add((Comparator<AnnotatedType>) mapper);
            }
        }
        for (ExtensionProvider<GeneratorConfiguration, Comparator<AnnotatedType>> provider : this.typeComparatorProviders) {
            typeComparators = provider.getExtensions(configuration, new ExtensionList<>(typeComparators));
        }
        List<Comparator<AnnotatedType>> finalTypeComparators = typeComparators;
        typeComparator = (t1, t2) -> finalTypeComparators.stream().anyMatch(comparator -> comparator.compare(t1, t2) == 0) ? 0 : -1;
    }


}
