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
public class JmixSidePanelLayout extends Component implements HasSize, HasStyle {

    protected Component content;
    protected Component sidePanelContent;

    protected ComponentInertManager componentInertManager;

    public JmixSidePanelLayout() {
        // Workaround for: https://github.com/vaadin/flow/issues/3496
        getElement().setProperty("sidePanelOpened", false);

        attachSidePanelOpenedChangedListener();
    }

    /**
     * @return the layout content
     */
    @Nullable
    public Component getContent() {
        return content;
    }

    /**
     * Sets the content that should be overlapped or pushed aside by the side panel.
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

            if (isSidePanelOpened()) {
                updateContentInert(isModal());
            }
        }
    }

    /**
     * @return side panel content components
     */
    public Component getSidePanelContent() {
        return sidePanelContent;
    }

    /**
     * Sets the side panel content components.
     *
     * @param sidePanelContent content to set
     */
    public void setSidePanelContent(@Nullable Component sidePanelContent) {
        if (this.sidePanelContent != null) {
            removeComponent(this.sidePanelContent);
            this.sidePanelContent = null;
        }

        if (sidePanelContent != null) {
            this.sidePanelContent = sidePanelContent;
            addComponent(sidePanelContent);
            updateSlot("sidePanelContentSlot", sidePanelContent);
        }
    }

    /**
     * @return whether the side panel is modal
     */
    public boolean isModal() {
        return getElement().getProperty("modal", true);
    }

    /**
     * Sets whether the side panel should be modal. If {@code true}, the {@link #content} will not receive requests
     * from client-side even if the modality curtain is removed.
     * <p>
     * The default value is {@code true}.
     *
     * @param modal whether the side panel should be modal
     */
    public void setModal(boolean modal) {
        getElement().setProperty("modal", modal);

        if (isSidePanelOpened()) {
            updateContentInert(modal);
        }
    }

    /**
     * @return the side panel mode
     */
    public SidePanelMode getSidePanelMode() {
        String mode = getElement().getProperty("sidePanelMode");
        if (Strings.isNullOrEmpty(mode)) {
            return SidePanelMode.OVERLAY;
        }
        return SidePanelMode.valueOf(mode.toUpperCase());
    }

    /**
     * Sets the way how should the side panel be displayed.
     *
     * @param panelMode side panel mode to set
     */
    public void setSidePanelMode(SidePanelMode panelMode) {
        getElement().setProperty("sidePanelMode", panelMode.name().toLowerCase());
    }

    /**
     * @return the side panel placement
     */
    public SidePanelPlacement getSidePanelPlacement() {
        String placement = getElement().getProperty("sidePanelPlacement");
        if (Strings.isNullOrEmpty(placement)) {
            return SidePanelPlacement.RIGHT;
        }
        return SidePanelPlacement.valueOf(placement.toUpperCase().replace("-", "_"));
    }

    /**
     * Sets the side panel placement.
     *
     * @param placement side panel placement to set
     */
    public void setSidePanelPlacement(SidePanelPlacement placement) {
        getElement().setProperty("sidePanelPlacement", placement.name().toLowerCase().replace("_", "-"));
    }

    /**
     * @return whether the side panel should be closed when the modality curtain is clicked
     */
    public boolean isCloseOnModalityCurtainClick() {
        return getElement().getProperty("closeOnModalityCurtainClick", true);
    }

    /**
     * Sets whether the side panel should be closed when the modality curtain is clicked.
     * <p>
     * The default value is {@code true}.
     *
     * @param closeOnClick closeOnClick option
     */
    public void setCloseOnModalityCurtainClick(boolean closeOnClick) {
        getElement().setProperty("closeOnModalityCurtainClick", closeOnClick);
    }

    /**
     * @return whether the side panel should be displayed as an overlay on small screens.
     */
    public boolean isDisplayAsOverlayOnSmallDevices() {
        return getElement().getProperty("displayAsOverlayOnSmallDevices", true);
    }

    /**
     * Sets whether the side panel should be displayed as an overlay on small screens.
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
     * Note that this does not return the actual size of the side panel but the height which has been set using
     * {@link #setSidePanelVerticalSize(String)} or using CSS property {@code --jmix-side-panel-vertical-size}.
     *
     * @return the height defined for the side panel
     */
    @Nullable
    public String getSidePanelVerticalSize() {
        return getElement().getStyle().get("--jmix-side-panel-vertical-size");
    }

