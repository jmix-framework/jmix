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

package io.jmix.ui.component;

import io.jmix.core.common.event.Subscription;
import org.springframework.context.ApplicationContextAware;
import io.jmix.ui.Screens;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioFacet;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenOptions;
import io.jmix.ui.sys.UiControllerProperty;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.EventObject;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Prepares and shows screens.
 */
@StudioFacet(
        xmlElement = "screen",
        caption = "Screen",
        description = "Prepares and shows screens",
        defaultProperty = "screenId",
        category = "Non-visual"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "id", type = PropertyType.COMPONENT_ID, required = true)
        }
)
public interface ScreenFacet<S extends Screen> extends Facet, ApplicationContextAware {

    /**
     * Sets the id of screen to open.
     *
     * @param screenId screen id
     */
    @StudioProperty(type = PropertyType.STRING)
    void setScreenId(@Nullable String screenId);

    /**
     * @return screen id
     */
    @Nullable
    String getScreenId();

    /**
     * Sets class of screen to open.
     *
     * @param screenClass screen class
     */
    @StudioProperty(type = PropertyType.JAVA_CLASS_NAME)
    void setScreenClass(@Nullable Class<S> screenClass);

    /**
     * @return class of screen to open
     */
    @Nullable
    Class<S> getScreenClass();

    /**
     * Sets screen {@link Screens.LaunchMode}.
     *
     * @param launchMode launch mode
     */
    void setLaunchMode(Screens.LaunchMode launchMode);

    /**
     * @return screen {@link Screens.LaunchMode}
     */
    Screens.LaunchMode getLaunchMode();

    /**
     * Sets the given {@code Supplier} as screen options provider.
     *
     * @param optionsProvider screen options provider
     */
    void setOptionsProvider(@Nullable Supplier<ScreenOptions> optionsProvider);

    /**
     * @return {@link ScreenOptions} provider
     */
    @Nullable
    Supplier<ScreenOptions> getOptionsProvider();

    /**
     * Sets properties that will be injected into opened screen via public setters.
     *
     * @param properties screen properties
     */
    void setProperties(Collection<UiControllerProperty> properties);

    /**
     * @return properties that will be injected into opened screen via public setters.
     */
    @Nullable
    Collection<UiControllerProperty> getProperties();

    /**
     * @return id of action that triggers screen
     */
    @Nullable
    String getActionTarget();

    /**
     * Sets that screen should be shown when action with id {@code actionId} is performed.
     *
     * @param actionId action id
     */
    @StudioProperty(type = PropertyType.COMPONENT_REF)
    void setActionTarget(@Nullable String actionId);

    /**
     * @return id of button that triggers screen
     */
    @Nullable
    String getButtonTarget();

    /**
     * Sets that screen should be shown when button with id {@code actionId} is clicked.
     *
     * @param buttonId button id
     */
    @StudioProperty(type = PropertyType.COMPONENT_REF)
    void setButtonTarget(@Nullable String buttonId);

    /**
     * @return new screen instance
     */
    S create();

    /**
     * Shows and returns screen.
     */
    S show();

    /**
     * Adds the given {@code Consumer} as screen after show event listener.
     *
     * @param listener listener
     * @return after show event subscription
     */
    Subscription addAfterShowEventListener(Consumer<AfterShowEvent> listener);

    /**
     * Adds the given {@code Consumer} as screen after close event listener.
     *
     * @param listener listener
     * @return after close event subscription
     */
    Subscription addAfterCloseEventListener(Consumer<AfterCloseEvent> listener);

    /**
     * Event that is fired after screen show.
     */
    class AfterShowEvent extends EventObject {

        protected Screen screen;

        public AfterShowEvent(ScreenFacet source, Screen screen) {
            super(source);
            this.screen = screen;
        }

        @Override
        public ScreenFacet getSource() {
            return (ScreenFacet) super.getSource();
        }

        public Screen getScreen() {
            return screen;
        }
    }

    /**
     * Event that is fired when screen is closed.
     */
    class AfterCloseEvent extends EventObject {

        protected Screen screen;

        public AfterCloseEvent(ScreenFacet source, Screen screen) {
            super(source);
            this.screen = screen;
        }

        @Override
        public ScreenFacet getSource() {
            return (ScreenFacet) super.getSource();
        }

        public Screen getScreen() {
            return screen;
        }
    }
}
