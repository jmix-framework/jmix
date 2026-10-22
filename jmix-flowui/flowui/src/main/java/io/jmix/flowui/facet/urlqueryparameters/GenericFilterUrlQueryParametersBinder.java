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

package io.jmix.flowui.facet.urlqueryparameters;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.AccessManager;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.accesscontext.EntityAttributeContext;
import io.jmix.core.annotation.Internal;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.core.querycondition.PropertyConditionUtils;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.filter.FilterComponent;
import io.jmix.flowui.component.filter.SingleFilterComponentBase;
import io.jmix.flowui.component.genericfilter.Configuration;
import io.jmix.flowui.component.genericfilter.FilterUtils;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.genericfilter.configuration.RunTimeConfiguration;
import io.jmix.flowui.component.genericfilter.converter.FilterConverter;
import io.jmix.flowui.component.genericfilter.registration.FilterComponents;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent.FilterComponentsChangeEvent;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.component.propertyfilter.PropertyFilterSupport;
import io.jmix.flowui.entity.filter.FilterValueComponent;
import io.jmix.flowui.entity.filter.PropertyFilterCondition;
import io.jmix.flowui.facet.UrlQueryParametersFacet.UrlQueryParametersChangeEvent;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.KeyValueCollectionLoader;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static io.jmix.flowui.facet.urlqueryparameters.FilterUrlQueryParametersSupport.SEPARATOR;

/**
 * A binder class for managing URL query parameters related to a {@link GenericFilter} component.
 * This class facilitates the serialization and deserialization of query parameters,
 * manages component state, and ensures updates based on the URL query parameters or filter configuration changes.
 */
