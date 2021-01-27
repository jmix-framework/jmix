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
package io.jmix.reportsui.screen.report.wizard;

import com.google.common.collect.ImmutableMap;
import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.reports.app.EntityTree;
import io.jmix.reports.app.service.ReportsWizard;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportGroup;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.charts.ChartType;
import io.jmix.reports.entity.wizard.ReportData;
import io.jmix.reports.entity.wizard.ReportRegion;
import io.jmix.reports.entity.wizard.TemplateFileType;
import io.jmix.reports.exception.TemplateGenerationException;
import io.jmix.reportsui.screen.ReportGuiManager;
import io.jmix.reportsui.screen.report.wizard.step.MainWizardFrame;
import io.jmix.reportsui.screen.report.wizard.step.StepFrame;
import io.jmix.reportsui.screen.report.wizard.step.StepFrameManager;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.component.*;
import io.jmix.ui.model.CollectionChangeType;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import io.jmix.ui.theme.ThemeConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;
import java.util.*;

import static io.jmix.ui.component.Window.COMMIT_ACTION_ID;

@UiController("report_ReportWizardCreator")
@UiDescriptor("report-wizard.xml")
public class ReportWizardCreator extends Screen implements MainWizardFrame<Screen> {

    @Autowired
    protected InstanceContainer<ReportData> reportDataDs;
    @Autowired
    protected CollectionContainer<ReportRegion> reportRegionsDc;
    @Autowired
    protected CollectionContainer<ReportGroup> groupsDs;
    @Named("fwd")
    protected Button fwdBtn;
    @Named("regionsStep.run")
    protected Button runBtn;
    @Named("bwd")
    protected Button bwdBtn;
    @Named("save")
    protected Button saveBtn;
    @Autowired
    protected Label<String> tipLabel;
    @Autowired
    protected BoxLayout editAreaVbox;
    @Autowired
    protected ButtonsPanel navBtnsPanel;
    @Autowired
    protected GroupBoxLayout editAreaGroupBox;
    @Autowired
    protected Dialogs dialogs;
    @Autowired
    protected ScreenBuilders screenBuilders;
    @Autowired
    protected DataManager dataManager;

    @Named("detailsStep.mainFields")
    protected Form mainFields;
    @Named("detailsStep.setQuery")
    protected Button setQueryButton;

    protected RadioButtonGroup reportTypeRadioButtonGroup;//this and following are set during creation
    protected ComboBox<TemplateFileType> templateFileFormat;
    protected ComboBox<MetaClass> entity;
    protected TextField<String> reportName;

    @Named("regionsStep.addRegionDisabledBtn")
    protected Button addRegionDisabledBtn;
    @Named("regionsStep.addTabulatedRegionDisabledBtn")
    protected Button addTabulatedRegionDisabledBtn;
    @Named("regionsStep.addSimpleRegionBtn")
    protected Button addSimpleRegionBtn;
    @Named("regionsStep.addTabulatedRegionBtn")
    protected Button addTabulatedRegionBtn;
    @Named("regionsStep.addRegionPopupBtn")
    protected PopupButton addRegionPopupBtn;
    @Named("regionsStep.moveUpBtn")
    protected Button moveUpBtn;
    @Named("regionsStep.moveDownBtn")
    protected Button moveDownBtn;
    @Named("regionsStep.removeBtn")
    protected Button removeBtn;
    @Named("regionsStep.regionsTable")
    protected Table<ReportRegion> regionsTable;
    @Named("regionsStep.buttonsBox")
    protected BoxLayout buttonsBox;

    @Named("saveStep.outputFileFormat")
    protected ComboBox<ReportOutputType> outputFileFormat;
    @Named("saveStep.outputFileName")
    protected TextField<String> outputFileName;
    @Named("saveStep.downloadTemplateFile")
    protected Button downloadTemplateFile;
    @Named("saveStep.diagramTypeLabel")
    protected Label<String> diagramTypeLabel;
    @Named("saveStep.diagramType")
    protected ComboBox<ChartType> diagramType;
    @Named("saveStep.chartPreviewBox")
    protected BoxLayout chartPreviewBox;

    @Autowired
    protected ExtendedEntities extendedEntities;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected ReportsWizard reportWizardService;
    @Autowired
    protected ThemeConstants themeConstants;
    @Autowired
    protected ReportGuiManager reportGuiManager;
    @Autowired
    protected Messages messages;
    @Autowired
    protected Notifications notifications;

    protected StepFrame detailsStepFrame;
    protected StepFrame regionsStepFrame;
    protected StepFrame saveStepFrame;
    protected StepFrameManager stepFrameManager;

