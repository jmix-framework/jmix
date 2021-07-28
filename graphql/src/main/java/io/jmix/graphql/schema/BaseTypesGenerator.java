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

import graphql.schema.*;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.graphql.NamingUtils;
import io.jmix.graphql.schema.scalar.ScalarTypes;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component("gql_BaseTypesGenerator")
public class BaseTypesGenerator {

    @Autowired
    private MetadataTools metadataTools;
    @Autowired
    ScalarTypes scalarTypes;

    /**
     * Shortcut for input value definition that has list type
     *
     * @param fieldName   field name
     * @param type        input value type
     * @param description input value description
     * @return field
     */
    public static GraphQLInputObjectField listInpObjectField(String fieldName, String type, @Nullable String description) {
        return GraphQLInputObjectField.newInputObjectField()
                .name(fieldName).type(listType(type))
                .description(description)
                .build();
    }

    /**
     * Shortcut for input value definition
     *
     * @param fieldName   field name
     * @param type        input value type
     * @param description input value description
     * @return field
     */
    public static GraphQLInputObjectField inpObjectField(String fieldName, String type, @Nullable String description) {
        return GraphQLInputObjectField.newInputObjectField()
                .name(fieldName).type(new GraphQLTypeReference(type))
                .description(StringUtils.isBlank(description) ? null : description)
                .build();
    }

    public static GraphQLArgument argNonNull(String id, String type) {
        return GraphQLArgument.newArgument()
                .name(id)
                .type(GraphQLNonNull.nonNull(new GraphQLTypeReference(type)))
                .build();
    }

    /**
     * Shortcut for query argument builder
     *
     * @param name        argument name
     * @param type        argument type
     * @param description argument description
     * @return argument
     */
    public static GraphQLArgument arg(String name, String type, @Nullable String description) {
        return GraphQLArgument.newArgument()
                .name(name).type(new GraphQLTypeReference(type))
                .description(description)
                .build();
    }

    /**
     * Shortcut for query argument builder (list type argument)
     *
     * @param name        argument name
     * @param type        argument type
     * @param description argument description
     * @return argument
     */
    public static GraphQLArgument listArg(String name, String type, @Nullable String description) {
        return GraphQLArgument.newArgument()
                .name(name).type(listType(type))
                .description(description)
                .build();
    }

    @NonNull
    public static GraphQLList listType(String typeName) {
        return GraphQLList.list(new GraphQLTypeReference(typeName));
    }


    public String getFieldTypeName(MetaProperty metaProperty) {
        if (metaProperty.getType() == MetaProperty.Type.DATATYPE) {
            return scalarTypes.getScalarFieldTypeName(metaProperty);
        }

        if (metaProperty.getType() == MetaProperty.Type.ENUM) {
            return getEnumFieldTypeName(metaProperty);
        }

        // todo no support for non-persistent jmix entities
        if ((metaProperty.getType() == MetaProperty.Type.ASSOCIATION
                || metaProperty.getType() == MetaProperty.Type.COMPOSITION
                || metaProperty.getType() == MetaProperty.Type.EMBEDDED)) {
            return getReferenceTypeName(metaProperty);
        }

        throw new UnsupportedOperationException(String.format("Can't define field type name for metaProperty %s class %s", metaProperty, metaProperty.getJavaType()));
    }


    protected String getReferenceTypeName(MetaProperty metaProperty) {
        if (metadataTools.isJpaEntity(metaProperty.getRange().asClass())) {
            return NamingUtils.normalizeName(metaProperty.getRange().asClass().getName());
        } else {
            // todo non-persistent entities are not fully supported now
            return "String";
        }
    }

    public static String getEnumFieldTypeName(MetaProperty metaProperty) {
        return metaProperty.getJavaType().getSimpleName();
    }

}
