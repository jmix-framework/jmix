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
package io.jmix.reportsui.gui.report.wizard;

import com.google.common.collect.ImmutableMap;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.GroupBoxLayout;
import com.haulmont.cuba.gui.components.Label;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import io.jmix.core.MessageTools;
import io.jmix.core.MetadataTools;
import io.jmix.core.Stores;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.reports.app.EntityTree;
import io.jmix.reports.app.service.ReportWizardService;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportGroup;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.charts.ChartType;
import io.jmix.reports.entity.wizard.ReportData;
import io.jmix.reports.entity.wizard.ReportRegion;
import io.jmix.reports.entity.wizard.TemplateFileType;
import io.jmix.reports.exception.TemplateGenerationException;
import io.jmix.reportsui.gui.ReportGuiManager;
import io.jmix.reportsui.gui.report.wizard.step.MainWizardFrame;
import io.jmix.reportsui.gui.report.wizard.step.StepFrame;
import io.jmix.reportsui.gui.report.wizard.step.StepFrameManager;
import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.component.*;
import io.jmix.ui.theme.ThemeConstants;
import io.jmix.ui.xml.layout.ComponentsFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;
import java.util.*;

public class ReportWizardCreator extends AbstractWindow implements MainWizardFrame<AbstractWindow> {

    @Autowired
    protected Datasource reportDataDs;
    @Autowired
    protected CollectionDatasource<ReportRegion, UUID> reportRegionsDs;
    @Autowired
    protected CollectionDatasource<ReportGroup, UUID> groupsDs;
    @Named("fwd")
    protected Button fwdBtn;
    @Named("regionsStep.run")
    protected Button runBtn;
    @Named("bwd")
    protected Button bwdBtn;
    @Named("save")
    protected Button saveBtn;
    @Autowired
    protected Label tipLabel;
    @Autowired
    protected BoxLayout editAreaVbox;
    @Autowired
    protected ButtonsPanel navBtnsPanel;
    @Autowired
    protected GroupBoxLayout editAreaGroupBox;

    @Named("detailsStep.mainFields")
    protected FieldGroup mainFields;
    @Named("detailsStep.setQuery")
    protected Button setQueryButton;

    protected OptionsGroup reportTypeOptionGroup;//this and following are set during creation
    protected LookupField<TemplateFileType> templateFileFormat;
    protected LookupField<MetaClass> entity;
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
    protected LookupField<ReportOutputType> outputFileFormat;
    @Named("saveStep.outputFileName")
    protected TextField<String> outputFileName;
    @Named("saveStep.downloadTemplateFile")
    protected Button downloadTemplateFile;
    @Named("saveStep.diagramTypeLabel")
    protected Label diagramTypeLabel;
    @Named("saveStep.diagramType")
    protected LookupField<ChartType> diagramType;
    @Named("saveStep.chartPreviewBox")
    protected BoxLayout chartPreviewBox;

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected ComponentsFactory componentsFactory;
    @Autowired
    protected ReportWizardService reportWizardService;
    @Autowired
    protected ThemeConstants themeConstants;

    protected StepFrame detailsStepFrame;
    protected StepFrame regionsStepFrame;
    protected StepFrame saveStepFrame;
    protected StepFrameManager stepFrameManager;
    protected ReportGuiManager reportGuiManager = AppBeans.get(ReportGuiManager.class);

    protected byte[] lastGeneratedTemplate;
    protected Report lastGeneratedTmpReport;
    protected boolean entityTreeHasSimpleAttrs;
    protected boolean entityTreeHasCollections;
    protected boolean needUpdateEntityModel = false;

    protected String query;
    protected String dataStore;
    protected List<ReportData.Parameter> queryParameters;
    protected int wizardWidth;
    protected int wizardHeight;

