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

package io.jmix.restds.extension;

import io.jmix.core.JmixOrder;
import io.jmix.core.LoadContext;
import io.jmix.core.SaveContext;
import io.jmix.core.metamodel.model.MetaClass;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.Ordered;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.Map;

/**
 * Extension point of the REST data store. Lets an application add parameters to the requests that the store sends
 * to the remote service, read the JSON of loaded entities, and change the JSON of entities being saved.
 * <p>
 * Register an implementation as a Spring bean. For each request, the store calls every extension whose
 * {@link #supports(String, MetaClass)} method returns {@code true}, in the order defined by {@link #getOrder()}.
 * <p>
 * The load and save methods receive root entities only: the entities that are loaded or saved directly, not the
 * entities they reference. Removing entities does not call any method.
 */
@NullMarked
public interface RestDataStoreExtension extends Ordered {

    /**
     * Returns whether the store must call this extension for the given data store and entity.
     *
     * @param dataStoreName name of the REST data store
     * @param metaClass     metaclass of the entity being loaded, counted or saved
     */
    boolean supports(String dataStoreName, MetaClass metaClass);

    /**
     * Called before the store sends a request that loads entities.
     * <p>
     * Parameters put into {@link BeforeLoadContext#getRequestParameters()} are sent to the remote service: in the
     * query string of a GET request, or as fields of the JSON body when the store uses the search endpoint.
     * Names that the store sets itself ({@code fetchPlan}, {@code limit}, {@code offset}, {@code sort},
     * {@code filter}, {@code returnCount}) and {@code null} values are not allowed.
     */
    default void beforeLoad(BeforeLoadContext context) {
    }

    /**
     * Called for each loaded root entity, with the JSON object the entity was read from.
     * <p>
     * JSON properties that the entity's metaclass does not have are skipped when the entity is read, so this method
     * is the only place where they are available. The method is not called when loading by id finds no entity.
     */
    default void afterLoad(AfterLoadContext context) {
    }

    /**
     * Called before the store sends a request that counts entities. Request parameters follow the same rules as in
     * {@link #beforeLoad(BeforeLoadContext)}.
     * <p>
     * If the count is computed by loading the entities (for example, because in-memory row-level policies apply),
     * {@link #beforeLoad(BeforeLoadContext)} is called instead.
     */
    default void beforeCount(BeforeCountContext context) {
    }

    /**
     * Called before the store sends a root entity to be created or updated. Changes made to
     * {@link BeforeSaveContext#getEntityJson()} are sent to the remote service.
     * <p>
     * {@code FileRef} values in the JSON contain the local names of file storages. The store replaces them with the
     * remote names after this method.
     */
    default void beforeSave(BeforeSaveContext context) {
    }

    /**
     * Called after the remote service returns a saved root entity.
     * <p>
     * The returned JSON contains the attributes of the {@code _base} fetch plan only, so values that
     * {@link #beforeSave(BeforeSaveContext)} added to the request are not in it. Take them from
     * {@link AfterSaveContext#getEntity()} if needed.
     * <p>
     * Unless {@link SaveContext#isDiscardSaved()} is set, the saved entities are then loaded again, and the caller
     * receives the loaded instances, not {@link AfterSaveContext#getSavedEntity()}. This load calls
     * {@link #beforeLoad(BeforeLoadContext)} and {@link #afterLoad(AfterLoadContext)} with a new
     * {@link LoadContext}, which does not have the hints of the save.
     */
    default void afterSave(AfterSaveContext context) {
    }

    @Override
    default int getOrder() {
        return JmixOrder.LOWEST_PRECEDENCE;
    }

    /**
     * Data passed to {@link #beforeLoad(BeforeLoadContext)}.
     */
    class BeforeLoadContext {

        private final String dataStoreName;
        private final LoadContext<?> loadContext;
        private final Map<String, Object> requestParameters;

        public BeforeLoadContext(String dataStoreName, LoadContext<?> loadContext,
                                 Map<String, Object> requestParameters) {
            this.dataStoreName = dataStoreName;
            this.loadContext = loadContext;
            this.requestParameters = requestParameters;
        }

        public String getDataStoreName() {
            return dataStoreName;
        }

        public LoadContext<?> getLoadContext() {
            return loadContext;
        }

        /**
         * Returns a mutable map of parameters to add to the request. All extensions called for the request share it.
         */
        public Map<String, Object> getRequestParameters() {
            return requestParameters;
        }
    }

