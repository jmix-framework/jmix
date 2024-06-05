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
import io.jmix.flowui.component.HasDataComponents;
import io.jmix.flowui.kit.component.HasActions;
import io.jmix.flowui.model.ViewData;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewActions;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.ComponentLoader.ComponentContext;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ComponentLoaderContext extends AbstractLoaderContext implements ComponentContext {

    protected View<?> view;
    protected String fullFrameId;
    protected String currentFrameId;

    protected List<ComponentLoader.InitTask> preInitTasks;
    protected List<ComponentLoader.AutowireTask> autowireTasks;

    public ComponentLoaderContext() {
    }

    @Override
    public ViewData getViewData() {
        if (dataHolder instanceof ViewData viewData) {
            return viewData;
        }

        throw new IllegalStateException("Data holder is not an instance of " +
                ViewData.class.getSimpleName());
    }

    /**
     * @deprecated Use {@link #setDataHolder(HasDataComponents)} instead
     */
    @Deprecated(since = "2.3", forRemoval = true)
    public void setViewData(ViewData viewData) {
        setDataHolder(viewData);
    }

    @Override
    public ViewActions getViewActions() {
        if (actionsHolder instanceof ViewActions viewActions) {
            return viewActions;
        }

        throw new IllegalStateException("Actions holder is not an instance of " +
                ViewActions.class.getSimpleName());
    }

    /**
     * @deprecated Use {@link #setActionsHolder(HasActions)} instead
     */
    @Deprecated(since = "2.3", forRemoval = true)
    public void setViewActions(ViewActions viewActions) {
        setActionsHolder(viewActions);
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

    @Override
    public String getFullFrameId() {
        return fullFrameId != null ? fullFrameId : getFullOriginId();
    }

    public void setFullFrameId(String frameId) {
        this.fullFrameId = frameId;
    }

    @Override
    public String getCurrentFrameId() {
        return currentFrameId != null ? currentFrameId : getFullOriginId();
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
    public Optional<ComponentContext> getParent() {
        if (parentContext == null) {
            return Optional.empty();
        }

        if (parentContext instanceof ComponentLoaderContext componentLoaderContext) {
            return Optional.of(componentLoaderContext);
        }

        throw new IllegalStateException("Parent context is not an instance of " +
                ComponentLoaderContext.class.getSimpleName());
    }

    /**
     * @deprecated Use {@link #setParentContext(ComponentLoader.Context)} instead
     */
    @Deprecated(since = "2.3", forRemoval = true)
    public void setParent(@Nullable ComponentContext parent) {
        setParentContext(parent);
    }

    @Deprecated(since = "2.3", forRemoval = true)
    public List<ComponentLoader.InitTask> getPreInitTasks() {
        return preInitTasks != null ? preInitTasks : Collections.emptyList();
    }

    @Deprecated(since = "2.3", forRemoval = true)
    public List<ComponentLoader.InitTask> getInitTasks() {
        return initTasks != null ? initTasks : Collections.emptyList();
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
