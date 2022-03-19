/*
 * Copyright 2019 Haulmont.
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
package io.jmix.ui.xml.layout.loader;

import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.component.Fragment;
import io.jmix.ui.component.impl.FragmentImplementation;
import io.jmix.ui.component.impl.FrameImplementation;
import io.jmix.ui.model.ScreenData;
import io.jmix.ui.monitoring.ScreenLifeCycle;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.ScreenContext;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.ScreenOptions;
import io.jmix.ui.sys.*;
import io.jmix.ui.sys.FragmentHelper.FragmentLoaderInitTask;
import io.jmix.ui.xml.layout.ComponentLoader;
import io.micrometer.core.instrument.Timer;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.jmix.ui.monitoring.UiMonitoring.createScreenTimer;
import static io.jmix.ui.screen.UiControllerUtils.*;
import static io.jmix.ui.sys.FragmentHelper.FragmentLoaderInjectTask;

public class FragmentComponentLoader extends ContainerLoader<Fragment> {

    protected ComponentLoader fragmentLoader;
    protected ComponentLoaderContext innerContext;

    @Override
    public void createComponent() {
        WindowInfo windowInfo = createWindowInfo(element);
        String fragmentId;
        if (element.attributeValue("id") != null) {
            fragmentId = element.attributeValue("id");
        } else {
            fragmentId = windowInfo.getId();
        }

        Timer.Sample createSample = Timer.start(getMeterRegistry());

        Fragment fragment = createComponentInternal();
        FragmentHelper fragmentHelper = getFragmentHelper();
        ScreenFragment controller = fragmentHelper.createController(windowInfo, fragment);

        // setup screen and controller
        ComponentLoaderContext parentContext = (ComponentLoaderContext) getContext();

        FrameOwner hostController = parentContext.getFrame().getFrameOwner();

        setHostController(controller, hostController);
        setWindowId(controller, windowInfo.getId());
        setFrame(controller, fragment);
        setupScreenContext(controller,
                new ScreenContextImpl(windowInfo, parentContext.getOptions(), getScreenContext(hostController))
        );
        setScreenData(controller, applicationContext.getBean(ScreenData.class));

        FragmentImplementation fragmentImpl = (FragmentImplementation) fragment;
        fragmentImpl.setFrameOwner(controller);
        fragmentImpl.setId(fragmentId);

        FragmentContextImpl frameContext = new FragmentContextImpl(fragment, innerContext);
        ((FrameImplementation) fragment).setContext(frameContext);

        // load from XML if needed

        if (windowInfo.getTemplate() != null) {
            String frameId = fragmentId;
            if (parentContext.getFullFrameId() != null) {
                frameId = parentContext.getFullFrameId() + "." + frameId;
            }

            innerContext = createInnerContext();
            innerContext.setCurrentFrameId(fragmentId);
            innerContext.setFullFrameId(frameId);
            innerContext.setFrame(fragment);
            innerContext.setParent(parentContext);
            innerContext.setProperties(loadProperties(element));

            LayoutLoader layoutLoader = getLayoutLoader(innerContext);

            ScreenXmlLoader screenXmlLoader = applicationContext.getBean(ScreenXmlLoader.class);

            Element rootElement = screenXmlLoader.load(windowInfo.getTemplate(), windowInfo.getId(),
                    getComponentContext().getParams());

            innerContext.setMessageGroup(fragmentHelper.findMessageGroup(rootElement, windowInfo.getTemplate()));

            loadAdditionalData(rootElement);

            this.fragmentLoader = layoutLoader.createFragmentContent(fragment, rootElement);
        }

        createSample.stop(createScreenTimer(getMeterRegistry(), ScreenLifeCycle.CREATE, windowInfo.getId()));

        this.resultComponent = fragment;
    }

    protected void loadAdditionalData(Element rootElement) {
        // do nothing
    }

    protected Fragment createComponentInternal() {
        return factory.create(Fragment.NAME);
    }

    protected WindowInfo createWindowInfo(Element element) {
        String screenId = element.attributeValue("screen");
        if (screenId == null) {
            throw new GuiDevelopmentException("'screen' attribute must be specified for 'fragment'",
                    context, "fragment", element.attributeValue("id"));
        }

        return getWindowConfig().getWindowInfo(screenId).resolve();
    }

    protected ComponentLoaderContext createInnerContext() {
        return new ComponentLoaderContext(getComponentContext().getOptions());
    }

    protected void setupScreenContext(ScreenFragment controller, ScreenContext screenContext) {
        setScreenContext(controller, screenContext);
    }

    protected FragmentHelper getFragmentHelper() {
        return applicationContext.getBean(FragmentHelper.class);
    }

    @Override
    public void loadComponent() {
        loadAliases();

        if (getComponentContext().getFrame() != null) {
            resultComponent.setFrame(getComponentContext().getFrame());
        }

        String src = element.attributeValue("src");
        String screenId = element.attributeValue("screen");
        String screenPath = StringUtils.isEmpty(screenId) ? src : screenId;
        if (element.attributeValue("id") != null) {
            screenPath = element.attributeValue("id");
        }
        if (getComponentContext().getFrame() != null) {
            String parentId = getComponentContext().getFullFrameId();
            if (StringUtils.isNotEmpty(parentId)) {
                screenPath = parentId + "." + screenPath;
            }
        }

        Timer.Sample sample = Timer.start(getMeterRegistry());

        // if fragment has XML descriptor

        if (fragmentLoader != null) {
            fragmentLoader.loadComponent();
        }

        // load properties after inner context, they must override values defined inside of fragment

        assignXmlDescriptor(resultComponent, element);
        loadVisible(resultComponent, element);
        loadEnable(resultComponent, element);

        loadStyleName(resultComponent, element);
        loadResponsive(resultComponent, element);
        loadCss(resultComponent, element);

        loadAlign(resultComponent, element);

        loadHeight(resultComponent, element);
        loadWidth(resultComponent, element);

        loadIcon(resultComponent, element);
        loadCaption(resultComponent, element);
        loadDescription(resultComponent, element);

        sample.stop(createScreenTimer(getMeterRegistry(), ScreenLifeCycle.LOAD, screenPath));

        // propagate init phases

        ComponentLoaderContext parentContext = (ComponentLoaderContext) getContext();

        if (innerContext != null) {
            parentContext.getInjectTasks().addAll(innerContext.getInjectTasks());
            parentContext.getInitTasks().addAll(innerContext.getInitTasks());
            parentContext.getPostInitTasks().addAll(innerContext.getPostInitTasks());
        }

        ScreenOptions options = parentContext.getOptions();
        parentContext.addInjectTask(new FragmentLoaderInjectTask(resultComponent, options, applicationContext));
        parentContext.addInitTask(new FragmentLoaderInitTask(resultComponent, options, innerContext, applicationContext));
    }

    protected List<UiControllerProperty> loadProperties(Element element) {
        Element propsEl = element.element("properties");
        if (propsEl == null) {
            return Collections.emptyList();
        }

        List<Element> propElements = propsEl.elements("property");
        if (propElements.isEmpty()) {
            return Collections.emptyList();
        }

        List<UiControllerProperty> properties = new ArrayList<>(propElements.size());

        for (Element property : propElements) {
            String name = property.attributeValue("name");
            if (name == null || name.isEmpty()) {
                throw new GuiDevelopmentException("Screen fragment property cannot have empty name", context);
            }

            String value = property.attributeValue("value");
            String ref = property.attributeValue("ref");

            if (StringUtils.isNotEmpty(value) && StringUtils.isNotEmpty(ref)) {
                throw new GuiDevelopmentException("Screen fragment property can have either a value or a reference. Property: " +
                        name, context);
            }

            if (StringUtils.isNotEmpty(value)) {
                properties.add(new UiControllerProperty(name, value, UiControllerProperty.Type.VALUE));
            } else if (StringUtils.isNotEmpty(ref)) {
                properties.add(new UiControllerProperty(name, ref, UiControllerProperty.Type.REFERENCE));
            } else {
                throw new GuiDevelopmentException("No value or reference found for screen fragment property: " + name,
                        context);
            }
        }

        return properties;
    }

    protected void loadAliases() {
        if (fragmentLoader instanceof FragmentLoader) {
            ComponentLoaderContext frameLoaderInnerContext = (ComponentLoaderContext) fragmentLoader.getContext();
            for (Element aliasElement : element.elements("dsAlias")) {
                String aliasDatasourceId = aliasElement.attributeValue("alias");
                String originalDatasourceId = aliasElement.attributeValue("datasource");
                if (StringUtils.isNotBlank(aliasDatasourceId) && StringUtils.isNotBlank(originalDatasourceId)) {
                    frameLoaderInnerContext.getAliasesMap().put(aliasDatasourceId, originalDatasourceId);
                }
            }
        }
    }

    protected WindowConfig getWindowConfig() {
        return applicationContext.getBean(WindowConfig.class);
    }
}
