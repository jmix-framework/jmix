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
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.component.grid.EnhancedDataGrid;
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

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.jmix.flowui.facet.urlqueryparameters.FilterUrlQueryParametersSupport.SEPARATOR;

public class DataGridFilterUrlQueryParametersBinder<T extends Grid<?> & EnhancedDataGrid<?>>
        extends AbstractUrlQueryParametersBinder {

    private static final Logger log = LoggerFactory.getLogger(DataGridFilterUrlQueryParametersBinder.class);

    public static final String NAME = "dataGridFilter";

    protected T grid;

    protected String parameter;

    protected ApplicationContext applicationContext;
    protected UrlParamSerializer urlParamSerializer;
    protected FilterUrlQueryParametersSupport filterUrlQueryParametersSupport;
    protected RouteSupport routeSupport;
    protected MetadataTools metadataTools;

    public DataGridFilterUrlQueryParametersBinder(T grid,
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
        metadataTools = applicationContext.getBean(MetadataTools.class);
    }

    protected void initComponent(T grid) {
        for (Grid.Column<?> column : grid.getColumns()) {
            if (column instanceof DataGridColumn<?> dataGridColumn
                    && dataGridColumn.isFilterable()) {
                setupColumn(dataGridColumn);
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
                .map(column -> ((DataGridHeaderFilter) column.getHeaderComponent()).getPropertyFilter())
                .map(this::serializePropertyFilter)
                .toList();

        QueryParameters queryParameters = new QueryParameters(ImmutableMap.of(getParameter(), parameters));

        fireQueryParametersChanged(new UrlQueryParametersFacet.UrlQueryParametersChangeEvent(this, queryParameters));
    }

    protected String serializePropertyFilter(PropertyFilter<?> propertyFilter) {
        String property = urlParamSerializer
                .serialize(filterUrlQueryParametersSupport.replaceSeparatorValue(
                        Objects.requireNonNull(propertyFilter.getProperty())));
        String operation = urlParamSerializer
                .serialize(filterUrlQueryParametersSupport.replaceSeparatorValue(
                        propertyFilter.getOperation().name().toLowerCase()));
        Object parameterValue = urlParamSerializer
                .serialize(filterUrlQueryParametersSupport.getSerializableValue(propertyFilter.getValue()));

        return property + SEPARATOR +
                operation + SEPARATOR +
                parameterValue;
    }

    @Override
    public void updateState(QueryParameters queryParameters) {
        Map<String, List<String>> parameters = queryParameters.getParameters();
        if (parameters.containsKey(getParameter())) {
            applyPropertyFilterParameters(parameters.get(getParameter()));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void applyPropertyFilterParameters(List<String> params) {
        for (String parameterString : params) {
            int separatorIndex = parameterString.indexOf(SEPARATOR);

            if (separatorIndex == -1) {
                throw new IllegalStateException("Can't parse property condition: " + parameterString);
            }

            String propertyString = parameterString.substring(0, separatorIndex);
            String property = urlParamSerializer.deserialize(String.class,
                    filterUrlQueryParametersSupport.restoreSeparatorValue(propertyString));
            MetaClass entityMetaClass = ((ContainerDataGridItems<?>) grid.getDataProvider()).getEntityMetaClass();

            DataGridColumn<?> column = grid.getColumnByMetaPropertyPath(metadataTools.resolveMetaPropertyPath(
                    entityMetaClass,
                    property
            ));
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
                            .parseValue(entityMetaClass, property, operation.getType(), valueString);
                    propertyFilter.setValue(parsedValue);
                } catch (Exception e) {
                    log.info("Cannot parse URL parameter. {}", e.toString());
                    propertyFilter.setValue(null);
                }
            }

            headerFilter.apply();
        }
    }

    public String getParameter() {
        return Strings.isNullOrEmpty(parameter)
                ? grid.getId().orElseThrow(() ->
                new IllegalStateException("Component has neither id nor explicit url query param"))
                + "Filter"
                : parameter;
    }

    public void setParameter(@Nullable String parameter) {
        this.parameter = parameter;
    }

    @Nullable
    @Override
    public Component getComponent() {
        return grid;
    }
}
