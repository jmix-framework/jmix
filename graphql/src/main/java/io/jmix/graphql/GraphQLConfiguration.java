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

package io.jmix.graphql;

import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import graphql.execution.instrumentation.Instrumentation;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import io.jmix.core.AccessManager;
import io.jmix.core.CoreConfiguration;
import io.jmix.core.Messages;
import io.jmix.core.annotation.JmixModule;
import io.jmix.graphql.datafetcher.MessagesDataFetcher;
import io.jmix.graphql.datafetcher.PermissionDataFetcher;
import io.jmix.graphql.limitation.JmixMaxQueryDepthInstrumentation;
import io.jmix.graphql.limitation.LimitationProperties;
import io.jmix.graphql.limitation.OperationRateLimitInstrumentation;
import io.jmix.graphql.limitation.OperationRateLimitService;
import io.jmix.graphql.schema.ClassTypesGenerator;
import io.jmix.graphql.schema.EnumTypesGenerator;
import io.jmix.graphql.schema.FilterTypesGenerator;
import io.jmix.graphql.schema.GenericSchemaGenerator;
import io.jmix.graphql.schema.JmixTypeInfoGenerator;
import io.jmix.graphql.schema.MessageTypesGenerator;
import io.jmix.graphql.schema.PermissionTypesGenerator;
import io.jmix.graphql.schema.scalar.ScalarTypes;
import io.jmix.graphql.schema.scalar.ScalarTypes;
import io.jmix.graphql.security.SpecificPermissionInstrumentation;
import io.jmix.graphql.security.impl.SecurityInstrumentation;
import io.jmix.graphql.spqr.SpqrCustomSchemeRegistry;
import io.jmix.graphql.spqr.SpqrSchemaGenerator;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.graphql.metadata.strategy.query.AbstractResolverBuilder;
import io.leangen.graphql.metadata.strategy.query.MethodInvokerFactory;
import io.leangen.graphql.metadata.strategy.query.ResolverBuilder;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import io.leangen.graphql.spqr.spring.annotations.WithResolverBuilder;
import io.leangen.graphql.spqr.spring.autoconfigure.AopAwareMethodInvokerFactory;
import io.leangen.graphql.spqr.spring.autoconfigure.SpqrProperties;
import io.leangen.graphql.util.Utils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.StandardMethodMetadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(dependsOn = CoreConfiguration.class)
@PropertySource(name = "io.jmix.graphql", value = "classpath:/io/jmix/graphql/module.properties")
public class GraphQLConfiguration {

    protected final ConfigurableApplicationContext context;
    private final MethodInvokerFactory aopAwareFactory = new AopAwareMethodInvokerFactory();

    @Autowired
    public GraphQLConfiguration(ConfigurableApplicationContext context) {
        this.context = context;
    }

    @Autowired
    EnumTypesGenerator enumTypesGenerator;
    @Autowired
    ClassTypesGenerator classTypesGenerator;
    @Autowired
    GenericSchemaGenerator genericGenerator;
    @Autowired
    FilterTypesGenerator filterTypesGenerator;
    @Autowired
    PermissionTypesGenerator permissionTypesGenerator;
    @Autowired
    MessageTypesGenerator messageTypesGenerator;
    @Autowired
    protected PermissionDataFetcher permissionDataFetcher;
    @Autowired
    protected MessagesDataFetcher messagesDataFetcher;
    @Autowired
    protected OperationRateLimitService operationRateLimitService;
    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected LimitationProperties limitationProperties;
    @Autowired
    protected Messages messages;
    @Autowired
    protected JmixTypeInfoGenerator jmixTypeInfoGenerator;
    @Autowired
    protected ScalarTypes scalarTypes;
    @Autowired
    SpqrCustomSchemeRegistry schemeRegistry;


