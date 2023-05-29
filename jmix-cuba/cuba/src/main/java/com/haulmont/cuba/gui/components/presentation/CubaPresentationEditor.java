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

package com.haulmont.cuba.gui.components.presentation;

import com.haulmont.cuba.gui.components.HasSettings;
import io.jmix.core.common.util.Dom4j;
import io.jmix.ui.component.HasTablePresentations;
import io.jmix.ui.component.presentation.PresentationEditor;
import io.jmix.ui.presentation.model.TablePresentation;
import io.jmix.ui.screen.FrameOwner;
import com.haulmont.cuba.settings.CubaLegacySettings;
import io.jmix.ui.settings.component.binder.ComponentSettingsBinder;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component(CubaPresentationEditor.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CubaPresentationEditor extends PresentationEditor {

    public static final String NAME = "cuba_CubaPresentationEditor";

    public CubaPresentationEditor(FrameOwner frameOwner,
                                  TablePresentation presentation,
                                  HasTablePresentations component,
                                  ComponentSettingsBinder settingsBinder) {
        super(frameOwner, presentation, component, settingsBinder);
    }

    @Override
    protected String getStringSettings() {
        if (frameOwner instanceof CubaLegacySettings) {
            Document doc = DocumentHelper.createDocument();
            doc.setRootElement(doc.addElement("presentation"));

            if (component instanceof HasSettings) {
                ((HasSettings) component).saveSettings(doc.getRootElement());
                return Dom4j.writeDocument(doc, false);
            } else {
                throw new IllegalStateException(String.format("Cannot commit presentation." +
                        " Component must implement '%s'", HasSettings.class));
            }
        } else {
            return super.getStringSettings();
        }
    }
}
