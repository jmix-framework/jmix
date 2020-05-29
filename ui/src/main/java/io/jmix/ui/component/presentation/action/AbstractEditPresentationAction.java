/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.component.presentation.action;

import io.jmix.core.DevelopmentException;
import io.jmix.ui.AppUI;
import io.jmix.ui.component.presentation.PresentationEditor;
import io.jmix.ui.component.HasTablePresentations;
import io.jmix.ui.component.Table;
import io.jmix.ui.presentation.model.TablePresentation;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.settings.component.binder.ComponentSettingsBinder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class AbstractEditPresentationAction extends AbstractPresentationAction {

    protected Class<? extends PresentationEditor> editorClass;

    public AbstractEditPresentationAction(Table table, String id, ComponentSettingsBinder settingsBinder) {
        super(table, id, settingsBinder);
    }

    protected void openEditor(TablePresentation presentation) {
        PresentationEditor window = createEditor(presentation, settingsBinder);
        AppUI.getCurrent().addWindow(window);
        window.center();
    }

    protected PresentationEditor createEditor(TablePresentation presentation, ComponentSettingsBinder settingsBinder) {
        Class<? extends PresentationEditor> windowClass = getPresentationEditorClass();
        try {
            Constructor<? extends PresentationEditor> windowConstructor = windowClass.getConstructor(
                    FrameOwner.class,
                    TablePresentation.class,
                    HasTablePresentations.class,
                    ComponentSettingsBinder.class);

            return windowConstructor.newInstance(table.getFrame().getFrameOwner(), presentation, table, settingsBinder);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new DevelopmentException("Invalid presentation's screen");
        }
    }

    protected Class<? extends PresentationEditor> getPresentationEditorClass() {
        return editorClass == null ? PresentationEditor.class : editorClass;
    }

    public void setEditorClass(Class<? extends PresentationEditor> editorClass) {
        this.editorClass = editorClass;
    }
}
