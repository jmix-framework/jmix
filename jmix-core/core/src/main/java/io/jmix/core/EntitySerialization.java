/*
 * Copyright 2020 Haulmont.
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

import io.jmix.core.metamodel.model.MetaClass;

import org.springframework.lang.Nullable;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * Class that is used for serialization and deserialization of entities to JSON.
 */
public interface EntitySerialization {

    String ENTITY_NAME_PROP = "_entityName";
    String INSTANCE_NAME_PROP = "_instanceName";

    /**
     * Serializes a single entity to the JSON object graph.
     * <p>
     * If the {@code fetchPlan} parameter is null then all loaded entity properties will be presented in JSON, otherwise only
     * loaded properties that are in the fetchPlan will be in the JSON object.
     * <p>
     * The {@code options} parameter specify some additional options for the serialization process. For example,
     * repeated entities may be replaced with the object with the only "id" property, making the result JSON more
     * compact. See {@link EntitySerializationOption} for details.
     * <p>
     * Additionally, an "_entityName" property is added to the JSON objects that represent an entity.
     *
     * @param entity    an entity to be serialized
     * @param fetchPlan a fetch plan that defines which entity properties should be added to the result JSON object
     * @param options   options specifying how an entity should be serialized
     * @return a string that represents a JSON object
     */
    String toJson(Object entity,
                  @Nullable FetchPlan fetchPlan,
                  EntitySerializationOption... options);

    /**
     * Serializes a collection of entities to the JSON array. Method works like the {@link #toJson(Object, FetchPlan,
     * EntitySerializationOption...)}, but return a JSON array as a result.
     *
     * @param entities  a list of entities to be serialized
     * @param fetchPlan a fetch plan that defines which entity properties should be added to the result JSON object
     * @param options   options specifying how an entity should be serialized
     * @return a string that represents a JSON array of objects.
     */
    String toJson(Collection<?> entities,
                  @Nullable FetchPlan fetchPlan,
                  EntitySerializationOption... options);

    /**
     * An overloaded version of the {@link #toJson(Object, FetchPlan, EntitySerializationOption...)} method with a null
     * {@code fetchPlan} parameter and with no serialization options.
     *
     * @param entity an entity to be serialized
     * @return a string that represents a JSON object
     */
    String toJson(Object entity);

    /**
     * An overloaded version of the {@link #toJson(Collection, FetchPlan, EntitySerializationOption...)} method with a null
     * {@code fetchPlan} parameter and with no serialization options.
     *
     * @param entities a collection of entities to be serialized
     * @return a string that represent a JSON array which contains entity objects
     */
    String toJson(Collection<?> entities);

    /**
     * Serializes any custom POJO or collection of POJOs or JSON. If some field in POJO is an entity then this field
     * will be serialized according to entity serialization rules. Date fields are serialized according to the format of
     * the {@link io.jmix.core.metamodel.datatype.impl.DateTimeDatatype}.
     *
     * @param object  any POJO or collection of POJOs
     * @param options options specifying how a JSON object graph for fields with type 'Entity' will be serialized
     * @return a string that represents a JSON object or JSON array
     */
    String objectToJson(Object object, EntitySerializationOption... options);

    /**
     * Deserializes a JSON object to the entity.
     * <p>
     * The {@code metaClass} parameter defines a result entity metaClass. It is optional. It must be defined if the JSON
     * object doesn't contain an "_entityName" property.
     * <p>
     * An entity may be serialized to the JSON in slightly different formats. The format is defined by the {@code
     * options} parameter. See {@link EntitySerializationOption} for details.
     *
     * @param json      a string that represents a JSON object
     * @param metaClass a metaClass of the entity that will be created
     * @param options   options specifying how a JSON object graph was serialized
     * @return an entity
     */
    <T> T entityFromJson(String json, @Nullable MetaClass metaClass,
                         EntitySerializationOption... options);

    /**
     * Deserializes a JSON array of objects to entities collection
     *
     * @param json      a string that represents a JSON array of objects
     * @param metaClass a metaClass of the entities that will be created
     * @param options   options specifying how a JSON object graph was serialized
     * @return an entities collection
     */
    <T> Collection<T> entitiesCollectionFromJson(String json,
                                                 @Nullable MetaClass metaClass,
                                                 EntitySerializationOption... options);

    /**
     * Deserializes an object of any class from JSON. Date fields are deserialized according to the {@link
     * io.jmix.core.metamodel.datatype.impl.DateTimeDatatype} format, entities object and fields of Entity type are
     * deserialized like in the {@link #entityFromJson(String, MetaClass, EntitySerializationOption...)} method
     *
     * @param json    a string that represents an object
     * @param type    type of the object
     * @param options options specifying how a JSON object graph was serialized
     * @return deserialized object
     */
    <T> T objectFromJson(String json, Type type, EntitySerializationOption... options);
}
