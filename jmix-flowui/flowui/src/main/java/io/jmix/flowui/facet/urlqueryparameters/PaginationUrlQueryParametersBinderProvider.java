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
import io.jmix.flowui.component.PaginationComponent;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.facet.UrlQueryParametersFacet;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.inittask.AbstractInitTask;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import org.dom4j.Element;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Provides a binder implementation for URL query parameters specifically designed
 * for pagination components. Binds URL query parameter values to a pagination
 * component's state and vice versa.
 */
@Component("flowui_PaginationUrlQueryParametersBinderProvider")
public class PaginationUrlQueryParametersBinderProvider extends AbstractUrlQueryParametersBinderProvider {

    public PaginationUrlQueryParametersBinderProvider(UrlParamSerializer urlParamSerializer,
                                                      LoaderSupport loaderSupport) {
        super(urlParamSerializer, loaderSupport);
    }

    @Override
    public boolean supports(Element element) {
        return PaginationUrlQueryParametersBinder.NAME.equals(element.getName());
    }

    @Override
    public void load(UrlQueryParametersFacet facet, Element element, ComponentLoader.ComponentContext context) {
        String componentId = loadRequiredAttribute(element, "component", context);
        String binderId = loadAttribute(element, "id");
        String firstResultParam = loadAttribute(element, "firstResultParam");
        String maxResultsParam = loadAttribute(element, "maxResultsParam");

        context.addPreInitTask(new PaginationQueryParametersBinderInitTask(
                facet, componentId, binderId, firstResultParam, maxResultsParam, urlParamSerializer
        ));
    }

    /**
     * A task that initializes and registers a {@link PaginationUrlQueryParametersBinder} for components
     * capable of handling pagination behavior.
     */
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
        public void execute(ComponentLoader.ComponentContext context, View<?> view) {
            // Is not invoked, do nothing
        }

        @Override
        public void execute(ComponentLoader.Context context) {
            Preconditions.checkState(facet.getOwner() != null, "%s owner is not set",
                    UrlQueryParametersFacet.NAME);

            com.vaadin.flow.component.Component component = UiComponentUtils.getComponent(facet.getOwner(), componentId);
            if (!(component instanceof PaginationComponent)) {
                throw new IllegalStateException(String.format("'%s' is not a pagination component", componentId));
            }

            PaginationUrlQueryParametersBinder binder =
                    new PaginationUrlQueryParametersBinder(((PaginationComponent<?>) component), urlParamSerializer);

            binder.setId(binderId);
            binder.setFirstResultParam(firstResultParam);
            binder.setMaxResultsParam(maxResultsParam);

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
