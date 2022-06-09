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

package io.jmix.reportsui.screen.template.edit;

import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.reports.ReportPrintHelper;
import io.jmix.reports.entity.CustomTemplateDefinedBy;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reportsui.screen.definition.edit.scripteditordialog.ScriptEditorDialog;
import io.jmix.reportsui.screen.report.run.ShowChartScreen;
import io.jmix.reportsui.screen.report.run.ShowPivotTableScreen;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.component.*;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import io.jmix.ui.upload.TemporaryStorage;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.*;

@UiController("report_ReportTemplate.edit")
@UiDescriptor("template-edit.xml")
@EditedEntityContainer("templateDc")
public class TemplateEditor extends StandardEditor<ReportTemplate> {

    public static final String CUSTOM_DEFINE_BY = "customDefinedBy";
    public static final String CUSTOM = "custom";
    public static final String REPORT_OUTPUT_TYPE = "reportOutputType";

    @Autowired
    protected Label<String> isCustomLabel;

    @Autowired
    protected CheckBox customField;

    @Autowired
    protected Label<String> templateFileLabel;

    @Autowired
    private FileUploadField templateUploadField;

    @Autowired
    protected RadioButtonGroup<Boolean> isGroovyRadioButtonGroup;

    @Autowired
    protected Label<String> isGroovyLabel;

    @Autowired
    protected TextArea<String> customDefinitionField;

    @Autowired
    protected LinkButton customDefinitionHelpLinkButton;

    @Autowired
    protected LinkButton fullScreenLinkButton;

    @Autowired
    protected Label<String> customDefinitionLabel;

    @Autowired
    protected ComboBox<CustomTemplateDefinedBy> customDefinedByField;

    @Autowired
    protected Label<String> customDefinedByLabel;

    @Autowired
    protected CheckBox alterableField;

    @Autowired
    protected Label<String> alterableLabel;

    @Autowired
    protected ComboBox<ReportOutputType> outputTypeField;

    @Autowired
    protected TextField<String> outputNamePatternField;

    @Autowired
    protected Label<String> outputNamePatternLabel;

    @Autowired
    protected ChartEditFragment chartEditFragment;

    @Autowired
    protected PivotTableEditFragment pivotTableEditFragment;

    @Autowired
    protected TableEditFragment tableEditFragment;

    @Autowired
    protected InstanceContainer<ReportTemplate> templateDc;

    @Autowired
    protected BoxLayout descriptionEditBox;

    @Autowired
    protected BoxLayout previewBox;

    @Autowired
    protected SourceCodeEditor templateFileEditor;

    @Autowired
    protected WindowConfig windowConfig;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected SecureOperations secureOperations;

    @Autowired
    protected PolicyStore policyStore;

    @Autowired
    protected TemporaryStorage temporaryStorage;

    @Autowired
    protected ScreenBuilders screenBuilders;

    @Autowired
    protected Dialogs dialogs;

    @Autowired
    protected Messages messages;

    @Autowired
    protected Notifications notifications;

    @Subscribe
    protected void onInit(InitEvent event) {
        Map<String, Boolean> groovyOptions = new HashMap<>();
        groovyOptions.put(messages.getMessage(getClass(), "template.freemarkerType"), Boolean.FALSE);
        groovyOptions.put(messages.getMessage(getClass(), "template.groovyType"), Boolean.TRUE);
        isGroovyRadioButtonGroup.setOptionsMap(groovyOptions);
    }

    @Install(to = "outputNamePatternLabel", subject = "contextHelpIconClickHandler")
    protected void outputNamePatternLabelContextHelpIconClickHandler(HasContextHelp.ContextHelpIconClickEvent contextHelpIconClickEvent) {
        dialogs.createMessageDialog()
                .withCaption(messages.getMessage("template.namePatternText"))
                .withMessage(messages.getMessage("template.namePatternTextHelp"))
                .withContentMode(ContentMode.HTML)
                .withModal(false)
                .withWidth("560px")
                .show();
    }

