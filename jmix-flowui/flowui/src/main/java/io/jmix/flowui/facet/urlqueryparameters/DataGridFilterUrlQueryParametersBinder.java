/*
 * Copyright 2023 Haulmont.
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
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.router.QueryParameters;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.component.grid.headerfilter.DataGridHeaderFilter;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.data.grid.ContainerDataGridItems;
import io.jmix.flowui.facet.UrlQueryParametersFacet;
import io.jmix.flowui.view.navigation.RouteSupport;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.jmix.flowui.facet.urlqueryparameters.FilterUrlQueryParametersSupport.SEPARATOR;

/**
 * This class provides functionality to bind filters in {@link DataGrid} to URL query parameters.
 */
public class DataGridFilterUrlQueryParametersBinder extends AbstractUrlQueryParametersBinder
        implements HasInitialState {

    private static final Logger log = LoggerFactory.getLogger(DataGridFilterUrlQueryParametersBinder.class);

    public static final String NAME = "dataGridFilter";

    protected Grid<?> grid;

    protected String parameter;

    protected ApplicationContext applicationContext;
    protected UrlParamSerializer urlParamSerializer;
    protected FilterUrlQueryParametersSupport filterUrlQueryParametersSupport;
    protected RouteSupport routeSupport;

    protected List<InitialState> initialStates = new ArrayList<>();

    public DataGridFilterUrlQueryParametersBinder(Grid<?> grid,
                                                  UrlParamSerializer urlParamSerializer,
                                                  ApplicationContext applicationContext) {
        this.grid = grid;
        this.urlParamSerializer = urlParamSerializer;
        this.applicationContext = applicationContext;

        autowireDependencies();
        initComponent(grid);
    }

    protected void autowireDependencies() {
        filterUrlQueryParametersSupport = applicationContext.getBean(FilterUrlQueryParametersSupport.class);
        routeSupport = applicationContext.getBean(RouteSupport.class);
    }

    protected void initComponent(Grid<?> grid) {
        for (Grid.Column<?> column : grid.getColumns()) {
            if (column instanceof DataGridColumn<?> dataGridColumn
                    && dataGridColumn.isFilterable()) {
                setupColumn(dataGridColumn);
            }
        }
    }

    @Override
    public void saveInitialState() {
        for (Grid.Column<?> column : grid.getColumns()) {
            if (column instanceof DataGridColumn<?> dataGridColumn
                    && dataGridColumn.isFilterable()
                    && dataGridColumn.getHeaderComponent() instanceof DataGridHeaderFilter headerFilter) {
                PropertyFilter<?> propertyFilter = headerFilter.getPropertyFilter();

                initialStates.add(
                        new InitialState(
                                dataGridColumn.getKey(),
                                Objects.requireNonNull(propertyFilter.getProperty()),
                                propertyFilter.getOperation(),
                                propertyFilter.getValue()
                        )
                );
            }
        }
    }

    protected void setupColumn(DataGridColumn<?> column) {
        DataGridHeaderFilter headerFilter = (DataGridHeaderFilter) column.getHeaderComponent();
        headerFilter.addApplyListener(__ -> updateQueryParameters());
    }

    protected void updateQueryParameters() {
        List<String> parameters = grid.getColumns().stream()
                .filter(column -> column instanceof DataGridColumn<?> dataGridColumn
                        && dataGridColumn.isFilterable()
                        && dataGridColumn.getHeaderComponent() instanceof DataGridHeaderFilter headerFilter
                        && headerFilter.isFilterApplied())
                .map(column -> serializePropertyFilter(
                        column.getKey(),
                        ((DataGridHeaderFilter) column.getHeaderComponent()).getPropertyFilter())
                )
                .toList();

        QueryParameters queryParameters = new QueryParameters(ImmutableMap.of(getParameter(), parameters));

        fireQueryParametersChanged(new UrlQueryParametersFacet.UrlQueryParametersChangeEvent(this, queryParameters));
    }

    protected String serializePropertyFilter(String columnKey, PropertyFilter<?> propertyFilter) {
        String key = urlParamSerializer
                .serialize(filterUrlQueryParametersSupport.replaceSeparatorValue(
                        Objects.requireNonNull(columnKey)));
        String property = urlParamSerializer
                .serialize(filterUrlQueryParametersSupport.replaceSeparatorValue(
                        Objects.requireNonNull(propertyFilter.getProperty())));
        String operation = urlParamSerializer
                .serialize(filterUrlQueryParametersSupport.replaceSeparatorValue(
                        propertyFilter.getOperation().name().toLowerCase()));
        Object parameterValue = urlParamSerializer
                .serialize(filterUrlQueryParametersSupport.getSerializableValue(propertyFilter.getValue()));

        return key + SEPARATOR +
                property + SEPARATOR +
                operation + SEPARATOR +
                parameterValue;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void applyInitialState() {
        for (InitialState initialState : initialStates) {
            Grid.Column<?> column = grid.getColumnByKey(initialState.key);

            if (column instanceof DataGridColumn<?> dataGridColumn
                    && dataGridColumn.isFilterable()
                    && dataGridColumn.getHeaderComponent() instanceof DataGridHeaderFilter headerFilter
                    && initialState.property.equals(headerFilter.getPropertyFilter().getProperty())) {
                PropertyFilter propertyFilter = headerFilter.getPropertyFilter();

                propertyFilter.setOperation(initialState.operation);
                propertyFilter.setValue(initialState.value);
                headerFilter.apply();
            }
        }
    }

    @Override
    public void updateState(QueryParameters queryParameters) {
        Map<String, List<String>> parameters = queryParameters.getParameters();
        if (parameters.containsKey(getParameter())) {
            applyPropertyFilterParameters(parameters.get(getParameter()));
        }
    }

    protected void applyPropertyFilterParameters(List<String> params) {
        for (String parameterString : params) {
            if (StringUtils.countOccurrencesOf(parameterString, SEPARATOR) == 3) {
                applyPropertyFilterParameter(parameterString);
            } else {
                // use legacy api
                _applyPropertyFilterParameter(parameterString);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void applyPropertyFilterParameter(String parameterString) {
        int separatorIndex = parameterString.indexOf(SEPARATOR);

        if (separatorIndex == -1) {
            throw new IllegalStateException("Can't parse property condition: " + parameterString);
        }

        String keyString = parameterString.substring(0, separatorIndex);

        parameterString = parameterString.substring(separatorIndex + 1);
        separatorIndex = parameterString.indexOf(SEPARATOR);
        if (separatorIndex == -1) {
            throw new IllegalStateException("Can't parse property condition: " + parameterString);
        }

        String propertyString = parameterString.substring(0, separatorIndex);
        String property = urlParamSerializer.deserialize(String.class,
                filterUrlQueryParametersSupport.restoreSeparatorValue(propertyString));

        DataGridColumn<?> column = (DataGridColumn<?>) grid.getColumnByKey(keyString);
        if (column == null) {
            throw new IllegalStateException("Can't find column with property: " + property);
        }
        if (!column.isFilterable()) {
            throw new IllegalStateException("Column must be filterable");
        }

        parameterString = parameterString.substring(separatorIndex + 1);
        separatorIndex = parameterString.indexOf(SEPARATOR);
        if (separatorIndex == -1) {
            throw new IllegalStateException("Can't parse property condition: " + parameterString);
        }

        String operationString = parameterString.substring(0, separatorIndex);
        PropertyFilter.Operation operation = urlParamSerializer
                .deserialize(PropertyFilter.Operation.class,
                        filterUrlQueryParametersSupport.restoreSeparatorValue(operationString));

        DataGridHeaderFilter headerFilter = (DataGridHeaderFilter) column.getHeaderComponent();
        PropertyFilter propertyFilter = headerFilter.getPropertyFilter();
        propertyFilter.setOperation(operation);

        String valueString = parameterString.substring(separatorIndex + 1);
        if (!Strings.isNullOrEmpty(valueString)) {
            try {
                Object parsedValue = filterUrlQueryParametersSupport
                        .parseValue(((ContainerDataGridItems<?>) grid.getDataProvider()).getEntityMetaClass(),
                                property, operation.getType(), valueString);
                propertyFilter.setValue(parsedValue);
            } catch (Exception e) {
                log.info("Cannot parse URL parameter. {}", e.toString());
                propertyFilter.setValue(null);
            }
        }

        headerFilter.apply();
    }

    /**
     * @deprecated use {@link #applyPropertyFilterParameter(String)} instead
     */
    @Deprecated(since = "2.3", forRemoval = true)
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void _applyPropertyFilterParameter(String parameterString) {
        int separatorIndex = parameterString.indexOf(SEPARATOR);

        if (separatorIndex == -1) {
            throw new IllegalStateException("Can't parse property condition: " + parameterString);
        }

        String propertyString = parameterString.substring(0, separatorIndex);
        String property = urlParamSerializer.deserialize(String.class,
                filterUrlQueryParametersSupport.restoreSeparatorValue(propertyString));

        DataGridColumn<?> column = (DataGridColumn<?>) grid.getColumnByKey(property);
        if (column == null) {
            throw new IllegalStateException("Can't find column with property: " + property);
        }
        if (!column.isFilterable()) {
            throw new IllegalStateException("Column must be filterable");
        }

        parameterString = parameterString.substring(separatorIndex + 1);
        separatorIndex = parameterString.indexOf(SEPARATOR);
        if (separatorIndex == -1) {
            throw new IllegalStateException("Can't parse property condition: " + parameterString);
        }

        String operationString = parameterString.substring(0, separatorIndex);
        PropertyFilter.Operation operation = urlParamSerializer
                .deserialize(PropertyFilter.Operation.class,
                        filterUrlQueryParametersSupport.restoreSeparatorValue(operationString));

        DataGridHeaderFilter headerFilter = (DataGridHeaderFilter) column.getHeaderComponent();
        PropertyFilter propertyFilter = headerFilter.getPropertyFilter();
        propertyFilter.setOperation(operation);

        String valueString = parameterString.substring(separatorIndex + 1);
        if (!Strings.isNullOrEmpty(valueString)) {
            try {
                Object parsedValue = filterUrlQueryParametersSupport
                        .parseValue(((ContainerDataGridItems<?>) grid.getDataProvider()).getEntityMetaClass(),
                                property, operation.getType(), valueString);
                propertyFilter.setValue(parsedValue);
            } catch (Exception e) {
                log.info("Cannot parse URL parameter. {}", e.toString());
                propertyFilter.setValue(null);
            }
        }

        headerFilter.apply();
    }

    /**
     * Returns the parameter name for the associated {@link DataGrid} component.
     * If the parameter is not explicitly set, constructs a default value based on the grid's ID
     * with "Filter" appended to it.
     *
     * @return the value of the parameter if set, or a generated default value based on the grid's ID
     */
    public String getParameter() {
        return Strings.isNullOrEmpty(parameter)
                ? grid.getId().orElseThrow(() ->
                new IllegalStateException("Component has neither id nor explicit url query param"))
                + "Filter"
                : parameter;
    }

    /**
     * Sets the parameter name for the associated {@link DataGrid} component.
     *
     * @param parameter the parameter value to set; may be {@code null} if no specific value is provided
     */
    public void setParameter(@Nullable String parameter) {
        this.parameter = parameter;
    }

    @Nullable
    @Override
    public Component getComponent() {
        return grid;
    }

    /**
     * A POJO class for storing properties of the {@link DataGridHeaderFilter}'s initial state.
     *
     * @param key       the value of {@code key} of the filter
     * @param property  the value of {@code property} of the filter
     * @param operation the value of {@code operation} at initialization
     * @param value     the value of {@code value} at initialization
     */
    protected record InitialState(String key, String property, PropertyFilter.Operation operation, Object value) {
    }
}
