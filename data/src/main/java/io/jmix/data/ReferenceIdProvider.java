/*
 * Copyright 2021 Haulmont.
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

package io.jmix.data;

import io.jmix.core.FetchPlan;

import javax.annotation.Nullable;

/**
 * Enables working with references to entities without loading them from the database.
 */
public interface ReferenceIdProvider {

    /**
     * Returns an ID of directly referenced entity without loading it from DB.
     * <p>
     * If the fetchPlan does not contain the reference and {@link FetchPlan#loadPartialEntities()} is true,
     * the returned {@link RefId} will have {@link RefId#isLoaded()} = false.
     *
     * <p>Usage example:
     * <pre>
     *   ReferenceIdProvider.RefId refId = referenceIdProvider.getReferenceId(doc, "currency");
     *   if (refId.isLoaded()) {
     *       String currencyCode = (String) refId.getValue();
     *   }
     * </pre>
     *
     * @param entity   entity instance in managed state
     * @param property name of reference property
     * @return {@link RefId} instance which contains the referenced entity ID
     * @throws IllegalArgumentException if the specified property is not a reference
     * @throws IllegalStateException    if the entity is not in Managed state
     * @throws RuntimeException         if anything goes wrong when retrieving the ID
     */
    RefId getReferenceId(Object entity, String property);

    /**
     * A wrapper for the reference ID value returned by {@link #getReferenceId(Object, String)} method.
     *
     * @see #isLoaded()
     * @see #getValue()
     */
    class RefId {

        private String name;
        private final boolean loaded;
        private final Object value;

        private RefId(String name, boolean loaded, @Nullable Object value) {
            this.name = name;
            this.loaded = loaded;
            this.value = value;
        }

        public static RefId create(String name, @Nullable Object value) {
            return new RefId(name, true, value);
        }

        public static RefId createNotLoaded(String name) {
            return new RefId(name, false, true);
        }

        /**
         * Returns true if the reference ID has been loaded and can be retrieved by calling {@link #getValue()}
         */
        public boolean isLoaded() {
            return loaded;
        }

        /**
         * Returns the reference ID value (can be null) if {@link #isLoaded()} is true
         *
         * @throws IllegalStateException if {@link #isLoaded()} is false
         */
        @Nullable
        public Object getValue() {
            if (!loaded)
                throw new IllegalStateException("Property '" + name + "' has not been loaded");
            return value;
        }
    }
}
