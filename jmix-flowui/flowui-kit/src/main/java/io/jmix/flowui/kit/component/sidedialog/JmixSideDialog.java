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
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.shared.Registration;
import jakarta.annotation.Nullable;

/**
 * The extension of the {@link Dialog} component that functions as a drawer panel. It has a popping-out animation of the
 * dialog and enables configuring the dialog placement.
 */
@Tag("jmix-side-dialog")
@JsModule("./src/side-dialog/jmix-side-dialog.js")
public class JmixSideDialog extends Dialog {

    /**
     * Returns the width of the dialog when horizontal placement is configured ({@link SideDialogPlacement#LEFT},
     * {@link SideDialogPlacement#RIGHT}, {@link SideDialogPlacement#INLINE_START} or
     * {@link SideDialogPlacement#INLINE_END}).
     *
     * @return the width of the dialog or {@code null} if the width is not set
     */
    @Nullable
    @Override
    public String getWidth() {
        return getElement().getProperty("horizontalSize");
    }

    /**
     * Sets the width of the dialog when horizontal placement is configured ({@link SideDialogPlacement#LEFT},
     * {@link SideDialogPlacement#RIGHT}, {@link SideDialogPlacement#INLINE_START} or
     * {@link SideDialogPlacement#INLINE_END}).
     * <p>
     * The width should be in a format understood by the browser, e.g. "100px" or "2.5em".
     * <p>
     * The default width is taken from the theme variable {@code --jmix-side-dialog-horizontal-size}. If it is not set,
     * the default value is {@code auto}.
     *
     * @param value the width of the dialog or {@code null} to set default width
     */
    @Override
    public void setWidth(@Nullable String value) {
        getElement().setProperty("horizontalSize", value);
    }

    /**
     * Returns the min-width of the dialog when horizontal placement is configured ({@link SideDialogPlacement#LEFT},
     * {@link SideDialogPlacement#RIGHT}, {@link SideDialogPlacement#INLINE_START} or
     * {@link SideDialogPlacement#INLINE_END}).
     *
     * @return the min-width of the dialog or {@code null} if the width is not set
     */
    @Nullable
    @Override
    public String getMinWidth() {
        return getElement().getProperty("horizontalMinSize");
    }

    /**
     * Sets the min-width of the dialog when horizontal placement is configured ({@link SideDialogPlacement#LEFT},
     * {@link SideDialogPlacement#RIGHT}, {@link SideDialogPlacement#INLINE_START} or
     * {@link SideDialogPlacement#INLINE_END}).
     * <p>
     * The min-width should be in a format understood by the browser, e.g. "100px" or "2.5em".
     * <p>
     * The default min-width is taken from the theme variable {@code --jmix-side-dialog-horizontal-min-size}. If it is
     * not set, the default value is {@code 16em}.
     *
     * @param value the min-width of the dialog or {@code null} to set default min-width
     */
    @Override
    public void setMinWidth(@Nullable String value) {
        getElement().setProperty("horizontalMinSize", value);
    }

    /**
     * Returns the max-width of the dialog when horizontal placement is configured ({@link SideDialogPlacement#LEFT},
     * {@link SideDialogPlacement#RIGHT}, {@link SideDialogPlacement#INLINE_START} or
     * {@link SideDialogPlacement#INLINE_END}).
     *
     * @return the max-width of the dialog or {@code null} if the width is not set
     */
    @Nullable
    @Override
    public String getMaxWidth() {
        return getElement().getProperty("horizontalMaxSize");
    }

    /**
     * Sets the max-width of the dialog when horizontal placement is configured ({@link SideDialogPlacement#LEFT},
     * {@link SideDialogPlacement#RIGHT}, {@link SideDialogPlacement#INLINE_START} or
     * {@link SideDialogPlacement#INLINE_END}).
     * <p>
     * The width should be in a format understood by the browser, e.g. "100px" or "2.5em".
     * <p>
     * The default max-width is taken from the theme variable {@code --jmix-side-dialog-horizontal-max-size}. If it is
     * not set, the default value is {@code 50%}.
     *
     * @param value the max-width of the dialog or {@code null} to set default max-width
     */
    @Override
    public void setMaxWidth(@Nullable String value) {
        getElement().setProperty("horizontalMaxSize", value);
    }

    /**
     * Returns the height of the dialog when vertical placement is configured ({@link SideDialogPlacement#TOP},
     * {@link SideDialogPlacement#BOTTOM}).
     *
     * @return the height of the dialog or {@code null} if the height is not set
     */
    @Nullable
    @Override
    public String getHeight() {
        return getElement().getProperty("verticalSize");
    }

    /**
     * Sets the height of the dialog when vertical placement is configured ({@link SideDialogPlacement#TOP},
     * {@link SideDialogPlacement#BOTTOM}).
     * <p>
     * The height should be in a format understood by the browser, e.g. "100px" or "2.5em".
     * <p>
     * The default height is taken from the theme variable {@code --jmix-side-dialog-vertical-size}. If it is
     * not set, the default value is {@code auto}.
     *
     * @param value the height of the dialog or {@code null} to set default height
     */
    @Override
    public void setHeight(@Nullable String value) {
        getElement().setProperty("verticalSize", value);
    }

