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

import io.jmix.core.BeanLocator;
import io.jmix.core.HotDeployManager;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.components.Fragment;
import io.jmix.ui.components.Frame;
import io.jmix.ui.monitoring.ScreenLifeCycle;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.ScreenOptions;
import io.jmix.ui.xml.layout.ComponentLoader;
import io.jmix.ui.xml.layout.loaders.ComponentLoaderContext;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import static io.jmix.ui.components.ComponentsHelper.getFullFrameId;
import static io.jmix.ui.monitoring.UiMonitoring.createScreenTimer;
import static io.jmix.ui.screen.UiControllerUtils.fireEvent;
import static org.apache.commons.lang3.reflect.ConstructorUtils.invokeConstructor;

/**
 * Provides shared functionality for fragment initialization from XML and programmatic creation.
 */
@Component(FragmentHelper.NAME)
@ParametersAreNonnullByDefault
public class FragmentHelper {

    @Inject
    protected ScreenXmlLoader screenXmlLoader;
    @Inject
    protected HotDeployManager hotDeployManager;

    public static final String NAME = "jmix_FragmentHelper";

    @SuppressWarnings("unchecked")
    public ScreenFragment createController(WindowInfo windowInfo, Fragment fragment) {
        Class screenClass = windowInfo.getControllerClass();

        // new screens cannot be opened in fragments
        if (!ScreenFragment.class.isAssignableFrom(screenClass)) {
            throw new IllegalStateException(
                    String.format("Fragment controllers should inherit ScreenFragment." +
                                    " UI controller is not ScreenFragment - %s %s",
                            windowInfo.toString(), screenClass.getSimpleName()));
        }
        ScreenFragment controller;
        try {
            controller = (ScreenFragment) invokeConstructor(screenClass);
        } catch (NoSuchMethodException | InstantiationException
                | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unable to create instance of screen class " + screenClass);
        }

        return controller;
    }

    public String getMessagePack(String descriptorPath) {
        if (descriptorPath.contains("/")) {
            descriptorPath = StringUtils.substring(descriptorPath, 0, descriptorPath.lastIndexOf("/"));
        }

        String messagesPack = descriptorPath.replace("/", ".");
        int start = messagesPack.startsWith(".") ? 1 : 0;
        messagesPack = messagesPack.substring(start);
        return messagesPack;
    }

    @SuppressWarnings("unchecked")
    public WindowInfo createFakeWindowInfo(String src, String fragmentId) {
        Element screenElement = DocumentHelper.createElement("screen");
        screenElement.addAttribute("template", src);
        screenElement.addAttribute("id", fragmentId);

        Element windowElement = screenXmlLoader.load(src, fragmentId, Collections.emptyMap());
        Class<? extends ScreenFragment> fragmentClass;

        String className = windowElement.attributeValue("class");
        if (StringUtils.isNotEmpty(className)) {
            fragmentClass = (Class<? extends ScreenFragment>) hotDeployManager.loadClass(className);
        } else {
            // fragmentClass = AbstractFrame.class; todo
            throw new UnsupportedOperationException();
        }

        return new WindowInfo(fragmentId, new WindowAttributesProvider() {
            @Override
            public WindowInfo.Type getType(WindowInfo wi) {
                return WindowInfo.Type.FRAGMENT;
            }

            @Override
            public String getTemplate(WindowInfo wi) {
                return src;
            }

            @Nonnull
            @Override
            public Class<? extends FrameOwner> getControllerClass(WindowInfo wi) {
                return fragmentClass;
            }

            @Override
            public WindowInfo resolve(WindowInfo windowInfo) {
                return windowInfo;
            }
        }, screenElement);
    }

    public static class FragmentLoaderInjectTask implements ComponentLoader.InjectTask {
        protected Fragment fragment;
        protected ScreenOptions options;
        protected BeanLocator beanLocator;

        public FragmentLoaderInjectTask(Fragment fragment, ScreenOptions options, BeanLocator beanLocator) {
            this.fragment = fragment;
            this.options = options;
            this.beanLocator = beanLocator;
        }

        @Override
        public void execute(ComponentLoader.ComponentContext windowContext, Frame window) {
            MeterRegistry meterRegistry = beanLocator.get(MeterRegistry.class);
            Timer.Sample sample = Timer.start(meterRegistry);

            FrameOwner controller = fragment.getFrameOwner();
            UiControllerDependencyInjector dependencyInjector =
                    beanLocator.getPrototype(UiControllerDependencyInjector.NAME, controller, options);
            dependencyInjector.inject();

            sample.stop(createScreenTimer(meterRegistry, ScreenLifeCycle.INJECTION, getFullFrameId(this.fragment)));
        }
    }

    public static class FragmentLoaderInitTask implements ComponentLoader.InitTask {
        protected Fragment fragment;
        protected ScreenOptions options;
        protected ComponentLoaderContext fragmentLoaderContext;
        protected BeanLocator beanLocator;

        public FragmentLoaderInitTask(Fragment fragment, ScreenOptions options,
                                      ComponentLoaderContext fragmentLoaderContext, BeanLocator beanLocator) {
            this.fragment = fragment;
            this.options = options;
            this.fragmentLoaderContext = fragmentLoaderContext;
            this.beanLocator = beanLocator;
        }

        @Override
        public void execute(ComponentLoader.ComponentContext windowContext, Frame window) {
            MeterRegistry meterRegistry = beanLocator.get(MeterRegistry.class);
            Timer.Sample sample = Timer.start(meterRegistry);

            ScreenFragment frameOwner = fragment.getFrameOwner();

            fireEvent(frameOwner, ScreenFragment.InitEvent.class,
                    new ScreenFragment.InitEvent(frameOwner, options));

            sample.stop(createScreenTimer(meterRegistry, ScreenLifeCycle.INIT, getFullFrameId(this.fragment)));

            fireEvent(frameOwner, ScreenFragment.AfterInitEvent.class,
                    new ScreenFragment.AfterInitEvent(frameOwner, options));

            List<UiControllerProperty> properties = fragmentLoaderContext.getProperties();
            if (!properties.isEmpty()) {
                UiControllerPropertyInjector propertyInjector =
                        beanLocator.getPrototype(UiControllerPropertyInjector.NAME, frameOwner, properties);
                propertyInjector.inject();
            }

            FragmentContextImpl fragmentContext = (FragmentContextImpl) fragment.getContext();
            fragmentContext.setInitialized(true);

            // fire attached

            if (!fragmentContext.isManualInitRequired()) {
                fireEvent(frameOwner, ScreenFragment.AttachEvent.class,
                        new ScreenFragment.AttachEvent(frameOwner));
            }
        }
    }
}