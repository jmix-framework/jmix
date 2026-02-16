package io.jmix.datatools.datamodel;

import io.jmix.datatools.datamodel.entity.AttributeModel;
import io.jmix.datatools.datamodel.entity.EntityModel;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages and provides access to entity data models organized by data stores.
 * It helps in managing entity metadata, attributes, and relationships.
 */
public class DataModelProvider {

    /**
     * A map that represents a general data model, grouped by data stores
     */
    protected final Map<String, Map<String, DataModel>> dataModels;

    public DataModelProvider() {
        this.dataModels = new HashMap<>();
    }

    /**
     * Adds a {@link DataModel} to the internal data model storage. If a data store already exists,
     * the method updates its corresponding entity map with the given data model. If the data store
     * is not present, a new entry is created for it.
     *
     * @param dataModel the data model to add, containing details regarding the entity, its metadata,
     *                  and relationships
     */
    public void putDataModel(DataModel dataModel) {
        String dataStore = dataModel.dataStore();
        String entityName = dataModel.entityName();

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

    /**
     * Retrieves the {@link DataModel} associated with the given data store and entity name.
     *
     * @param dataStore  the name of the data store from which the data model is to be fetched
     * @param entityName the name of the entity whose data model is to be fetched
     * @return the {@link DataModel} corresponding to the specified data store and entity name,
     * or {@code null} if no such data model exists
     */
    public DataModel getDataModel(String dataStore, String entityName) {
        return dataModels.get(dataStore).get(entityName);
    }

    public boolean isModelExists(String dataStore, String entityName) {
        if (!dataModels.containsKey(dataStore)) {
            return false;
        }

        return dataModels.get(dataStore).containsKey(entityName);
    }

    /**
     * Determines whether the given entity in the specified data store has any relationships defined.
     *
     * @param dataStore  the name of the data store to which the entity belongs; must not be null
     * @param entityName the name of the entity to check for relationships; must not be null
     * @return {@code true} if the entity has at least one relationship defined in the data model,
     * {@code false} otherwise
     */
    public boolean hasRelations(String dataStore, String entityName) {
        if (!dataModels.containsKey(dataStore)) {
            return false;
        }

        if (dataModels.get(dataStore).containsKey(entityName)) {
            return !dataModels.get(dataStore).get(entityName).relations().isEmpty();
        }

        return false;
    }

    /**
     * Retrieves the relationships defined for a specific entity in the data model of a given data store.
     *
     * @param dataStore  the name of the data store containing the desired entity; must not be null
     * @param entityName the name of the entity for which the relationships are to be retrieved; must not be null
     * @return a map where the keys specify the type of relationships (e.g., MANY_TO_ONE, ONE_TO_MANY)
     * and the values are lists of {@link Relation} objects detailing the relationships;
     * if the entity or data store has no relationships, an empty map is returned
     */
    public Map<RelationType, List<Relation>> getRelationsByEntity(String dataStore, String entityName) {
        return dataModels.get(dataStore).get(entityName).relations();
    }

    /**
     * Retrieves the {@link EntityModel} associated with the specified data store and entity name.
     *
     * @param dataStore  the name of the data store where the entity resides; must not be null
     * @param entityName the name of the entity whose model is to be retrieved; must not be null
     * @return the {@link EntityModel} corresponding to the specified data store and entity name,
     * or {@code null} if no such entity model exists
     */
    public EntityModel getEntityModel(String dataStore, String entityName) {
        return dataModels.get(dataStore).get(entityName).entityModel();
    }

    /**
     * Retrieves a list of attributes for a specific entity within a given data store.
     *
     * @param dataStore  the name of the data store containing the entity; must not be null
     * @param entityName the name of the entity whose attributes are to be retrieved; must not be null
     * @return a list of {@link AttributeModel} representing the attributes defined for the specified entity;
     * an empty list is returned if the entity has no attributes or if the data store or entity does not exist
     */
    public List<AttributeModel> getAttributesByEntity(String dataStore, String entityName) {
        return dataModels.get(dataStore).get(entityName).attributeModels();
    }

    /**
     * Retrieves an unmodifiable view of the internal data model storage.
     * The returned map represents an organizational structure where the first-level keys are
     * data store identifiers and the values are nested maps. The nested maps use
     * entity names as keys and their corresponding {@link DataModel} objects as values.
     *
     * @return a map containing data store identifiers as keys, where each value is another map that
     * maps entity names to their respective {@link DataModel} instances. The returned map
     * is unmodifiable.
     */
    public Map<String, Map<String, DataModel>> getDataModels() {
        return Collections.unmodifiableMap(dataModels);
    }

    /**
     * Retrieves the data models associated with a specific data store.
     *
     * @param dataStore the name of the data store whose data models are to be retrieved; must not be null
     * @return a map where the keys are entity names and the values are the corresponding {@link DataModel} instances;
     * or {@code null} if no data models exist for the specified data store
     */
    public Map<String, DataModel> getDataModels(String dataStore) {
        return dataModels.get(dataStore);
    }

    /**
     * Returns the number of data models present in the specified data store.
     *
     * @param dataStore the name of the data store for which the model count is to be retrieved; must not be null
     * @return the number of data models in the specified data store
     */
    public int getModelsCount(String dataStore) {
        return dataModels.get(dataStore).size();
    }

    /**
     * Calculates the total number of data models across all data stores.
     *
     * @return the total count of data models present in the internal storage
     */
    public int getModelsCount() {
        return dataModels.values().stream()
                .map(Map::size)
                .reduce(0, Integer::sum);
    }

    /**
     * Clears all entries from the internal data model storage.
     * This method removes all existing mappings between data stores,
     * entities, and their corresponding {@link DataModel} instances,
     * effectively resetting the storage to an empty state.
     */
    public void clear() {
        dataModels.clear();
    }
}