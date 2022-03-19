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

@Component("gql_MessageTypesGenerator")
public class MessageTypesGenerator {

    @SuppressWarnings("rawtypes")
    public List<GraphQLType> generateMessageTypes() {
        List<GraphQLType> messagesTypes = new ArrayList<>();

        messagesTypes.add(GraphQLObjectType.newObject()
                .name(NamingUtils.TYPE_GQL_MESSAGE_DETAIL)
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("key")
                        .type(new GraphQLTypeReference(Scalars.GraphQLString.getName()))
                        .build())
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("value")
                        .type(new GraphQLTypeReference(Scalars.GraphQLString.getName()))
                        .build())
                .build());

        return messagesTypes;
    }
}
