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

package io.jmix.datatoolsui;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

import javax.annotation.Nullable;
import java.util.List;

@ConfigurationProperties(prefix = "jmix.datatools")
@ConstructorBinding
public class DatatoolsUiProperties {

    /**
     * Whether the controls to display SQL-scripts for creating / updating / retrieving an entity instance in the Entity
     * Information window is visible
     */
    boolean entityInfoScriptsEnabled;

    /**
     * Properties of entity inspector browse screen.
     */
    EntityInspectorBrowse entityInspectorBrowse;

    public DatatoolsUiProperties(@DefaultValue("true") boolean entityInfoScriptsEnabled,
                                 @DefaultValue EntityInspectorBrowse entityInspectorBrowse) {
        this.entityInfoScriptsEnabled = entityInfoScriptsEnabled;
        this.entityInspectorBrowse = entityInspectorBrowse;
    }

    /**
     * @see #entityInfoScriptsEnabled
     */
    public boolean isEntityInfoScriptsEnabled() {
        return entityInfoScriptsEnabled;
    }

    /**
     * @see #entityInspectorBrowse
     */
    public EntityInspectorBrowse getEntityInspectorBrowse() {
        return entityInspectorBrowse;
    }

    public static class EntityInspectorBrowse {

        /**
         * Options for items per page ComboBox in the pagination.
         */
        List<Integer> itemsPerPageOptions;

        /**
         * Defines whether items per page ComboBox in the pagination should be visible.
         */
        boolean itemsPerPageVisible;

        /**
         * Defines whether unlimited option in the pagination's items per page ComboBox should be visible.
         */
        boolean itemsPerPageUnlimitedOptionVisible;

        public EntityInspectorBrowse(@Nullable List<Integer> itemsPerPageOptions,
                                     @DefaultValue("true") boolean itemsPerPageVisible,
                                     @DefaultValue("true") boolean itemsPerPageUnlimitedOptionVisible) {
            this.itemsPerPageOptions = itemsPerPageOptions;
            this.itemsPerPageVisible = itemsPerPageVisible;
            this.itemsPerPageUnlimitedOptionVisible = itemsPerPageUnlimitedOptionVisible;
        }

        /**
         * @see #itemsPerPageOptions
         */
        public List<Integer> getItemsPerPageOptions() {
            return itemsPerPageOptions;
        }

        /**
         * @see #itemsPerPageVisible
         */
        public boolean isItemsPerPageVisible() {
            return itemsPerPageVisible;
        }

        /**
         * @see #itemsPerPageUnlimitedOptionVisible
         */
        public boolean isItemsPerPageUnlimitedOptionVisible() {
            return itemsPerPageUnlimitedOptionVisible;
        }
    }
}
