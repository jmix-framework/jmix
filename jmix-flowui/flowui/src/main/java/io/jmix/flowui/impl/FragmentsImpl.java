/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.impl;

import com.vaadin.flow.component.ComponentUtil;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.fragment.*;
import io.jmix.flowui.sys.ViewDescriptorUtils;
import io.jmix.flowui.sys.autowire.AutowireManager;
import io.jmix.flowui.sys.autowire.FragmentAutowireContext;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.view.ViewInfo;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.loader.ComponentLoaderContext;
import io.jmix.flowui.xml.layout.loader.FragmentDescriptorLoader;
import io.jmix.flowui.xml.layout.loader.FragmentLoader;
import io.jmix.flowui.xml.layout.loader.FragmentLoaderContext;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("flowui_Fragments")
public class FragmentsImpl implements Fragments {

    private static final Logger log = LoggerFactory.getLogger(FragmentsImpl.class);

    protected final ApplicationContext applicationContext;
    protected final FragmentDescriptorLoader fragmentDescriptorLoader;
    protected final UiComponents uiComponents;
    protected final ViewRegistry viewRegistry;
    protected final AutowireManager autowireManager;

    public FragmentsImpl(ApplicationContext applicationContext,
                         FragmentDescriptorLoader fragmentDescriptorLoader,
                         UiComponents uiComponents,
                         ViewRegistry viewRegistry,
                         AutowireManager autowireManager) {
        this.applicationContext = applicationContext;
        this.fragmentDescriptorLoader = fragmentDescriptorLoader;
        this.uiComponents = uiComponents;
        this.viewRegistry = viewRegistry;
        this.autowireManager = autowireManager;
    }

    @Override
    public <F extends Fragment<?>> F create(FragmentOwner parent, Class<F> fragmentClass) {
        Preconditions.checkNotNullArgument(parent, "Parent must not be null");
        Preconditions.checkNotNullArgument(fragmentClass, Fragment.class.getSimpleName() + " class must not be null");

        log.trace("Creating {} fragment", fragmentClass.getName());

        // fake host loader context
        ComponentLoader.Context hostContext = createHostLoaderContext(parent);

        F fragment = uiComponents.create(fragmentClass);
        init(hostContext, fragment);

        // perform automatic autowiring when the fragment is created programmatically
        if (hostContext instanceof ComponentLoader.ComponentContext componentContext) {
            componentContext.executeAutowireTasks();
        }

        return fragment;
    }

    @Override
    public void init(ComponentLoader.Context hostContext, Fragment<?> fragment) {
        Preconditions.checkNotNullArgument(hostContext, "Host context must not be null");
        Preconditions.checkNotNullArgument(fragment, Fragment.class.getSimpleName() + " must not be null");

        log.trace("Initializing {} fragment", fragment.getClass().getName());

        FragmentOwner origin = ((FragmentOwner) hostContext.getOrigin());
        FragmentUtils.setParentController(fragment, origin);

        FragmentData fragmentData = applicationContext.getBean(FragmentData.class);
        FragmentUtils.setFragmentData(fragment, fragmentData);

        FragmentActions actions = applicationContext.getBean(FragmentActions.class, fragment);
        FragmentUtils.setFragmentActions(fragment, actions);

        FragmentLoaderContext context;
        String descriptorPath = FragmentUtils.resolveDescriptorPath(fragment.getClass());
        if (descriptorPath != null) {
            context = new FragmentLoaderContext();
            context.setFragment(fragment);
            context.setFullOriginId(getFullOriginId(hostContext, fragment));
            context.setMessageGroup(FragmentUtils.getMessageGroup(descriptorPath));
            context.setActionsHolder(actions);
            context.setParentContext(hostContext);

            processFragmentDescriptor(context, descriptorPath);
        } else {
            context = null;
        }

        ComponentLoader.Context hostLoaderContext = findHostLoaderContext(hostContext);
        if (hostLoaderContext instanceof ComponentLoader.ComponentContext componentContext) {
            componentContext.addAutowireTask(__ -> postInit(fragment, context));
        } else {
            postInit(fragment, context);
        }
    }

    protected String getFullOriginId(ComponentLoader.Context hostContext, Fragment<?> fragment) {
        Optional<String> fragmentId = hostContext instanceof FragmentLoaderContext
                ? FragmentUtils.getComponentId(fragment)
                : fragment.getId();

        return hostContext.getFullOriginId() +
                "." +
                fragmentId.orElse(fragment.getClass().getSimpleName());
    }

    protected void postInit(Fragment<?> fragment, @Nullable FragmentLoaderContext context) {
        autowireFragment(fragment);

        // Init tasks should be executed after fragment is autowired,
        // but before ReadyEvent. In particular, this is needed to make
        // sure that setting properties from XML is processed after
        // UI components are injected which so properties are handled
        // the same for declarative creation of fragment and programmatic
        if (context != null) {
            context.executeInitTasks();
        }

        ComponentUtil.fireEvent(fragment, new Fragment.ReadyEvent(fragment));
    }

    protected void autowireFragment(Fragment<?> fragment) {
        FragmentAutowireContext fragmentAutowireContext = new FragmentAutowireContext(fragment);
        autowireManager.autowire(fragmentAutowireContext);
    }

    @Nullable
    protected ComponentLoader.Context findHostLoaderContext(ComponentLoader.Context hostContext) {
        ComponentLoader.Context targetContext = hostContext;
        while (targetContext.getParentContext() != null) {
            targetContext = targetContext.getParentContext();
        }

        return targetContext;
    }

    protected void processFragmentDescriptor(FragmentLoaderContext context, String descriptorPath) {
        Element element = fragmentDescriptorLoader.load(descriptorPath);
        FragmentLoader fragmentLoader = applicationContext.getBean(FragmentLoader.class, context, element);
        fragmentLoader.createContent();
    }

    protected ComponentLoader.Context createHostLoaderContext(FragmentOwner parent) {
        if (parent instanceof View<?> view) {
            return createHostViewLoaderContext(view);
        } else if (parent instanceof Fragment<?> fragment) {
            return createHostFragmentLoaderContext(fragment);
        }

        throw new IllegalArgumentException("Unknown parent type: " + parent.getClass().getName());
    }

    protected ComponentLoaderContext createHostViewLoaderContext(View<?> view) {
        ComponentLoaderContext hostContext = new ComponentLoaderContext();

        String viewId = ViewDescriptorUtils.getInferredViewId(view.getClass());
        ViewInfo viewInfo = viewRegistry.getViewInfo(viewId);

        hostContext.setView(view);
        hostContext.setActionsHolder(ViewControllerUtils.getViewActions(view));
        hostContext.setDataHolder(ViewControllerUtils.getViewData(view));
        hostContext.setMessageGroup(ViewControllerUtils.getPackage(viewInfo.getControllerClass()));
        hostContext.setFullOriginId(viewInfo.getId());

        return hostContext;
    }

    protected FragmentLoaderContext createHostFragmentLoaderContext(Fragment<?> fragment) {
        FragmentLoaderContext hostContext = new FragmentLoaderContext();
        hostContext.setFragment(fragment);
        hostContext.setFullOriginId(fragment.getId().orElse(fragment.getClass().getSimpleName()));
        hostContext.setActionsHolder(FragmentUtils.getFragmentActions(fragment));
        hostContext.setDataHolder(FragmentUtils.getFragmentData(fragment));
        hostContext.setMessageGroup(FragmentUtils.getPackage(fragment.getClass()));

        return hostContext;
    }
}
