package io.jmix.graphql.schema;

import graphql.Scalars;
import graphql.language.*;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaPrinter;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.graphql.datafetcher.EntityMutationDataFetcher;
import io.jmix.graphql.datafetcher.EntityQueryDataFetcher;
import io.jmix.graphql.schema.scalar.CustomScalars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

@Component
public class SchemaBuilder {

    private static final Logger log = LoggerFactory.getLogger(SchemaBuilder.class);

    public static final String SCHEMA_QUERY = "Query";
    public static final String SCHEMA_MUTATION = "Mutation";

    @Value("${io.jmix.graphql.additionalClasses:[]}")
    private String[] additionalClasses;

    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected OutTypesBuilder outTypesBuilder;
    @Autowired
    protected InpTypesBuilder inpTypesBuilder;
    @Autowired
    protected FilterTypesBuilder filterTypesBuilder;
    @Autowired
    protected EntityMutationDataFetcher entityMutationDataFetcher;
    @Autowired
    protected EntityQueryDataFetcher entityQueryDataFetcher;
    @Autowired
    protected Metadata metadata;

    public GraphQLSchema createSchema() {

        Collection<MetaClass> allPersistentMetaClasses = metadataTools.getAllJpaEntityMetaClasses()
                // todo need to be fixed later - ReferenceToEntity is not persistent but returned in 'metadataTools.getAllPersistentMetaClasses'
                .stream()
                .filter(metaClass -> !metaClass.getJavaClass().getSimpleName().equals("ReferenceToEntity"))
                .collect(Collectors.toList());

        TypeDefinitionRegistry typeDefinitionRegistry = new TypeDefinitionRegistry();

        Collection<MetaClass> queryMetaClasses = new ArrayList<>(allPersistentMetaClasses);
        // todo Dto classes temporary switched off for queries
//        queryMetaClasses
//                .addAll(Arrays.stream(additionalClasses)
//                        .map(entityName -> metadata.findClass(entityName))
//                        .filter(Objects::nonNull)
//                        .collect(Collectors.toList()));

        typeDefinitionRegistry.add(buildQuerySection(queryMetaClasses));
        typeDefinitionRegistry.add(buildMutationSection(queryMetaClasses));

        // jmix custom scalars
        typeDefinitionRegistry.add(ScalarTypeDefinition.newScalarTypeDefinition()
                .name(CustomScalars.GraphQLUUID.getName())
                .build());
        typeDefinitionRegistry.add(ScalarTypeDefinition.newScalarTypeDefinition()
                .name(CustomScalars.GraphQLDate.getName())
                .build());
        typeDefinitionRegistry.add(ScalarTypeDefinition.newScalarTypeDefinition()
                .name(CustomScalars.GraphQLLocalDateTime.getName())
                .build());
        typeDefinitionRegistry.add(ScalarTypeDefinition.newScalarTypeDefinition()
                .name(CustomScalars.GraphQLVoid.getName())
                .build());

        // find enums used in properties and create type definitions for enums
        allPersistentMetaClasses.stream()
                .filter(this::isNotIgnored)
                .flatMap(metaClass -> metaClass.getProperties().stream())
                .filter(metaProperty -> metaProperty.getType() == MetaProperty.Type.ENUM)
                .distinct()
                .forEach(metaProperty -> typeDefinitionRegistry.add(BaseTypesBuilder.buildEnumTypeDef(metaProperty.getJavaType())));

        // output type definitions for jmix entities
        allPersistentMetaClasses.stream()
                .filter(this::isNotIgnored)
                .forEach(metaClass -> typeDefinitionRegistry.add(outTypesBuilder.buildObjectTypeDef(metaClass)));

        // input type definitions for jmix entities
        allPersistentMetaClasses.stream()
                .filter(this::isNotIgnored)
                .forEach(metaClass -> typeDefinitionRegistry.add(inpTypesBuilder.buildObjectTypeDef(metaClass)));

        /* Filter types */

        // filter type definitions for jmix entities
        allPersistentMetaClasses.stream()
                .filter(this::isNotIgnored)
                .forEach(metaClass -> typeDefinitionRegistry.add(filterTypesBuilder.buildFilterConditionType(metaClass)));

        // scalar filter conditions
        Map<String, InputObjectTypeDefinition> scalarFilterConditionTypesMap = Arrays.stream(Types.scalars)
                    // todo byte type not supported now
                    .filter(type -> !type.getName().equals(Scalars.GraphQLByte.getName()))
                    .collect(Collectors.toMap(GraphQLScalarType::getName, type -> filterTypesBuilder.buildScalarFilterConditionType(type.getName())));
        scalarFilterConditionTypesMap.values().forEach(typeDefinitionRegistry::add);

        // order by
        typeDefinitionRegistry.add(Types.enumSortOrder);

        // filter order by types
        allPersistentMetaClasses.stream()
                .filter(this::isNotIgnored)
                .forEach(metaClass -> typeDefinitionRegistry.add(filterTypesBuilder.buildFilterOrderByType(metaClass)));

        // todo need to be reimplemented more correctly
        // additional (now it's non persistent) types
        Arrays.stream(additionalClasses).forEach(entityName -> {
            MetaClass metaClass = metadata.findClass(entityName);
            if (metaClass != null) {
                log.debug("createSchema: build additional types for entity name {}", entityName);
                typeDefinitionRegistry.add(inpTypesBuilder.buildObjectTypeDef(metaClass));
                typeDefinitionRegistry.add(outTypesBuilder.buildObjectTypeDef(metaClass));
            } else {
                log.warn("createSchema: can't find meta class for entity name {}", entityName);
            }
        });

        GraphQLSchema graphQLSchema = new SchemaGenerator()
                .makeExecutableSchema(typeDefinitionRegistry, buildRuntimeWiring(allPersistentMetaClasses).build());
        // schema could be downloaded via 'graphqurl', not need in log
        log.trace("createSchema:\n {}", new SchemaPrinter().print(graphQLSchema));
        return graphQLSchema;
    }

