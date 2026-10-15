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

package io.jmix.flowui.component.splitlayout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayoutI18n;
import io.jmix.core.Messages;
import io.jmix.flowui.component.ComponentContainer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.jmix.flowui.component.UiComponentUtils.sameId;

public class JmixSplitLayout extends SplitLayout implements ComponentContainer, ApplicationContextAware,
        InitializingBean {

    protected ApplicationContext applicationContext;
    protected Messages messages;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        autowireDependencies();
        initComponent();
    }

    protected void autowireDependencies() {
        messages = applicationContext.getBean(Messages.class);
    }

    protected void initComponent() {
        setI18n(new SplitLayoutI18n()
                .setSeparator(messages.getMessage("splitLayout.i18n.separator")));
    }

    @Override
    public Optional<Component> findOwnComponent(String id) {
        return getOwnComponents().stream()
                .filter(component -> sameId(component, id))
                .findAny();
    }

    @Override
    public Collection<Component> getOwnComponents() {
        List<Component> ownComponents = new ArrayList<>();

        if (getPrimaryComponent() != null) {
            ownComponents.add(getPrimaryComponent());
        }

        if (getSecondaryComponent() != null) {
            ownComponents.add(getSecondaryComponent());
        }

        return ownComponents.isEmpty() ? Collections.emptyList() : List.copyOf(ownComponents);
    }
}
