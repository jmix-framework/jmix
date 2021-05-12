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
import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.SDLDefinition;
import graphql.language.TypeName;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MessagesTypesBuilder {

    @SuppressWarnings("rawtypes")
    public List<SDLDefinition> buildMessagesTypes() {
        List<SDLDefinition> messagesTypes = new ArrayList<>();

        messagesTypes.add(ObjectTypeDefinition.newObjectTypeDefinition()
                .name(NamingUtils.TYPE_GQL_MESSAGE_DETAIL)
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("key")
                        .type(new TypeName(Scalars.GraphQLString.getName()))
                        .build())
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("value")
                        .type(new TypeName(Scalars.GraphQLString.getName()))
                        .build())
                .build());

        return messagesTypes;
    }
}
