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
import io.jmix.flowui.component.HasDataComponents;
import io.jmix.flowui.kit.component.HasActions;
import io.jmix.flowui.xml.layout.ComponentLoader;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractLoaderContext implements ComponentLoader.Context {

    protected String fullOriginId;

    protected ComponentLoader.Context parentContext;

    protected HasDataComponents dataHolder;
    protected HasActions actionsHolder;

    protected String messageGroup;
    protected List<ComponentLoader.InitTask> initTasks;

    @Override
    public String getFullOriginId() {
        return fullOriginId;
    }

    public void setFullOriginId(String fullOriginId) {
        this.fullOriginId = fullOriginId;
    }

    @Nullable
    @Override
    public ComponentLoader.Context getParentContext() {
        return parentContext;
    }

    public void setParentContext(@Nullable ComponentLoader.Context parentContext) {
        this.parentContext = parentContext;
    }

    @Override
    public String getMessageGroup() {
        return messageGroup;
    }

    public void setMessageGroup(String messageGroup) {
        this.messageGroup = messageGroup;
    }

    @Override
    public HasActions getActionsHolder() {
        return actionsHolder;
    }

    public void setActionsHolder(HasActions actionsHolder) {
        this.actionsHolder = actionsHolder;
    }

    @Override
    public HasDataComponents getDataHolder() {
        return dataHolder;
    }

    public void setDataHolder(HasDataComponents dataHolder) {
        this.dataHolder = dataHolder;
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
