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
import io.jmix.flowui.component.genericfilter.configuration.DesignTimeConfiguration;
import io.jmix.flowui.component.genericfilter.configuration.RunTimeConfiguration;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;

import org.jspecify.annotations.Nullable;

/**
 * A configuration is a set of filter components.
 *
 * <p><b>Important:</b> several mutating methods declared in this interface
 * ({@code set*} / {@code reset*}) throw {@link UnsupportedOperationException}
 * when called on a {@link DesignTimeConfiguration}.  Those methods are marked
 * {@link Deprecated} and will be <b>removed from this interface in Jmix 3.0</b>
 * — they will exist only in {@link MutableConfiguration}, which is implemented
 * exclusively by {@link RunTimeConfiguration} and guarantees that every
 * mutating method works without throwing.
 *
 * <p>Migration path:
 * <ul>
 *   <li>Declare variables / parameters as {@link MutableConfiguration} instead of
 *       {@code Configuration} when you need to call mutating methods.  This gives a
 *       compile-time guarantee and removes the deprecation warning.</li>
 *   <li>Use {@code instanceof MutableConfiguration} checks where the type of the
 *       configuration is not statically known.</li>
 * </ul>
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
     * Sets the name of configuration.
     *
     * @param name a configuration name
     * @throws UnsupportedOperationException when called on {@link DesignTimeConfiguration}
     * @deprecated since 2.8, for removal in 3.0.
     *             This method will be removed from {@link Configuration} and kept only in
     *             {@link MutableConfiguration}.  Declare the variable as
     *             {@link MutableConfiguration} to call this method without a warning and
     *             without risk of {@link UnsupportedOperationException}.
     * @see MutableConfiguration
     * @see RunTimeConfiguration
     */
    @Deprecated(since = "2.8", forRemoval = true)
    void setName(@Nullable String name);

    /**
     * @return a root element of configuration
     * @see LogicalFilterComponent
     */
    LogicalFilterComponent<?> getRootLogicalFilterComponent();

    /**
     * Sets the root element of configuration.
     *
     * @param rootLogicalFilterComponent a root element of configuration
     * @throws UnsupportedOperationException when called on {@link DesignTimeConfiguration}
     * @deprecated since 2.8, for removal in 3.0.
     *             This method will be removed from {@link Configuration} and kept only in
     *             {@link MutableConfiguration}.  Declare the variable as
     *             {@link MutableConfiguration} to call this method without a warning and
     *             without risk of {@link UnsupportedOperationException}.
     * @see MutableConfiguration
     * @see LogicalFilterComponent
     * @see RunTimeConfiguration
     */
    @Deprecated(since = "2.8", forRemoval = true)
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
     * @param modified whether configuration is modified
     * @throws UnsupportedOperationException when called on {@link DesignTimeConfiguration}
     * @deprecated since 2.8, for removal in 3.0.
     *             This method will be removed from {@link Configuration} and kept only in
     *             {@link MutableConfiguration}.  Declare the variable as
     *             {@link MutableConfiguration} to call this method without a warning and
     *             without risk of {@link UnsupportedOperationException}.
     * @see MutableConfiguration
     */
    @Deprecated(since = "2.8", forRemoval = true)
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
     * @throws UnsupportedOperationException when called on {@link DesignTimeConfiguration}
     * @deprecated since 2.8, for removal in 3.0.
     *             This method will be removed from {@link Configuration} and kept only in
     *             {@link MutableConfiguration}.  Declare the variable as
     *             {@link MutableConfiguration} to call this method without a warning and
     *             without risk of {@link UnsupportedOperationException}.
     * @see MutableConfiguration
     */
    @Deprecated(since = "2.8", forRemoval = true)
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
     * @throws UnsupportedOperationException when called on {@link DesignTimeConfiguration}
     * @deprecated since 2.8, for removal in 3.0.
     *             This method will be removed from {@link Configuration} and kept only in
     *             {@link MutableConfiguration}.  Declare the variable as
     *             {@link MutableConfiguration} to call this method without a warning and
     *             without risk of {@link UnsupportedOperationException}.
     * @see MutableConfiguration
     */
    @Deprecated(since = "2.8", forRemoval = true)
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
     *
     * @throws UnsupportedOperationException when called on {@link DesignTimeConfiguration}
     * @deprecated since 2.8, for removal in 3.0.
     *             This method will be removed from {@link Configuration} and kept only in
     *             {@link MutableConfiguration}.  Declare the variable as
     *             {@link MutableConfiguration} to call this method without a warning and
     *             without risk of {@link UnsupportedOperationException}.
     * @see MutableConfiguration
     */
    @Deprecated(since = "2.8", forRemoval = true)
    void resetAllDefaultValues();

    /**
     * Returns whether the configuration is available for all users
     *
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
