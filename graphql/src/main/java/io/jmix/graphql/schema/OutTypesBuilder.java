package io.jmix.graphql.schema;


import graphql.language.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.jmix.graphql.schema.NamingUtils.normalizeOutTypeName;

@Component
public class OutTypesBuilder extends BaseTypesBuilder {


    private static final Logger log = LoggerFactory.getLogger(OutTypesBuilder.class);

    @Override
    protected String normalizeName(String entityName) {
        return normalizeOutTypeName(entityName);
    }

    public ObjectTypeDefinition buildObjectTypeDef(MetaClass metaClass) {
        List<MetaProperty> properties = metaClass.getProperties().stream()
                .filter(this::isNotIgnored).collect(Collectors.toList());
        log.debug("buildObjectTypeDef: for meta class {} properties {}", metaClass.getName(), properties);

        return ObjectTypeDefinition.newObjectTypeDefinition()
                .name(normalizeOutTypeName(metaClass.getName()))
                .fieldDefinitions(properties.stream()
                        .flatMap(this::getObjectFieldDef)
                        .collect(Collectors.toList()))
                // add system attrs
                .fieldDefinition(FieldDefinition.newFieldDefinition()
                        .name(NamingUtils.SYS_ATTR_INSTANCE_NAME).type(new TypeName("String"))
                        .build())
                .build();
    }

    protected Stream<FieldDefinition> getObjectFieldDef(MetaProperty metaProperty) {
        String typeName = getFieldTypeName(metaProperty);
        boolean isMany = metaProperty.getRange().getCardinality().isMany();

        Type<?> type = isMany ? new ListType(new TypeName(typeName)) : new TypeName(typeName);
        return Stream.of(FieldDefinition.newFieldDefinition()
                .name(metaProperty.getName())
                .type(type)
                .build());
    }

    protected boolean isNotIgnored(MetaProperty metaProperty) {
        return true;
    }

}