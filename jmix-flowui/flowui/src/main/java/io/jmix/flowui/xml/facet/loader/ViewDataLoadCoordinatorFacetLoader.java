/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowui.xml.facet.loader;

import com.vaadin.flow.component.Composite;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.facet.FacetOwner;
import io.jmix.flowui.facet.ViewDataLoadCoordinator;
import io.jmix.flowui.view.View;
import org.dom4j.Element;

public class ViewDataLoadCoordinatorFacetLoader
        extends AbstractDataLoadCoordinatorFacetLoader<ViewDataLoadCoordinator> {

    @Override
    protected ViewDataLoadCoordinator createFacet() {
        ViewDataLoadCoordinator facet = facets.create(ViewDataLoadCoordinator.class);
        facet.setOwner((Composite<?> & FacetOwner) context.getOrigin());
        return facet;
    }

    protected void loadRefresh(ViewDataLoadCoordinator facet, Element element) {
        String loaderId = loaderSupport.loadString(element, "loader")
                .orElseThrow(() ->
                        new GuiDevelopmentException("'dataLoadCoordinator/refresh' element has no 'loader' attribute",
                                context));

        for (Element eventElement : element.elements()) {
            switch (eventElement.getName()) {
                case "onViewEvent" -> loadOnViewEvent(facet, loaderId, eventElement);
                case "onContainerItemChanged" -> loadOnContainerItemChanged(facet, loaderId, eventElement);
                case "onComponentValueChanged" -> loadOnComponentValueChanged(facet, loaderId, eventElement);
                default -> throw new GuiDevelopmentException(
                        "Unsupported nested element in 'dataLoadCoordinator/refresh': %s"
                                .formatted(eventElement.getName()),
                        context);
            }
        }
    }

    protected void loadOnViewEvent(ViewDataLoadCoordinator facet, String loaderId, Element element) {
        String type = loadEventRequiredAttribute(element, "type");

        Class<?> eventClass = switch (type) {
            case "Init" -> View.InitEvent.class;
            case "BeforeShow" -> View.BeforeShowEvent.class;
            case "Ready" -> View.ReadyEvent.class;
            default -> throw new GuiDevelopmentException("Unsupported 'dataLoadCoordinator/refresh/onViewEvent.event'" +
                    " value: " + type, context);
        };

        context.addPreInitTask(new OnViewEventLoadTriggerInitTask(facet, loaderId, eventClass));
    }
}
