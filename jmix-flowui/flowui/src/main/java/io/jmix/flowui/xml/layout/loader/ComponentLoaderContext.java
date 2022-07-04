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

package io.jmix.flowui.xml.layout.loader;


import io.jmix.flowui.model.ViewData;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewActions;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.ComponentLoader.ComponentContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ComponentLoaderContext implements ComponentContext {

    protected ComponentContext parent;

    protected ViewData viewData;
    protected ViewActions viewActions;

    protected String messageGroup;
    protected View<?> view;
    protected String fullFrameId;
    protected String currentFrameId;

    protected List<ComponentLoader.InitTask> initTasks = new ArrayList<>();

    public ComponentLoaderContext() {
    }

    @Override
    public ViewData getViewData() {
        return viewData;
    }

    public void setViewData(ViewData viewData) {
        this.viewData = viewData;
    }

    @Override
    public ViewActions getViewActions() {
        return viewActions;
    }

    public void setViewActions(ViewActions viewActions) {
        this.viewActions = viewActions;
    }

    @Override
    public View<?> getView() {
        return view;
    }

    public void setView(View<?> view) {
        this.view = view;
    }

    @Override
    public String getMessageGroup() {
        return messageGroup;
    }

    public void setMessageGroup(String messageGroup) {
        this.messageGroup = messageGroup;
    }

    @Override
    public String getFullFrameId() {
        return fullFrameId;
    }

    public void setFullFrameId(String frameId) {
        this.fullFrameId = frameId;
    }

    @Override
    public String getCurrentFrameId() {
        return currentFrameId;
    }

    public void setCurrentFrameId(String currentFrameId) {
        this.currentFrameId = currentFrameId;
    }

    @Override
    public void addInitTask(ComponentLoader.InitTask task) {
        initTasks.add(task);
    }

    @Override
    public Optional<ComponentContext> getParent() {
        return Optional.ofNullable(parent);
    }

    public void setParent(@Nullable ComponentContext parent) {
        this.parent = parent;
    }

    public List<ComponentLoader.InitTask> getInitTasks() {
        return initTasks;
    }

    @Override
    public void executeInitTasks() {
        for (ComponentLoader.InitTask initTask : initTasks) {
            initTask.execute(this, view);
        }
        initTasks.clear();
    }
}
