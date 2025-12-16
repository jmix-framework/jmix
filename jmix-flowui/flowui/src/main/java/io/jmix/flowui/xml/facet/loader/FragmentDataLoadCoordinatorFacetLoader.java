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
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.facet.FacetOwner;
import io.jmix.flowui.facet.FragmentDataLoadCoordinator;
import io.jmix.flowui.fragment.FragmentData;
import io.jmix.flowui.fragment.FragmentUtils;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.view.View;
import io.jmix.flowui.xml.layout.ComponentLoader;
import org.dom4j.Element;

public class FragmentDataLoadCoordinatorFacetLoader
        extends AbstractDataLoadCoordinatorFacetLoader<FragmentDataLoadCoordinator> {

    @Override
    protected FragmentDataLoadCoordinator createFacet() {
        FragmentDataLoadCoordinator facet = facets.create(FragmentDataLoadCoordinator.class);
        facet.setOwner((Composite<?> & FacetOwner) context.getOrigin());
        return facet;
    }

    @Override
    protected ComponentLoader.ComponentContext getComponentContext() {
        return findHostViewContext((ComponentLoader.FragmentContext) context);
    }

    protected void loadRefresh(FragmentDataLoadCoordinator facet, Element element) {
        String loaderId = loaderSupport.loadString(element, "loader")
                .orElseThrow(() ->
                        new GuiDevelopmentException(
                                "'fragmentDataLoadCoordinator/refresh' element has no 'loader' attribute",
                                context));

        for (Element eventElement : element.elements()) {
            switch (eventElement.getName()) {
                case "onFragmentEvent" -> loadOnFragmentEvent(facet, loaderId, eventElement);
                case "onContainerItemChanged" -> loadOnContainerItemChanged(facet, loaderId, eventElement);
                case "onComponentValueChanged" -> loadOnComponentValueChanged(facet, loaderId, eventElement);
                default -> throw new GuiDevelopmentException(
                        "Unsupported nested element in 'fragmentDataLoadCoordinator/refresh': %s"
                                .formatted(eventElement.getName()),
                        context);
            }
        }
    }

    protected void loadOnFragmentEvent(FragmentDataLoadCoordinator facet, String loaderId, Element element) {
        String type = loadEventRequiredAttribute(element, "type");

        Class<?> eventClass;

        if ("Ready".equals(type)) {
            eventClass = View.ReadyEvent.class;
        } else {
            throw new GuiDevelopmentException(
                    "Unsupported 'fragmentDataLoadCoordinator/refresh/oFragmentEvent.event'" +
                            " value: " + type, context);
        }

        getComponentContext().addPreInitTask(new OnFragmentEventLoadTriggerInitTask(facet, loaderId, eventClass));
    }

    protected ComponentLoader.ComponentContext findHostViewContext(ComponentLoader.FragmentContext fragmentContext) {
        ComponentLoader.Context currentContext = fragmentContext;
        while (currentContext.getParentContext() != null) {
            currentContext = currentContext.getParentContext();
        }

        if (currentContext instanceof ComponentLoader.ComponentContext viewContext) {
            return viewContext;
        }

        throw new IllegalStateException("%s has no parent view context"
                .formatted(fragmentContext.getClass().getSimpleName()));
    }

    public static class OnFragmentEventLoadTriggerInitTask implements ComponentLoader.InitTask {

        protected final FragmentDataLoadCoordinator facet;
        protected final String loaderId;
        protected final Class<?> eventClass;

        public OnFragmentEventLoadTriggerInitTask(FragmentDataLoadCoordinator facet, String loaderId, Class<?> eventClass) {
            this.facet = facet;
            this.loaderId = loaderId;
            this.eventClass = eventClass;
        }

        @Override
        public void execute(ComponentLoader.Context context) {
            Preconditions.checkNotNullArgument(facet.getOwner());

            FragmentData fragmentData = FragmentUtils.getFragmentData(facet.getOwner());
            DataLoader loader = fragmentData.getLoader(loaderId);
            facet.addOnFragmentEventLoadTrigger(loader, eventClass);
        }
    }
}
