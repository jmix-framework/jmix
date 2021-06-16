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
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component("gql_EnumTypesGenerator")
public class EnumTypesGenerator {

    @Autowired
    private MetadataTools metadataTools;

    public Collection<GraphQLType> generateEnumTypes() {
        Collection<GraphQLType> types = new ArrayList<>();
        Collection<MetaClass> allPersistentMetaClasses = metadataTools.getAllJpaEntityMetaClasses()
                // todo need to be fixed later - ReferenceToEntity is not persistent but returned in 'metadataTools.getAllPersistentMetaClasses'
                .stream()
                .filter(metaClass -> !metaClass.getJavaClass().getSimpleName().equals("ReferenceToEntity"))
                .collect(Collectors.toList());

        // find enums used in properties and create type definitions for enums
        allPersistentMetaClasses.stream()
                .flatMap(metaClass -> metaClass.getProperties().stream())
                .filter(metaProperty -> metaProperty.getType() == MetaProperty.Type.ENUM)
                .distinct()
                .forEach(metaProperty -> types.add(generateEnumType(metaProperty.getJavaType())));
        return types;
    }

    public static GraphQLEnumType generateEnumType(Class<?> javaType)  {
        String enumClassName = javaType.getSimpleName();
        Enum<?>[] enumValues;
        try {
            enumValues = (Enum<?>[]) javaType.getDeclaredMethod("values").invoke(null);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new UnsupportedOperationException("Can't build enum type definition for java type " + enumClassName, e);
        }
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
