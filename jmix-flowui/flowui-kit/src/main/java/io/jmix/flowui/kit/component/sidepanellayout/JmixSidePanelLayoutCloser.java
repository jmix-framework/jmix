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

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;
import io.jmix.flowui.kit.icon.JmixFontIcon;
import jakarta.annotation.Nullable;

@Tag("jmix-side-panel-layout-closer")
@JsModule("./src/side-panel-layout/jmix-side-panel-layout-closer.js")
public class JmixSidePanelLayoutCloser extends Component implements HasTheme, Focusable<JmixSidePanelLayoutCloser>,
        HasStyle, HasAriaLabel, HasSize {

    protected Component icon;
    protected Component defaultIcon;
    protected JmixSidePanelLayout sidePanelLayout;

    public JmixSidePanelLayoutCloser() {
        setDefaultIcon(JmixFontIcon.SIDE_PANEL_LAYOUT_CLOSER.create());
        setIcon(null);
    }

    public JmixSidePanelLayout getSidePanelLayout() {
        return sidePanelLayout;
    }

    public void setSidePanelLayout(JmixSidePanelLayout sidePanelLayout) {
        this.sidePanelLayout = sidePanelLayout;

        getElement().executeJs("this.sidePanelElement = $0", sidePanelLayout);
    }

    @Nullable
    public Component getIcon() {
        return icon;
    }

    public void setIcon(@Nullable Component icon) {
        if (icon != null && icon.getElement().isTextNode()) {
            throw new IllegalArgumentException("Text node can't be used as an icon");
        }

        if (this.icon != null) {
            remove(this.icon);
        } else {
            remove(defaultIcon);
        }

        this.icon = icon;

        if (icon == null) {
            add(defaultIcon);
        } else {
            add(icon);
        }

        updateThemeAttribute();
    }

    protected void setDefaultIcon(Component defaultIcon) {
        this.defaultIcon = defaultIcon;
    }

    protected void add(Component... components) {
        for (Component component : components) {
            getElement().appendChild(component.getElement());
        }
    }

    protected void remove(Component... components) {
        for (Component component : components) {
            if (getElement().equals(component.getElement().getParent())) {
                getElement().removeChild(component.getElement());
            }
        }
    }

    protected void updateThemeAttribute() {
        if (getChildren().count() == 1) {
            getThemeNames().add("icon");
        } else {
            getThemeNames().remove("icon");
        }
    }
}
