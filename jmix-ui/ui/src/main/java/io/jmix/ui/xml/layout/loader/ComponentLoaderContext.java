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

package io.jmix.ui.xml.layout.loader;

import io.jmix.ui.component.Frame;
import io.jmix.ui.model.ScreenData;
import io.jmix.ui.screen.MapScreenOptions;
import io.jmix.ui.screen.ScreenOptions;
import io.jmix.ui.sys.UiControllerProperty;
import io.jmix.ui.xml.layout.ComponentLoader;
import io.jmix.ui.xml.layout.ComponentLoader.InitTask;
import io.jmix.ui.xml.layout.ComponentLoader.InjectTask;

import javax.annotation.Nullable;
import java.util.*;

public class ComponentLoaderContext implements ComponentLoader.ComponentContext {

    protected ComponentLoader.ComponentContext parent;
    protected ScreenOptions options;

    protected ScreenData screenData;

    protected String messageGroup;
    protected Frame frame;
    protected String fullFrameId;
    protected String currentFrameId;

    protected List<UiControllerProperty> properties = Collections.emptyList();

    protected List<InitTask> postInitTasks = new ArrayList<>();
    protected List<InjectTask> injectTasks = new ArrayList<>();
    protected List<InitTask> initTasks = new ArrayList<>();

    protected Map<String, Object> parameters;
    protected Map<String, String> aliasesMap = new HashMap<>();

    public ComponentLoaderContext(ScreenOptions options) {
        this.options = options;

        this.parameters = Collections.emptyMap();
        if (options instanceof MapScreenOptions) {
            parameters = ((MapScreenOptions) options).getParams();
        }
    }

    @Override
    public ScreenOptions getOptions() {
        return options;
    }

    @Override
    public Map<String, Object> getParams() {
        return parameters;
    }

    public ScreenData getScreenData() {
        return screenData;
    }

    public void setScreenData(ScreenData screenData) {
        this.screenData = screenData;
    }

    @Override
    public Frame getFrame() {
        return frame;
    }

    public void setFrame(Frame frame) {
        this.frame = frame;
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
    public void addPostInitTask(InitTask task) {
        postInitTasks.add(task);
    }

    @Nullable
    @Override
    public ComponentLoader.ComponentContext getParent() {
        return parent;
    }

    public void setParent(@Nullable ComponentLoader.ComponentContext parent) {
        this.parent = parent;
    }

    public List<UiControllerProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<UiControllerProperty> properties) {
        this.properties = properties;
    }

    @Override
    public void executePostInitTasks() {
        for (InitTask postInitTask : postInitTasks) {
            postInitTask.execute(this, frame);
        }
        postInitTasks.clear();
    }

    @Override
    public void addInjectTask(InjectTask task) {
        injectTasks.add(task);
    }

    @Override
    public void executeInjectTasks() {
        for (InjectTask injectTask : injectTasks) {
            injectTask.execute(ComponentLoaderContext.this, frame);
        }
        injectTasks.clear();
    }

    @Override
    public void addInitTask(InitTask task) {
        initTasks.add(task);
    }

    @Override
    public void executeInitTasks() {
        for (InitTask initTask : initTasks) {
            initTask.execute(this, frame);
        }
        initTasks.clear();
    }

    public List<InjectTask> getInjectTasks() {
        return injectTasks;
    }

    public List<InitTask> getPostInitTasks() {
        return postInitTasks;
    }

    public List<InitTask> getInitTasks() {
        return initTasks;
    }

    public Map<String, String> getAliasesMap() {
        return aliasesMap;
    }
}
