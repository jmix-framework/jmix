package io.jmix.graphql.schema;

import graphql.language.*;
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
    protected EntityMutationDataFetcher entityMutationDataFetcher;
    @Autowired
    protected EntityQueryDataFetcher entityQueryDataFetcher;
    @Autowired
    protected Metadata metadata;

    public GraphQLSchema createSchema() {

        Collection<MetaClass> allPersistentMetaClasses = metadataTools.getAllPersistentMetaClasses();
        TypeDefinitionRegistry typeDefinitionRegistry = new TypeDefinitionRegistry();

        Collection<MetaClass> queryMetaClasses = new ArrayList<>(allPersistentMetaClasses);
        queryMetaClasses.addAll(Arrays.stream(additionalClasses)
                .map(entityName -> metadata.findClass(entityName))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        typeDefinitionRegistry.add(buildQuerySection(queryMetaClasses));
        typeDefinitionRegistry.add(buildMutationSection(queryMetaClasses));

        // system types (filters, conditions e.t.c)
        typeDefinitionRegistry.add(Types.Condition);
        typeDefinitionRegistry.add(Types.GroupCondition);
        typeDefinitionRegistry.add(Types.GroupConditionType);

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
        log.debug("createSchema:\n9 {}", new SchemaPrinter().print(graphQLSchema));
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
                    Class<Object> javaClass = metaClass.getJavaClass();
                    rwBuilder.type(SCHEMA_QUERY, typeWiring -> typeWiring
                            .dataFetcher(NamingUtils.composeListQueryName(javaClass), entityQueryDataFetcher.loadEntities(metaClass))
                            .dataFetcher(NamingUtils.composeByIdQueryName(javaClass), entityQueryDataFetcher.loadEntity(metaClass))
                            .dataFetcher(NamingUtils.composeCountQueryName(javaClass), entityQueryDataFetcher.countEntities(metaClass)));

                    rwBuilder.type(SCHEMA_MUTATION, typeWiring -> typeWiring
                            .dataFetcher(NamingUtils.composeUpsertMutationName(javaClass), entityMutationDataFetcher.upsertEntity(metaClass))
                    );

                    rwBuilder.type(SCHEMA_MUTATION, typeWiring -> typeWiring
                            .dataFetcher(NamingUtils.composeDeleteMutationName(javaClass), entityMutationDataFetcher.deleteEntity(metaClass))
                    );
                });
        return rwBuilder;
    }

    protected ObjectTypeDefinition buildQuerySection(Collection<MetaClass> metaClasses) {
        List<FieldDefinition> fields = new ArrayList<>();

        // todo filter persistent only

        metaClasses.forEach(metaClass -> {
            Class<Object> javaClass = metaClass.getJavaClass();
            String typeName = NamingUtils.normalizeOutTypeName(metaClass.getName());

            // query 'cars(filter, limit, offset, sortBy, sortOrder)'
            fields.add(
                    FieldDefinition.newFieldDefinition()
                            .name(NamingUtils.composeListQueryName(javaClass))
                            .type(ListType.newListType(new TypeName(typeName)).build())

                            .inputValueDefinition(new InputValueDefinition(
                                    NamingUtils.FILTER, new TypeName(Types.GroupCondition.getName())))
                            .inputValueDefinition(arg(NamingUtils.LIMIT, "Int"))
                            .inputValueDefinition(arg(NamingUtils.OFFSET, "Int"))
                            .inputValueDefinition(arg(NamingUtils.SORT, "String"))
                            .build());

            // query 'carById(id)'
            fields.add(
                    FieldDefinition.newFieldDefinition()
                            .name(NamingUtils.composeByIdQueryName(javaClass))
                            .type(new TypeName(typeName))
                            // todo we need to define 'id' arg type depends on entity id type
                            .inputValueDefinition(argNonNull("id", "String"))
                            .build());

            // query 'countCars()'
            fields.add(
                    FieldDefinition.newFieldDefinition()
                            .name(NamingUtils.composeCountQueryName(javaClass))
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

            // mutation upsertCar(car: scr_Car)
            fields.add(
                    FieldDefinition.newFieldDefinition()
                            .name(NamingUtils.composeUpsertMutationName(javaClass))
                            .type(new TypeName(outTypeName))
                            .inputValueDefinition(argNonNull(NamingUtils.uncapitalizedSimpleName(javaClass), inpTypeName))
                            .build());

            // mutation deleteCar(id: UUID)
            fields.add(
                    FieldDefinition.newFieldDefinition()
                            .name(NamingUtils.composeDeleteMutationName(javaClass))
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
     * @return argument
     */
    private static InputValueDefinition arg(String name, String type) {
        return InputValueDefinition.newInputValueDefinition()
                .name(name).type(new TypeName(type)).build();
    }

    /**
     * Shortcut for not null query argument builder
     *
     * @param name argument name
     * @param type argument type
     * @return argument
     */
    private static InputValueDefinition argNonNull(String name, String type) {
        return InputValueDefinition.newInputValueDefinition()
                .name(name).type(NonNullType.newNonNullType(new TypeName(type)).build())
                .build();
    }

}
