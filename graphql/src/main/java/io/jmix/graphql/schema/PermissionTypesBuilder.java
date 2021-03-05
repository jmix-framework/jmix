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
import graphql.language.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PermissionTypesBuilder {

    public List<SDLDefinition> buildPermissionTypes() {
        List<SDLDefinition> permissionTypes = new ArrayList<>();

        permissionTypes.add(ObjectTypeDefinition.newObjectTypeDefinition()
                .name(NamingUtils.TYPE_SEC_PERMISSION)
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("target")
                        .type(new TypeName(Scalars.GraphQLString.getName()))
                        .build())
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name("value")
                        .type(new TypeName(Scalars.GraphQLInt.getName()))
                        .build())
                .build());

        permissionTypes.add(ObjectTypeDefinition.newObjectTypeDefinition()
                .name(NamingUtils.TYPE_SEC_PERMISSION_CONFIG)
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name(NamingUtils.ENTITIES)
                        .type(ListType.newListType(new TypeName(NamingUtils.TYPE_SEC_PERMISSION)).build())
                        .build())
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name(NamingUtils.ENTITY_ATTRS)
                        .type(ListType.newListType(new TypeName(NamingUtils.TYPE_SEC_PERMISSION)).build())
                        .build())
                .build());
        return permissionTypes;
    }

}
