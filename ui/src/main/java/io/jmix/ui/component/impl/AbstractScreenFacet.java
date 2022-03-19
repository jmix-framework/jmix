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

package io.jmix.ui.component.impl;

import io.jmix.core.common.event.Subscription;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.component.*;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenOptions;
import io.jmix.ui.sys.UiControllerProperty;
import io.jmix.ui.sys.UiControllerPropertyInjector;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public abstract class AbstractScreenFacet<S extends Screen> extends AbstractFacet
        implements ScreenFacet<S> {

    protected ApplicationContext applicationContext;

    protected String screenId;
    protected Class<S> screenClass;

    protected OpenMode openMode = OpenMode.NEW_TAB;

    protected Consumer<S> screenConfigurer;
    protected Supplier<ScreenOptions> optionsProvider;
    protected Collection<UiControllerProperty> properties;

    protected String actionId;
    protected String buttonId;

    protected List<Consumer<Screen.AfterShowEvent>> afterShowListeners = new ArrayList<>();
    protected List<Consumer<Screen.AfterCloseEvent>> afterCloseListeners = new ArrayList<>();

    protected S screen;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setScreenId(@Nullable String screenId) {
        this.screenId = screenId;
    }

    @Nullable
    @Override
    public String getScreenId() {
        return screenId;
    }

    @Override
    public void setScreenClass(@Nullable Class<S> screenClass) {
        this.screenClass = screenClass;
    }

    @Nullable
    @Override
    public Class<S> getScreenClass() {
        return screenClass;
    }

    @Override
    public OpenMode getOpenMode() {
        return openMode;
    }

    @Override
    public void setOpenMode(OpenMode openMode) {
        this.openMode = openMode;
    }

    @Override
    public void setOptionsProvider(@Nullable Supplier<ScreenOptions> optionsProvider) {
        this.optionsProvider = optionsProvider;
    }

    @Nullable
    @Override
    public Supplier<ScreenOptions> getOptionsProvider() {
        return optionsProvider;
    }

    @Nullable
    @Override
    public Consumer<S> getScreenConfigurer() {
        return screenConfigurer;
    }

    @Override
    public void setScreenConfigurer(Consumer<S> screenConfigurer) {
        this.screenConfigurer = screenConfigurer;
    }

    @Override
    public void setProperties(Collection<UiControllerProperty> properties) {
        this.properties = properties;
    }

    @Nullable
    @Override
    public Collection<UiControllerProperty> getProperties() {
        return properties;
    }

    @Nullable
    @Override
    public String getActionTarget() {
        return actionId;
    }

    @Override
    public void setActionTarget(@Nullable String actionId) {
        this.actionId = actionId;
    }

    @Nullable
    @Override
    public String getButtonTarget() {
        return buttonId;
    }

    @Override
    public void setButtonTarget(@Nullable String buttonId) {
        this.buttonId = buttonId;
    }

    @Override
    public void setOwner(@Nullable Frame owner) {
        super.setOwner(owner);

        subscribe();
    }

    @Override
    public Subscription addAfterShowEventListener(Consumer<Screen.AfterShowEvent> listener) {
        afterShowListeners.add(listener);
        return () -> internalRemoveAfterShowEventListener(listener);
    }

    protected void internalRemoveAfterShowEventListener(Consumer<Screen.AfterShowEvent> listener) {
        afterShowListeners.remove(listener);
    }

    @Override
    public Subscription addAfterCloseEventListener(Consumer<Screen.AfterCloseEvent> listener) {
        afterCloseListeners.add(listener);
        return () -> internalRemoveAfterCloseEventListener(listener);
    }

    protected void internalRemoveAfterCloseEventListener(Consumer<Screen.AfterCloseEvent> listener) {
        afterCloseListeners.remove(listener);
    }

    protected void initScreenListeners(Screen screen) {
        for (Consumer<Screen.AfterShowEvent> afterShowListener : afterShowListeners) {
            screen.addAfterShowListener(afterShowListener);
        }

        for (Consumer<Screen.AfterCloseEvent> afterCloseListener : afterCloseListeners) {
            screen.addAfterCloseListener(afterCloseListener);
        }
    }

    protected void subscribe() {
        Frame owner = getOwner();
        if (owner == null) {
            throw new IllegalStateException("Notification is not attached to Frame");
        }

        if (isNotEmpty(actionId)
                && isNotEmpty(buttonId)) {
            throw new GuiDevelopmentException(
                    "Notification facet should have either action or button target", owner.getId());
        }

        if (isNotEmpty(actionId)) {
            subscribeOnAction(owner);
        } else if (isNotEmpty(buttonId)) {
            subscribeOnButton(owner);
        }
    }

    protected void subscribeOnAction(Frame owner) {
        Action action = ComponentsHelper.findAction(owner, actionId);

        if (!(action instanceof BaseAction)) {
            throw new GuiDevelopmentException(
                    String.format("Unable to find Screen target action with id '%s'", actionId),
                    owner.getId());
        }

        ((BaseAction) action).addActionPerformedListener(e -> show());
    }

    protected void subscribeOnButton(Frame owner) {
        Component component = owner.getComponent(buttonId);

        if (!(component instanceof Button)) {
            throw new GuiDevelopmentException(
                    String.format("Unable to find Screen target button with id '%s'", buttonId),
                    owner.getId());
        }

        ((Button) component).addClickListener(e -> show());
    }

    protected void injectScreenProperties(S screen, Collection<UiControllerProperty> properties) {
        if (CollectionUtils.isNotEmpty(properties)) {
            if (applicationContext == null) {
                throw new IllegalStateException("Unable to inject properties. ApplicationContext is null.");
            }

            UiControllerPropertyInjector injector = applicationContext.getBean(UiControllerPropertyInjector.class,
                    screen, owner.getFrameOwner(), properties);

            injector.inject();
        }
    }

    protected void applyScreenConfigurer(S screen) {
        if (screenConfigurer != null) {
            screenConfigurer.accept(screen);
        }
    }

    protected ScreenOptions getScreenOptions() {
        return optionsProvider != null
                ? optionsProvider.get()
                : FrameOwner.NO_OPTIONS;
    }
}
