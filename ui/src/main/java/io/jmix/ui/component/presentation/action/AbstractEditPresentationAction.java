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

import org.springframework.context.ApplicationContext;
import io.jmix.ui.AppUI;
import io.jmix.ui.component.presentation.PresentationEditor;
import io.jmix.ui.component.Table;
import io.jmix.ui.presentation.model.TablePresentation;
import io.jmix.ui.settings.component.binder.ComponentSettingsBinder;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractEditPresentationAction extends AbstractPresentationAction {

    protected Class<? extends PresentationEditor> editorClass;

    @Autowired
    protected ApplicationContext applicationContext;

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

        return applicationContext.getBean(windowClass,
                table.getFrame().getFrameOwner(),
                presentation,
                table,
                settingsBinder);
    }

    protected Class<? extends PresentationEditor> getPresentationEditorClass() {
        return editorClass == null ? PresentationEditor.class : editorClass;
    }

    /**
     * Sets Editor class that should be opened.
     * <p>
     * Note, editor class should be a PROTOTYPE bean.
     *
     * @param editorClass editor class
     * @see PresentationEditor
     */
    public void setEditorClass(Class<? extends PresentationEditor> editorClass) {
        this.editorClass = editorClass;
    }
}
