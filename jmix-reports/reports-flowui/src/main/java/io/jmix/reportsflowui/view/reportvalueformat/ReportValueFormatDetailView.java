package io.jmix.reportsflowui.view.reportvalueformat;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import io.jmix.core.Metadata;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.codeeditor.CodeEditor;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.kit.component.codeeditor.CodeEditorMode;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.jmix.reports.entity.ReportValueFormat;
import io.jmix.reportsflowui.ReportsUiHelper;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Route(value = "reports/valueFormats/:id", layout = DefaultMainViewParent.class)
@ViewController("report_ReportValueFormat.detail")
@ViewDescriptor("report-value-format-detail-view.xml")
@EditedEntityContainer("valuesFormatsDc")
@DialogMode(width = "40em")
public class ReportValueFormatDetailView extends StandardDetailView<ReportValueFormat> {

    protected static final String RETURN_VALUE = "return value";

    protected static final String[] defaultFormats = new String[]{
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
    protected JmixComboBox<String> formatField;
    @ViewComponent
    protected JmixCheckbox groovyField;
    @ViewComponent
    protected CodeEditor groovyCodeEditor;
    @ViewComponent
    protected Div groovyCodeEditorBox;

    @ViewComponent
    protected InstanceContainer<ReportValueFormat> valuesFormatsDc;

    @Autowired
    protected SecureOperations secureOperations;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected PolicyStore policyStore;
    @Autowired
    protected Dialogs dialogs;
    @Autowired
    protected MessageBundle messageBundle;
    @Autowired
    protected ReportsUiHelper reportsUiHelper;

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

    @Subscribe("groovyField")
    public void onGroovyFieldComponentValueChange(AbstractField.ComponentValueChangeEvent<Checkbox, Boolean> event) {
        Boolean visible = event.getValue();
        Boolean prevVisible = event.getOldValue();

        Boolean userOriginated = event.isFromClient();

        if (isClickTrueGroovyScript(visible, prevVisible, userOriginated)) {
            groovyCodeEditor.setValue(RETURN_VALUE);
        }
        if (Boolean.FALSE.equals(visible)) {
            formatField.clear();
        }
        groovyCodeEditorBox.setVisible(Boolean.TRUE.equals(visible));
        formatField.setVisible(Boolean.FALSE.equals(visible));
    }

    @Subscribe("groovyCodeEditorHelpBtn")
    public void onGroovyCodeEditorHelpBtnClick(final ClickEvent<Button> event) {
        onGroovyCodeHelpIconClick();
    }

    @Subscribe("fullScreenTransformationBtn")
    public void onFullScreenTransformationBtnClick(final ClickEvent<Button> event) {
        reportsUiHelper.showScriptEditorDialog(
                messageBundle.getMessage("fullScreenBtn.title"),
                valuesFormatsDc.getItem().getFormatString(),
                value -> valuesFormatsDc.getItem().setFormatString(value),
                CodeEditorMode.GROOVY,
                icon -> onGroovyCodeHelpIconClick()
        );
    }

    protected void onGroovyCodeHelpIconClick() {
        dialogs.createMessageDialog()
                .withHeader(messageBundle.getMessage("valuesFormats.groovyScript"))
                .withContent(new Html(messageBundle.getMessage("valuesFormats.groovyScriptHelpText")))
                .withResizable(true)
                .withModal(false)
                .withWidth("50em")
                .open();
    }

    protected void initFormatComboBox() {
        formatField.setItems(Arrays.asList(defaultFormats));

        formatField.addCustomValueSetListener(event -> {
            String text = event.getDetail();
            addFormatItem(text);
            formatField.setValue(text);
        });

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