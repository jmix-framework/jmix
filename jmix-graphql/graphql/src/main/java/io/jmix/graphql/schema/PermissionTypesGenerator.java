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
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import io.jmix.graphql.NamingUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static io.jmix.graphql.schema.BaseTypesGenerator.listType;

@Component("gql_PermissionTypesGenerator")
public class PermissionTypesGenerator {

    public List<GraphQLType> generatePermissionTypes() {
        List<GraphQLType> permissionTypes = new ArrayList<>();

        permissionTypes.add(GraphQLObjectType.newObject()
                .name(NamingUtils.TYPE_SEC_PERMISSION)
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("target")
                        .type(new GraphQLTypeReference(Scalars.GraphQLString.getName()))
                        .build())
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("value")
                        .type(new GraphQLTypeReference(Scalars.GraphQLInt.getName()))
                        .build())
                .build());

        permissionTypes.add(GraphQLObjectType.newObject()
                .name(NamingUtils.TYPE_SEC_PERMISSION_CONFIG)
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name(NamingUtils.ENTITIES)
                        .type(listType(NamingUtils.TYPE_SEC_PERMISSION))
                        .build())
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name(NamingUtils.ENTITY_ATTRS)
                        .type(listType(NamingUtils.TYPE_SEC_PERMISSION))
                        .build())
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name(NamingUtils.SPECIFICS)
                        .type(listType(NamingUtils.TYPE_SEC_PERMISSION))
                        .build())
                .build());
        return permissionTypes;
    }

}