    @Bean
    public List<Instrumentation> instrumentationList() {
        return Arrays.asList(
                new SecurityInstrumentation(schemeRegistry,accessManager,messages),
                new OperationRateLimitInstrumentation(operationRateLimitService),
                new SpecificPermissionInstrumentation(accessManager, messages),
                new JmixMaxQueryDepthInstrumentation(limitationProperties.getMaxQueryDepth())
        );
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder.serializerByType(Timestamp.class,
                new DateSerializer(false, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")));
    }

    @Bean
    public GraphQLSchema graphQLSchema(SpqrSchemaGenerator generator) {
        Collection<GraphQLType> types = new ArrayList<>();
        // enums
        types.addAll(enumTypesGenerator.generateEnumTypes());
        // out class types
        types.addAll(classTypesGenerator.generateInputTypes());
        // input class types
        types.addAll(classTypesGenerator.generateOutTypes());
        // filter
        types.addAll(filterTypesGenerator.generateFilterTypes());
        // scalars
        types.addAll(scalarTypes.scalars());
        // permissions
        types.addAll(permissionTypesGenerator.generatePermissionTypes());
        // messages
        types.addAll(messageTypesGenerator.generateMessageTypes());

        generator.withAdditionalTypes(types);

        GraphQLCodeRegistry.Builder codeRegistryBuilder = GraphQLCodeRegistry.newCodeRegistry();
        genericGenerator.assignDataFetchers(codeRegistryBuilder);

        // custom queries - permissions and messages
        codeRegistryBuilder.dataFetcher(
                FieldCoordinates.coordinates("Query", NamingUtils.QUERY_PERMISSIONS),
                permissionDataFetcher.loadPermissions());
        codeRegistryBuilder.dataFetcher(
                FieldCoordinates.coordinates("Query", NamingUtils.QUERY_ENTITY_MESSAGES),
                messagesDataFetcher.loadEntityMessages());
        codeRegistryBuilder.dataFetcher(
                FieldCoordinates.coordinates("Query", NamingUtils.QUERY_ENUM_MESSAGES),
                messagesDataFetcher.loadEnumMessages());

        generator.withDataFetchers(codeRegistryBuilder.build());
        generator.withTypeInfoGenerator(jmixTypeInfoGenerator);
        return generator.generate();
    }

    @Bean
    @Primary
    public SpqrSchemaGenerator graphQLSchemaGenerator(SpqrProperties spqrProperties) {
        SpqrSchemaGenerator schemaGenerator = new SpqrSchemaGenerator(genericGenerator);

        schemaGenerator.withBasePackages(spqrProperties.getBasePackages());

//        if (spqrProperties.getRelay().isEnabled()) {
//            if (Utils.isNotEmpty(spqrProperties.getRelay().getMutationWrapper())) {
//                schemaGenerator.withRelayCompliantMutations(
//                        spqrProperties.getRelay().getMutationWrapper(), spqrProperties.getRelay().getMutationWrapperDescription()
//                );
//            } else {
//                schemaGenerator.withRelayCompliantMutations();
//            }
//        }

        Map<String, SpqrBean> apiComponents = findGraphQLApiComponents();
        addOperationSources(schemaGenerator, apiComponents.values());
//
//        // Modules should be registered first, so that extension providers have a chance to override what they need
//        // Built-in modules must go before the user-provided ones for similar reasons
//        if (internalModules != null) {
//            internalModules.forEach(module -> schemaGenerator.withModules(module.get()));
//        }
//
//        if (moduleExtensionProvider != null) {
//            schemaGenerator.withModules(moduleExtensionProvider);
//        }
//
//        if (globalResolverBuilderExtensionProvider != null) {
//            schemaGenerator.withResolverBuilders(globalResolverBuilderExtensionProvider);
//        } else {
//            schemaGenerator.withResolverBuilders(defaultAnnotatedResolverBuilder());
//        }
//
//        if (typeMapperExtensionProvider != null) {
//            schemaGenerator.withTypeMappers(typeMapperExtensionProvider);
//        }
//
//        if (inputConverterExtensionProvider != null) {
//            schemaGenerator.withInputConverters(inputConverterExtensionProvider);
//        }
//
//        if (outputConverterExtensionProvider != null) {
//            schemaGenerator.withOutputConverters(outputConverterExtensionProvider);
//        }
//
//        if (argumentInjectorExtensionProvider != null) {
//            schemaGenerator.withArgumentInjectors(argumentInjectorExtensionProvider);
//        }
//
//        if (schemaTransformerExtensionProvider != null) {
//            schemaGenerator.withSchemaTransformers(schemaTransformerExtensionProvider);
//        }
//
//        if (resolverInterceptorFactoryExtensionProvider != null) {
//            schemaGenerator.withResolverInterceptorFactories(resolverInterceptorFactoryExtensionProvider);
//        }
//
//        if (valueMapperFactory != null) {
//            schemaGenerator.withValueMapperFactory(valueMapperFactory);
//        }
//
//        if (inputFieldBuilderProvider != null) {
//            schemaGenerator.withInputFieldBuilders(inputFieldBuilderProvider);
//        }
//
//        if (typeInfoGenerator != null) {
//            schemaGenerator.withTypeInfoGenerator(typeInfoGenerator);
//        }
//
//        if (spqrProperties.isAbstractInputTypeResolution()) {
//            schemaGenerator.withAbstractInputTypeResolution();
//        }
//
//        if (abstractInputHandler != null) {
//            schemaGenerator.withAbstractInputHandler(abstractInputHandler);
//        }
//
//        if (messageBundles != null && !messageBundles.isEmpty()) {
//            schemaGenerator.withStringInterpolation(messageBundles.toArray(new MessageBundle[0]));
//        }
//
//        if (inclusionStrategy != null) {
//            schemaGenerator.withInclusionStrategy(inclusionStrategy);
//        }
//
//        if (interfaceMappingStrategy != null) {
//            schemaGenerator.withInterfaceMappingStrategy(interfaceMappingStrategy);
//        }

//        if (spqrProperties.getRelay().isConnectionCheckRelaxed()) {
//            schemaGenerator.withRelayConnectionCheckRelaxed();
//        }

        return schemaGenerator;
    }

    protected Map<String, SpqrBean> findGraphQLApiComponents() {
        final String[] apiBeanNames = context.getBeanNamesForAnnotation(GraphQLApi.class);
        final ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();

        Map<String, SpqrBean> result = new HashMap<>();
        for (String beanName : apiBeanNames) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            AnnotatedType beanType;
            Set<WithResolverBuilder> resolverBuilders;
            if (beanDefinition.getSource() instanceof StandardMethodMetadata) {
                StandardMethodMetadata metadata = (StandardMethodMetadata) beanDefinition.getSource();
                beanType = metadata.getIntrospectedMethod().getAnnotatedReturnType();
                resolverBuilders = AnnotatedElementUtils.findMergedRepeatableAnnotations(metadata.getIntrospectedMethod(), WithResolverBuilder.class);
            } else {
                BeanDefinition current = beanDefinition;
                BeanDefinition originatingBeanDefinition = current;
                while (current != null) {
                    originatingBeanDefinition = current;
                    current = current.getOriginatingBeanDefinition();
                }
                ResolvableType resolvableType = originatingBeanDefinition.getResolvableType();
                if (resolvableType != ResolvableType.NONE && Utils.isNotEmpty(originatingBeanDefinition.getBeanClassName())
                        //Sanity check only -- should never happen
                        && !originatingBeanDefinition.getBeanClassName().startsWith("org.springframework.")) {
                    beanType = GenericTypeReflector.annotate(resolvableType.getType());
                } else {
                    beanType = GenericTypeReflector.annotate(AopUtils.getTargetClass(context.getBean(beanName)));
                }
                resolverBuilders = AnnotatedElementUtils.findMergedRepeatableAnnotations(beanType, WithResolverBuilder.class);
            }
            List<ResolverBuilderBeanCriteria> builders = resolverBuilders.stream()
                    .map(builder -> new ResolverBuilderBeanCriteria(builder.value(), builder.qualifierValue(), builder.qualifierType()))
                    .collect(Collectors.toList());
            result.put(beanName, new SpqrBean(context, beanName, beanType, builders));
        }
        return result;
    }

    protected void addOperationSources(SpqrSchemaGenerator schemaGenerator, Collection<SpqrBean> spqrBeans) {
        spqrBeans.forEach(spqrBean ->
                schemaGenerator.withOperationsFromBean(
                        spqrBean.beanSupplier,
                        spqrBean.type,
                        spqrBean.exposedType,
                        spqrBean.resolverBuilders.stream()
                                .map(criteria -> findQualifiedBeanByType(
                                        criteria.getResolverType(),
                                        criteria.getValue(),
                                        criteria.getQualifierType()))
                                .peek(resolverBuilder -> {
                                    if (resolverBuilder instanceof AbstractResolverBuilder) {
                                        ((AbstractResolverBuilder) resolverBuilder).withMethodInvokerFactory(aopAwareFactory);
                                    }
                                })
                                .toArray(ResolverBuilder[]::new)
                )
        );
    }

    protected <T> T findQualifiedBeanByType(Class<? extends T> type, String qualifierValue, Class<? extends Annotation> qualifierType) {
        final NoSuchBeanDefinitionException noSuchBeanDefinitionException = new NoSuchBeanDefinitionException(qualifierValue, "No matching " + type.getSimpleName() +
                " bean found for qualifier " + qualifierValue + " of type " + qualifierType.getSimpleName() + " !");
        try {
            if (Utils.isEmpty(qualifierValue)) {
                if (qualifierType.equals(Qualifier.class)) {
                    return Optional.of(
                            context.getBean(type))
                            .orElseThrow(() -> noSuchBeanDefinitionException);
                }
                return context.getBean(
                        Arrays.stream(context.getBeanNamesForAnnotation(qualifierType))
                                .filter(beanName -> type.isInstance(context.getBean(beanName)))
                                .findFirst()
                                .orElseThrow(() -> noSuchBeanDefinitionException),
                        type);
            }

            return BeanFactoryAnnotationUtils.qualifiedBeanOfType(context.getBeanFactory(), type, qualifierValue);
        } catch (NoSuchBeanDefinitionException noBeanException) {
            ConfigurableListableBeanFactory factory = context.getBeanFactory();

            for (String name : factory.getBeanDefinitionNames()) {
                BeanDefinition bd = factory.getBeanDefinition(name);

                if (bd.getSource() instanceof StandardMethodMetadata) {
                    StandardMethodMetadata metadata = (StandardMethodMetadata) bd.getSource();

                    if (metadata.getReturnTypeName().equals(type.getName())) {
                        Map<String, Object> attributes = metadata.getAnnotationAttributes(qualifierType.getName());
                        if (null != attributes) {
                            if (qualifierType.equals(Qualifier.class)) {
                                if (qualifierValue.equals(attributes.get("value"))) {
                                    return context.getBean(name, type);
                                }
                            }
                            return context.getBean(name, type);
                        }
                    }
                }
            }

            throw noSuchBeanDefinitionException;
        }
    }

    protected static class SpqrBean {

        final BeanScope scope;
        final Supplier<Object> beanSupplier;
        final AnnotatedType type;
        final Class<?> exposedType;
        final List<ResolverBuilderBeanCriteria> resolverBuilders;

        SpqrBean(ApplicationContext context, String beanName, AnnotatedType type, List<ResolverBuilderBeanCriteria> resolverBuilders) {
            BeanScope beanScope = BeanScope.findBeanScope(context, beanName);
            if (beanScope == BeanScope.SINGLETON) {
                Object bean = context.getBean(beanName);
                this.beanSupplier = () -> bean;
            } else {
                this.beanSupplier = () -> context.getBean(beanName);
            }
            this.scope = beanScope;
            this.type = type;
            this.exposedType = context.getType(beanName);
            this.resolverBuilders = Collections.unmodifiableList(resolverBuilders);
        }
    }

    protected static class ResolverBuilderBeanCriteria {
        protected final Class<? extends ResolverBuilder> resolverType;
        protected final String value;
        protected final Class<? extends Annotation> qualifierType;

        protected ResolverBuilderBeanCriteria(Class<? extends ResolverBuilder> resolverType, String value, Class<? extends Annotation> qualifierType) {
            this.resolverType = resolverType;
            this.value = value;
            this.qualifierType = qualifierType;
        }

        String getValue() {
            return value;
        }

        Class<? extends Annotation> getQualifierType() {
            return qualifierType;
        }

        Class<? extends ResolverBuilder> getResolverType() {
            return resolverType;
        }
    }

    protected enum BeanScope {
        SINGLETON,
        PROTOTYPE,
        UNKNOWN;

        static BeanScope findBeanScope(ApplicationContext context, String beanName) {
            if (context.isSingleton(beanName)) {
                return SINGLETON;
            } else if (context.isPrototype(beanName)) {
                return PROTOTYPE;
            } else {
                return UNKNOWN;
            }
        }
    }

}