    @Subscribe
    protected void initNewItem(InitEntityEvent<ReportTemplate> event) {
        ReportTemplate template = event.getEntity();
        if (StringUtils.isEmpty(template.getCode())) {
            Report report = template.getReport();
            if (report != null) {
                if (report.getTemplates() == null || report.getTemplates().isEmpty()) {
                    template.setCode(ReportTemplate.DEFAULT_TEMPLATE_CODE);
                } else {
                    template.setCode("Template_" + report.getTemplates().size());
                }
            }
        }
    }

    @Subscribe
    protected void onAfterInit(AfterInitEvent event) {
        templateDc.addItemPropertyChangeListener(e -> {
            ReportTemplate reportTemplate = getEditedEntity();
            switch (e.getProperty()) {
                case REPORT_OUTPUT_TYPE: {
                    ReportOutputType prevOutputType = (ReportOutputType) e.getPrevValue();
                    ReportOutputType newOutputType = (ReportOutputType) e.getValue();
                    setupVisibility(reportTemplate.getCustom(), newOutputType);
                    if (hasHtmlCsvTemplateOutput(prevOutputType) && !hasTemplateOutput(newOutputType)) {
                        dialogs.createMessageDialog()
                                .withCaption(messages.getMessage(getClass(), "templateEditor.warning"))
                                .withMessage(messages.getMessage(getClass(), "templateEditor.clearTemplateMessage"))
                                .show();
                    }
                    break;
                }
                case CUSTOM: {
                    setupVisibility(Boolean.TRUE.equals(e.getValue()), reportTemplate.getReportOutputType());
                    break;
                }
                case CUSTOM_DEFINE_BY: {
                    boolean isGroovyScript = hasScriptCustomDefinedBy(reportTemplate.getCustomDefinedBy());
                    fullScreenLinkButton.setVisible(isGroovyScript);
                    customDefinitionHelpLinkButton.setVisible(isGroovyScript);
                    break;
                }
            }
        });
        initOutputTypeList();
    }

    protected boolean hasScriptCustomDefinedBy(CustomTemplateDefinedBy customTemplateDefinedBy) {
        return CustomTemplateDefinedBy.SCRIPT == customTemplateDefinedBy;
    }

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        initUploadField();

