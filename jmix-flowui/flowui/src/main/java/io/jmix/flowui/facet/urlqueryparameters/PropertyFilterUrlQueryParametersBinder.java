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
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.QueryParameters;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.component.propertyfilter.PropertyFilter.Operation;
import io.jmix.flowui.facet.UrlQueryParametersFacet.UrlQueryParametersChangeEvent;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.jmix.flowui.facet.urlqueryparameters.FilterUrlQueryParametersSupport.SEPARATOR;

/**
 * A URL query parameters binder for {@link PropertyFilter} components. This class enables binding a
 * {@link PropertyFilter} to URL query parameters, allowing the component's state to be reflected in
 * and restored from the URL.
 * <p>
 * The class listens to changes in the {@link PropertyFilter}, such as value or operation changes,
 * and updates the corresponding query parameters. It also supports saving and restoring the
 * {@link PropertyFilter}'s initial state.
 */
public class PropertyFilterUrlQueryParametersBinder extends AbstractUrlQueryParametersBinder
        implements HasInitialState {

    private static final Logger log = LoggerFactory.getLogger(PropertyFilterUrlQueryParametersBinder.class);

    public static final String NAME = "propertyFilter";

    @SuppressWarnings("rawtypes")
    protected PropertyFilter filter;

    protected String parameter;

    protected ApplicationContext applicationContext;
    protected UrlParamSerializer urlParamSerializer;
    protected FilterUrlQueryParametersSupport filterUrlQueryParametersSupport;

    protected InitialState initialState;

    public PropertyFilterUrlQueryParametersBinder(PropertyFilter<?> filter,
                                                  UrlParamSerializer urlParamSerializer,
                                                  ApplicationContext applicationContext) {
        this.filter = filter;
        this.urlParamSerializer = urlParamSerializer;
        this.applicationContext = applicationContext;

        autowireDependencies();
        initComponent(filter);
    }

    protected void autowireDependencies() {
        filterUrlQueryParametersSupport = applicationContext.getBean(FilterUrlQueryParametersSupport.class);
    }

    protected void initComponent(PropertyFilter<?> filter) {
        filter.addValueChangeListener(this::onValueChange);
        filter.addOperationChangeListener(this::onOperationChange);
    }

    @Override
    public void saveInitialState() {
        initialState = new InitialState(filter.getOperation(), filter.getValue());
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
                .serialize(filterUrlQueryParametersSupport.replaceSeparatorValue(
                        filter.getOperation().name().toLowerCase()));
        String serializedValue = urlParamSerializer
                .serialize(filterUrlQueryParametersSupport.getSerializableValue(filter.getValue()));

        String paramValue = serializedOperation + SEPARATOR + serializedValue;
        QueryParameters queryParameters = QueryParameters
                .simple(ImmutableMap.of(getParameter(), paramValue));

        fireQueryParametersChanged(new UrlQueryParametersChangeEvent(this, queryParameters));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void applyInitialState() {
        filter.setOperation(initialState.operation);
        filter.setValue(initialState.value);
    }

    @Override
    public void updateState(QueryParameters queryParameters) {
        Map<String, List<String>> parameters = queryParameters.getParameters();
        if (parameters.containsKey(getParameter())) {
            String serializedSettings = parameters.get(getParameter()).get(0);

            int separatorIndex = serializedSettings.indexOf(SEPARATOR);
            if (separatorIndex == -1) {
                throw new IllegalStateException("Can't parse property filter settings: " + serializedSettings);
            }

            String operationString = serializedSettings.substring(0, separatorIndex);
            Operation operation = urlParamSerializer.deserialize(Operation.class,
                    filterUrlQueryParametersSupport.restoreSeparatorValue(operationString));

            if (filter.isOperationEditable()) {
                filter.setOperation(operation);
            } else if (filter.getOperation() != operation) {
                // ignore the value if operations are not equal
                return;
            }

            String valueString = serializedSettings.substring(separatorIndex + 1);
            if (!Strings.isNullOrEmpty(valueString)) {
                MetaClass entityMetaClass = filter.getDataLoader().getContainer().getEntityMetaClass();
                try {
                    Object parsedValue = filterUrlQueryParametersSupport.parseValue(entityMetaClass,
                            Objects.requireNonNull(filter.getProperty()), operation.getType(), valueString);
                    //noinspection unchecked,rawtypes
                    ((PropertyFilter) filter).setValue(parsedValue);
                } catch (Exception e) {
                    log.info("Cannot parse URL parameter. {}", e.toString());
                    filter.setValue(null);
                }
            }
        }
    }

    /**
     * Returns the value of the parameter. If the parameter is not explicitly set,
     * it defaults to the filter's ID.
     *
     * @return the value of the parameter
     * @throws IllegalStateException if neither parameter nor filter ID is available
     */
    public String getParameter() {
        return Strings.isNullOrEmpty(parameter)
                ? filter.getId().orElseThrow(() ->
                new IllegalStateException("Component has neither id nor explicit url query param"))
                : parameter;
    }

    /**
     * Sets the value of the parameter.
     *
     * @param parameter the value to set for the parameter
     */
    public void setParameter(@Nullable String parameter) {
        this.parameter = parameter;
    }

    @Nullable
    @Override
    public Component getComponent() {
        return filter;
    }

    /**
     * A POJO class for storing properties of the {@link PropertyFilter}'s initial state.
     *
     * @param operation the value of {@code operation} at initialization
     * @param value     the value of {@code value} at initialization
     */
    protected record InitialState(Operation operation, Object value) {
    }
}
