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

package io.jmix.flowui.component.genericfilter;

import io.jmix.core.annotation.Experimental;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.filter.FilterComponent;
import io.jmix.flowui.component.filter.SingleFilterComponentBase;
import io.jmix.flowui.component.genericfilter.configuration.RunTimeConfiguration;
import io.jmix.flowui.component.logicalfilter.GroupFilter;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Fluent builder for {@link RunTimeConfiguration}.
 * <p>
 * Use this builder when you need a <em>dynamic</em> configuration whose filter components
 * can be added or removed at runtime (e.g. via the per-condition remove button).
 * <p>
 * Encapsulates all required steps:
 * <ul>
 *   <li>Creating and configuring the root {@link GroupFilter}</li>
 *   <li>Adding filter components and recording their default values in the configuration</li>
 *   <li>Marking all added components as modified so remove buttons appear immediately</li>
 *   <li>Registering the configuration via {@link GenericFilter#addConfiguration(Configuration)}</li>
 *   <li>Optionally activating the configuration via
 *       {@link GenericFilter#setCurrentConfiguration(Configuration)}</li>
 * </ul>
 * <p>
 * Obtain an instance via {@link GenericFilter#runtimeConfigurationBuilder()}:
 * <pre>{@code
 * filter.runtimeConfigurationBuilder()
 *       .id("dynamicSearch")
 *       .name("Dynamic Search")
 *       .add(nameFilter)
 *       .add(statusFilter, "NEW")
 *       .makeCurrent()
 *       .buildAndRegister();
 * }</pre>
 */
@Experimental
public class RunTimeConfigurationBuilder {

    protected final GenericFilter filter;
    protected final UiComponents uiComponents;

    protected String id;
    protected String name;
    protected LogicalFilterComponent.Operation operation = LogicalFilterComponent.Operation.AND;
    protected boolean makeCurrent = false;
    protected boolean allowDeletion = false;
    protected boolean built = false;

    protected final List<ComponentEntry> entries = new ArrayList<>();

    protected RunTimeConfigurationBuilder(GenericFilter filter, UiComponents uiComponents) {
        this.filter = filter;
        this.uiComponents = uiComponents;
    }

    /**
     * Sets the configuration id. Required.
     *
     * @param id unique configuration identifier within this filter
     */
    public RunTimeConfigurationBuilder id(String id) {
        checkNotNullArgument(id, "id must not be null");
        this.id = id;
        return this;
    }

    /**
     * Sets the configuration display name.
     *
     * @param name display name shown in the configuration selector
     */
    public RunTimeConfigurationBuilder name(@Nullable String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the logical operation of the root filter component. Defaults to {@code AND}.
     *
     * @param operation logical operation
     */
    public RunTimeConfigurationBuilder operation(LogicalFilterComponent.Operation operation) {
        checkNotNullArgument(operation, "operation must not be null");
        this.operation = operation;
        return this;
    }

    /**
     * Adds a filter component using the component's current value (if any) as the default.
     *
     * @param filterComponent filter component to add
     */
    public RunTimeConfigurationBuilder add(FilterComponent filterComponent) {
        checkNotNullArgument(filterComponent, "filterComponent must not be null");
        entries.add(new ComponentEntry(filterComponent, null, false));
        return this;
    }

    /**
     * Adds several filter components at once, each using its current value (if any) as the default.
     *
     * @param filterComponents filter components to add
     */
    public RunTimeConfigurationBuilder addAll(FilterComponent... filterComponents) {
        checkNotNullArgument(filterComponents, "filterComponents must not be null");
        for (FilterComponent filterComponent : filterComponents) {
            add(filterComponent);
        }
        return this;
    }

    /**
     * Adds a filter component, overriding its default value.
     * <p>
     * The type parameter {@code V} ensures the {@code defaultValue} matches the
     * component's own value type at compile time.
     *
     * @param filterComponent filter component to add
     * @param defaultValue    value to apply and record as the configuration default
     * @param <V>             the value type of the filter component
     */
    public <V> RunTimeConfigurationBuilder add(SingleFilterComponentBase<V> filterComponent,
                                               @Nullable V defaultValue) {
        checkNotNullArgument(filterComponent, "filterComponent must not be null");
        entries.add(new ComponentEntry(filterComponent, defaultValue, true));
        return this;
    }

    /**
     * Makes this configuration the current (active) one immediately after it is registered.
     * <p>
     * This is not the persistent <em>default</em> configuration marker (set via the
     * {@code genericFilter_makeDefault} action and stored per user): it simply activates this
     * configuration now, equivalent to {@link GenericFilter#setCurrentConfiguration(Configuration)}.
     */
    public RunTimeConfigurationBuilder makeCurrent() {
        this.makeCurrent = true;
        return this;
    }

    /**
     * Controls whether the user can delete this configuration through the UI.
     * <p>
     * By default, configurations created via this builder are protected from user deletion
     * ({@link RunTimeConfiguration#setProtectedFromUserDeletion(boolean)}).
     *
     * @param allowDeletion {@code true} to allow the user to remove the configuration,
     *                      {@code false} (default) to protect it
     */
    public RunTimeConfigurationBuilder allowDeletion(boolean allowDeletion) {
        this.allowDeletion = allowDeletion;
        return this;
    }

    /**
     * Allows the user to delete this configuration through the UI.
     * Convenience alias for {@code allowDeletion(true)}.
     */
    public RunTimeConfigurationBuilder allowDeletion() {
        return allowDeletion(true);
    }

    /**
     * Builds the {@link RunTimeConfiguration}, registers it with the filter, and
     * optionally activates it.
     * <p>
     * Automatically:
     * <ul>
     *   <li>Creates the root {@link GroupFilter} with {@code setConditionModificationDelegated(true)}
     *       and {@code setDataLoader(filter.getDataLoader())}</li>
     *   <li>Adds each filter component to the root</li>
     *   <li>Calls {@code setFilterComponentDefaultValue} for every component with a value</li>
     *   <li>Registers the configuration via {@link GenericFilter#addConfiguration(Configuration)}, and
     *       activates it via {@link GenericFilter#setCurrentConfiguration(Configuration)} if
     *       {@link #makeCurrent()} was requested</li>
     * </ul>
     *
     * @return the newly created and registered {@link RunTimeConfiguration}
     * @throws IllegalStateException if this builder instance has already been used, if {@code id}
     *         was not set, if a configuration with the same id is already registered in the filter,
     *         or if the filter has no DataLoader
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public RunTimeConfiguration buildAndRegister() {
        if (built) {
            throw new IllegalStateException(
                    "RunTimeConfigurationBuilder.buildAndRegister() must not be called more than once; create a new instance per configuration");
        }
        if (id == null) {
            throw new IllegalStateException(
                    "RunTimeConfigurationBuilder: 'id' is required — call .id(\"...\") before .buildAndRegister()");
        }
        if (id.equals(filter.getEmptyConfiguration().getId())) {
            throw new IllegalStateException(String.format(
                    "RunTimeConfigurationBuilder: 'id' must not be the reserved empty-configuration id '%s'", id));
        }
        if (filter.getConfiguration(id) != null) {
            throw new IllegalStateException(String.format(
                    "RunTimeConfigurationBuilder: a configuration with id '%s' is already registered in this filter", id));
        }
        if (filter.getDataLoader() == null) {
            throw new IllegalStateException(
                    "RunTimeConfigurationBuilder: the filter has no DataLoader; set it before building a configuration");
        }

        // Build the root GroupFilter — mirrors GenericFilter.createConfigurationRootLogicalFilterComponent()
        GroupFilter root = uiComponents.create(GroupFilter.class);
        root.setConditionModificationDelegated(true);
        root.setOperation(operation);
        root.setOperationTextVisible(false);
        root.setAutoApply(filter.isAutoApply());
        root.setDataLoader(filter.getDataLoader());

        RunTimeConfiguration config = new RunTimeConfiguration(id, root, filter);
        config.setName(name);

        for (ComponentEntry entry : entries) {
            FilterComponent fc = entry.filterComponent;

            if (entry.overrideDefault && fc instanceof SingleFilterComponentBase sfc) {
                sfc.setValue(entry.defaultValue);
            }

            root.add(fc);

            // Persist the default value for reset/restore behaviour.
            // Skip components without a parameter name (e.g. void JpqlFilter with Void parameterClass).
            if (fc instanceof SingleFilterComponentBase<?> sfc) {
                String paramName = sfc.getParameterName();
                Object valueToStore = entry.overrideDefault ? entry.defaultValue : sfc.getValue();
                if (paramName != null && valueToStore != null) {
                    config.setFilterComponentDefaultValue(paramName, valueToStore);
                }
            }
        }

        // All components are added — mark them as modified so remove buttons appear.
        config.setModified(true);
        config.setProtectedFromUserDeletion(!allowDeletion);

        filter.addConfiguration(config);
        if (makeCurrent) {
            filter.setCurrentConfiguration(config);
        }

        built = true;
        return config;
    }

    protected static class ComponentEntry {
        protected final FilterComponent filterComponent;
        protected final Object defaultValue;
        protected final boolean overrideDefault;

        protected ComponentEntry(FilterComponent filterComponent, @Nullable Object defaultValue, boolean overrideDefault) {
            this.filterComponent = filterComponent;
            this.defaultValue = defaultValue;
            this.overrideDefault = overrideDefault;
        }
    }
}
