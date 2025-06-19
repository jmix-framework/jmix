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
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.genericfilter.GenericFilter;
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
 * Provides an implementation of a URL query parameter binder provider for a generic filter component.
 * This class is responsible for creating and configuring instances of
 * {@link GenericFilterUrlQueryParametersBinder} and integrating them with a {@link UrlQueryParametersFacet}.
 */
@Component("flowui_GenericFilterUrlQueryParametersBinderProvider")
public class GenericFilterUrlQueryParametersBinderProvider extends AbstractUrlQueryParametersBinderProvider
        implements ApplicationContextAware {

    protected ApplicationContext applicationContext;

    public GenericFilterUrlQueryParametersBinderProvider(UrlParamSerializer urlParamSerializer,
                                                         LoaderSupport loaderSupport) {
        super(urlParamSerializer, loaderSupport);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean supports(Element element) {
        return GenericFilterUrlQueryParametersBinder.NAME.equals(element.getName());
    }

    @Override
    public void load(UrlQueryParametersFacet facet, Element element, ComponentLoader.ComponentContext context) {
        String componentId = loadRequiredAttribute(element, "component", context);
        String binderId = loadAttribute(element, "id");
        String configurationParam = loadAttribute(element, "configurationParam");
        String conditionParam = loadAttribute(element, "conditionParam");

        context.addPreInitTask(new GenericFilterQueryParametersBinderInitTask(
                facet, componentId, binderId, configurationParam, conditionParam, urlParamSerializer, applicationContext
        ));
    }

    /**
     * A task for initializing and configuring a {@link GenericFilterUrlQueryParametersBinder}
     * that binds a {@link GenericFilter} component to URL query parameters.
     */
    public static class GenericFilterQueryParametersBinderInitTask implements ComponentLoader.InitTask {

        protected final UrlQueryParametersFacet facet;
        protected final String binderId;
        protected final String componentId;
        protected final String configurationParam;
        protected final String conditionParam;
        protected final UrlParamSerializer urlParamSerializer;
        protected final ApplicationContext applicationContext;

        public GenericFilterQueryParametersBinderInitTask(UrlQueryParametersFacet facet,
                                                          String componentId,
                                                          @Nullable String binderId,
                                                          @Nullable String configurationParam,
                                                          @Nullable String conditionParam,
                                                          UrlParamSerializer urlParamSerializer,
                                                          ApplicationContext applicationContext) {
            this.facet = facet;
            this.binderId = binderId;
            this.componentId = componentId;
            this.configurationParam = configurationParam;
            this.conditionParam = conditionParam;
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
            if (!(component instanceof GenericFilter)) {
                throw new IllegalStateException(String.format("'%s' is not a generic filter component", componentId));
            }

            GenericFilterUrlQueryParametersBinder binder =
                    new GenericFilterUrlQueryParametersBinder(((GenericFilter) component),
                            urlParamSerializer, applicationContext);

            binder.setId(binderId);
            binder.setConfigurationParam(configurationParam);
            binder.setConditionParam(conditionParam);

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
