package io.jmix.datatools.datamodel;

import io.jmix.datatools.datamodel.app.Relation;
import io.jmix.datatools.datamodel.app.RelationType;
import io.jmix.datatools.datamodel.entity.AttributeModel;
import io.jmix.datatools.datamodel.entity.EntityModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataModelHolder {

    private final Map<String, DataModel> entityModels;

    public DataModelHolder() {
        this.entityModels = new HashMap<>();
    }

    public void putDataModel(DataModel dataModel) {
        entityModels.put(dataModel.getEntityName(), dataModel);
    }

    public DataModel getDataModel(String entityName) {
        return entityModels.get(entityName);
    }

    public boolean isModelExists(String entityName) {
        return entityModels.containsKey(entityName);
    }

    public boolean hasRelations(String entityName) {
        if (entityModels.containsKey(entityName)) {
            return !entityModels.get(entityName).getRelations().isEmpty();
        }

        return false;
    }

    public void createRelation(String currentEntityType, Relation relation, RelationType relationType) {
        if (entityModels.containsKey(currentEntityType)) {
            DataModel dataModel = entityModels.get(currentEntityType);

            if (dataModel.getRelations().containsKey(relationType)) {
                dataModel.getRelations().get(relationType).add(relation);
            } else {
                dataModel.getRelations().put(relationType, new ArrayList<>(List.of(relation)));
            }
        }
    }

    public Map<RelationType, List<Relation>> getRelationsByEntity(String entityName) {
        return entityModels.get(entityName).getRelations();
    }

    public void putAttribute(String entityName, AttributeModel attributeModel) {
        if (!entityModels.containsKey(entityName)) {
            throw new IllegalArgumentException(String.format("Entity model with name %s not found in its Map", entityName));
        }
        entityModels.get(entityName).getAttributeModels().add(attributeModel);
    }

    public EntityModel getEntityModel(String entityName) {
        return entityModels.get(entityName).getEntityModel();
    }

    public List<AttributeModel> getAttributesByEntity(String entityName) {
        return entityModels.get(entityName).getAttributeModels();
    }

    public Map<String, DataModel> getDataModels() {
        return entityModels;
    }

    public int modelsCount() {
        return entityModels.size();
    }

    public void clear() {
        entityModels.clear();
    }
}