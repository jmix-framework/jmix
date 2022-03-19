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

package io.jmix.ui;

import io.jmix.ui.component.AppWorkArea;
import io.jmix.ui.screen.*;
import io.jmix.ui.util.OperationResult;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Interface defining methods for creation and displaying of UI screens.
 *
 * @see ScreenBuilders
 */
public interface Screens {

    String NAVIGATION_CLOSE_ACTION_ID = "mainMenu";

    /**
     * Constant that is passed to {@link Screen#close(CloseAction)} when the screen is closed by screens manager.
     */
    CloseAction NAVIGATION_CLOSE_ACTION = new StandardCloseAction(NAVIGATION_CLOSE_ACTION_ID);

    /**
     * Creates a screen by its controller class.
     * <p>
     * By default, the screen will be opened in the current tab of the main window ({@link OpenMode#THIS_TAB}).
     *
     * @param screenClass screen controller class
     */
    default <T extends Screen> T create(Class<T> screenClass) {
        return create(screenClass, OpenMode.THIS_TAB, FrameOwner.NO_OPTIONS);
    }

    /**
     * Creates a screen by its controller class.
     *
     * @param screenClass screen controller class
     * @param openMode    how the screen should be opened
     */
    default <T extends Screen> T create(Class<T> screenClass, OpenMode openMode) {
        return create(screenClass, openMode, FrameOwner.NO_OPTIONS);
    }

    /**
     * Creates a screen by its screen id.
     *
     * @param screenId screen id
     * @param openMode how the screen should be opened
     */
    default Screen create(String screenId, OpenMode openMode) {
        return create(screenId, openMode, FrameOwner.NO_OPTIONS);
    }

    /**
     * Creates a screen by its controller class.
     *
     * @param screenClass screen controller class
     * @param openMode    how the screen should be opened
     * @param options     screen parameters
     */
    <T extends Screen> T create(Class<T> screenClass, OpenMode openMode, ScreenOptions options);

    /**
     * Creates a screen by its screen id.
     *
     * @param screenId screen id
     * @param openMode how the screen should be opened
     * @param options  screen parameters
     */
    Screen create(String screenId, OpenMode openMode, ScreenOptions options);

    /**
     * Displays the given screen according to its {@link OpenMode}.
     *
     * @param screen screen
     * @return {@link OperationResult#success()} if screen is shown or otherwise {@link OperationResult#fail()}
     */
    OperationResult show(Screen screen);

    /**
     * Displays the given screen taking into account already opened screens and multipleOpen option.
     *
     * @param screen screen
     * @return operation result
     */
    OperationResult showFromNavigation(Screen screen);

    /**
     * Removes screen from UI and releases all the resources of screen.
     *
     * @param screen screen
     */
    void remove(Screen screen);

    /**
     * Removes all child screens (screens of work area and dialog screens) from the root screen and releases their resources.
     */
    void removeAll();

    /**
     * Check if there are screens that have unsaved changes.
     *
     * @return true if there are screens with unsaved changes
     */
    boolean hasUnsavedChanges();

    /**
     * @return object that provides information about opened screens
     */
    OpenedScreens getOpenedScreens();

    @Nullable
    AppWorkArea getConfiguredWorkAreaOrNull();

    /**
     * Represents single tab / window stack in {@link AppWorkArea}.
     */
    interface WindowStack {
        /**
         * @return screens of the container in descending order, first element is active screen
         * @throws IllegalStateException in case window stack has been closed
         */
        Collection<Screen> getBreadcrumbs();

        /**
         * @return true if either window stack tab is selected or if {@link AppWorkArea.Mode#SINGLE} mode is enabled.
         */
        boolean isSelected();

        /**
         * Select tab in tabbed UI.
         */
        void select();
    }

    /**
     * Provides information about opened screens, does not store state. <br>
     * Each method obtains current info from UI components tree.
     */
    interface OpenedScreens {

        /**
         * @return the root screen of UI
         * @throws IllegalStateException in case there is no root screen in UI
         */
        Screen getRootScreen();

        /**
         * @return the root screen or null
         */
        @Nullable
        Screen getRootScreenOrNull();

        /**
         * @return all opened screens excluding the root screen or empty collection if there is no root screen
         * or root screen does not have {@link AppWorkArea}
         */
        Collection<Screen> getAll();

        /**
         * @return all opened screens excluding the root screen and dialogs or empty collection
         * if there is no root screen or root screen does not have {@link AppWorkArea}
         */
        Collection<Screen> getWorkAreaScreens();

        /**
         * @return top screens from work area tabs and all dialog windows or empty collection if there is no root screen
         * or root screen does not have {@link AppWorkArea}
         */
        Collection<Screen> getActiveScreens();

        /**
         * @return top screens from work area tabs or empty collection if there is no root screen
         * or root screen does not have {@link AppWorkArea}
         */
        Collection<Screen> getActiveWorkAreaScreens();

        /**
         * @return all dialog screens
         */
        Collection<Screen> getDialogScreens();

        /**
         * @return screens of the currently opened tab of work area in descending order (first element is active screen)
         * or empty collection if there is no root screen or root screen does not have {@link AppWorkArea}
         */
        Collection<Screen> getCurrentBreadcrumbs();

        /**
         * @return tab containers or single window container with access to breadcrumbs or empty collection
         * if there is no root screen or root screen does not have {@link AppWorkArea}
         */
        Collection<WindowStack> getWorkAreaStacks();
    }
}