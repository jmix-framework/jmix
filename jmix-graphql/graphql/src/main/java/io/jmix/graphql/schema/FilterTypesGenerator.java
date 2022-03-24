/*
 * Copyright 2021 Haulmont.
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

package io.jmix.graphql.schema;

import graphql.Scalars;
import graphql.schema.*;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.graphql.GraphQlProperties;
import io.jmix.graphql.MetadataUtils;
import io.jmix.graphql.NamingUtils;
import io.jmix.graphql.schema.scalar.CustomScalars;
import io.jmix.graphql.schema.scalar.ScalarTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static graphql.Scalars.*;
import static io.jmix.graphql.NamingUtils.INPUT_TYPE_PREFIX;
import static io.jmix.graphql.NamingUtils.SYS_ATTR_INSTANCE_NAME;
import static io.jmix.graphql.schema.BaseTypesGenerator.*;
import static io.jmix.graphql.schema.Types.FilterOperation.*;
import static io.jmix.graphql.schema.scalar.CustomScalars.GraphQLUUID;

@Component("gql_FilterTypesGenerator")
public class FilterTypesGenerator {

    protected static final GraphQLEnumType enumSortOrder = EnumTypesGenerator.generateEnumType(Types.SortOrder.class);

    @Autowired
    MetadataUtils metadataUtils;
    @Autowired
    MetadataTools metadataTools;
    @Autowired
    private BaseTypesGenerator baseTypesGenerator;
    @Autowired
    ScalarTypes scalarTypes;
    @Autowired
    protected DatatypeRegistry datatypes;
    @Autowired
    protected GraphQlProperties graphQlProperties;

    public Collection<GraphQLType> generateFilterTypes() {
        Collection<GraphQLType> types = new ArrayList<>();

        /* Filter types */

        // filter type definitions for jmix entities
        List<MetaClass> allPersistentMetaClasses = metadataUtils.allSupportedMetaClasses();

        allPersistentMetaClasses
                .forEach(metaClass -> types.add(generateFilterConditionType(metaClass)));

        // scalar filter conditions
        Map<String, GraphQLInputObjectType> scalarFilterConditionTypesMap =
                scalarTypes.scalars().stream()
                // todo byte type not supported now
                //GraphQLVoid excluded because no need to filter such scalar type
                .filter(type -> !type.getName().equals(Scalars.GraphQLByte.getName())
                        && !type.getName().equals(CustomScalars.GraphQLVoid.getName()))
                .collect(Collectors.toMap(GraphQLScalarType::getName, this::generateScalarFilterConditionType));
        types.addAll(scalarFilterConditionTypesMap.values());

        // enum filter conditions
        types.addAll(generateEnumFilterTypes());

        // order by
        types.add(enumSortOrder);

        // filter order by classes types
        allPersistentMetaClasses.forEach(metaClass -> types.add(generateFilterOrderByType(metaClass)));
        return types;
    }

    protected GraphQLInputObjectType generateFilterConditionType(MetaClass metaClass) {

        String className = composeFilterConditionTypeName(metaClass);

        GraphQLInputObjectType.Builder builder = GraphQLInputObjectType.newInputObject()
                .name(className);

        List<GraphQLInputObjectField> valueDefs = metaClass.getProperties().stream()
                .map(metaProperty -> {

                    if (metaProperty.getType().equals(MetaProperty.Type.ENUM)) {
                        return listInpObjectField(metaProperty.getName(),
                                composeFilterConditionTypeName(metaProperty.getJavaType().getSimpleName()), null);
                    }

                    if (metaProperty.getRange().getCardinality().isMany()) {
                        String typeName = composeFilterConditionTypeName(baseTypesGenerator.getFieldTypeName(metaProperty));
                        return listInpObjectField(metaProperty.getName(), typeName, null);
                    }

                    String typeName = composeFilterConditionTypeName(baseTypesGenerator.getFieldTypeName(metaProperty));
                    return listInpObjectField(metaProperty.getName(), typeName, null);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // todo description
        valueDefs.add(listInpObjectField(Types.ConditionUnionType.AND.name(), className, null));
//        valueDefs.add(listValueDef(ConditionUnionType.NOT.name(), className, null));
        valueDefs.add(listInpObjectField(Types.ConditionUnionType.OR.name(), className, null));
        //condition for filtering by null references
        valueDefs.add(inpObjectField(IS_NULL.getId(), GraphQLBoolean.getName(), IS_NULL.getDescription()));

        builder.fields(valueDefs);

//        log.debug("buildFilterConditionType: /for class {}", metaClass);
        return builder.build();
    }

    protected GraphQLInputObjectType generateFilterOrderByType(MetaClass metaClass) {

        String className = composeFilterOrderByTypeName(metaClass);

        GraphQLInputObjectType.Builder builder = GraphQLInputObjectType.newInputObject()
                .name(className);

        List<GraphQLInputObjectField> fields = metaClass.getProperties().stream()
                .map(metaProperty -> {

                    if (metaProperty.getType().equals(MetaProperty.Type.ENUM)) {
                        return inpObjectField(metaProperty.getName(), Types.SortOrder.class.getSimpleName(), null);
                    }

                    // todo "-to-many" relations are not supported now
                    if (metaProperty.getRange().getCardinality().isMany()) {
                        return null;
                    }

                    if (metaProperty.getJavaType().getSimpleName().equals("String")) {
                        return inpObjectField(metaProperty.getName(), enumSortOrder.getName(), null);
                    }

                    if (metaProperty.getRange().isClass()) {
                        // todo now we support only persistent entities
                        if (!metadataTools.isJpaEntity(metaProperty.getJavaType())) {
                            return null;
                        }
                        String typeName = composeFilterOrderByTypeName(baseTypesGenerator.getFieldTypeName(metaProperty));
                        return graphQlProperties.isMultipleSortSupported()?
                                listInpObjectField(metaProperty.getName(), typeName, null):
                                inpObjectField(metaProperty.getName(), typeName, null);
                    }

                    // datatype attributes
                    return inpObjectField(metaProperty.getName(), Types.SortOrder.class.getSimpleName(), null);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        fields.add(inpObjectField(SYS_ATTR_INSTANCE_NAME, Types.SortOrder.class.getSimpleName(), null));
        builder.fields(fields);

//        log.debug("buildFilterOrderByType: for class {}", metaClass);
        return builder.build();
    }


    // todo operators to constants or enums
    protected GraphQLInputObjectType generateScalarFilterConditionType(GraphQLScalarType scalarType) {

        String scalarTypeName = scalarType.getName();
        String comment = String.format(
                "expression to compare columns of type %s. All fields are combined with logical 'AND'", scalarTypeName);

        String name = composeFilterConditionTypeName(scalarTypeName);

        EnumSet<Types.FilterOperation> availableOperations = availableOperations(scalarType);

        GraphQLInputObjectType.Builder defBuilder = GraphQLInputObjectType.newInputObject()
                .name(name)
                .description(comment);

        availableOperations.forEach(operation -> {
            if (IN_LIST.equals(operation) || NOT_IN_LIST.equals(operation)) {
                defBuilder.field(listInpObjectField(operation.getId(), scalarTypeName, operation.getDescription()));
            } else if (IS_NULL.equals(operation)) {
                defBuilder.field(inpObjectField(operation.getId(), GraphQLBoolean.getName(), operation.getDescription()));
            } else {
                defBuilder.field(inpObjectField(operation.getId(), scalarTypeName, operation.getDescription()));
            }
        });

        return defBuilder.build();
    }

    public Collection<GraphQLType> generateEnumFilterTypes() {
        return metadataUtils.allEnumJavaClasses().stream()
                .map(FilterTypesGenerator::generateEnumFilterType)
                .collect(Collectors.toList());
    }

    public static GraphQLInputObjectType generateEnumFilterType(Class<?> javaType)  {
        String className = composeFilterConditionTypeName(javaType.getSimpleName());
        String enumTypeName = javaType.getSimpleName();

        return GraphQLInputObjectType.newInputObject()
                .name(className)
                .field(listInpObjectField(IN_LIST.getId(), enumTypeName, IN_LIST.getDescription()))
                .field(listInpObjectField(NOT_IN_LIST.getId(), enumTypeName, NOT_IN_LIST.getDescription()))
                .field(inpObjectField(EQ.getId(), enumTypeName, EQ.getDescription()))
                .field(inpObjectField(NEQ.getId(), enumTypeName, NEQ.getDescription()))
                .field(inpObjectField(IS_NULL.getId(), GraphQLBoolean.getName(), IS_NULL.getDescription()))
                .field(listInpObjectField(Types.ConditionUnionType.AND.name(), className, null))
                .field(listInpObjectField(Types.ConditionUnionType.OR.name(), className, null))
                .build();
    }


    protected static String composeFilterOrderByTypeName(MetaClass metaClass) {
        return composeFilterOrderByTypeName(metaClass.getName());
    }

    protected static String composeFilterOrderByTypeName(String name) {
        return composeFilterTypeName(name, "OrderBy");
    }

    protected static String composeFilterConditionTypeName(MetaClass metaClass) {
        return composeFilterTypeName(metaClass.getName(), "FilterCondition");
    }

    protected static String composeFilterConditionTypeName(String name) {
        return composeFilterTypeName(name, "FilterCondition");
    }

    protected static String composeFilterTypeName(String name, String suffix) {
        // verify that name is normalized
        if (!name.startsWith(INPUT_TYPE_PREFIX)) {
            name = NamingUtils.normalizeInpTypeName(name);
        }
        return name + suffix;
    }

    public EnumSet<Types.FilterOperation> availableOperations(GraphQLScalarType scalarType) {

        if (scalarType.equals(GraphQLUUID)) {
            return EnumSet.of(EQ, NEQ, IN_LIST, NOT_IN_LIST, IS_NULL);
        }
        if (Types.numberTypes.contains(scalarType)) {
            return EnumSet.of(EQ, NEQ, GT, GTE, LT, LTE, IN_LIST, NOT_IN_LIST, IS_NULL);
        }
        if (scalarType.equals(GraphQLString)) {
            return EnumSet.of(EQ, NEQ, IN_LIST, NOT_IN_LIST, STARTS_WITH, ENDS_WITH, CONTAINS, NOT_CONTAINS, IS_NULL);
        }
        if (scalarType.equals(GraphQLChar)) {
            return EnumSet.of(EQ, NEQ, IN_LIST, NOT_IN_LIST, IS_NULL);
        }
        if (Types.dateTimeTypes.contains(scalarType)) {
            return EnumSet.of(EQ, NEQ, IN_LIST, NOT_IN_LIST, GT, GTE, LT, LTE, IS_NULL);
        }
        if (scalarTypes.isTimeType(scalarType)) {
            return EnumSet.of(EQ, NEQ, GT, GTE, LT, LTE, IS_NULL);
        }
        if (scalarType.equals(GraphQLBoolean)) {
            return EnumSet.of(EQ, NEQ, IS_NULL);
        }
        if (scalarTypes.isFileRefType(scalarType)) {
            return EnumSet.of(EQ, NEQ, IN_LIST, NOT_IN_LIST, IS_NULL);
        }

        throw new UnsupportedOperationException("Can't define the operation type for " + scalarType);
    }


}
