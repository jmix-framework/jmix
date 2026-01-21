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
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dialog.Dialog;
import io.jmix.flowui.kit.component.sidepanellayout.SidePanelPlacement;
import jakarta.annotation.Nullable;

@Tag("jmix-side-dialog")
@JsModule("./src/side-dialog/jmix-side-dialog.js")
public class JmixSideDialogOverlay extends Dialog {

    public SidePanelPlacement getSidePanelPlacement() {
        String placement = getElement().getProperty("sidePanelPlacement");
        if (Strings.isNullOrEmpty(placement)) {
            return SidePanelPlacement.RIGHT;
        }
        return SidePanelPlacement.valueOf(placement.toUpperCase().replace("-", "_"));
    }

    public void setSidePanelPlacement(SidePanelPlacement placement) {
        getElement().setProperty("sidePanelPlacement", placement.name().toLowerCase().replace("_", "-"));
    }

    @Override
    public void setWidth(@Nullable String value) {
        getElement().setProperty("horizontalSize", value);
    }

    @Override
    public void setMinWidth(@Nullable String value) {
        getElement().setProperty("horizontalMinSize", value);
    }

    @Override
    public void setMaxWidth(@Nullable String value) {
        getElement().setProperty("horizontalMaxSize", value);
    }

    @Override
    public void setHeight(@Nullable String value) {
        getElement().setProperty("verticalSize", value);
    }

    @Override
    public void setMinHeight(@Nullable String value) {
        getElement().setProperty("verticalMinSize", value);
    }

    @Override
    public void setMaxHeight(@Nullable String value) {
        getElement().setProperty("verticalMaxSize", value);
    }

    @Override
    public void setWidthFull() {
        setMaxWidth("100%");
        setWidth("100%");
    }

    @Override
    public void setHeightFull() {
        setMaxHeight("100%");
        setHeight("100%");
    }

    /**
     * Sets defaults to vertical and horizontal sizes.
     */
    @Override
    public void setSizeUndefined() {
        super.setSizeUndefined();
    }
}
