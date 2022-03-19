/*
 * Copyright 2021 Haulmont.
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

package com.haulmont.cuba.settings;

import com.google.common.base.Strings;
import com.haulmont.cuba.gui.screen.ScreenSettings;
import io.jmix.core.common.event.EventHub;
import io.jmix.core.common.event.Subscription;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.impl.AbstractFacet;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.UiControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;

public class CubaSettingsFacetImpl extends AbstractFacet implements CubaSettingsFacet {

    protected ScreenSettings screenSettings;

    protected Subscription beforeShowSubscription;
    protected Subscription afterShowSubscription;
    protected Subscription afterDetachedSubscription;

    protected Settings settings;

    @Autowired
    public void setScreenSettings(ScreenSettings screenSettings) {
        this.screenSettings = screenSettings;
    }

    @Override
    public void setOwner(@Nullable Frame owner) {
        super.setOwner(owner);

        unsubscribe();
        if (getScreenOwner() != null) {
            subscribe();
            settings = createSettings(getScreenOwner());
        }
    }

    @Nullable
    protected Screen getScreenOwner() {
        Frame frame = getOwner();
        if (frame == null) {
            return null;
        }
        if (frame.getFrameOwner() instanceof ScreenFragment) {
            throw new IllegalStateException("CubaSettingsFacet does not work in fragments");
        }

        return (Screen) frame.getFrameOwner();
    }

    protected void subscribe() {
        checkAttachedFrame();

        //noinspection ConstantConditions
        EventHub screenEvents = UiControllerUtils.getEventHub(getScreenOwner());

        beforeShowSubscription = screenEvents.subscribe(Screen.BeforeShowEvent.class, this::onBeforeShowEvent);
        afterShowSubscription = screenEvents.subscribe(Screen.AfterShowEvent.class, this::onAfterShowEvent);
        afterDetachedSubscription = screenEvents.subscribe(Screen.AfterDetachEvent.class, this::onAfterDetachEvent);
    }

    protected void unsubscribe() {
        if (beforeShowSubscription != null) {
            beforeShowSubscription.remove();
            beforeShowSubscription = null;
        }
        if (afterShowSubscription != null) {
            afterShowSubscription.remove();
            afterShowSubscription = null;
        }
        if (afterDetachedSubscription != null) {
            afterDetachedSubscription.remove();
            afterDetachedSubscription = null;
        }
    }

    protected void onBeforeShowEvent(Screen.BeforeShowEvent e) {
        screenSettings.applyDataLoadingSettings(e.getSource(), settings);
    }

    protected void onAfterShowEvent(Screen.AfterShowEvent e) {
        screenSettings.applySettings(e.getSource(), settings);
    }

    private void onAfterDetachEvent(Screen.AfterDetachEvent e) {
        screenSettings.saveSettings(e.getSource(), settings);
    }

    protected void checkAttachedFrame() {
        Frame frame = getOwner();
        if (frame == null) {
            throw new IllegalStateException("CubaSettingsFacet is not attached to the screen");
        }
    }

    protected Settings createSettings(FrameOwner frameOwner) {
        Screen screen = (Screen) frameOwner;
        if (Strings.isNullOrEmpty(screen.getId())) {
            throw new IllegalStateException(
                    String.format("Cannot create setting due to '%s' screen does not contain an id",
                            screen.getClass().getCanonicalName()));
        }
        return new SettingsImpl(screen.getId());
    }
}
