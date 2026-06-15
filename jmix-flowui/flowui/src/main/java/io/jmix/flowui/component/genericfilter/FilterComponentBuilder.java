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

import io.jmix.core.Metadata;
import io.jmix.core.annotation.Experimental;
import io.jmix.core.querycondition.PropertyConditionUtils;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.filter.FilterComponent;
import io.jmix.flowui.component.genericfilter.converter.FilterConverter;
import io.jmix.flowui.component.genericfilter.registration.FilterComponents;
import io.jmix.flowui.component.jpqlfilter.JpqlFilter;
import io.jmix.flowui.component.jpqlfilter.JpqlFilterSupport;
import io.jmix.flowui.component.logicalfilter.GroupFilter;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.entity.filter.FilterValueComponent;
import io.jmix.flowui.entity.filter.JpqlFilterCondition;
import io.jmix.flowui.entity.filter.PropertyFilterCondition;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * A factory for creating filter components ({@link PropertyFilter}, {@link JpqlFilter},
 * {@link GroupFilter}) that are pre-configured for use inside a specific {@link GenericFilter}.
 * <p>
 * Single filter components are built by assembling a condition model from the fluent parameters
 * and delegating to the same converter the framework uses when loading a filter from XML or
 * adding a condition through the UI. The converter is the single source of truth for component
 * initialisation, so a programmatically built component behaves exactly like a declarative one.
 * <p>
 * The owning {@link GenericFilter} must have a {@code DataLoader}; otherwise {@code build()}
 * throws an {@link IllegalStateException}.
 * <p>
 * This factory and its nested builders are designed for extension: their constructors and
 * fields are {@code protected}. To customise behaviour, subclass {@code FilterComponentBuilder}
 * and override the relevant factory method ({@link #propertyFilter()}, {@link #jpqlFilter()},
 * {@link #groupFilter()}) to return your own builder subclass, then return your factory from a
 * {@link GenericFilter} subclass that overrides {@link GenericFilter#filterComponentBuilder()}.
 * <p>
 * Obtain an instance via {@link GenericFilter#filterComponentBuilder()}:
 * <pre>{@code
 * FilterComponentBuilder builder = filter.filterComponentBuilder();
 *
 * PropertyFilter<String> nameFilter = builder.propertyFilter()
 *         .property("name")
 *         .operation(PropertyFilter.Operation.CONTAINS)
 *         .defaultValue("Acme")
 *         .build();
 *
 * // void (checkbox) filter: toggles a fixed condition on/off
 * JpqlFilter<Boolean> activeFilter = builder.jpqlFilter()
 *         .where("{E}.status = 'ACTIVE'")
 *         .defaultValue(true)
 *         .build();
 *
 * // typed filter: a user-supplied value is bound to the query parameter
 * JpqlFilter<String> codeFilter = builder.jpqlFilter(String.class)
 *         .where("{E}.code = ?")
 *         .build();
 * }</pre>
 */
@Experimental
public class FilterComponentBuilder {

    protected final GenericFilter filter;
    protected final ApplicationContext applicationContext;

    protected FilterComponentBuilder(GenericFilter filter, ApplicationContext applicationContext) {
        this.filter = filter;
        this.applicationContext = applicationContext;
    }

    /**
     * Returns a builder for a {@link PropertyFilter} bound to the owning filter.
     *
     * @param <V> the value type inferred from the entity property
     * @return a new {@link PropertyFilterBuilder}
     */
    public <V> PropertyFilterBuilder<V> propertyFilter() {
        return new PropertyFilterBuilder<>(filter, applicationContext);
    }

    /**
     * Returns a builder for a <em>void</em> (checkbox) {@link JpqlFilter}.
     * <p>
     * Use this variant for a condition with a fixed WHERE clause that carries no user-supplied
     * value, e.g. {@code {E}.status = 'ACTIVE'}. The filter is rendered as a checkbox: when
     * checked the WHERE clause is applied, when unchecked it is omitted. The value type is
     * therefore {@link Boolean}; use {@code defaultValue(true)} to make the condition active by
     * default.
     *
     * @return a new {@link JpqlFilterBuilder} that produces a checkbox filter
     */
    public JpqlFilterBuilder<Boolean> jpqlFilter() {
        return new JpqlFilterBuilder<>(filter, applicationContext, Void.class);
    }

    /**
     * Returns a builder for a <em>typed</em> {@link JpqlFilter} whose user-supplied value is
     * bound to the query parameter.
     *
     * @param parameterClass the Java class of the query parameter
     * @param <V>            the value type
     * @return a new {@link JpqlFilterBuilder}
     */
    public <V> JpqlFilterBuilder<V> jpqlFilter(Class<V> parameterClass) {
        checkNotNullArgument(parameterClass, "parameterClass must not be null");
        return new JpqlFilterBuilder<>(filter, applicationContext, parameterClass);
    }

    /**
     * Returns a builder for a {@link GroupFilter} bound to the owning filter.
     *
     * @return a new {@link GroupFilterBuilder}
     */
    public GroupFilterBuilder groupFilter() {
        return new GroupFilterBuilder(filter, applicationContext);
    }

    protected static void checkDataLoaderPresent(GenericFilter filter) {
        if (filter.getDataLoader() == null) {
            throw new IllegalStateException(String.format(
                    "%s has no DataLoader; set one before building filter components.",
                    filter.getClass().getSimpleName()));
        }
    }

    /**
     * Fluent builder for {@link PropertyFilter}.
     *
     * @param <V> the value type
     */
    @Experimental
    public static class PropertyFilterBuilder<V> {

        protected final GenericFilter filter;
        protected Metadata metadata;
        protected FilterComponents filterComponents;

        protected String property;
        protected PropertyFilter.Operation operation;
        protected V defaultValue;
        protected String label;
        protected boolean operationEditable = false;
        protected boolean operationTextVisible = true;
        protected boolean built = false;

        protected PropertyFilterBuilder(GenericFilter filter, ApplicationContext applicationContext) {
            this.filter = filter;
            autowireDependencies(applicationContext);
        }

        protected void autowireDependencies(ApplicationContext applicationContext) {
            this.metadata = applicationContext.getBean(Metadata.class);
            this.filterComponents = applicationContext.getBean(FilterComponents.class);
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
         * Controls whether the user can change the filter operation at runtime via a
         * dropdown selector. Defaults to {@code false}.
         */
        public PropertyFilterBuilder<V> operationEditable(boolean operationEditable) {
            this.operationEditable = operationEditable;
            return this;
        }

        /**
         * Controls whether the operation label (e.g. "contains", "=") is shown next to
         * the value field. Defaults to {@code true}.
         */
        public PropertyFilterBuilder<V> operationTextVisible(boolean operationTextVisible) {
            this.operationTextVisible = operationTextVisible;
            return this;
        }

        /**
         * Builds and returns the configured {@link PropertyFilter}.
         *
         * @throws IllegalStateException if {@code property} or {@code operation} was not set,
         *         if the owning filter has no DataLoader, or if this builder instance has
         *         already been used
         */
        @SuppressWarnings("unchecked")
        public PropertyFilter<V> build() {
            if (built) {
                throw new IllegalStateException(
                        "PropertyFilterBuilder.build() must not be called more than once; create a new instance per PropertyFilter");
            }
            built = true;
            if (property == null) {
                throw new IllegalStateException(
                        "PropertyFilterBuilder: 'property' is required — call .property(\"...\") before .build()");
            }
            if (operation == null) {
                throw new IllegalStateException(
                        "PropertyFilterBuilder: 'operation' is required — call .operation(...) before .build()");
            }
            checkDataLoaderPresent(filter);

            PropertyFilterCondition model = metadata.create(PropertyFilterCondition.class);
            model.setProperty(property);
            model.setOperation(operation);
            model.setParameterName(PropertyConditionUtils.generateParameterName(property));
            model.setOperationEditable(operationEditable);
            model.setOperationTextVisible(operationTextVisible);
            model.setLabel(label);
            model.setValueComponent(metadata.create(FilterValueComponent.class));

            FilterConverter<PropertyFilter<V>, PropertyFilterCondition> converter =
                    (FilterConverter<PropertyFilter<V>, PropertyFilterCondition>) filterComponents
                            .getConverterByModelClass(PropertyFilterCondition.class, filter);
            PropertyFilter<V> propertyFilter = converter.convertToComponent(model);
            if (defaultValue != null) {
                propertyFilter.setValue(defaultValue);
            }
            return propertyFilter;
        }
    }

    /**
     * Fluent builder for {@link JpqlFilter}.
     *
     * @param <V> the value type ({@link Boolean} for a void/checkbox filter, or the parameter type)
     */
    @Experimental
    public static class JpqlFilterBuilder<V> {

        protected final GenericFilter filter;
        protected final Class<?> parameterClass;
        protected Metadata metadata;
        protected FilterComponents filterComponents;
        protected JpqlFilterSupport jpqlFilterSupport;

        protected String where;
        protected String join;
        protected String parameterName;
        protected V defaultValue;
        protected String label;
        protected boolean hasInExpression;
        protected boolean built = false;

        protected JpqlFilterBuilder(GenericFilter filter, ApplicationContext applicationContext, Class<?> parameterClass) {
            this.filter = filter;
            this.parameterClass = parameterClass;
            autowireDependencies(applicationContext);
        }

        protected void autowireDependencies(ApplicationContext applicationContext) {
            this.metadata = applicationContext.getBean(Metadata.class);
            this.filterComponents = applicationContext.getBean(FilterComponents.class);
            this.jpqlFilterSupport = applicationContext.getBean(JpqlFilterSupport.class);
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
         * Sets the query parameter name. Optional — if not set, a name is generated
         * automatically (matching the behaviour of the XML loader). Set it explicitly only
         * when the WHERE clause uses a named bind parameter ({@code :paramName}) that must
         * match a specific name.
         */
        public JpqlFilterBuilder<V> parameterName(String parameterName) {
            checkNotNullArgument(parameterName, "parameterName must not be null");
            this.parameterName = parameterName;
            return this;
        }

        /**
         * Sets the default (pre-filled) value. Optional.
         * <p>
         * For a void (checkbox) filter the value type is {@link Boolean}: {@code defaultValue(true)}
         * makes the condition active by default.
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
         *
         * @throws IllegalStateException if {@code where} was not set, if the owning filter has
         *         no DataLoader, or if this builder instance has already been used
         */
        @SuppressWarnings("unchecked")
        public JpqlFilter<V> build() {
            if (built) {
                throw new IllegalStateException(
                        "JpqlFilterBuilder.build() must not be called more than once; create a new instance per JpqlFilter");
            }
            built = true;
            if (where == null) {
                throw new IllegalStateException(
                        "JpqlFilterBuilder: 'where' is required — call .where(\"...\") before .build()");
            }
            checkDataLoaderPresent(filter);

            JpqlFilterCondition model = metadata.create(JpqlFilterCondition.class);
            model.setParameterClass(parameterClass.getName());
            model.setParameterName(parameterName != null
                    ? parameterName
                    : jpqlFilterSupport.generateParameterName(null, parameterClass.getSimpleName()));
            model.setWhere(where);
            model.setJoin(join);
            model.setHasInExpression(hasInExpression);
            model.setLabel(label);
            model.setValueComponent(metadata.create(FilterValueComponent.class));

            FilterConverter<JpqlFilter<V>, JpqlFilterCondition> converter =
                    (FilterConverter<JpqlFilter<V>, JpqlFilterCondition>) filterComponents
                            .getConverterByModelClass(JpqlFilterCondition.class, filter);
            JpqlFilter<V> jpqlFilter = converter.convertToComponent(model);
            if (defaultValue != null) {
                jpqlFilter.setValue(defaultValue);
            }
            return jpqlFilter;
        }
    }

    /**
     * Fluent builder for {@link GroupFilter}.
     */
    @Experimental
    public static class GroupFilterBuilder {

        protected final GenericFilter filter;
        protected UiComponents uiComponents;

        protected LogicalFilterComponent.Operation operation = LogicalFilterComponent.Operation.AND;
        protected final List<FilterComponent> components = new ArrayList<>();
        protected boolean built = false;

        protected GroupFilterBuilder(GenericFilter filter, ApplicationContext applicationContext) {
            this.filter = filter;
            autowireDependencies(applicationContext);
        }

        protected void autowireDependencies(ApplicationContext applicationContext) {
            this.uiComponents = applicationContext.getBean(UiComponents.class);
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
         * Adds several filter components to this group at once.
         * <p>
         * Named {@code addAll} (rather than overloading {@code add}) for consistency with
         * {@link RunTimeConfigurationBuilder#addAll(FilterComponent...)}.
         */
        public GroupFilterBuilder addAll(FilterComponent... filterComponents) {
            checkNotNullArgument(filterComponents, "filterComponents must not be null");
            for (FilterComponent filterComponent : filterComponents) {
                add(filterComponent);
            }
            return this;
        }

        /**
         * Builds and returns the configured {@link GroupFilter}.
         *
         * @throws IllegalStateException if the owning filter has no DataLoader, or if this
         *         builder instance has already been used
         */
        public GroupFilter build() {
            if (built) {
                throw new IllegalStateException(
                        "GroupFilterBuilder.build() must not be called more than once; create a new instance per GroupFilter");
            }
            built = true;
            checkDataLoaderPresent(filter);

            GroupFilter groupFilter = uiComponents.create(GroupFilter.class);
            groupFilter.setConditionModificationDelegated(true);
            groupFilter.setAutoApply(filter.isAutoApply());
            groupFilter.setDataLoader(filter.getDataLoader());
            groupFilter.setOperation(operation);
            for (FilterComponent component : components) {
                groupFilter.add(component);
            }
            return groupFilter;
        }
    }
}
