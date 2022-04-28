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


import io.jmix.flowui.model.ScreenData;
import io.jmix.flowui.screen.Screen;
import io.jmix.flowui.screen.ScreenActions;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.ComponentLoader.ComponentContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ComponentLoaderContext implements ComponentContext {

    protected ComponentContext parent;
//    protected ScreenOptions options;

    protected ScreenData screenData;
    protected ScreenActions screenActions;

    protected String messageGroup;
    protected Screen screen;
    protected String fullFrameId;
    protected String currentFrameId;

//    protected List<UiControllerProperty> properties = Collections.emptyList();

    protected List<ComponentLoader.InitTask> initTasks = new ArrayList<>();
//    protected List<InjectTask> injectTasks = new ArrayList<>();
//    protected List<InitTask> initTasks = new ArrayList<>();

    public ComponentLoaderContext(/*ScreenOptions options*/) {
//        this.options = options;

//        this.parameters = Collections.emptyMap();
//        if (options instanceof MapScreenOptions) {
//            parameters = ((MapScreenOptions) options).getParams();
//        }
    }

    /*@Override
    public ScreenOptions getOptions() {
        return options;
    }*/

    @Override
    public ScreenData getScreenData() {
        return screenData;
    }

    public void setScreenData(ScreenData screenData) {
        this.screenData = screenData;
    }

    @Override
    public ScreenActions getScreenActions() {
        return screenActions;
    }

    public void setScreenActions(ScreenActions screenActions) {
        this.screenActions = screenActions;
    }

    @Override
    public Screen getScreen() {
        return screen;
    }

    public void setScreen(Screen screen) {
        this.screen = screen;
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

    /*public List<UiControllerProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<UiControllerProperty> properties) {
        this.properties = properties;
    }*/

    @Override
    public void executeInitTasks() {
        for (ComponentLoader.InitTask initTask : initTasks) {
            initTask.execute(this, screen);
        }
        initTasks.clear();
    }

    /*@Override
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
*/
    public List<ComponentLoader.InitTask> getInitTasks() {
        return initTasks;
    }
/*
    public List<InitTask> getInitTasks() {
        return initTasks;
    }*/
}
