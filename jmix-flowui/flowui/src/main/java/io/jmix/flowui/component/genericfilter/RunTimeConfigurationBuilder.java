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

import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.filter.FilterComponent;
import io.jmix.flowui.component.filter.SingleFilterComponentBase;
import io.jmix.flowui.component.genericfilter.configuration.RunTimeConfiguration;
import io.jmix.flowui.component.logicalfilter.GroupFilter;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import org.springframework.lang.Nullable;

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
 *   <li>Registering the configuration via {@link GenericFilter#addConfiguration(Configuration)}</li>
 *   <li>Optionally marking all components as modified (so remove buttons appear immediately)</li>
 *   <li>Optionally activating the configuration via
 *       {@link GenericFilter#setCurrentConfiguration(Configuration)}</li>
 * </ul>
 * <p>
 * Note: added components are <em>automatically</em> marked as modified by
 * {@link RunTimeConfiguration}'s internal change listener, so an explicit call to
 * {@link #markAllModified()} is usually not necessary unless components were added before
 * the listener was registered.
 * <p>
 * Obtain an instance via {@link GenericFilter#runtimeConfigurationBuilder()}:
 * <pre>{@code
 * filter.runtimeConfigurationBuilder()
 *       .id("dynamicSearch")
 *       .name("Dynamic Search")
 *       .add(nameFilter)
 *       .add(statusFilter, "NEW")
 *       .asDefault()
 *       .buildAndRegister();
 * }</pre>
 */
public class RunTimeConfigurationBuilder {

    protected final GenericFilter filter;
    protected final UiComponents uiComponents;

    protected String id;
    protected String name;
    protected LogicalFilterComponent.Operation operation = LogicalFilterComponent.Operation.AND;
    protected boolean makeDefault = false;
    protected boolean markModified = false;

    protected final List<ComponentEntry> entries = new ArrayList<>();

    RunTimeConfigurationBuilder(GenericFilter filter, UiComponents uiComponents) {
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
     * Adds a filter component, overriding its default value.
     *
     * @param filterComponent filter component to add
     * @param defaultValue    value to apply and record as the configuration default
     */
    public RunTimeConfigurationBuilder add(FilterComponent filterComponent, @Nullable Object defaultValue) {
        checkNotNullArgument(filterComponent, "filterComponent must not be null");
        entries.add(new ComponentEntry(filterComponent, defaultValue, true));
        return this;
    }

    /**
     * Requests that all added filter components be explicitly marked as modified
     * (making remove buttons visible) after the configuration is built.
     * <p>
     * Since {@link RunTimeConfiguration} already auto-tracks modifications via its internal
     * change listener, this call is optional. It is provided as an explicit opt-in for
     * scenarios where the caller wants to be certain irrespective of listener timing.
     */
    public RunTimeConfigurationBuilder markAllModified() {
        this.markModified = true;
        return this;
    }

    /**
     * Marks this configuration as the default (currently active) configuration.
     */
    public RunTimeConfigurationBuilder asDefault() {
        this.makeDefault = true;
        return this;
    }

    /**
     * Builds the {@link RunTimeConfiguration}, registers it with the filter, and
     * optionally activates it.
     * <p>
     * Automatically:
     * <ul>
     *   <li>Creates the root {@link GroupFilter} with {@code setConditionModificationDelegated(true)}
     *       and {@code setDataLoader(filter.getDataLoader())}</li>
     *   <li>Adds each filter component to the root (triggering the auto-tracking listener)</li>
     *   <li>Calls {@code setFilterComponentDefaultValue} for every component with a value</li>
     *   <li>Calls {@link GenericFilter#addAndSetCurrentConfiguration(Configuration)} if
     *       {@link #asDefault()} was requested, or {@link GenericFilter#addConfiguration(Configuration)}
     *       otherwise</li>
     * </ul>
     *
     * @return the newly created and registered {@link RunTimeConfiguration}
     * @throws IllegalStateException if {@code id} was not set
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public RunTimeConfiguration buildAndRegister() {
        if (id == null) {
            throw new IllegalStateException(
                    "RunTimeConfigurationBuilder: 'id' is required — call .id(\"...\") before .buildAndRegister()");
        }

        // Build the root GroupFilter — mirrors GenericFilter.createConfigurationRootLogicalFilterComponent()
        GroupFilter root = uiComponents.create(GroupFilter.class);
        root.setConditionModificationDelegated(true);
        root.setOperation(operation);
        root.setOperationTextVisible(false);
        if (filter.getDataLoader() != null) {
            root.setDataLoader(filter.getDataLoader());
            root.setAutoApply(filter.isAutoApply());
        }

        RunTimeConfiguration config = new RunTimeConfiguration(id, root, filter);
        config.setName(name);

        for (ComponentEntry entry : entries) {
            FilterComponent fc = entry.filterComponent;

            if (entry.overrideDefault && fc instanceof SingleFilterComponentBase<?> sfc) {
                ((SingleFilterComponentBase) sfc).setValue(entry.defaultValue);
            }

            // Adding to root triggers the auto-tracking listener in RunTimeConfiguration,
            // which marks the component as modified automatically.
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

        // Explicit markAllModified — belt-and-suspenders approach
        if (markModified) {
            config.setModified(true);
        }

        if (makeDefault) {
            filter.addAndSetCurrentConfiguration(config);
        } else {
            filter.addConfiguration(config);
        }

        return config;
    }

    // -------------------------------------------------------------------------

    protected static class ComponentEntry {
        final FilterComponent filterComponent;
        final Object defaultValue;
        final boolean overrideDefault;

        ComponentEntry(FilterComponent filterComponent, @Nullable Object defaultValue, boolean overrideDefault) {
            this.filterComponent = filterComponent;
            this.defaultValue = defaultValue;
            this.overrideDefault = overrideDefault;
        }
    }
}
