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

import com.vaadin.flow.component.*;
import com.vaadin.flow.dom.ClassList;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.Style;
import jakarta.annotation.Nullable;

import java.util.*;

public class JmixSideDialog extends Composite<JmixSideDialogOverlay> implements HasComponents {

    protected DrawerHeader header;
    protected DrawerFooter footer;

    @Override
    public void add(Collection<Component> components) {
        getContent().add(components);
    }

    @Override
    public void addComponentAtIndex(int index, Component component) {
        getContent().addComponentAtIndex(index, component);
    }

    /**
     * @return the content of the dialog
     */
    public List<Component> getContentComponents() {
        return getContent().getChildren().toList();
    }

    /**
     * Returns the width of the dialog when horizontal placement is configured ({@link SideDialogPlacement#LEFT},
     * {@link SideDialogPlacement#RIGHT}, {@link SideDialogPlacement#INLINE_START} or
     * {@link SideDialogPlacement#INLINE_END}).
     * <p>
     * Note that this does not return the actual size of the dialog but the width which has been set using
     * {@link #setHorizontalSize(String)}.
     *
     * @return the width of the dialog or {@code null} if the width is not set
     */
    @Nullable
    public String getHorizontalSize() {
        return getContent().getWidth();
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
     * @param size the width of the dialog or {@code null} to remove the inline width from the style
     */
    public void setHorizontalSize(@Nullable String size) {
        getContent().setWidth(size);
    }

    /**
     * Returns the max-width of the dialog when horizontal placement is configured ({@link SideDialogPlacement#LEFT},
     * {@link SideDialogPlacement#RIGHT}, {@link SideDialogPlacement#INLINE_START} or
     * {@link SideDialogPlacement#INLINE_END}).
     * <p>
     * Note that this does not return the actual size of the dialog but the max-width which has been set using
     * {@link #setHorizontalMaxSize(String)}.
     *
     * @return the max-width of the dialog or {@code null} if the width is not set
     */
    @Nullable
    public String getHorizontalMaxSize() {
        return getContent().getMaxWidth();
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
     * @param maxSize the max-width of the dialog or {@code null} to remove the inline max-width property from the style
     */
    public void setHorizontalMaxSize(@Nullable String maxSize) {
        getContent().setMaxWidth(maxSize);
    }

    /**
     * Returns the min-width of the dialog when horizontal placement is configured ({@link SideDialogPlacement#LEFT},
     * {@link SideDialogPlacement#RIGHT}, {@link SideDialogPlacement#INLINE_START} or
     * {@link SideDialogPlacement#INLINE_END}).
     * <p>
     * Note that this does not return the actual size of the dialog but the min-width which has been set using
     * {@link #setHorizontalMinSize(String)}.
     *
     * @return the min-width of the dialog or {@code null} if the min-width is not set
     */
    @Nullable
    public String getHorizontalMinSize() {
        return getContent().getMinWidth();
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
     * @param minSize the min-width of the dialog or {@code null} to remove the inline min-width property from the style
     */
    public void setHorizontalMinSize(@Nullable String minSize) {
        getContent().setMinWidth(minSize);
    }

    /**
     * Returns the height of the dialog when vertical placement is configured ({@link SideDialogPlacement#TOP},
     * {@link SideDialogPlacement#BOTTOM}).
     * <p>
     * Note that this does not return the actual size of the dialog but the height which has been set using
     * {@link #setVerticalSize(String)}.
     *
     * @return the height of the dialog or {@code null} if the height is not set
     */
    @Nullable
    public String getVerticalSize() {
        return getContent().getHeight();
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
     * @param size the height of the dialog or {@code null} to remove the inline height property from the style
     */
    public void setVerticalSize(@Nullable String size) {
        getContent().setHeight(size);
    }

    /**
     * Returns the max-height of the dialog when vertical placement is configured ({@link SideDialogPlacement#TOP},
     * {@link SideDialogPlacement#BOTTOM}).
     * <p>
     * Note that this does not return the actual size of the side panel but the max-height which has been set using
     * {@link #setVerticalMaxSize(String)}.
     *
     * @return the max-height of the dialog or {@code null} if the max-height is not set
     */
    @Nullable
    public String getVerticalMaxSize() {
        return getContent().getMaxHeight();
    }

    /**
     * Sets the max-height of the dialog when vertical placement is configured ({@link SideDialogPlacement#TOP},
     * {@link SideDialogPlacement#BOTTOM}).
     * <p>
     * The max-height should be in a format understood by the browser, e.g. "100px" or "2.5em".
     * <p>
     * The default max-height is taken from the theme variable {@code --jmix-side-dialog-vertical-max-size}. If it is
     * not set, the default value is {@code 50vh}.
     *
     * @param maxSize the max-height of the dialog or {@code null} to remove the inline max-height property from style
     */
    public void setVerticalMaxSize(@Nullable String maxSize) {
        getContent().setMaxHeight(maxSize);
    }

    /**
     * Returns the min-height of the dialog when vertical placement is configured ({@link SideDialogPlacement#TOP},
     * {@link SideDialogPlacement#BOTTOM}).
     * <p>
     * Note that this does not return the actual size of the side panel but the min-height which has been set using
     * {@link #setVerticalMinSize(String)}.
     *
     * @return the min-height of the dialog or {@code null} if the min-height is not set
     */
    @Nullable
    public String getVerticalMinSize() {
        return getContent().getMinHeight();
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
     * @param minSize the min-height of the dialog or {@code null} to remove the inline min-height property from style
     */
    public void setVerticalMinSize(@Nullable String minSize) {
        getContent().setMinHeight(minSize);
    }

    @Override
    public void setClassName(String className) {
        getContent().addClassNames(className);
    }

    @Override
    public ClassList getClassNames() {
        return getContent().getClassNames();
    }

    @Override
    public Style getStyle() {
        throw new UnsupportedOperationException(
                JmixSideDialog.class.getSimpleName() + " does not support adding styles using this method");
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

    /**
     * @return the side dialog placement
     */
    public SideDialogPlacement getSideDialogPlacement() {
        return getContent().getSideDialogPlacement();
    }

    /**
     * Sets the side dialog placement.
     * <p>
     * The default value is {@link SideDialogPlacement#RIGHT}.
     *
     * @param placement the side dialog placement
     */
    public void setSideDialogPlacement(SideDialogPlacement placement) {
        getContent().setSideDialogPlacement(placement);
    }

    /**
     * @return {@code true} if the dialog is opened, {@code false} otherwise
     */
    public boolean isOpened() {
        return getContent().isOpened();
    }

    /**
     * Opens the dialog.
     */
    public void open() {
        getContent().open();
    }

    /**
     * Closes the dialog.
     */
    public void close() {
        getContent().close();
    }

    // TODO: pinyazhin, replace by getModality() after upgrading Vaadin 25
    public boolean isModal() {
        return getContent().isModal();
    }

    // TODO: pinyazhin, replace by setModality() after upgrading Vaadin 25
    public void setModal(boolean modal) {
        getContent().setModal(modal);
    }

    /**
     * @return {@code true} if the dialog should be closed when hitting the ESC key
     */
    public boolean isCloseOnEsc() {
        return getContent().isCloseOnEsc();
    }

    /**
     * Sets whether this dialog can be closed by hitting the ESC key or not.
     * <p>
     * The default value is {@code true}.
     *
     * @param closeOnEsc closeOnEsc option
     */
    public void setCloseOnEsc(boolean closeOnEsc) {
        getContent().setCloseOnEsc(closeOnEsc);
    }

    /**
     * @return {@code true} if the dialog should be closed when clicking outside it
     */
    public boolean isCloseOnOutsideClick() {
        return getContent().isCloseOnOutsideClick();
    }

    /**
     * Sets whether this dialog can be closed by clicking outside it or not.
     * <p>
     * The default value is {@code true}.
     *
     * @param closeOnOutsideClick closeOnOutsideClick option
     */
    public void setCloseOnOutsideClick(boolean closeOnOutsideClick) {
        getContent().setCloseOnOutsideClick(closeOnOutsideClick);
    }

    /**
     * @return the title to be rendered on the dialog header or empty string if not set
     */
    public String getHeaderTitle() {
        return getContent().getHeaderTitle();
    }

    /**
     * Sets the title to be rendered on the dialog header.
     *
     * @param title the title to set or {@code null} to remove the header title
     */
    public void setHeaderTitle(@Nullable String title) {
        getContent().setHeaderTitle(title);
    }

    // TODO: pinyazhin, replace by setRole after upgrading Vaadin 25
    public String getOverlayRole() {
        return getContent().getOverlayRole();
    }

    // TODO: pinyazhin, replace by setRole after upgrading Vaadin 25
    public void setOverlayRole(String role) {
        getContent().setOverlayRole(role);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        getContent().setVisible(visible);
    }

    /**
     * @return the header of the dialog
     */
    public DrawerHeader getHeader() {
        if (header == null) {
            header = new DrawerHeader();
        }
        return header;
    }

    /**
     * @return the footer of the dialog
     */
    public DrawerFooter getFooter() {
        if (footer == null) {
            footer = new DrawerFooter();
        }
        return footer;
    }

    /**
     * Represents the header section of the side dialog.
     */
    public class DrawerHeader extends AbstractDrawerHeaderFooter {

        @Override
        protected HasComponents getHeaderFooter() {
            return getContent().getHeader();
        }
    }

    /**
     * Represents the footer section of the side dialog.
     */
    public class DrawerFooter extends AbstractDrawerHeaderFooter {
        @Override
        protected HasComponents getHeaderFooter() {
            return getContent().getFooter();
        }
    }

    protected abstract class AbstractDrawerHeaderFooter implements HasComponents {

        protected List<Component> components;

        @Override
        public void add(Component... components) {
            components().addAll(Arrays.asList(components));

            getHeaderFooter().add(components);
        }

        @Override
        public void add(Collection<Component> components) {
            components().addAll(components);

            getHeaderFooter().add(components);
        }

        @Override
        public void add(String text) {
            Text textComponent = new Text(text);

            components().add(textComponent);

            getHeaderFooter().add(textComponent);
        }

        @Override
        public void remove(Component... components) {
            components().removeAll(Arrays.asList(components));

            getHeaderFooter().remove(components);
        }

        @Override
        public void remove(Collection<Component> components) {
            components().removeAll(components);

            getHeaderFooter().remove(components);
        }

        @Override
        public void removeAll() {
            components().clear();

            getHeaderFooter().removeAll();
        }

        @Override
        public void addComponentAtIndex(int index, Component component) {
            components().add(index, component);

            getHeaderFooter().addComponentAtIndex(index, component);
        }

        @Override
        public void addComponentAsFirst(Component component) {
            components().add(0, component);

            getHeaderFooter().addComponentAsFirst(component);
        }

        public List<Component> getComponents() {
            return components == null ? Collections.emptyList() : List.copyOf(components);
        }

        @Override
        public Element getElement() {
            return getContent().getHeader().getElement();
        }

        protected abstract HasComponents getHeaderFooter();

        protected List<Component> components() {
            if (components == null) {
                this.components = new ArrayList<>();
            }
            return components;
        }
    }
}
