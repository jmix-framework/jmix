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
package io.jmix.reportsui.screen.report.edit;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.haulmont.yarg.structure.BandOrientation;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.SaveContext;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.reports.ReportPrintHelper;
import io.jmix.reports.ReportsPersistence;
import io.jmix.reports.ReportsSerialization;
import io.jmix.reports.entity.*;
import io.jmix.reportsui.screen.ReportsClientProperties;
import io.jmix.reportsui.screen.definition.edit.BandDefinitionEditor;
import io.jmix.reportsui.screen.report.edit.tabs.ReportEditGeneralFragment;
import io.jmix.reportsui.screen.report.run.InputParametersDialog;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import io.jmix.ui.*;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.GroupBoxLayout;
import io.jmix.ui.component.Tree;
import io.jmix.ui.component.ValidationErrors;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@UiController("report_Report.edit")
@UiDescriptor("report-edit.xml")
@EditedEntityContainer("reportDc")
public class ReportEditor extends StandardEditor<Report> {

    public static final String ROOT_BAND = "Root";

    @Named("generalFragment.bandEditor")
    protected BandDefinitionEditor bandEditor;

    @Named("generalFragment.bandsTree")
    protected Tree<BandDefinition> bandsTree;

    @Named("parametersFragment.validationScriptGroupBox")
    protected GroupBoxLayout validationScriptGroupBox;

    @Autowired
    protected InstanceContainer<Report> reportDc;

    @Autowired
    protected CollectionContainer<ReportGroup> groupsDc;

    @Autowired
    private CollectionLoader<ReportGroup> groupsDl;

    @Autowired
    protected CollectionContainer<ReportInputParameter> parametersDc;

    @Autowired
    protected CollectionContainer<ReportScreen> reportScreensDc;

    @Autowired
    protected CollectionContainer<DataSet> dataSetsDc;

    @Autowired
    protected CollectionContainer<ReportTemplate> templatesDc;

    @Autowired
    protected UiComponents uiComponents;

    @Autowired
    protected ReportsPersistence reportsPersistence;

    @Autowired
    protected ReportsSerialization reportsSerialization;

    @Autowired
    protected CollectionContainer<BandDefinition> bandsDc;

    @Autowired
    protected CollectionContainer<BandDefinition> availableParentBandsDc;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected SecureOperations secureOperations;

    @Autowired
    protected PolicyStore policyStore;

    @Autowired
    protected UiProperties uiProperties;

    @Autowired
    protected Notifications notifications;

    @Autowired
    protected Dialogs dialogs;

    @Autowired
    protected ScreenBuilders screenBuilders;

    @Autowired
    protected Screens screens;

    @Autowired
    protected Actions actions;

    @Autowired
    protected ReportEditGeneralFragment generalFragment;

    @Autowired
    protected ScreenValidation screenValidation;

    @Autowired
    protected ReportsClientProperties reportsClientProperties;

    @Autowired
    protected DataContext dataContext;

    @Autowired
    protected MessageBundle messageBundle;

    @Subscribe
    protected void initNewItem(InitEntityEvent<Report> event) {
        Report report = event.getEntity();

        if (report.getReportType() == null) {
            report.setReportType(ReportType.SIMPLE);
        }

        if (report.getBands().isEmpty()) {
            BandDefinition rootDefinition = createRootBandDefinition(report);

            report.getBands().add(rootDefinition);
        }

        groupsDl.load();
        Collection<ReportGroup> reportGroups = groupsDc.getItems();
        if (!reportGroups.isEmpty()) {
            ReportGroup reportGroup = reportGroups.iterator().next();
            report.setGroup(groupsDc.getItem(reportGroup.getId()));
        }
    }

    protected BandDefinition createRootBandDefinition(Report report) {
        BandDefinition rootDefinition = dataContext.create(BandDefinition.class);
        rootDefinition.setName(ROOT_BAND);
        rootDefinition.setPosition(0);
        rootDefinition.setReport(report);
        return rootDefinition;
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        bandsTree.expandTree();
        bandsTree.setSelected(getEditedEntity().getRootBandDefinition());

        generalFragment.setupDropZoneForTemplate();
        generalFragment.sortBandDefinitionsByPosition();

        setScreenCaption();
    }

    @Override
    protected String getSaveNotificationCaption() {
        return messageBundle.formatMessage("notification.completeSuccessfully", getEditedEntity().getName());
    }


    protected void setScreenCaption() {
        if (!StringUtils.isEmpty(getEditedEntity().getName())) {
            getWindow().setCaption(messageBundle.formatMessage("reportEditor.format", getEditedEntity().getName()));
        }
    }

    @Subscribe
    protected void onAfterInit(AfterInitEvent event) {
        bandsTree.expandTree();

        if (bandsTree.getSingleSelected() == null) {
            bandEditor.setEnabled(false);
        }
    }

    @Install(target = Target.DATA_CONTEXT)
    protected Set<Object> commitDelegate(SaveContext saveContext) {
        Set<Object> result = new HashSet<>();
        Report reportToStore = null;
        for (Object entity : saveContext.getEntitiesToSave()) {
            if (entity instanceof Report) {
                reportToStore = (Report) entity;
            } else if (entity instanceof ReportTemplate) {
                reportToStore = ((ReportTemplate) entity).getReport();
            }
        }

        if (reportToStore != null) {
            result.add(reportsPersistence.save(reportToStore));
        }
        return result;
    }