    protected RuntimeWiring.Builder buildRuntimeWiring(Collection<MetaClass> allPersistentMetaClasses) {
        RuntimeWiring.Builder rwBuilder = newRuntimeWiring()
                .scalar(CustomScalars.GraphQLUUID)
                .scalar(CustomScalars.GraphQLLong)
                .scalar(CustomScalars.GraphQLBigDecimal)
                .scalar(CustomScalars.GraphQLDate)
                .scalar(CustomScalars.GraphQLLocalDateTime)
                .scalar(CustomScalars.GraphQLVoid);

        allPersistentMetaClasses.stream()
                .filter(this::isNotIgnored)
                // todo filter persistent
                .forEach(metaClass -> {
                    rwBuilder.type(SCHEMA_QUERY, typeWiring -> typeWiring
                            .dataFetcher(NamingUtils.composeListQueryName(metaClass), entityQueryDataFetcher.loadEntities(metaClass))
                            .dataFetcher(NamingUtils.composeByIdQueryName(metaClass), entityQueryDataFetcher.loadEntity(metaClass))
                            .dataFetcher(NamingUtils.composeCountQueryName(metaClass), entityQueryDataFetcher.countEntities(metaClass)));

                    rwBuilder.type(SCHEMA_MUTATION, typeWiring -> typeWiring
                            .dataFetcher(NamingUtils.composeUpsertMutationName(metaClass), entityMutationDataFetcher.upsertEntity(metaClass))
                    );

                    rwBuilder.type(SCHEMA_MUTATION, typeWiring -> typeWiring
                            .dataFetcher(NamingUtils.composeDeleteMutationName(metaClass), entityMutationDataFetcher.deleteEntity(metaClass))
                    );
                });
        return rwBuilder;
    }

