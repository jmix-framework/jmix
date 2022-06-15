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

package io.jmix.flowui.xml.layout.support;

import io.jmix.core.annotation.Internal;
import io.jmix.flowui.facet.Facet;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.model.ScreenData;
import io.jmix.flowui.model.impl.ScreenDataXmlLoader;
import io.jmix.flowui.screen.Screen;
import io.jmix.flowui.screen.ScreenActions;
import io.jmix.flowui.screen.ScreenFacets;
import io.jmix.flowui.screen.UiControllerUtils;
import io.jmix.flowui.xml.facet.FacetLoader;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.loader.ComponentLoaderContext;
import org.dom4j.Element;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;

@Internal
@Component("flowui_ScreenLoaderSupport")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ScreenLoaderSupport implements ApplicationContextAware {

    protected ApplicationContext applicationContext;
    protected ScreenDataXmlLoader screenDataXmlLoader;

    protected ActionLoaderSupport actionLoaderSupport;

    protected Screen<?> screen;
    protected ComponentLoader.Context context;

    public ScreenLoaderSupport(Screen<?> screen, ComponentLoader.Context context) {
        this.screen = screen;
        this.context = context;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public void setScreenDataXmlLoader(ScreenDataXmlLoader screenDataXmlLoader) {
        this.screenDataXmlLoader = screenDataXmlLoader;
    }

    protected ActionLoaderSupport getActionLoaderSupport() {
        if (actionLoaderSupport == null) {
            actionLoaderSupport = applicationContext.getBean(ActionLoaderSupport.class, context);
        }
        return actionLoaderSupport;
    }

    public void loadScreenData(Element element) {
        Element dataElement = element.element("data");
        if (dataElement != null) {
            ScreenData screenData = UiControllerUtils.getScreenData(screen);
            screenDataXmlLoader.load(screenData, dataElement, null);
            ((ComponentLoaderContext) context).setScreenData(screenData);
        }
    }

    public void loadScreenActions(Element element) {
        Element actionsEl = element.element("actions");
        if (actionsEl == null) {
            return;
        }

        ScreenActions screenActions = UiControllerUtils.getScreenActions(screen);
        for (Element actionEl : actionsEl.elements("action")) {
            screenActions.addAction(loadDeclarativeAction(actionEl));
        }

        ((ComponentLoaderContext) context).setScreenActions(screenActions);
    }

    public void loadFacets(Element element) {
        Element facetsElement = element.element("facets");
        if (facetsElement != null) {
            List<Element> facetElements = facetsElement.elements();

            ScreenFacets screenFacets = UiControllerUtils.getScreenFacets(screen);
            FacetLoader loader = applicationContext.getBean(FacetLoader.class);
            for (Element facetElement : facetElements) {
                Facet facet = loader.load(facetElement, getComponentContext());

                screenFacets.addFacet(facet);
            }
        }
    }

    protected Action loadDeclarativeAction(Element element) {
        return getActionLoaderSupport().loadDeclarativeActionByType(element)
                .orElseGet(() ->
                        getActionLoaderSupport().loadDeclarativeAction(element));
    }

    protected ComponentLoader.ComponentContext getComponentContext() {
        checkState(context instanceof ComponentLoader.ComponentContext,
                "'context' must implement " + ComponentLoader.ComponentContext.class.getName());

        return (ComponentLoader.ComponentContext) context;
    }
}
