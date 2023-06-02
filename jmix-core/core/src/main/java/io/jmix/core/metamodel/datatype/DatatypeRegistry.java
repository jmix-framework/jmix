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

package io.jmix.core.metamodel.datatype;

import org.springframework.lang.Nullable;
import java.util.Optional;
import java.util.Set;

/**
 * Registry for {@link Datatype}s
 */
public interface DatatypeRegistry {

    /**
     * Get Datatype instance by its unique id
     * @return Datatype instance
     * @throws IllegalArgumentException if no datatype with the given name found
     */
    Datatype<?> get(String id);

    /**
     * Get Datatype instance by id.
     * @return Datatype instance or null if not found
     */
    @Nullable
    Datatype<?> find(String id);

    /**
     * Get Datatype instance by the corresponding Java class. This method tries to find matching supertype too.
     * @return Datatype instance or null if not found
     */
    @Nullable
    <T> Datatype<T> find(Class<T> javaClass);

    /**
     * Get Datatype instance by the corresponding Java class. This method tries to find matching supertype too.
     * @return Datatype instance
     * @throws IllegalArgumentException if no datatype suitable for the given type found
     */
    <T> Datatype<T> get(Class<T> javaClass);

    /**
     * Returns an ID of the given datatype in the registry.
     * @throws IllegalArgumentException if the datatype is not registered
     */
    String getId(Datatype<?> datatype);

    /**
     * @return the ID of the given datatype wrapped in {@link Optional} if it found in the registry,
     * otherwise an empty {@link Optional}.
     */
    Optional<String> getIdOptional(Datatype<?> datatype);

    /**
     * Returns an ID of a first datatype handling the given Java class.
     * @throws IllegalArgumentException if no datatypes handle the given Java class
     */
    String getIdByJavaClass(Class<?> javaClass);

    /**
     * @return the ID of a first datatype handling the given Java class wrapped in
     * {@link Optional} if it found, otherwise an empty {@link Optional}.
     */
    Optional<String> getIdByJavaClassOptional(Class<?> javaClass);

    /**
     * @return all registered datatype identifiers.
     */
    Set<String> getIds();

    /**
     * Register a datatype instance
     * @param datatype              datatype instance
     * @param id                    unique registration id
     * @param defaultForJavaClass   true if the datatype should be default for a Java class handled by this datatype
     */
    void register(Datatype<?> datatype, String id, boolean defaultForJavaClass);
}
