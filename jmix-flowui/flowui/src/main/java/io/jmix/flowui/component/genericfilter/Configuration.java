/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.component.genericfilter;

import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.flowui.component.filter.FilterComponent;
import io.jmix.flowui.component.genericfilter.configuration.RunTimeConfiguration;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;

import org.springframework.lang.Nullable;

/**
 * A configuration is a set of filter components.
 */
public interface Configuration extends Comparable<Configuration> {

    /**
     * @return a {@link GenericFilter} owning the configuration
     */
    GenericFilter getOwner();

    /**
     * @return a configuration id
     */
    String getId();

    /**
     * @return a configuration name
     */
    @Nullable
    String getName();

    /**
     * Sets the name of configuration. This method is only available for
     * the {@link RunTimeConfiguration}.
     *
     * @param name a configuration name
     * @see RunTimeConfiguration
     */
    void setName(@Nullable String name);

    /**
     * @return a root element of configuration
     * @see LogicalFilterComponent
     */
    LogicalFilterComponent<?> getRootLogicalFilterComponent();

    /**
     * Sets the root element of configuration. This method is only available for
     * the {@link RunTimeConfiguration}.
     *
     * @param rootLogicalFilterComponent a root element of configuration
     * @see LogicalFilterComponent
     * @see RunTimeConfiguration
     */
    void setRootLogicalFilterComponent(LogicalFilterComponent<?> rootLogicalFilterComponent);

    /**
     * @return a {@link LogicalCondition} related to the configuration
     */
    LogicalCondition getQueryCondition();

    /**
     * @return true if the configuration is modified
     */
    boolean isModified();

    /**
     * Sets whether configuration is modified. If a filter component is modified,
     * then a remove button appears next to it.
     *
     * @param modified whether configuration is modified.
     */
    void setModified(boolean modified);

    /**
     * Returns whether the {@link FilterComponent} of configuration is modified.
     * If a filter component is modified, then a remove button appears next to it.
     *
     * @param filterComponent the filter component to check
     * @return whether the filter component of configuration is modified
     */
    boolean isFilterComponentModified(FilterComponent filterComponent);

    /**
     * Sets whether the {@link FilterComponent} of configuration is modified.
     * If a filter component is modified, then a remove button appears next to it.
     *
     * @param filterComponent a filter component
     * @param modified        whether the filter component of configuration is modified
     */
    void setFilterComponentModified(FilterComponent filterComponent, boolean modified);

    /**
     * Sets a default value of {@link FilterComponent} for the configuration by the parameter name.
     * This allows the default values to be saved and displayed in the configuration editor.
     *
     * @param parameterName a parameter name of filter component
     * @param defaultValue  a default value
     */
    void setFilterComponentDefaultValue(String parameterName, @Nullable Object defaultValue);

    /**
     * Resets a default value of {@link FilterComponent}. The default value for the filter
     * component becomes null.
     *
     * @param parameterName a parameter name of filter component
     */
    void resetFilterComponentDefaultValue(String parameterName);

    /**
     * Returns a default value of {@link FilterComponent} by parameter name.
     *
     * @param parameterName a parameter name of filter component
     * @return a default value of filter component by parameter name
     */
    @Nullable
    Object getFilterComponentDefaultValue(String parameterName);

    /**
     * Sets null as the default value for all configuration filter components.
     */
    void resetAllDefaultValues();

    /**
     * Returns whether the configuration is available for all users
     * @return true if the configuration is available for all users, otherwise false.
     */
    default boolean isAvailableForAllUsers() {
        return false;
    }

    /**
     * Sets whether the configuration is available for all users or not
     */
    default void setAvailableForAllUsers(boolean availableForAllUsers) {
    }
}
