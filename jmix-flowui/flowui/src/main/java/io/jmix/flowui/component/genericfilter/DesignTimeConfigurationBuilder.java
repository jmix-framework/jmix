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
import io.jmix.flowui.component.genericfilter.configuration.DesignTimeConfiguration;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Fluent builder for {@link DesignTimeConfiguration}.
 * <p>
 * Encapsulates all steps that would otherwise need to be performed manually:
 * <ul>
 *   <li>Creating and configuring the root {@link LogicalFilterComponent}</li>
 *   <li>Adding filter components to the root</li>
 *   <li>Calling {@code setFilterComponentDefaultValue} for each component that has a value</li>
 *   <li>Registering the configuration via {@link GenericFilter#addConfiguration(Configuration)}</li>
 *   <li>Optionally activating it via {@link GenericFilter#setCurrentConfiguration(Configuration)}</li>
 * </ul>
 * <p>
 * Obtain an instance via {@link GenericFilter#configurationBuilder()}:
 * <pre>{@code
 * filter.configurationBuilder()
 *       .id("byStatus")
 *       .name("Search by Status")
 *       .operation(LogicalFilterComponent.Operation.AND)
 *       .add(statusFilter)
 *       .add(nameFilter, "Acme")   // override default value for this component
 *       .asDefault()
 *       .buildAndRegister();
 * }</pre>
 */
public class DesignTimeConfigurationBuilder {

    protected final GenericFilter filter;
    protected final UiComponents uiComponents;

    protected String id;
    protected String name;
    protected LogicalFilterComponent.Operation operation = LogicalFilterComponent.Operation.AND;
    protected boolean makeDefault = false;

    protected final List<ComponentEntry> entries = new ArrayList<>();

    DesignTimeConfigurationBuilder(GenericFilter filter, UiComponents uiComponents) {
        this.filter = filter;
        this.uiComponents = uiComponents;
    }

    /**
     * Sets the configuration id. Required.
     *
     * @param id unique configuration identifier within this filter
     */
    public DesignTimeConfigurationBuilder id(String id) {
        checkNotNullArgument(id, "id must not be null");
        this.id = id;
        return this;
    }

    /**
     * Sets the configuration display name.
     *
     * @param name display name shown in the configuration selector
     */
    public DesignTimeConfigurationBuilder name(@Nullable String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the logical operation of the root filter component. Defaults to {@code AND}.
     *
     * @param operation logical operation
     */
    public DesignTimeConfigurationBuilder operation(LogicalFilterComponent.Operation operation) {
        checkNotNullArgument(operation, "operation must not be null");
        this.operation = operation;
        return this;
    }

    /**
     * Adds a filter component to the configuration using the component's current value
     * (if any) as the default.
     *
     * @param filterComponent filter component to add
     */
    public DesignTimeConfigurationBuilder add(FilterComponent filterComponent) {
        checkNotNullArgument(filterComponent, "filterComponent must not be null");
        entries.add(new ComponentEntry(filterComponent, null, false));
        return this;
    }

    /**
     * Adds a filter component to the configuration, overriding its default value.
     * <p>
     * This is equivalent to calling {@code component.setValue(defaultValue)} followed by
     * {@code config.setFilterComponentDefaultValue(parameterName, defaultValue)}.
     *
     * @param filterComponent filter component to add
     * @param defaultValue    value to set as both the component's current value and the
     *                        configuration's default
     */
    public DesignTimeConfigurationBuilder add(FilterComponent filterComponent, @Nullable Object defaultValue) {
        checkNotNullArgument(filterComponent, "filterComponent must not be null");
        entries.add(new ComponentEntry(filterComponent, defaultValue, true));
        return this;
    }

    /**
     * Marks this configuration as the default (currently active) configuration.
     * After {@link #buildAndRegister()} the configuration will be set as the current
     * configuration of the filter.
     */
    public DesignTimeConfigurationBuilder asDefault() {
        this.makeDefault = true;
        return this;
    }

    /**
     * Builds the {@link DesignTimeConfiguration}, registers it with the filter, and
     * optionally activates it.
     * <p>
     * Automatically:
     * <ul>
     *   <li>Adds each filter component to the configuration's root</li>
     *   <li>Calls {@code setFilterComponentDefaultValue} for every component that carries a
     *       value (either via the overload with an explicit default or because the component
     *       already has a non-null value at the time {@code add()} was called)</li>
     *   <li>Calls {@link GenericFilter#addConfiguration(Configuration)}</li>
     *   <li>If {@link #asDefault()} was called, calls
     *       {@link GenericFilter#setCurrentConfiguration(Configuration)}</li>
     * </ul>
     *
     * @return the newly created and registered {@link DesignTimeConfiguration}
     * @throws IllegalStateException if {@code id} was not set
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public DesignTimeConfiguration buildAndRegister() {
        if (id == null) {
            throw new IllegalStateException(
                    "DesignTimeConfigurationBuilder: 'id' is required — call .id(\"...\") before .buildAndRegister()");
        }

        DesignTimeConfiguration config = filter.addConfiguration(id, name, operation);
        LogicalFilterComponent<?> root = config.getRootLogicalFilterComponent();

        for (ComponentEntry entry : entries) {
            FilterComponent fc = entry.filterComponent;

            if (entry.overrideDefault) {
                // Apply the explicit default value to the component.
                // Best-effort: the value component may not be ready yet when no DataLoader
                // is assigned (e.g. in tests or lazy initialisation scenarios).
                // The default value is still persisted in the configuration below.
                if (fc instanceof SingleFilterComponentBase<?> sfc) {
                    try {
                        ((SingleFilterComponentBase) sfc).setValue(entry.defaultValue);
                    } catch (RuntimeException ignored) {
                        // component not fully initialised; default stored in config below
                    }
                }
            }

            root.add(fc);

            // Persist the default value in the configuration so it survives reset.
            // Skip components without a parameter name (e.g. void JpqlFilter with Void parameterClass).
            if (fc instanceof SingleFilterComponentBase<?> sfc) {
                String paramName = sfc.getParameterName();
                Object valueToStore = entry.overrideDefault ? entry.defaultValue : sfc.getValue();
                if (paramName != null && valueToStore != null) {
                    config.setFilterComponentDefaultValue(paramName, valueToStore);
                }
            }
        }

        if (makeDefault) {
            filter.setCurrentConfiguration(config);
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
