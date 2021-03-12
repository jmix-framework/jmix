/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.settings.component.binder;

import io.jmix.core.JmixOrder;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ResizableTextArea;
import io.jmix.ui.component.ResizableTextArea.ResizeDirection;
import io.jmix.ui.component.impl.ResizableTextAreaImpl;
import io.jmix.ui.settings.component.ComponentSettings;
import io.jmix.ui.settings.component.ResizableTextAreaSettings;
import io.jmix.ui.settings.component.SettingsWrapper;
import io.jmix.ui.widget.JmixResizableTextAreaWrapper;
import org.springframework.core.annotation.Order;

@Order(JmixOrder.LOWEST_PRECEDENCE)
@org.springframework.stereotype.Component("ui_ResizableTextAreaSettingsBinder")
public class ResizableTextAreaSettingsBinder implements ComponentSettingsBinder<ResizableTextArea, ResizableTextAreaSettings> {

    @Override
    public Class<? extends Component> getComponentClass() {
        return ResizableTextAreaImpl.class;
    }

    @Override
    public Class<? extends ComponentSettings> getSettingsClass() {
        return ResizableTextAreaSettings.class;
    }

    @Override
    public void applySettings(ResizableTextArea textArea, SettingsWrapper wrapper) {
        ResizableTextAreaSettings settings = wrapper.getSettings();

        if (textArea.getResizableDirection() == ResizeDirection.NONE) {
            return;
        }

        if (settings.getHeight() != null
                && settings.getWidth() != null) {
            textArea.setWidth(settings.getWidth());
            textArea.setHeight(settings.getHeight());
        }
    }

    @Override
    public boolean saveSettings(ResizableTextArea textArea, SettingsWrapper wrapper) {
        ResizableTextAreaSettings settings = wrapper.getSettings();

        if (textArea.getResizableDirection() == ResizeDirection.NONE) {
            return false;
        }


        if (isSettingsChanged(textArea, settings)) {
            settings.setWidth(getWidth(textArea));
            settings.setHeight(getHeight(textArea));

            return true;
        }

        return false;
    }

    @Override
    public ResizableTextAreaSettings getSettings(ResizableTextArea textArea) {
        ResizableTextAreaSettings settings = createSettings();
        settings.setId(textArea.getId());

        settings.setWidth(getWidth(textArea));
        settings.setHeight(getHeight(textArea));

        return settings;
    }

    protected boolean isSettingsChanged(ResizableTextArea textArea, ResizableTextAreaSettings settings) {
        if (settings.getHeight() == null || settings.getWidth() == null) {
            return true;
        }

        return !getWidth(textArea).equals(settings.getWidth())
                || !getHeight(textArea).equals(settings.getHeight());
    }

    protected String getWidth(ResizableTextArea textArea) {
        JmixResizableTextAreaWrapper textAreaWrapper =
                (JmixResizableTextAreaWrapper) ((Component.Wrapper) textArea).getComposition();

        return textArea.getWidth() + textAreaWrapper.getWidthUnits().toString();
    }

    protected String getHeight(ResizableTextArea textArea) {
        JmixResizableTextAreaWrapper textAreaWrapper =
                (JmixResizableTextAreaWrapper) ((Component.Wrapper) textArea).getComposition();

        return textArea.getHeight() + textAreaWrapper.getHeightUnits().toString();
    }

    protected ResizableTextAreaSettings createSettings() {
        return new ResizableTextAreaSettings();
    }
}
