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

package io.jmix.flowui.facet.urlqueryparameters;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.grid.Grid;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.facet.UrlQueryParametersFacet;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.inittask.AbstractInitTask;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import org.dom4j.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Provider for binding URL query parameters to {@link DataGrid} filters.
 */
@Component("flowui_DataGridFilterUrlQueryParametersBinderProvider")
public class DataGridFilterUrlQueryParametersBinderProvider extends AbstractUrlQueryParametersBinderProvider
        implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    public DataGridFilterUrlQueryParametersBinderProvider(UrlParamSerializer urlParamSerializer,
                                                          LoaderSupport loaderSupport) {
        super(urlParamSerializer, loaderSupport);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean supports(Element element) {
        return DataGridFilterUrlQueryParametersBinder.NAME.equals(element.getName());
    }

    @Override
    public void load(UrlQueryParametersFacet facet, Element element, ComponentLoader.ComponentContext context) {
        String componentId = loadRequiredAttribute(element, "component", context);
        String binderId = loadAttribute(element, "id");
        String param = loadAttribute(element, "param");

        context.addPreInitTask(new DataGridFilterQueryParametersBinderInitTask(
                facet, componentId, param, binderId, urlParamSerializer, applicationContext
        ));
    }

    /**
     * A task for initializing and registering a binder of type
     * {@link DataGridFilterUrlQueryParametersBinder} to a {@link UrlQueryParametersFacet}.
     */
    public static class DataGridFilterQueryParametersBinderInitTask implements ComponentLoader.InitTask {

        protected final UrlQueryParametersFacet facet;
        protected final String binderId;
        protected final String componentId;
        protected final String parameter;
        protected final UrlParamSerializer urlParamSerializer;
        protected final ApplicationContext applicationContext;

        public DataGridFilterQueryParametersBinderInitTask(UrlQueryParametersFacet facet,
                                                           String componentId,
                                                           @Nullable String parameter,
                                                           @Nullable String binderId,
                                                           UrlParamSerializer urlParamSerializer,
                                                           ApplicationContext applicationContext) {
            this.facet = facet;
            this.binderId = binderId;
            this.componentId = componentId;
            this.parameter = parameter;
            this.urlParamSerializer = urlParamSerializer;
            this.applicationContext = applicationContext;
        }

        @Override
        public void execute(ComponentLoader.ComponentContext context, View<?> view) {
            // Is not invoked, do nothing
        }

        @Override
        public void execute(ComponentLoader.Context context) {
            Preconditions.checkState(facet.getOwner() != null, "%s owner is not set",
                    UrlQueryParametersFacet.NAME);

            com.vaadin.flow.component.Component component = UiComponentUtils.getComponent(facet.getOwner(), componentId);
            if (!(component instanceof Grid<?>)) {
                throw new IllegalStateException(String.format("'%s' is not a %s component", componentId,
                        Grid.class.getSimpleName()));
            }

            DataGridFilterUrlQueryParametersBinder binder =
                    new DataGridFilterUrlQueryParametersBinder(((Grid<?>) component),
                            urlParamSerializer, applicationContext);

            binder.setId(binderId);
            binder.setParameter(parameter);

            facet.registerBinder(binder);

            context.addInitTask(new AbstractInitTask() {
                @Override
                public void execute(ComponentLoader.Context context) {
                    binder.saveInitialState();
                }
            });
        }
    }
}