    protected byte[] lastGeneratedTemplate;
    protected Report lastGeneratedTmpReport;
    protected boolean entityTreeHasSimpleAttrs;
    protected boolean entityTreeHasCollections;
    protected boolean needUpdateEntityModel = false;

    protected String query;
    protected String dataStore;
    protected List<ReportData.Parameter> queryParameters;

    @Subscribe
    @SuppressWarnings("unchecked")
    protected void onInit(InitEvent event) {
        reportDataDs.setItem(metadata.create(ReportData.class));

        stepFrameManager = new StepFrameManager(this, getStepFrames());

        initAvailableFormats();
        initMainButtons();
        initMainFields();

        stepFrameManager.showCurrentFrame();
        tipLabel.setValue(getMessage("enterMainParameters"));

        reportRegionsDc.addCollectionChangeListener(e -> {
            if (e.getChangeType().equals(CollectionChangeType.ADD_ITEMS)) {
                regionsTable.setSelected((Collection) e.getChanges());
            }
        });

        reportRegionsDc.addItemChangeListener(e -> {
            if (regionsTable.getSingleSelected() != null) {
                moveDownBtn.setEnabled(true);
                moveUpBtn.setEnabled(true);
                removeBtn.setEnabled(true);
            }
        });

        outputFileName.setContextHelpIconClickHandler(e ->
                dialogs.createMessageDialog()
                        .withCaption(messages.getMessage("template.namePatternText"))
                        .withMessage(messages.getMessage("template.namePatternTextHelp"))
                        .withModal(false)
                        .withWidth("560px")
                        .show());
    }

    protected void initMainButtons() {
        fwdBtn.setAction(new AbstractAction("fwd") {
            @Override
            public void actionPerform(Component component) {
                if (entity.getValue() == null) {
                    notifications.create(Notifications.NotificationType.TRAY)
                            .withCaption(messages.getMessage("fillEntityMsg"))
                            .show();
                    return;
                }

                if (needUpdateEntityModel) {
                    EntityTree entityTree = reportWizardService.buildEntityTree(entity.getValue());
                    entityTreeHasSimpleAttrs = entityTree.getEntityTreeStructureInfo().isEntityTreeHasSimpleAttrs();
                    entityTreeHasCollections = entityTree.getEntityTreeStructureInfo().isEntityTreeRootHasCollections();
                    entityTree.getEntityTreeRootNode().getLocalizedName();
                    getItem().setEntityTreeRootNode(entityTree.getEntityTreeRootNode());
                    needUpdateEntityModel = false;
                }
                stepFrameManager.nextFrame();
                refreshFrameVisible();
            }
        });
        bwdBtn.setAction(new AbstractAction("bwd") {
            @Override
            public void actionPerform(Component component) {
                stepFrameManager.prevFrame();
                refreshFrameVisible();
            }
        });
    }

    protected void initMainFields() {
        //todo
//        mainFields.addCustomField("entity", (datasource, propertyId) -> {
//            ComboBox comboBox = uiComponents.create(ComboBox.class);
//            //TODO request focus
////            lookupField.requestFocus();
//            entity = comboBox;
//            return comboBox;
//        });
//        mainFields.addCustomField("reportName", (datasource, propertyId) -> {
//            TextField textField = uiComponents.create(TextField.class);
//            textField.setMaxLength(255);
//            reportName = textField;
//            return textField;
//        });
//        mainFields.addCustomField("templateFileFormat", (datasource, propertyId) -> {
//            ComboBox comboBox = uiComponents.create(ComboBox.class);
//            templateFileFormat = comboBox;
//            return comboBox;
//        });
//        mainFields.addCustomField("reportType", (datasource, propertyId) -> {
//            RadioButtonGroup radioButtonGroup = uiComponents.create(RadioButtonGroup.class);
//            radioButtonGroup.setOrientation(OptionsGroup.Orientation.VERTICAL);
//            reportTypeRadioButtonGroup = radioButtonGroup;
//            return radioButtonGroup;
//        });
    }

    protected void refreshFrameVisible() {
        if (detailsStepFrame.getFrame().isVisible()) {
            tipLabel.setValue(getMessage("enterMainParameters"));
            editAreaVbox.add(editAreaGroupBox);
            editAreaVbox.remove(regionsStepFrame.getFrame());
            editAreaGroupBox.remove(saveStepFrame.getFrame());
            editAreaGroupBox.add(detailsStepFrame.getFrame());
        } else if (regionsStepFrame.getFrame().isVisible()) {
            tipLabel.setValue(getMessage("addPropertiesAndTableAreas"));
            editAreaVbox.remove(editAreaGroupBox);
            editAreaVbox.add(regionsStepFrame.getFrame());
        } else if (saveStepFrame.getFrame().isVisible()) {
            tipLabel.setValue(getMessage("finishPrepareReport"));
            editAreaVbox.add(editAreaGroupBox);
            editAreaVbox.remove(regionsStepFrame.getFrame());
            editAreaGroupBox.add(saveStepFrame.getFrame());
            editAreaGroupBox.remove(detailsStepFrame.getFrame());
        }
    }

