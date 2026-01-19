package io.jmix.datatools.datamodel;

import io.jmix.datatools.datamodel.app.Relation;
import io.jmix.datatools.datamodel.app.RelationType;
import io.jmix.datatools.datamodel.entity.AttributeModel;
import io.jmix.datatools.datamodel.entity.EntityModel;

import java.util.*;

/**
 * This class provides for storaging data model
 */
public class DataModelProvider {

    /**
     * A map that represents a general data model, grouped by data stores
     */
    private final Map<String, Map<String, DataModel>> dataModels;

    public DataModelProvider() {
        this.dataModels = new HashMap<>();
    }

    public void putDataModel(DataModel dataModel) {
        String dataStore = dataModel.getDataStore();
        String entityName = dataModel.getEntityName();

        if (dataModels.containsKey(dataStore)) {
            dataModels.get(dataStore).put(entityName, dataModel);
        } else {
            dataModels.put(dataStore, new HashMap<>() {
                {
                    put(entityName, dataModel);
                }
            });
        }
    }

    public DataModel getDataModel(String dataStore, String entityName) {
        return dataModels.get(dataStore).get(entityName);
    }

    public boolean isModelExists(String dataStore, String entityName) {
        if (!dataModels.containsKey(dataStore)) return false;

        return dataModels.get(dataStore).containsKey(entityName);
    }

    public boolean hasRelations(String dataStore, String entityName) {
        if (!dataModels.containsKey(dataStore)) return false;
        if (dataModels.get(dataStore).containsKey(entityName)) {
            return !dataModels.get(dataStore).get(entityName).getRelations().isEmpty();
        }

        return false;
    }

    public Map<RelationType, List<Relation>> getRelationsByEntity(String dataStore, String entityName) {
        return dataModels.get(dataStore).get(entityName).getRelations();
    }

    public EntityModel getEntityModel(String dataStore, String entityName) {
        return dataModels.get(dataStore).get(entityName).getEntityModel();
    }

    public List<AttributeModel> getAttributesByEntity(String dataStore, String entityName) {
        return dataModels.get(dataStore).get(entityName).getAttributeModels();
    }

    public Map<String, Map<String, DataModel>> getDataModels() {
        return dataModels;
    }

    public Map<String, DataModel> getDataModels(String dataStore) {
        return dataModels.get(dataStore);
    }

    public int modelsCount(String dataStore) {
        return dataModels.get(dataStore).size();
    }

    public int modelsCount() {
        Set<String> dataStores = dataModels.keySet();
        int count = 0;

        for (String store : dataStores) {
            count += dataModels.get(store).size();
        }

        return count;
    }

    public void clear() {
        dataModels.clear();
    }
}