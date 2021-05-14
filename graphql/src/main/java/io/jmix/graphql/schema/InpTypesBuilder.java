package io.jmix.graphql.schema;

import graphql.Scalars;
import graphql.language.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.graphql.MetadataUtils;
import io.jmix.graphql.datafetcher.GqlEntityValidationException;
import io.jmix.graphql.schema.scalar.CustomScalars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.*;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.jmix.graphql.schema.NamingUtils.normalizeInpTypeName;

@Component
public class InpTypesBuilder extends BaseTypesBuilder {

    private static final Logger log = LoggerFactory.getLogger(OutTypesBuilder.class);

    @Override
    protected String normalizeName(String entityName) {
        return normalizeInpTypeName(entityName);
    }

    public InputObjectTypeDefinition buildObjectTypeDef(MetaClass metaClass) {
        Collection<MetaProperty> properties = metaClass.getProperties().stream()
                .filter(this::isNotIgnored)
                .collect(Collectors.toList());
        log.debug("buildObjectTypeDef: for meta class {} properties {}", metaClass.getName(), properties);

        return InputObjectTypeDefinition.newInputObjectDefinition()
                .name(normalizeInpTypeName(metaClass.getName()))
                .inputValueDefinitions(properties.stream()
                        .flatMap(this::getObjectFieldDef)
                        .collect(Collectors.toList()))
                .build();
    }

    protected Stream<InputValueDefinition> getObjectFieldDef(MetaProperty metaProperty) {
        String typeName = getFieldTypeName(metaProperty);
        boolean isMany = metaProperty.getRange().getCardinality().isMany();

        Type<?> type = isMany ? new ListType(new TypeName(typeName)) : new TypeName(typeName);

        return Stream.of(InputValueDefinition.newInputValueDefinition()
                .name(metaProperty.getName())
                .type(type)
                .build());
    }

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
        }

        if (metaProperty.getType() == MetaProperty.Type.ENUM) {
            return javaType.getSimpleName();
        }

        // todo non-persistent jmix entities
        if ((metaProperty.getType() == MetaProperty.Type.ASSOCIATION || metaProperty.getType() == MetaProperty.Type.COMPOSITION)) {

            if (metadataTools.isJpaEntity(metaProperty.getRange().asClass())) {
                return normalizeInpTypeName(metaProperty.getRange().asClass().getName());
            }
        }

        return "String";
    }

    protected boolean isNotIgnored(MetaProperty metaProperty) {
        return true;
    }
}