    @Override
    @SuppressWarnings("unchecked")
    public void init(Map<String, Object> params) {
        super.init(params);

        reportDataDs.setItem(metadata.create(ReportData.class));

        wizardWidth = themeConstants.getInt("cuba.gui.report.ReportWizard.width");
        wizardHeight = themeConstants.getInt("cuba.gui.report.ReportWizard.height");
        //TODO dialog options
//        getDialogOptions()
//                .setWidth(wizardWidth).setWidthUnit(SizeUnit.PIXELS)
//                .setHeight(wizardHeight).setHeightUnit(SizeUnit.PIXELS);

        stepFrameManager = new StepFrameManager(this, getStepFrames());

        initAvailableFormats();
        initMainButtons();
        initMainFields();

        stepFrameManager.showCurrentFrame();
        tipLabel.setValue(getMessage("enterMainParameters"));

        reportRegionsDs.addCollectionChangeListener(e -> {
            if (e.getOperation() == CollectionDatasource.Operation.ADD) {
                regionsTable.setSelected((Collection) e.getItems());
            }
        });

        reportRegionsDs.addItemChangeListener(e -> {
            if (regionsTable.getSingleSelected() != null) {
                moveDownBtn.setEnabled(true);
                moveUpBtn.setEnabled(true);
                removeBtn.setEnabled(true);
            }
        });

        outputFileName.setContextHelpIconClickHandler(e ->
                showMessageDialog(getMessage("template.namePatternText"), getMessage("template.namePatternTextHelp"),
                        MessageType.CONFIRMATION_HTML
                                .modal(false)
                                .width(560f)));
    }

    protected void initMainButtons() {
        fwdBtn.setAction(new AbstractAction("fwd") {
            @Override
            public void actionPerform(Component component) {
                if (entity.getValue() == null) {
                    showNotification(getMessage("fillEntityMsg"), NotificationType.TRAY_HTML);
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
        mainFields.addCustomField("entity", (datasource, propertyId) -> {
            LookupField lookupField = componentsFactory.createComponent(LookupField.class);
            //TODO request focus
//            lookupField.requestFocus();
            entity = lookupField;
            return lookupField;
        });
        mainFields.addCustomField("reportName", (datasource, propertyId) -> {
            TextField textField = componentsFactory.createComponent(TextField.class);
            textField.setMaxLength(255);
            reportName = textField;
            return textField;
        });
        mainFields.addCustomField("templateFileFormat", (datasource, propertyId) -> {
            LookupField lookupField = componentsFactory.createComponent(LookupField.class);
            templateFileFormat = lookupField;
            return lookupField;
        });
        mainFields.addCustomField("reportType", (datasource, propertyId) -> {
            OptionsGroup optionsGroup = componentsFactory.createComponent(OptionsGroup.class);
            optionsGroup.setMultiSelect(false);
            optionsGroup.setOrientation(OptionsGroup.Orientation.VERTICAL);
            reportTypeOptionGroup = optionsGroup;
            return optionsGroup;
        });
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
    public AbstractWindow getMainWizardFrame() {
        return this;
    }

    protected void setupButtonsVisibility() {
        buttonsBox.remove(addRegionDisabledBtn);
        buttonsBox.remove(addTabulatedRegionDisabledBtn);
        buttonsBox.remove(addSimpleRegionBtn);
        buttonsBox.remove(addTabulatedRegionBtn);
        buttonsBox.remove(addRegionPopupBtn);
        if (((ReportData.ReportType) reportTypeOptionGroup.getValue()).isList()) {
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
        reportData.setReportType((ReportData.ReportType) reportTypeOptionGroup.getValue());
        groupsDs.refresh();
        if (groupsDs.getItemIds() != null) {
            UUID id = groupsDs.getItemIds().iterator().next();
            reportData.setGroup(groupsDs.getItem(id));
        }

        //be sure that reportData.name and reportData.outputFileFormat is not null before generation of template
        try {
            byte[] templateByteArray = reportWizardService.generateTemplate(reportData, templateFileFormat.getValue());
            reportData.setTemplateContent(templateByteArray);
        } catch (TemplateGenerationException e) {
            showNotification(getMessage("templateGenerationException"), NotificationType.WARNING);
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

    /**
     * Dead code. Must to be tested after platform fixes in com.haulmont.cuba.web.WebWindowManager
     * Web modal editor window always closed forced, therefore that preClose method is not called
     * <p>
     * Confirm closing without save if regions are created
     */
    @Override
    public boolean preClose(String actionId) {
        if (!COMMIT_ACTION_ID.equals(actionId) && reportRegionsDs.getItems() != null) {
            showOptionDialog(getMessage("dialogs.Confirmation"), getMessage("interruptConfirm"), MessageType.CONFIRMATION, new Action[]{
                    new DialogAction(DialogAction.Type.YES) {
                        @Override
                        public void actionPerform(Component component) {
                            ReportWizardCreator.this.close(CLOSE_ACTION_ID);
                        }
                    },
                    new DialogAction(DialogAction.Type.NO, Action.Status.PRIMARY)
            });
        }
        return false;
    }

    @Override
    public String getMessage(String key) {
        return super.getMessage(key);
    }

    @Override
    public String formatMessage(String key, Object... params) {
        return super.formatMessage(key, params);
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
        return (ReportData) reportDataDs.getItem();
    }
}