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

package io.jmix.flowui.kit.component.sidepanellayout;

import com.google.common.base.Strings;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementUtil;
import com.vaadin.flow.dom.PropertyChangeEvent;
import com.vaadin.flow.shared.Registration;
import jakarta.annotation.Nullable;

@Tag("jmix-side-panel-layout")
@JsModule("./src/side-panel-layout/jmix-side-panel-layout.js")
public class JmixDrawerLayout extends Component implements HasSize, HasStyle {

    protected Component content;
    protected Component drawerContent;

    protected ComponentInertManager componentInertManager;

    public JmixDrawerLayout() {
        // Workaround for: https://github.com/vaadin/flow/issues/3496
        getElement().setProperty("drawerOpened", false);

        attachDrawerOpenedChangedListener();
    }

    /**
     * @return the layout content
     */
    @Nullable
    public Component getContent() {
        return content;
    }

    /**
     * Sets the content that should be overlapped or pushed aside by the drawer panel.
     *
     * @param content the content to set
     */
    public void setContent(@Nullable Component content) {
        if (this.content != null) {
            updateContentInert(false);
            componentInertManager = null;
            removeComponent(this.content);
        }

        this.content = content;

        if (content != null) {
            componentInertManager = createComponentInertManager(content);

            addComponent(content);
            updateSlot("contentSlot", content);

            if (isDrawerOpened()) {
                updateContentInert(isModal());
            }
        }
    }

    /**
     * @return drawer panel content components
     */
    public Component getDrawerContent() {
        return drawerContent;
    }

    /**
     * Sets the drawer panel content components.
     *
     * @param drawerContent content to set
     */
    public void setDrawerContent(@Nullable Component drawerContent) {
        if (this.drawerContent != null) {
            removeComponent(this.drawerContent);
            this.drawerContent = null;
        }

        if (drawerContent != null) {
            this.drawerContent = drawerContent;
            addComponent(drawerContent);
            updateSlot("drawerContentSlot", drawerContent);
        }
    }

    /**
     * @return whether the drawer panel is modal
     */
    public boolean isModal() {
        return getElement().getProperty("modal", true);
    }

    /**
     * Sets whether the drawer panel should be modal. If {@code true}, the {@link #content} will not receive requests
     * from clinet-side even if the modality curtain is removed.
     * <p>
     * The default value is {@code true}.
     *
     * @param modal whether the drawer panel should be modal
     */
    public void setModal(boolean modal) {
        getElement().setProperty("modal", modal);

        if (isDrawerOpened()) {
            updateContentInert(modal);
        }
    }

    /**
     * @return the drawer mode
     */
    public DrawerMode getDrawerMode() {
        String mode = getElement().getProperty("drawerMode");
        if (Strings.isNullOrEmpty(mode)) {
            return DrawerMode.OVERLAY;
        }
        return DrawerMode.valueOf(mode.toUpperCase());
    }

    /**
     * Sets the way how should the drawer panel should be displayed.
     *
     * @param drawerMode drawer mode to set
     */
    public void setDrawerMode(DrawerMode drawerMode) {
        getElement().setProperty("drawerMode", drawerMode.name().toLowerCase());
    }

    /**
     * @return the drawer placement
     */
    public DrawerPlacement getDrawerPlacement() {
        String placement = getElement().getProperty("drawerPlacement");
        if (Strings.isNullOrEmpty(placement)) {
            return DrawerPlacement.RIGHT;
        }
        return DrawerPlacement.valueOf(placement.toUpperCase().replace("-", "_"));
    }

    /**
     * Sets the drawer placement.
     *
     * @param placement drawer placement to set
     */
    public void setDrawerPlacement(DrawerPlacement placement) {
        getElement().setProperty("drawerPlacement", placement.name().toLowerCase().replace("_", "-"));
    }

    /**
     * @return whether the drawer should be closed when the modality curtain is clicked
     */
    public boolean isCloseOnModalityCurtainClick() {
        return getElement().getProperty("closeOnModalityCurtainClick", true);
    }

