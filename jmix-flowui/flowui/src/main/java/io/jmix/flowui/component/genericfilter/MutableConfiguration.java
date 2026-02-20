/*
 * Copyright 2024 Haulmont.
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

import io.jmix.flowui.component.filter.FilterComponent;
import io.jmix.flowui.component.genericfilter.configuration.DesignTimeConfiguration;
import io.jmix.flowui.component.genericfilter.configuration.RunTimeConfiguration;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import org.springframework.lang.Nullable;

/**
 * A mutable extension of {@link Configuration} that supports all modifier operations.
 * <p>
 * {@link RunTimeConfiguration} implements this interface; {@link DesignTimeConfiguration}
 * only implements the read-only {@link Configuration} and throws
 * {@link UnsupportedOperationException} for methods declared here.
 * <p>
 * Use this interface as the declared type when you need to call mutating methods on a
 * configuration, so the compiler can catch misuse of {@link DesignTimeConfiguration} at
 * compile time instead of at runtime:
 * <pre>{@code
 * MutableConfiguration config = filter.runtimeConfigurationBuilder()
 *         .id("myConfig")
 *         .buildAndRegister();
 * config.setModified(true);   // safe — compiler guarantees this is RunTimeConfiguration
 * }</pre>
 *
 * @see Configuration
 * @see RunTimeConfiguration
 */
public interface MutableConfiguration extends Configuration {

    /**
     * Sets the name of configuration.
     *
     * @param name a configuration name
     */
    void setName(@Nullable String name);

    /**
     * Sets the root logical filter component of configuration.
     *
     * @param rootLogicalFilterComponent a root element of configuration
     */
    void setRootLogicalFilterComponent(LogicalFilterComponent<?> rootLogicalFilterComponent);

    /**
     * Sets whether the configuration is modified.
     * <p>
     * If a filter component is modified, a remove button appears next to it.
     * <p>
     * <b>Note:</b> this method iterates over components that are already added to the root
     * at the time of the call. For automatic tracking of added/removed components, see
     * {@link RunTimeConfiguration} which subscribes to
     * {@link LogicalFilterComponent.FilterComponentsChangeEvent} from its root component.
     *
     * @param modified whether configuration is modified
     */
    void setModified(boolean modified);

    /**
     * Sets whether the given filter component of configuration is modified.
     * If a filter component is modified, a remove button appears next to it.
     *
     * @param filterComponent a filter component
     * @param modified        whether the filter component of configuration is modified
     */
    void setFilterComponentModified(FilterComponent filterComponent, boolean modified);

    /**
     * Sets a default value of the given filter component for the configuration.
     * This allows the default values to be saved and displayed in the configuration editor.
     *
     * @param parameterName a parameter name of filter component
     * @param defaultValue  a default value
     */
    void setFilterComponentDefaultValue(String parameterName, @Nullable Object defaultValue);

    /**
     * Resets the default value of the filter component identified by the parameter name.
     *
     * @param parameterName a parameter name of filter component
     */
    void resetFilterComponentDefaultValue(String parameterName);

    /**
     * Sets null as the default value for all configuration filter components.
     */
    void resetAllDefaultValues();

    /**
     * Sets whether the configuration is available for all users.
     *
     * @param availableForAllUsers whether the configuration is available for all users
     */
    void setAvailableForAllUsers(boolean availableForAllUsers);
}