    /**
     * Data passed to {@link #afterLoad(AfterLoadContext)}.
     */
    class AfterLoadContext {

        private final String dataStoreName;
        private final LoadContext<?> loadContext;
        private final Object entity;
        private final JsonNode entityJson;

        public AfterLoadContext(String dataStoreName, LoadContext<?> loadContext, Object entity,
                                JsonNode entityJson) {
            this.dataStoreName = dataStoreName;
            this.loadContext = loadContext;
            this.entity = entity;
            this.entityJson = entityJson;
        }

        public String getDataStoreName() {
            return dataStoreName;
        }

        public LoadContext<?> getLoadContext() {
            return loadContext;
        }

        /**
         * Returns the loaded root entity.
         */
        public Object getEntity() {
            return entity;
        }

        /**
         * Returns the JSON object the entity was read from. Changing it has no effect.
         */
        public JsonNode getEntityJson() {
            return entityJson;
        }
    }

    /**
     * Data passed to {@link #beforeCount(BeforeCountContext)}.
     */
    class BeforeCountContext {

        private final String dataStoreName;
        private final LoadContext<?> loadContext;
        private final Map<String, Object> requestParameters;

        public BeforeCountContext(String dataStoreName, LoadContext<?> loadContext,
                                  Map<String, Object> requestParameters) {
            this.dataStoreName = dataStoreName;
            this.loadContext = loadContext;
            this.requestParameters = requestParameters;
        }

        public String getDataStoreName() {
            return dataStoreName;
        }

        public LoadContext<?> getLoadContext() {
            return loadContext;
        }

        /**
         * Returns a mutable map of parameters to add to the request. All extensions called for the request share it.
         */
        public Map<String, Object> getRequestParameters() {
            return requestParameters;
        }
    }

    /**
     * Data passed to {@link #beforeSave(BeforeSaveContext)}.
     */
    class BeforeSaveContext {

        private final String dataStoreName;
        private final SaveContext saveContext;
        private final Object entity;
        private final boolean newEntity;
        private final ObjectNode entityJson;

        public BeforeSaveContext(String dataStoreName, SaveContext saveContext, Object entity, boolean newEntity,
                                 ObjectNode entityJson) {
            this.dataStoreName = dataStoreName;
            this.saveContext = saveContext;
            this.entity = entity;
            this.newEntity = newEntity;
            this.entityJson = entityJson;
        }

        public String getDataStoreName() {
            return dataStoreName;
        }

        public SaveContext getSaveContext() {
            return saveContext;
        }

        /**
         * Returns the root entity being saved.
         */
        public Object getEntity() {
            return entity;
        }

        /**
         * Returns true if the entity is going to be created, false if it is going to be updated.
         */
        public boolean isNew() {
            return newEntity;
        }

        /**
         * Returns the JSON object that will be sent to the remote service. Changes to it are sent.
         */
        public ObjectNode getEntityJson() {
            return entityJson;
        }
    }

    /**
     * Data passed to {@link #afterSave(AfterSaveContext)}.
     */
    class AfterSaveContext {

        private final String dataStoreName;
        private final SaveContext saveContext;
        private final Object entity;
        private final Object savedEntity;
        private final boolean newEntity;
        private final JsonNode savedEntityJson;

        public AfterSaveContext(String dataStoreName, SaveContext saveContext, Object entity, Object savedEntity,
                                boolean newEntity, JsonNode savedEntityJson) {
            this.dataStoreName = dataStoreName;
            this.saveContext = saveContext;
            this.entity = entity;
            this.savedEntity = savedEntity;
            this.newEntity = newEntity;
            this.savedEntityJson = savedEntityJson;
        }

        public String getDataStoreName() {
            return dataStoreName;
        }

        public SaveContext getSaveContext() {
            return saveContext;
        }

        /**
         * Returns the root entity passed to the store for saving.
         */
        public Object getEntity() {
            return entity;
        }

        /**
         * Returns the entity read from the response of the remote service. The store's save operation returns this
         * instance. See {@link RestDataStoreExtension#afterSave(AfterSaveContext)} for the instance that the caller
         * of {@code DataManager} receives.
         */
        public Object getSavedEntity() {
            return savedEntity;
        }

        /**
         * Returns true if the entity was created, false if it was updated.
         */
        public boolean isNew() {
            return newEntity;
        }

        /**
         * Returns the JSON object returned by the remote service. Changing it has no effect.
         */
        public JsonNode getSavedEntityJson() {
            return savedEntityJson;
        }
    }
}