    /**
     * Returns the min-height of the dialog when vertical placement is configured ({@link SideDialogPlacement#TOP},
     * {@link SideDialogPlacement#BOTTOM}).
     *
     * @return the min-height of the dialog or {@code null} if the min-height is not set
     */
    @Nullable
    @Override
    public String getMinHeight() {
        return getElement().getProperty("verticalMinSize");
    }

    /**
     * Sets the min-height of the dialog when vertical placement is configured ({@link SideDialogPlacement#TOP},
     * {@link SideDialogPlacement#BOTTOM}).
     * <p>
     * The min-height should be in a format understood by the browser, e.g. "100px" or "2.5em".
     * <p>
     * The default min-height is taken from the theme variable {@code --jmix-side-dialog-vertical-min-size}. If it is
     * not set, the default value is {@code 16em}.
     *
     * @param value the min-height of the dialog or {@code null} to set default min-height
     */
    @Override
    public void setMinHeight(@Nullable String value) {
        getElement().setProperty("verticalMinSize", value);
    }

    /**
     * Returns the max-height of the dialog when vertical placement is configured ({@link SideDialogPlacement#TOP},
     * {@link SideDialogPlacement#BOTTOM}).
     *
     * @return the max-height of the dialog or {@code null} if the max-height is not set
     */
    @Nullable
    @Override
    public String getMaxHeight() {
        return getElement().getProperty("verticalMaxSize");
    }

    /**
     * Sets the max-height of the dialog when vertical placement is configured ({@link SideDialogPlacement#TOP},
     * {@link SideDialogPlacement#BOTTOM}).
     * <p>
     * The max-height should be in a format understood by the browser, e.g. "100px" or "2.5em".
     * <p>
     * The default max-height is taken from the theme variable {@code --jmix-side-dialog-vertical-max-size}. If it is
     * not set, the default value is {@code 50%}.
     *
     * @param value the max-height of the dialog or {@code null} to set default max-height
     */
    @Override
    public void setMaxHeight(@Nullable String value) {
        getElement().setProperty("verticalMaxSize", value);
    }

    /**
     * Sets the full width of the dialog when horizontal placement is configured ({@link SideDialogPlacement#LEFT},
     * {@link SideDialogPlacement#RIGHT}, {@link SideDialogPlacement#INLINE_START} or
     * {@link SideDialogPlacement#INLINE_END}).
     */
    @Override
    public void setWidthFull() {
        super.setWidthFull();

        setMaxWidth("100%");
    }

    /**
     * Sets the height of the dialog when vertical placement is configured ({@link SideDialogPlacement#TOP},
     * {@link SideDialogPlacement#BOTTOM}).
     */
    @Override
    public void setHeightFull() {
        super.setHeightFull();

        setMaxHeight("100%");
    }

    /**
     * Sets defaults to vertical and horizontal sizes.
     */
    @Override
    public void setSizeUndefined() {
        super.setSizeUndefined();
    }

    @Override
    public void setDraggable(boolean draggable) {
        throw new UnsupportedOperationException("Draggable is not supported for side dialog");
    }

    @Override
    public void setResizable(boolean resizable) {
        throw new UnsupportedOperationException("Resizable is not supported for side dialog");
    }

    @Override
    public void setTop(String top) {
        throw new UnsupportedOperationException("Top position is not supported for side dialog, use setSideDialogPlacement() instead");
    }

    @Override
    public void setLeft(String left) {
        throw new UnsupportedOperationException("Left position is not supported for side dialog, use setSideDialogPlacement() instead");
    }

    @Override
    public Registration addResizeListener(ComponentEventListener<DialogResizeEvent> listener) {
        throw new UnsupportedOperationException("Resize listener is not supported for side dialog");
    }

    @Override
    public Registration addDraggedListener(ComponentEventListener<DialogDraggedEvent> listener) {
        throw new UnsupportedOperationException("Dragged listener is not supported for side dialog");
    }

    /**
     * @return the dialog placement
     */
    public SideDialogPlacement getSideDialogPlacement() {
        String placement = getElement().getProperty("sideDialogPlacement");
        if (Strings.isNullOrEmpty(placement)) {
            return SideDialogPlacement.RIGHT;
        }
        return SideDialogPlacement.valueOf(placement.toUpperCase().replace("-", "_"));
    }

    /**
     * Sets the dialog placement. The default value is {@link SideDialogPlacement#RIGHT}.
     *
     * @param placement the dialog placement
     */
    public void setSideDialogPlacement(SideDialogPlacement placement) {
        getElement().setProperty("sideDialogPlacement", placement.name().toLowerCase().replace("_", "-"));
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
}
