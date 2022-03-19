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

package com.haulmont.cuba.gui.presentation;

import com.haulmont.cuba.gui.components.HasSettings;
import io.jmix.ui.component.presentation.TablePresentationsLayout;
import io.jmix.ui.component.presentation.action.PresentationActionsBuilder;
import io.jmix.ui.presentation.model.TablePresentation;
import io.jmix.ui.settings.component.binder.ComponentSettingsBinder;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component(LegacyPresentationsDelegate.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LegacyPresentationsDelegate {

    public static final String NAME = "cuba_LegacyPresentationsDelegate";

    protected Presentations presentations;
    protected HasSettings component;

    protected ComponentSettingsBinder settingsBinder;

    protected ApplicationContext applicationContext;

    public LegacyPresentationsDelegate(HasSettings component,
                                       Presentations presentations,
                                       ComponentSettingsBinder settingsBinder) {
        this.presentations = presentations;
        this.component = component;
        this.settingsBinder = settingsBinder;
    }

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public TablePresentationsLayout createTablePresentationsLayout(TablePresentationsLayout layout) {
        layout.setPresentationActionsBuilder(
                applicationContext.getBean(PresentationActionsBuilder.class, component, settingsBinder));
        layout.build();
        return layout;
    }

    public void updatePresentationSettings(Presentations p) {
        Element e = presentations.getSettings(p.getCurrent());
        component.saveSettings(e);
        presentations.setSettings(p.getCurrent(), e);
    }

    public void applyPresentationSettings(TablePresentation p) {
        Element settingsElement = presentations.getSettings(p);
        component.applySettings(settingsElement);
    }

    public void resetPresentations(Document defaultSettings) {
        if (defaultSettings != null) {
            component.applySettings(defaultSettings.getRootElement());
            if (presentations != null) {
                presentations.setCurrent(null);
            }
        }
    }
}
