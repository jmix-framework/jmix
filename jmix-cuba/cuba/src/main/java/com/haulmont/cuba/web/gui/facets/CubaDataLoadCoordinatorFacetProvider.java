/*
 * Copyright 2020 Haulmont.
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

package com.haulmont.cuba.web.gui.facets;

import com.haulmont.cuba.gui.xml.layout.CubaLoaderConfig;
import com.haulmont.cuba.web.gui.components.CubaDataLoadCoordinator;
import io.jmix.core.impl.QueryParamValuesManager;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.component.DataLoadCoordinator;
import io.jmix.ui.facet.DataLoadCoordinatorFacetProvider;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.sys.UiControllerReflectionInspector;
import io.jmix.ui.xml.layout.ComponentLoader;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(CubaDataLoadCoordinatorFacetProvider.NAME)
public class CubaDataLoadCoordinatorFacetProvider extends DataLoadCoordinatorFacetProvider {

    public static final String NAME = "cuba_DataLoadCoordinatorFacetProvider";

    @Autowired
    private UiControllerReflectionInspector reflectionInspector;

    @Autowired
    private QueryParamValuesManager queryParamValuesManager;

    @SuppressWarnings("rawtypes")
    @Override
    public Class getFacetClass() {
        return CubaDataLoadCoordinator.class;
    }

    @Override
    public DataLoadCoordinator create() {
        return new CubaDataLoadCoordinator(reflectionInspector, queryParamValuesManager);
    }

    @Override
    protected void loadRefresh(DataLoadCoordinator facet, ComponentLoader.ComponentContext context, Element element) {
        String schema = element.getNamespace().getStringValue();
        if (!schema.startsWith(CubaLoaderConfig.CUBA_XSD_PREFIX)) {
            super.loadRefresh(facet, context, element);
            return;
        }

        String loaderId = element.attributeValue("loader");
        if (loaderId == null) {
            throw new GuiDevelopmentException("'dataLoadCoordinator.loader' element has no 'ref' attribute", context);
        }

        String onScreenEvent = element.attributeValue("onScreenEvent");
        if (onScreenEvent != null) {
            Class eventClass;
            switch (onScreenEvent) {
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
                    throw new GuiDevelopmentException("Unsupported 'dataLoadCoordinator/refresh/onScreenEvent' " +
                            "value: " + onScreenEvent, context);
            }
            context.addInjectTask(new OnFrameOwnerEventLoadTriggerInitTask(facet, loaderId, eventClass));
            return;
        }

        String onFragmentEvent = element.attributeValue("onFragmentEvent");
        if (onFragmentEvent != null) {
            Class eventClass;
            switch (onFragmentEvent) {
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
                    throw new GuiDevelopmentException("Unsupported 'dataLoadCoordinator/refresh/onFragmentEvent' " +
                            "value: " + onFragmentEvent, context);
            }
            context.addInjectTask(new OnFrameOwnerEventLoadTriggerInitTask(facet, loaderId, eventClass));
            return;
        }

        String onContainerItemChanged = element.attributeValue("onContainerItemChanged");
        if (onContainerItemChanged != null) {
            String param = element.attributeValue("param");
            context.addInjectTask(new OnContainerItemChangedLoadTriggerInitTask(
                    facet, loaderId, onContainerItemChanged, param));
            return;
        }

        String onComponentValueChanged = element.attributeValue("onComponentValueChanged");
        if (onComponentValueChanged != null) {
            String param = loadParam(element);
            DataLoadCoordinator.LikeClause likeClause = loadLikeClause(element);

            context.addInjectTask(new OnComponentValueChangedLoadTriggerInitTask(
                    facet, loaderId, onComponentValueChanged, param, likeClause));
        }
    }
}
