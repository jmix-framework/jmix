/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowui.kit.component.drawerlayout;

import com.google.common.base.Strings;
import com.vaadin.flow.component.*;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementUtil;
import com.vaadin.flow.dom.PropertyChangeEvent;
import jakarta.annotation.Nullable;

public class JmixDrawerLayout extends Component implements HasSize, HasTheme {

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

            updateContentInert(isModal());
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

        updateContentInert(modal);
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
     * @return whether the drawer is opened
     */
    @Synchronize(property = "drawerOpened", value = "drawer-opened-changed", allowInert = true)
    public boolean isDrawerOpened() {
        return getElement().getProperty("drawerOpened", false);
    }

    /**
     * Opens the drawer panel.
     *
     * @see DrawerOpenEvent
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

    protected void doSetOpened(boolean opened, boolean fromClient) {
        getElement().setProperty("drawerOpened", opened);

        updateContentInert(isModal());

        if (opened) {
            fireEvent(new DrawerOpenEvent(this, fromClient));
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

        updateContentInert(isModal());
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
