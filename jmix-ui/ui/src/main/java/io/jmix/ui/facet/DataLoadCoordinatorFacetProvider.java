/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.ui.facet;

import com.google.common.base.Preconditions;
import io.jmix.core.impl.QueryParamValuesManager;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.DataLoadCoordinator;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.impl.DataLoadCoordinatorImpl;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.model.ScreenData;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.sys.UiControllerReflectionInspector;
import io.jmix.ui.xml.FacetProvider;
import io.jmix.ui.xml.layout.ComponentLoader;
import io.jmix.ui.xml.layout.ComponentLoader.ComponentContext;
import org.dom4j.Element;

import javax.annotation.Nullable;
import javax.inject.Inject;

@org.springframework.stereotype.Component("ui_DataLoadCoordinatorFacetProvider")
public class DataLoadCoordinatorFacetProvider implements FacetProvider<DataLoadCoordinator> {

    private UiControllerReflectionInspector reflectionInspector;
    private QueryParamValuesManager queryParamValuesManager;

    @Inject
    public void setReflectionInspector(UiControllerReflectionInspector reflectionInspector) {
        this.reflectionInspector = reflectionInspector;
    }

    @Inject
    public void setQueryParamValuesManager(QueryParamValuesManager queryParamValuesManager) {
        this.queryParamValuesManager = queryParamValuesManager;
    }

    @Override
    public Class<DataLoadCoordinator> getFacetClass() {
        return DataLoadCoordinator.class;
    }

    @Override
    public DataLoadCoordinator create() {
        return new DataLoadCoordinatorImpl(reflectionInspector, queryParamValuesManager);
    }

    @Override
    public String getFacetTag() {
        return "dataLoadCoordinator";
    }

    @Override
    public void loadFromXml(DataLoadCoordinator facet, Element element, ComponentContext context) {
        facet.setOwner(context.getFrame());

        String id = element.attributeValue("id");
        if (id != null) {
            facet.setId(id);
        }

        String containerPrefix = element.attributeValue("containerPrefix");
        if (containerPrefix != null) {
            facet.setContainerPrefix(containerPrefix);
        }
        String componentPrefix = element.attributeValue("componentPrefix");
        if (componentPrefix != null) {
            facet.setComponentPrefix(componentPrefix);
        }

        for (Element loaderEl : element.elements("refresh")) {
            loadRefresh(facet, context, loaderEl);
        }

        if (Boolean.parseBoolean(element.attributeValue("auto"))) {
            context.addInjectTask(new AutoConfigurationInitTask(facet));
        }
    }

    protected void loadRefresh(DataLoadCoordinator facet, ComponentContext context, Element element) {
        String loaderId = element.attributeValue("loader");
        if (loaderId == null) {
            throw new GuiDevelopmentException("'dataLoadCoordinator/refresh' element has no 'loader' attribute", context);
        }

        for (Element eventElement : element.elements()) {
            switch (eventElement.getName()) {
                case "onScreenEvent":
                    loadOnScreenEvent(facet, context, loaderId, eventElement);
                    break;
                case "onFragmentEvent":
                    loadOnFragmentEvent(facet, context, loaderId, eventElement);
                    break;
                case "onContainerItemChanged":
                    loadOnContainerItemChanged(facet, context, loaderId, eventElement);
                    break;
                case "onComponentValueChanged":
                    loadOnComponentValueChanged(facet, context, loaderId, eventElement);
                    break;
                default:
                    throw new GuiDevelopmentException("Unsupported nested element in 'dataLoadCoordinator/refresh': " +
                            eventElement.getName(), context);
            }
        }
    }

    protected void loadOnScreenEvent(DataLoadCoordinator facet, ComponentContext context,
                                     String loaderId, Element element) {
        String type = loadEventRequiredAttribute(element, "type", context);

        Class<?> eventClass;
        switch (type) {
            case "Init":
                eventClass = Screen.InitEvent.class;
                break;
            case "AfterInit":
                eventClass = Screen.AfterInitEvent.class;
                break;
            case "BeforeShow":
                eventClass = Screen.BeforeShowEvent.class;
                break;
            case "AfterShow":
                eventClass = Screen.AfterShowEvent.class;
                break;
            default:
                throw new GuiDevelopmentException("Unsupported 'dataLoadCoordinator/refresh/onScreenEvent.event' " +
                        "value: " + type, context);
        }

        context.addInjectTask(new OnFrameOwnerEventLoadTriggerInitTask(facet, loaderId, eventClass));
    }

