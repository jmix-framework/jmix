/*
 * Copyright 2022 Haulmont.
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

package io.jmix.reportsflowui.view.report;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Multimap;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.*;
import io.jmix.flowui.action.SecuredBaseAction;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.codeeditor.CodeEditor;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.download.ByteArrayDownloadDataProvider;
import io.jmix.flowui.download.DownloadFormat;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.codeeditor.CodeEditorMode;
import io.jmix.flowui.model.*;
import io.jmix.flowui.util.RemoveOperation;
import io.jmix.flowui.view.*;
import io.jmix.reports.ReportPrintHelper;
import io.jmix.reports.ReportsPersistence;
import io.jmix.reports.ReportsSerialization;
import io.jmix.reports.entity.*;
import io.jmix.reports.util.DataSetFactory;
import io.jmix.reports.yarg.structure.BandOrientation;
import io.jmix.reportsflowui.constant.ReportStyleConstants;
import io.jmix.reportsflowui.support.CrossTabDataGridSupport;
import io.jmix.reportsflowui.ReportsClientProperties;
import io.jmix.reportsflowui.helper.ReportScriptEditor;
import io.jmix.reportsflowui.view.run.InputParametersDialog;
import io.jmix.reportsflowui.view.template.ReportTemplateDetailView;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import io.jmix.security.model.BaseRole;
import io.jmix.security.role.ResourceRoleRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@Route(value = "reports/:id", layout = DefaultMainViewParent.class)
@ViewController("report_Report.detail")
@ViewDescriptor("report-detail-view.xml")
@EditedEntityContainer("reportDc")
public class ReportDetailView extends StandardDetailView<Report> {

    public static final String ROOT_BAND = "Root";

    @ViewComponent
    protected DataContext dataContext;
    @ViewComponent
    protected InstanceContainer<Report> reportDc;
    @ViewComponent
    protected CollectionContainer<BandDefinition> availableParentBandsDc;
    @ViewComponent
    protected CollectionPropertyContainer<BandDefinition> bandsDc;
    @ViewComponent
    protected CollectionPropertyContainer<ReportTemplate> templatesDc;
    @ViewComponent
    protected CollectionPropertyContainer<DataSet> dataSetsDc;
    @ViewComponent
    protected CollectionPropertyContainer<ReportInputParameter> parametersDc;

    @ViewComponent
    protected EntityComboBox<ReportTemplate> defaultTemplateField;
    @ViewComponent(value = "defaultTemplateField.create")
    protected SecuredBaseAction defaultTemplateFieldCreateAction;
    @ViewComponent(value = "defaultTemplateField.upload")
    protected SecuredBaseAction defaultTemplateFieldUploadAction;
    @ViewComponent(value = "defaultTemplateField.edit")
    protected SecuredBaseAction defaultTemplateFieldEditAction;
    @ViewComponent
    protected TreeDataGrid<BandDefinition> bandsTreeDataGrid;
    @ViewComponent
    protected TypedTextField<String> bandNameField;
    @ViewComponent
    protected JmixSelect<Orientation> orientationField;
    @ViewComponent
    protected EntityComboBox<BandDefinition> parentBandField;
    @ViewComponent
    protected JmixCheckbox multiDataSetField;
    @ViewComponent
    protected Div multiDataSetLayout;
    @ViewComponent
    protected Div singleDataSetLayout;
    @ViewComponent
    protected Div dataSetDetailsLayout;
    @ViewComponent
    protected Div dataSetTypeLayout;
    @ViewComponent
    protected VerticalLayout dataSetsDataGridLayout;
    @ViewComponent
    protected DataGrid<DataSet> dataSetsDataGrid;
    @ViewComponent
    protected JmixSelect<DataSetType> singleDataSetTypeField;
    @ViewComponent
    protected CodeEditor dataSetScriptCodeEditor;
    @ViewComponent
    protected JmixButton dataSetScriptCodeEditorHelpBtn;
    @ViewComponent
    protected JmixSelect<String> dataStoreField;
    @ViewComponent
    protected JmixCheckbox isProcessTemplateField;
    @ViewComponent
    protected VerticalLayout commonEntityGrid;
    @ViewComponent
    protected VerticalLayout jsonDataSetTypeVBox;
    @ViewComponent
    protected VerticalLayout dataSetScriptBox;
    @ViewComponent
    protected JmixComboBox<String> entitiesParamField;
    @ViewComponent
    protected JmixComboBox<String> entityParamField;
    @ViewComponent
    protected JmixComboBox<String> fetchPlanNameField;
    @ViewComponent
    protected JmixButton fetchPlanEditButton;
    @ViewComponent
    protected JmixCheckbox isUseExistingFetchPlanField;
    @ViewComponent
    protected JmixComboBox<JsonSourceType> jsonSourceTypeField;
    @ViewComponent
    protected JmixTextArea jsonPathQueryTextAreaField;
    @ViewComponent
    protected JmixTextArea jsonSourceURLTextArea;
    @ViewComponent
    protected FormLayout jsonQueryParameterForm;
    @ViewComponent
    protected CodeEditor jsonGroovyCodeEditor;
    @ViewComponent
    protected DataGrid<ReportTemplate> templatesDataGrid;
    @ViewComponent
    protected CodeEditor validationScriptCodeEditor;
    @ViewComponent
    protected JmixTextArea localeTextField;
    @ViewComponent
    protected JmixComboBox<String> screenIdField;
    @ViewComponent
    protected CollectionPropertyContainer<ReportScreen> reportScreensDc;
    @ViewComponent
    protected JmixComboBox<BaseRole> rolesField;
    @ViewComponent
    protected CollectionPropertyContainer<ReportRole> reportRolesDc;
    @ViewComponent
    protected DataGrid<ReportInputParameter> inputParametersDataGrid;

    @Autowired
    protected ReportsPersistence reportsPersistence;
    @Autowired
    protected MessageBundle messageBundle;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected ViewValidation viewValidation;
    @Autowired
    protected ReportsSerialization reportsSerialization;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected DialogWindows dialogWindows;
    @Autowired
    protected SecureOperations secureOperations;
    @Autowired
    protected PolicyStore policyStore;
    @Autowired
    protected Downloader downloader;
    @Autowired
    protected UiProperties uiProperties;
    @Autowired
    protected CoreProperties coreProperties;
    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected DataSetFactory dataSetFactory;
    @Autowired
    protected FetchPlanRepository fetchPlanRepository;
    @Autowired
    protected Stores stores;
    @Autowired
    protected ReportScriptEditor reportScriptEditor;
    @Autowired
    protected CrossTabDataGridSupport dataGridDecorator;
    @Autowired
    protected ReportsClientProperties reportsClientProperties;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected Dialogs dialogs;
    @Autowired
    protected ResourceRoleRepository resourceRoleRepository;
    @Autowired
    protected ViewRegistry viewRegistry;

    protected JmixComboBoxBinder<String> entityParamFieldBinder;
    protected JmixComboBoxBinder<String> entitiesParamFieldBinder;
    protected JmixComboBoxBinder<String> fetchPlanNameFieldBinder;

    @Subscribe
    public void onInit(InitEvent event) {
        dataSetsDataGridLayout.setWidth(null);

        hideAllDataSetEditComponents();

        initParametersDataGrid();

        initBandsTreeDataGrid();
        initDataSetsDataGrid();
        initTemplateDataGrid();

        initDataStoreField();
        initJsonPathQueryTextAreaField();

        initEntitiesParamField();
        initEntityParamField();
        initFetchPlanNameField();

        defaultTemplateFieldCreateAction.refreshState();
        defaultTemplateFieldUploadAction.refreshState();
        defaultTemplateFieldEditAction.refreshState();
        defaultTemplateField.setReadOnly(!isUpdatePermitted());
        refreshBandActionStates();
        refreshDataSetsActionStates();
        initLocaleDetailReportTextField();
        initRoleField();
        initScreenIdField();
    }

    private void initTemplateDataGrid() {
        templatesDataGrid.addComponentColumn(template -> createCheckbox(template.getAlterable()))
                .setHeader(messageBundle.getMessage("templatesTab.templatesDataGrid.alterable"))
                .setKey("alterable")
                .setSortable(false)
                .setResizable(true);

        templatesDataGrid.addComponentColumn(template -> createCheckbox(template.equals(reportDc.getItem().getDefaultTemplate())))
                .setHeader(messageBundle.getMessage("templatesTab.templatesDataGrid.default"))
                .setKey("default")
                .setSortable(false)
                .setResizable(true);
    }

    protected void initParametersDataGrid() {
        inputParametersDataGrid.addComponentColumn(parameter -> createCheckbox(parameter.getRequired()))
                .setHeader(messageBundle.getMessage("parametersTab.inputParameterDataGrid.required"))
                .setKey("required")
                .setResizable(true)
                .setSortable(true);

        inputParametersDataGrid.addComponentColumn(parameter -> createCheckbox(parameter.getValidationOn()))
                .setHeader(messageBundle.getMessage("parametersTab.inputParameterDataGrid.validationOn"))
                .setKey("validationOn")
                .setResizable(true)
                .setSortable(true);
    }

    protected Checkbox createCheckbox(Boolean value) {
        JmixCheckbox checkbox = uiComponents.create(JmixCheckbox.class);
        checkbox.setEnabled(false);
        UiComponentUtils.setValue(checkbox, value);

        return checkbox;
    }

    @Subscribe
    protected void onInitEntity(InitEntityEvent<Report> event) {
        Report report = event.getEntity();

        if (report.getReportType() == null) {
            report.setReportType(ReportType.SIMPLE);
        }

        if (report.getBands().isEmpty()) {
            BandDefinition rootDefinition = createRootBandDefinition(report);

            report.getBands().add(rootDefinition);
        }
    }

    @Subscribe
    protected void onReady(ReadyEvent event) {
        bandsTreeDataGrid.expand(bandsDc.getItems());
        bandsTreeDataGrid.select(getEditedEntity().getRootBandDefinition());

        sortBandDefinitionsByPosition();
    }

    @Subscribe("defaultTemplateField.create")
    protected void onDefaultTemplateFieldCreate(ActionPerformedEvent event) {
        View<?> view = UiComponentUtils.getView(this);

        DialogWindow<ReportTemplateDetailView> templateDetailViewDialog = dialogWindows.detail(view, ReportTemplate.class)
                .withViewClass(ReportTemplateDetailView.class)
                .withContainer(templatesDc)
                .newEntity()
                .withInitializer(item -> {
                    Report report = reportDc.getItem();
                    item.setReport(report);
                })
                .build();
        templateDetailViewDialog.addAfterCloseListener(this::onReportTemplateDetailViewDialogClose);
        templateDetailViewDialog.open();
    }

    @Subscribe("defaultTemplateField.download")
    protected void onDefaultTemplateFieldDownload(ActionPerformedEvent event) {
        ReportTemplate defaultTemplate = reportDc.getItem().getDefaultTemplate();
        if (defaultTemplate != null) {
            if (defaultTemplate.isCustom()) {
                notifications.create(
                        messageBundle.getMessage(
                                "detailsTab.notification.unableToSaveCustomTemplate.text")).show();
            } else if (isTemplateWithoutFile(defaultTemplate)) {
                notifications.create(
                        messageBundle.getMessage(
                                "detailsTab.notification.unableToSaveSpecificTypes.text")).show();
            } else {
                byte[] reportTemplate = defaultTemplate.getContent();
                downloader.download(
                        new ByteArrayDownloadDataProvider(reportTemplate,
                                uiProperties.getSaveExportedByteArrayDataThresholdBytes(),
                                coreProperties.getTempDir()),
                        defaultTemplate.getName(),
                        DownloadFormat.getByExtension(defaultTemplate.getExt()));
            }
        } else {
            notifications.create(
                    messageBundle.getMessage(
                            "detailsTab.notification.defaultTemplateIsEmpty.text")).show();
        }
        defaultTemplateField.focus();
    }

    @Install(to = "defaultTemplateField.create", subject = "enabledRule")
    protected boolean defaultTemplateFieldCreateEnabledRule() {
        return isUpdatePermitted();
    }

    @Subscribe("defaultTemplateField.upload")
    protected void onDefaultTemplateFieldUpload(ActionPerformedEvent event) {
        ReportTemplate defaultTemplate = reportDc.getItem().getDefaultTemplate();
        if (defaultTemplate != null) {
            // todo
        }
    }

    @Install(to = "defaultTemplateField.upload", subject = "enabledRule")
    protected boolean defaultTemplateFieldUploadEnabledRule() {
        return isUpdatePermitted();
    }

    @Subscribe("defaultTemplateField.edit")
    protected void onDefaultTemplateFieldEdit(ActionPerformedEvent event) {
        Report report = reportDc.getItem();
        ReportTemplate defaultTemplate = report.getDefaultTemplate();
        if (defaultTemplate != null) {
            DialogWindow<ReportTemplateDetailView> templateDetailViewDialog = dialogWindows.detail(defaultTemplateField)
                    .withViewClass(ReportTemplateDetailView.class)
                    .withContainer(templatesDc)
                    .editEntity(defaultTemplate)
                    .build();
            templateDetailViewDialog.addAfterCloseListener(this::onReportTemplateDetailViewDialogClose);
            templateDetailViewDialog.open();
        } else {
            notifications.create(
                    messageBundle.getMessage(
                            "detailsTab.notification.defaultTemplateIsEmpty.text")).show();
        }
    }

    @Install(to = "defaultTemplateField.edit", subject = "enabledRule")
    protected boolean defaultTemplateFieldEditEnabledRule() {
        return isUpdatePermitted();
    }

    @Subscribe("bandsTreeDataGrid.create")
    protected void onBandsTreeDataGridCreate(ActionPerformedEvent event) {
        BandDefinition parentDefinition = bandsDc.getItemOrNull();
        Report report = reportDc.getItem();
        // Use root band as parent if no items selected
        if (parentDefinition == null) {
            parentDefinition = report.getRootBandDefinition();
        }
        if (parentDefinition.getChildrenBandDefinitions() == null) {
            parentDefinition.setChildrenBandDefinitions(new ArrayList<>());
        }

        orderBandDefinitions(parentDefinition);

        BandDefinition newBandDefinition = dataContext.create(BandDefinition.class);
        newBandDefinition.setName("newBand" + (parentDefinition.getChildrenBandDefinitions().size() + 1));
        newBandDefinition.setOrientation(Orientation.HORIZONTAL);
        newBandDefinition.setParentBandDefinition(parentDefinition);
        if (parentDefinition.getChildrenBandDefinitions() != null) {
            newBandDefinition.setPosition(parentDefinition.getChildrenBandDefinitions().size());
        } else {
            newBandDefinition.setPosition(0);
        }
        newBandDefinition.setReport(report);
        parentDefinition.getChildrenBandDefinitions().add(newBandDefinition);

        bandsDc.getMutableItems().add(newBandDefinition);
        bandsDc.setItem(newBandDefinition);

        // Create default DataSet for band
        DataSet dataset = dataSetFactory.createEmptyDataSet(newBandDefinition);
        dataset.setName("dataSet1");
        newBandDefinition.getDataSets().add(dataset);
        dataSetsDc.getMutableItems().add(dataset);
        dataSetsDc.setItem(dataset);
        dataSetsDataGrid.select(dataset);

        bandsTreeDataGrid.expand(parentDefinition);
        bandsTreeDataGrid.select(newBandDefinition);
        bandsTreeDataGrid.focus();
    }

    @Install(to = "bandsTreeDataGrid.create", subject = "enabledRule")
    protected boolean bandsTreeDataGridCreateEnabledRule() {
        return isUpdatePermitted();
    }

    @Subscribe("bandsTreeDataGrid.remove")
    protected void onBandsTreeDataGridRemove(ActionPerformedEvent event) {
        Set<BandDefinition> selected = bandsTreeDataGrid.getSelectedItems();
        removeChildrenCascade(selected);
        for (Object object : selected) {
            BandDefinition definition = (BandDefinition) object;
            if (definition.getParentBandDefinition() != null) {
                orderBandDefinitions(definition.getParentBandDefinition());
            }
        }
        bandsTreeDataGrid.focus();
    }

    @Install(to = "bandsTreeDataGrid.remove", subject = "enabledRule")
    protected boolean bandsTreeDataGridRemoveEnabledRule() {
        Object selectedItem = bandsTreeDataGrid.getSingleSelectedItem();
        if (selectedItem != null) {
            return !Objects.equals(reportDc.getItem().getRootBandDefinition(), selectedItem);
        }
        return false;
    }

    @Subscribe("bandsTreeDataGrid.upBand")
    protected void onBandsTreeDataGridUpBand(ActionPerformedEvent event) {
        BandDefinition definition = bandsTreeDataGrid.getSingleSelectedItem();
        if (definition != null && definition.getParentBandDefinition() != null) {
            BandDefinition parentDefinition = definition.getParentBandDefinition();
            List<BandDefinition> definitionsList = parentDefinition.getChildrenBandDefinitions();
            int index = definitionsList.indexOf(definition);
            if (index > 0) {
                BandDefinition previousDefinition = definitionsList.get(index - 1);
                definition.setPosition(definition.getPosition() - 1);
                previousDefinition.setPosition(previousDefinition.getPosition() + 1);

                definitionsList.set(index, previousDefinition);
                definitionsList.set(index - 1, definition);

                sortBandDefinitionsByPosition();
                refreshBandActionStates();
            }
        }
    }

    @Install(to = "bandsTreeDataGrid.upBand", subject = "enabledRule")
    protected boolean bandsTreeDataGridUpBandEnabledRule() {
        return isUpBandButtonEnabled();
    }

    @Subscribe("bandsTreeDataGrid.downBand")
    protected void onBandsTreeDataGridDownBand(ActionPerformedEvent event) {
        BandDefinition definition = bandsTreeDataGrid.getSingleSelectedItem();
        if (definition != null && definition.getParentBandDefinition() != null) {
            BandDefinition parentDefinition = definition.getParentBandDefinition();
            List<BandDefinition> definitionsList = parentDefinition.getChildrenBandDefinitions();
            int index = definitionsList.indexOf(definition);
            if (index < definitionsList.size() - 1) {
                BandDefinition nextDefinition = definitionsList.get(index + 1);
                definition.setPosition(definition.getPosition() + 1);
                nextDefinition.setPosition(nextDefinition.getPosition() - 1);

                definitionsList.set(index, nextDefinition);
                definitionsList.set(index + 1, definition);

                sortBandDefinitionsByPosition();
                refreshBandActionStates();
            }
        }
    }

    @Install(to = "bandsTreeDataGrid.downBand", subject = "enabledRule")
    protected boolean bandsTreeDataGridDownBandEnabledRule() {
        return isDownBandButtonEnabled();
    }

    @Subscribe("dataSetsDataGrid.create")
    protected void onDataSetsDataGridCreate(ActionPerformedEvent event) {
        BandDefinition selectedBand = bandsDc.getItem();

        DataSet dataset = dataSetFactory.createEmptyDataSet(selectedBand);
        dataset.setName(getDefaultDataSetName(selectedBand));
        selectedBand.getDataSets().add(dataset);
        dataSetsDc.getMutableItems().add(dataset);
        dataSetsDc.setItem(dataset);
        dataSetsDataGrid.select(dataset);
    }

    @Install(to = "dataSetsDataGrid.create", subject = "enabledRule")
    protected boolean dataSetsDataGridCreateEnabledRule() {
        return isUpdatePermitted();
    }

    @Install(to = "dataSetsDataGrid.remove", subject = "enabledRule")
    protected boolean dataSetsDataGridRemoveEnabledRule() {
        return dataSetsDc.getItems().size() > 1;
    }

    @Subscribe(id = "reportDc", target = Target.DATA_CONTAINER)
    protected void onReportDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<Report> event) {
        boolean validationOnChanged = event.getProperty().equalsIgnoreCase("validationOn");

        if (validationOnChanged) {
            setValidationScriptGroupBoxCaption(event.getItem().getValidationOn());
        }
    }

    @Subscribe(id = "bandsDc", target = Target.DATA_CONTAINER)
    protected void onBandsDcItemChange(InstanceContainer.ItemChangeEvent<BandDefinition> event) {
        updateBandFieldsAvailability(event.getItem());
        singleDataSetTypeField.setEnabled(event.getItem() != null);

        availableParentBandsDc.getMutableItems().clear();
        if (event.getItem() != null) {
            for (BandDefinition bandDefinition : bandsDc.getItems()) {
                if (!isChildOrEqual(event.getItem(), bandDefinition) ||
                        Objects.equals(event.getItem().getParentBandDefinition(), bandDefinition)) {
                    availableParentBandsDc.getMutableItems().add(bandDefinition);
                }
            }
        }
        BandDefinition item = event.getItem();
        boolean fieldReadOnly = item != null && item.getParent() == null && isUpdatePermitted();

        bandNameField.setReadOnly(fieldReadOnly);
        parentBandField.setReadOnly(fieldReadOnly);
        multiDataSetField.setEnabled(bandsDc.getItemOrNull() != null && dataSetsDc.getItems().size() <= 1);

        updateBandFieldRequiredIndicators(item);
        selectFirstDataSet();
    }

    @Subscribe(id = "bandsDc", target = Target.DATA_CONTAINER)
    protected void onBandsDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<BandDefinition> event) {
        if ("parentBandDefinition".equals(event.getProperty())) {
            BandDefinition previousParent = (BandDefinition) event.getPrevValue();
            BandDefinition parent = (BandDefinition) event.getValue();

            if (event.getValue() == event.getItem()) {
                event.getItem().setParentBandDefinition(previousParent);
            } else {
                if (previousParent != null) {
                    previousParent.getChildrenBandDefinitions().remove(event.getItem());
                    bandsDc.replaceItem(previousParent);
                }
                if (parent != null) {
                    parent.getChildrenBandDefinitions().add(event.getItem());
                    bandsDc.replaceItem(parent);
                }
            }

            if (event.getPrevValue() != null) {
                orderBandDefinitions(previousParent);
            }

            if (event.getValue() != null) {
                orderBandDefinitions(parent);
            }
        }
        if ("name".equals(event.getProperty()) && StringUtils.isBlank((String) event.getValue())) {
            event.getItem().setName("*");
        }
    }

    @Subscribe(id = "bandsDc", target = Target.DATA_CONTAINER)
    protected void onBandsDcCollectionChange(CollectionContainer.CollectionChangeEvent<BandDefinition> event) {
        if (event.getChangeType() == CollectionChangeType.REFRESH) {
            bandsTreeDataGrid.expand(event.getSource().getItems());
        }
    }

    @Subscribe(id = "dataSetsDc", target = Target.DATA_CONTAINER)
    protected void onDataSetsDcItemChange(InstanceContainer.ItemChangeEvent<DataSet> event) {
        DataSet dataSet = event.getItem();

        if (dataSet == null) {
            hideAllDataSetEditComponents();
            return;
        }

        applyVisibilityRules(event.getItem());

        setupEntityParamFieldValue(dataSet);
        setupEntitiesParamFieldValue(dataSet);

        String fetchPlanName = dataSetsDc.getItem().getFetchPlanName();
        if (dataSet.getType() == DataSetType.SINGLE) {
            updateFetchPlanNameFieldItems(findParameterByAlias(dataSet.getEntityParamName()));
            fetchPlanNameField.setValue(Strings.nullToEmpty(fetchPlanName));
        } else if (dataSet.getType() == DataSetType.MULTI) {
            updateFetchPlanNameFieldItems(findParameterByAlias(dataSet.getListEntitiesParamName()));
            fetchPlanNameField.setValue(Strings.nullToEmpty(fetchPlanName));
        }
    }

    @Subscribe(id = "dataSetsDc", target = Target.DATA_CONTAINER)
    protected void onDataSetsDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<DataSet> event) {
        applyVisibilityRules(event.getItem());

        if ("entityParamName".equals(event.getProperty())) {
            setupEntityParamFieldValue(event.getItem());
        }
        if ("listEntitiesParamName".equals(event.getProperty())) {
            setupEntitiesParamFieldValue(event.getItem());
        }
        if ("entityParamName".equals(event.getProperty())
                || "listEntitiesParamName".equals(event.getProperty())) {
            ReportInputParameter linkedParameter = findParameterByAlias(String.valueOf(event.getValue()));
            updateFetchPlanNameFieldItems(linkedParameter);
            dataSetsDc.getItem().setFetchPlanName(null);
        }
        if ("fetchPlanName".equals(event.getProperty())) {
            fetchPlanNameField.setValue(Strings.nullToEmpty((String) event.getValue()));
        }

        if ("processTemplate".equals(event.getProperty())) {
            applyVisibilityRulesForType(event.getItem());
        }
    }

    @Subscribe(id = "dataSetsDc", target = Target.DATA_CONTAINER)
    protected void onDataSetsDcCollectionChange(CollectionContainer.CollectionChangeEvent<DataSet> event) {
        multiDataSetField.setEnabled(bandsDc.getItemOrNull() != null && event.getSource().getItems().size() <= 1);
    }

    @Subscribe(id = "parametersDc", target = Target.DATA_CONTAINER)
    protected void onParametersDcCollectionChange(CollectionContainer.CollectionChangeEvent<ReportInputParameter> event) {
        Map<String, String> paramAliases = new HashMap<>();

        for (ReportInputParameter item : event.getSource().getItems()) {
            paramAliases.put(item.getName(), item.getAlias());
        }
        BiMap<String, String> biMap = ImmutableBiMap.copyOf(paramAliases);

        entitiesParamFieldBinder.setItemsSilently(biMap.values(), true);
        entitiesParamField.setItemLabelGenerator(o -> biMap.inverse().getOrDefault(o, o));

        entityParamFieldBinder.setItemsSilently(biMap.values(), true);
        entityParamField.setItemLabelGenerator(o -> biMap.inverse().getOrDefault(o, o));
    }

    @Subscribe
    protected void onValidation(ValidationEvent event) {
        validateBands(event.getErrors());
    }

    @Subscribe
    protected void onBeforeSave(BeforeSaveEvent event) {
        setupReportXml();
    }

    @Subscribe("multiDataSetField")
    protected void onMultiDataSetFieldComponentValueChange(ComponentValueChangeEvent<Checkbox, Boolean> event) {
        updateDataSetsLayout(event.getValue());
    }

    @Subscribe("orientationField")
    protected void onOrientationFieldComponentValueChange(
            ComponentValueChangeEvent<JmixSelect<Orientation>, Orientation> event) {
        if (bandsDc.getItemOrNull() != null
                && Boolean.FALSE.equals(bandsDc.getItem().getMultiDataSet())
                && bandsDc.getItem().getBandOrientation() == BandOrientation.CROSS) {
            multiDataSetField.setValue(true);
        }
    }

    protected void onFetchPlanNameFieldComponentValueChange(ComponentValueChangeEvent<JmixComboBox<String>, String> event) {
        EntityValues.setValue(dataSetsDc.getItem(), "fetchPlanName", event.getValue());
    }

    protected void onEntityParamFieldComponentValueChange(ComponentValueChangeEvent<JmixComboBox<String>, String> event) {
        EntityValues.setValue(dataSetsDc.getItem(), "entityParamName", event.getValue());
    }

    protected void onEntitiesParamFieldComponentValueChange(ComponentValueChangeEvent<JmixComboBox<String>, String> event) {
        EntityValues.setValue(dataSetsDc.getItem(), "listEntitiesParamName", event.getValue());
    }

    @Subscribe("fetchPlanEditButton")
    protected void onFetchPlanEditButtonClick(ClickEvent<Button> event) {
        // todo implement RegionEditor (EditFetchPlanAction?)
    }

    protected void setupReportXml() {
        String xml = reportsSerialization.convertToString(getEditedEntity());
        getEditedEntity().setXml(xml);
    }

    @Install(target = Target.DATA_CONTEXT)
    protected Set<Object> saveDelegate(SaveContext saveContext) {
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

    @Subscribe("runAction")
    protected void onRunAction(ActionPerformedEvent event) {
        ValidationErrors errors = new ValidationErrors();
        validateBands(errors);
        if (errors.isEmpty()) {
            if (validateInputOutputFormats()) {
                getEditedEntity().setIsTmp(true);

                DialogWindow<InputParametersDialog> inputParametersDialogWindow = dialogWindows.view(this, InputParametersDialog.class)
                        .withAfterCloseListener(e -> bandsTreeDataGrid.focus())
                        .build();

                InputParametersDialog inputParametersDialog = inputParametersDialogWindow.getView();
                inputParametersDialog.setInBackground(reportsClientProperties.getUseBackgroundReportProcessing());
                inputParametersDialog.setReport(getEditedEntity());

                inputParametersDialogWindow.open();
            }
        } else {
            viewValidation.showValidationErrors(errors);
        }
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

    protected void checkForNameDuplication(ValidationErrors errors, Multimap<String, BandDefinition> names) {
        for (String name : names.keySet()) {
            Collection<BandDefinition> bandDefinitionsWithsSameNames = names.get(name);
            if (bandDefinitionsWithsSameNames != null && bandDefinitionsWithsSameNames.size() > 1) {
                errors.add(messageBundle.formatMessage("validation.error.bandNamesDuplicated", name));
            }
        }
    }

    protected void validateBand(ValidationErrors errors, BandDefinition band, Multimap<String, BandDefinition> names) {
        names.put(band.getName(), band);

        if (StringUtils.isBlank(band.getName())) {
            errors.add(messageBundle.getMessage("validation.error.bandNameNull"));
        }

        if (band.getBandOrientation() == BandOrientation.UNDEFINED) {
            errors.add(messageBundle.formatMessage("validation.error.bandOrientationNull", band.getName()));
        }

        if (CollectionUtils.isNotEmpty(band.getDataSets())) {
            for (DataSet dataSet : band.getDataSets()) {
                if (StringUtils.isBlank(dataSet.getName())) {
                    errors.add(messageBundle.getMessage("validation.error.dataSetNameNull"));
                }

                if (dataSet.getType() == null) {
                    errors.add(messageBundle.formatMessage("validation.error.dataSetTypeNull", dataSet.getName()));
                }

                if (dataSet.getType() == DataSetType.GROOVY
                        || dataSet.getType() == DataSetType.SQL
                        || dataSet.getType() == DataSetType.JPQL) {
                    if (StringUtils.isBlank(dataSet.getScript())) {
                        errors.add(messageBundle.formatMessage(
                                "validation.error.dataSetScriptNull", dataSet.getName()));
                    }
                } else if (dataSet.getType() == DataSetType.JSON) {
                    if (StringUtils.isBlank(dataSet.getJsonSourceText())
                            && dataSet.getJsonSourceType() != JsonSourceType.PARAMETER) {
                        errors.add(messageBundle.formatMessage(
                                "validation.error.jsonDataSetScriptNull", dataSet.getName()));
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

    // todo check can we implement
    /*protected void setScreenCaption() {
        if (!StringUtils.isEmpty(getEditedEntity().getName())) {
            getWindow().setCaption(messageBundle.formatMessage("reportEditor.format", getEditedEntity().getName()));
        }
    }*/

    protected boolean validateInputOutputFormats() {
        ReportTemplate template = getEditedEntity().getDefaultTemplate();
        if (template != null && !template.isCustom()
                && template.getReportOutputType() != ReportOutputType.CHART
                && template.getReportOutputType() != ReportOutputType.TABLE
                && template.getReportOutputType() != ReportOutputType.PIVOT_TABLE) {
            String inputType = template.getExt();
            if (!ReportPrintHelper.getInputOutputTypesMapping().containsKey(inputType) ||
                    !ReportPrintHelper.getInputOutputTypesMapping().get(inputType).contains(template.getReportOutputType())) {
                notifications.create(messageBundle.getMessage("validation.error.inputOutputTypesIncompatible"))
                        .withType(Notifications.Type.ERROR)
                        .withPosition(Notification.Position.BOTTOM_END)
                        .show();
                return false;
            }
        }
        return true;
    }

    protected BandDefinition createRootBandDefinition(Report report) {
        BandDefinition rootDefinition = dataContext.create(BandDefinition.class);
        rootDefinition.setName(ROOT_BAND);
        rootDefinition.setPosition(0);
        rootDefinition.setReport(report);

        DataSet dataSet = dataSetFactory.createEmptyDataSet(rootDefinition);
        dataSet.setName(getDefaultDataSetName(rootDefinition));

        rootDefinition.getDataSets().add(dataSet);

        return rootDefinition;
    }

    protected String getDefaultDataSetName(BandDefinition band) {
        return "dataSet" + (band.getDataSets().size() + 1);
    }

    protected void setValidationScriptGroupBoxCaption(Boolean onOffFlag) {
        // todo implement
        /*if (BooleanUtils.isTrue(onOffFlag)) {
            validationScriptGroupBox.setCaption(messageBundle.getMessage("report.validationScriptOn"));
        } else {
            validationScriptGroupBox.setCaption(messageBundle.getMessage("report.validationScriptOff"));
        }*/
    }

    protected void onReportTemplateDetailViewDialogClose(DialogWindow.AfterCloseEvent<ReportTemplateDetailView> event) {
        if (StandardOutcome.SAVE.getCloseAction().equals(event.getCloseAction())) {
            ReportTemplate item = event.getView().getEditedEntity();
            reportDc.getItem().setDefaultTemplate(item);
        }
        defaultTemplateField.focus();
    }

    protected boolean isUpdatePermitted() {
        return secureOperations.isEntityUpdatePermitted(metadata.getClass(Report.class), policyStore);
    }

    protected boolean isTemplateWithoutFile(ReportTemplate template) {
        return template.getOutputType() == JmixReportOutputType.chart ||
                template.getOutputType() == JmixReportOutputType.table ||
                template.getOutputType() == JmixReportOutputType.pivot;
    }

    protected void orderBandDefinitions(BandDefinition parent) {
        if (parent.getChildrenBandDefinitions() != null) {
            List<BandDefinition> childrenBandDefinitions = parent.getChildrenBandDefinitions();
            for (int i = 0, childrenBandDefinitionsSize = childrenBandDefinitions.size(); i < childrenBandDefinitionsSize; i++) {
                BandDefinition bandDefinition = childrenBandDefinitions.get(i);
                bandDefinition.setPosition(i);
            }
            sortBandDefinitionsByPosition();
        }
    }

    public void sortBandDefinitionsByPosition() {
        bandsDc.getSorter().sort(Sort.by(Sort.Direction.ASC, "position"));
    }

    private void removeChildrenCascade(Collection<?> selected) {
        for (Object o : selected) {
            BandDefinition definition = (BandDefinition) o;
            BandDefinition parentDefinition = definition.getParentBandDefinition();
            if (parentDefinition != null) {
                definition.getParentBandDefinition().getChildrenBandDefinitions().remove(definition);
            }

            if (definition.getChildrenBandDefinitions() != null) {
                removeChildrenCascade(new ArrayList<>(definition.getChildrenBandDefinitions()));
            }

            if (definition.getDataSets() != null) {
                bandsDc.setItem(definition);
                for (DataSet dataSet : new ArrayList<>(definition.getDataSets())) {
                    if (entityStates.isNew(dataSet)) {
                        dataSetsDc.getMutableItems().remove(dataSet);
                    }
                }
            }
            bandsDc.getMutableItems().remove(definition);
        }
    }

    protected boolean isUpBandButtonEnabled() {
        BandDefinition selectedItem = bandsTreeDataGrid.getSingleSelectedItem();
        return selectedItem != null && selectedItem.getPosition() > 0 && isUpdatePermitted();
    }

    protected boolean isDownBandButtonEnabled() {
        BandDefinition bandDefinition = bandsTreeDataGrid.getSingleSelectedItem();
        if (bandDefinition != null) {
            BandDefinition parent = bandDefinition.getParentBandDefinition();
            return parent != null &&
                    parent.getChildrenBandDefinitions() != null &&
                    bandDefinition.getPosition() < parent.getChildrenBandDefinitions().size() - 1
                    && isUpdatePermitted();
        }
        return false;
    }

    protected void refreshBandActionStates() {
        bandsTreeDataGrid.getActions().forEach(Action::refreshState);
    }

    protected void refreshDataSetsActionStates() {
        dataSetsDataGrid.getActions().forEach(Action::refreshState);
    }

    protected void updateBandFieldRequiredIndicators(@Nullable BandDefinition item) {
        boolean required = !(item == null || reportDc.getItem().getRootBandDefinition().equals(item));
        parentBandField.setRequired(required);
//        parentBandField.setEmptySelectionAllowed(!required);
        orientationField.setRequired(required);
        orientationField.setEmptySelectionAllowed(!required);
        bandNameField.setRequired(item != null);
    }

    protected void updateBandFieldsAvailability(@Nullable BandDefinition item) {
        boolean enabled = item != null;
        parentBandField.setEnabled(enabled);
        orientationField.setEnabled(enabled);
        bandNameField.setEnabled(enabled);
        multiDataSetField.setEnabled(enabled);
    }

    protected void selectFirstDataSet() {
        if (!dataSetsDc.getItems().isEmpty()) {
            DataSet item = dataSetsDc.getItems().iterator().next();
            dataSetsDataGrid.select(item);
        } else {
            dataSetsDataGrid.deselectAll();
        }
    }

    @SuppressWarnings("unchecked")
    protected void initDataSetsDataGrid() {
        dataGridDecorator.decorate(dataSetsDataGrid, dataSetsDc, bandsDc);
        dataSetsDataGrid
                .addComponentColumn(this::dataSetTypeColumnValueProvider)
                .setHeader(messageBundle.getMessage("bandsTab.dataSetsDataGrid.typeColumn.header"));

        dataSetsDataGrid.getActions().forEach(action -> action.setText(""));
    }

    protected void initBandsTreeDataGrid() {
        bandsTreeDataGrid.getActions().forEach(action -> action.setText(""));
    }

    @SuppressWarnings("unchecked")
    protected Component dataSetTypeColumnValueProvider(DataSet item) {
        JmixComboBox<DataSetType> field = uiComponents.create(JmixComboBox.class);
        field.setItems(DataSetType.class);
        field.setValue(item.getType());
        field.setRequired(true);
        field.setStatusChangeHandler(typedTextFieldStatusContext -> {/*do nothing*/});
        field.setWidthFull();
        field.addValueChangeListener(event -> {
            item.setType(event.getValue());
            // Avoiding bug with not selected edited row
            dataSetsDc.setItem(item);
            dataSetsDataGrid.select(item);
        });
        return field;
    }

    protected void updateDataSetsLayout(boolean isMultiDataSet) {
        singleDataSetTypeField.setVisible(!isMultiDataSet);
        singleDataSetLayout.setVisible(!isMultiDataSet);
        multiDataSetLayout.setVisible(isMultiDataSet);

        dataSetTypeLayout.removeFromParent();
        if (isMultiDataSet) {
            // Caution! Selection helps to avoid generating rows without fields.
            Set<DataSet> selected = dataSetsDataGrid.getSelectedItems();
            dataSetsDataGrid.deselectAll();
            dataSetsDataGrid.select(selected);


            dataSetDetailsLayout.add(dataSetTypeLayout);
        } else {
            singleDataSetLayout.add(dataSetTypeLayout);
        }
    }

    protected boolean isChildOrEqual(BandDefinition definition, @Nullable BandDefinition child) {
        if (definition.equals(child)) {
            return true;
        } else if (child != null) {
            return isChildOrEqual(definition, child.getParentBandDefinition());
        } else {
            return false;
        }
    }

    protected void applyVisibilityRules(DataSet item) {
        applyVisibilityRulesForType(item);
        if (item.getType() == DataSetType.SINGLE || item.getType() == DataSetType.MULTI) {
            applyVisibilityRulesForEntityType(item);
        }
    }

    protected void applyVisibilityRulesForType(DataSet dataSet) {
        hideAllDataSetEditComponents();

        if (dataSet.getType() != null) {
            switch (dataSet.getType()) {
                case SQL:
                case JPQL:
                    dataStoreField.setVisible(true);
                    isProcessTemplateField.setVisible(true);
                    dataSetScriptBox.setVisible(true);
                    break;
                case GROOVY:
                    dataSetScriptBox.setVisible(true);
                    break;
                case SINGLE:
                    // todo rp rename
                    commonEntityGrid.setVisible(true);
                    setCommonEntityGridVisibility(true, false);
                    break;
                case MULTI:
                    commonEntityGrid.setVisible(true);
                    setCommonEntityGridVisibility(false, true);
                    break;
                case JSON:
                    initJsonDataSetOptions(dataSet);
                    jsonDataSetTypeVBox.setVisible(true);
                    break;
            }

            switch (dataSet.getType()) {
                case SQL -> dataSetScriptCodeEditor.setMode(CodeEditorMode.SQL);
                case GROOVY -> dataSetScriptCodeEditor.setMode(CodeEditorMode.GROOVY);
                default -> dataSetScriptCodeEditor.setMode(CodeEditorMode.TEXT);
            }
            dataSetScriptCodeEditorHelpBtn.setVisible(CodeEditorMode.GROOVY.equals(dataSetScriptCodeEditor.getMode()));
        }
    }

    protected void updateFetchPlanNameFieldItems(@Nullable ReportInputParameter reportInputParameter) {
        if (reportInputParameter == null
                || StringUtils.isBlank(reportInputParameter.getEntityMetaClass())) {
            fetchPlanNameFieldBinder.setItemsSilently(Collections.emptyList());
            return;
        }

        MetaClass parameterMetaClass = metadata.getClass(reportInputParameter.getEntityMetaClass());
        Collection<String> fetchPlanNames = new ArrayList<>(fetchPlanRepository.getFetchPlanNames(parameterMetaClass));
        fetchPlanNames.add(FetchPlan.LOCAL);
        fetchPlanNames.add(FetchPlan.INSTANCE_NAME);
        fetchPlanNames.add(FetchPlan.BASE);

        fetchPlanNameFieldBinder.setItemsSilently(fetchPlanNames);
    }

    @Nullable
    protected ReportInputParameter findParameterByAlias(String alias) {
        for (ReportInputParameter reportInputParameter : parametersDc.getItems()) {
            if (reportInputParameter.getAlias().equals(alias)) {
                return reportInputParameter;
            }
        }
        return null;
    }

    protected void hideAllDataSetEditComponents() {
        dataStoreField.setVisible(false);
        isProcessTemplateField.setVisible(false);
        dataSetScriptBox.setVisible(false);
        commonEntityGrid.setVisible(false);
        jsonDataSetTypeVBox.setVisible(false);
    }

    protected void applyVisibilityRulesForEntityType(DataSet item) {
        fetchPlanNameField.setVisible(false);
        fetchPlanEditButton.setVisible(false);
        isUseExistingFetchPlanField.setVisible(false);

        if (Boolean.TRUE.equals(item.getUseExistingFetchPLan())) {
            fetchPlanNameField.setVisible(true);
        } else {
            fetchPlanEditButton.setVisible(true);
        }

        isUseExistingFetchPlanField.setVisible(true);
    }

    protected void initJsonDataSetOptions(DataSet dataSet) {
        setJsonDataSetFieldsVisibility(false);

        jsonSourceTypeField.setVisible(true);
        jsonPathQueryTextAreaField.setVisible(true);

        if (dataSet.getJsonSourceType() == null) {
            dataSet.setJsonSourceType(JsonSourceType.GROOVY_SCRIPT);
        }

        switch (dataSet.getJsonSourceType()) {
            case GROOVY_SCRIPT:
                jsonGroovyCodeEditor.setVisible(true);
                jsonDataSetTypeVBox.expand(jsonGroovyCodeEditor);
                break;
            case URL:
                jsonSourceURLTextArea.setVisible(true);
                jsonDataSetTypeVBox.expand(jsonSourceURLTextArea);
                break;
            case PARAMETER:
                jsonQueryParameterForm.setVisible(true);
                break;
        }
    }

    protected void setJsonDataSetFieldsVisibility(boolean visible) {
        jsonSourceTypeField.setVisible(visible);
        jsonPathQueryTextAreaField.setVisible(visible);
        jsonSourceURLTextArea.setVisible(visible);
        jsonQueryParameterForm.setVisible(visible);
        jsonGroovyCodeEditor.setVisible(visible);
    }

    protected void initDataStoreField() {
        List<String> stores = new ArrayList<>(this.stores.getAdditional());
        stores.add(Stores.MAIN);
        dataStoreField.setItems(new ListDataProvider<>(stores));
        dataStoreField.setItemLabelGenerator(storeName -> {
            if (Stores.MAIN.equals(storeName)) {
                return messageBundle.getMessage("dataSet.dataStoreMain");
            }
            return Strings.nullToEmpty(storeName);
        });
    }

    protected void initEntitiesParamField() {
        entitiesParamField.addCustomValueSetListener(customValueEvent ->
                dataSetsDc.getItem().setListEntitiesParamName(customValueEvent.getDetail()));

        entityParamFieldBinder = new JmixComboBoxBinder(entityParamField);
        entityParamFieldBinder.setValueChangeListener(this::onEntityParamFieldComponentValueChange);
    }

    protected void initEntityParamField() {
        entityParamField.addCustomValueSetListener(customValueEvent ->
                dataSetsDc.getItem().setEntityParamName(customValueEvent.getDetail()));

        entitiesParamFieldBinder = new JmixComboBoxBinder(entitiesParamField);
        entitiesParamFieldBinder.setValueChangeListener(this::onEntitiesParamFieldComponentValueChange);
    }

    protected void initFetchPlanNameField() {
        fetchPlanNameField.addCustomValueSetListener(customValueEvent ->
                dataSetsDc.getItem().setFetchPlanName(customValueEvent.getDetail()));

        fetchPlanNameFieldBinder = new JmixComboBoxBinder(fetchPlanNameField);
        fetchPlanNameFieldBinder.setValueChangeListener(this::onFetchPlanNameFieldComponentValueChange);
    }

    @Subscribe("dataSetScriptFullScreenBtn")
    protected void onDataSetScriptFullScreenBtnClick(ClickEvent<Button> event) {
        onDataSetScriptFieldExpandIconClick();
    }

    @Subscribe("dataSetScriptCodeEditorHelpBtn")
    protected void onDataSetScriptCodeEditorHelpBtnClick(ClickEvent<Button> event) {
        onJsonGroovyCodeEditorHelpIconClick();
    }

    @Subscribe("jsonGroovyCodeCodeEditorHelpBtn")
    protected void onJsonGroovyCodeCodeEditorHelpBtnClick(ClickEvent<Button> event) {
        onJsonGroovyCodeEditorHelpIconClick();
    }

    protected void onDataSetScriptFieldExpandIconClick() {
        reportScriptEditor.create(this)
                .withTitle(getScriptEditorDialogCaption())
                .withValue(dataSetsDc.getItem().getText())
                .withEditorMode(CodeEditorMode.GROOVY)
                .withCloseOnClick(value -> dataSetsDc.getItem().setText(value))
                .withHelpOnClick(this::onJsonGroovyCodeEditorHelpIconClick)
                .open();
    }

    protected void initJsonPathQueryTextAreaField() {
        Icon icon = VaadinIcon.QUESTION_CIRCLE.create();
        icon.addClickListener(this::onJsonPathQueryTextAreaFieldHelpIconClick);
        icon.addClassName(ReportStyleConstants.FIELD_ICON_CLASS_NAME);

        jsonPathQueryTextAreaField.setSuffixComponent(icon);
    }

    protected void onJsonPathQueryTextAreaFieldHelpIconClick(ClickEvent<Icon> event) {
        Html content = new Html(messageBundle.getMessage(
                "bandsTab.dataSetTypeLayout.jsonPathQueryTextAreaField.helpIcon.dialog.content"));
        content.addClassName(ReportStyleConstants.TRANSPARENT_CODE_CLASS_NAME);

        dialogs.createMessageDialog()
                .withHeader(messageBundle.getMessage(
                        "bandsTab.dataSetTypeLayout.jsonPathQueryTextAreaField.helpIcon.dialog.header"))
                .withContent(content)
                .withResizable(true)
                .withModal(false)
                .withWidth("50em")
                .open();
    }

    @Subscribe("jsonGroovyCodeEditorFullScreenBtn")
    public void onJsonGroovyCodeEditorFullScreenBtnClick(final ClickEvent<Button> event) {
        reportScriptEditor.create(this)
                .withTitle(getScriptEditorDialogCaption())
                .withValue(dataSetsDc.getItem().getJsonSourceText())
                .withEditorMode(CodeEditorMode.GROOVY)
                .withCloseOnClick(value -> dataSetsDc.getItem().setJsonSourceText(value))
                .withHelpOnClick(this::onJsonGroovyCodeEditorHelpIconClick)
                .open();
    }

    protected String getScriptEditorDialogCaption() {
        String reportName = reportDc.getItem().getName();
        String bandName = bandsDc.getItem().getName();

        if (ObjectUtils.isNotEmpty(bandName) && ObjectUtils.isNotEmpty(reportName)) {
            return messageBundle.formatMessage(
                    "bandsTab.dataSetTypeLayout.jsonGroovyCodeEditor.expandIcon.dialog.header", reportName, bandName);
        }
        return StringUtils.EMPTY;
    }

    protected void onJsonGroovyCodeEditorHelpIconClick() {
        dialogs.createMessageDialog()
                .withHeader(messageBundle.getMessage(
                        "bandsTab.dataSetTypeLayout.jsonGroovyCodeEditor.helpIcon.dialog.header"))
                .withContent(new Html(messageBundle.getMessage(
                        "bandsTab.dataSetTypeLayout.jsonGroovyCodeEditor.helpIcon.dialog.content")))
                .withResizable(true)
                .withModal(false)
                .withWidth("50em")
                .open();
    }

    protected void setCommonEntityGridVisibility(boolean visibleEntityGrid, boolean visibleEntitiesGrid) {
        entityParamField.setVisible(visibleEntityGrid);
        entitiesParamField.setVisible(visibleEntitiesGrid);
    }
