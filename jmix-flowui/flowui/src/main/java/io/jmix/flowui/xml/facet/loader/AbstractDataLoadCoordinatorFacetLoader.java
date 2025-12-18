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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.component.HasDataComponents;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.facet.DataLoadCoordinator;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentUtils;
import io.jmix.flowui.impl.FacetsImpl;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.ViewData;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.xml.facet.FacetProvider;
import io.jmix.flowui.xml.layout.ComponentLoader;
import org.dom4j.Element;
import org.springframework.lang.Nullable;

public abstract class AbstractDataLoadCoordinatorFacetLoader<T extends DataLoadCoordinator>
        extends AbstractFacetLoader<T> {

    @Override
    public void loadFacet() {
        // for backward compatibility, should be removed in future releases
        if (facets instanceof FacetsImpl facetsImpl) {
            FacetProvider<DataLoadCoordinator> provider = facetsImpl.getProvider(DataLoadCoordinator.class);

            if (provider != null && context instanceof ComponentLoader.ComponentContext componentContext) {
                provider.loadFromXml(resultFacet, element, componentContext);
                return;
            }
        }

        loaderSupport.loadString(element, "id", resultFacet::setId);
        loaderSupport.loadString(element, "containerPrefix", resultFacet::setContainerPrefix);
        loaderSupport.loadString(element, "componentPrefix", resultFacet::setComponentPrefix);

        loadRefreshElements(element);
        loadAuto(element);
    }

    protected void loadAuto(Element element) {
        loaderSupport.loadBoolean(element, "auto")
                .ifPresent(auto -> {
                    if (auto) {
                        context.addPreInitTask(new AutoConfigurationInitTask(resultFacet));
                    }
                });
    }

    protected void loadRefreshElements(Element element) {
        for (Element loaderEl : element.elements("refresh")) {
            loadRefresh(resultFacet, loaderEl);
        }
    }

    protected abstract void loadRefresh(T facet, Element element);

    protected void loadOnContainerItemChanged(T facet, String loaderId, Element element) {
        String container = loadEventRequiredAttribute(element, "container");
        String param = loadParam(element);

        context.addPreInitTask(new OnContainerItemChangedLoadTriggerInitTask(facet, loaderId, container, param));
    }

    protected void loadOnComponentValueChanged(T facet, String loaderId, Element element) {
        String component = loadEventRequiredAttribute(element, "component");

        String param = loadParam(element);
        DataLoadCoordinator.LikeClause likeClause = loadLikeClause(element);

        context.addPreInitTask(new OnComponentValueChangedLoadTriggerInitTask(
                facet, loaderId, component, param, likeClause));
    }

    protected String loadEventRequiredAttribute(Element element, String attributeName) {
        return loaderSupport.loadString(element, attributeName)
                .orElseThrow(() -> new GuiDevelopmentException(
                        "'dataLoadCoordinator/refresh/%s' has no '%s' attribute"
                                .formatted(element.getName(), attributeName),
                        context));
    }

    @Nullable
    protected String loadParam(Element element) {
        return loaderSupport.loadString(element, "param").orElse(null);
    }

    protected DataLoadCoordinator.LikeClause loadLikeClause(Element element) {
        return loaderSupport.loadEnum(element, DataLoadCoordinator.LikeClause.class, "likeClause")
                .orElse(DataLoadCoordinator.LikeClause.NONE);
    }

    @SuppressWarnings("ClassCanBeRecord")
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
        public void execute(ComponentLoader.Context context) {
            Preconditions.checkNotNullArgument(facet.getOwner());

            ViewData viewData = ViewControllerUtils.getViewData(facet.getOwner());
            DataLoader loader = viewData.getLoader(loaderId);
            InstanceContainer<?> container = viewData.getContainer(containerId);

            facet.addOnContainerItemChangedLoadTrigger(loader, container, param);
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static class OnComponentValueChangedLoadTriggerInitTask implements ComponentLoader.InitTask {

        protected final DataLoadCoordinator facet;
        protected final String loaderId;
        protected final String componentId;
        protected final String param;
        protected final DataLoadCoordinator.LikeClause likeClause;

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
        public void execute(ComponentLoader.Context context) {
            Preconditions.checkNotNullArgument(facet.getOwner());

            ViewData viewData = ViewControllerUtils.getViewData(facet.getOwner());
            DataLoader loader = viewData.getLoader(loaderId);
            Component component = UiComponentUtils.getComponent(facet.getOwner(), componentId);

            facet.addOnComponentValueChangedLoadTrigger(loader, component, param, likeClause);
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static class AutoConfigurationInitTask implements ComponentLoader.InitTask {

        protected final DataLoadCoordinator facet;

        public AutoConfigurationInitTask(DataLoadCoordinator facet) {
            this.facet = facet;
        }

        @Override
        public void execute(ComponentLoader.Context context) {
            facet.configureAutomatically();
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
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
        public void execute(ComponentLoader.Context context) {
            Composite<?> owner = facet.getOwner();
            Preconditions.checkNotNullArgument(owner);

            HasDataComponents data;
            if (owner instanceof Fragment<?> fragment) {
                data = FragmentUtils.getFragmentData(fragment);
            } else if (owner instanceof View<?> view){
                data = ViewControllerUtils.getViewData(view);
            } else {
                throw new IllegalStateException("Unsupported owner of the %s"
                        .formatted(facet.getClass().getSimpleName()));
            }

            DataLoader loader = data.getLoader(loaderId);
            facet.addOnViewEventLoadTrigger(loader, eventClass);
        }
    }
}
