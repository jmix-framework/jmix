package io.jmix.graphql.schema;

import graphql.Scalars;
import graphql.language.EnumTypeDefinition;
import graphql.language.EnumValueDefinition;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.graphql.MetadataUtils;
import io.jmix.graphql.datafetcher.GqlEntityValidationException;
import io.jmix.graphql.schema.scalar.CustomScalars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.*;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BaseTypesBuilder {

    private static final Logger log = LoggerFactory.getLogger(BaseTypesBuilder.class);

    @Autowired
    protected MetadataTools metadataTools;

    public static EnumTypeDefinition buildEnumTypeDef(Class<?> javaType) {
        String enumClassName = javaType.getSimpleName();
        Enum<?>[] enumValues;
        try {
            enumValues = (Enum<?>[]) javaType.getDeclaredMethod("values").invoke(null);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new UnsupportedOperationException("Can't build enum type definition for java type " + enumClassName, e);
        }
        log.debug("buildEnumTypeDef: for class {} values {}", enumClassName, enumValues);
        return EnumTypeDefinition.newEnumTypeDefinition()
                .name(enumClassName)
                .enumValueDefinitions(Arrays.stream(enumValues)
                        .flatMap(BaseTypesBuilder::getEnumValueDef)
                        .collect(Collectors.toList()))
                .build();
    }

    protected static Stream<EnumValueDefinition> getEnumValueDef(Enum<?> enumClass) {
        return Stream.of(EnumValueDefinition.newEnumValueDefinition().name(enumClass.name()).build());
    }

    protected abstract String normalizeName(String entityName);

    protected String getFieldTypeName(MetaProperty metaProperty) {
        if (metaProperty.getType() == MetaProperty.Type.DATATYPE) {
            return getDatatypeFieldTypeName(metaProperty);
        }

        if (metaProperty.getType() == MetaProperty.Type.ENUM) {
            return getEnumFieldTypeName(metaProperty.getJavaType());
        }

        // todo no support for non-persistent jmix entities
        if ((metaProperty.getType() == MetaProperty.Type.ASSOCIATION || metaProperty.getType() == MetaProperty.Type.COMPOSITION)) {
            return getReferenceTypeName(metaProperty);
        }

        throw new UnsupportedOperationException(String.format("Can't define field type name for metaProperty %s class %s", metaProperty, metaProperty.getJavaType()));
    }

    protected String getDatatypeFieldTypeName(MetaProperty metaProperty) {
        Class<?> javaType = metaProperty.getRange().asDatatype().getJavaClass();

        // scalars from graphql-java

        if (String.class.isAssignableFrom(javaType))
            return Scalars.GraphQLString.getName();
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

        log.warn("getDatatypeFieldTypeName: can't resolve type for datatype meta property {} class {}", metaProperty, javaType);
        // todo a couple of classes are not supported now
        return "String";
//        throw new UnsupportedOperationException(String.format("Can't define field type name for datatype class %s", javaType));
    }

    protected String getReferenceTypeName(MetaProperty metaProperty) {
        if (metadataTools.isJpaEntity(metaProperty.getRange().asClass())) {
            return normalizeName(metaProperty.getRange().asClass().getName());
        } else {
            // todo non-persistent entities are not fully supported now
            return "String";
        }
    }

    protected String getEnumFieldTypeName(Class<?> javaType) {
        return javaType.getSimpleName();
    }

}
