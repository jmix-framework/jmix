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

package io.jmix.flowui.kit.component.draweroverlay;

import com.vaadin.flow.component.*;
import com.vaadin.flow.dom.ClassList;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.Style;
import io.jmix.flowui.kit.component.sidepanellayout.SidePanelPlacement;

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

    public void setHorizontalSize(String size) {
        getContent().setWidth(size);
    }

    public void setHorizontalMinSize(String minSize) {
        getContent().setMinWidth(minSize);
    }

    public void setHorizontalMaxSize(String maxSize) {
        getContent().setMaxWidth(maxSize);
    }

    public void setVerticalSize(String size) {
        getContent().setHeight(size);
    }

    public void setVerticalMaxSize(String maxSize) {
        getContent().setMaxHeight(maxSize);
    }

    public void setVerticalMinSize(String minSize) {
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

    public boolean isFullscreenOnSmallDevices() {
        return getElement().getProperty("fullscreenOnSmallDevices", true);
    }

    public void setFullscreenOnSmallDevices(boolean fullscreenOnSmallDevice) {
        getElement().setProperty("fullscreenOnSmallDevices", fullscreenOnSmallDevice);
    }

    public SidePanelPlacement getSidePanelPlacement() {
        return getContent().getSidePanelPlacement();
    }

    public void setSidePanelPlacement(SidePanelPlacement placement) {
        getContent().setSidePanelPlacement(placement);
    }

    public boolean isOpened() {
        return getContent().isOpened();
    }

    public void open() {
        getContent().open();
    }

    public void close() {
        getContent().close();
    }

    // TODO: pinyazhin, wait Vaadin 25
    public boolean isModal() {
        return getContent().isModal();
    }

    public void setModal(boolean modal) {
        getContent().setModal(modal);
    }

    public boolean isCloseOnEsc() {
        return getContent().isCloseOnEsc();
    }

    public void setCloseOnEsc(boolean closeOnEsc) {
        getContent().setCloseOnEsc(closeOnEsc);
    }

    public boolean isCloseOnOutsideClick() {
        return getContent().isCloseOnOutsideClick();
    }

    public void setCloseOnOutsideClick(boolean closeOnOutsideClick) {
        getContent().setCloseOnOutsideClick(closeOnOutsideClick);
    }

    public String getHeaderTitle() {
        return getContent().getHeaderTitle();
    }

    public void setHeaderTitle(String title) {
        getContent().setHeaderTitle(title);
    }

    public String getOverlayRole() {
        return getContent().getOverlayRole();
    }

    public void setOverlayRole(String role) {
        getContent().setOverlayRole(role);
    }

    public DrawerHeader getHeader() {
        if (header == null) {
            header = new DrawerHeader();
        }
        return header;
    }

    public DrawerFooter getFooter() {
        if (footer == null) {
            footer = new DrawerFooter();
        }
        return footer;
    }

    public class DrawerHeader extends AbstractDrawerHeaderFooter {

        @Override
        protected HasComponents getHeaderFooter() {
            return getContent().getHeader();
        }
    }

    public class DrawerFooter extends AbstractDrawerHeaderFooter {
        @Override
        protected HasComponents getHeaderFooter() {
            return getContent().getFooter();
        }
    }

    protected abstract class AbstractDrawerHeaderFooter implements HasComponents {

        protected List<Component> components;

        public AbstractDrawerHeaderFooter() {
            this.components = new ArrayList<>();
        }

        @Override
        public void add(Component... components) {
            this.components.addAll(Arrays.asList(components));

            getHeaderFooter().add(components);
        }

        @Override
        public void add(Collection<Component> components) {
            this.components.addAll(components);

            getHeaderFooter().add(components);
        }

        @Override
        public void add(String text) {
            Text textComponent = new Text(text);

            this.components.add(textComponent);

            getHeaderFooter().add(textComponent);
        }

        @Override
        public void remove(Component... components) {
            this.components.removeAll(Arrays.asList(components));

            getHeaderFooter().remove(components);
        }

        @Override
        public void remove(Collection<Component> components) {
            this.components.removeAll(components);

            getHeaderFooter().remove(components);
        }

        @Override
        public void removeAll() {
            this.components.clear();

            getHeaderFooter().removeAll();
        }

        @Override
        public void addComponentAtIndex(int index, Component component) {
            this.components.add(index, component);

            getHeaderFooter().addComponentAtIndex(index, component);
        }

        @Override
        public void addComponentAsFirst(Component component) {
            this.components.add(0, component);

            getHeaderFooter().addComponentAsFirst(component);
        }

        public List<Component> getComponents() {
            return List.copyOf(components);
        }

        @Override
        public Element getElement() {
            return getContent().getHeader().getElement();
        }

        protected abstract HasComponents getHeaderFooter();
    }
}
