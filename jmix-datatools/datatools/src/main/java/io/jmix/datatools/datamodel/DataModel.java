package io.jmix.datatools.datamodel;

import io.jmix.datatools.datamodel.app.Relation;
import io.jmix.datatools.datamodel.app.RelationType;
import io.jmix.datatools.datamodel.entity.AttributeModel;
import io.jmix.datatools.datamodel.entity.EntityModel;

import java.util.List;
import java.util.Map;

public class DataModel {
    private String entityName;
    private EntityModel entityModel;
    private List<AttributeModel> attributeModels;
    private Map<RelationType, List<Relation>> relations;
    private String entityDescription;

    public DataModel(String entityName, EntityModel entityModel, Map<RelationType, List<Relation>> relations,
                     String entityDescription, List<AttributeModel> attributeModels) {
        this.entityName = entityName;
        this.entityModel = entityModel;
        this.relations = relations;
        this.entityDescription = entityDescription;
        this.attributeModels = attributeModels;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public void setEntityModel(EntityModel entityModel) {
        this.entityModel = entityModel;
    }

    public void setAttributeModels(List<AttributeModel> attributeModels) {
        this.attributeModels = attributeModels;
    }

    public void setRelations(Map<RelationType, List<Relation>> relations) {
        this.relations = relations;
    }

    public void setEntityDescription(String entityDescription) {
        this.entityDescription = entityDescription;
    }

    public String getEntityName() {
        return entityName;
    }

    public EntityModel getEntityModel() {
        return entityModel;
    }

    public List<AttributeModel> getAttributeModels() {
        return attributeModels;
    }

    public Map<RelationType, List<Relation>> getRelations() {
        return relations;
    }

    public String getEntityDescription() {
        return entityDescription;
    }
}
