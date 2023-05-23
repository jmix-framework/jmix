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
import io.jmix.flowui.facet.UrlQueryParametersFacet.UrlQueryParametersChangeEvent;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.KeyValueCollectionLoader;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import org.springframework.context.ApplicationContext;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Map;

import static io.jmix.flowui.facet.urlqueryparameters.FilterUrlQueryParametersSupport.SEPARATOR;
import static java.util.Objects.requireNonNull;

public class GenericFilterUrlQueryParametersBinder extends AbstractUrlQueryParametersBinder {

    public static final String NAME = "genericFilter";

    public static final String PROPERTY_CONDITION_PREFIX = "property:";
    public static final String DEFAULT_CONDITION_PARAM = "genericFilterCondition";

    protected GenericFilter filter;

    protected String conditionParam;

    protected ApplicationContext applicationContext;
    protected UrlParamSerializer urlParamSerializer;
    protected UiComponents uiComponents;
    protected SingleFilterSupport singleFilterSupport;
    protected FilterUrlQueryParametersSupport filterUrlQueryParametersSupport;

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
        uiComponents = applicationContext.getBean(UiComponents.class);
        filterUrlQueryParametersSupport = applicationContext.getBean(FilterUrlQueryParametersSupport.class);
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

        List<Condition> conditions = queryCondition.getConditions();
        List<String> params = new ArrayList<>(conditions.size());
        for (Condition condition : conditions) {
            if (condition instanceof PropertyCondition) {
                params.add(serializePropertyCondition(((PropertyCondition) condition)));
            }
        }

        QueryParameters queryParameters =
                new QueryParameters(ImmutableMap.of(getConditionParam(), params));
        fireQueryParametersChanged(new UrlQueryParametersChangeEvent(this, queryParameters));
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
    public void updateState(QueryParameters queryParameters) {
        Map<String, List<String>> parameters = queryParameters.getParameters();
        if (parameters.containsKey(getConditionParam())) {
            List<String> conditionParams = queryParameters.getParameters().get(getConditionParam());

            Configuration currentConfiguration = filter.getCurrentConfiguration();
            LogicalFilterComponent<?> rootLogicalFilterComponent = currentConfiguration.getRootLogicalFilterComponent();

            List<FilterComponent> conditions = deserializeConditions(conditionParams,
                    rootLogicalFilterComponent.getDataLoader());
            conditions.forEach(filterComponent -> {
                rootLogicalFilterComponent.add(filterComponent);
                currentConfiguration.setFilterComponentModified(filterComponent, true);
            });

            FilterUtils.setCurrentConfiguration(filter, currentConfiguration, true);
        }
    }

    protected List<FilterComponent> deserializeConditions(List<String> conditionParams, DataLoader dataLoader) {
        List<FilterComponent> conditions = new ArrayList<>(conditionParams.size());
        for (String conditionString : conditionParams) {
            conditions.add(parseCondition(conditionString, dataLoader));
        }

        return conditions;
    }

    protected FilterComponent parseCondition(String conditionString, DataLoader dataLoader) {
        if (conditionString.startsWith(PROPERTY_CONDITION_PREFIX)) {
            String propertyConditionString = conditionString.substring(PROPERTY_CONDITION_PREFIX.length());
            return parsePropertyCondition(propertyConditionString, dataLoader);
        }

        throw new IllegalStateException("Unknown condition type: " + conditionString);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected PropertyFilter<?> parsePropertyCondition(String conditionString, DataLoader dataLoader) {
        int separatorIndex = conditionString.indexOf(SEPARATOR);
        if (separatorIndex == -1) {
            throw new IllegalStateException("Can't parse property condition: " + conditionString);
        }

        String propertyString = conditionString.substring(0, separatorIndex);
        String property = urlParamSerializer.deserialize(String.class,
                filterUrlQueryParametersSupport.restoreSeparatorValue(propertyString));

        conditionString = conditionString.substring(separatorIndex + 1);
        separatorIndex = conditionString.indexOf(SEPARATOR);
        if (separatorIndex == -1) {
            throw new IllegalStateException("Can't parse property condition: " + conditionString);
        }

        String operationString = conditionString.substring(0, separatorIndex);
        PropertyFilter.Operation operation = urlParamSerializer
                .deserialize(PropertyFilter.Operation.class,
                        filterUrlQueryParametersSupport.restoreSeparatorValue(operationString));

        PropertyFilter propertyFilter = uiComponents.create(PropertyFilter.class);
        propertyFilter.setProperty(property);
        propertyFilter.setOperation(operation);
        // TODO: gg, change when configurations and custom conditions will be implemented
        propertyFilter.setOperationEditable(true);

        propertyFilter.setParameterName(PropertyConditionUtils.generateParameterName(property));
        propertyFilter.setDataLoader(dataLoader);

        propertyFilter.setValueComponent(generatePropertyFilterValueComponent(propertyFilter));

        String valueString = conditionString.substring(separatorIndex + 1);
        if (!Strings.isNullOrEmpty(valueString)) {
            Object parsedValue = filterUrlQueryParametersSupport
                    .parseValue(dataLoader.getContainer().getEntityMetaClass(),
                            property, operation.getType(), valueString);
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

    public String getConditionParam() {
        return Strings.isNullOrEmpty(conditionParam) ? DEFAULT_CONDITION_PARAM : conditionParam;
    }

    public void setConditionParam(@Nullable String conditionParam) {
        this.conditionParam = conditionParam;
    }

    protected SingleFilterSupport getSingleFilterSupport() {
        if (singleFilterSupport == null) {
            singleFilterSupport = applicationContext.getBean(SingleFilterSupport.class);
        }
        return singleFilterSupport;
    }
}
