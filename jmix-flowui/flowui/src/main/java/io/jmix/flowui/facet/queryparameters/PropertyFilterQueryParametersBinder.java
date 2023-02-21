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
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.router.QueryParameters;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.component.propertyfilter.PropertyFilter.Operation;
import io.jmix.flowui.facet.QueryParametersFacet.QueryParametersChangeEvent;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PropertyFilterQueryParametersBinder extends AbstractQueryParametersBinder {

    public static final String NAME = "propertyFilter";

    public static final String SETTINGS_SEPARATOR = "_";

    protected PropertyFilter<?> filter;

    protected String filterParam;

    protected ApplicationContext applicationContext;
    protected UrlParamSerializer urlParamSerializer;
    protected QueryParametersSupport queryParametersSupport;

    public PropertyFilterQueryParametersBinder(PropertyFilter<?> filter,
                                               UrlParamSerializer urlParamSerializer,
                                               ApplicationContext applicationContext) {
        this.filter = filter;
        this.urlParamSerializer = urlParamSerializer;
        this.applicationContext = applicationContext;

        autowireDependencies();
        initComponent(filter);
    }

    protected void autowireDependencies() {
        queryParametersSupport = applicationContext.getBean(QueryParametersSupport.class);
    }

    protected void initComponent(PropertyFilter<?> filter) {
        filter.addValueChangeListener(this::onValueChange);
        filter.addOperationChangeListener(this::onOperationChange);
    }

    @SuppressWarnings("rawtypes")
    protected void onValueChange(AbstractField.ComponentValueChangeEvent event) {
        updateQueryParameters();
    }

    protected void onOperationChange(PropertyFilter.OperationChangeEvent<?> event) {
        updateQueryParameters();
    }

    protected void updateQueryParameters() {
        String serializedOperation = urlParamSerializer
                .serialize(queryParametersSupport.convertFromEnumName(filter.getOperation().name().toLowerCase()));
        String serializedValue = urlParamSerializer
                .serialize(queryParametersSupport.getSerializableValue(filter.getValue()));

        String paramValue = serializedOperation + SETTINGS_SEPARATOR + serializedValue;
        QueryParameters queryParameters = QueryParameters
                .simple(ImmutableMap.of(getFilterParam(), paramValue));

        fireQueryParametersChanged(new QueryParametersChangeEvent(this, queryParameters));
    }

    @Override
    public void updateState(QueryParameters queryParameters) {
        Map<String, List<String>> parameters = queryParameters.getParameters();
        if (parameters.containsKey(getFilterParam())) {
            String serializedSettings = parameters.get(getFilterParam()).get(0);
            String[] values = serializedSettings.split(SETTINGS_SEPARATOR);
            if (values.length < 1) {
                throw new IllegalStateException("Can't parse property filter settings: " + serializedSettings);
            }

            Operation operation = urlParamSerializer.deserialize(Operation.class,
                    queryParametersSupport.convertToEnumName(values[0]));
            filter.setOperation(operation);

            if (values.length == 2
                    && !Strings.isNullOrEmpty(values[1])) {
                MetaClass entityMetaClass = filter.getDataLoader().getContainer().getEntityMetaClass();
                Object parsedValue = queryParametersSupport.parseValue(entityMetaClass,
                        Objects.requireNonNull(filter.getProperty()), operation.getType(), values[1]);
                //noinspection unchecked,rawtypes
                ((PropertyFilter) filter).setValue(parsedValue);
            }
        }
    }

    public String getFilterParam() {
        return Strings.isNullOrEmpty(filterParam)
                ? filter.getId().orElseThrow(() ->
                new IllegalStateException("Component has neither id nor explicit url query param"))
                : filterParam;
    }

    public void setFilterParam(@Nullable String filterParam) {
        this.filterParam = filterParam;
    }
}