public class GenericFilterUrlQueryParametersBinder extends AbstractUrlQueryParametersBinder
        implements HasInitialState {

    private static final Logger log = LoggerFactory.getLogger(GenericFilterUrlQueryParametersBinder.class);

    public static final String NAME = "genericFilter";

    public static final String PROPERTY_CONDITION_PREFIX = "property:";
    public static final String DEFAULT_CONFIGURATION_PARAM = "genericFilterConfiguration";
    public static final String DEFAULT_CONDITION_PARAM = "genericFilterCondition";

    protected GenericFilter filter;

    protected String configurationParam;
    protected String conditionParam;

    protected ApplicationContext applicationContext;
    protected UrlParamSerializer urlParamSerializer;
    protected SingleFilterComponentStateSupport singleFilterComponentStateSupport;
    protected MetadataTools metadataTools;
    protected FilterUrlQueryParametersSupport filterUrlQueryParametersSupport;
    protected AccessManager accessManager;
    protected FilterComponents filterComponents;
    protected Metadata metadata;
    protected PropertyFilterSupport propertyFilterSupport;

    protected Registration filterComponentsChangeRegistration;

    protected final List<InitialState> initialStates = new ArrayList<>();

    protected Configuration entryConfiguration;

    public GenericFilterUrlQueryParametersBinder(GenericFilter filter,
                                                 UrlParamSerializer urlParamSerializer,
                                                 ApplicationContext applicationContext) {
        this.filter = filter;
        this.urlParamSerializer = urlParamSerializer;
        this.applicationContext = applicationContext;

        autowireDependencies();
        initComponent(filter);
    }

    protected void autowireDependencies() {
        singleFilterComponentStateSupport = applicationContext.getBean(SingleFilterComponentStateSupport.class);
        filterUrlQueryParametersSupport = applicationContext.getBean(FilterUrlQueryParametersSupport.class);
        accessManager = applicationContext.getBean(AccessManager.class);
        filterComponents = applicationContext.getBean(FilterComponents.class);
        metadata = applicationContext.getBean(Metadata.class);
        propertyFilterSupport = applicationContext.getBean(PropertyFilterSupport.class);
    }

    protected void initComponent(GenericFilter filter) {
        filter.addConfigurationChangeListener(this::onConfigurationChanged);
        bindFilterComponentsChangeListener(filter);
        bindDataLoaderListener(filter);
    }

    @Override
    public void saveInitialState() {
        // Snapshot every configuration, not only the entry one, so a clean re-navigation can restore
        // each configuration the user or the URL touched during the session (not just the current one).
        initialStates.clear();
        entryConfiguration = filter.getCurrentConfiguration();
        for (Configuration configuration : collectConfigurations()) {
            LogicalFilterComponent<?> rootLogicalFilterComponent = configuration.getRootLogicalFilterComponent();
            initialStates.add(new InitialState(configuration,
                    captureStructure(configuration, rootLogicalFilterComponent),
                    captureInitialStates(rootLogicalFilterComponent),
                    captureInitialDefaultValues(configuration, rootLogicalFilterComponent)));
        }
    }

    protected List<Configuration> collectConfigurations() {
        List<Configuration> configurations = new ArrayList<>(filter.getConfigurations());
        if (!configurations.contains(filter.getEmptyConfiguration())) {
            configurations.add(filter.getEmptyConfiguration());
        }
        return configurations;
    }

    protected List<ComponentNode> captureStructure(Configuration configuration,
                                                   LogicalFilterComponent<?> logicalFilterComponent) {
        List<ComponentNode> nodes = new ArrayList<>();
        for (FilterComponent component : logicalFilterComponent.getOwnFilterComponents()) {
            List<ComponentNode> children = component instanceof LogicalFilterComponent<?> nestedComponent
                    ? captureStructure(configuration, nestedComponent)
                    : List.of();
            nodes.add(new ComponentNode(component,
                    configuration.isFilterComponentModified(component),
                    children));
        }
        return nodes;
    }

    protected Map<String, Object> captureInitialDefaultValues(Configuration configuration,
                                                              LogicalFilterComponent<?> rootLogicalFilterComponent) {
        Map<String, Object> defaultValues = new HashMap<>();
        for (FilterComponent component : rootLogicalFilterComponent.getFilterComponents()) {
            if (component instanceof SingleFilterComponentBase<?> singleFilterComponent) {
                String parameterName = singleFilterComponent.getParameterName();
                if (parameterName != null) {
                    Object defaultValue = configuration.getFilterComponentDefaultValue(parameterName);
                    if (defaultValue != null) {
                        defaultValues.put(parameterName, defaultValue);
                    }
                }
            }
        }
        return defaultValues;
    }

    protected Map<SingleFilterComponentBase<?>, SingleFilterComponentStateSupport.State> captureInitialStates(
            LogicalFilterComponent<?> rootLogicalFilterComponent) {
        Map<SingleFilterComponentBase<?>, SingleFilterComponentStateSupport.State> states = new IdentityHashMap<>();
        collectInitialStates(rootLogicalFilterComponent.getOwnFilterComponents(), states);
        return states;
    }

    protected void collectInitialStates(Collection<FilterComponent> components,
                                        Map<SingleFilterComponentBase<?>, SingleFilterComponentStateSupport.State> states) {
        for (FilterComponent component : components) {
            if (component instanceof SingleFilterComponentBase<?> singleFilterComponent) {
                states.put(singleFilterComponent, singleFilterComponentStateSupport.capture(singleFilterComponent));
            } else if (component instanceof LogicalFilterComponent<?> logicalFilterComponent) {
                collectInitialStates(logicalFilterComponent.getOwnFilterComponents(), states);
            }
        }
    }

    protected void bindDataLoaderListener(GenericFilter filter) {
        DataLoader dataLoader = filter.getDataLoader();
        if (dataLoader instanceof CollectionLoader) {
            ((CollectionLoader<?>) dataLoader).addPostLoadListener(this::onPostLoad);
        } else if (dataLoader instanceof KeyValueCollectionLoader) {
            ((KeyValueCollectionLoader) dataLoader).addPostLoadListener(this::onPostLoad);
        }
    }

    protected void onPostLoad(EventObject event) {
        updateQueryParameters();
    }

    protected void onConfigurationChanged(GenericFilter.ConfigurationChangeEvent event) {
        unbindFilterComponentsChange();
        bindFilterComponentsChangeListener(event.getSource());

        updateQueryParameters();
    }

    protected void onFilterComponentsChanged(FilterComponentsChangeEvent<?> event) {
        updateQueryParameters();
    }

    protected void updateQueryParameters() {
        ImmutableMap<String, List<String>> parametersMap = serializeQueryParameters();

        QueryParameters queryParameters = new QueryParameters(parametersMap);
        fireQueryParametersChanged(new UrlQueryParametersChangeEvent(this, queryParameters));
    }

    /**
     * Serializes query parameters into an immutable map. The method processes the current
     * configuration and its associated conditions to generate query parameters.
     *
     * @return an immutable map containing serialized query parameters. The keys represent
     * parameter types (e.g., configuration and condition parameters), while the
     * values are lists of their respective serialized representations.
     */
    public ImmutableMap<String, List<String>> serializeQueryParameters() {
        Configuration currentConfiguration = filter.getCurrentConfiguration();
        LogicalCondition queryCondition = currentConfiguration.getQueryCondition();

        List<Condition> conditions = queryCondition.getConditions();
        List<String> conditionParams = new ArrayList<>(conditions.size());
        List<String> configurationParam;

        if (currentConfiguration != filter.getEmptyConfiguration()) {
            configurationParam = Collections.singletonList(serializeConfigurationId(currentConfiguration));
        } else {
            configurationParam = Collections.emptyList();
        }

        for (Condition condition : conditions) {
            if (condition instanceof PropertyCondition) {
                conditionParams.add(serializePropertyCondition(((PropertyCondition) condition)));
            }
        }

        return ImmutableMap.of(
                getConfigurationParam(), configurationParam,
                getConditionParam(), conditionParams
        );
    }

    protected String serializeConfigurationId(Configuration configuration) {
        return urlParamSerializer.serialize(configuration.getId());
    }

    protected String serializePropertyCondition(PropertyCondition condition) {
        String property = urlParamSerializer.serialize(
                filterUrlQueryParametersSupport.replaceSeparatorValue(condition.getProperty()));
        String operation = urlParamSerializer.serialize(
                filterUrlQueryParametersSupport.replaceSeparatorValue(condition.getOperation()));
        Object parameterValue = urlParamSerializer.serialize(
                filterUrlQueryParametersSupport.getSerializableValue(condition.getParameterValue()));

        return PROPERTY_CONDITION_PREFIX +
                property + SEPARATOR +
                operation + SEPARATOR +
                parameterValue;
    }

    @Override
    public void applyInitialState() {
        // The reconciliation performs many add/remove operations; each would otherwise fire a
        // FilterComponentsChangeEvent and trigger a full updateQueryParameters(). Suppress that
        // storm by unbinding the listener for the duration and rebinding once afterwards.
        unbindFilterComponentsChange();
        try {
            // Restore every snapshotted configuration, not only the entry one, so changes made to
            // other configurations during the session are reverted as well.
            for (InitialState state : initialStates) {
                // Structural restore and default-value re-registration apply only to runtime
                // configurations; a design-time configuration has no such user/URL-driven changes.
                if (state.configuration instanceof RunTimeConfiguration runTimeConfiguration) {
                    restoreStructure(runTimeConfiguration,
                            runTimeConfiguration.getRootLogicalFilterComponent(),
                            state.structure);
                    restoreInitialDefaultValues(runTimeConfiguration, state);
                }
                // A value or operation changed via the URL must be reset for any configuration type,
                // including a design-time configuration, whose captured state is restored here too.
                restoreInitialStates(state);
            }
        } finally {
            bindFilterComponentsChangeListener(filter);
        }

        filter.setCurrentConfiguration(entryConfiguration);
    }

    protected void restoreStructure(Configuration configuration,
                                    LogicalFilterComponent<?> logicalFilterComponent,
                                    List<ComponentNode> structure) {
        // Reconcile this logical component's own children to their initial set, in the original order:
        // drop components added after initialization (e.g. from the URL) and restore those the user
        // removed, recursing into nested groups so the whole configuration tree is rebuilt exactly.
        // Component instances are preserved, so references handed to application code stay valid.
        // For the empty configuration the initial structure is empty, so this clears the root.
        for (FilterComponent component : List.copyOf(logicalFilterComponent.getOwnFilterComponents())) {
            logicalFilterComponent.remove(component);
            configuration.setFilterComponentModified(component, false);
        }
        for (ComponentNode node : structure) {
            logicalFilterComponent.add(node.component());
            configuration.setFilterComponentModified(node.component(), node.modified());
            if (node.component() instanceof LogicalFilterComponent<?> nestedComponent) {
                restoreStructure(configuration, nestedComponent, node.children());
            }
        }
    }

    protected void restoreInitialDefaultValues(RunTimeConfiguration configuration, InitialState state) {
        // The condition remove button and URL-driven operation changes reset registered default
        // values; re-register the initial ones so a later configuration switch restores them
        // instead of falling back to null.
        configuration.resetAllDefaultValues();
        state.defaultValues.forEach(configuration::setFilterComponentDefaultValue);
    }

    protected void restoreInitialStates(InitialState state) {
        // The resettable fields (operation + value) and their restore order are defined in
        // SingleFilterComponentStateSupport. A surviving baseline component can be mutated in place
        // only by #updatePropertyCondition; keep the captured fields in sync if URL matching is
        // extended to JpqlFilter/GroupFilter.
        state.states.forEach(singleFilterComponentStateSupport::restore);
    }

    @Override
    public void updateState(QueryParameters queryParameters) {
        Map<String, List<String>> parameters = queryParameters.getParameters();

        if (parameters.containsKey(getConfigurationParam())) {
            List<String> configurationParam = parameters.get(getConfigurationParam());
            String configurationId = deserializeConfigurationId(configurationParam.get(0));
            Optional<Configuration> currentConfiguration = filter.getConfigurations().stream()
                    .filter(configuration -> configurationId.equals(configuration.getId()))
                    .findAny();

            currentConfiguration.ifPresent(configuration -> {
                if (parameters.containsKey(getConditionParam())) {
                    List<String> conditionParams = parameters.get(getConditionParam());
                    updateConfigurationConditions(configuration, conditionParams);
                }

                FilterUtils.setCurrentConfiguration(filter, configuration, true);
            });
        } else if (parameters.containsKey(getConditionParam())) {
            List<String> conditionParams = parameters.get(getConditionParam());

            // For cases where there is a default design-time configuration.
            // The design-time configuration can't be modified, so an empty configuration is entered
            // to be able to add conditions from the URL query parameters.
            Configuration currentConfiguration = filter.getCurrentConfiguration() instanceof RunTimeConfiguration
                    ? filter.getCurrentConfiguration()
                    : filter.getEmptyConfiguration();

            LogicalFilterComponent<?> rootLogicalFilterComponent = currentConfiguration.getRootLogicalFilterComponent();

            List<UrlPropertyCondition> conditions = deserializeConditions(conditionParams, getEntityMetaClass());
            for (UrlPropertyCondition condition : conditions) {
                PropertyFilter<?> propertyFilter = createPropertyFilter(condition);
                rootLogicalFilterComponent.add(propertyFilter);
                currentConfiguration.setFilterComponentModified(propertyFilter, true);
            }

            FilterUtils.setCurrentConfiguration(filter, currentConfiguration, true);
        }
    }

    protected String deserializeConfigurationId(String configurationParam) {
        return urlParamSerializer.deserialize(String.class, configurationParam);
    }

    /**
     * Parses the condition strings from the URL and keeps the conditions that the filter permits.
     * No value is parsed and no filter component is created at this point.
     *
     * @param conditionParams the condition strings from the URL
     * @param entityMetaClass the meta class of the entity the filter loads
     * @return the parsed conditions that are permitted
     */
    protected List<UrlPropertyCondition> deserializeConditions(List<String> conditionParams,
                                                               MetaClass entityMetaClass) {
        List<UrlPropertyCondition> conditions = new ArrayList<>(conditionParams.size());
        for (String conditionString : conditionParams) {
            UrlPropertyCondition condition = parseCondition(conditionString, entityMetaClass);
            if (condition != null && isPermitted(entityMetaClass, condition)) {
                conditions.add(condition);
            }
        }

        return conditions;
    }

    /**
     * Checks whether the filter permits a condition on the attribute of the URL condition: the attribute
     * passes the filter's property predicate, can be viewed by the current user and is not system-level.
     * Returns {@code false} if the entity has no such attribute.
     *
     * @param entityMetaClass the meta class of the entity the filter loads
     * @param condition       the condition parsed from the URL
     * @return {@code true} if the condition can be applied to the filter
     */
    protected boolean isPermitted(MetaClass entityMetaClass, UrlPropertyCondition condition) {
        MetaPropertyPath propertyPath = getMetadataTools()
                .resolveMetaPropertyPathOrNull(entityMetaClass, condition.property());
        if (propertyPath == null) {
            return false;
        }

        Predicate<MetaPropertyPath> propertyFiltersPredicate = filter.getPropertyFiltersPredicate();
        if (propertyFiltersPredicate != null && !propertyFiltersPredicate.test(propertyPath)) {
            return false;
        }

        EntityAttributeContext context = new EntityAttributeContext(propertyPath);
        accessManager.applyRegisteredConstraints(context);
        if (!context.canView()) {
            return false;
        }

        return !propertyPath.getMetaProperty().getAnnotatedElement().isAnnotationPresent(SystemLevel.class);
    }

    /**
     * Applies the conditions from the URL to the given configuration. Each condition is matched onto
     * a property condition of the configuration, and each property condition of the configuration is matched
     * by at most one URL condition; an unmatched condition is added as a new condition to a runtime
     * configuration and is ignored for a design-time one.
     *
     * @param currentConfiguration the configuration to apply the conditions to
     * @param conditionParams      the condition strings from the URL
     */
    protected void updateConfigurationConditions(Configuration currentConfiguration, List<String> conditionParams) {
        LogicalFilterComponent<?> rootLogicalFilterComponent = currentConfiguration.getRootLogicalFilterComponent();

        List<UrlPropertyCondition> conditions = deserializeConditions(conditionParams, getEntityMetaClass());
        List<FilterComponent> configurationComponents = rootLogicalFilterComponent.getFilterComponents();

        for (UrlPropertyCondition condition : conditions) {
            PropertyFilter<?> usedPropertyFilter = null;

            for (int i = 0; i < configurationComponents.size() && usedPropertyFilter == null; ++i) {
                if (configurationComponents.get(i) instanceof PropertyFilter<?> configurationPropertyFilter) {
                    usedPropertyFilter = updatePropertyCondition(configurationPropertyFilter, condition);
                }
            }

            if (usedPropertyFilter != null) {
                configurationComponents.remove(usedPropertyFilter);
            } else if (currentConfiguration instanceof RunTimeConfiguration) {
                PropertyFilter<?> propertyFilter = createPropertyFilter(condition);
                currentConfiguration.setFilterComponentModified(propertyFilter, true);
                rootLogicalFilterComponent.add(propertyFilter);
            } else {
                log.debug("Cannot add URL condition on attribute '{}' to design-time configuration '{}'",
                        condition.property(), currentConfiguration.getId());
            }
        }
    }

    /**
     * Applies the URL condition to the given property condition of a configuration if it has the same
     * attribute. The operation of the property condition is changed only if it is editable, and the value
     * is set only if the operations then match.
     *
     * @param configurationComponent the property condition of a configuration
     * @param condition              the condition parsed from the URL
     * @return the property condition if the URL condition is applied to it, {@code null} otherwise
     */
    @Nullable
    protected PropertyFilter<?> updatePropertyCondition(PropertyFilter<?> configurationComponent,
                                                        UrlPropertyCondition condition) {
        if (isPropertyMatched(configurationComponent, condition)) {
            if (configurationComponent.isOperationEditable()) {
                configurationComponent.setOperation(condition.operation());
            }

            if (isOperationMatched(configurationComponent, condition)) {
                Object value = parseConditionValue(getEntityMetaClass(), condition);
                if (value != null) {
                    setConditionValue(configurationComponent, value);
                }

                return configurationComponent;
            }
        }

        return null;
    }

    /**
     * Parses a condition string from the URL.
     *
     * @param conditionString the condition string from the URL
     * @param entityMetaClass the meta class of the entity the filter loads
     * @return the parsed condition, or {@code null} if the condition cannot be applied to the entity
     * @throws IllegalStateException if the condition type is unknown
     */
    @Nullable
    protected UrlPropertyCondition parseCondition(String conditionString, MetaClass entityMetaClass) {
        if (conditionString.startsWith(PROPERTY_CONDITION_PREFIX)) {
            String propertyConditionString = conditionString.substring(PROPERTY_CONDITION_PREFIX.length());
            return parsePropertyCondition(propertyConditionString, entityMetaClass);
        }

        throw new IllegalStateException("Unknown condition type: " + conditionString);
    }

    /**
     * Parses a property condition string from the URL, without the {@link #PROPERTY_CONDITION_PREFIX},
     * into the attribute, the operation and the raw value string.
     *
     * @param conditionString the property condition string from the URL
     * @param entityMetaClass the meta class of the entity the filter loads
     * @return the parsed condition, or {@code null} if the entity has no such attribute or the operation
     * is not available for it
     * @throws IllegalStateException if the condition string has no separator between its parts
     */
    @Nullable
    protected UrlPropertyCondition parsePropertyCondition(String conditionString, MetaClass entityMetaClass) {
        int separatorIndex = conditionString.indexOf(SEPARATOR);
        if (separatorIndex == -1) {
            throw new IllegalStateException("Can't parse property condition: " + conditionString);
        }

        String propertyString = conditionString.substring(0, separatorIndex);
        String property = urlParamSerializer.deserialize(String.class,
                filterUrlQueryParametersSupport.restoreSeparatorValue(propertyString));

        MetaPropertyPath propertyPath = getMetadataTools().resolveMetaPropertyPathOrNull(entityMetaClass, property);
        if (propertyPath == null) {
            log.info("Cannot apply URL condition. Attribute '{}' is not found in the '{}' entity",
                    property, entityMetaClass.getName());
            return null;
        }

        conditionString = conditionString.substring(separatorIndex + 1);
        separatorIndex = conditionString.indexOf(SEPARATOR);
        if (separatorIndex == -1) {
            throw new IllegalStateException("Can't parse property condition: " + conditionString);
        }

        String operationString = conditionString.substring(0, separatorIndex);
        PropertyFilter.Operation operation = urlParamSerializer
                .deserialize(PropertyFilter.Operation.class,
                        filterUrlQueryParametersSupport.restoreSeparatorValue(operationString));

        if (!propertyFilterSupport.getAvailableOperations(propertyPath).contains(operation)) {
            log.debug("Cannot apply URL condition. " +
                            "Operation '{}' is not available for attribute '{}' of the '{}' entity",
                    operation, property, entityMetaClass.getName());
            return null;
        }

        String valueString = conditionString.substring(separatorIndex + 1);
        return new UrlPropertyCondition(property, operation, Strings.emptyToNull(valueString));
    }

    /**
     * Creates a new property condition from the URL condition, with the parsed value set.
     *
     * @param condition the condition parsed from the URL
     * @return a new property condition to add to a configuration
     */
    @SuppressWarnings("unchecked")
    protected PropertyFilter<?> createPropertyFilter(UrlPropertyCondition condition) {
        PropertyFilterCondition model = metadata.create(PropertyFilterCondition.class);
        model.setProperty(condition.property());
        model.setOperation(condition.operation());
        model.setOperationEditable(true);
        model.setParameterName(PropertyConditionUtils.generateParameterName(condition.property()));
        model.setValueComponent(metadata.create(FilterValueComponent.class));

        FilterConverter<PropertyFilter<?>, PropertyFilterCondition> converter =
                (FilterConverter<PropertyFilter<?>, PropertyFilterCondition>) filterComponents
                        .getConverterByModelClass(PropertyFilterCondition.class, filter);
        PropertyFilter<?> propertyFilter = converter.convertToComponent(model);

        // The value is set before the condition is added to a configuration: adding it updates the URL
        // from the condition's current value.
        Object value = parseConditionValue(getEntityMetaClass(), condition);
        if (value != null) {
            setConditionValue(propertyFilter, value);
        }

        return propertyFilter;
    }

    /**
     * Parses the value of the URL condition for its attribute and operation.
     *
     * @param entityMetaClass the meta class of the entity the filter loads
     * @param condition       the condition parsed from the URL
     * @return the parsed value, or {@code null} if the URL condition has no value or it cannot be parsed
     */
    @Nullable
    protected Object parseConditionValue(MetaClass entityMetaClass, UrlPropertyCondition condition) {
        String valueString = condition.valueString();
        if (valueString == null) {
            return null;
        }

        try {
            return filterUrlQueryParametersSupport.parseValue(entityMetaClass, condition.property(),
                    condition.operation().getType(), valueString);
        } catch (Exception e) {
            log.info("Cannot parse URL parameter. {}", e.toString());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private void setConditionValue(PropertyFilter<?> propertyFilter, Object value) {
        // A value the value component rejects leaves the condition without it instead of failing the navigation.
        try {
            UiComponentUtils.setValue((PropertyFilter<Object>) propertyFilter, value);
        } catch (Exception e) {
            log.info("Cannot parse URL parameter. {}", e.toString());
        }
    }

    private MetaClass getEntityMetaClass() {
        return filter.getDataLoader().getContainer().getEntityMetaClass();
    }

    protected void bindFilterComponentsChangeListener(GenericFilter filter) {
        LogicalFilterComponent<?> rootLogicalFilterComponent = filter.getCurrentConfiguration()
                .getRootLogicalFilterComponent();
        filterComponentsChangeRegistration = rootLogicalFilterComponent
                .addFilterComponentsChangeListener(this::onFilterComponentsChanged);
    }

    protected void unbindFilterComponentsChange() {
        if (filterComponentsChangeRegistration != null) {
            filterComponentsChangeRegistration.remove();
            filterComponentsChangeRegistration = null;
        }
    }

    /**
     * Returns the current configuration parameter name for the URL. If the parameter
     * is null or empty, a default name is returned.
     *
     * @return the configuration parameter name if set, otherwise the default configuration parameter name
     */
    public String getConfigurationParam() {
        return getOwnerId("genericFilter") + "_" +
                (Strings.isNullOrEmpty(configurationParam) ? DEFAULT_CONFIGURATION_PARAM : configurationParam);
    }

    /**
     * Sets the condition parameter name for the URL.
     *
     * @param conditionParam the condition parameter name to set
     */
    public void setConfigurationParam(@Nullable String conditionParam) {
        this.conditionParam = conditionParam;
    }

    /**
     * Returns the current condition parameter name for the URL. If the condition parameter
     * is null or empty, a default name is returned.
     *
     * @return the condition parameter name if set, otherwise the default condition parameter name
     */
    public String getConditionParam() {
        return getOwnerId("genericFilter") + "_" +
                (Strings.isNullOrEmpty(conditionParam) ? DEFAULT_CONDITION_PARAM : conditionParam);
    }

    /**
     * Sets the condition parameter name for the URL.
     *
     * @param conditionParam the condition parameter name to set
     */
    public void setConditionParam(@Nullable String conditionParam) {
        this.conditionParam = conditionParam;
    }

    /**
     * Checks whether the property condition and the URL condition have the same attribute.
     *
     * @param propertyFilter the property condition of a configuration
     * @param condition      the condition parsed from the URL
     * @return {@code true} if the attributes are the same
     */
    protected boolean isPropertyMatched(PropertyFilter<?> propertyFilter, UrlPropertyCondition condition) {
        return Objects.equals(propertyFilter.getProperty(), condition.property());
    }

    /**
     * Checks whether the property condition and the URL condition have the same operation.
     *
     * @param propertyFilter the property condition of a configuration
     * @param condition      the condition parsed from the URL
     * @return {@code true} if the operations are the same
     */
    protected boolean isOperationMatched(PropertyFilter<?> propertyFilter, UrlPropertyCondition condition) {
        return Objects.equals(propertyFilter.getOperation(), condition.operation());
    }

    protected MetadataTools getMetadataTools() {
        if (metadataTools == null) {
            metadataTools = applicationContext.getBean(MetadataTools.class);
        }
        return metadataTools;
    }

    @Nullable
    @Override
    public Component getComponent() {
        return filter;
    }

    /**
     * A POJO class for storing the {@link GenericFilter}'s initial state.
     *
     * @param configuration the current configuration at initialization
     * @param structure     the configuration's initial component tree — each root child in order,
     *                      recursively including nested groups' own children — that a restore
     *                      reconstructs (re-adding user-removed components, dropping URL-added ones),
     *                      carrying each component's {@code modified} flag (controls its remove button)
     * @param states        the initial state (operation and value) of the baseline single filter
     *                      components, keyed by identity, used to reset components whose operation or
     *                      value was changed via the URL
     * @param defaultValues the initial default values registered for the configuration, keyed by
     *                      parameter name, re-registered on restore so a later configuration switch
     *                      keeps them instead of falling back to null
     */
    @Internal
    protected record InitialState(Configuration configuration,
                                  List<ComponentNode> structure,
                                  Map<SingleFilterComponentBase<?>, SingleFilterComponentStateSupport.State> states,
                                  Map<String, Object> defaultValues) {
    }

    /**
     * A node of the captured configuration structure: a filter component, its initial {@code modified}
     * flag, and, for a nested {@link LogicalFilterComponent}, its own ordered child nodes.
     *
     * @param component the filter component
     * @param modified  the component's {@code modified} flag at initialization
     * @param children  the ordered child nodes for a nested logical component, empty otherwise
     */
    @Internal
    protected record ComponentNode(FilterComponent component,
                                   boolean modified,
                                   List<ComponentNode> children) {
    }

    /**
     * A property condition parsed from the URL. The value is kept as the raw string from the URL and is
     * parsed only when the condition is applied to a property condition of the filter.
     *
     * @param property    the attribute path
     * @param operation   the filtering operation
     * @param valueString the value string from the URL, or {@code null} if the URL has no value
     */
    protected record UrlPropertyCondition(String property,
                                          PropertyFilter.Operation operation,
                                          @Nullable String valueString) {
    }
}
