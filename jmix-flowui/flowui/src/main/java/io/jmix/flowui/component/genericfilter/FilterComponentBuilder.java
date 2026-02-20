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
import io.jmix.flowui.component.jpqlfilter.JpqlFilter;
import io.jmix.flowui.component.logicalfilter.GroupFilter;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * A factory for creating filter components ({@link PropertyFilter}, {@link JpqlFilter},
 * {@link GroupFilter}) that are pre-configured for use inside a specific {@link GenericFilter}.
 * <p>
 * All builders returned by this factory automatically perform the initialisation steps
 * that the XML loader ({@code GenericFilterLoader}) does implicitly:
 * <ul>
 *   <li>{@code setConditionModificationDelegated(true)}</li>
 *   <li>{@code setDataLoader(filter.getDataLoader())}</li>
 *   <li>Correct ordering of {@code property → operation → value} for {@link PropertyFilter}</li>
 * </ul>
 * <p>
 * Obtain an instance via {@link GenericFilter#componentBuilder()}:
 * <pre>{@code
 * FilterComponentBuilder builder = filter.componentBuilder();
 *
 * PropertyFilter<String> nameFilter = builder.propertyFilter()
 *         .property("name")
 *         .operation(PropertyFilter.Operation.CONTAINS)
 *         .defaultValue("Acme")
 *         .build();
 *
 * JpqlFilter<Boolean> activeFilter = builder.jpqlFilter()
 *         .where("{E}.status = 'ACTIVE'")
 *         .build();
 * }</pre>
 */
public class FilterComponentBuilder {

    protected final GenericFilter filter;
    protected final UiComponents uiComponents;

    FilterComponentBuilder(GenericFilter filter, UiComponents uiComponents) {
        this.filter = filter;
        this.uiComponents = uiComponents;
    }

    /**
     * Returns a builder for a {@link PropertyFilter} bound to the owning filter.
     *
     * @param <V> the value type inferred from the entity property
     * @return a new {@link PropertyFilterBuilder}
     */
    public <V> PropertyFilterBuilder<V> propertyFilter() {
        return new PropertyFilterBuilder<>(filter, uiComponents);
    }

    /**
     * Returns a builder for a {@link JpqlFilter} without a query parameter
     * (rendered as a checkbox; parameter class is {@code Void}).
     *
     * @return a new {@link JpqlFilterBuilder} with {@code parameterClass = Void.class}
     */
    public JpqlFilterBuilder<Boolean> jpqlFilter() {
        return new JpqlFilterBuilder<>(filter, uiComponents, Void.class);
    }

    /**
     * Returns a builder for a {@link JpqlFilter} with a typed query parameter.
     *
     * @param parameterClass the Java class of the query parameter
     * @param <V>            the value type
     * @return a new {@link JpqlFilterBuilder}
     */
    public <V> JpqlFilterBuilder<V> jpqlFilter(Class<V> parameterClass) {
        checkNotNullArgument(parameterClass, "parameterClass must not be null");
        //noinspection unchecked
        return new JpqlFilterBuilder<>(filter, uiComponents, (Class<?>) parameterClass);
    }

    /**
     * Returns a builder for a {@link GroupFilter} bound to the owning filter.
     *
     * @return a new {@link GroupFilterBuilder}
     */
    public GroupFilterBuilder groupFilter() {
        return new GroupFilterBuilder(filter, uiComponents);
    }

    // -------------------------------------------------------------------------
    // PropertyFilterBuilder
    // -------------------------------------------------------------------------

    /**
     * Fluent builder for {@link PropertyFilter}.
     * <p>
     * All mandatory initialisation is handled inside {@link #build()}: the correct call
     * order ({@code setDataLoader → setProperty → setOperation → setValue}) is enforced
     * internally, so the caller is free to set these in any order via the fluent methods.
     *
     * @param <V> the value type
     */
    public static class PropertyFilterBuilder<V> {

        private final GenericFilter filter;
        private final UiComponents uiComponents;

        private String property;
        private PropertyFilter.Operation operation;
        private V defaultValue;
        private String label;

        PropertyFilterBuilder(GenericFilter filter, UiComponents uiComponents) {
            this.filter = filter;
            this.uiComponents = uiComponents;
        }

        /**
         * Sets the entity property path (e.g. {@code "name"} or {@code "customer.city.name"}).
         * Required.
         */
        public PropertyFilterBuilder<V> property(String property) {
            checkNotNullArgument(property, "property must not be null");
            this.property = property;
            return this;
        }

        /**
         * Sets the filtering operation (e.g. {@link PropertyFilter.Operation#EQUAL}).
         * Required.
         */
        public PropertyFilterBuilder<V> operation(PropertyFilter.Operation operation) {
            checkNotNullArgument(operation, "operation must not be null");
            this.operation = operation;
            return this;
        }

        /**
         * Sets the default (pre-filled) value for the filter condition. Optional.
         */
        public PropertyFilterBuilder<V> defaultValue(@Nullable V defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        /**
         * Overrides the auto-generated label text. Optional.
         */
        public PropertyFilterBuilder<V> label(@Nullable String label) {
            this.label = label;
            return this;
        }

        /**
         * Builds and returns the configured {@link PropertyFilter}.
         * <p>
         * Enforces the required call order:
         * <ol>
         *   <li>{@code setConditionModificationDelegated(true)}</li>
         *   <li>{@code setDataLoader(...)}</li>
         *   <li>{@code setProperty(...)}</li>
         *   <li>{@code setOperation(...)}</li>
         *   <li>{@code setValue(...)} (only if a default value was provided)</li>
         * </ol>
         *
         * @throws IllegalStateException if {@code property} or {@code operation} was not set
         */
        @SuppressWarnings("unchecked")
        public PropertyFilter<V> build() {
            if (property == null) {
                throw new IllegalStateException(
                        "PropertyFilterBuilder: 'property' is required — call .property(\"...\") before .build()");
            }
            if (operation == null) {
                throw new IllegalStateException(
                        "PropertyFilterBuilder: 'operation' is required — call .operation(...) before .build()");
            }

            PropertyFilter<V> pf = uiComponents.create(PropertyFilter.class);
            // 1. delegate condition modification — must be first
            pf.setConditionModificationDelegated(true);
            // 2. data loader before property so that initOperationSelectorActions runs correctly
            if (filter.getDataLoader() != null) {
                pf.setDataLoader(filter.getDataLoader());
            }
            // 3. property
            pf.setProperty(property);
            // 4. operation
            pf.setOperation(operation);
            // 5. optional overrides
            if (label != null) {
                pf.setLabel(label);
            }
            if (defaultValue != null) {
                pf.setValue(defaultValue);
            }
            return pf;
        }
    }

    // -------------------------------------------------------------------------
    // JpqlFilterBuilder
    // -------------------------------------------------------------------------

    /**
     * Fluent builder for {@link JpqlFilter}.
     *
     * @param <V> the value type ({@code Boolean} for void/checkbox filters, or the parameter type)
     */
    public static class JpqlFilterBuilder<V> {

        private final GenericFilter filter;
        private final UiComponents uiComponents;
        private final Class<?> parameterClass;

        private String where;
        private String join;
        private String parameterName;
        private V defaultValue;
        private String label;
        private boolean hasInExpression;

        JpqlFilterBuilder(GenericFilter filter, UiComponents uiComponents, Class<?> parameterClass) {
            this.filter = filter;
            this.uiComponents = uiComponents;
            this.parameterClass = parameterClass;
        }

        /**
         * Sets the JPQL WHERE clause fragment (use {@code {E}} as the entity alias and
         * {@code ?} as the parameter placeholder). Required.
         */
        public JpqlFilterBuilder<V> where(String where) {
            checkNotNullArgument(where, "where must not be null");
            this.where = where;
            return this;
        }

        /**
         * Sets the optional JPQL JOIN clause fragment (e.g. {@code "join {E}.tags t"}).
         */
        public JpqlFilterBuilder<V> join(@Nullable String join) {
            this.join = join;
            return this;
        }

        /**
         * Sets the query parameter name. Optional for void filters; required when the
         * parameter class is not {@code Void}.
         */
        public JpqlFilterBuilder<V> parameterName(String parameterName) {
            checkNotNullArgument(parameterName, "parameterName must not be null");
            this.parameterName = parameterName;
            return this;
        }

        /**
         * Sets the default (pre-filled) value. Optional.
         */
        public JpqlFilterBuilder<V> defaultValue(@Nullable V defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        /**
         * Overrides the auto-generated label text. Optional.
         */
        public JpqlFilterBuilder<V> label(@Nullable String label) {
            this.label = label;
            return this;
        }

        /**
         * Marks the condition as having an IN expression (the value is a collection).
         */
        public JpqlFilterBuilder<V> hasInExpression(boolean hasInExpression) {
            this.hasInExpression = hasInExpression;
            return this;
        }

        /**
         * Builds and returns the configured {@link JpqlFilter}.
         * <p>
         * Initialisation order enforced internally:
         * <ol>
         *   <li>{@code setConditionModificationDelegated(true)}</li>
         *   <li>{@code setDataLoader(...)}</li>
         *   <li>{@code setParameterName(...)} (if provided)</li>
         *   <li>{@code setParameterClass(...)}</li>
         *   <li>{@code setCondition(where, join)}</li>
         *   <li>{@code setValue(...)} (only if a default value was provided)</li>
         * </ol>
         *
         * @throws IllegalStateException if {@code where} was not set
         */
        @SuppressWarnings("unchecked")
        public JpqlFilter<V> build() {
            if (where == null) {
                throw new IllegalStateException(
                        "JpqlFilterBuilder: 'where' is required — call .where(\"...\") before .build()");
            }

            JpqlFilter<V> jf = uiComponents.create(JpqlFilter.class);
            // 1. delegate modification
            jf.setConditionModificationDelegated(true);
            // 2. data loader
            if (filter.getDataLoader() != null) {
                jf.setDataLoader(filter.getDataLoader());
            }
            // 3. parameterName before parameterClass (affects where substitution)
            if (parameterName != null) {
                jf.setParameterName(parameterName);
            }
            // 4. parameterClass — one-time setter, must come before setCondition
            jf.setParameterClass((Class) parameterClass);
            // 5. where / join
            jf.setCondition(where, join);
            // 6. optional
            if (label != null) {
                jf.setLabel(label);
            }
            if (hasInExpression) {
                jf.setHasInExpression(true);
            }
            if (defaultValue != null) {
                jf.setValue(defaultValue);
            }
            return jf;
        }
    }

    // -------------------------------------------------------------------------
    // GroupFilterBuilder
    // -------------------------------------------------------------------------

    /**
     * Fluent builder for {@link GroupFilter}.
     */
    public static class GroupFilterBuilder {

        private final GenericFilter filter;
        private final UiComponents uiComponents;

        private LogicalFilterComponent.Operation operation = LogicalFilterComponent.Operation.AND;
        private final List<FilterComponent> components = new ArrayList<>();

        GroupFilterBuilder(GenericFilter filter, UiComponents uiComponents) {
            this.filter = filter;
            this.uiComponents = uiComponents;
        }

        /**
         * Sets the logical operation ({@code AND} or {@code OR}). Defaults to {@code AND}.
         */
        public GroupFilterBuilder operation(LogicalFilterComponent.Operation operation) {
            checkNotNullArgument(operation, "operation must not be null");
            this.operation = operation;
            return this;
        }

        /**
         * Adds a filter component to this group.
         */
        public GroupFilterBuilder add(FilterComponent filterComponent) {
            checkNotNullArgument(filterComponent, "filterComponent must not be null");
            components.add(filterComponent);
            return this;
        }

        /**
         * Builds and returns the configured {@link GroupFilter}.
         */
        public GroupFilter build() {
            GroupFilter gf = uiComponents.create(GroupFilter.class);
            gf.setConditionModificationDelegated(true);
            if (filter.getDataLoader() != null) {
                gf.setDataLoader(filter.getDataLoader());
            }
            gf.setOperation(operation);
            for (FilterComponent fc : components) {
                gf.add(fc);
            }
            return gf;
        }
    }
}
