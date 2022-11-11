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

import com.vaadin.flow.component.Component;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.facet.DataLoadCoordinator;
import io.jmix.flowui.facet.DataLoadCoordinator.LikeClause;
import io.jmix.flowui.facet.impl.DataLoadCoordinatorImpl;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.ViewData;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.sys.ViewControllerReflectionInspector;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.ComponentLoader.ComponentContext;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import org.dom4j.Element;

import javax.annotation.Nullable;

@org.springframework.stereotype.Component("flowui_DataLoadCoordinatorFacetProvider")
public class DataLoadCoordinatorFacetProvider implements FacetProvider<DataLoadCoordinator> {

    protected LoaderSupport loaderSupport;
    protected ViewControllerReflectionInspector reflectionInspector;

    public DataLoadCoordinatorFacetProvider(LoaderSupport loaderSupport,
                                            ViewControllerReflectionInspector reflectionInspector) {
        this.loaderSupport = loaderSupport;
        this.reflectionInspector = reflectionInspector;
    }

    @Override
    public Class<DataLoadCoordinator> getFacetClass() {
        return DataLoadCoordinator.class;
    }

    @Override
    public DataLoadCoordinator create() {
        return new DataLoadCoordinatorImpl(reflectionInspector);
    }

    @Override
    public String getFacetTag() {
        return DataLoadCoordinator.NAME;
    }

    @Override
    public void loadFromXml(DataLoadCoordinator facet, Element element, ComponentContext context) {
        facet.setOwner(context.getView());

        loaderSupport.loadString(element, "id", facet::setId);
        loaderSupport.loadString(element, "containerPrefix", facet::setContainerPrefix);
        loaderSupport.loadString(element, "componentPrefix", facet::setComponentPrefix);

        for (Element loaderEl : element.elements("refresh")) {
            loadRefresh(facet, context, loaderEl);
        }

        loadAuto(facet, element, context);
    }

    protected void loadAuto(DataLoadCoordinator facet, Element element, ComponentContext context) {
        loaderSupport.loadBoolean(element, "auto")
                .ifPresent(auto -> {
                    if (auto) {
                        context.addPreInitTask(new AutoConfigurationInitTask(facet));
                    }
                });
    }

    protected void loadRefresh(DataLoadCoordinator facet, ComponentContext context, Element element) {
        String loaderId = loaderSupport.loadString(element, "loader")
                .orElseThrow(() ->
                        new GuiDevelopmentException(
                                String.format("'%s/refresh element has no 'loader' attribute", getFacetTag()),
                                context));

        for (Element eventElement : element.elements()) {
            switch (eventElement.getName()) {
                case "onViewEvent":
                    loadOnViewEvent(facet, context, loaderId, eventElement);
                    break;
                case "onContainerItemChanged":
                    loadOnContainerItemChanged(facet, context, loaderId, eventElement);
                    break;
                case "onComponentValueChanged":
                    loadOnComponentValueChanged(facet, context, loaderId, eventElement);
                    break;
                default:
                    throw new GuiDevelopmentException(
                            String.format("Unsupported nested element in '%s/refresh': %s",
                                    getFacetTag(), eventElement.getName()), context);
            }
        }
    }

    protected void loadOnViewEvent(DataLoadCoordinator facet, ComponentContext context,
                                   String loaderId, Element element) {
        String type = loadEventRequiredAttribute(element, "type", context);

        Class<?> eventClass;
        switch (type) {
            case "Init":
                eventClass = View.InitEvent.class;
                break;
            case "BeforeShow":
                eventClass = View.BeforeShowEvent.class;
                break;
            case "Ready":
                eventClass = View.ReadyEvent.class;
                break;
            default:
                throw new GuiDevelopmentException("Unsupported 'dataLoadCoordinator/refresh/onViewEvent.event' " +
                        "value: " + type, context);
        }

        context.addPreInitTask(new OnViewEventLoadTriggerInitTask(facet, loaderId, eventClass));
    }

    protected void loadOnContainerItemChanged(DataLoadCoordinator facet, ComponentContext context,
                                              String loaderId, Element element) {
        String container = loadEventRequiredAttribute(element, "container", context);

        String param = loadParam(element);
        context.addPreInitTask(new OnContainerItemChangedLoadTriggerInitTask(facet, loaderId, container, param));
    }

