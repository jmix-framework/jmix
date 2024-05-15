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

package io.jmix.flowui.xml.layout.loader;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.component.composite.CompositeComponent;
import io.jmix.flowui.kit.component.HasActions;
import io.jmix.flowui.xml.layout.ComponentLoader;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class CompositeComponentLoaderContext implements ComponentLoader.CompositeComponentContext {

    protected String messageGroup;
    protected List<ComponentLoader.InitTask> initTasks;

    protected CompositeComponent<?> composite;
    protected HasActions actionsHolder;

    @Override
    public String getMessageGroup() {
        return messageGroup;
    }

    public void setMessageGroup(String messageGroup) {
        this.messageGroup = messageGroup;
    }

    @Override
    public Component getOrigin() {
        return getComposite();
    }

    @Override
    public CompositeComponent<?> getComposite() {
        return composite;
    }

    public void setComposite(CompositeComponent<?> composite) {
        this.composite = composite;
    }

    @Override
    public HasActions getActionsHolder() {
        return actionsHolder;
    }

    public void setActionsHolder(HasActions actionsHolder) {
        this.actionsHolder = actionsHolder;
    }

    @Override
    public void addInitTask(ComponentLoader.InitTask task) {
        if (initTasks == null) {
            initTasks = new ArrayList<>();
        }

        initTasks.add(task);
    }

    @Override
    public void executeInitTasks() {
        if (CollectionUtils.isNotEmpty(initTasks)) {
            for (ComponentLoader.InitTask initTask : initTasks) {
                initTask.execute(this);
            }
            initTasks.clear();
        }
    }
}