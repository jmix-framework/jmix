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

import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLTypeReference;
import io.jmix.graphql.MetadataUtils;
import io.jmix.graphql.NamingUtils;
import io.jmix.graphql.datafetcher.EntityMutationDataFetcher;
import io.jmix.graphql.datafetcher.EntityQueryDataFetcher;
import io.jmix.graphql.schema.scalar.CustomScalars;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static io.jmix.graphql.schema.BaseTypesGenerator.*;

@Component("gql_GenericSchemaGenerator")
public class GenericSchemaGenerator {

    @Autowired
    private MetadataUtils metadataUtils;
    @Autowired
    private EntityQueryDataFetcher entityQueryDataFetcher;
    @Autowired
    private EntityMutationDataFetcher entityMutationDataFetcher;

    public List<GraphQLFieldDefinition> generateQueryFields() {

        List<GraphQLFieldDefinition> fields = new ArrayList<>();

        // todo filter persistent only
        metadataUtils.allSupportedMetaClasses().forEach(metaClass -> {
            String typeName = NamingUtils.normalizeName(metaClass.getName());

            // query 'scr_CarList(filter, limit, offset, orderBy)'
            String filterDesc = String.format(
                    "expressions to compare %s objects, all items are combined with logical 'AND'", typeName);
            fields.add(
                    GraphQLFieldDefinition.newFieldDefinition()
                            .name(NamingUtils.composeListQueryName(metaClass))
                            .type(listType(typeName))
                            .argument(listArg(NamingUtils.FILTER,
                                    FilterTypesGenerator.composeFilterConditionTypeName(metaClass),
                                    filterDesc))
                            .argument(arg(NamingUtils.LIMIT, "Int", "limit the number of items returned"))
                            .argument((arg(NamingUtils.OFFSET, "Int", "skip the first n items")))
                            // todo array in order by, add ability to order by nested objects
                            .argument(arg(NamingUtils.ORDER_BY, FilterTypesGenerator.composeFilterOrderByTypeName(metaClass),
                                    "sort the items by one or more fields"))
                            .argument(arg(NamingUtils.SOFT_DELETION, "Boolean", "set false to load soft-deleted entities"))
                            .build());

            // query 'scr_CarById(id: UUID!)'
            fields.add(
                    GraphQLFieldDefinition.newFieldDefinition()
                            .name(NamingUtils.composeByIdQueryName(metaClass))
                            .type(new GraphQLTypeReference(typeName))
                            // todo we need to define 'id' arg type depends on entity id type
                            .argument(argNonNull("id", "String"))
                            .argument(arg(NamingUtils.SOFT_DELETION, "Boolean", "set false to load soft-deleted entity"))
                            .build());

            // query 'scr_CarCount(filter)'
            fields.add(
                    GraphQLFieldDefinition.newFieldDefinition()
                            .name(NamingUtils.composeCountQueryName(metaClass))
                            .type(new GraphQLTypeReference(CustomScalars.GraphQLLong.getName()))
                            .argument(listArg(NamingUtils.FILTER,
                                    FilterTypesGenerator.composeFilterConditionTypeName(metaClass),
                                    filterDesc))
                            .argument(arg(NamingUtils.SOFT_DELETION, "Boolean", "set false to include soft-deleted entities"))
                            .build());

        });

        // custom query for permissions
        fields.add(
                GraphQLFieldDefinition.newFieldDefinition()
                        .name(NamingUtils.QUERY_PERMISSIONS)
                        .type(new GraphQLTypeReference(NamingUtils.TYPE_SEC_PERMISSION_CONFIG))
                        .build());

        // custom query for entities messages
        fields.add(
                GraphQLFieldDefinition.newFieldDefinition()
                        .name(NamingUtils.QUERY_ENTITY_MESSAGES)
                        .type(listType(NamingUtils.TYPE_GQL_MESSAGE_DETAIL))
                        .argument(arg("className", "String", null))
                        .argument(arg("locale", "String", null))
                        .build());

        // custom query for enum messages
        fields.add(
                GraphQLFieldDefinition.newFieldDefinition()
                        .name(NamingUtils.QUERY_ENUM_MESSAGES)
                        .type(listType(NamingUtils.TYPE_GQL_MESSAGE_DETAIL))
                        .argument(arg("className", "String", null))
                        .argument(arg("locale", "String", null))
                        .build());


        return fields;
    }

    public List<GraphQLFieldDefinition> generateMutationFields() {
        List<GraphQLFieldDefinition> fields = new ArrayList<>();

        metadataUtils.allSupportedMetaClasses().forEach(metaClass -> {
            Class<Object> javaClass = metaClass.getJavaClass();
            String outTypeName = NamingUtils.normalizeName(metaClass.getName());
            String inpTypeName = NamingUtils.normalizeInpTypeName(metaClass.getName());

            // mutation upsert_scr_Car(car: scr_Car!)
            fields.add(
                    GraphQLFieldDefinition.newFieldDefinition()
                            .name(NamingUtils.composeUpsertMutationName(metaClass))
                            .type(new GraphQLTypeReference(outTypeName))
                            .argument(argNonNull(NamingUtils.uncapitalizedSimpleName(javaClass), inpTypeName))
                            .build());

            // mutation delete_scr_Car(id: UUID!)
            fields.add(
                    GraphQLFieldDefinition.newFieldDefinition()
                            .name(NamingUtils.composeDeleteMutationName(metaClass))
                            .type(new GraphQLTypeReference(CustomScalars.GraphQLVoid.getName()))
                            // todo we need to define 'id' arg type depends on entity id type
                            .argument(argNonNull("id", "String"))
                            .build());
        });

        return fields;
    }

    public void assignDataFetchers(GraphQLCodeRegistry.Builder codeRegistryBuilder) {
        metadataUtils.allSupportedMetaClasses().forEach(metaClass -> {
            codeRegistryBuilder.dataFetcher(
                    FieldCoordinates.coordinates("Query", NamingUtils.composeListQueryName(metaClass)),
                    entityQueryDataFetcher.loadEntities(metaClass));
            codeRegistryBuilder.dataFetcher(
                    FieldCoordinates.coordinates("Query", NamingUtils.composeCountQueryName(metaClass)),
                    entityQueryDataFetcher.countEntities(metaClass));
            codeRegistryBuilder.dataFetcher(
                    FieldCoordinates.coordinates("Query", NamingUtils.composeByIdQueryName(metaClass)),
                    entityQueryDataFetcher.loadEntity(metaClass));

            codeRegistryBuilder.dataFetcher(
                    FieldCoordinates.coordinates("Mutation", NamingUtils.composeUpsertMutationName(metaClass)),
                    entityMutationDataFetcher.upsertEntity(metaClass));
            codeRegistryBuilder.dataFetcher(
                    FieldCoordinates.coordinates("Mutation", NamingUtils.composeDeleteMutationName(metaClass)),
                    entityMutationDataFetcher.deleteEntity(metaClass));
        });
    }
}
