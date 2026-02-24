/*
 * Copyright 2026 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.datatoolsflowui.datamodel;

import io.jmix.core.common.util.Preconditions;
import io.jmix.datatools.datamodel.DataModel;
import io.jmix.datatools.datamodel.Relation;
import io.jmix.datatools.datamodel.RelationType;
import io.jmix.datatools.datamodel.entity.AttributeModel;
import io.jmix.datatools.datamodel.entity.EntityModel;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DataModelProvider {

    protected final Map<String, Map<String, DataModel>> dataModels;

    public DataModelProvider(Map<String, Map<String, DataModel>> dataModels) {
        this.dataModels = dataModels;
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
        return dataModels;
    }

    /**
     * Retrieves the data models associated with a specific data store.
     *
     * @param dataStore the name of the data store whose data models are to be retrieved; must not be null
     * @return a map where the keys are entity names and the values are the corresponding {@link DataModel} instances
     */
    public Map<String, DataModel> getDataModels(String dataStore) {
        Map<String, DataModel> dataModelMap = dataModels.get(dataStore);
        return dataModelMap != null ? dataModelMap : Collections.emptyMap();
    }

    /**
     * Retrieves the {@link DataModel} associated with the given data store and entity name.
     *
     * @param dataStore  the name of the data store from which the data model is to be fetched
     * @param entityName the name of the entity whose data model is to be fetched
     * @return the {@link DataModel} corresponding to the specified data store and entity name,
     * or {@code null} if no such data model exists
     */
    @Nullable
    public DataModel getDataModel(String dataStore, String entityName) {
        Preconditions.checkNotNullArgument(dataStore, "Data store name cannot be null");
        Preconditions.checkNotNullArgument(entityName, "Entity name cannot be null");

        return getDataModels(dataStore).get(entityName);
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
        Preconditions.checkNotNullArgument(dataStore, "Data store name cannot be null");
        Preconditions.checkNotNullArgument(entityName, "Entity name cannot be null");

        Map<String, DataModel> dataModels = getDataModels(dataStore);
        if (dataModels.isEmpty()) {
            return false;
        }

        if (dataModels.containsKey(entityName)) {
            DataModel dataModel = dataModels.get(entityName);
            return dataModel != null && !dataModel.relations().isEmpty();
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
    public Map<RelationType, List<Relation>> getEntityRelations(String dataStore, String entityName) {
        Preconditions.checkNotNullArgument(dataStore, "Data store name cannot be null");
        Preconditions.checkNotNullArgument(entityName, "Entity name cannot be null");

        DataModel dataModel = getDataModels(dataStore).get(entityName);
        return dataModel != null
                ? Collections.unmodifiableMap(dataModel.relations())
                : Collections.emptyMap();
    }

    /**
     * Retrieves a list of attributes for a specific entity within a given data store.
     *
     * @param dataStore  the name of the data store containing the entity; must not be null
     * @param entityName the name of the entity whose attributes are to be retrieved; must not be null
     * @return a list of {@link AttributeModel} representing the attributes defined for the specified entity;
     * an empty list is returned if the entity has no attributes or if the data store or entity does not exist
     */
    public List<AttributeModel> getEntityAttributes(String dataStore, String entityName) {
        Preconditions.checkNotNullArgument(dataStore, "Data store name cannot be null");
        Preconditions.checkNotNullArgument(entityName, "Entity name cannot be null");

        DataModel dataModel = getDataModels(dataStore).get(entityName);
        return dataModel != null
                ? Collections.unmodifiableList(dataModel.attributeModels())
                : Collections.emptyList();
    }

    /**
     * Retrieves the {@link EntityModel} associated with the specified data store and entity name.
     *
     * @param dataStore  the name of the data store where the entity resides; must not be null
     * @param entityName the name of the entity whose model is to be retrieved; must not be null
     * @return the {@link EntityModel} corresponding to the specified data store and entity name,
     * or {@code null} if no such entity model exists
     */
    @Nullable
    public EntityModel getEntityModel(String dataStore, String entityName) {
        Preconditions.checkNotNullArgument(dataStore, "Data store name cannot be null");
        Preconditions.checkNotNullArgument(entityName, "Entity name cannot be null");

        DataModel dataModel = getDataModels(dataStore).get(entityName);
        return dataModel != null
                ? dataModel.entityModel()
                : null;
    }
}
