package io.jmix.graphql.schema;

import graphql.Scalars;
import graphql.language.EnumTypeDefinition;
import graphql.language.EnumValueDefinition;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.datatype.impl.EnumClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.graphql.schema.scalar.CustomScalars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BaseTypesBuilder {

    private static final Logger log = LoggerFactory.getLogger(BaseTypesBuilder.class);

    @Autowired
    protected MetadataTools metadataTools;

    public static EnumTypeDefinition buildEnumTypeDef(Class<?> javaType)  {
        String enumClassName = javaType.getSimpleName();
        EnumClass<?>[] enumValues;
        try {
            enumValues = (EnumClass<?>[]) javaType.getDeclaredMethod("values").invoke(null);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new UnsupportedOperationException("Can't build enum type definition for java type " + enumClassName, e);
        }
        log.debug("buildEnumTypeDef: for class {} values {}", enumClassName, enumValues);
        return EnumTypeDefinition.newEnumTypeDefinition()
                .name(javaType.getSimpleName())
                .enumValueDefinitions(Arrays.stream(enumValues)
                        .flatMap(BaseTypesBuilder::getEnumValueDef)
                        .collect(Collectors.toList()))
                .build();
    }

    protected static Stream<EnumValueDefinition> getEnumValueDef(EnumClass<?> enumClass) {
        return Stream.of(EnumValueDefinition.newEnumValueDefinition().name(((Enum<?>) enumClass).name()).build());
    }

    protected abstract String normalizeName(String entityName);

    protected String getFieldTypeName(MetaProperty metaProperty) {

        Class<?> javaType = metaProperty.getJavaType();

        if (metaProperty.getType() == MetaProperty.Type.DATATYPE) {

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
                return CustomScalars.GraphQLDate.getName();
            }
            if (LocalDateTime.class.isAssignableFrom(javaType)) {
                return CustomScalars.GraphQLLocalDateTime.getName();
            }
        }

        if (metaProperty.getType() == MetaProperty.Type.ENUM) {
            return javaType.getSimpleName();
        }

        // todo non-persistent jmix entities
        if ((metaProperty.getType() == MetaProperty.Type.ASSOCIATION || metaProperty.getType() == MetaProperty.Type.COMPOSITION)) {

            if (metaProperty.getRange().getCardinality().isMany()) {
                return normalizeName(metadataTools.getEntityName(metaProperty.getRange().asClass().getJavaClass()));
            }

            if (metadataTools.isPersistent(metaProperty.getJavaType())) {
                return normalizeName(metadataTools.getEntityName(javaType));
            }
        }

        return "String";
    }

}
