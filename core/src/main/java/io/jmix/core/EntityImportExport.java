/*
 * Copyright 2019 Haulmont.
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

package io.jmix.core;

import java.util.Collection;

/**
 * Bean that is used for exporting a collection of entities and importing them.
 */
public interface EntityImportExport {

    /**
     * <p>Serializes a collection of entities to JSON using {@link EntitySerialization}
     * and packs the JSON file into ZIP archive.</p> <p>Serialization is described in the {@link
     * EntitySerialization#toJson(Collection)} method documentation</p>
     *
     * @param entities a collection of entities to export
     * @return a byte array of zipped JSON file
     */
    byte[] exportEntitiesToZIP(Collection<Object> entities);

    /**
     * <p>Serializes a collection of entities to JSON using {@link EntitySerialization}
     * and packs the JSON file into ZIP archive. Before the serialization entities will be reloaded with the fetch plan
     * passed as method parameter.</p> <p>Serialization is described in the {@link EntitySerialization#toJson(Collection)}
     * method documentation</p>
     *
     * @param entities  a collection of entities to export
     * @param fetchPlan before serialization to JSON entities will be reloaded with this fetch plan
     * @return a byte array of zipped JSON file
     */
    byte[] exportEntitiesToZIP(Collection<Object> entities, FetchPlan fetchPlan);

    /**
     * <p>Serializes a collection of entities to JSON using {@link EntitySerialization}.
     * Before the serialization entities will be reloaded with the fetch plan passed as method parameter.</p>
     * <p>Serialization is described in the {@link EntitySerialization#toJson(Collection)} method documentation</p>
     *
     * @param entities  a collection of entities to export
     * @param fetchPlan before serialization to JSON entities will be reloaded with this fetch plan
     * @return a JSON string
     */
    String exportEntitiesToJSON(Collection<Object> entities, FetchPlan fetchPlan);

    /**
     * <p>Serializes a collection of entities to JSON using {@link EntitySerialization}.</p>
     * <p>Serialization is described in the {@link EntitySerialization#toJson(Collection)}
     * method documentation</p>
     *
     * @param entities a collection of entities to export
     * @return a JSON string
     */
    String exportEntitiesToJSON(Collection<Object> entities);

    /**
     * Deserializes the JSON and persists deserialized entities according to the rules, described by the {@code
     * importPlan} parameter. If the entity is not present in the database, it will be saved. Otherwise the fields of
     * the existing entity that are in the {@code importPlan} will be updated.
     *
     * @param json       JSON file containing entities
     * @param importPlan {@code EntityImportPlan} with the rules that describes how entities should be persisted.
     * @return a collection of entities that have been imported
     * @see EntityImportPlan
     */
    Collection<Object> importEntitiesFromJson(String json, EntityImportPlan importPlan);

    /**
     * Reads a zip archive that contains a JSON file, deserializes the JSON and persists deserialized entities according
     * to the rules, described by the {@code importPlan} parameter. If the entity is not present in the database, it
     * will be saved. Otherwise the fields of the existing entity that are in the {@code importPlan} will be updated.
     *
     * @param zipBytes   byte array of ZIP archive with JSON file
     * @param importPlan {@code EntityImportPlan} with the rules that describes how entities should be persisted.
     * @return a collection of entities that have been imported
     * @see EntityImportPlan
     */
    Collection<Object> importEntitiesFromZIP(byte[] zipBytes, EntityImportPlan importPlan);

    /**
     * See {@link #importEntities(Collection, EntityImportPlan, boolean)}. The current method doesn't perform bean
     * validation
     */
    Collection<Object> importEntities(Collection<Object> entities, EntityImportPlan importPlan);

    /**
     * Persists entities according to the rules, described by the {@code importPlan} parameter. If the entity is not
     * present in the database, it will be saved. Otherwise the fields of the existing entity that are in the {@code
     * importPlan} will be updated.
     * <p>
     * If the import plan contains a property for composition attribute then all composition collection members that are
     * absent in the passed entity will be removed.
     *
     * @param importPlan {@code EntityImportPlan} with the rules that describes how entities should be persisted.
     * @param validate   whether the passed entities should be validated by the bean validation mechanism before
     *                   entities are persisted
     * @return a collection of entities that have been imported
     */
    Collection<Object> importEntities(Collection<Object> entities, EntityImportPlan importPlan, boolean validate);

    /**
     * Persists entities according to the rules, described by the {@code importPlan} parameter. If the entity is not
     * present in the database, it will be saved. Otherwise the fields of the existing entity that are in the {@code
     * importPlan} will be updated.
     * <p>
     * If the import plan contains a property for composition attribute then all composition collection members that are
     * absent in the passed entity will be removed.
     *
     * @param importPlan        {@link EntityImportPlan} with the rules that describes how entities should be
     *                          persisted.
     * @param validate          whether the passed entities should be validated by the bean validation mechanism before
     *                          entities are persisted
     * @param optimisticLocking whether the passed entities versions should be validated before entities are persisted
     * @return a collection of entities that have been imported
     */
    Collection<Object> importEntities(Collection<Object> entities, EntityImportPlan importPlan, boolean validate, boolean optimisticLocking);

    Collection<Object> importEntities(Collection<Object> entities, EntityImportPlan importPlan, boolean validate, boolean optimisticLocking, boolean additionComposition);

    void importEntityIntoSaveContext(SaveContext saveContext, Object srcEntity, EntityImportPlan importPlan, boolean validate);

    void importEntityIntoSaveContext(SaveContext saveContext, Object srcEntity, EntityImportPlan importPlan, boolean validate, boolean optimisticLocking);

    void importEntityIntoSaveContext(SaveContext saveContext, Object srcEntity, EntityImportPlan importPlan, boolean validate, boolean optimisticLocking, boolean additionComposition);
}
