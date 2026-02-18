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

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.graphql.MetadataUtils;
import io.jmix.graphql.NamingUtils;
import io.jmix.graphql.schema.scalar.ScalarTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.jmix.graphql.NamingUtils.normalizeInpTypeName;
import static io.jmix.graphql.NamingUtils.normalizeName;

@Component("gql_ClassTypesGenerator")
public class ClassTypesGenerator {

    @Autowired
    MetadataUtils metadataUtils;
    @Autowired
    MetadataTools metadataTools;
    @Autowired
    BaseTypesGenerator baseTypesGenerator;
    @Autowired
    ScalarTypes scalarTypes;

    public Collection<GraphQLType> generateInputTypes() {
        // input type definitions for jmix entities
        return metadataUtils.allSupportedMetaClasses()
                .stream().map(this::generateInpObjectType)
                .collect(Collectors.toList());
    }

    public Collection<GraphQLType> generateOutTypes() {
        // output type definitions for jmix entities
        return metadataUtils.allSupportedMetaClasses()
                .stream().map(this::generateOutObjectType)
                .collect(Collectors.toList());
    }


    protected GraphQLType generateInpObjectType(MetaClass metaClass) {
        return GraphQLInputObjectType.newInputObject()
                .name(normalizeInpTypeName(metaClass.getName()))
                .fields(metaClass.getProperties().stream()
                        .flatMap(this::getInpObjectField)
                        .collect(Collectors.toList()))
                .build();
    }

    protected GraphQLType generateOutObjectType(MetaClass metaClass) {
        return GraphQLObjectType.newObject()
                .name(normalizeName(metaClass.getName()))
                .fields(metaClass.getProperties().stream()
                        .flatMap(this::getOutObjectFieldDef)
                        .collect(Collectors.toList()))
                // add system attrs
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name(NamingUtils.SYS_ATTR_INSTANCE_NAME).type(new GraphQLTypeReference("String"))
                        .build())
                .build();
    }

    protected Stream<GraphQLInputObjectField> getInpObjectField(MetaProperty metaProperty) {
        String typeName = getInpFieldTypeName(metaProperty);
        boolean isMany = metaProperty.getRange().getCardinality().isMany();

        GraphQLInputType type = isMany
                ? GraphQLList.list(new GraphQLTypeReference(typeName))
                : new GraphQLTypeReference(typeName);

        return Stream.of(GraphQLInputObjectField.newInputObjectField()
                .name(metaProperty.getName())
                .type(type)
                .build());
    }

    protected Stream<GraphQLFieldDefinition> getOutObjectFieldDef(MetaProperty metaProperty) {
        String typeName = baseTypesGenerator.getFieldTypeName(metaProperty);
        boolean isMany = metaProperty.getRange().getCardinality().isMany();

        GraphQLOutputType type = isMany
                ? GraphQLList.list(new GraphQLTypeReference(typeName))
                : new GraphQLTypeReference(typeName);

        return Stream.of(GraphQLFieldDefinition.newFieldDefinition()
                .name(metaProperty.getName())
                .type(type)
                .build());
    }

    protected String getInpFieldTypeName(MetaProperty metaProperty) {
        if (metaProperty.getType() == MetaProperty.Type.DATATYPE) {
            return scalarTypes.getScalarFieldTypeName(metaProperty);
        }

        if (metaProperty.getType() == MetaProperty.Type.ENUM) {
            return BaseTypesGenerator.getEnumFieldTypeName(metaProperty);
        }

        // todo non-persistent jmix entities
        if ((metaProperty.getType() == MetaProperty.Type.ASSOCIATION || metaProperty.getType() == MetaProperty.Type.COMPOSITION)) {
            if (metadataTools.isJpaEntity(metaProperty.getRange().asClass())) {
                return normalizeInpTypeName(metaProperty.getRange().asClass().getName());
            }
        }

        return "String";
    }

}