    /**
     * Sets whether the drawer should be closed when the modality curtain is clicked.
     * <p>
     * The default value is {@code true}.
     *
     * @param closeOnClick closeOnClick option
     */
    public void setCloseOnModalityCurtainClick(boolean closeOnClick) {
        getElement().setProperty("closeOnModalityCurtainClick", closeOnClick);
    }

    /**
     * @return whether the drawer should be displayed as an overlay on small screens.
     */
    public boolean isDisplayAsOverlayOnSmallDevices() {
        return getElement().getProperty("displayAsOverlayOnSmallDevices", true);
    }

    /**
     * Sets whether the drawer should be displayed as an overlay on small screens.
     * <p>
     * The default value is {@code true}.
     *
     * @param displayAsOverlay displayAsOverlay option
     */
    public void setDisplayAsOverlayOnSmallDevices(boolean displayAsOverlay) {
        getElement().setProperty("displayAsOverlayOnSmallDevices", displayAsOverlay);
    }

    @Nullable
    public String getOverlayAriaLabel() {
        return getElement().getProperty("overlayAriaLabel");
    }

    public void setOverlayAriaLabel(String ariaLabel) {
        getElement().setProperty("overlayAriaLabel", ariaLabel);
    }

    /**
     * Note that this does not return the actual size of the drawer panel but the height which has been set using
     * {@link #setDrawerVerticalSize(String)} or using CSS property {@code --jmix-side-panel-layout-drawer-vertical-size}.
     *
     * @return the height defined for the drawer panel
     */
    @Nullable
    public String getDrawerVerticalSize() {
        return getElement().getStyle().get("--jmix-side-panel-layout-drawer-vertical-size");
    }

    /**
     * Sets the height of the drawer panel when drawer placement is horizontal ({@link DrawerPlacement#TOP},
     * {@link DrawerPlacement#BOTTOM}).
     * <p>
     * The height should be in a format understood by the browser, e.g. "100px" or "2.5rem".
     * <p>
     * If the provided height value is {@code null} then height is removed from the component style.
     *
     * @param size height to set
     */
    public void setDrawerVerticalSize(@Nullable String size) {
        getElement().getStyle().set("--jmix-side-panel-layout-drawer-vertical-size", size);
    }

    /**
     * Note that this does not return the actual size of the drawer panel but the max-height which has been set using
     * {@link #setDrawerVerticalMaxSize(String)} or using CSS property {@code --jmix-side-panel-layout-drawer-vertical-max-size}.
     *
     * @return the max-height defined for the drawer panel
     */
    @Nullable
    public String getDrawerVerticalMaxSize() {
        return getElement().getStyle().get("--jmix-side-panel-layout-drawer-vertical-max-size");
    }

    /**
     * Sets the max-height of the drawer panel when drawer placement is horizontal ({@link DrawerPlacement#TOP},
     * {@link DrawerPlacement#BOTTOM}).
     * <p>
     * The max-height should be in a format understood by the browser, e.g. "100px" or "2.5rem".
     * <p>
     * If the provided max-height value is {@code null} then max-height is removed from the component style.
     *
     * @param maxSize max-height to set
     */
    public void setDrawerVerticalMaxSize(@Nullable String maxSize) {
        getElement().getStyle().set("--jmix-side-panel-layout-drawer-vertical-max-size", maxSize);
    }

    /**
     * Note that this does not return the actual size of the drawer panel but the min-height which has been set using
     * {@link #setDrawerVerticalMinSize(String)} or using CSS property {@code --jmix-side-panel-layout-drawer-vertical-min-size}.
     *
     * @return the min-height defined for the drawer panel
     */
    @Nullable
    public String getDrawerVerticalMinSize() {
        return getElement().getStyle().get("--jmix-side-panel-layout-drawer-vertical-min-size");
    }

    /**
     * Sets the min-height of the drawer panel when drawer placement is horizontal ({@link DrawerPlacement#TOP},
     * {@link DrawerPlacement#BOTTOM}).
     * <p>
     * The min-height should be in a format understood by the browser, e.g. "100px" or "2.5rem".
     * <p>
     * If the provided min-height value is {@code null} then min-height is removed from the component style.
     *
     * @param minSize min-height to set
     */
    public void setDrawerVerticalMinSize(@Nullable String minSize) {
        getElement().getStyle().set("--jmix-side-panel-layout-drawer-vertical-min-size", minSize);
    }

