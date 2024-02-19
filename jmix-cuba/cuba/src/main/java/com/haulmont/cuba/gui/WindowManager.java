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

import com.haulmont.cuba.gui.components.Frame.MessageType;
import com.haulmont.cuba.gui.components.Frame.NotificationType;
import com.haulmont.cuba.gui.data.Datasource;
import io.jmix.core.Entity;
import io.jmix.core.common.util.Preconditions;
import io.jmix.ui.Notifications;
import io.jmix.ui.WebBrowserTools;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.*;
import io.jmix.ui.screen.OpenMode;
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
    com.haulmont.cuba.gui.components.Window openWindow(WindowInfo windowInfo,
                                                       OpenType openType,
                                                       Map<String, Object> params);

    @Deprecated
    com.haulmont.cuba.gui.components.Window openWindow(WindowInfo windowInfo,
                                                       OpenType openType);

    @Deprecated
    com.haulmont.cuba.gui.components.Window.Editor openEditor(WindowInfo windowInfo, Entity item, OpenType openType,
                                                              Datasource parentDs);

    @Deprecated
    com.haulmont.cuba.gui.components.Window.Editor openEditor(WindowInfo windowInfo, Entity item, OpenType openType);

    @Deprecated
    com.haulmont.cuba.gui.components.Window.Editor openEditor(WindowInfo windowInfo, Entity item, OpenType openType,
                                                              Map<String, Object> params);

    @Deprecated
    com.haulmont.cuba.gui.components.Window.Editor openEditor(WindowInfo windowInfo, Entity item,
                                                              OpenType openType, Map<String, Object> params,
                                                              Datasource parentDs);

    // used only for legacy screens
    Screen createEditor(WindowInfo windowInfo, Entity item, OpenType openType, Map<String, Object> params);

    @Deprecated
    com.haulmont.cuba.gui.components.Window.Lookup openLookup(WindowInfo windowInfo,
                                                              com.haulmont.cuba.gui.components.Window.Lookup.Handler handler,
                                                              OpenType openType,
                                                              Map<String, Object> params);

    @Deprecated
    com.haulmont.cuba.gui.components.Window.Lookup openLookup(WindowInfo windowInfo,
                                                              com.haulmont.cuba.gui.components.Window.Lookup.Handler handler,
                                                              OpenType openType);

    @Deprecated
    Frame openFrame(Frame parentFrame, Component parent, WindowInfo windowInfo);

    @Deprecated
    Frame openFrame(Frame parentFrame, Component parent, WindowInfo windowInfo, Map<String, Object> params);

    @Deprecated
    Frame openFrame(Frame parentFrame, Component parent, @Nullable String id,
                    WindowInfo windowInfo, Map<String, Object> params);

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Closes a given window.
     *
     * @param window a window to close
     */
    @Deprecated
    void close(Window window);

    /**
     * Opens default screen. Implemented only for the web module.
     * <p>
     * Default screen can be defined with the {@code cuba.web.defaultScreenId} application property.
     */
    default void openDefaultScreen() {
        // todo move to ScreenTools bean
    }

    /**
     * Show notification with {@link NotificationType#HUMANIZED}. <br>
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
     * @deprecated Use {@link Notifications} instead
     */
    @Deprecated
    void showNotification(String caption, NotificationType type);

    /**
     * Show notification with caption description. <br>
     * Supports line breaks ({@code \n}).
     *
     * @param caption     caption
     * @param description text
     * @param type        defines how to display the notification.
     *                    Don't forget to escape data from the database in case of {@code *_HTML} types!
     * @deprecated Use {@link Notifications} instead
     */
    @Deprecated
    void showNotification(String caption, String description, NotificationType type);

    /**
     * Show message dialog with title and message. <br>
     * Supports line breaks ({@code \n}) for non HTML messageType.
     *
     * @param title       dialog title
     * @param message     text
     * @param messageType defines how to display the dialog.
     *                    Don't forget to escape data from the database in case of {@code *_HTML} types!
     * @deprecated Use {@link io.jmix.ui.Dialogs#createMessageDialog()} instead
     */
    @Deprecated
    void showMessageDialog(String title, String message, MessageType messageType);

    /**
     * Show options dialog with title and message. <br>
     * Supports line breaks ({@code \n}) for non HTML messageType.
     *
     * @param title       dialog title
     * @param message     text
     * @param messageType defines how to display the dialog.
     *                    Don't forget to escape data from the database in case of {@code *_HTML} types!
     * @param actions     available actions
     * @deprecated Use {@link io.jmix.ui.Dialogs#createMessageDialog()} instead
     */
    @Deprecated
    void showOptionDialog(String title, String message, MessageType messageType, Action[] actions);

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

    /**
     * How to open a screen: {@link #NEW_TAB}, {@link #THIS_TAB}, {@link #DIALOG}, {@link #NEW_WINDOW}.
     * <p>
     * You can set additional parameters for window using builder style methods:
     * <pre>
     * openEditor("sales$Customer.edit", customer,
     *            OpenType.DIALOG.width(300).resizable(false), params);
     * </pre>
     *
     * @deprecated use {@link OpenMode} instead
     */
    @Deprecated
    final class OpenType {

        /**
         * Open a screen in new tab of the main window.
         * <br> In Web Client with {@code AppWindow.Mode.SINGLE} the new screen replaces current screen.
         */
        public static final OpenType NEW_TAB = new OpenType(OpenMode.NEW_TAB, false);

        /**
         * Open a screen on top of the current tab screens stack.
         */
        public static final OpenType THIS_TAB = new OpenType(OpenMode.THIS_TAB, false);

        /**
         * Open a screen as modal dialog.
         */
        public static final OpenType DIALOG = new OpenType(OpenMode.DIALOG, false);

        /**
         * In Desktop Client open a screen in new main window, in Web Client the same as new {@link #NEW_TAB}
         */
        @Deprecated
        public static final OpenType NEW_WINDOW = new OpenType(OpenMode.NEW_WINDOW, false);

        private OpenMode openMode;
        private boolean mutable = true;

        private Float width;
        private SizeUnit widthUnit;
        private Float height;
        private SizeUnit heightUnit;

        private Integer positionX;
        private Integer positionY;

        private Boolean resizable;
        private Boolean closeable;
        private Boolean modal;
        private Boolean closeOnClickOutside;
        private Boolean maximized;

        public OpenType(OpenMode openMode) {
            this.openMode = openMode;
        }

        private OpenType(OpenMode openMode, boolean mutable) {
            this.openMode = openMode;
            this.mutable = mutable;
        }

        public OpenMode getOpenMode() {
            return openMode;
        }

        public OpenType setOpenMode(OpenMode openMode) {
            OpenType instance = getMutableInstance();

            instance.openMode = openMode;
            return instance;
        }

        @Nullable
        public SizeUnit getHeightUnit() {
            return heightUnit;
        }

        public OpenType setHeightUnit(SizeUnit heightUnit) {
            OpenType instance = getMutableInstance();
            instance.heightUnit = heightUnit;
            return instance;
        }

        @Nullable
        public Float getHeight() {
            return height;
        }

        /**
         * @deprecated Use {@link #height(Float)} instead.
         */
        @Deprecated
        public OpenType height(Integer height) {
            return height(height.floatValue());
        }

        /**
         * @deprecated Use {@link #setHeight(Float)} instead.
         */
        @Deprecated
        public OpenType setHeight(Integer height) {
            return setHeight(height.floatValue());
        }

        public OpenType height(Float height) {
            OpenType instance = getMutableInstance();

            instance.height = height;
            return instance;
        }

        public OpenType setHeight(Float height) {
            OpenType instance = getMutableInstance();

            instance.height = height;
            return instance;
        }

        public OpenType height(String height) {
            return setHeight(height);
        }

        public OpenType setHeight(String height) {
            OpenType instance = getMutableInstance();

            SizeWithUnit size = SizeWithUnit.parseStringSize(height);

            instance.height = size.getSize();
            instance.heightUnit = size.getUnit();
            return instance;
        }

        public OpenType heightAuto() {
            OpenType instance = getMutableInstance();

            instance.height = -1.0f;
            instance.heightUnit = SizeUnit.PIXELS;
            return instance;
        }

        @Nullable
        public String getHeightString() {
            if (height == null) {
                return null;
            }

            return height + (heightUnit != null ? heightUnit.getSymbol() : "px");
        }

        @Nullable
        public SizeUnit getWidthUnit() {
            return widthUnit;
        }

        public OpenType setWidthUnit(SizeUnit widthUnit) {
            OpenType instance = getMutableInstance();
            instance.widthUnit = widthUnit;
            return instance;
        }

        @Nullable
        public Float getWidth() {
            return width;
        }

        @Nullable
        public String getWidthString() {
            if (width == null) {
                return null;
            }

            return width + (widthUnit != null ? widthUnit.getSymbol() : "px");
        }

        /**
         * @deprecated Use {@link #width(Float)} instead.
         */
        @Deprecated
        public OpenType width(Integer width) {
            return width(width.floatValue());
        }

        /**
         * @deprecated Use {@link #setWidth(Float)} instead.
         */
        @Deprecated
        public OpenType setWidth(Integer width) {
            return setWidth(width.floatValue());
        }

        public OpenType width(Float width) {
            OpenType instance = getMutableInstance();

            instance.width = width;
            return instance;
        }

        public OpenType setWidth(Float width) {
            OpenType instance = getMutableInstance();

            instance.width = width;
            return instance;
        }

        public OpenType width(String width) {
            return setWidth(width);
        }

        public OpenType setWidth(String width) {
            OpenType instance = getMutableInstance();

            SizeWithUnit size = SizeWithUnit.parseStringSize(width);

            instance.width = size.getSize();
            instance.widthUnit = size.getUnit();
            return instance;
        }

        public OpenType widthAuto() {
            OpenType instance = getMutableInstance();

            instance.width = -1.0f;
            instance.widthUnit = SizeUnit.PIXELS;
            return instance;
        }

        public Integer getPositionX() {
            return positionX;
        }

        public OpenType setPositionX(Integer positionX) {
            OpenType instance = getMutableInstance();

            instance.positionX = positionX;
            return instance;
        }

        public OpenType positionX(Integer positionX) {
            OpenType instance = getMutableInstance();

            instance.positionX = positionX;
            return instance;
        }

        public Integer getPositionY() {
            return positionY;
        }

        public OpenType setPositionY(Integer positionY) {
            OpenType instance = getMutableInstance();

            instance.positionY = positionY;
            return instance;
        }

        public OpenType positionY(Integer positionY) {
            OpenType instance = getMutableInstance();

            instance.positionY = positionY;
            return instance;
        }

        public OpenType center() {
            OpenType instance = getMutableInstance();
            instance.positionX = null;
            instance.positionY = null;
            return instance;
        }

        @Nullable
        public Boolean getResizable() {
            return resizable;
        }

        public OpenType setResizable(Boolean resizable) {
            OpenType instance = getMutableInstance();

            instance.resizable = resizable;
            return instance;
        }

        public OpenType resizable(Boolean resizable) {
            OpenType instance = getMutableInstance();

            instance.resizable = resizable;
            return instance;
        }

        @Nullable
        public Boolean getCloseable() {
            return closeable;
        }

        public OpenType closeable(Boolean closeable) {
            OpenType instance = getMutableInstance();

            instance.closeable = closeable;
            return instance;
        }

        public OpenType setCloseable(Boolean closeable) {
            OpenType instance = getMutableInstance();

            instance.closeable = closeable;
            return instance;
        }

        @Nullable
        public Boolean getModal() {
            return modal;
        }

        public OpenType modal(Boolean modal) {
            OpenType instance = getMutableInstance();

            instance.modal = modal;
            return instance;
        }

        public OpenType setModal(Boolean modal) {
            OpenType instance = getMutableInstance();

            instance.modal = modal;
            return instance;
        }

        /**
         * @return true if a window can be closed by click on outside window area
         */
        @Nullable
        public Boolean getCloseOnClickOutside() {
            return closeOnClickOutside;
        }

        /**
         * Set closeOnClickOutside to true if a window should be closed by click on outside window area.
         * It works when a window has a modal mode.
         */
        public OpenType closeOnClickOutside(Boolean closeOnClickOutside) {
            OpenType instance = getMutableInstance();

            instance.closeOnClickOutside = closeOnClickOutside;
            return instance;
        }

        /**
         * Set closeOnClickOutside to true if a window should be closed by click on outside window area.
         * It works when a window has a modal mode.
         */
        public OpenType setCloseOnClickOutside(Boolean closeOnClickOutside) {
            OpenType instance = getMutableInstance();

            instance.closeOnClickOutside = closeOnClickOutside;
            return instance;
        }

        /**
         * @return true if a window is maximized across the screen.
         */
        @Nullable
        public Boolean getMaximized() {
            return maximized;
        }

        /**
         * Set maximized to true if a window should be maximized across the screen.
         */
        public OpenType maximized(Boolean maximized) {
            OpenType instance = getMutableInstance();

            instance.maximized = maximized;
            return instance;
        }

        /**
         * Set maximized to true if a window should be maximized across the screen.
         */
        public OpenType setMaximized(Boolean maximized) {
            OpenType instance = getMutableInstance();

            instance.maximized = maximized;
            return instance;
        }

        private OpenType getMutableInstance() {
            if (!mutable) {
                return copy();
            }

            return this;
        }

        public static OpenType valueOf(String openTypeString) {
            Preconditions.checkNotNullArgument(openTypeString, "openTypeString should not be null");

            switch (openTypeString) {
                case "NEW_TAB":
                    return NEW_TAB;

                case "THIS_TAB":
                    return THIS_TAB;

                case "DIALOG":
                    return DIALOG;

                case "NEW_WINDOW":
                    return NEW_WINDOW;

                default:
                    throw new IllegalArgumentException("Unable to parse OpenType");
            }
        }

        public OpenType copy() {
            OpenType openType = new OpenType(openMode);

            openType.setModal(modal);
            openType.setResizable(resizable);
            openType.setCloseable(closeable);
            openType.setHeight(height);
            openType.setHeightUnit(heightUnit);
            openType.setWidth(width);
            openType.setWidthUnit(widthUnit);
            openType.setCloseOnClickOutside(closeOnClickOutside);
            openType.setMaximized(maximized);
            openType.setPositionX(positionX);
            openType.setPositionY(positionY);

            return openType;
        }
    }
}
