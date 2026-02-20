package io.jmix.datatools.datamodel;

import io.jmix.datatools.datamodel.entity.AttributeModel;
import io.jmix.datatools.datamodel.entity.EntityModel;

import java.util.List;
import java.util.Map;

/**
 * Represents the data model for an entity, encapsulating its metadata, attributes, and relationships.
 */
public record DataModel(String entityName, String dataStore, EntityModel entityModel,
                        Map<RelationType, List<Relation>> relations, String entityDescription,
                        List<AttributeModel> attributeModels) {
}