//todo AN implement value provider
//    @Install(to = "inputParametersDataGrid.name", subject = "valueProvider")
//    protected String inputParametersDataGridNameValueProvider(ReportInputParameter parameter) {
//        return metadataTools.getInstanceName(parameter);
//    }

    @Install(to = "inputParametersDataGrid.up", subject = "enabledRule")
    protected boolean inputParametersDataGridUpEnabledRule() {
        if (inputParametersDataGrid != null) {
            ReportInputParameter item = inputParametersDataGrid.getSingleSelectedItem();
            if (item != null) {
                return item.getPosition() > 0 && isUpdatePermitted();
            }
        }

        return false;
    }

    @Subscribe("inputParametersDataGrid.up")
    protected void oninputParametersDataGridUp(ActionPerformedEvent event) {
        replaceParameters(true);
    }

    @Install(to = "inputParametersDataGrid.down", subject = "enabledRule")
    protected boolean inputParametersDataGridDownEnabledRule() {
        if (inputParametersDataGrid != null) {
            ReportInputParameter item = inputParametersDataGrid.getSingleSelectedItem();
            if (item != null) {
                parametersDc.getItems();
                return item.getPosition() < parametersDc.getItems().size() - 1 && isUpdatePermitted();
            }
        }

        return false;
    }

    @Subscribe("inputParametersDataGrid.down")
    protected void oninputParametersDataGridDown(ActionPerformedEvent event) {
        replaceParameters(false);
    }

    @Install(to = "inputParametersDataGrid.createParameter", subject = "initializer")
    protected void inputParametersDataGridCreateInitializer(ReportInputParameter reportInputParameter) {
        reportInputParameter.setReport(reportDc.getItem());
        reportInputParameter.setPosition(parametersDc.getItems().size());
    }


    @Install(to = "inputParametersDataGrid.removeParameter", subject = "afterActionPerformedHandler")
    private void inputParametersDataGridRemoveParameterAfterActionPerformedHandler(RemoveOperation.AfterActionPerformedEvent<ReportInputParameter> afterActionPerformedEvent) {
        orderParameters();
    }

    protected void orderParameters() {
        Report report = reportDc.getItem();
        if (report.getInputParameters() == null) {
            report.setInputParameters(new ArrayList<>());
        }

        for (int i = 0; i < report.getInputParameters().size(); i++) {
            report.getInputParameters().get(i).setPosition(i);
        }
    }

    protected void replaceParameters(boolean up) {
        List<ReportInputParameter> items = parametersDc.getMutableItems();
        ReportInputParameter currentItem = parametersDc.getItem();
        if ((up && currentItem.getPosition() != 0)
                || (!up && currentItem.getPosition() != items.size() - 1)) {

            ReportInputParameter itemToSwap = IterableUtils.find(items,
                    e -> e.getPosition().equals(currentItem.getPosition() - (up ? 1 : -1)));
            int currentPosition = currentItem.getPosition();

            currentItem.setPosition(itemToSwap.getPosition());
            itemToSwap.setPosition(currentPosition);

            Collections.swap(items, itemToSwap.getPosition(), currentItem.getPosition());
        }

    }

    @Install(to = "valuesFormatsDataGrid.createValueFormat", subject = "initializer")
    protected void valuesFormatsDataGridCreateInitializer(ReportValueFormat reportValueFormat) {
        reportValueFormat.setReport(reportDc.getItem());
    }

    protected void initLocaleDetailReportTextField() {
        Icon icon = VaadinIcon.QUESTION_CIRCLE.create();
        icon.addClickListener(this::onLocaleHelpIconClick);
        icon.addClassName(ReportStyleConstants.FIELD_ICON_CLASS_NAME);
        localeTextField.setSuffixComponent(icon);
    }

    protected void onLocaleHelpIconClick(ClickEvent<Icon> event) {
        dialogs.createMessageDialog()
                .withHeader(messageBundle.getMessage("detailsTab.localeFieldHelp.header"))
                .withContent(new Html(messageBundle.getMessage("detailsTab.localeFieldHelp.text")))
                .withResizable(true)
                .withModal(false)
                .withWidth("40em")
                .open();
    }

    @Subscribe("validationScriptFullScreenBtn")
    public void onValidationScriptFullScreenBtnClick(final ClickEvent<Button> event) {
        reportScriptEditor.create(this)
                .withTitle(messageBundle.getMessage("fullScreenBtn.title"))
                .withValue(reportDc.getItem().getValidationScript())
                .withEditorMode(CodeEditorMode.GROOVY)
                .withCloseOnClick(value -> reportDc.getItem().setValidationScript(value))
                .withHelpOnClick(this::onValidationScriptCodeEditorHelpBtnClick)
                .open();
    }

    @Subscribe("validationScriptCodeEditorHelpBtn")
    protected void onValidationScriptCodeEditorHelpBtnClick(ClickEvent<Button> event) {
        onValidationScriptCodeEditorHelpBtnClick();
    }

    protected void onValidationScriptCodeEditorHelpBtnClick() {
        dialogs.createMessageDialog()
                .withHeader(messageBundle.getMessage("parametersTab.validationFieldHelp.header"))
                .withContent(new Html(messageBundle.getMessage("parametersTab.validationFieldHelp.text")))
                .withResizable(true)
                .withModal(false)
                .withWidth("40em")
                .open();
    }

    protected void initScreenIdField() {
        Collection<ViewInfo> viewInfoCollection = viewRegistry.getViewInfos();
        Map<String, String> screens = new LinkedHashMap<>();
        for (ViewInfo windowInfo : viewInfoCollection) {
            String id = windowInfo.getId();
            String menuId = "menu-config." + id;
            String localeMsg = messageBundle.getMessage(menuId);
            String title = menuId.equals(localeMsg) ? id : id + " ( " + localeMsg + " )";
            screens.put(id, title);
        }
        ComponentUtils.setItemsMap(screenIdField, screens);
    }

    protected void initRoleField() {
        Map<BaseRole, String> roles = new LinkedHashMap<>();
        for (BaseRole baseRole : resourceRoleRepository.getAllRoles()) {
            roles.put(baseRole, baseRole.getName());
        }

        ComponentUtils.setItemsMap(rolesField, roles);
    }

    @Install(to = "rolesDataGrid.exclude", subject = "enabledRule")
    protected boolean rolesDataGridExcludeEnabledRule() {
        return isUpdatePermitted();
    }

    @Install(to = "rolesDataGrid.add", subject = "enabledRule")
    protected boolean rolesDataGridAddEnabledRule() {
        return isUpdatePermitted();
    }

    @Subscribe("rolesDataGrid.add")
    public void onrolesDataGridAdd(ActionPerformedEvent event) {
        if (rolesField.getValue() != null) {
            BaseRole role = rolesField.getValue();

            boolean exists = reportRolesDc.getItems()
                    .stream()
                    .anyMatch(reportRole -> role.getCode().equalsIgnoreCase(reportRole.getRoleCode()));

            if (!exists) {
                ReportRole reportRole = metadata.create(ReportRole.class);
                reportRole.setRoleName(role.getName());
                reportRole.setRoleCode(role.getCode());
                reportRolesDc.getMutableItems().add(reportRole);
            }
        }
    }


    @Subscribe("screenDataGrid.add")
    public void onscreenDataGridAdd(ActionPerformedEvent event) {
        if (screenIdField.getValue() != null) {
            String screenId = screenIdField.getValue();

            boolean exists = reportScreensDc.getItems()
                    .stream()
                    .anyMatch(reportScreen -> screenId.equalsIgnoreCase(reportScreen.getScreenId()));

            if (!exists) {
                ReportScreen reportScreen = metadata.create(ReportScreen.class);
                reportScreen.setScreenId(screenId);
                reportScreensDc.getMutableItems().add(reportScreen);
            }
        }
    }

    @Subscribe("templatesDataGrid.create")
    protected void ontemplatesDataGridCreate(ActionPerformedEvent event) {
        View<?> view = UiComponentUtils.getView(this);
        DialogWindow<ReportTemplateDetailView> templateDetailViewDialog = dialogWindows
                .detail(view, ReportTemplate.class)
                .withViewClass(ReportTemplateDetailView.class)
                .withContainer(templatesDc)
                .newEntity()
                .withInitializer(item -> {
                    Report report = reportDc.getItem();
                    item.setReport(report);
                })
                .withAfterCloseListener(this::templatesDataGridCreateAfterSaveHandler)
                .build();
        templateDetailViewDialog.open();
    }

    protected void templatesDataGridCreateAfterSaveHandler(DialogWindow.AfterCloseEvent<ReportTemplateDetailView> event) {
        Report report = reportDc.getItem();
        ReportTemplate defaultTemplate = reportDc.getItem().getDefaultTemplate();
        if (defaultTemplate == null) {
            report.setDefaultTemplate(event.getView().getEditedEntity());
        }
    }

    @Subscribe("templatesDataGrid.edit")
    protected void onTemplatesDataGridEdit(ActionPerformedEvent event) {
        ReportTemplate selectedTemplate = templatesDataGrid.getSingleSelectedItem();
        if (selectedTemplate == null) {
            return;
        }
        DialogWindow<ReportTemplateDetailView> templateDetailViewDialog = dialogWindows.detail(defaultTemplateField)
                .withViewClass(ReportTemplateDetailView.class)
                .withContainer(templatesDc)
                .editEntity(selectedTemplate)
                .build();
        templateDetailViewDialog.open();
    }

    @Install(to = "templatesDataGrid.copy", subject = "enabledRule")
    protected boolean templatesDataGridCopyEnabledRule() {
        if (templatesDataGrid != null) {
            Object selectedItem = templatesDataGrid.getSingleSelectedItem();
            return selectedItem != null && isUpdatePermitted();

        }
        return false;
    }

    @Subscribe("templatesDataGrid.copy")
    protected void ontemplatesDataGridCopy(ActionPerformedEvent event) {
        ReportTemplate template = templatesDataGrid.getSingleSelectedItem();
        if (template != null) {

            ReportTemplate copy = metadataTools.copy(template);
            copy.setId(UuidProvider.createUuid());
            copy.setVersion(null);

            String copyNamingPattern = messageBundle.getMessage("template.copyNamingPattern");
            String copyCode = String.format(copyNamingPattern, StringUtils.isEmpty(copy.getCode())
                    ? StringUtils.EMPTY
                    : copy.getCode());

            List<String> codes = templatesDc.getItems().stream()
                    .map(ReportTemplate::getCode)
                    .filter(o -> !StringUtils.isEmpty(o))
                    .collect(Collectors.toList());
            if (codes.contains(copyCode)) {
                String code = copyCode;
                int i = 0;
                while ((codes.contains(code))) {
                    i += 1;
                    code = copyCode + " " + i;
                }
                copyCode = code;
            }
            copy.setCode(copyCode);

            templatesDc.getMutableItems().add(copy);
        }
    }


    @Install(to = "templatesDataGrid.defaultAction", subject = "enabledRule")
    protected boolean templatesDataGridDefaultEnabledRule() {
        if (templatesDataGrid != null) {
            ReportTemplate selectedItem = templatesDataGrid.getSingleSelectedItem();
            if (selectedItem != null) {
                return !Objects.equals(reportDc.getItem().getDefaultTemplate(), selectedItem) && isUpdatePermitted();
            }
        }

        return false;
    }

    @Subscribe("templatesDataGrid.defaultAction")
    protected void ontemplatesDataGridDefault(ActionPerformedEvent event) {
        ReportTemplate template = templatesDataGrid.getSingleSelectedItem();
        if (template != null) {
            reportDc.getItem().setDefaultTemplate(template);
        }
        event.getSource().refreshState();

        templatesDataGrid.focus();
        templatesDataGrid.getDataProvider().refreshAll();
    }

    protected void setupEntityParamFieldValue(DataSet dataSet) {
        entityParamField.setValue(dataSet.getEntityParamName());
    }

    protected void setupEntitiesParamFieldValue(DataSet dataSet) {
        entitiesParamField.setValue(dataSet.getListEntitiesParamName());
    }

    /**
     * Class wraps ComboBox value change listener and manages its registration.
     *
     * @param <V> type of field value
     */
    protected static class JmixComboBoxBinder<V> {

        protected JmixComboBox<V> comboBox;
        protected Registration valueChangeRegistration;
        protected ValueChangeListener<ComponentValueChangeEvent<JmixComboBox<V>, V>> listener;

        public JmixComboBoxBinder(JmixComboBox<V> comboBox) {
            this.comboBox = comboBox;
        }

        public void setValueChangeListener(ValueChangeListener<ComponentValueChangeEvent<JmixComboBox<V>, V>> listener) {
            this.listener = listener;
            bind();
        }

        /**
         * Sets items without triggering {@link ComponentValueChangeEvent}.
         * <p>
         * Note that field's value will be unset.
         *
         * @param items items to set
         */
        public void setItemsSilently(Collection<V> items) {
            setItemsSilently(items, false);
        }

        /**
         * Sets items without triggering {@link ComponentValueChangeEvent}. If the {@code restoreValue} parameter is
         * {@code true}, the field's value will be restored.
         *
         * @param items        items to set
         * @param restoreValue whether field's value should be restored after setting items
         */
        public void setItemsSilently(Collection<V> items, boolean restoreValue) {
            unbind();

            V value = comboBox.getValue();

            comboBox.setItems(items);

            if (restoreValue) {
                comboBox.setValue(value);
            }

            bind();
        }

        @SuppressWarnings("unchecked")
        private void bind() {
            valueChangeRegistration = comboBox.addValueChangeListener((ValueChangeListener) listener);
        }

        private void unbind() {
            if (valueChangeRegistration != null) {
                valueChangeRegistration.remove();
                valueChangeRegistration = null;
            }
        }
    }
}