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

import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLEnumValueDefinition;
import graphql.schema.GraphQLType;
import io.jmix.graphql.MetadataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component("gql_EnumTypesGenerator")
public class EnumTypesGenerator {

    @Autowired
    private MetadataUtils metadataUtils;

    public Collection<GraphQLType> generateEnumTypes() {
        // find enums used in properties and create type definitions for enums
        return metadataUtils.allEnumJavaClasses().stream()
                .map(EnumTypesGenerator::generateEnumType)
                .collect(Collectors.toList());
    }

    public static GraphQLEnumType generateEnumType(Class<?> javaType)  {
        String enumClassName = javaType.getSimpleName();
        Enum<?>[] enumValues;
        enumValues = MetadataUtils.getEnumValues(javaType);
//		log.debug("buildEnumTypeDef: for class {} values {}", enumClassName, enumValues);
        return GraphQLEnumType.newEnum()
                .name(enumClassName)
                .values(Arrays.stream(enumValues)
                        .flatMap(EnumTypesGenerator::getEnumValueDef)
                        .collect(Collectors.toList()))
                .build();
    }

    protected static Stream<GraphQLEnumValueDefinition> getEnumValueDef(Enum<?> anEnum) {
        return Stream.of(GraphQLEnumValueDefinition.newEnumValueDefinition()
                .name(anEnum.name())
                .value(anEnum)
                .build());
    }

}
