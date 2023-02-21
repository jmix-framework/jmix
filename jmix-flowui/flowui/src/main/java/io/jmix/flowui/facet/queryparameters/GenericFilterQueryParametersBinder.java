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

package io.jmix.flowui.facet.queryparameters;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.core.querycondition.PropertyConditionUtils;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.filer.FilterComponent;
import io.jmix.flowui.component.genericfilter.Configuration;
import io.jmix.flowui.component.genericfilter.FilterUtils;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent.FilterComponentsChangeEvent;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.component.propertyfilter.SingleFilterSupport;
import io.jmix.flowui.facet.QueryParametersFacet.QueryParametersChangeEvent;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.KeyValueCollectionLoader;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class GenericFilterQueryParametersBinder extends AbstractQueryParametersBinder {

    public static final String NAME = "genericFilter";

    public static final String PROPERTY_CONDITION_PREFIX = "propertyCondition";

    public static final String CONDITIONS_PARAM = "genericFilterConditions";
    public static final String CONDITIONS_SEPARATOR = ";";
    public static final String CONDITIONS_EMPTY_VALUE = "<empty>";

    public static final String CONDITION_TYPE_SEPARATOR = ":";
    public static final String CONDITION_VALUES_SEPARATOR = "_";

    protected GenericFilter filter;

    protected String conditionsParam;

    protected ApplicationContext applicationContext;
    protected UrlParamSerializer urlParamSerializer;
    protected UiComponents uiComponents;
    protected SingleFilterSupport singleFilterSupport;
    protected QueryParametersSupport queryParametersSupport;

    public GenericFilterQueryParametersBinder(GenericFilter filter,
                                              UrlParamSerializer urlParamSerializer,
                                              ApplicationContext applicationContext) {
        this.filter = filter;
        this.urlParamSerializer = urlParamSerializer;
        this.applicationContext = applicationContext;

        autowireDependencies();
        initComponent(filter);
    }

    protected void autowireDependencies() {
        uiComponents = applicationContext.getBean(UiComponents.class);
        queryParametersSupport = applicationContext.getBean(QueryParametersSupport.class);
    }

    protected Registration filterComponentsChangeRegistration;

    protected void initComponent(GenericFilter filter) {
        filter.addConfigurationChangeListener(this::onConfigurationChanged);
        bindFilterComponentsChangeListener(filter);
        bindDataLoaderListener(filter);
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
        Configuration currentConfiguration = filter.getCurrentConfiguration();
        LogicalCondition queryCondition = currentConfiguration.getQueryCondition();

        String conditionsString;
        List<Condition> conditions = queryCondition.getConditions();
        if (conditions.isEmpty()) {
            conditionsString = CONDITIONS_EMPTY_VALUE;
        } else {
            List<String> params = new ArrayList<>(conditions.size());
            for (Condition condition : conditions) {
                if (condition instanceof PropertyCondition) {
                    params.add(serializePropertyCondition(((PropertyCondition) condition)));
                }
            }

            conditionsString = StringUtils.join(params, CONDITIONS_SEPARATOR);
        }

        QueryParameters queryParameters = QueryParameters
                .simple(ImmutableMap.of(getConditionsParam(), conditionsString));

        fireQueryParametersChanged(new QueryParametersChangeEvent(this, queryParameters));
    }

    protected String serializePropertyCondition(PropertyCondition condition) {
        String property = urlParamSerializer.serialize(condition.getProperty());
        String operation = urlParamSerializer.serialize(
                queryParametersSupport.convertFromEnumName(condition.getOperation()));
        Object parameterValue = urlParamSerializer.serialize(
                queryParametersSupport.getSerializableValue(condition.getParameterValue()));

        return PROPERTY_CONDITION_PREFIX + CONDITION_TYPE_SEPARATOR +
                property + CONDITION_VALUES_SEPARATOR +
                operation + CONDITION_VALUES_SEPARATOR +
                parameterValue;
    }

    @Override
    public void updateState(QueryParameters queryParameters) {
        Map<String, List<String>> parameters = queryParameters.getParameters();
        if (parameters.containsKey(getConditionsParam())) {
            String conditionsValue = queryParameters.getParameters().get(getConditionsParam()).get(0);

            if (CONDITIONS_EMPTY_VALUE.equals(conditionsValue)) {
                return;
            }

            Configuration currentConfiguration = filter.getCurrentConfiguration();
            LogicalFilterComponent<?> rootLogicalFilterComponent = currentConfiguration.getRootLogicalFilterComponent();

            List<FilterComponent> conditions = deserializeConditions(conditionsValue,
                    rootLogicalFilterComponent.getDataLoader());
            conditions.forEach(filterComponent -> {
                rootLogicalFilterComponent.add(filterComponent);
                currentConfiguration.setFilterComponentModified(filterComponent, true);
            });

            FilterUtils.setCurrentConfiguration(filter, currentConfiguration, true);
        }
    }

    protected List<FilterComponent> deserializeConditions(String conditionsValue, DataLoader dataLoader) {
        conditionsValue = urlParamSerializer.deserialize(String.class, conditionsValue);
        String[] conditionStrings = conditionsValue.split(CONDITIONS_SEPARATOR);

        List<FilterComponent> conditions = new ArrayList<>(conditionStrings.length);
        for (String conditionString : conditionStrings) {
            conditions.add(parseCondition(conditionString, dataLoader));
        }

        return conditions;
    }

    protected FilterComponent parseCondition(String conditionString, DataLoader dataLoader) {
        String[] conditionParts = conditionString.split(CONDITION_TYPE_SEPARATOR);
        if (conditionParts.length != 2) {
            throw new IllegalStateException("Can't parse condition string: " + conditionString);
        }

        String conditionType = conditionParts[0];
        String conditionValue = conditionParts[1];

        if (conditionType.equals(PROPERTY_CONDITION_PREFIX)) {
            return parsePropertyCondition(conditionValue, dataLoader);
        }

        throw new IllegalStateException("Unknown condition type: " + conditionType);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected PropertyFilter<?> parsePropertyCondition(String conditionString, DataLoader dataLoader) {
        String[] values = conditionString.split(CONDITION_VALUES_SEPARATOR);

        PropertyFilter propertyFilter = uiComponents.create(PropertyFilter.class);

        String property = urlParamSerializer.deserialize(String.class, values[0]);
        propertyFilter.setProperty(property);

        PropertyFilter.Operation operation = urlParamSerializer
                .deserialize(PropertyFilter.Operation.class,
                        queryParametersSupport.convertToEnumName(values[1]));
        propertyFilter.setOperation(operation);
        // TODO: gg, change when configurations and custom conditions will be implemented
        propertyFilter.setOperationEditable(true);

        propertyFilter.setParameterName(PropertyConditionUtils.generateParameterName(property));
        propertyFilter.setDataLoader(dataLoader);

        propertyFilter.setValueComponent(generatePropertyFilterValueComponent(propertyFilter));

        if (values.length == 3
                && !Strings.isNullOrEmpty(values[2])) {
            Object parsedValue = queryParametersSupport
                    .parseValue(dataLoader.getContainer().getEntityMetaClass(),
                            property, operation.getType(), values[2]);
            propertyFilter.setValue(parsedValue);
        }

        return propertyFilter;
    }

    protected HasValueAndElement<?, ?> generatePropertyFilterValueComponent(PropertyFilter<?> propertyFilter) {
        MetaClass metaClass = propertyFilter.getDataLoader().getContainer().getEntityMetaClass();
        return getSingleFilterSupport().generateValueComponent(metaClass,
                requireNonNull(propertyFilter.getProperty()), propertyFilter.getOperation());
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

    public String getConditionsParam() {
        return Strings.isNullOrEmpty(conditionsParam) ? CONDITIONS_PARAM : conditionsParam;
    }

    public void setConditionsParam(@Nullable String conditionsParam) {
        this.conditionsParam = conditionsParam;
    }

    protected SingleFilterSupport getSingleFilterSupport() {
        if (singleFilterSupport == null) {
            singleFilterSupport = applicationContext.getBean(SingleFilterSupport.class);
        }
        return singleFilterSupport;
    }
}