    /**
     * Note that this does not return the actual size of the drawer panel but the width which has been set using
     * {@link #setDrawerHorizontalSize(String)} or using CSS property {@code --jmix-side-panel-layout-drawer-horizontal-size}.
     *
     * @return the width defined for the drawer panel
     */
    @Nullable
    public String getDrawerHorizontalSize() {
        return getElement().getStyle().get("--jmix-side-panel-layout-drawer-horizontal-size");
    }

    /**
     * Sets the width of the drawer panel when drawer placement is horizontal ({@link DrawerPlacement#LEFT},
     * {@link DrawerPlacement#RIGHT}, {@link DrawerPlacement#INLINE_START}, {@link DrawerPlacement#INLINE_END}).
     * <p>
     * The width should be in a format understood by the browser, e.g. "100px" or "2.5rem".
     * <p>
     * If the provided width value is {@code null} then width is removed from the component style.
     *
     * @param size width to set
     */
    public void setDrawerHorizontalSize(@Nullable String size) {
        getElement().getStyle().set("--jmix-side-panel-layout-drawer-horizontal-size", size);
    }

    /**
     * Note that this does not return the actual size of the drawer panel but the max-width which has been set using
     * {@link #setDrawerHorizontalMaxSize(String)} or using CSS property {@code --jmix-side-panel-layout-drawer-horizontal-max-size}.
     *
     * @return the max-width defined for the drawer panel
     */
    @Nullable
    public String getDrawerHorizontalMaxSize() {
        return getElement().getStyle().get("--jmix-side-panel-layout-drawer-horizontal-max-size");
    }

    /**
     * Sets the max-width of the drawer panel when drawer placement is horizontal ({@link DrawerPlacement#LEFT},
     * {@link DrawerPlacement#RIGHT}, {@link DrawerPlacement#INLINE_START}, {@link DrawerPlacement#INLINE_END}).
     * <p>
     * The max-width should be in a format understood by the browser, e.g. "100px" or "2.5rem".
     * <p>
     * If the provided width value is {@code null} then max-width is removed from the component style.
     *
     * @param maxSize max-width to set
     */
    public void setDrawerHorizontalMaxSize(@Nullable String maxSize) {
        getElement().getStyle().set("--jmix-side-panel-layout-drawer-horizontal-max-size", maxSize);
    }

    /**
     * Note that this does not return the actual size of the drawer panel but the min-width which has been set using
     * {@link #setDrawerHorizontalMinSize(String)} or using CSS property {@code --jmix-side-panel-layout-drawer-horizontal-min-size}.
     *
     * @return the min-width defined for the drawer panel
     */
    @Nullable
    public String getDrawerHorizontalMinSize() {
        return getElement().getStyle().get("--jmix-side-panel-layout-drawer-horizontal-min-size");
    }

    /**
     * Sets the min-width of the drawer panel when drawer placement is horizontal ({@link DrawerPlacement#LEFT},
     * {@link DrawerPlacement#RIGHT}, {@link DrawerPlacement#INLINE_START}, {@link DrawerPlacement#INLINE_END}).
     * <p>
     * The min-width should be in a format understood by the browser, e.g. "100px" or "2.5rem".
     * <p>
     * If the provided min-width value is {@code null} then min-width is removed from the component style.
     *
     * @param minSize min-width to set
     */
    public void setDrawerHorizontalMinSize(@Nullable String minSize) {
        getElement().getStyle().set("--jmix-side-panel-layout-drawer-horizontal-min-size", minSize);
    }

    /**
     * @return whether the drawer is opened
     */
    @Synchronize(property = "drawerOpened", value = "drawer-opened-changed", allowInert = true)
    public boolean isDrawerOpened() {
        return getElement().getProperty("drawerOpened", false);
    }

    /**
     * Adds a listener to handle modality curtain clicks.
     *
     * @param listener listener to add
     * @return a registration for removing the listener
     */
    public Registration addModalityCurtainClickListener(ComponentEventListener<ModalityCurtainClickEvent> listener) {
        return addListener(ModalityCurtainClickEvent.class, listener);
    }