    /**
     * Sets the height of the side panel when side placement is horizontal ({@link SidePanelPlacement#TOP},
     * {@link SidePanelPlacement#BOTTOM}).
     * <p>
     * The height should be in a format understood by the browser, e.g. "100px" or "2.5em".
     * <p>
     * If the provided height value is {@code null} then height is removed from the component style.
     *
     * @param size height to set
     */
    public void setSidePanelVerticalSize(@Nullable String size) {
        getElement().getStyle().set("--jmix-side-panel-vertical-size", size);
    }

    /**
     * Note that this does not return the actual size of the side panel but the max-height which has been set using
     * {@link #setSidePanelVerticalMaxSize(String)} or using CSS property {@code --jmix-side-panel-vertical-max-size}.
     *
     * @return the max-height defined for the side panel
     */
    @Nullable
    public String getSidePanelVerticalMaxSize() {
        return getElement().getStyle().get("--jmix-side-panel-vertical-max-size");
    }

    /**
     * Sets the max-height of the side panel when side panel placement is horizontal ({@link SidePanelPlacement#TOP},
     * {@link SidePanelPlacement#BOTTOM}).
     * <p>
     * The max-height should be in a format understood by the browser, e.g. "100px" or "2.5em".
     * <p>
     * If the provided max-height value is {@code null} then max-height is removed from the component style.
     *
     * @param maxSize max-height to set
     */
    public void setSidePanelVerticalMaxSize(@Nullable String maxSize) {
        getElement().getStyle().set("--jmix-side-panel-vertical-max-size", maxSize);
    }

    /**
     * Note that this does not return the actual size of the side panel but the min-height which has been set using
     * {@link #setSidePanelVerticalMinSize(String)} or using CSS property {@code --jmix-side-panel-vertical-min-size}.
     *
     * @return the min-height defined for the side panel
     */
    @Nullable
    public String getSidePanelVerticalMinSize() {
        return getElement().getStyle().get("--jmix-side-panel-vertical-min-size");
    }

    /**
     * Sets the min-height of the side panel when placement is horizontal ({@link SidePanelPlacement#TOP},
     * {@link SidePanelPlacement#BOTTOM}).
     * <p>
     * The min-height should be in a format understood by the browser, e.g. "100px" or "2.5em".
     * <p>
     * If the provided min-height value is {@code null} then min-height is removed from the component style.
     *
     * @param minSize min-height to set
     */
    public void setSidePanelVerticalMinSize(@Nullable String minSize) {
        getElement().getStyle().set("--jmix-side-panel-vertical-min-size", minSize);
    }

    /**
     * Note that this does not return the actual size of the side panel but the width which has been set using
     * {@link #setSidePanelHorizontalSize(String)} or using CSS property {@code --jmix-side-panel-horizontal-size}.
     *
     * @return the width defined for the side panel
     */
    @Nullable
    public String getSidePanelHorizontalSize() {
        return getElement().getStyle().get("--jmix-side-panel-horizontal-size");
    }

    /**
     * Sets the width of the side panel when placement is horizontal ({@link SidePanelPlacement#LEFT},
     * {@link SidePanelPlacement#RIGHT}, {@link SidePanelPlacement#INLINE_START}, {@link SidePanelPlacement#INLINE_END}).
     * <p>
     * The width should be in a format understood by the browser, e.g. "100px" or "2.5em".
     * <p>
     * If the provided width value is {@code null} then width is removed from the component style.
     *
     * @param size width to set
     */
    public void setSidePanelHorizontalSize(@Nullable String size) {
        getElement().getStyle().set("--jmix-side-panel-horizontal-size", size);
    }

    /**
     * Note that this does not return the actual size of the side panel but the max-width which has been set using
     * {@link #setSidePanelHorizontalMaxSize(String)} or using CSS property {@code --jmix-side-panel-horizontal-max-size}.
     *
     * @return the max-width defined for the side panel
     */
    @Nullable
    public String getSidePanelHorizontalMaxSize() {
        return getElement().getStyle().get("--jmix-side-panel-horizontal-max-size");
    }

