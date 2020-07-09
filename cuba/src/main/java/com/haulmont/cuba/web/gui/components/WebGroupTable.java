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

import com.haulmont.cuba.gui.components.GroupTable;
import com.haulmont.cuba.gui.presentation.LegacyPresentationsDelegate;
import com.haulmont.cuba.gui.presentation.Presentations;
import com.haulmont.cuba.settings.binder.CubaGroupTableSettingsBinder;
import com.haulmont.cuba.settings.component.LegacySettingsDelegate;
import io.jmix.core.Entity;
import io.jmix.ui.component.presentation.TablePresentationsLayout;
import io.jmix.ui.presentation.TablePresentations;
import io.jmix.ui.presentation.model.TablePresentation;
import com.haulmont.cuba.settings.converter.LegacyGroupTableSettingsConverter;
import io.jmix.ui.settings.component.binder.ComponentSettingsBinder;
import org.dom4j.Element;

@Deprecated
public class WebGroupTable<E extends Entity> extends io.jmix.ui.component.impl.WebGroupTable<E>
        implements GroupTable<E> {

    protected LegacySettingsDelegate settingsDelegate;
    protected LegacyPresentationsDelegate presentationsDelegate;

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();

        settingsDelegate = createSettingsDelegate();
    }

    @Override
    public void applyDataLoadingSettings(Element element) {
        settingsDelegate.applyDataLoadingSettings(element);
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

    @Override
    protected ComponentSettingsBinder getSettingsBinder() {
        return beanLocator.get(CubaGroupTableSettingsBinder.NAME);
    }

    protected LegacySettingsDelegate createSettingsDelegate() {
        return beanLocator.getPrototype(LegacySettingsDelegate.NAME,
                this, new LegacyGroupTableSettingsConverter(), getSettingsBinder());
    }

    @Override
    protected TablePresentations createTablePresentations() {
        Presentations presentations = beanLocator.getPrototype(Presentations.NAME, this);

        presentationsDelegate = beanLocator.getPrototype(LegacyPresentationsDelegate.NAME,
                this, presentations, getSettingsBinder());

        return presentations;
    }

    @Override
    protected TablePresentationsLayout createTablePresentationsLayout() {
        TablePresentationsLayout layout = super.createTablePresentationsLayout();
        return presentationsDelegate.createTablePresentationsLayout(layout);
    }

    @Override
    protected void updatePresentationSettings(TablePresentations p) {
        if (settingsDelegate.isLegacySettings(getFrame())) {
            presentationsDelegate.updatePresentationSettings((Presentations) p);
        } else {
            super.updatePresentationSettings(p);
        }
    }

    @Override
    protected void applyPresentationSettings(TablePresentation p) {
        if (settingsDelegate.isLegacySettings(getFrame())) {
            presentationsDelegate.applyPresentationSettings(p);
        } else {
            super.applyPresentationSettings(p);
        }
    }

    @Override
    public void resetPresentation() {
        if (settingsDelegate.isLegacySettings(getFrame())) {
            presentationsDelegate.resetPresentations(settingsDelegate.getDefaultSettings());
        } else {
            super.resetPresentation();
        }
    }
}