    protected ObjectTypeDefinition buildQuerySection(Collection<MetaClass> metaClasses) {
        List<FieldDefinition> fields = new ArrayList<>();

        // todo filter persistent only

        metaClasses.forEach(metaClass -> {
            String typeName = NamingUtils.normalizeOutTypeName(metaClass.getName());

            // query 'scr_CarList(filter, limit, offset, orderBy)'
            String filterDesc = String.format(
                    "expressions to compare %s objects, all items are combined with logical 'AND'", typeName);
            fields.add(
                    FieldDefinition.newFieldDefinition()
                            .name(NamingUtils.composeListQueryName(metaClass))
                            .type(ListType.newListType(new TypeName(typeName)).build())
                            .inputValueDefinition(listArg(NamingUtils.FILTER,
                                    filterTypesBuilder.composeFilterConditionTypeName(metaClass),
                                    filterDesc))
                            .inputValueDefinition(arg(NamingUtils.LIMIT, "Int", "limit the number of items returned"))
                            .inputValueDefinition(arg(NamingUtils.OFFSET, "Int", "skip the first n items"))
                            // todo array in order by, add ability to order by nested objects
                            .inputValueDefinition(arg(NamingUtils.ORDER_BY,
                                    filterTypesBuilder.composeFilterOrderByTypeName(metaClass),
                                    "sort the items by one or more fields"))
                            .build());

            // query 'scr_CarById(id)'
            fields.add(
                    FieldDefinition.newFieldDefinition()
                            .name(NamingUtils.composeByIdQueryName(metaClass))
                            .type(new TypeName(typeName))
                            // todo we need to define 'id' arg type depends on entity id type
                            .inputValueDefinition(argNonNull("id", "String"))
                            .build());

            // query 'scr_CarCount()'
            fields.add(
                    FieldDefinition.newFieldDefinition()
                            .name(NamingUtils.composeCountQueryName(metaClass))
                            .type(new TypeName(CustomScalars.GraphQLLong.getName()))
                            .build());

        });

        return ObjectTypeDefinition.newObjectTypeDefinition()
                .name(SCHEMA_QUERY)
                .fieldDefinitions(fields).build();
    }

    ObjectTypeDefinition buildMutationSection(Collection<MetaClass> metaClasses) {
        List<FieldDefinition> fields = new ArrayList<>();

        // todo filter persistent only

        metaClasses.forEach(metaClass -> {
            Class<Object> javaClass = metaClass.getJavaClass();
            String outTypeName = NamingUtils.normalizeOutTypeName(metaClass.getName());
            String inpTypeName = NamingUtils.normalizeInpTypeName(metaClass.getName());

            // mutation upsert_scr_Car(car: scr_Car)
            fields.add(
                    FieldDefinition.newFieldDefinition()
                            .name(NamingUtils.composeUpsertMutationName(metaClass))
                            .type(new TypeName(outTypeName))
                            .inputValueDefinition(argNonNull(NamingUtils.uncapitalizedSimpleName(javaClass), inpTypeName))
                            .build());

            // mutation delete_scr_Car(id: UUID)
            fields.add(
                    FieldDefinition.newFieldDefinition()
                            .name(NamingUtils.composeDeleteMutationName(metaClass))
                            .type(new TypeName(CustomScalars.GraphQLVoid.getName()))
                            // todo we need to define 'id' arg type depends on entity id type
                            .inputValueDefinition(argNonNull("id", "String"))
                            .build());
        });

        return ObjectTypeDefinition.newObjectTypeDefinition()
                .name(SCHEMA_MUTATION)
                .fieldDefinitions(fields).build();
    }

    protected boolean isNotIgnored(MetaClass metaClass) {
        return true;
    }

    /**
     * Shortcut for query argument builder
     *
     * @param name argument name
     * @param type argument type
     * @param description argument description
     * @return argument
     */
    protected static InputValueDefinition arg(String name, String type, @Nullable String description) {
        return InputValueDefinition.newInputValueDefinition()
                .name(name).type(new TypeName(type))
                .description(new Description(description, null, false))
                .build();
    }

    /**
     * Shortcut for query argument builder (list type argument)
     *
     * @param name argument name
     * @param type argument type
     * @param description argument description
     * @return argument
     */
    protected static InputValueDefinition listArg(String name, String type, @Nullable String description) {
        return InputValueDefinition.newInputValueDefinition()
                .name(name).type(new ListType(new TypeName(type)))
                .description(new Description(description, null, false))
                .build();
    }

    /**
     * Shortcut for not null query argument builder
     *
     * @param name argument name
     * @param type argument type
     * @return argument
     */
    protected static InputValueDefinition argNonNull(String name, String type) {
        return InputValueDefinition.newInputValueDefinition()
                .name(name).type(NonNullType.newNonNullType(new TypeName(type)).build())
                .build();
    }

}
