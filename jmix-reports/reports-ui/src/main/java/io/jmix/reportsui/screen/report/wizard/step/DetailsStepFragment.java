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

package io.jmix.reportsui.screen.report.wizard.step;

import io.jmix.core.ExtendedEntities;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.reports.entity.wizard.ReportData;
import io.jmix.reports.entity.wizard.ReportRegion;
import io.jmix.reports.entity.wizard.ReportTypeGenerate;
import io.jmix.reports.entity.wizard.TemplateFileType;
import io.jmix.reportsui.screen.report.run.ShowChartScreen;
import io.jmix.reportsui.screen.report.wizard.ReportsWizard;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.RadioButtonGroup;
import io.jmix.ui.model.CollectionPropertyContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

@UiController("report_DetailsStep.fragment")
@UiDescriptor("details-step-fragment.xml")
public class DetailsStepFragment extends StepFragment {

    @Autowired
    private InstanceContainer<ReportData> reportDataDc;

    @Autowired
    private CollectionPropertyContainer<ReportRegion> reportRegionsDc;

    @Autowired
    protected Dialogs dialogs;

    @Autowired
    protected ComboBox<MetaClass> entityField;

    @Autowired
    protected ComboBox<TemplateFileType> templateFileTypeField;

    @Autowired
    protected ExtendedEntities extendedEntities;

    @Autowired
    protected RadioButtonGroup<ReportTypeGenerate> reportTypeGenerateField;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected ReportsWizard reportsWizard;

    @Autowired
    protected Notifications notifications;

    @Autowired
    protected WindowConfig windowConfig;

    protected boolean needUpdateEntityModel = false;

    public boolean isNeedUpdateEntityModel() {
        return needUpdateEntityModel;
    }

    public void setNeedUpdateEntityModel(boolean needUpdateEntityModel) {
        this.needUpdateEntityModel = needUpdateEntityModel;
    }

    @Subscribe
    public void onInit(InitEvent event) {
        initReportTypeOptionGroup();
        initTemplateFormatLookupField();
        initEntityLookupField();
    }

    protected void initEntityLookupField() {
        entityField.setOptionsMap(getAvailableEntities());
    }

    @Subscribe("reportTypeGenerateField")
    public void onReportTypeGenerateValueChange(HasValue.ValueChangeEvent<ReportTypeGenerate> event) {
        ReportData reportData = reportDataDc.getItem();
        ReportTypeGenerate currentType = event.getValue();
        updateReportTypeGenerate(reportData, currentType);
    }

    protected void updateReportTypeGenerate(ReportData reportData, @Nullable ReportTypeGenerate reportTypeGenerate) {
        reportData.setReportTypeGenerate(reportTypeGenerate);
        reportRegionsDc.getMutableItems().clear();

        clearQuery();
    }

    @Subscribe("entityField")
    public void onEntityValueChange(HasValue.ValueChangeEvent<MetaClass> event) {
        ReportData reportData = reportDataDc.getItem();
        updateReportEntity(event.getPrevValue(), event.getValue(), reportData);
    }

    protected void updateReportEntity(@Nullable MetaClass prevValue, MetaClass value, ReportData reportData) {
        needUpdateEntityModel = true;
        setReportName(reportData, prevValue, value);

        reportRegionsDc.getMutableItems().clear();
        reportData.setEntityName(value.getName());

        clearQuery();
    }

    protected void setReportName(ReportData reportData, @Nullable MetaClass prevValue, MetaClass value) {
        String oldName = reportData.getName();
        if (StringUtils.isBlank(oldName)) {
            reportData.setName(messageBundle.formatMessage("reportNamePattern", messageTools.getEntityCaption(value)));
        } else {
            if (prevValue != null) {
                //if old text contains MetaClass name substring, just replace it
                String prevEntityCaption = messageTools.getEntityCaption(prevValue);
                if (StringUtils.contains(oldName, prevEntityCaption)) {

                    String newName = oldName;
                    int index = oldName.lastIndexOf(prevEntityCaption);
                    if (index > -1) {
                        newName = StringUtils.substring(oldName, 0, index)
                                + messageTools.getEntityCaption(value)
                                + StringUtils.substring(oldName, index + prevEntityCaption.length(), oldName.length());
                    }

                    reportData.setName(newName);
                    if (!oldName.equals(messageBundle.formatMessage("reportNamePattern", prevEntityCaption))) {
                        //if user changed auto generated report name and we have changed it, we show message to him
                        notifications.create(Notifications.NotificationType.TRAY)
                                .withCaption(messageBundle.getMessage("reportNameChanged"))
                                .show();
                    }
                }
            }
        }
    }

    protected void initTemplateFormatLookupField() {
        templateFileTypeField.setOptionsMap(getAvailableTemplateFormats());
        templateFileTypeField.setTextInputAllowed(false);
        templateFileTypeField.setValue(TemplateFileType.DOCX);
    }

    protected void initReportTypeOptionGroup() {
        reportTypeGenerateField.setOptionsMap(getListedReportOptionsMap());
        reportTypeGenerateField.setValue(ReportTypeGenerate.SINGLE_ENTITY);
    }

    protected Map<String, ReportTypeGenerate> getListedReportOptionsMap() {
        Map<String, ReportTypeGenerate> result = new LinkedHashMap<>(3);
        result.put(messageBundle.getMessage("singleEntityReport"), ReportTypeGenerate.SINGLE_ENTITY);
        result.put(messageBundle.getMessage("listOfEntitiesReport"), ReportTypeGenerate.LIST_OF_ENTITIES);
        result.put(messageBundle.getMessage("listOfEntitiesReportWithQuery"), ReportTypeGenerate.LIST_OF_ENTITIES_WITH_QUERY);
        return result;
    }

    protected Map<String, TemplateFileType> getAvailableTemplateFormats() {
        Map<String, TemplateFileType> result = new LinkedHashMap<>(4);
        result.put(messages.getMessage(TemplateFileType.XLSX), TemplateFileType.XLSX);
        result.put(messages.getMessage(TemplateFileType.DOCX), TemplateFileType.DOCX);
        result.put(messages.getMessage(TemplateFileType.HTML), TemplateFileType.HTML);
        result.put(messages.getMessage(TemplateFileType.CSV), TemplateFileType.CSV);
        result.put(messages.getMessage(TemplateFileType.TABLE), TemplateFileType.TABLE);

        if (windowConfig.hasWindow(ShowChartScreen.JSON_CHART_SCREEN_ID)) {
            result.put(messages.getMessage(TemplateFileType.CHART), TemplateFileType.CHART);
        }
        return result;
    }

    protected Map<String, MetaClass> getAvailableEntities() {
        Map<String, MetaClass> result = new TreeMap<>(String::compareTo);
        Collection<MetaClass> classes = metadataTools.getAllJpaEntityMetaClasses();
        for (MetaClass metaClass : classes) {
            MetaClass effectiveMetaClass = extendedEntities.getEffectiveMetaClass(metaClass);
            if (!reportsWizard.isEntityAllowedForReportWizard(effectiveMetaClass)) {
                continue;
            }
            result.put(messageTools.getEntityCaption(effectiveMetaClass) + " (" + effectiveMetaClass.getName() + ")", effectiveMetaClass);
        }
        return result;
    }

    @Override
    public String getCaption() {
        return messageBundle.getMessage("reportDetails");
    }

    @Override
    public String getDescription() {
        return messageBundle.getMessage("enterMainParameters");
    }

    protected void clearQuery() {
        ReportData reportData = reportDataDc.getItem();
        reportData.setQuery(null);
        reportData.setQueryParameters(null);
    }
}