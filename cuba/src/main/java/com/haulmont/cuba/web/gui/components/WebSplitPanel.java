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

import com.haulmont.cuba.gui.components.CubaComponentsHelper;
import com.haulmont.cuba.gui.components.SplitPanel;
import com.haulmont.cuba.settings.component.LegacySettingsDelegate;
import com.haulmont.cuba.settings.converter.LegacySplitPanelSettingsConverter;
import io.jmix.ui.component.impl.SplitPanelImpl;
import io.jmix.ui.settings.ComponentSettingsRegistry;
import io.jmix.ui.settings.component.binder.ComponentSettingsBinder;
import org.dom4j.Element;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Consumer;

@Deprecated
public class WebSplitPanel extends SplitPanelImpl implements SplitPanel, InitializingBean {

    protected ComponentSettingsRegistry settingsRegistry;
    protected LegacySettingsDelegate settingsDelegate;

    @Autowired
    public void setSettingsRegistry(ComponentSettingsRegistry settingsRegistry) {
        this.settingsRegistry = settingsRegistry;
    }

    @Override
    public void setSplitPosition(int pos, int unit) {
        setSplitPosition(pos, CubaComponentsHelper.convertToSizeUnit(unit));
    }

    @Override
    public void setSplitPosition(int pos, int unit, boolean reversePosition) {
        setSplitPosition(pos, CubaComponentsHelper.convertToSizeUnit(unit), reversePosition);
    }

    @Override
    public int getSplitPositionUnit() {
        return CubaComponentsHelper.convertFromSizeUnit(getSplitPositionSizeUnit());
    }

    @Override
    public void setMinSplitPosition(int pos, int unit) {
        setMinSplitPosition(pos, CubaComponentsHelper.convertToSizeUnit(unit));
    }

    @Override
    public void setMaxSplitPosition(int pos, int unit) {
        setMaxSplitPosition(pos, CubaComponentsHelper.convertToSizeUnit(unit));
    }

    @Override
    public void removeSplitPositionChangeListener(Consumer<SplitPositionChangeEvent> listener) {
        unsubscribe(SplitPositionChangeEvent.class, listener);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        settingsDelegate = createSettingsDelegate();
    }

    @Override
    public void applySettings(Element element) {
        settingsDelegate.applySettings(element);
    }

    @Override
    public boolean saveSettings(Element element) {
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
                this, new LegacySplitPanelSettingsConverter(), getSettingsBinder());
    }

    protected ComponentSettingsBinder getSettingsBinder() {
        return settingsRegistry.getSettingsBinder(this.getClass());
    }
}
