package io.jmix.reportsflowui.view.reportvalueformatdetail;


import io.jmix.reportsflowui.view.main.MainView;
import io.jmix.reportsflowui.view.scripteditor.ScriptEditorView;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.view.*;
import io.jmix.reports.entity.ReportValueFormat;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Route(value = "ReportValueFormatDetailView/:id", layout = MainView.class)
@ViewController("report_ReportValueFormat.detail")
@ViewDescriptor("report-value-format-detail-view.xml")
@EditedEntityContainer("valuesFormatsDc")
public class ReportValueFormatDetailView extends StandardDetailView<ReportValueFormat> {
    public static final String RETURN_VALUE = "return value";

    protected static final String FIELD_ICON_CLASS_NAME = "template-detailview-field-icon";

    protected String[] defaultFormats = new String[]{
            "#,##0",
            "##,##0",
            "#,##0.###",
            "#,##0.##",
            "#,##0.00",
            "dd/MM/yyyy HH:mm",
            "${image:WxH}",
            "${bitmap:WxH}",
            "${imageFileId:WxH}",
            "${html}",
            "class:"
    };

    @ViewComponent
    private JmixComboBox<String> formatField;
    @ViewComponent
    private JmixCheckbox groovyField;
    @Autowired
    private SecureOperations secureOperations;
    @Autowired
    private Metadata metadata;
    @Autowired
    private PolicyStore policyStore;
    @ViewComponent
    private VerticalLayout groovyVBox;
    @ViewComponent
    private JmixTextArea groovyCodeEditor;
    @Autowired
    private DialogWindows dialogWindows;
    @Autowired
    private Dialogs dialogs;
    @Autowired
    private Messages messages;

    @Subscribe
    public void onInit(InitEvent event) {
        initFormatComboBox();
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        String value = formatField.getValue();
        if (value != null) {
            List<String> optionsList = formatField.getListDataView().getItems()
                    .toList();


            if (!optionsList.contains(value) && Boolean.FALSE.equals(groovyField.getValue())) {
                addFormatItem(value);
            }
            formatField.setValue(value);
        }
    }


    @Subscribe
    public void onBeforeSave(BeforeSaveEvent event) {
        getEditedEntity().setFormatString(formatField.getValue());
    }


    @Subscribe("groovyFullScreenLinkButton")
    protected void showGroovyEditorDialog(ClickEvent<Button> event) {


        DialogWindow<ScriptEditorView> editorDialog = dialogWindows.view(this, ScriptEditorView.class).open();
//todo an return with code editor
//        editorDialog.addAfterCloseListener();
//
//                .withAfterCloseListener(actionId-> {
//                    StandardCloseAction closeAction = (StandardCloseAction) actionId.getCloseAction();
//                    if (COMMIT_ACTION_ID.equals(closeAction.getActionId())) {
//                        groovyCodeEditor.setValue(editorDialog.getValue());
//                    }
//                })
// поменять на code editor когда его доделают
//        String reportName = document.getDocumentName();
//        ScriptEditorDialog editorDialog = screenBuilders.screen(this)
//                .withScreenClass(ScriptEditorDialog.class)
//                .withOpenMode(OpenMode.DIALOG)
//                .withOptions(new MapScreenOptions(ParamsMap.of(
//                        "mode", groovyScriptFieldMode,
//                        "suggester", groovyCodeEditor.getSuggester(),
//                        "scriptValue", groovyCodeEditor.getValue(),
//                        "helpVisible", groovyCodeEditor.isVisible(),
//                        "helpMsgKey", "valuesFormats.groovyScriptHelpText"
//                )))
//                .build();
//        editorDialog.addAfterCloseListener(actionId -> {
//            StandardCloseAction closeAction = (StandardCloseAction) actionId.getCloseAction();
//            closeAction
//            if (.equals(closeAction.getActionId())) {
//                groovyCodeEditor.setValue(editorDialog.getValue());
//            }
//        });
//        editorDialog.show();
    }

    @Subscribe("groovyField")
    public void onGroovyFieldComponentValueChange
            (AbstractField.ComponentValueChangeEvent<Checkbox, Boolean> event) {
        Boolean visible = event.getValue();
        Boolean prevVisible = event.getOldValue();

        Boolean userOriginated = event.isFromClient();

        if (isClickTrueGroovyScript(visible, prevVisible, userOriginated)) {
            groovyCodeEditor.setValue(RETURN_VALUE);
        }
        if (Boolean.FALSE.equals(visible)) {
            formatField.clear();
        }
        groovyVBox.setVisible(Boolean.TRUE.equals(visible));
        formatField.setVisible(Boolean.FALSE.equals(visible));

    }


    //todo an
//    @Install(to = "groovyField", subject = "contextHelpIconClickHandler")
//    protected void groovyCheckBoxContextHelpIconClickHandler(HasContextHelp.ContextHelpIconClickEvent
//                                                                     contextHelpIconClickEvent) {
//        dialogs.createMessageDialog()
//                .withHeader(messages.getMessage("valuesFormats.groovyScript"))
//                .withText(messages.getMessage("valuesFormats.groovyScriptHelpText"))
//                .withModal(false)
//                .withWidth("700px")
//                .open();
//    }

    protected void initFormatComboBox() {
        formatField.setItems(Arrays.asList(defaultFormats));

        formatField.setEnabled(secureOperations.isEntityUpdatePermitted(metadata.getClass(ReportValueFormat.class), policyStore));
    }

    protected boolean isClickTrueGroovyScript(@Nullable Boolean visible, @Nullable Boolean prevVisible, Boolean
            userOriginated) {
        return Boolean.TRUE.equals(userOriginated) && Boolean.TRUE.equals(visible) && Boolean.FALSE.equals(prevVisible);
    }

    protected void addFormatItem(String caption) {
        List<String> optionsList = new ArrayList<>(formatField.getListDataView().getItems()
                .toList());
        optionsList.add(caption);

        formatField.setItems(optionsList);
    }
}