    protected void loadOnFragmentEvent(DataLoadCoordinator facet, ComponentContext context,
                                       String loaderId, Element element) {
        String type = loadEventRequiredAttribute(element, "type", context);

        Class<?> eventClass;
        switch (type) {
            case "Init":
                eventClass = ScreenFragment.InitEvent.class;
                break;
            case "AfterInit":
                eventClass = ScreenFragment.AfterInitEvent.class;
                break;
            case "Attach":
                eventClass = ScreenFragment.AttachEvent.class;
                break;
            default:
                throw new GuiDevelopmentException("Unsupported 'dataLoadCoordinator/refresh/onFragmentEvent.event' " +
                        "value: " + type, context);
        }
        context.addInjectTask(new OnFrameOwnerEventLoadTriggerInitTask(facet, loaderId, eventClass));
    }

    protected void loadOnContainerItemChanged(DataLoadCoordinator facet, ComponentContext context,
                                              String loaderId, Element element) {
        String container = loadEventRequiredAttribute(element, "container", context);

        String param = loadParam(element);
        context.addInjectTask(new OnContainerItemChangedLoadTriggerInitTask(facet, loaderId, container, param));
    }

    protected void loadOnComponentValueChanged(DataLoadCoordinator facet, ComponentContext context,
                                               String loaderId, Element element) {
        String component = loadEventRequiredAttribute(element, "component", context);

        String param = loadParam(element);
        DataLoadCoordinator.LikeClause likeClause = loadLikeClause(element);

        context.addInjectTask(new OnComponentValueChangedLoadTriggerInitTask(
                facet, loaderId, component, param, likeClause));
    }

    protected String loadEventRequiredAttribute(Element element, String name, ComponentContext context) {
        String value = element.attributeValue(name);
        if (value == null) {
            throw new GuiDevelopmentException("'dataLoadCoordinator/refresh/" + element.getName() +
                    "' nas no '" + name + "' attribute", context);
        }

        return value;
    }

    @Nullable
    protected String loadParam(Element element) {
        return element.attributeValue("param");
    }

    protected DataLoadCoordinator.LikeClause loadLikeClause(Element element) {
        String likeClauseAttr = element.attributeValue("likeClause");
        DataLoadCoordinator.LikeClause likeClause = likeClauseAttr == null
                ? DataLoadCoordinator.LikeClause.NONE
                : DataLoadCoordinator.LikeClause.valueOf(likeClauseAttr);
        return likeClause;
    }

    public static class OnFrameOwnerEventLoadTriggerInitTask implements ComponentLoader.InjectTask {

        private final DataLoadCoordinator facet;
        private final String loaderId;
        private final Class eventClass;

        public OnFrameOwnerEventLoadTriggerInitTask(DataLoadCoordinator facet, String loaderId, Class eventClass) {
            this.facet = facet;
            this.loaderId = loaderId;
            this.eventClass = eventClass;
        }

        @Override
        public void execute(ComponentContext context, Frame window) {
            Preconditions.checkNotNull(facet.getOwner());
            ScreenData screenData = UiControllerUtils.getScreenData(facet.getOwner().getFrameOwner());
            DataLoader loader = screenData.getLoader(loaderId);
            facet.addOnFrameOwnerEventLoadTrigger(loader, eventClass);
        }
    }

    public static class OnContainerItemChangedLoadTriggerInitTask implements ComponentLoader.InjectTask {

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
        public void execute(ComponentContext context, Frame window) {
            Preconditions.checkNotNull(facet.getOwner());
            ScreenData screenData = UiControllerUtils.getScreenData(facet.getOwner().getFrameOwner());
            DataLoader loader = screenData.getLoader(loaderId);
            InstanceContainer container = screenData.getContainer(containerId);
            facet.addOnContainerItemChangedLoadTrigger(loader, container, param);
        }
    }

    public static class OnComponentValueChangedLoadTriggerInitTask implements ComponentLoader.InjectTask {

        private final DataLoadCoordinator facet;
        private final String loaderId;
        private final String componentId;
        private final String param;
        private DataLoadCoordinator.LikeClause likeClause;

        public OnComponentValueChangedLoadTriggerInitTask(
                DataLoadCoordinator facet, String loaderId, String componentId,
                @Nullable String param, DataLoadCoordinator.LikeClause likeClause) {
            this.facet = facet;
            this.loaderId = loaderId;
            this.componentId = componentId;
            this.param = param;
            this.likeClause = likeClause;
        }

        @Override
        public void execute(ComponentContext context, Frame window) {
            Preconditions.checkNotNull(facet.getOwner());
            ScreenData screenData = UiControllerUtils.getScreenData(facet.getOwner().getFrameOwner());
            DataLoader loader = screenData.getLoader(loaderId);
            Component component = facet.getOwner().getComponentNN(componentId);
            facet.addOnComponentValueChangedLoadTrigger(loader, component, param, likeClause);
        }
    }

    public static class AutoConfigurationInitTask implements ComponentLoader.InjectTask {

        private final DataLoadCoordinator facet;

        public AutoConfigurationInitTask(DataLoadCoordinator facet) {
            this.facet = facet;
        }

        @Override
        public void execute(ComponentContext context, Frame window) {
            facet.configureAutomatically();
        }
    }
}
