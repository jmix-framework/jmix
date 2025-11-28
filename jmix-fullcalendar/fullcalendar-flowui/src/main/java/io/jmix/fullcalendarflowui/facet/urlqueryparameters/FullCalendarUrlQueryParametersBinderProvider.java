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

package io.jmix.fullcalendarflowui.facet.urlqueryparameters;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.Component;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.facet.UrlQueryParametersFacet;
import io.jmix.flowui.facet.urlqueryparameters.AbstractUrlQueryParametersBinderProvider;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import io.jmix.fullcalendarflowui.component.FullCalendar;
import org.dom4j.Element;
import org.springframework.lang.Nullable;

@org.springframework.stereotype.Component("fcalen_FullCalendarUrlQueryParametersBinderProvider")
public class FullCalendarUrlQueryParametersBinderProvider extends AbstractUrlQueryParametersBinderProvider {

    public FullCalendarUrlQueryParametersBinderProvider(UrlParamSerializer urlParamSerializer,
                                                        LoaderSupport loaderSupport) {
        super(urlParamSerializer, loaderSupport);
    }

    @Override
    public boolean supports(Element element) {
        return FullCalendarUrlQueryParametersBinder.NAME.equals(element.getName());
    }

    @Override
    public void load(UrlQueryParametersFacet facet, Element element, ComponentLoader.ComponentContext context) {
        String componentId = loadRequiredAttribute(element, "component", context);
        String binderId = loadAttribute(element, "id");
        String displayModeParam = loadAttribute(element, "calendarDisplayModeParam");
        String dateParam = loadAttribute(element, "calendarDateParam");

        context.addPreInitTask(new FullCalendarQueryParametersBinderInitTask(
                facet, componentId, binderId, displayModeParam, dateParam, urlParamSerializer
        ));
    }

    public static class FullCalendarQueryParametersBinderInitTask implements ComponentLoader.InitTask {

        protected final UrlQueryParametersFacet facet;
        protected final String binderId;
        protected final String componentId;
        protected final String displayModeParam;
        protected final String dateParam;
        protected final UrlParamSerializer urlParamSerializer;

        public FullCalendarQueryParametersBinderInitTask(UrlQueryParametersFacet facet,
                                                         String componentId,
                                                         @Nullable String binderId,
                                                         @Nullable String displayModeParam,
                                                         @Nullable String dateParam,
                                                         UrlParamSerializer urlParamSerializer) {
            this.facet = facet;
            this.binderId = binderId;
            this.componentId = componentId;
            this.displayModeParam = displayModeParam;
            this.dateParam = dateParam;
            this.urlParamSerializer = urlParamSerializer;
        }

        @Override
        public void execute(ComponentLoader.Context context) {
            Preconditions.checkState(facet.getOwner() != null, "%s owner is not set",
                    UrlQueryParametersFacet.NAME);

            Component component = UiComponentUtils.getComponent(facet.getOwner(), componentId);
            if (!(component instanceof FullCalendar)) {
                throw new IllegalStateException(String.format("'%s' is not a '%s' component", componentId,
                        FullCalendar.class.getSimpleName()));
            }

            FullCalendarUrlQueryParametersBinder binder =
                    new FullCalendarUrlQueryParametersBinder(((FullCalendar) component), urlParamSerializer);

            binder.setId(binderId);
            binder.setCalendarDisplayModeParam(displayModeParam);
            binder.setCalendarDateParam(dateParam);

            facet.registerBinder(binder);
        }
    }
}
