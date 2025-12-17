package io.jmix.datatools.datamodel.engine;

import io.jmix.datatools.datamodel.app.RelationType;
import io.jmix.datatools.datamodel.entity.AttributeModel;

import java.util.List;

public interface DiagramConstructor {
    String constructEntityDescription(String entityName, List<AttributeModel> attributeModelList);
    String constructRelationDescription(String currentEntityType, String refEntityType, RelationType relationType);
    byte[] getDiagram(String entitiesDescription, String relationsDescriptions);
}
