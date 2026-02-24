/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.kit.component.sidedialog;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dialog.Dialog;
import jakarta.annotation.Nullable;

/**
 * The extension of the {@link Dialog} component that functions as a drawer panel. It has a popping-out animation of the
 * dialog and enables configuring the dialog position.
 */
@Tag("jmix-side-dialog")
@JsModule("./src/side-dialog/jmix-side-dialog.js")
public class JmixSideDialogOverlay extends Dialog {

    public JmixSideDialogOverlay() {
        getElement().getNode().addAttachListener(this::onNodeAttach);
    }

    @Nullable
    @Override
    public String getWidth() {
        return getElement().getProperty("horizontalSize");
    }

    @Override
    public void setWidth(@Nullable String value) {
        getElement().setProperty("horizontalSize", value);
    }

    @Nullable
    @Override
    public String getMinWidth() {
        return getElement().getProperty("horizontalMinSize");
    }

    @Override
    public void setMinWidth(@Nullable String value) {
        getElement().setProperty("horizontalMinSize", value);
    }

    @Nullable
    @Override
    public String getMaxWidth() {
        return getElement().getProperty("horizontalMaxSize");
    }

    @Override
    public void setMaxWidth(@Nullable String value) {
        getElement().setProperty("horizontalMaxSize", value);
    }

    @Nullable
    @Override
    public String getHeight() {
        return getElement().getProperty("verticalSize");
    }

    @Override
    public void setHeight(@Nullable String value) {
        getElement().setProperty("verticalSize", value);
    }

    @Nullable
    @Override
    public String getMinHeight() {
        return getElement().getProperty("verticalMinSize");
    }

    @Override
    public void setMinHeight(@Nullable String value) {
        getElement().setProperty("verticalMinSize", value);
    }

    @Nullable
    @Override
    public String getMaxHeight() {
        return getElement().getProperty("verticalMaxSize");
    }

    @Override
    public void setMaxHeight(@Nullable String value) {
        getElement().setProperty("verticalMaxSize", value);
    }

    /**
     * Sets the full width of the dialog when horizontal position is configured ({@link SideDialogPosition#LEFT},
     * {@link SideDialogPosition#RIGHT}, {@link SideDialogPosition#INLINE_START} or
     * {@link SideDialogPosition#INLINE_END}).
     */
    @Override
    public void setWidthFull() {
        super.setWidthFull();

        setMaxWidth("100%");
    }

    /**
     * Sets the height of the dialog when vertical position is configured ({@link SideDialogPosition#TOP},
     * {@link SideDialogPosition#BOTTOM}).
     */
    @Override
    public void setHeightFull() {
        super.setHeightFull();

        setMaxHeight("100%");
    }

    /**
     * @return the dialog position
     */
    public SideDialogPosition getSideDialogPosition() {
        String position = getElement().getProperty("sideDialogPosition");
        if (Strings.isNullOrEmpty(position)) {
            return SideDialogPosition.RIGHT;
        }
        return SideDialogPosition.valueOf(position.toUpperCase().replace("-", "_"));
    }

    /**
     * Sets the dialog position. The default value is {@link SideDialogPosition#RIGHT}.
     *
     * @param position the dialog position
     */
    public void setSideDialogPosition(SideDialogPosition position) {
        getElement().setProperty("sideDialogPosition", position.name().toLowerCase().replace("_", "-"));
    }

    /**
     * @return {@code true} if the dialog should be displayed in fullscreen mode on small devices
     */
    public boolean isFullscreenOnSmallDevices() {
        return getElement().getProperty("fullscreenOnSmallDevices", true);
    }

    /**
     * Sets whether the dialog should be displayed in fullscreen mode on small devices.
     * <p>
     * The default value is {@code true}.
     *
     * @param fullscreenOnSmallDevice fullscreen option
     */
    public void setFullscreenOnSmallDevices(boolean fullscreenOnSmallDevice) {
        getElement().setProperty("fullscreenOnSmallDevices", fullscreenOnSmallDevice);
    }

    protected void onNodeAttach() {
        getElement().callJsFunction("_updateSizes");
    }
}
