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

import com.google.common.base.Strings;
import com.vaadin.flow.component.*;
import io.jmix.flowui.kit.component.drawerlayout.DrawerPlacement;
import jakarta.annotation.Nullable;

import java.util.Collection;

public class JmixDrawerOverlay extends Composite<JmixDrawerDialog> implements HasComponents, HasSize {

    @Override
    public void add(Collection<Component> components) {
        getContent().add(components);
    }

    @Override
    public void addComponentAtIndex(int index, Component component) {
        getContent().addComponentAtIndex(index, component);
    }

    @Nullable
    @Override
    public String getWidth() {
        return getContent().getWidth();
    }

    @Override
    public void setWidth(@Nullable String width) {
        getContent().setWidth(width);
    }

    @Nullable
    @Override
    public String getMaxWidth() {
        return getContent().getMaxWidth();
    }

    @Override
    public void setMaxWidth(@Nullable String maxWidth) {
        getContent().setMaxWidth(maxWidth);
    }

    @Nullable
    @Override
    public String getMinWidth() {
        return getContent().getMinWidth();
    }

    @Override
    public void setMinWidth(@Nullable String minWidth) {
        getContent().setMinWidth(minWidth);
    }

    @Nullable
    @Override
    public String getHeight() {
        return getContent().getHeight();
    }

    @Override
    public void setHeight(@Nullable String height) {
        getContent().setHeight(height);
    }

    @Nullable
    @Override
    public String getMaxHeight() {
        return getContent().getMaxHeight();
    }

    @Override
    public void setMaxHeight(@Nullable String maxHeight) {
        getContent().setMaxHeight(maxHeight);
    }

    @Nullable
    @Override
    public String getMinHeight() {
        return getContent().getMinHeight();
    }

    @Override
    public void setMinHeight(@Nullable String minHeight) {
        getContent().setMinHeight(minHeight);
    }

    public boolean isFullscreenOnSmallDevices() {
        return getElement().getProperty("fullscreenOnSmallDevices", true);
    }

    public void setFullscreenOnSmallDevices(boolean fullscreenOnSmallDevice) {
        getElement().setProperty("fullscreenOnSmallDevices", fullscreenOnSmallDevice);
    }

    public DrawerPlacement getDrawerPlacement() {
        String placement = getElement().getProperty("drawerPlacement");
        if (Strings.isNullOrEmpty(placement)) {
            return DrawerPlacement.RIGHT;
        }
        return DrawerPlacement.valueOf(placement.toUpperCase().replace("-", "_"));
    }

    public void setDrawerPlacement(DrawerPlacement placement) {
        getElement().setProperty("drawerPlacement", placement.name().toLowerCase().replace("_", "-"));
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

    public void toggle() {
        if (isOpened()) {
            close();
        } else {
            open();
        }
    }

    // TODO: pinyazhin, wait Vaadin 25
    public boolean isModal() {
        return getContent().isModal();
    }

    public void setModal(boolean modal) {
        getContent().setModal(modal);
    }
}