    /**
     * Adds a listener to handle drawer open events.
     *
     * @param listener listener to add
     * @return a registration for removing the listener
     */
    public Registration addDrawerBeforeOpenListener(ComponentEventListener<DrawerBeforeOpenEvent> listener) {
        return addListener(DrawerBeforeOpenEvent.class, listener);
    }

    /**
     * Adds a listener to handle drawer after open events.
     * @param listener listener to add
     * @return a registration for removing the listener
     */
    public Registration addDrawerAfterOpenListener(ComponentEventListener<DrawerAfterOpenEvent> listener) {
        return addListener(DrawerAfterOpenEvent.class, listener);
    }

    /**
     * Adds a listener to handle drawer close events.
     *
     * @param listener listener to add
     * @return a registration for removing the listener
     */
    public Registration addDrawerCloseListener(ComponentEventListener<DrawerCloseEvent> listener) {
        return addListener(DrawerCloseEvent.class, listener);
    }

    /**
     * Opens the drawer panel.
     *
     * @see DrawerBeforeOpenEvent
     */
    public void openDrawer() {
        if (!isDrawerOpened()) {
            doSetOpened(true, false);
        }
    }

    /**
     * Closes the drawer panel.
     *
     * @see DrawerCloseEvent
     */
    public void closeDrawer() {
        if (isDrawerOpened()) {
            doSetOpened(false, false);
        }
    }

    /**
     * Opens or closes the drawer panel depending on drawer's state.
     */
    public void toggleDrawer() {
        if (isDrawerOpened()) {
            closeDrawer();
        } else {
            openDrawer();
        }
    }

    protected void doSetOpened(boolean opened, boolean fromClient) {
        getElement().setProperty("drawerOpened", opened);

        updateContentInert(opened && isModal());

        if (opened) {
            fireEvent(new DrawerBeforeOpenEvent(this, fromClient));
        } else {
            fireEvent(new DrawerCloseEvent(this, fromClient));
        }
    }

    protected void addComponent(Component... components) {
        if (components == null) {
            return;
        }
        for (Component component : components) {
            if (component != null) {
                getElement().appendChild(component.getElement());
            }
        }
    }

    protected void removeComponent(Component... components) {
        for (Component component : components) {
            if (getElement().equals(component.getElement().getParent())) {
                component.getElement().removeAttribute("slot");
                getElement().removeChild(component.getElement());
            } else {
                throw new IllegalArgumentException("The given component (" + component +
                        ") is not a child of this component");
            }
        }

        Element[] existingChildren = getChildren()
                .map(Component::getElement)
                .toList()
                .toArray(new Element[0]);

        // When fullscreen enabled and the drawer is opened, removed components are not deleted from
        // the client side. We need to explicitly send existing children to the client to delete the
        // difference.
        getElement().callJsFunction("_updateControllers", existingChildren);
    }

    protected void updateContentInert(boolean modal) {
        if (componentInertManager != null) {
            componentInertManager.setInert(modal);
        }
    }

    protected void updateSlot(String slot, Component... components) {
        for (Component component : components) {
            component.getElement().setAttribute("slot", slot);
        }
    }

    protected void attachDrawerOpenedChangedListener() {
        getElement().addPropertyChangeListener("drawerOpened", this::onDrawerOpenedChanged);
    }

    protected void onDrawerOpenedChanged(PropertyChangeEvent event) {
        if (event.isUserOriginated()) {
            doSetOpened((boolean) event.getValue(), event.isUserOriginated());
        }
    }

    protected ComponentInertManager createComponentInertManager(Component content) {
        return new ComponentInertManager(content);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        if (isDrawerOpened()) {
            updateContentInert(isModal());
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);

        updateContentInert(false);
    }

    protected static class ComponentInertManager {

        protected final Component component;

        protected boolean inert;

        public ComponentInertManager(Component component) {
            this.component = component;
        }

        public void setInert(boolean inert) {
            if (this.inert == inert) {
                return;
            }
            this.inert = inert;
            ElementUtil.setInert(component.getElement(), inert);
        }

        public boolean isInert() {
            return inert;
        }
    }
}