    protected void loadOnComponentValueChanged(DataLoadCoordinator facet, ComponentContext context,
                                               String loaderId, Element element) {
        String component = loadEventRequiredAttribute(element, "component", context);

        String param = loadParam(element);
        LikeClause likeClause = loadLikeClause(element);

        context.addPreInitTask(new OnComponentValueChangedLoadTriggerInitTask(
                facet, loaderId, component, param, likeClause));
    }

    protected String loadEventRequiredAttribute(Element element, String name, ComponentContext context) {
        return loaderSupport.loadString(element, name)
                .orElseThrow(() -> new GuiDevelopmentException(
                        String.format("'%s/refresh/%s' has no '%s' attribute",
                                getFacetTag(), element.getName(), name), context));
    }

    @Nullable
    protected String loadParam(Element element) {
        return loaderSupport.loadString(element, "param").orElse(null);
    }

    protected LikeClause loadLikeClause(Element element) {
        return loaderSupport.loadEnum(element, LikeClause.class, "likeClause")
                .orElse(LikeClause.NONE);
    }

    public static class OnViewEventLoadTriggerInitTask implements ComponentLoader.InitTask {

        protected final DataLoadCoordinator facet;
        protected final String loaderId;
        protected final Class<?> eventClass;

        public OnViewEventLoadTriggerInitTask(DataLoadCoordinator facet, String loaderId, Class<?> eventClass) {
            this.facet = facet;
            this.loaderId = loaderId;
            this.eventClass = eventClass;
        }

        @Override
        public void execute(ComponentContext context, View<?> view) {
            Preconditions.checkNotNullArgument(facet.getOwner());

            ViewData viewData = ViewControllerUtils.getViewData(facet.getOwner());
            DataLoader loader = viewData.getLoader(loaderId);
            facet.addOnViewEventLoadTrigger(loader, eventClass);
        }
    }

    public static class OnContainerItemChangedLoadTriggerInitTask implements ComponentLoader.InitTask {

        private final DataLoadCoordinator facet;
        private final String loaderId;
        private final String containerId;
        private final String param;

        public OnContainerItemChangedLoadTriggerInitTask(
                DataLoadCoordinator facet, String loaderId, String containerId, @Nullable String param) {
            this.facet = facet;
            this.loaderId = loaderId;
            this.containerId = containerId;
            this.param = param;
        }

        @Override
        public void execute(ComponentContext context, View<?> view) {
            Preconditions.checkNotNullArgument(facet.getOwner());

            ViewData viewData = ViewControllerUtils.getViewData(facet.getOwner());
            DataLoader loader = viewData.getLoader(loaderId);
            InstanceContainer<?> container = viewData.getContainer(containerId);

            facet.addOnContainerItemChangedLoadTrigger(loader, container, param);
        }
    }

    public static class OnComponentValueChangedLoadTriggerInitTask implements ComponentLoader.InitTask {

        protected final DataLoadCoordinator facet;
        protected final String loaderId;
        protected final String componentId;
        protected final String param;
        protected final LikeClause likeClause;

        public OnComponentValueChangedLoadTriggerInitTask(
                DataLoadCoordinator facet, String loaderId, String componentId,
                @Nullable String param, LikeClause likeClause) {
            this.facet = facet;
            this.loaderId = loaderId;
            this.componentId = componentId;
            this.param = param;
            this.likeClause = likeClause;
        }

        @Override
        public void execute(ComponentContext context, View<?> view) {
            Preconditions.checkNotNullArgument(facet.getOwner());

            ViewData viewData = ViewControllerUtils.getViewData(facet.getOwner());
            DataLoader loader = viewData.getLoader(loaderId);
            Component component = UiComponentUtils.getComponent(facet.getOwner(), componentId);

            facet.addOnComponentValueChangedLoadTrigger(loader, component, param, likeClause);
        }
    }

    public static class AutoConfigurationInitTask implements ComponentLoader.InitTask {

        protected final DataLoadCoordinator facet;

        public AutoConfigurationInitTask(DataLoadCoordinator facet) {
            this.facet = facet;
        }

        @Override
        public void execute(ComponentContext context, View<?> view) {
            facet.configureAutomatically();
        }
    }
}
