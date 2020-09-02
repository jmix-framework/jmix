/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.reports.gui.definition.edit.scripteditordialog;

import com.haulmont.cuba.gui.components.*;
import io.jmix.ui.Dialogs;
import io.jmix.ui.WindowParam;
import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.HasContextHelp;
import io.jmix.ui.component.autocomplete.Suggester;
import io.jmix.ui.settings.UserSettingService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;
import java.util.function.Consumer;

public class ScriptEditorDialog extends AbstractWindow {

    public static final String WIDTH_SCRIPT_EDITOR_DIALOG = "reporting.widthScriptEditorDialog";
    public static final String HEIGHT_SCRIPT_EDITOR_DIALOG = "reporting.heightScriptEditorDialog";
    public static final String FULL = "full";

    @WindowParam
    protected SourceCodeEditor.Mode mode;

    @WindowParam
    protected Suggester suggester;

    @WindowParam
    protected String scriptValue;

    @WindowParam
    protected Consumer<HasContextHelp.ContextHelpIconClickEvent> helpHandler;

    @Autowired
    protected SourceCodeEditor editor;

    @Autowired
    protected UserSettingService userSettingService;

    @Override
    public void init(Map<String, Object> params) {
        initEditor();
        initActions();
        loadParameterWindow();

        Object caption = params.get("caption");
        if (ObjectUtils.isNotEmpty(caption)) {
            setCaption(caption.toString());
        }

        addAfterCloseListener(afterCloseEvent -> saveParameterWindow());
    }

    protected void initEditor() {
        editor.setMode(mode != null ? mode : SourceCodeEditor.Mode.Text);
        editor.setSuggester(suggester);
        editor.setValue(scriptValue);
        editor.setHandleTabKey(true);
        editor.setContextHelpIconClickHandler(helpHandler);
    }

    protected void initActions() {
        addAction(new AbstractAction("windowCommit") {
            @Override
            public void actionPerform(Component component) {
                close(COMMIT_ACTION_ID);
            }

            @Override
            public String getCaption() {
                return messages.getMainMessage("actions.Ok");
            }
        });
        addAction(new AbstractAction("windowClose") {
            @Override
            public void actionPerform(Component component) {
                close(CLOSE_ACTION_ID);
            }

            @Override
            public String getCaption() {
                return messages.getMainMessage("actions.Cancel");
            }
        });
    }

    protected void loadParameterWindow() {
        String width = userSettingService.loadSetting(WIDTH_SCRIPT_EDITOR_DIALOG);
        String height = userSettingService.loadSetting(HEIGHT_SCRIPT_EDITOR_DIALOG);

        //TODO dialog params
        if (StringUtils.equals(FULL, height) && StringUtils.equals(FULL, width)) {
//            getDialogOptions().setMaximized(true);
            return;
        }
        if (NumberUtils.isCreatable(width) && NumberUtils.isCreatable(height)) {
//            getDialogOptions().setWidth(width);
//            getDialogOptions().setHeight(height);
        }
    }

    protected void saveParameterWindow() {
//        if (getDialogOptions().getMaximized()) {
//            userSettingService.saveSetting(WIDTH_SCRIPT_EDITOR_DIALOG, FULL);
//            userSettingService.saveSetting(HEIGHT_SCRIPT_EDITOR_DIALOG, FULL);
//        } else {
//            userSettingService.saveSetting(WIDTH_SCRIPT_EDITOR_DIALOG, String.valueOf(getDialogOptions().getWidth()));
//            userSettingService.saveSetting(HEIGHT_SCRIPT_EDITOR_DIALOG, String.valueOf(getDialogOptions().getHeight()));
//        }
    }

    public String getValue() {
        return editor.getValue();
    }
}