    @Subscribe("run")
    protected void onRunClick(Button.ClickEvent event) {
        ValidationErrors errors = new ValidationErrors();
        validateBands(errors);
        if (errors.isEmpty()) {
            if (validateInputOutputFormats()) {
                getEditedEntity().setIsTmp(true);
                Map<String, Object> params = ParamsMap.of("report", getEditedEntity());

                InputParametersDialog screen = screenBuilders.screen(getWindow().getFrameOwner())
                        .withScreenClass(InputParametersDialog.class)
                        .withOpenMode(OpenMode.DIALOG)
                        .withOptions(new MapScreenOptions(params))
                        .build();
                screen.setInBackground(reportsClientProperties.getUseBackgroundReportProcessing());
                screen.addAfterCloseListener(e -> bandsTree.focus());
                screen.show();
            }
        } else {
            screenValidation.showValidationErrors(this, errors);
        }
    }


    @Subscribe
    protected void onValidation(ValidationEvent event) {
        validateBands(event.getErrors());
    }

    protected void validateBands(ValidationErrors validationErrors) {
        if (getEditedEntity().getRootBand() == null) {
            validationErrors.add(messageBundle.getMessage("error.rootBandNull"));
        }
        if (CollectionUtils.isNotEmpty(getEditedEntity().getRootBandDefinition().getChildrenBandDefinitions())) {
            Multimap<String, BandDefinition> names = ArrayListMultimap.create();
            names.put(getEditedEntity().getRootBandDefinition().getName(), getEditedEntity().getRootBandDefinition());

            for (BandDefinition band : getEditedEntity().getRootBandDefinition().getChildrenBandDefinitions()) {
                validateBand(validationErrors, band, names);
            }

            checkForNameDuplication(validationErrors, names);
        }
    }

    protected boolean isUpdatePermitted() {
        return secureOperations.isEntityUpdatePermitted(metadata.getClass(Report.class), policyStore);
    }

    protected boolean validateInputOutputFormats() {
        ReportTemplate template = getEditedEntity().getDefaultTemplate();
        if (template != null && !template.isCustom()
                && template.getReportOutputType() != ReportOutputType.CHART
                && template.getReportOutputType() != ReportOutputType.TABLE
                && template.getReportOutputType() != ReportOutputType.PIVOT_TABLE) {
            String inputType = template.getExt();
            if (!ReportPrintHelper.getInputOutputTypesMapping().containsKey(inputType) ||
                    !ReportPrintHelper.getInputOutputTypesMapping().get(inputType).contains(template.getReportOutputType())) {
                notifications.create(Notifications.NotificationType.TRAY)
                        .withCaption(messageBundle.getMessage("inputOutputTypesError"))
                        .show();
                return false;
            }
        }
        return true;
    }

    @Subscribe
    protected void onBeforeCommit(BeforeCommitChangesEvent event) {
        addCommitListeners();
    }

    protected void addCommitListeners() {
        String xml = reportsSerialization.convertToString(getEditedEntity());
        getEditedEntity().setXml(xml);
    }

    protected void checkForNameDuplication(ValidationErrors errors, Multimap<String, BandDefinition> names) {
        for (String name : names.keySet()) {
            Collection<BandDefinition> bandDefinitionsWithsSameNames = names.get(name);
            if (bandDefinitionsWithsSameNames != null && bandDefinitionsWithsSameNames.size() > 1) {
                errors.add(messageBundle.formatMessage("error.bandNamesDuplicated", name));
            }
        }
    }

    protected void validateBand(ValidationErrors errors, BandDefinition band, Multimap<String, BandDefinition> names) {
        names.put(band.getName(), band);

        if (StringUtils.isBlank(band.getName())) {
            errors.add(messageBundle.getMessage("error.bandNameNull"));
        }

        if (band.getBandOrientation() == BandOrientation.UNDEFINED) {
            errors.add(messageBundle.formatMessage("error.bandOrientationNull", band.getName()));
        }

        if (CollectionUtils.isNotEmpty(band.getDataSets())) {
            for (DataSet dataSet : band.getDataSets()) {
                if (StringUtils.isBlank(dataSet.getName())) {
                    errors.add(messageBundle.getMessage("error.dataSetNameNull"));
                }

                if (dataSet.getType() == null) {
                    errors.add(messageBundle.formatMessage("error.dataSetTypeNull", dataSet.getName()));
                }

                if (dataSet.getType() == DataSetType.GROOVY
                        || dataSet.getType() == DataSetType.SQL
                        || dataSet.getType() == DataSetType.JPQL) {
                    if (StringUtils.isBlank(dataSet.getScript())) {
                        errors.add(messageBundle.formatMessage("error.dataSetScriptNull", dataSet.getName()));
                    }
                } else if (dataSet.getType() == DataSetType.JSON) {
                    if (StringUtils.isBlank(dataSet.getJsonSourceText()) && dataSet.getJsonSourceType() != JsonSourceType.PARAMETER) {
                        errors.add(messageBundle.formatMessage("error.jsonDataSetScriptNull", dataSet.getName()));
                    }
                }
            }
        }

        if (CollectionUtils.isNotEmpty(band.getChildrenBandDefinitions())) {
            for (BandDefinition child : band.getChildrenBandDefinitions()) {
                validateBand(errors, child, names);
            }
        }
    }

    @Subscribe(id = "reportDc", target = Target.DATA_CONTAINER)
    protected void onReportDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<Report> event) {
        boolean validationOnChanged = event.getProperty().equalsIgnoreCase("validationOn");

        if (validationOnChanged) {
            setValidationScriptGroupBoxCaption(event.getItem().getValidationOn());
        }
    }

    protected void setValidationScriptGroupBoxCaption(Boolean onOffFlag) {
        if (BooleanUtils.isTrue(onOffFlag)) {
            validationScriptGroupBox.setCaption(messageBundle.getMessage("report.validationScriptOn"));
        } else {
            validationScriptGroupBox.setCaption(messageBundle.getMessage("report.validationScriptOff"));
        }
    }
}