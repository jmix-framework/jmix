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
import io.jmix.ui.meta.PropertiesConstraint;
import io.jmix.ui.meta.PropertiesGroup;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElementsGroup;
import io.jmix.ui.meta.StudioFacet;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;
import io.jmix.ui.screen.Install;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenOptions;
import io.jmix.ui.sys.UiControllerProperty;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;
import java.util.Collection;
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
        category = "Facets",
        icon = "io/jmix/ui/icon/facet/screen.svg",
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/facets/screen-facet.html"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "id", type = PropertyType.COMPONENT_ID, required = true)
        },
        groups = {
                @PropertiesGroup(properties = {"screenId", "screenClass"}, constraint = PropertiesConstraint.ONE_OF)
        }
)
public interface ScreenFacet<S extends Screen> extends Facet, ApplicationContextAware {

    /**
     * Sets the id of screen to open.
     *
     * @param screenId screen id
     */
    @StudioProperty(type = PropertyType.SCREEN_ID, typeParameter = "S")
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
    @StudioProperty(type = PropertyType.SCREEN_CLASS_NAME, typeParameter = "S")
    void setScreenClass(@Nullable Class<S> screenClass);

    /**
     * @return class of screen to open
     */
    @Nullable
    Class<S> getScreenClass();

    /**
     * @return screen {@link OpenMode}
     */
    OpenMode getOpenMode();

    /**
     * Sets screen {@link OpenMode}.
     *
     * @param openMode an open mode to set
     */
    @StudioProperty(name = "openMode", type = PropertyType.SCREEN_OPEN_MODE, defaultValue = "NEW_TAB")
    void setOpenMode(OpenMode openMode);

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
     * @return the screen configurer or {@code null} of not set
     */
    @Nullable
    Consumer<S> getScreenConfigurer();

    /**
     * Sets the screen configurer. Use the configurer if you need to
     * provide parameters to the opened screen through setters.
     *
     * The preferred way to set the configurer is using a controller
     * method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "userBrowseFacet", subject = "screenConfigurer")
     * private void userBrowseFacetScreenConfigurer(UserBrowse userBrowse) {
     *     userBrowse.setSomeParameter(someValue);
     * }
     * </pre>
     *
     * @param screenConfigurer the configurer to set.
     */
    void setScreenConfigurer(Consumer<S> screenConfigurer);

    /**
     * Sets properties that will be injected into opened screen via public setters.
     *
     * @param properties screen properties
     */
    @StudioElementsGroup(caption = "Properties",
            xmlElement = "properties",
            icon = "io/jmix/ui/icon/element/properties.svg")
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
    @StudioProperty(name = "onAction", type = PropertyType.COMPONENT_REF,
            options = "io.jmix.ui.action.Action")
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
    @StudioProperty(name = "onButton", type = PropertyType.COMPONENT_REF,
            options = "io.jmix.ui.component.Button")
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
    Subscription addAfterShowEventListener(Consumer<Screen.AfterShowEvent> listener);

    /**
     * Adds the given {@code Consumer} as screen after close event listener.
     *
     * @param listener listener
     * @return after close event subscription
     */
    Subscription addAfterCloseEventListener(Consumer<Screen.AfterCloseEvent> listener);
}
