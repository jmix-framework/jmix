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

package io.jmix.ui.component;

import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.ui.UiProperties;
import io.jmix.ui.app.filter.condition.AddConditionScreen;
import io.jmix.ui.component.filter.configuration.DesignTimeConfiguration;
import io.jmix.ui.component.filter.configuration.RunTimeConfiguration;
import io.jmix.ui.model.DataLoader;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

/**
 * Generic filter component.
 */
public interface Filter extends Component, Component.BelongToFrame, Component.HasDescription, Component.HasCaption,
        Component.HasIcon, HasHtmlCaption, HasHtmlDescription, HasContextHelp, HasHtmlSanitizer, Collapsable,
        ActionsHolder {

    String NAME = "filter";

    /**
     * Returns the number of columns to be displayed on one row.
     * The default value is taken from {@link UiProperties#getGenericFilterColumnsCount()}.
     *
     * @return the number of columns to be displayed on one row
     */
    int getColumnsCount();

    /**
     * Sets the number of columns to be displayed on one row.
     * The default value is taken from {@link UiProperties#getGenericFilterColumnsCount()}.
     *
     * @param columnsCount the number of columns to be displayed on one row
     */
    void setColumnsCount(int columnsCount);

    /**
     * @return a properties filter predicate
     */
    @Nullable
    Predicate<MetaPropertyPath> getPropertiesFilterPredicate();

    /**
     * Sets a predicate that tests whether a property with the given path should be
     * available for filtering.
     *
     * @param propertiesFilterPredicate a predicate to set
     */
    void setPropertiesFilterPredicate(@Nullable Predicate<MetaPropertyPath> propertiesFilterPredicate);

    /**
     * Adds a predicate to the current properties filter predicate. The result predicate
     * is a composed predicate that represents a short-circuiting logical AND of given
     * predicate and current properties filter predicate.
     *
     * @param propertiesFilterPredicate a predicate to add
     */
    void addPropertiesFilterPredicate(Predicate<MetaPropertyPath> propertiesFilterPredicate);

    /**
     * @return a {@link DataLoader} related to the filter
     */
    DataLoader getDataLoader();

    /**
     * Sets a {@link DataLoader} related to the filter.
     *
     * @param dataLoader a {@link DataLoader} to set
     */
    void setDataLoader(DataLoader dataLoader);

    /**
     * @return {@code true} if the filter should be automatically applied to
     * the {@link DataLoader} when the value component value is changed
     */
    boolean isAutoApply();

    /**
     * Sets whether the filter should be automatically applied to the
     * {@link DataLoader} when the value component value is changed.
     *
     * @param autoApply {@code true} if the filter should be automatically
     *                  applied to the {@link DataLoader} when the value
     *                  component value is changed
     */
    void setAutoApply(boolean autoApply);

    /**
     * Adds design-time configuration with given code and caption. A configuration is a set
     * of {@link FilterComponent}s. The configuration does not store a reference to all
     * components, but stores a reference only to the root element {@link LogicalFilterComponent}
     * from which the rest {@link FilterComponent}s can be obtained. The root
     * {@link LogicalFilterComponent} is generated with a {@link LogicalFilterComponent.Operation#AND}
     * operation.
     * <p>
     * The configuration defined in XML is a {@link DesignTimeConfiguration}.
     *
     * @param code    a code. Must be unique within this filter
     * @param caption a caption of configuration
     * @return {@link DesignTimeConfiguration}
     * @see DesignTimeConfiguration
     * @see LogicalFilterComponent
     */
    Configuration addConfiguration(String code, @Nullable String caption);

    /**
     * Adds design-time configuration with given code and caption. A configuration is a set
     * of {@link FilterComponent}s. The configuration does not store a reference to all
     * components, but stores a reference only to the root element {@link LogicalFilterComponent}
     * from which the rest {@link FilterComponent}s can be obtained. The root
     * {@link LogicalFilterComponent} is generated with a given operation.
     * <p>
     * The configuration defined in XML is a {@link DesignTimeConfiguration}.
     *
     * @param code          a code. Must be unique within this filter
     * @param caption       a caption of configuration
     * @param rootOperation an operation of root {@link LogicalFilterComponent}
     * @return {@link DesignTimeConfiguration}
     * @see DesignTimeConfiguration
     * @see LogicalFilterComponent
     */
    Configuration addConfiguration(String code, @Nullable String caption,
                                   LogicalFilterComponent.Operation rootOperation);

    /**
     * Adds a configuration to the filter.
     *
     * @param configuration configuration to add
     * @see DesignTimeConfiguration
     * @see RunTimeConfiguration
     */
    void addConfiguration(Configuration configuration);

    /**
     * Removes a configuration from filter.
     *
     * @param configuration configuration to remove
     */
    void removeConfiguration(Configuration configuration);

    /**
     * Sets the given configuration as current and displays filter components from the current
     * configuration.
     *
     * @param currentConfiguration a configuration
     */
    void setCurrentConfiguration(Configuration currentConfiguration);

    /**
     * Gets the current configuration that is currently displayed inside the filter.
     *
     * @return a current configuration
     */
    Configuration getCurrentConfiguration();

    /**
     * Gets a configuration by code.
     *
     * @param code the configuration code
     * @return the configuration of {@code null} if not found
     */
    @Nullable
    Configuration getConfiguration(String code);

    /**
     * Gets an empty configuration that is used when the user has not selected any of the existing
     * configurations.
     *
     * @return an empty configuration
     */
    Configuration getEmptyConfiguration();

    /**
     * @return a list of all configurations related to the filter
     */
    List<Configuration> getConfigurations();

    /**
     * Adds a condition to the filter. A condition is a {@link FilterComponent} that is not initially
     * added to any of the configurations, but the user can select this component in the
     * {@link AddConditionScreen} in the {@code Conditions} section and add it to the
     * {@link RunTimeConfiguration}.
     *
     * @param filterComponent a filter component to add to conditions
     * @see FilterComponent
     * @see AddConditionScreen
     * @see RunTimeConfiguration
     */
    void addCondition(FilterComponent filterComponent);

    /**
     * @return a list of all conditions related to the filter
     */
    List<FilterComponent> getConditions();

    /**
     * Removes a condition from filter.
     *
     * @param filterComponent a filter component to remove from conditions
     */
    void removeCondition(FilterComponent filterComponent);

    /**
     * A configuration is a set of filter components that has a caption and code.
     */
    interface Configuration {

        /**
         * @return a {@link Filter} owning the configuration
         */
        Filter getOwner();

        /**
         * @return a configuration code
         */
        String getCode();

        /**
         * @return a configuration caption
         */
        String getCaption();

        /**
         * Sets the caption of configuration. This method is only available for
         * the {@link RunTimeConfiguration}.
         *
         * @param caption a caption
         * @see RunTimeConfiguration
         */
        void setCaption(@Nullable String caption);

        /**
         * @return a root element of configuration
         * @see LogicalFilterComponent
         */
        LogicalFilterComponent getRootLogicalFilterComponent();

        /**
         * Sets the root element of configuration. This method is only available for
         * the {@link RunTimeConfiguration}.
         *
         * @param rootLogicalFilterComponent a root element of configuration
         * @see LogicalFilterComponent
         * @see RunTimeConfiguration
         */
        void setRootLogicalFilterComponent(LogicalFilterComponent rootLogicalFilterComponent);

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
        boolean isModified(FilterComponent filterComponent);

        /**
         * Sets whether the {@link FilterComponent} of configuration is modified.
         * If a filter component is modified, then a remove button appears next to it.
         *
         * @param filterComponent a filter component
         * @param modified        whether the filter component of configuration is modified
         */
        void setModified(FilterComponent filterComponent, boolean modified);
    }
}
