/*
 * Copyright 2021 Haulmont.
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

package io.jmix.reportsui.action.list;

import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.reportsui.action.AbstractPrintFormAction;
import io.jmix.ui.Notifications;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.component.Component;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.screen.EditorScreen;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;

@ActionType(EditorPrintFormAction.ID)
public class EditorPrintFormAction extends AbstractPrintFormAction {

    public static final String ID = "editorPrintForm";

    protected EditorScreen editor;
    protected String reportOutputName;

    public EditorPrintFormAction() {
        this(ID);
    }

    public EditorPrintFormAction(String id) {
        super(id);
        this.icon = JmixIcon.PRINT.source();
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.caption = messages.getMessage(getClass(), "actions.Report");
    }

    public void setEditor(@Nullable EditorScreen editor) {
        this.editor = editor;
    }

    public void setReportOutputName(@Nullable String reportOutputName) {
        this.reportOutputName = reportOutputName;
    }

    @Override
    public void actionPerform(Component component) {
        Object entity = editor.getEditedEntity();
        if (entity != null) {
            MetaClass metaClass = metadata.getClass(entity);
            openRunReportScreen((Screen) editor, entity, metaClass, reportOutputName);
        } else {
            Notifications notifications = UiControllerUtils.getScreenContext((FrameOwner) editor).getNotifications();

            notifications.create()
                    .withCaption(messages.getMessage(getClass(), "notifications.noSelectedEntity"))
                    .show();
        }
    }
}