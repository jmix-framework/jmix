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
import io.jmix.flowui.component.filter.FilterComponent;
import io.jmix.flowui.component.filter.SingleFilterComponentBase;
import io.jmix.flowui.component.genericfilter.configuration.DesignTimeConfiguration;
import io.jmix.flowui.component.genericfilter.configuration.RunTimeConfiguration;
import io.jmix.flowui.component.logicalfilter.GroupFilter;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Fluent builder for a {@link DesignTimeConfiguration} registered from code.
 * <p>
 * A configuration built by this builder belongs to the code that registers it, like a configuration
 * declared in XML: it is created anew every time the view opens and is never stored. The user can
 * change the values of its conditions, clear them and make the configuration their default one, but
 * cannot edit, save or remove it. To change the conditions themselves, the user copies the
 * configuration and saves the copy as their own configuration.
 * <p>
 * Encapsulates all required steps:
 * <ul>
 *   <li>Creating and configuring the root {@link GroupFilter}</li>
 *   <li>Adding filter components and recording their default values in the configuration</li>
 *   <li>Registering the configuration via {@link GenericFilter#addConfiguration(Configuration)}</li>
 *   <li>Optionally activating the configuration via
 *       {@link GenericFilter#setCurrentConfiguration(Configuration)}</li>
 * </ul>
 * <p>
 * Obtain an instance via {@link GenericFilter#filterConfigurationBuilder()}:
 * <pre>{@code
 * filter.filterConfigurationBuilder()
 *       .id("openOrders")
 *       .name("Open Orders")
 *       .add(numberFilter)
 *       .add(statusFilter, "NEW")
 *       .makeCurrent()
 *       .buildAndRegister();
 * }</pre>
 */
@Experimental
public class FilterConfigurationBuilder {

    protected final GenericFilter filter;

    protected String id;
    protected String name;
    protected LogicalFilterComponent.Operation operation = LogicalFilterComponent.Operation.AND;
    protected boolean makeCurrent = false;
    protected boolean built = false;

    protected final List<ComponentEntry> entries = new ArrayList<>();

    protected FilterConfigurationBuilder(GenericFilter filter) {
        this.filter = filter;
    }

    /**
     * Sets the configuration id. Required.
     *
     * @param id unique configuration identifier within this filter
     */
    public FilterConfigurationBuilder id(String id) {
        checkNotNullArgument(id, "id must not be null");
        this.id = id;
        return this;
    }

    /**
     * Sets the configuration display name.
     *
     * @param name display name shown in the configuration selector
     */
    public FilterConfigurationBuilder name(@Nullable String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the logical operation of the root filter component. Defaults to {@code AND}.
     *
     * @param operation logical operation
     */
    public FilterConfigurationBuilder operation(LogicalFilterComponent.Operation operation) {
        checkNotNullArgument(operation, "operation must not be null");
        this.operation = operation;
        return this;
    }

    /**
     * Adds a filter component using the component's current value (if any) as the default.
     *
     * @param filterComponent filter component to add
     */
    public FilterConfigurationBuilder add(FilterComponent filterComponent) {
        checkNotNullArgument(filterComponent, "filterComponent must not be null");
        entries.add(new ComponentEntry(filterComponent, null, false));
        return this;
    }

    /**
     * Adds several filter components at once, each using its current value (if any) as the default.
     *
     * @param filterComponents filter components to add
     */
    public FilterConfigurationBuilder addAll(FilterComponent... filterComponents) {
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
    public <V> FilterConfigurationBuilder add(SingleFilterComponentBase<V> filterComponent,
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
    public FilterConfigurationBuilder makeCurrent() {
        this.makeCurrent = true;
        return this;
    }

    /**
     * Builds the {@link DesignTimeConfiguration}, registers it with the filter, and
     * optionally activates it.
     * <p>
     * Automatically:
     * <ul>
     *   <li>Creates the root {@link GroupFilter} as the filter creates it for a configuration declared in XML</li>
     *   <li>Adds each filter component to the root</li>
     *   <li>Calls {@code setFilterComponentDefaultValue} for every component with a value, including the
     *       components nested in a group</li>
     *   <li>Registers the configuration via {@link GenericFilter#addConfiguration(Configuration)}, where a
     *       run-time configuration with the same id, such as a stored one, gives way to it, and activates it via
     *       {@link GenericFilter#setCurrentConfiguration(Configuration)} if {@link #makeCurrent()} was
     *       requested</li>
     * </ul>
     *
     * @return the newly created and registered {@link DesignTimeConfiguration}
     * @throws IllegalStateException if this builder instance has already been used, if {@code id}
     *         was not set or is the id of the empty configuration, if a configuration with the same id that is
     *         not a run-time one, such as a design-time configuration, is already registered in the filter, or if
     *         the filter has no DataLoader
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public DesignTimeConfiguration buildAndRegister() {
        if (built) {
            throw new IllegalStateException(
                    "%s.buildAndRegister() must not be called more than once; create a new instance per configuration"
                            .formatted(FilterConfigurationBuilder.class.getSimpleName()));
        }

        if (id == null) {
            throw new IllegalStateException(
                    "%s: 'id' is required — call .id(\"...\") before .buildAndRegister()"
                            .formatted(FilterConfigurationBuilder.class.getSimpleName()));
        }

        if (id.equals(filter.getEmptyConfiguration().getId())) {
            throw new IllegalStateException("%s: 'id' must not be the reserved empty-configuration id '%s'"
                    .formatted(FilterConfigurationBuilder.class.getSimpleName(), id));
        }

        Configuration registeredConfiguration = filter.getConfiguration(id);
        // A run-time configuration with the id, such as a stored one loaded before the builder runs, gives way.
        if (registeredConfiguration != null && !(registeredConfiguration instanceof RunTimeConfiguration)) {
            throw new IllegalStateException("%s: a configuration with id '%s' is already registered in this filter"
                    .formatted(FilterConfigurationBuilder.class.getSimpleName(), id));
        }

        if (filter.getDataLoader() == null) {
            throw new IllegalStateException("%s: the filter has no DataLoader; set it before building a configuration"
                    .formatted(FilterConfigurationBuilder.class.getSimpleName()));
        }

        LogicalFilterComponent<?> root = filter.createConfigurationRootLogicalFilterComponent(operation);
        DesignTimeConfiguration config = new DesignTimeConfiguration(id, name, root, filter);

        for (ComponentEntry entry : entries) {
            FilterComponent fc = entry.filterComponent;

            if (entry.overrideDefault && fc instanceof SingleFilterComponentBase sfc) {
                sfc.setValue(entry.defaultValue);
            }

            root.add(fc);
        }

        // Persist the default values for reset/restore behaviour, including the ones of conditions nested in a group.
        // Skip components without a parameter name (e.g. void JpqlFilter with Void parameterClass).
        for (FilterComponent fc : root.getFilterComponents()) {
            if (fc instanceof SingleFilterComponentBase<?> sfc) {
                String paramName = sfc.getParameterName();
                Object valueToStore = sfc.getValue();
                if (paramName != null && valueToStore != null) {
                    config.setFilterComponentDefaultValue(paramName, valueToStore);
                }
            }
        }

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
