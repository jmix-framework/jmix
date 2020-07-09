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

import com.haulmont.cuba.gui.components.SplitPanel;
import com.haulmont.cuba.settings.binder.CubaSplitPanelSettingsBinder;
import com.haulmont.cuba.settings.component.LegacySettingsDelegate;
import com.haulmont.cuba.settings.converter.LegacySplitPanelSettingsConverter;
import io.jmix.ui.settings.component.binder.ComponentSettingsBinder;
import org.dom4j.Element;
import org.springframework.beans.factory.InitializingBean;

@Deprecated
public class WebSplitPanel extends io.jmix.ui.component.impl.WebSplitPanel implements SplitPanel, InitializingBean {

    protected LegacySettingsDelegate settingsDelegate;

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
        return beanLocator.getPrototype(LegacySettingsDelegate.NAME,
                this, new LegacySplitPanelSettingsConverter(), getSettingsBinder());
    }

    protected ComponentSettingsBinder getSettingsBinder() {
        return beanLocator.get(CubaSplitPanelSettingsBinder.NAME);
    }
}