    protected List<StepFrame> getStepFrames() {
        detailsStepFrame = new DetailsStepFrame(this);
        regionsStepFrame = new RegionsStepFrame(this);
        saveStepFrame = new SaveStepFrame(this);
        return Arrays.asList(detailsStepFrame, regionsStepFrame, saveStepFrame);
    }

    protected String generateTemplateFileName(String fileExtension) {
        if (entity.getValue() == null) {
            return "";
        }
        return formatMessage("downloadTemplateFileNamePattern", reportName.getValue(), fileExtension);
    }

    protected String generateOutputFileName(String fileExtension) {
        if (StringUtils.isBlank(reportName.getValue())) {
            if (entity.getValue() != null) {
                return formatMessage("downloadOutputFileNamePattern", messageTools.getEntityCaption(entity.getValue()), fileExtension);
            } else {
                return "";
            }
        } else {
            return reportName.getValue() + "." + fileExtension;
        }
    }


    @Override
    public Button getForwardBtn() {
        return fwdBtn;
    }

    @Override
    public void removeBtns() {
        navBtnsPanel.remove(fwdBtn);
        navBtnsPanel.remove(bwdBtn);
        navBtnsPanel.remove(saveBtn);
    }

    @Override
    public void addForwardBtn() {
        navBtnsPanel.add(fwdBtn);
    }

    @Override
    public void addBackwardBtn() {
        navBtnsPanel.add(bwdBtn);
    }

    @Override
    public void addSaveBtn() {
        navBtnsPanel.add(saveBtn);
    }

    @Override
    public Button getBackwardBtn() {
        return bwdBtn;
    }

    @Override
    public Screen getMainWizardFrame() {
        return this;
    }

    protected void setupButtonsVisibility() {
        buttonsBox.remove(addRegionDisabledBtn);
        buttonsBox.remove(addTabulatedRegionDisabledBtn);
        buttonsBox.remove(addSimpleRegionBtn);
        buttonsBox.remove(addTabulatedRegionBtn);
        buttonsBox.remove(addRegionPopupBtn);
        if (((ReportData.ReportType) reportTypeRadioButtonGroup.getValue()).isList()) {
            tipLabel.setValue(formatMessage("regionTabulatedMessage",
                    messages.getMessage(entity.getValue().getJavaClass(),
                            entity.getValue().getJavaClass().getSimpleName())
            ));
            if (entityTreeHasSimpleAttrs && getItem().getReportRegions().isEmpty()) {
                buttonsBox.add(addTabulatedRegionBtn);
            } else {
                buttonsBox.add(addTabulatedRegionDisabledBtn);
            }
        } else {
            tipLabel.setValue(getMessage("addPropertiesAndTableAreas"));
            if (entityTreeHasSimpleAttrs && entityTreeHasCollections) {
                buttonsBox.add(addRegionPopupBtn);
            } else if (entityTreeHasSimpleAttrs) {
                buttonsBox.add(addSimpleRegionBtn);
            } else if (entityTreeHasCollections) {
                buttonsBox.add(addTabulatedRegionBtn);
            } else {
                buttonsBox.add(addRegionDisabledBtn);
            }
        }

        if (regionsTable.getSingleSelected() != null) {
            moveDownBtn.setEnabled(true);
            moveUpBtn.setEnabled(true);
            removeBtn.setEnabled(true);
        } else {
            moveDownBtn.setEnabled(false);
            moveUpBtn.setEnabled(false);
            removeBtn.setEnabled(false);
        }
    }