        ReportTemplate reportTemplate = getEditedEntity();
        initTemplateEditor(reportTemplate);
        getDescriptionEditFragments().forEach(controller -> controller.setItem(reportTemplate));
        setupVisibility(reportTemplate.getCustom(), reportTemplate.getReportOutputType());
    }

    @Subscribe("templateUploadField")
    protected void onTemplateUploadFieldFileUploadStart(UploadField.FileUploadStartEvent event) {
        templateUploadField.setFileName(event.getFileName());
    }

    protected Collection<DescriptionEditFragment> getDescriptionEditFragments() {
        return Arrays.asList(chartEditFragment, pivotTableEditFragment, tableEditFragment);
    }

    protected boolean hasTemplateOutput(ReportOutputType reportOutputType) {
        return reportOutputType != ReportOutputType.CHART
                && reportOutputType != ReportOutputType.TABLE
                && reportOutputType != ReportOutputType.PIVOT_TABLE;
    }

    protected boolean hasChartTemplateOutput(ReportOutputType reportOutputType) {
        return reportOutputType == ReportOutputType.CHART;
    }

    protected boolean hasHtmlCsvTemplateOutput(@Nullable ReportOutputType reportOutputType) {
        return reportOutputType == ReportOutputType.CSV || reportOutputType == ReportOutputType.HTML;
    }

    protected void setupVisibility(boolean customEnabled, ReportOutputType reportOutputType) {
        boolean templateOutputVisibility = hasTemplateOutput(reportOutputType);
        boolean enabled = templateOutputVisibility && customEnabled;
        boolean groovyScriptVisibility = enabled && hasScriptCustomDefinedBy(getEditedEntity().getCustomDefinedBy());

        customField.setVisible(templateOutputVisibility);
        isCustomLabel.setVisible(templateOutputVisibility);

        customDefinedByField.setVisible(enabled);
        customDefinitionField.setVisible(enabled);
        customDefinedByLabel.setVisible(enabled);
        customDefinitionLabel.setVisible(enabled);

        customDefinitionHelpLinkButton.setVisible(groovyScriptVisibility);
        fullScreenLinkButton.setVisible(groovyScriptVisibility);

        customDefinedByField.setRequired(enabled);
        customDefinedByField.setRequiredMessage(messages.getMessage(getClass(), "templateEditor.customDefinedBy"));
        customDefinitionField.setRequired(enabled);
        customDefinitionField.setRequiredMessage(messages.getMessage(getClass(), "templateEditor.classRequired"));

        boolean supportAlterableForTemplate = templateOutputVisibility && !enabled;
        alterableField.setVisible(supportAlterableForTemplate);
        alterableLabel.setVisible(supportAlterableForTemplate);

        templateUploadField.setVisible(templateOutputVisibility);
        templateFileLabel.setVisible(templateOutputVisibility);
        outputNamePatternField.setVisible(templateOutputVisibility);
        outputNamePatternLabel.setVisible(templateOutputVisibility);

        setupTemplateTypeVisibility(templateOutputVisibility);
        setupVisibilityDescriptionEdit(enabled, reportOutputType);
    }

    protected void setupTemplateTypeVisibility(boolean visibility) {
        String extension = "";
        if (getEditedEntity().getDocumentName() != null) {
            extension = FilenameUtils.getExtension(getEditedEntity().getDocumentName()).toUpperCase();
        }
        isGroovyRadioButtonGroup.setVisible(visibility
                && ReportOutputType.HTML.equals(ReportOutputType.getTypeFromExtension(extension)));
        isGroovyLabel.setVisible(visibility
                && ReportOutputType.HTML.equals(ReportOutputType.getTypeFromExtension(extension)));
    }

    protected void setupVisibilityDescriptionEdit(boolean customEnabled, ReportOutputType reportOutputType) {
        DescriptionEditFragment applicableFragment =
                getDescriptionEditFragments().stream()
                        .filter(c -> c.isApplicable(reportOutputType))
                        .findFirst().orElse(null);
        if (applicableFragment != null) {
            descriptionEditBox.setVisible(!customEnabled);
            applicableFragment.setVisible(!customEnabled);
            applicableFragment.setItem(getEditedEntity());

            if (!customEnabled && applicableFragment.isSupportPreview()) {
                applicableFragment.showPreview();
            } else {
                applicableFragment.hidePreview();
            }
        }

        for (DescriptionEditFragment fragment : getDescriptionEditFragments()) {
            if (applicableFragment != fragment) {
                fragment.setVisible(false);
            }
            if (applicableFragment == null) {
                fragment.hidePreview();
                descriptionEditBox.setVisible(false);
            }
        }
    }

    protected void updateOutputType() {
        if (outputTypeField.getValue() == null) {
            String extension = FilenameUtils.getExtension(templateUploadField.getFileName()).toUpperCase();
            ReportOutputType reportOutputType = ReportOutputType.getTypeFromExtension(extension);
            if (reportOutputType != null) {
                outputTypeField.setValue(reportOutputType);
            }
        }
    }

    protected void initOutputTypeList() {
        ArrayList<ReportOutputType> outputTypes = new ArrayList<>(Arrays.asList(ReportOutputType.values()));

        if (!windowConfig.hasWindow(ShowChartScreen.JSON_CHART_SCREEN_ID)) {
            outputTypes.remove(ReportOutputType.CHART);
        }
        if (!windowConfig.hasWindow(ShowPivotTableScreen.PIVOT_TABLE_SCREEN_ID)) {
            outputTypes.remove(ReportOutputType.PIVOT_TABLE);
        }

        outputTypeField.setOptionsList(outputTypes);
    }

    @Subscribe("templateUploadField")
    protected void onTemplateUploadFieldFileUploadError(UploadField.FileUploadErrorEvent event) {
        notifications.create(Notifications.NotificationType.WARNING)
                .withCaption(messages.getMessage(getClass(), "templateEditor.uploadUnsuccess"))
                .show();
    }

    @Subscribe("templateUploadField")
    protected void onTemplateUploadFieldFileUploadSucceed(SingleFileUploadField.FileUploadSucceedEvent event) {
        String fileName = templateUploadField.getFileName();
        ReportTemplate reportTemplate = getEditedEntity();
        reportTemplate.setName(fileName);

        byte[] data = templateUploadField.getValue();
        reportTemplate.setContent(data);
        initTemplateEditor(reportTemplate);
        setupTemplateTypeVisibility(hasTemplateOutput(reportTemplate.getReportOutputType()));
        updateOutputType();

        notifications.create(Notifications.NotificationType.TRAY)
                .withCaption(messages.getMessage(getClass(), "templateEditor.uploadSuccess"))
                .show();
    }

    protected void initUploadField() {
        ReportTemplate reportTemplate = getEditedEntity();
        byte[] templateFile = reportTemplate.getContent();
        if (templateFile != null && !hasChartTemplateOutput(reportTemplate.getReportOutputType())) {
            temporaryStorage.saveFile(templateFile);
            templateUploadField.setValue(templateFile);
            templateUploadField.setFileName(reportTemplate.getName());
        }

        templateUploadField.setEditable(isUpdatePermitted(reportTemplate));
    }

    protected boolean isUpdatePermitted(ReportTemplate reportTemplate) {
        return secureOperations.isEntityUpdatePermitted(metadata.getClass(reportTemplate), policyStore)
                && secureOperations.isEntityAttrUpdatePermitted(metadata.getClass(reportTemplate).getPropertyPath("content"), policyStore);
    }

    protected void initTemplateEditor(ReportTemplate reportTemplate) {
        templateFileEditor.setMode(SourceCodeEditor.Mode.HTML);
        String extension = FilenameUtils.getExtension(templateUploadField.getFileName());
        if (extension == null) {
            visibleTemplateEditor(null);
            return;
        }
        ReportOutputType outputType = ReportOutputType.getTypeFromExtension(extension.toUpperCase());
        visibleTemplateEditor(outputType);
        if (hasHtmlCsvTemplateOutput(outputType)) {
            String templateContent = new String(reportTemplate.getContent(), StandardCharsets.UTF_8);
            templateFileEditor.setValue(templateContent);
        }
        templateFileEditor.setEditable(secureOperations.isEntityUpdatePermitted(metadata.getClass(reportTemplate), policyStore));
    }

    protected void visibleTemplateEditor(@Nullable ReportOutputType outputType) {
        String extension = FilenameUtils.getExtension(templateUploadField.getFileName());
        if (extension == null) {
            templateFileEditor.setVisible(false);
            return;
        }
        templateFileEditor.setVisible(hasHtmlCsvTemplateOutput(outputType));
    }

    @Subscribe
    protected void onBeforeCommit(BeforeCommitChangesEvent event) {
        if (!validateTemplateFile() || !validateInputOutputFormats()) {
            event.preventCommit();
        }
        ReportTemplate reportTemplate = getEditedEntity();
        for (DescriptionEditFragment fragment : getDescriptionEditFragments()) {
            if (fragment.isApplicable(reportTemplate.getReportOutputType())) {
                if (!fragment.applyChanges()) {
                    event.preventCommit();
                }
            }
        }

        if (!Boolean.TRUE.equals(reportTemplate.getCustom())) {
            reportTemplate.setCustomDefinition("");
        }

        String extension = FilenameUtils.getExtension(templateUploadField.getFileName());
        if (extension != null) {
            ReportOutputType outputType = ReportOutputType.getTypeFromExtension(extension.toUpperCase());
            if (hasHtmlCsvTemplateOutput(outputType)) {
                byte[] bytes = templateFileEditor.getValue() == null ?
                        new byte[0] :
                        templateFileEditor.getValue().getBytes(StandardCharsets.UTF_8);
                reportTemplate.setContent(bytes);
            }
        }
    }

    protected boolean validateInputOutputFormats() {
        ReportTemplate reportTemplate = getEditedEntity();
        String name = reportTemplate.getName();
        if (!Boolean.TRUE.equals(reportTemplate.getCustom())
                && hasTemplateOutput(reportTemplate.getReportOutputType())
                && name != null) {
            String inputType = name.contains(".") ? name.substring(name.lastIndexOf(".") + 1) : "";

            ReportOutputType outputTypeValue = outputTypeField.getValue();
            if (!ReportPrintHelper.getInputOutputTypesMapping().containsKey(inputType) ||
                    !ReportPrintHelper.getInputOutputTypesMapping().get(inputType).contains(outputTypeValue)) {
                notifications.create(Notifications.NotificationType.TRAY)
                        .withCaption(messages.getMessage(getClass(), "inputOutputTypesError"))
                        .show();
                return false;
            }
        }
        return true;
    }

    protected boolean validateTemplateFile() {
        ReportTemplate template = getEditedEntity();
        if (!Boolean.TRUE.equals(template.getCustom())
                && hasTemplateOutput(template.getReportOutputType())
                && template.getContent() == null) {
            StringBuilder notification = new StringBuilder(messages.getMessage(getClass(), "template.uploadTemplate"));

            if (StringUtils.isEmpty(template.getCode())) {
                notification.append("\n").append(messages.getMessage(getClass(), "template.codeMsg"));
            }

            if (template.getOutputType() == null) {
                notification.append("\n").append(messages.getMessage(getClass(), "template.outputTypeMsg"));
            }

            notifications.create(Notifications.NotificationType.TRAY)
                    .withCaption(messages.getMessage("validationFail.caption"))
                    .withDescription(notification.toString())
                    .show();

            return false;
        }
        return true;
    }

    @Subscribe("fullScreenLinkButton")
    protected void showGroovyScriptEditorDialog(Button.ClickEvent event) {
        ScriptEditorDialog editorDialog = screenBuilders.screen(this)
                .withScreenClass(ScriptEditorDialog.class)
                .withOpenMode(OpenMode.DIALOG)
                .withOptions(new MapScreenOptions(ParamsMap.of(
                        "mode", SourceCodeEditor.Mode.Groovy,
                        "scriptValue", customDefinitionField.getValue(),
                        "helpVisible", customDefinitionHelpLinkButton.isVisible(),
                        "helpMsgKey", "templateEditor.textHelpGroovy"
                )))
                .build();
        editorDialog.addAfterCloseListener(actionId -> {
            StandardCloseAction closeAction = (StandardCloseAction) actionId.getCloseAction();
            if (Window.COMMIT_ACTION_ID.equals(closeAction.getActionId())) {
                customDefinitionField.setValue(editorDialog.getValue());
            }
        });
        editorDialog.show();
    }

    @Subscribe("customDefinitionHelpLinkButton")
    public void onCustomDefinitionHelpLinkButtonClick(Button.ClickEvent event) {
        dialogs.createMessageDialog()
                .withCaption(messages.getMessage(getClass(), "templateEditor.titleHelpGroovy"))
                .withMessage(messages.getMessage(getClass(), "templateEditor.textHelpGroovy"))
                .withModal(false)
                .withContentMode(ContentMode.HTML)
                .withWidth("700px")
                .show();
    }
}