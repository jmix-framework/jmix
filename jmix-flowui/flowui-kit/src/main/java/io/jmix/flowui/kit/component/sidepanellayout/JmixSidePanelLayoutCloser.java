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

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JsModule;
import io.jmix.flowui.kit.icon.JmixFontIcon;
import jakarta.annotation.Nullable;

@Tag("jmix-side-panel-layout-closer")
@JsModule("./src/side-panel-layout/jmix-side-panel-layout-closer.js")
public class JmixSidePanelLayoutCloser extends Button {

    protected Component icon;

    public JmixSidePanelLayoutCloser() {
        attachClickListener();
        setIcon(null);
    }

    @Nullable
    @Override
    public Component getIcon() {
        return icon;
    }

    public void setIcon(@Nullable Component icon) {
        this.icon = icon;

        setIconInternal(icon);
    }

    protected void setIconInternal(@Nullable Component icon) {
        super.setIcon(icon == null
                ? JmixFontIcon.SIDE_PANEL_LAYOUT_CLOSER.create()
                : icon);

        // The slot attribute needs to be removed because jmix-side-panel-layout-closer
        // template doesn't have prefix and suffix slots
        if (super.getIcon() != null) {
            super.getIcon().getElement().removeAttribute("slot");
        }
    }

    protected void attachClickListener() {
        addClickListener(this::onClick);
    }

    protected void onClick(ClickEvent<Button> event) {
        // To be used in subclasses
    }
}
