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

package io.jmix.flowui.xml.layout.loader;


import com.vaadin.flow.component.Component;
import io.jmix.flowui.view.View;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.ComponentLoader.ComponentContext;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class ComponentLoaderContext extends AbstractLoaderContext implements ComponentContext {

    protected View<?> view;
    protected String fullFrameId;
    protected String currentFrameId;

    protected List<ComponentLoader.InitTask> preInitTasks;
    protected List<ComponentLoader.AutowireTask> autowireTasks;

    public ComponentLoaderContext() {
    }

    @Override
    public Component getOrigin() {
        return getView();
    }

    @Override
    public View<?> getView() {
        return view;
    }

    public void setView(View<?> view) {
        this.view = view;
    }

    public void setFullFrameId(String frameId) {
        this.fullFrameId = frameId;
    }

    public void setCurrentFrameId(String currentFrameId) {
        this.currentFrameId = currentFrameId;
    }

    @Override
    public void addPreInitTask(ComponentLoader.InitTask task) {
        if (preInitTasks == null) {
            preInitTasks = new ArrayList<>();
        }

        preInitTasks.add(task);
    }

    @Override
    public void addAutowireTask(ComponentLoader.AutowireTask task) {
        if (autowireTasks == null) {
            autowireTasks = new ArrayList<>();
        }

        autowireTasks.add(task);
    }

    @Override
    public void executePreInitTasks() {
        if (CollectionUtils.isNotEmpty(preInitTasks)) {
            for (ComponentLoader.InitTask initTask : preInitTasks) {
                initTask.execute(this);
            }
            preInitTasks.clear();
        }
    }

    @Override
    public void executeAutowireTasks() {
        if (CollectionUtils.isNotEmpty(autowireTasks)) {
            for (ComponentLoader.AutowireTask autowireTask : autowireTasks) {
                autowireTask.execute(this);
            }
            autowireTasks.clear();
        }
    }
}
