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

package io.jmix.flowui.sys.autowire;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import io.jmix.core.JmixOrder;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentActions;
import io.jmix.flowui.fragment.FragmentData;
import io.jmix.flowui.fragment.FragmentUtils;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.HasActions;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewControllerUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;

import java.util.Optional;

import static io.jmix.flowui.sys.ValuePathHelper.parse;
import static io.jmix.flowui.sys.ValuePathHelper.pathPrefix;

/**
 * An injector that autowires a fields and setters methods that are annotated by the {@link ViewComponent} annotation
 * in the {@link Fragment}.
 */
@Order(JmixOrder.LOWEST_PRECEDENCE - 90)
@org.springframework.stereotype.Component("flowui_FragmentElementsDependencyInjector")
public class FragmentElementsDependencyInjector extends AbstractElementsDependencyInjector {

    protected ApplicationContext applicationContext;

    public FragmentElementsDependencyInjector(ApplicationContext applicationContext,
                                              ReflectionCacheManager reflectionCacheManager) {
        super(reflectionCacheManager);
        this.applicationContext = applicationContext;
    }

    @Override
    @Nullable
    protected Object getAutowiredInstance(Class<?> type, String name, Composite<?> composite) {
        Fragment<?> fragment = (Fragment<?>) composite;

        if (Component.class.isAssignableFrom(type)) {
            Optional<Component> childComponent = UiComponentUtils.findComponent(fragment, name);
            // Autowiring a UI component
            return childComponent.orElse(null);
        } else if (InstanceContainer.class.isAssignableFrom(type)) {
            // Autowiring a container
            FragmentData data = FragmentUtils.getFragmentData(fragment);
            return data.getContainer(name);
        } else if (DataLoader.class.isAssignableFrom(type)) {
            // Autowiring a loader
            FragmentData data = FragmentUtils.getFragmentData(fragment);
            return data.getLoader(name);
        } else if (DataContext.class.isAssignableFrom(type)) {
            // Autowiring a dataContext
            FragmentData data = FragmentUtils.getFragmentData(fragment);
            return data.getDataContext();
        } else if (Action.class.isAssignableFrom(type)) {
            // Autowiring an action
            String[] elements = parse(name);
            if (elements.length == 1) {
                FragmentActions fragmentActions = FragmentUtils.getFragmentActions(fragment);
                return fragmentActions.getAction(name);
            }

            String prefix = pathPrefix(elements);
            return UiComponentUtils.findComponent(fragment, prefix)
                    .filter(c -> c instanceof HasActions)
                    .map(c -> ((HasActions) c))
                    .map(component -> component.getAction(elements[elements.length - 1]))
                    .orElse(null);
        } else if (MessageBundle.class == type) {
            return createMessageBundle(fragment);
        }

        return null;
    }

    protected MessageBundle createMessageBundle(Fragment<?> fragment) {
        MessageBundle messageBundle = applicationContext.getBean(MessageBundle.class);
        messageBundle.setMessageGroup(ViewControllerUtils.getPackage(fragment.getClass()));

        String templatePath = FragmentUtils.resolveDescriptorPath(fragment.getClass());
        String messageGroup = FragmentUtils.getMessageGroup(Strings.nullToEmpty(templatePath));
        if (!Strings.isNullOrEmpty(messageGroup)) {
            messageBundle.setMessageGroup(messageGroup);
        }

        return messageBundle;
    }

    @Override
    public boolean isApplicable(AutowireContext<?> autowireContext) {
        return autowireContext instanceof FragmentAutowireContext;
    }
}