    protected Report buildReport(boolean temporary) {
        ReportData reportData = getItem();
        reportData.setName(reportName.getValue());
        reportData.setTemplateFileName(generateTemplateFileName(templateFileFormat.getValue().toString().toLowerCase()));
        if (outputFileFormat.getValue() == null) {
            reportData.setOutputFileType(ReportOutputType.fromId(((TemplateFileType) templateFileFormat.getValue()).getId()));
        } else {
            //lets generate output report in same format as the template
            reportData.setOutputFileType(outputFileFormat.getValue());
        }
        reportData.setReportType((ReportData.ReportType) reportTypeRadioButtonGroup.getValue());
        //groupsDs.refresh();
        if (!groupsDs.getItems().isEmpty()) {
            UUID id = groupsDs.getItems().iterator().next().getId();
            reportData.setGroup(groupsDs.getItem(id));
        }

        //be sure that reportData.name and reportData.outputFileFormat is not null before generation of template
        try {
            byte[] templateByteArray = reportWizardService.generateTemplate(reportData, templateFileFormat.getValue());
            reportData.setTemplateContent(templateByteArray);
        } catch (TemplateGenerationException e) {
            notifications.create(Notifications.NotificationType.WARNING)
                    .withCaption(messages.getMessage("templateGenerationException"))
                    .show();
            return null;
        }
        reportData.setTemplateFileType(templateFileFormat.getValue());
        reportData.setOutputNamePattern(outputFileName.getValue());

        if (query != null) {
            reportData.setQuery(query);
            reportData.setQueryParameters(queryParameters);
            MetaClass entityMetaClass = entity.getValue();
            String storeName = metadataTools.getStoreName(entityMetaClass);
            if (!Stores.isMain(storeName)) {
                reportData.setDataStore(storeName);
            }
        }

        Report report = reportWizardService.toReport(reportData, temporary);
        reportData.setGeneratedReport(report);
        return report;
    }

    //todo
    public String getMessage(String key) {
        return ""/*super.getMessage(key)*/;
    }

    public String formatMessage(String key, Object... params) {
        return ""/*super.formatMessage(key, params)*/;
    }

    protected void setCorrectReportOutputType() {
        ReportOutputType outputFileFormatPrevValue = outputFileFormat.getValue();
        outputFileFormat.setValue(null);
        Map<String, ReportOutputType> optionsMap = refreshOutputAvailableFormats(templateFileFormat.getValue());
        outputFileFormat.setOptionsMap(optionsMap);

        if (outputFileFormatPrevValue != null) {
            if (optionsMap.containsKey(outputFileFormatPrevValue.toString())) {
                outputFileFormat.setValue(outputFileFormatPrevValue);
            }
        }
        if (outputFileFormat.getValue() == null) {
            if (optionsMap.size() > 1) {
                outputFileFormat.setValue(optionsMap.get(templateFileFormat.getValue().toString()));
            } else if (optionsMap.size() == 1) {
                outputFileFormat.setValue(optionsMap.values().iterator().next());
            }
        }
    }

    protected Map<String, ReportOutputType> refreshOutputAvailableFormats(TemplateFileType templateFileType) {
        return availableOutputFormats.get(templateFileType);
    }

    protected Map<TemplateFileType, Map<String, ReportOutputType>> availableOutputFormats;

    private void initAvailableFormats() {
        availableOutputFormats = new ImmutableMap.Builder<TemplateFileType, Map<String, ReportOutputType>>()
                .put(TemplateFileType.DOCX, new ImmutableMap.Builder<String, ReportOutputType>()
                        .put(messages.getMessage(ReportOutputType.DOCX), ReportOutputType.DOCX)
                        .put(messages.getMessage(ReportOutputType.HTML), ReportOutputType.HTML)
                        .put(messages.getMessage(ReportOutputType.PDF), ReportOutputType.PDF)
                        .build())
                .put(TemplateFileType.XLSX, new ImmutableMap.Builder<String, ReportOutputType>()
                        .put(messages.getMessage(ReportOutputType.XLSX), ReportOutputType.XLSX)
                        .put(messages.getMessage(ReportOutputType.HTML), ReportOutputType.HTML)
                        .put(messages.getMessage(ReportOutputType.PDF), ReportOutputType.PDF)
                        .put(messages.getMessage(ReportOutputType.CSV), ReportOutputType.CSV)
                        .build())
                .put(TemplateFileType.HTML, new ImmutableMap.Builder<String, ReportOutputType>()
                        .put(messages.getMessage(ReportOutputType.HTML), ReportOutputType.HTML)
                        .put(messages.getMessage(ReportOutputType.PDF), ReportOutputType.PDF)
                        .build())
                .put(TemplateFileType.CHART, new ImmutableMap.Builder<String, ReportOutputType>()
                        .put(messages.getMessage(ReportOutputType.CHART), ReportOutputType.CHART)
                        .build())
                .put(TemplateFileType.CSV, new ImmutableMap.Builder<String, ReportOutputType>()
                        .put(messages.getMessage(ReportOutputType.CSV), ReportOutputType.CSV)
                        .build())
                .put(TemplateFileType.TABLE, new ImmutableMap.Builder<String, ReportOutputType>()
                        .put(messages.getMessage(ReportOutputType.TABLE), ReportOutputType.TABLE)
                        .build())
                .build();
    }

    public ReportData getItem() {
        return reportDataDs.getItem();
    }
}