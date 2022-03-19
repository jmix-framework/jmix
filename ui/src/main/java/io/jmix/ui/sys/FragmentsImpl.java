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

package io.jmix.ui.sys;

import com.vaadin.spring.annotation.UIScope;
import io.jmix.ui.*;
import io.jmix.ui.component.Fragment;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.impl.FragmentImplementation;
import io.jmix.ui.component.impl.FrameImplementation;
import io.jmix.ui.model.ScreenData;
import io.jmix.ui.monitoring.ScreenLifeCycle;
import io.jmix.ui.screen.*;
import io.jmix.ui.sys.FragmentHelper.FragmentLoaderInitTask;
import io.jmix.ui.sys.FragmentHelper.FragmentLoaderInjectTask;
import io.jmix.ui.xml.layout.ComponentLoader;
import io.jmix.ui.xml.layout.loader.ComponentLoaderContext;
import io.jmix.ui.xml.layout.loader.LayoutLoader;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.ParametersAreNonnullByDefault;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;
import static io.jmix.ui.monitoring.UiMonitoring.createScreenTimer;
import static io.jmix.ui.screen.UiControllerUtils.*;
import static java.util.Collections.emptyMap;

@UIScope
@Component("ui_Fragments")
@ParametersAreNonnullByDefault
public class FragmentsImpl implements Fragments {

    @Autowired
    protected ScreenXmlLoader screenXmlLoader;
    @Autowired
    protected WindowConfig windowConfig;
    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected MeterRegistry meterRegistry;

    @Autowired
    protected FragmentHelper fragmentHelper;

    protected AppUI ui;

    @Autowired
    public void setAppUi(AppUI ui) {
        this.ui = ui;
    }

    @Override
    public <T extends ScreenFragment> T create(FrameOwner parent, Class<T> requiredFragmentClass, ScreenOptions options) {
        checkNotNullArgument(parent);
        checkNotNullArgument(requiredFragmentClass);
        checkNotNullArgument(options);

        WindowInfo windowInfo = getFragmentInfo(requiredFragmentClass).resolve();

        return createFragment(parent, windowInfo, options);
    }

    @Override
    public ScreenFragment create(FrameOwner parent, String screenFragmentId, ScreenOptions options) {
        checkNotNullArgument(parent);
        checkNotNullArgument(screenFragmentId);
        checkNotNullArgument(options);

        WindowInfo windowInfo = windowConfig.getWindowInfo(screenFragmentId).resolve();

        return createFragment(parent, windowInfo, options);
    }

    protected <T extends ScreenFragment> WindowInfo getFragmentInfo(Class<T> fragmentClass) {
        UiController uiController = fragmentClass.getAnnotation(UiController.class);
        if (uiController == null) {
            throw new IllegalArgumentException("No @UiController annotation for class " + fragmentClass);
        }

        String screenId = UiDescriptorUtils.getInferredScreenId(uiController, fragmentClass);

        return windowConfig.getWindowInfo(screenId);
    }

    protected <T extends ScreenFragment> T createFragment(FrameOwner parent, WindowInfo windowInfo,
                                                          ScreenOptions options) {
        if (windowInfo.getType() != WindowInfo.Type.FRAGMENT) {
            throw new IllegalArgumentException(
                    String.format("Unable to create fragment %s it is a screen: %s", windowInfo.getId(), windowInfo.getControllerClass())
            );
        }

        Timer.Sample createSample = Timer.start(meterRegistry);

        Fragment fragment = createFragmentInternal();
        ScreenFragment controller = fragmentHelper.createController(windowInfo, fragment);

        // setup screen and controller

        setHostController(controller, parent);
        setWindowId(controller, windowInfo.getId());
        setFrame(controller, fragment);
        setScreenContext(controller,
                new ScreenContextImpl(windowInfo, options, getScreenContext(parent))
        );
        setScreenData(controller, applicationContext.getBean(ScreenData.class));

        FragmentImplementation fragmentImpl = (FragmentImplementation) fragment;
        fragmentImpl.setFrameOwner(controller);
        fragmentImpl.setId(controller.getId());

        createSample.stop(createScreenTimer(meterRegistry, ScreenLifeCycle.CREATE, windowInfo.getId()));

        Timer.Sample loadSample = Timer.start(meterRegistry);

        Frame parentFrame = getFrame(parent);

        // fake parent loader context
        ComponentLoaderContext loaderContext = createComponentLoaderContext(options);

        FragmentContextImpl frameContext = new FragmentContextImpl(fragment, loaderContext);
        frameContext.setManualInitRequired(true);
        ((FrameImplementation) fragment).setContext(frameContext);

        loaderContext.setCurrentFrameId(windowInfo.getId());
        loaderContext.setFullFrameId(windowInfo.getId());
        loaderContext.setFrame(fragment);
        loaderContext.setParent(null);
        loaderContext.setScreenData(UiControllerUtils.getScreenData(parent));

        // load XML if needed
        if (windowInfo.getTemplate() != null) {
            ComponentLoaderContext innerContext = createComponentLoaderContext(options);
            innerContext.setCurrentFrameId(windowInfo.getId());
            innerContext.setFullFrameId(windowInfo.getId());
            innerContext.setFrame(fragment);
            innerContext.setParent(loaderContext);

            LayoutLoader layoutLoader = applicationContext.getBean(LayoutLoader.class, innerContext);

            Element rootElement = screenXmlLoader.load(windowInfo.getTemplate(), windowInfo.getId(), emptyMap());

            innerContext.setMessageGroup(fragmentHelper.findMessageGroup(rootElement, windowInfo.getTemplate()));

            loadAdditionalData(rootElement, innerContext);

            ComponentLoader<Fragment> fragmentLoader =
                    layoutLoader.createFragmentContent(fragment, rootElement);

            fragmentLoader.loadComponent();

            loaderContext.getInjectTasks().addAll(innerContext.getInjectTasks());
            loaderContext.getInitTasks().addAll(innerContext.getInitTasks());
            loaderContext.getPostInitTasks().addAll(innerContext.getPostInitTasks());
        }

        loaderContext.addInjectTask(new FragmentLoaderInjectTask(fragment, options, applicationContext));
        loaderContext.addInitTask(new FragmentLoaderInitTask(fragment, options, loaderContext, applicationContext));

        loadSample.stop(createScreenTimer(meterRegistry, ScreenLifeCycle.LOAD, windowInfo.getId()));

        loaderContext.executeInjectTasks();

        fragmentImpl.setFrame(parentFrame);

        //noinspection unchecked
        return (T) controller;
    }

    protected void loadAdditionalData(Element rootElement, ComponentLoaderContext innerContext) {
        // do nothing
    }

    protected Fragment createFragmentInternal() {
        return uiComponents.create(Fragment.NAME);
    }

    @Override
    public void init(ScreenFragment controller) {
        checkNotNullArgument(controller);

        FragmentContextImpl fragmentContext = (FragmentContextImpl) controller.getFragment().getContext();
        if (fragmentContext.isInitialized()) {
            throw new IllegalStateException("Fragment is already initialized " + controller.getId());
        }

        ComponentLoaderContext loaderContext = fragmentContext.getLoaderContext();

        loaderContext.executeInitTasks();
        loaderContext.executePostInitTasks();

        fragmentContext.setInitialized(true);
    }

    protected ComponentLoaderContext createComponentLoaderContext(ScreenOptions options) {
        return new ComponentLoaderContext(options);
    }
}