    /**
     * Sets the max-width of the side panel when placement is horizontal ({@link SidePanelPlacement#LEFT},
     * {@link SidePanelPlacement#RIGHT}, {@link SidePanelPlacement#INLINE_START}, {@link SidePanelPlacement#INLINE_END}).
     * <p>
     * The max-width should be in a format understood by the browser, e.g. "100px" or "2.5em".
     * <p>
     * If the provided width value is {@code null} then max-width is removed from the component style.
     *
     * @param maxSize max-width to set
     */
    public void setSidePanelHorizontalMaxSize(@Nullable String maxSize) {
        getElement().getStyle().set("--jmix-side-panel-horizontal-max-size", maxSize);
    }

    /**
     * Note that this does not return the actual size of the side panel but the min-width which has been set using
     * {@link #setSidePanelHorizontalMinSize(String)} or using CSS property {@code --jmix-side-panel-horizontal-min-size}.
     *
     * @return the min-width defined for the side panel
     */
    @Nullable
    public String getSidePanelHorizontalMinSize() {
        return getElement().getStyle().get("--jmix-side-panel-horizontal-min-size");
    }

    /**
     * Sets the min-width of the side panel when placement is horizontal ({@link SidePanelPlacement#LEFT},
     * {@link SidePanelPlacement#RIGHT}, {@link SidePanelPlacement#INLINE_START}, {@link SidePanelPlacement#INLINE_END}).
     * <p>
     * The min-width should be in a format understood by the browser, e.g. "100px" or "2.5em".
     * <p>
     * If the provided min-width value is {@code null} then min-width is removed from the component style.
     *
     * @param minSize min-width to set
     */
    public void setSidePanelHorizontalMinSize(@Nullable String minSize) {
        getElement().getStyle().set("--jmix-side-panel-horizontal-min-size", minSize);
    }

    /**
     * @return whether the side panel is opened
     */
    @Synchronize(property = "sidePanelOpened", value = "side-panel-opened-changed", allowInert = true)
    public boolean isSidePanelOpened() {
        return getElement().getProperty("sidePanelOpened", false);
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
     * Adds a listener to handle side panel open events.
     *
     * @param listener listener to add
     * @return a registration for removing the listener
     */
    public Registration addSidePanelBeforeOpenListener(ComponentEventListener<SidePanelBeforeOpenEvent> listener) {
        return addListener(SidePanelBeforeOpenEvent.class, listener);
    }

    /**
     * Adds a listener to handle the side panel after open events.
     * @param listener listener to add
     * @return a registration for removing the listener
     */
    public Registration addSidePanelAfterOpenListener(ComponentEventListener<SidePanelAfterOpenEvent> listener) {
        return addListener(SidePanelAfterOpenEvent.class, listener);
    }

    /**
     * Adds a listener to handle side panel close events.
     *
     * @param listener listener to add
     * @return a registration for removing the listener
     */
    public Registration addSidePanelCloseListener(ComponentEventListener<SidePanelCloseEvent> listener) {
        return addListener(SidePanelCloseEvent.class, listener);
    }

    /**
     * Opens the side panel.
     *
     * @see SidePanelBeforeOpenEvent
     */
    public void openSidePanel() {
        if (!isSidePanelOpened()) {
            doSetOpened(true, false);
        }
    }

    /**
     * Closes the side panel.
     *
     * @see SidePanelCloseEvent
     */
    public void closeSidePanel() {
        if (isSidePanelOpened()) {
            doSetOpened(false, false);
        }
    }

    /**
     * Opens or closes the side panel depending on the panel's state.
     */
    public void toggleSidePanel() {
        if (isSidePanelOpened()) {
            closeSidePanel();
        } else {
            openSidePanel();
        }
    }

    protected void doSetOpened(boolean opened, boolean fromClient) {
        getElement().setProperty("sidePanelOpened", opened);

        updateContentInert(opened && isModal());

        if (opened) {
            fireEvent(new SidePanelBeforeOpenEvent(this, fromClient));
        } else {
            fireEvent(new SidePanelCloseEvent(this, fromClient));
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

        // When fullscreen enabled and the side panel is opened, removed components are not deleted from
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

    protected void attachSidePanelOpenedChangedListener() {
        getElement().addPropertyChangeListener("sidePanelOpened", this::onSidePanelOpenedChanged);
    }

    protected void onSidePanelOpenedChanged(PropertyChangeEvent event) {
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

        if (isSidePanelOpened()) {
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
