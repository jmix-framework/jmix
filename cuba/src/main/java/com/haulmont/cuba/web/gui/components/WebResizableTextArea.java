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

package com.haulmont.cuba.web.gui.components;

import com.haulmont.cuba.gui.components.ResizableTextArea;
import com.haulmont.cuba.settings.component.LegacySettingsDelegate;
import com.haulmont.cuba.settings.converter.LegacyResizableTextAreaSettingsConverter;
import io.jmix.ui.component.impl.ResizableTextAreaImpl;
import io.jmix.ui.settings.ComponentSettingsRegistry;
import io.jmix.ui.settings.component.binder.ComponentSettingsBinder;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Consumer;

@Deprecated
public class WebResizableTextArea<V> extends ResizableTextAreaImpl<V>
        implements ResizableTextArea<V> {

    protected ComponentSettingsRegistry settingsRegistry;
    protected LegacySettingsDelegate settingsDelegate;

    protected int columns;

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();

        settingsDelegate = createSettingsDelegate();
    }

    @Autowired
    public void setSettingsRegistry(ComponentSettingsRegistry settingsRegistry) {
        this.settingsRegistry = settingsRegistry;
    }

    @Override
    public void applySettings(Element element) {
        if (isResizable()) {
            settingsDelegate.applySettings(element);
        }
    }

    @Override
    public boolean saveSettings(Element element) {
        if (!isResizable()) {
            return false;
        }

        return settingsDelegate.saveSettings(element);
    }

    @Override
    public boolean isSettingsEnabled() {
        return settingsDelegate.isSettingsEnabled();
    }

    @Override
    public void setSettingsEnabled(boolean settingsEnabled) {
        settingsDelegate.setSettingsEnabled(settingsEnabled);
    }

    protected LegacySettingsDelegate createSettingsDelegate() {
        return (LegacySettingsDelegate) applicationContext.getBean(LegacySettingsDelegate.NAME,
                this, new LegacyResizableTextAreaSettingsConverter(), getSettingsBinder());
    }

    protected ComponentSettingsBinder getSettingsBinder() {
        return settingsRegistry.getSettingsBinder(this.getClass());
    }

    @Override
    public void addValidator(Consumer<? super V> validator) {
        addValidator(validator::accept);
    }

    @Override
    public void removeValidator(Consumer<V> validator) {
        removeValidator(validator::accept);
    }

    @Override
    public boolean isResizable() {
        return getResizableDirection() != ResizeDirection.NONE;
    }

    @Override
    public void setResizable(boolean resizable) {
        ResizeDirection value = resizable ? ResizeDirection.BOTH : ResizeDirection.NONE;
        setResizableDirection(value);
    }

    @Override
    public void removeResizeListener(Consumer<ResizableTextArea.ResizeEvent> listener) {
        unsubscribe(ResizableTextArea.ResizeEvent.class, listener);
    }

    @Override
    public int getColumns() {
        return columns;
    }

    @Override
    public void setColumns(int columns) {
        this.columns = columns;
        // See com.vaadin.v7.client.ui.VTextField.setColumns for formula
        component.setWidth(columns + "em");
    }

    @Override
    public void removeTextChangeListener(Consumer<TextChangeEvent> listener) {
        unsubscribe(TextChangeEvent.class, listener);
    }
}
