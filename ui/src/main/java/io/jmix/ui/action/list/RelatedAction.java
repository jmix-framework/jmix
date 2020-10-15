/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.action.list;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.Screens.LaunchMode;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.data.meta.EntityDataUnit;
import io.jmix.ui.relatedentities.RelatedEntitiesSupport;
import io.jmix.ui.relatedentities.RelatedEntitiesBuilder;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenOptions;
import io.jmix.ui.sys.ActionScreenInitializer;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

//@StudioAction(category = "List Actions", description = "")
@ActionType(RelatedAction.ID)
public class RelatedAction extends SecuredListAction
        implements Action.AdjustWhenScreenReadOnly, Action.ScreenOpeningAction, Action.ExecutableAction {

    public static final String ID = "related";

    protected RelatedEntitiesSupport relatedEntitiesSupport;
    protected ActionScreenInitializer screenInitializer = new ActionScreenInitializer();

    protected MetaProperty metaProperty;
    protected String filterCaption;

    public RelatedAction() {
        super(ID);
    }

    public RelatedAction(String id) {
        super(id);
    }

    @Autowired
    public void setRelatedEntitiesApi(RelatedEntitiesSupport relatedEntitiesSupport) {
        this.relatedEntitiesSupport = relatedEntitiesSupport;
    }

    @Nullable
    @Override
    public LaunchMode getLaunchMode() {
        return screenInitializer.getLaunchMode();
    }

    @Override
    public void setLaunchMode(@Nullable LaunchMode launchMode) {
        screenInitializer.setLaunchMode(launchMode);
    }

    @Nullable
    @Override
    public String getScreenId() {
        return screenInitializer.getScreenId();
    }

    @Override
    public void setScreenId(@Nullable String screenId) {
        screenInitializer.setScreenId(screenId);
    }

    @Nullable
    @Override
    public Class<? extends Screen> getScreenClass() {
        return screenInitializer.getScreenClass();
    }

    @Override
    public void setScreenClass(@Nullable Class<? extends Screen> screenClass) {
        screenInitializer.setScreenClass(screenClass);
    }

    @Override
    public void setScreenOptionsSupplier(Supplier<ScreenOptions> screenOptionsSupplier) {
        screenInitializer.setScreenOptionsSupplier(screenOptionsSupplier);
    }

    @Override
    public void setScreenConfigurer(Consumer<Screen> screenConfigurer) {
        screenInitializer.setScreenConfigurer(screenConfigurer);
    }

    @Override
    public void setAfterCloseHandler(Consumer<Screen.AfterCloseEvent> afterCloseHandler) {
        screenInitializer.setAfterCloseHandler(afterCloseHandler);
    }

    public MetaProperty getMetaProperty() {
        return metaProperty;
    }

    public void setMetaProperty(MetaProperty metaProperty) {
        this.metaProperty = metaProperty;
    }

    @Nullable
    public String getFilterCaption() {
        return filterCaption;
    }

    public void setFilterCaption(@Nullable String filterCaption) {
        this.filterCaption = filterCaption;
    }

    @Override
    public void actionPerform(Component component) {
        // if standard behaviour
        if (!hasSubscriptions(ActionPerformedEvent.class)) {
            execute();
        } else {
            super.actionPerform(component);
        }
    }

    @Override
    public void execute() {
        if (target == null) {
            throw new IllegalStateException("RelatedAction target is not set");
        }

        if (!(target.getItems() instanceof EntityDataUnit)) {
            throw new IllegalStateException("RelatedAction target items is null or does not implement EntityDataUnit");
        }

        MetaClass metaClass = ((EntityDataUnit) target.getItems()).getEntityMetaClass();
        if (metaClass == null) {
            throw new IllegalStateException("Target is not bound to entity");
        }

        if (metaProperty == null) {
            throw new IllegalStateException("'metaProperty' is not set");
        }

        Frame frame = target.getFrame();
        if (frame == null) {
            throw new IllegalStateException("Target is not bound to a frame");
        }

        RelatedEntitiesBuilder builder = relatedEntitiesSupport.builder(frame.getFrameOwner())
                .withMetaClass(metaClass)
                .withMetaProperty(metaProperty)
                .withSelectedEntities(target.getSelected())
                .withFilterCaption(filterCaption);

        builder = screenInitializer.initBuilder(builder);

        Screen screen = builder.build();

        screenInitializer.initScreen(screen);

        screen.show();
    }
}
