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
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.graphql.MetadataUtils;
import io.jmix.graphql.NamingUtils;
import io.jmix.graphql.datafetcher.GqlEntityValidationException;
import io.jmix.graphql.schema.scalar.CustomScalars;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.*;
import java.util.Date;
import java.util.UUID;

@Component("gql_BaseTypesGenerator")
public class BaseTypesGenerator {

    @Autowired
    private MetadataTools metadataTools;

    /**
     * Shortcut for input value definition that has list type
     *
     * @param fieldName field name
     * @param type input value type
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
     * @param fieldName field name
     * @param type input value type
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

    @NotNull
    public static GraphQLList listType(String typeName) {
        return GraphQLList.list(new GraphQLTypeReference(typeName));
    }


    public String getFieldTypeName(MetaProperty metaProperty) {
        if (metaProperty.getType() == MetaProperty.Type.DATATYPE) {
            return getScalarFieldTypeName(metaProperty);
        }

        if (metaProperty.getType() == MetaProperty.Type.ENUM) {
            return getEnumFieldTypeName(metaProperty);
        }

        // todo no support for non-persistent jmix entities
        if ((metaProperty.getType() == MetaProperty.Type.ASSOCIATION || metaProperty.getType() == MetaProperty.Type.COMPOSITION)) {
            return getReferenceTypeName(metaProperty);
        }

        throw new UnsupportedOperationException(String.format("Can't define field type name for metaProperty %s class %s", metaProperty, metaProperty.getJavaType()));
    }


    protected static String getScalarFieldTypeName(MetaProperty metaProperty) {
        Class<?> javaType = metaProperty.getRange().asDatatype().getJavaClass();

        // scalars from graphql-java

        if (String.class.isAssignableFrom(javaType))
            return Scalars.GraphQLString.getName();
        if (Character.class.isAssignableFrom(javaType))
            return Scalars.GraphQLChar.getName();
        if (Integer.class.isAssignableFrom(javaType) || int.class.isAssignableFrom(javaType)) {
            return Scalars.GraphQLInt.getName();
        }
        if (Short.class.isAssignableFrom(javaType) || short.class.isAssignableFrom(javaType)) {
            return Scalars.GraphQLShort.getName();
        }
        if (Float.class.isAssignableFrom(javaType) || float.class.isAssignableFrom(javaType)
                || Double.class.isAssignableFrom(javaType) || double.class.isAssignableFrom(javaType)) {
            return Scalars.GraphQLFloat.getName();
        }
        if (Boolean.class.isAssignableFrom(javaType) || boolean.class.isAssignableFrom(javaType)) {
            return Scalars.GraphQLBoolean.getName();
        }

        // more scalars added in jmix-graphql

        if (UUID.class.isAssignableFrom(javaType)) {
            return CustomScalars.GraphQLUUID.getName();
        }
        if (Long.class.isAssignableFrom(javaType) || long.class.isAssignableFrom(javaType)) {
            return CustomScalars.GraphQLLong.getName();
        }
        if (BigDecimal.class.isAssignableFrom(javaType)) {
            return CustomScalars.GraphQLBigDecimal.getName();
        }
        if (Date.class.isAssignableFrom(javaType)) {
            if (MetadataUtils.isDate(metaProperty)) {
                return CustomScalars.GraphQLDate.getName();
            }
            if (MetadataUtils.isTime(metaProperty)) {
                return CustomScalars.GraphQLTime.getName();
            }
            if (MetadataUtils.isDateTime(metaProperty)) {
                    return CustomScalars.GraphQLDateTime.getName();
            }
            throw new GqlEntityValidationException("Unsupported datatype mapping for date property " + metaProperty);
        }
        if (LocalDateTime.class.isAssignableFrom(javaType)) {
            return CustomScalars.GraphQLLocalDateTime.getName();
        }
        if (LocalDate.class.isAssignableFrom(javaType)) {
            return CustomScalars.GraphQLLocalDate.getName();
        }
        if (LocalTime.class.isAssignableFrom(javaType)) {
            return CustomScalars.GraphQLLocalTime.getName();
        }
        if (OffsetDateTime.class.isAssignableFrom(javaType)) {
            return CustomScalars.GraphQLOffsetDateTime.getName();
        }
        if (OffsetTime.class.isAssignableFrom(javaType)) {
            return CustomScalars.GraphQLOffsetTime.getName();
        }

//        log.warn("getDatatypeFieldTypeName: can't resolve type for datatype meta property {} class {}", metaProperty, javaType);
        // todo a couple of classes are not supported now
        return "String";
//        throw new UnsupportedOperationException(String.format("Can't define field type name for datatype class %s", javaType));
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
