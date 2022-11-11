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
package io.jmix.reportsui.screen.valueformat.edit;

import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.reports.entity.ReportValueFormat;
import io.jmix.reportsui.screen.definition.edit.scripteditordialog.ScriptEditorDialog;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.*;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.jmix.ui.component.Window.COMMIT_ACTION_ID;

@UiController("report_ReportValueFormat.edit")
@UiDescriptor("value-format-edit.xml")
@EditedEntityContainer("valuesFormatsDc")
public class ValueFormatEditor extends StandardEditor<ReportValueFormat> {

    public static final String RETURN_VALUE = "return value";

    protected String[] defaultFormats = new String[]{
            "#,##0",
            "##,##0",
            "#,##0.###",
            "#,##0.##",
            "dd/MM/yyyy HH:mm",
            "${image:WxH}",
            "${bitmap:WxH}",
            "${imageFileId:WxH}",
            "${html}",
            "class:"
    };

    @Autowired
    protected ComboBox<String> formatField;

    @Autowired
    protected Form formatForm;

    @Autowired
    protected CheckBox groovyField;

    @Autowired
    protected LinkButton groovyFullScreenLinkButton;

    @Autowired
    protected VBoxLayout groovyVBox;

    @Autowired
    protected SourceCodeEditor groovyCodeEditor;

    @Autowired
    protected InstanceContainer<ReportValueFormat> valuesFormatsDc;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected SecureOperations secureOperations;

    @Autowired
    protected PolicyStore policyStore;

    @Autowired
    protected Notifications notifications;

    @Autowired
    protected Dialogs dialogs;

    @Autowired
    protected MessageBundle messageBundle;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected ScreenBuilders screenBuilders;

    @Autowired
    protected UiComponents uiComponents;

    protected SourceCodeEditor.Mode groovyScriptFieldMode = SourceCodeEditor.Mode.Groovy;

    @Subscribe
    protected void onInit(InitEvent event) {
        // Add default format strings to comboBox
        initFormatComboBox();
    }

    @Install(to = "groovyField", subject = "contextHelpIconClickHandler")
    protected void groovyCheckBoxContextHelpIconClickHandler(HasContextHelp.ContextHelpIconClickEvent contextHelpIconClickEvent) {
        dialogs.createMessageDialog()
                .withCaption(messageBundle.getMessage("valuesFormats.groovyScript"))
                .withMessage(messageBundle.getMessage("valuesFormats.groovyScriptHelpText"))
                .withContentMode(ContentMode.HTML)
                .withModal(false)
                .withWidth("700px")
                .show();
    }

    @Subscribe("groovyField")
    protected void onGroovyCheckBoxValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        Boolean visible = event.getValue();
        Boolean prevVisible = event.getPrevValue();

        Boolean userOriginated = event.isUserOriginated();

        if (isClickTrueGroovyScript(visible, prevVisible, userOriginated)) {
            groovyCodeEditor.setValue(RETURN_VALUE);
        }
        if (Boolean.FALSE.equals(visible)) {
            formatField.clear();
        }

        groovyVBox.setVisible(Boolean.TRUE.equals(visible));
        formatField.setVisible(Boolean.FALSE.equals(visible));
    }

    protected void initFormatComboBox() {
        formatField.setOptionsList(Arrays.asList(defaultFormats));

        formatField.setEnterPressHandler(enterPressEvent -> {
            String text = enterPressEvent.getText();
            addFormatItem(text);
            formatField.setValue(text);
        });

        formatField.setEditable(secureOperations.isEntityUpdatePermitted(metadata.getClass(ReportValueFormat.class), policyStore));
    }

    protected boolean isClickTrueGroovyScript(@Nullable Boolean visible, @Nullable Boolean prevVisible, Boolean userOriginated) {
        return Boolean.TRUE.equals(userOriginated) && Boolean.TRUE.equals(visible) && Boolean.FALSE.equals(prevVisible);
    }

    protected void addFormatItem(String caption) {
        //noinspection unchecked
        List<String> optionsList = formatField.getOptions().getOptions()
                .collect(Collectors.toList());
        optionsList.add(caption);

        formatField.setOptionsList(optionsList);
    }

    @Subscribe
    protected void onAfterInit(AfterInitEvent event) {
        String value = formatField.getValue();
        if (value != null) {
            List<String> optionsList = formatField.getOptions().getOptions()
                    .collect(Collectors.toList());

            if (!optionsList.contains(value) && Boolean.FALSE.equals(groovyField.isChecked())) {
                addFormatItem(value);
            }
            formatField.setValue(value);
        }
    }

    @Subscribe
    protected void onBeforeCommitChanges(BeforeCommitChangesEvent event) {
        getEditedEntity().setFormatString(formatField.getValue());
    }

    @Subscribe("groovyFullScreenLinkButton")
    protected void showGroovyEditorDialog(Button.ClickEvent event) {
        ScriptEditorDialog editorDialog = screenBuilders.screen(this)
                .withScreenClass(ScriptEditorDialog.class)
                .withOpenMode(OpenMode.DIALOG)
                .withOptions(new MapScreenOptions(ParamsMap.of(
                        "mode", groovyScriptFieldMode,
                        "suggester", groovyCodeEditor.getSuggester(),
                        "scriptValue", groovyCodeEditor.getValue(),
                        "helpVisible", groovyCodeEditor.isVisible(),
                        "helpMsgKey", "valuesFormats.groovyScriptHelpText"
                )))
                .build();
        editorDialog.addAfterCloseListener(actionId -> {
            StandardCloseAction closeAction = (StandardCloseAction) actionId.getCloseAction();
            if (COMMIT_ACTION_ID.equals(closeAction.getActionId())) {
                groovyCodeEditor.setValue(editorDialog.getValue());
            }
        });
        editorDialog.show();
    }
}
