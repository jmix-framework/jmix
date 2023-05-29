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

package io.jmix.flowui.xml.facet;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.Component;
import io.jmix.flowui.component.PaginationComponent;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.facet.UrlQueryParametersFacet;
import io.jmix.flowui.facet.impl.UrlQueryParametersFacetImpl;
import io.jmix.flowui.facet.urlqueryparameters.GenericFilterUrlQueryParametersBinder;
import io.jmix.flowui.facet.urlqueryparameters.PaginationUrlQueryParametersBinder;
import io.jmix.flowui.facet.urlqueryparameters.PropertyFilterUrlQueryParametersBinder;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.navigation.RouteSupport;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.ComponentLoader.ComponentContext;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import org.dom4j.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import org.springframework.lang.Nullable;

@org.springframework.stereotype.Component("flowui_UrlQueryParametersFacetProvider")
public class UrlQueryParametersFacetProvider implements FacetProvider<UrlQueryParametersFacet>, ApplicationContextAware {

    protected LoaderSupport loaderSupport;
    protected RouteSupport routeSupport;
    protected UrlParamSerializer urlParamSerializer;
    protected ApplicationContext applicationContext;

    public UrlQueryParametersFacetProvider(LoaderSupport loaderSupport,
                                           RouteSupport routeSupport,
                                           UrlParamSerializer urlParamSerializer) {
        this.loaderSupport = loaderSupport;
        this.routeSupport = routeSupport;
        this.urlParamSerializer = urlParamSerializer;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Class<UrlQueryParametersFacet> getFacetClass() {
        return UrlQueryParametersFacet.class;
    }

    @Override
    public UrlQueryParametersFacet create() {
        return new UrlQueryParametersFacetImpl(routeSupport);
    }

    @Override
    public String getFacetTag() {
        return UrlQueryParametersFacet.NAME;
    }

    @Override
    public void loadFromXml(UrlQueryParametersFacet facet, Element element, ComponentContext context) {
        facet.setOwner(context.getView());

        loaderSupport.loadString(element, "id", facet::setId);

        for (Element binderEl : element.elements()) {
            loadBinder(facet, binderEl, context);
        }
    }

    protected void loadBinder(UrlQueryParametersFacet facet, Element element, ComponentContext context) {
        // TODO: gg, rework, some registration is needed
        switch (element.getName()) {
            case PaginationUrlQueryParametersBinder.NAME:
                loadPaginationQueryParametersBinder(facet, element, context);
                break;
            case GenericFilterUrlQueryParametersBinder.NAME:
                loadGenericFilterQueryParametersBinder(facet, element, context);
                break;
            case PropertyFilterUrlQueryParametersBinder.NAME:
                loadPropertyFilterQueryParametersBinder(facet, element, context);
                break;
            default:
                throw new GuiDevelopmentException(
                        String.format("Unsupported nested element in '%s': %s",
                                getFacetTag(), element.getName()), context);
        }
    }

    protected void loadPropertyFilterQueryParametersBinder(UrlQueryParametersFacet facet,
                                                           Element element, ComponentContext context) {
        String componentId = loadRequiredAttribute(element, "component", context);
        String binderId = loadAttribute(element, "id");
        String param = loadAttribute(element, "param");

        context.addPreInitTask(new PropertyFilterQueryParametersBinderInitTask(
                facet, componentId, param, binderId, urlParamSerializer, applicationContext
        ));
    }

    protected void loadGenericFilterQueryParametersBinder(UrlQueryParametersFacet facet,
                                                          Element element, ComponentContext context) {
        String componentId = loadRequiredAttribute(element, "component", context);
        String binderId = loadAttribute(element, "id");
        String conditionParam = loadAttribute(element, "conditionParam");

        context.addPreInitTask(new GenericFilterQueryParametersBinderInitTask(
                facet, componentId, binderId, conditionParam, urlParamSerializer, applicationContext
        ));
    }

    protected void loadPaginationQueryParametersBinder(UrlQueryParametersFacet facet,
                                                       Element element, ComponentContext context) {
        String componentId = loadRequiredAttribute(element, "component", context);
        String binderId = loadAttribute(element, "id");
        String firstResultParam = loadAttribute(element, "firstResultParam");
        String maxResultsParam = loadAttribute(element, "maxResultsParam");

        context.addPreInitTask(new PaginationQueryParametersBinderInitTask(
                facet, componentId, binderId, firstResultParam, maxResultsParam, urlParamSerializer
        ));
    }

    @Nullable
    protected String loadAttribute(Element element, String name) {
        return loaderSupport.loadString(element, name).orElse(null);
    }

    protected String loadRequiredAttribute(Element element, String name, ComponentContext context) {
        return loaderSupport.loadString(element, name)
                .orElseThrow(() -> new GuiDevelopmentException(
                        String.format("'%s/%s' has no '%s' attribute",
                                getFacetTag(), element.getName(), name), context));
    }

    public static class PaginationQueryParametersBinderInitTask implements ComponentLoader.InitTask {

        protected final UrlQueryParametersFacet facet;
        protected final String binderId;
        protected final String componentId;
        protected final String firstResultParam;
        protected final String maxResultsParam;
        protected final UrlParamSerializer urlParamSerializer;

        public PaginationQueryParametersBinderInitTask(UrlQueryParametersFacet facet,
                                                       String componentId,
                                                       @Nullable String binderId,
                                                       @Nullable String firstResultParam,
                                                       @Nullable String maxResultsParam,
                                                       UrlParamSerializer urlParamSerializer) {
            this.facet = facet;
            this.binderId = binderId;
            this.componentId = componentId;
            this.firstResultParam = firstResultParam;
            this.maxResultsParam = maxResultsParam;
            this.urlParamSerializer = urlParamSerializer;
        }

        @Override
        public void execute(ComponentContext context, View<?> view) {
            Preconditions.checkState(facet.getOwner() != null, "%s owner is not set", UrlQueryParametersFacet.NAME);

            Component component = UiComponentUtils.getComponent(facet.getOwner(), componentId);
            if (!(component instanceof PaginationComponent)) {
                throw new IllegalStateException(String.format("'%s' is not a pagination component", componentId));
            }

            PaginationUrlQueryParametersBinder binder =
                    new PaginationUrlQueryParametersBinder(((PaginationComponent<?>) component), urlParamSerializer);

            binder.setId(binderId);
            binder.setFirstResultParam(firstResultParam);
            binder.setMaxResultsParam(maxResultsParam);

            facet.registerBinder(binder);
        }
    }

    public static class PropertyFilterQueryParametersBinderInitTask implements ComponentLoader.InitTask {

        protected final UrlQueryParametersFacet facet;
        protected final String binderId;
        protected final String componentId;
        protected final String parameter;
        protected final UrlParamSerializer urlParamSerializer;
        protected final ApplicationContext applicationContext;

        public PropertyFilterQueryParametersBinderInitTask(UrlQueryParametersFacet facet,
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
        public void execute(ComponentContext context, View<?> view) {
            Preconditions.checkState(facet.getOwner() != null, "%s owner is not set", UrlQueryParametersFacet.NAME);

            Component component = UiComponentUtils.getComponent(facet.getOwner(), componentId);
            if (!(component instanceof PropertyFilter)) {
                throw new IllegalStateException(String.format("'%s' is not a property filter component", componentId));
            }

            PropertyFilterUrlQueryParametersBinder binder =
                    new PropertyFilterUrlQueryParametersBinder(((PropertyFilter<?>) component),
                            urlParamSerializer, applicationContext);

            binder.setId(binderId);
            binder.setParameter(parameter);

            facet.registerBinder(binder);
        }
    }

    public static class GenericFilterQueryParametersBinderInitTask implements ComponentLoader.InitTask {

        protected final UrlQueryParametersFacet facet;
        protected final String binderId;
        protected final String componentId;
        protected final String conditionParam;
        protected final UrlParamSerializer urlParamSerializer;
        protected final ApplicationContext applicationContext;

        public GenericFilterQueryParametersBinderInitTask(UrlQueryParametersFacet facet,
                                                          String componentId,
                                                          @Nullable String binderId,
                                                          @Nullable String conditionParam,
                                                          UrlParamSerializer urlParamSerializer,
                                                          ApplicationContext applicationContext) {
            this.facet = facet;
            this.binderId = binderId;
            this.componentId = componentId;
            this.conditionParam = conditionParam;
            this.urlParamSerializer = urlParamSerializer;
            this.applicationContext = applicationContext;
        }

        @Override
        public void execute(ComponentContext context, View<?> view) {
            Preconditions.checkState(facet.getOwner() != null, "%s owner is not set", UrlQueryParametersFacet.NAME);

            Component component = UiComponentUtils.getComponent(facet.getOwner(), componentId);
            if (!(component instanceof GenericFilter)) {
                throw new IllegalStateException(String.format("'%s' is not a generic filter component", componentId));
            }

            GenericFilterUrlQueryParametersBinder binder =
                    new GenericFilterUrlQueryParametersBinder(((GenericFilter) component),
                            urlParamSerializer, applicationContext);

            binder.setId(binderId);
            binder.setConditionParam(conditionParam);

            facet.registerBinder(binder);
        }
    }
}
