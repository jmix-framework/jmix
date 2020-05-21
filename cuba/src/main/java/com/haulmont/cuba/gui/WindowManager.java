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
package com.haulmont.cuba.gui;

import com.haulmont.cuba.gui.data.Datasource;
import io.jmix.core.Entity;
import io.jmix.ui.*;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.Window;
import io.jmix.ui.gui.OpenType;
import io.jmix.ui.screen.Screen;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

/**
 * Legacy window manager.
 *
 * @deprecated Use {@link Screens}, {@link Dialogs} and {@link Notifications} APIs instead.
 */
@Deprecated
public interface WindowManager {

    /**
     * @deprecated Use {@link Screens#getOpenedScreens()} instead.
     */
    @Deprecated
    Collection<Window> getOpenWindows();

    /**
     * Select tab with window in main tabsheet.
     *
     * @deprecated Use {@link Screens#getOpenedScreens()} and {@code WindowStack#select()} instead TODO: legacy-ui.
     */
    @Deprecated
    void selectWindowTab(Window window);

    /**
     * @deprecated Please use {@link Window#setCaption(String)} ()} and {@link Window#setDescription(String)} ()} methods.
     */
    @Deprecated
    default void setWindowCaption(Window window, String caption, String description) {
        window.setCaption(caption);
        window.setDescription(description);
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Deprecated
    Window openWindow(WindowInfo windowInfo, OpenType openType, Map<String, Object> params);

    @Deprecated
    Window openWindow(WindowInfo windowInfo, OpenType openType);

    @Deprecated
    Window.Editor openEditor(WindowInfo windowInfo, Entity item, OpenType openType,
                             Datasource parentDs);

    @Deprecated
    Window.Editor openEditor(WindowInfo windowInfo, Entity item, OpenType openType);

    @Deprecated
    Window.Editor openEditor(WindowInfo windowInfo, Entity item, OpenType openType, Map<String, Object> params);

    @Deprecated
    Window.Editor openEditor(WindowInfo windowInfo, Entity item,
                             OpenType openType, Map<String, Object> params,
                             Datasource parentDs);

    // used only for legacy screens
    Screen createEditor(WindowInfo windowInfo, Entity item, OpenType openType, Map<String, Object> params);

    @Deprecated
    Window.Lookup openLookup(WindowInfo windowInfo, Window.Lookup.Handler handler,
                             OpenType openType, Map<String, Object> params);

    @Deprecated
    Window.Lookup openLookup(WindowInfo windowInfo, Window.Lookup.Handler handler, OpenType openType);

    @Deprecated
    Frame openFrame(Frame parentFrame, Component parent, WindowInfo windowInfo);

    @Deprecated
    Frame openFrame(Frame parentFrame, Component parent, WindowInfo windowInfo, Map<String, Object> params);

    @Deprecated
    Frame openFrame(Frame parentFrame, Component parent, @Nullable String id,
                    WindowInfo windowInfo, Map<String, Object> params);

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*
    TODO: legacy-ui
    default void close(Window window) {
        remove(window.getFrameOwner());
    }*/

    /**
     * Opens default screen. Implemented only for the web module.
     * <p>
     * Default screen can be defined with the {@code cuba.web.defaultScreenId} application property.
     */
    default void openDefaultScreen() {
        // todo move to ScreenTools bean
    }

    /**
     * Show notification with {@link Frame.NotificationType#HUMANIZED}. <br>
     * Supports line breaks ({@code \n}).
     *
     * @param caption text
     */
    @Deprecated
    void showNotification(String caption);

    /**
     * Show notification. <br>
     * Supports line breaks ({@code \n}).
     *
     * @param caption text
     * @param type    defines how to display the notification.
     *                Don't forget to escape data from the database in case of {@code *_HTML} types!
     */
    @Deprecated
    void showNotification(String caption, Frame.NotificationType type);

    /**
     * Show notification with caption description. <br>
     * Supports line breaks ({@code \n}).
     *
     * @param caption     caption
     * @param description text
     * @param type        defines how to display the notification.
     *                    Don't forget to escape data from the database in case of {@code *_HTML} types!
     */
    @Deprecated
    void showNotification(String caption, String description, Frame.NotificationType type);

    /**
     * Show message dialog with title and message. <br>
     * Supports line breaks ({@code \n}) for non HTML messageType.
     *
     * @param title       dialog title
     * @param message     text
     * @param messageType defines how to display the dialog.
     *                    Don't forget to escape data from the database in case of {@code *_HTML} types!
     */
    @Deprecated
    void showMessageDialog(String title, String message, Frame.MessageType messageType);

    /**
     * Show options dialog with title and message. <br>
     * Supports line breaks ({@code \n}) for non HTML messageType.
     *
     * @param title       dialog title
     * @param message     text
     * @param messageType defines how to display the dialog.
     *                    Don't forget to escape data from the database in case of {@code *_HTML} types!
     * @param actions     available actions
     */
    @Deprecated
    void showOptionDialog(String title, String message, Frame.MessageType messageType, Action[] actions);

    /**
     * Shows exception dialog with default caption, message and displays stacktrace of given throwable.
     *
     * @param throwable throwable
     */
    @Deprecated
    void showExceptionDialog(Throwable throwable);

    /**
     * Shows exception dialog with given caption, message and displays stacktrace of given throwable.
     *
     * @param throwable throwable
     * @param caption   dialog caption
     * @param message   dialog message
     */
    @Deprecated
    void showExceptionDialog(Throwable throwable, @Nullable String caption, @Nullable String message);

    /**
     * Open a web page in browser.
     * <br>
     * It is recommended to use {@link WebBrowserTools} instead.
     *
     * @param url    URL of the page
     * @param params optional parameters.
     *               <br>The following parameters are recognized by Web client:
     *               <ul>
     *               <li>{@code target} - String value used as the target name in a
     *               window.open call in the client. This means that special values such as
     *               "_blank", "_self", "_top", "_parent" have special meaning. If not specified, "_blank" is used.</li>
     *               <li> {@code width} - Integer value specifying the width of the browser window in pixels</li>
     *               <li> {@code height} - Integer value specifying the height of the browser window in pixels</li>
     *               <li> {@code border} - String value specifying the border style of the window of the browser window.
     *               Possible values are "DEFAULT", "MINIMAL", "NONE".</li>
     *               </ul>
     *               Desktop client doesn't support any parameters and just ignores them.
     * @see WebBrowserTools#showWebPage(String, Map)
     */
    @Deprecated
    void showWebPage(String url, @Nullable Map<String, Object> params);
}
