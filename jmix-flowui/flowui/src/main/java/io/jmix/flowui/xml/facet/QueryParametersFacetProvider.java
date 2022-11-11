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
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.facet.QueryParametersFacet;
import io.jmix.flowui.facet.impl.QueryParametersFacetImpl;
import io.jmix.flowui.facet.queryparameters.PaginationQueryParametersBinder;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.navigation.RouteSupport;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.ComponentLoader.ComponentContext;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import org.dom4j.Element;

import javax.annotation.Nullable;

@org.springframework.stereotype.Component("flowui_QueryParametersFacetProvider")
public class QueryParametersFacetProvider implements FacetProvider<QueryParametersFacet> {

    protected LoaderSupport loaderSupport;
    protected RouteSupport routeSupport;
    protected UrlParamSerializer urlParamSerializer;

    public QueryParametersFacetProvider(LoaderSupport loaderSupport,
                                        RouteSupport routeSupport,
                                        UrlParamSerializer urlParamSerializer) {
        this.loaderSupport = loaderSupport;
        this.routeSupport = routeSupport;
        this.urlParamSerializer = urlParamSerializer;
    }

    @Override
    public Class<QueryParametersFacet> getFacetClass() {
        return QueryParametersFacet.class;
    }

    @Override
    public QueryParametersFacet create() {
        return new QueryParametersFacetImpl(routeSupport);
    }

    @Override
    public String getFacetTag() {
        return QueryParametersFacet.NAME;
    }

    @Override
    public void loadFromXml(QueryParametersFacet facet, Element element, ComponentContext context) {
        facet.setOwner(context.getView());

        loaderSupport.loadString(element, "id", facet::setId);

        for (Element binderEl : element.elements()) {
            loadBinder(facet, binderEl, context);
        }
    }

    protected void loadBinder(QueryParametersFacet facet, Element element, ComponentContext context) {
        switch (element.getName()) {
            case PaginationQueryParametersBinder.NAME:
                loadPaginationQueryParametersBinder(facet, element, context);
                break;
            default:
                throw new GuiDevelopmentException(
                        String.format("Unsupported nested element in '%s': %s",
                                getFacetTag(), element.getName()), context);
        }
    }

    protected void loadPaginationQueryParametersBinder(QueryParametersFacet facet,
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

        protected final QueryParametersFacet facet;
        protected final String binderId;
        protected final String componentId;
        protected final String firstResultParam;
        protected final String maxResultsParam;
        protected final UrlParamSerializer urlParamSerializer;

        public PaginationQueryParametersBinderInitTask(QueryParametersFacet facet,
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
            Preconditions.checkState(facet.getOwner() != null, "%s owner is not set", QueryParametersFacet.NAME);

            Component component = UiComponentUtils.getComponent(facet.getOwner(), componentId);
            if (!(component instanceof PaginationComponent)) {
                throw new IllegalStateException(String.format("'%s' is not a pagination component", componentId));
            }

            PaginationQueryParametersBinder binder =
                    new PaginationQueryParametersBinder(((PaginationComponent<?>) component), urlParamSerializer);

            binder.setId(binderId);
            binder.setFirstResultParam(firstResultParam);
            binder.setMaxResultsParam(maxResultsParam);

            facet.registerBinder(binder);
        }
    }
}
