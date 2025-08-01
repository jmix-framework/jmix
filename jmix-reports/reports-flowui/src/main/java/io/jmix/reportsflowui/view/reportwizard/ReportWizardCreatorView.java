package io.jmix.reportsflowui.view.reportwizard;


import com.google.common.collect.Lists;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.router.Route;
import io.jmix.core.*;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.data.QueryParser;
import io.jmix.data.QueryTransformerFactory;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.component.codeeditor.CodeEditor;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.radiobuttongroup.JmixRadioButtonGroup;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.download.ByteArrayDownloadDataProvider;
import io.jmix.flowui.download.DownloadFormat;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.codeeditor.CodeEditorMode;
import io.jmix.flowui.model.*;
import io.jmix.flowui.view.*;
import io.jmix.reports.app.EntityTree;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.wizard.*;
import io.jmix.reports.exception.TemplateGenerationException;
import io.jmix.reports.libintegration.JmixObjectToStringConverter;
import io.jmix.reportsflowui.ReportsClientProperties;
import io.jmix.reportsflowui.download.ReportDownloader;
import io.jmix.reportsflowui.helper.PackageHelper;
import io.jmix.reportsflowui.helper.ReportScriptEditor;
import io.jmix.reportsflowui.runner.FluentUiReportRunner;
import io.jmix.reportsflowui.runner.ParametersDialogShowMode;
import io.jmix.reportsflowui.runner.UiReportRunner;
import io.jmix.reportsflowui.view.entitytreelist.EntityTreeNodeListView;
import io.jmix.reportsflowui.view.region.ReportRegionWizardDetailView;
import io.jmix.reportsflowui.view.reportwizard.template.query.JpqlQueryBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.text.SimpleDateFormat;
import java.util.*;

@Route(value = "reports/wizard", layout = DefaultMainViewParent.class)
@DialogMode(draggable = false, width = "45em")
@ViewController("report_ReportWizardCreatorView")
@ViewDescriptor("report-wizard-creator-view.xml")
public class ReportWizardCreatorView extends StandardView {
    protected static final int MAX_ATTRS_BTN_CAPTION_WIDTH = 135;

    @ViewComponent
    protected InstanceContainer<ReportData> reportDataDc;
    @ViewComponent
    protected DataContext dataContext;
    @ViewComponent
    protected JmixComboBox<TemplateFileType> templateFileTypeField;
    @ViewComponent
    protected JmixComboBox<ReportOutputType> outputFileFormat;
    @ViewComponent
    protected TypedTextField<String> outputFileName;
    @ViewComponent
    protected JmixButton downloadTemplateFileBtn;
    @ViewComponent
    protected JmixButton moveUpBtn;
    @ViewComponent
    protected JmixButton moveDownBtn;
    @ViewComponent
    protected VerticalLayout detailsVBox;
    @ViewComponent
    protected VerticalLayout saveVBox;
    @ViewComponent
    protected VerticalLayout queryVBox;
    @ViewComponent
    protected DataGrid<ReportRegion> regionDataGrid;
    @ViewComponent
    protected DataGrid<QueryParameter> reportParameterDataGrid;
    @ViewComponent
    protected HorizontalLayout buttonsBox;
    @ViewComponent
    protected JmixButton addRegionDisabledBtn;
    @ViewComponent
    protected JmixRadioButtonGroup<ReportTypeGenerate> reportTypeGenerateField;
    @ViewComponent
    protected CodeEditor reportQueryCodeEditor;
    @ViewComponent
    protected Span wizardDescriptionSpan;
    @ViewComponent
    protected CollectionPropertyContainer<QueryParameter> queryParametersDc;
    @ViewComponent
    protected JmixButton regionsRunBtn;
    @ViewComponent
    protected VerticalLayout regionsVBox;
    @ViewComponent
    protected JmixButton addTabulatedRegionBtn;
    @ViewComponent
    protected JmixButton addSimpleRegionBtn;
    @ViewComponent
    protected CollectionPropertyContainer<ReportRegion> reportRegionsDc;
    @ViewComponent
    protected JmixButton nextBtn;
    @ViewComponent
    protected JmixButton backBtn;
    @ViewComponent
    protected JmixButton saveBtn;
    @ViewComponent
    protected JmixComboBox<MetaClass> entityField;

    @Autowired
    protected Messages messages;
    @Autowired
    protected MessageBundle messageBundle;
    @Autowired
    protected Dialogs dialogs;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected ExtendedEntities extendedEntities;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected ReportWizard reportWizardService;
    @Autowired
    protected UiProperties uiProperties;
    @Autowired
    protected CoreProperties coreProperties;
    @Autowired
    protected QueryTransformerFactory queryTransformerFactory;
    @Autowired
    protected ReportDownloader downloader;
    @Autowired
    protected OutputFormatTools outputFormatTools;
    @Autowired
    protected JmixObjectToStringConverter jmixObjectToStringConverter;
    @Autowired
    protected ReportScriptEditor reportScriptEditor;
    @Autowired
    protected UiReportRunner uiReportRunner;
    @Autowired
    protected ReportsClientProperties reportsClientProperties;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected DialogWindows dialogWindows;
    @Autowired
    protected FormatStringsRegistry formatStringsRegistry;
    @Autowired
    protected CurrentAuthentication currentAuthentication;
    @Autowired
    protected ViewValidation viewValidation;
    @Autowired
    private PackageHelper packageHelper;

    protected int currentFragmentIdx = 0;
    protected boolean regenerateQuery = false;
    protected boolean needUpdateEntityModel = false;
    protected boolean entityTreeHasSimpleAttrs;
    protected boolean entityTreeHasCollections;
    protected Report lastGeneratedTmpReport;
    protected List<VerticalLayout> fragmentsList;
    protected Map<VerticalLayout, String> fragmentDescriptionMap = new HashMap<>();

    @Subscribe
    public void onInit(InitEvent event) {
        initItem();
        initFragments();
        initFragmentDescription();
        initReportTypeOptionGroup();
        initTemplateFormatLookupField();
        initEntityLookupField();
        initReportParameterDataGrid();
        updateWizardDescription();
    }

    protected void initItem() {
        reportDataDc.setItem(dataContext.create(ReportData.class));
    }

    protected List<VerticalLayout> getFragmentsList() {
        return Lists.newArrayList(detailsVBox,
                regionsVBox,
                saveVBox
        );
    }

    protected void initFragments() {
        detailsVBox.setVisible(true);
        saveVBox.setVisible(false);
        regionsVBox.setVisible(false);
        queryVBox.setVisible(false);
        fragmentsList = getFragmentsList();
    }

    @Subscribe("reportNameField")
    public void onReportNameFieldTypedValueChange(final SupportsTypedValue.TypedValueChangeEvent<TypedTextField<String>, String> event) {
        if (!StringUtils.isEmpty(event.getValue())) {
            outputFileName.setTypedValue(generateOutputFileName(reportDataDc.getItem().getTemplateFileType().toString(),
                    getReportTypeGenerate()));
        }
    }

    protected void beforeShowFragments() {
        VerticalLayout vbox = fragmentsList.get(currentFragmentIdx);
        if (vbox.equals(regionsVBox)) {
            addRegionDescription();
            updateRegionButtons();
            showAddRegion();
            updateFragmentChangeButtons();
        } else if (vbox.equals(queryVBox)) {
            ReportData item = reportDataDc.getItem();
            String resultQuery = item.getQuery();
            if (StringUtils.isEmpty(resultQuery) || regenerateQuery) {
                item.setQuery(String.format("select e from %s e", item.getEntityName()));
                if (CollectionUtils.isNotEmpty(item.getReportRegions())) {
                    resultQuery = new JpqlQueryBuilder(item, item.getReportRegions().get(0)).buildInitialQuery();
                }
                queryParametersDc.getMutableItems().clear();
                regenerateQuery = false;
            }
            reportQueryCodeEditor.setValue(resultQuery);
        }
    }

    public void addRegionDescription() {
        fragmentDescriptionMap.put(regionsVBox, getRegionDescription());
    }

    public String getRegionDescription() {
        String regionDescription;
        if (getReportTypeGenerate() != null && getReportTypeGenerate().isList()) {
            MetaClass entityMetaClass = metadata.getClass(reportDataDc.getItem().getEntityName());
            regionDescription = messageBundle.formatMessage("regionTabulatedMessage",
                    messageTools.getEntityCaption(entityMetaClass));
        } else {
            regionDescription = messageBundle.getMessage("addPropertiesAndTableAreas");
        }
        return regionDescription;
    }

    public void initFragmentDescription() {
        fragmentDescriptionMap.put(detailsVBox, messageBundle.getMessage("enterMainParameters"));
        fragmentDescriptionMap.put(queryVBox, messageBundle.getMessage("enterQuery"));
        fragmentDescriptionMap.put(saveVBox, messageBundle.getMessage("finishPrepareReport"));
    }

    public void updateWizardDescription() {
        wizardDescriptionSpan.setText(messageBundle.formatMessage("stepNo",
                fragmentDescriptionMap.get(getCurrentFragment()),
                currentFragmentIdx + 1,
                fragmentsList.size()));
    }

    @Subscribe(id = "reportDataDc", target = Target.DATA_CONTAINER)
    public void onReportDataDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<ReportData> event) {
        ReportData reportData = reportDataDc.getItem();
        if ("reportTypeGenerate".equals(event.getProperty())) {
            List<VerticalLayout> stepFragments = getFragmentsList();
            if (Objects.equals(event.getValue(), ReportTypeGenerate.LIST_OF_ENTITIES_WITH_QUERY)) {
                stepFragments.add(2, queryVBox);
            }
            fragmentsList = stepFragments;
            if (StringUtils.isNotBlank(reportData.getEntityName()))
                updateReportOutputName(reportData, metadata.getClass(reportData.getEntityName()));
        }
        if ("entityName".equals(event.getProperty())) {
            updateDownloadTemplateFile();
            outputFileName.setTypedValue(generateOutputFileName(reportDataDc.getItem().getTemplateFileType().toString(),
                    getReportTypeGenerate()));
        }
        if ("templateFileType".equals(event.getProperty())
                && reportDataDc.getItem().getTemplateFileType() != null) {
            updateCorrectReportOutputType((TemplateFileType) event.getValue());
            updateDownloadTemplateFile();
            outputFileName.setTypedValue(generateOutputFileName(reportDataDc.getItem().getTemplateFileType().toString(),
                    getReportTypeGenerate()));
        }
    }

    @Subscribe("nextBtn")
    public void onNextBtnClick(ClickEvent<Button> event) {
        if (!validateFragment()) {
            return;
        }

        MetaClass metaClass = metadata.findClass(getItem().getEntityName());
        if (isNeedUpdateEntityModel()) {
            EntityTree entityTree = reportWizardService.buildEntityTree(metaClass);

            setEntityTreeHasSimpleAttrs(entityTree.getEntityTreeStructureInfo().isEntityTreeHasSimpleAttrs());
            setEntityTreeHasCollections(entityTree.getEntityTreeStructureInfo().isEntityTreeRootHasCollections());

            getItem().setEntityTreeRootNode(entityTree.getEntityTreeRootNode());
            setNeedUpdateEntityModel(false);
        }
        nextFragment();
    }

    @Subscribe("backBtn")
    public void onBackBtnClick(ClickEvent<Button> event) {
        prevFragment();
    }

    protected void updateFragmentChangeButtons() {
        if (currentFragmentIdx == 0) {
            backBtn.setVisible(false);
            saveBtn.setVisible(false);
            downloadTemplateFileBtn.setVisible(false);
        } else if (currentFragmentIdx == fragmentsList.size() - 1) {
            saveBtn.setVisible(true);
            downloadTemplateFileBtn.setVisible(true);
            nextBtn.setVisible(false);
        } else {
            backBtn.setVisible(true);
            nextBtn.setVisible(true);
            saveBtn.setVisible(false);
            downloadTemplateFileBtn.setVisible(false);
        }
    }

    protected void nextFragment() {
        if (currentFragmentIdx < fragmentsList.size() - 1) {
            fragmentsList.get(currentFragmentIdx).setVisible(false);
            currentFragmentIdx++;
            beforeShowFragments();
            fragmentsList.get(currentFragmentIdx).setVisible(true);
        }
        updateWizardDescription();
        updateFragmentChangeButtons();
    }

    protected void prevFragment() {
        if (currentFragmentIdx > 0) {
            fragmentsList.get(currentFragmentIdx).setVisible(false);
            currentFragmentIdx--;
            beforeShowFragments();
            fragmentsList.get(currentFragmentIdx).setVisible(true);
        }
        updateWizardDescription();
        updateFragmentChangeButtons();
    }

    public boolean isNeedUpdateEntityModel() {
        return needUpdateEntityModel;
    }

    public void setNeedUpdateEntityModel(boolean needUpdateEntityModel) {
        this.needUpdateEntityModel = needUpdateEntityModel;
    }

    protected VerticalLayout getCurrentFragment() {
        return fragmentsList.get(currentFragmentIdx);
    }

    protected boolean validateFragment() {
        VerticalLayout currentFragment = getCurrentFragment();

        ValidationErrors validationErrors = viewValidation.validateUiComponents(currentFragment);
        if (currentFragment.equals(regionsVBox)) {
            if (reportDataDc.getItem().getReportRegions().isEmpty()) {
                validationErrors.add(messageBundle.getMessage("addRegionsWarn.message"));
            }
        }

        if (!validationErrors.isEmpty()) {
            viewValidation.showValidationErrors(validationErrors);
        }
        return validationErrors.isEmpty();
    }

    @Subscribe("saveBtn")
    public void onSaveBtnClick(ClickEvent<Button> event) {
        if (!validateFragment()) {
            return;
        }

        if (reportDataDc.getItem().getReportRegions().isEmpty()) {
            dialogs.createOptionDialog()
                    .withHeader(messageBundle.getMessage("dialogConfirmation.header"))
                    .withText(messageBundle.getMessage("saveReport.confirmSaveWithoutRegions.text"))
                    .withActions(
                            new DialogAction(DialogAction.Type.OK)
                                    .withVariant(ActionVariant.PRIMARY)
                                    .withHandler(handle -> convertToReportAndForceCloseWizard()),
                            new DialogAction(DialogAction.Type.NO)
                    ).open();
        } else {
            convertToReportAndForceCloseWizard();
        }
    }

    protected void convertToReportAndForceCloseWizard() {
        Report report = buildReport(false);
        if (report != null) {
            close(StandardOutcome.SAVE);
        }
    }

    @Nullable
    public Report buildReport(boolean temporary) {
        ReportData reportData = reportDataDc.getItem();

        // be sure that reportData.name and reportData.outputFileFormat is not null before generation of template
        try {
            byte[] templateByteArray = reportWizardService.generateTemplate(reportData, reportData.getTemplateFileType());
            reportData.setTemplateContent(templateByteArray);
        } catch (TemplateGenerationException e) {
            notifications.create(messageBundle.getMessage("templateGenerationException.message"))
                    .withType(Notifications.Type.DEFAULT)
                    .withPosition(Notification.Position.BOTTOM_END)
                    .show();
            return null;
        }

        MetaClass entityMetaClass = metadata.getClass(reportData.getEntityName());
        String storeName = entityMetaClass.getStore().getName();

        if (!Stores.isMain(storeName)) {
            reportData.setDataStore(storeName);
        }

        Report report = reportWizardService.toReport(reportData, temporary);
        reportData.setGeneratedReport(report);
        return report;
    }


    public ReportData getItem() {
        return reportDataDc.getItem();
    }

    @Subscribe
    public void onBeforeClose(BeforeCloseEvent event) {
        CloseAction closeAction = event.getCloseAction();
        boolean checkUnsavedChanges = closeAction instanceof ChangeTrackerCloseAction
                && ((ChangeTrackerCloseAction) closeAction).isCheckForUnsavedChanges();

        if (!event.closedWith(StandardOutcome.SAVE) && checkUnsavedChanges
                && CollectionUtils.isNotEmpty(reportRegionsDc.getItems())) {
            dialogs.createOptionDialog()
                    .withHeader(messageBundle.getMessage("dialogConfirmation.header"))
                    .withText(messageBundle.getMessage("beforeClose.interruptConfirm.text"))
                    .withActions(
                            new DialogAction(DialogAction.Type.OK)
                                    .withVariant(ActionVariant.PRIMARY)
                                    .withHandler(handle -> close(StandardOutcome.DISCARD)),
                            new DialogAction(DialogAction.Type.NO)
                    ).open();
            event.preventClose();
        }
    }

    protected void initEntityLookupField() {
        ComponentUtils.setItemsMap(entityField, getAvailableEntities());
    }

    @Subscribe("reportTypeGenerateField")
    public void onReportTypeGenerateFieldComponentValueChange(AbstractField.ComponentValueChangeEvent<JmixRadioButtonGroup, Object> event) {
        ReportData reportData = reportDataDc.getItem();
        ReportTypeGenerate currentType = (ReportTypeGenerate) event.getValue();
        updateReportTypeGenerate(reportData, currentType);
        updateWizardDescription();
    }

    protected void updateReportTypeGenerate(ReportData reportData, @Nullable ReportTypeGenerate reportTypeGenerate) {
        reportData.setReportTypeGenerate(reportTypeGenerate);
        reportRegionsDc.getMutableItems().clear();

        clearQuery();
    }


    @Subscribe("entityField")
    public void onEntityFieldComponentValueChange(AbstractField.ComponentValueChangeEvent<JmixComboBox, MetaClass> event) {
        ReportData reportData = reportDataDc.getItem();
        updateReportEntity(event.getOldValue(), event.getValue(), reportData);
    }

    protected void updateReportEntity(@Nullable MetaClass prevValue, MetaClass value, ReportData reportData) {
        if (value != null) {
            needUpdateEntityModel = true;
            setReportName(reportData, prevValue, value);

            reportRegionsDc.getMutableItems().clear();
            reportData.setEntityName(value.getName());

            clearQuery();
        }
    }

    protected void setReportName(ReportData reportData, @Nullable MetaClass prevValue, MetaClass value) {
        String oldName = reportData.getName();
        if (StringUtils.isBlank(oldName)) {
            updateReportOutputName(reportData, value);
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
                    if (!oldName.equals(messageBundle.formatMessage("reportData.reportNamePattern", prevEntityCaption))) {
                        //if user changed auto generated report name and we have changed it, we show message to him
                        notifications.create(messageBundle.getMessage("reportData.reportNameChanged.message"))
                                .withType(Notifications.Type.DEFAULT)
                                .withPosition(Notification.Position.BOTTOM_END)
                                .show();
                    }
                }
            }
        }
    }

    protected void updateReportOutputName(ReportData reportData, MetaClass value) {
        if (ReportTypeGenerate.SINGLE_ENTITY == getReportTypeGenerate()) {
            reportData.setName(messageTools.getEntityCaption(value));
        } else {
            reportData.setName(messageBundle.formatMessage(
                    "downloadEntityListNamePattern",
                    messageTools.getEntityCaption(value)));
        }
    }

    @Subscribe("templateFileTypeField")
    public void onTemplateFileTypeFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<JmixComboBox<TemplateFileType>, TemplateFileType> event) {
        updateDownloadTemplateFile();
    }


    protected void initTemplateFormatLookupField() {
        ComponentUtils.setItemsMap(templateFileTypeField, getAvailableTemplateFormats());
        templateFileTypeField.setAllowCustomValue(false);
        templateFileTypeField.setValue(TemplateFileType.XLSX);
    }

    protected void initReportTypeOptionGroup() {
        reportTypeGenerateField.setItems(ReportTypeGenerate.LIST_OF_ENTITIES_WITH_QUERY, ReportTypeGenerate.LIST_OF_ENTITIES, ReportTypeGenerate.SINGLE_ENTITY);
        reportTypeGenerateField.setItemLabelGenerator(this::itemLabelGenerator);
        reportTypeGenerateField.setValue(ReportTypeGenerate.LIST_OF_ENTITIES_WITH_QUERY);
    }

    protected String itemLabelGenerator(ReportTypeGenerate reportTypeGenerate) {
        return switch (reportTypeGenerate) {
            case SINGLE_ENTITY -> messageBundle.getMessage("singleEntityReport");
            case LIST_OF_ENTITIES -> messageBundle.getMessage("listOfEntitiesReport");
            case LIST_OF_ENTITIES_WITH_QUERY -> messageBundle.getMessage("listOfEntitiesReportWithQuery");
        };

    }

    protected Map<TemplateFileType, String> getAvailableTemplateFormats() {
        return Map.of(TemplateFileType.XLSX, messages.getMessage(TemplateFileType.XLSX),
                TemplateFileType.DOCX, messages.getMessage(TemplateFileType.DOCX),
                TemplateFileType.HTML, messages.getMessage(TemplateFileType.HTML),
                TemplateFileType.CSV, messages.getMessage(TemplateFileType.CSV),
                TemplateFileType.TABLE, messages.getMessage(TemplateFileType.TABLE));
    }

    protected Map<MetaClass, String> getAvailableEntities() {
        Map<MetaClass, String> result = new LinkedHashMap<>();

        Map<MetaClass, String> projectEntities = new TreeMap<>(Comparator.comparing(MetaClass::getName));
        Map<MetaClass, String> externalEntities = new TreeMap<>(Comparator.comparing(MetaClass::getName));

        Optional<String> basePackageOpt = packageHelper.getBasePackage();

        Collection<MetaClass> classes = metadataTools.getAllJpaEntityMetaClasses();
        for (MetaClass metaClass : classes) {
            MetaClass effectiveMetaClass = extendedEntities.getEffectiveMetaClass(metaClass);
            if (!reportWizardService.isEntityAllowedForReportWizard(effectiveMetaClass)) {
                continue;
            }

            String caption = getEntityCaption(effectiveMetaClass);
            Package pack = metaClass.getJavaClass().getPackage();
            if (basePackageOpt.isPresent() && pack.getName().startsWith(basePackageOpt.get())) {
                projectEntities.put(effectiveMetaClass, caption);
            } else {
                externalEntities.put(effectiveMetaClass, caption);
            }
        }

        result.putAll(projectEntities);
        result.putAll(externalEntities);

        return result;
    }

    private String getEntityCaption(MetaClass effectiveMetaClass) {
        String entityCaption = messageTools.getEntityCaption(effectiveMetaClass);
        return entityCaption + " (" + effectiveMetaClass.getName() + ")";
    }

    protected void clearQuery() {
        ReportData reportData = reportDataDc.getItem();
        reportData.setQuery(null);
        reportData.setQueryParameters(null);
    }

    public void setEntityTreeHasCollections(boolean entityTreeHasCollections) {
        this.entityTreeHasCollections = entityTreeHasCollections;
    }

    public void setEntityTreeHasSimpleAttrs(boolean entityTreeHasSimpleAttrs) {
        this.entityTreeHasSimpleAttrs = entityTreeHasSimpleAttrs;
    }

    @Nullable
    protected ReportTypeGenerate getReportTypeGenerate() {
        return reportDataDc.getItem().getReportTypeGenerate();
    }

    @Subscribe("regionDataGrid.remove")
    public void onRegionDataGridRemoveItemAction(ActionPerformedEvent event) {
        for (ReportRegion item : regionDataGrid.getSelectedItems()) {
            reportRegionsDc.getMutableItems().remove(item);
            normalizeRegionPropertiesOrderNum();
        }
        regionDataGrid.deselectAll();
    }

    @Subscribe("regionDataGrid.edit")
    public void onRegionDataGridEditItemAction(ActionPerformedEvent event) {
        editRegion();
    }

    @Subscribe("regionsRunBtn")
    public void onRegionsRunBtnClick(ClickEvent<Button> event) {
        if (reportDataDc.getItem().getReportRegions().isEmpty()) {
            notifications.create(messageBundle.getMessage("addRegionsWarn.message"))
                    .withType(Notifications.Type.DEFAULT)
                    .withPosition(Notification.Position.BOTTOM_END)
                    .show();
            return;
        }

        lastGeneratedTmpReport = buildReport(true);

        if (lastGeneratedTmpReport != null) {
            FluentUiReportRunner fluentRunner = uiReportRunner.byReportEntity(lastGeneratedTmpReport)
                    .withParametersDialogShowMode(ParametersDialogShowMode.IF_REQUIRED);
            if (reportsClientProperties.getUseBackgroundReportProcessing()) {
                fluentRunner.inBackground(this);
            }
            fluentRunner.runAndShow();
        }
    }

    @Install(to = "regionDataGrid.up", subject = "enabledRule")
    protected boolean regionDataGridUpEnabledRule() {
        ReportRegion item = regionDataGrid.getSingleSelectedItem();
        if (item == null) {
            return false;
        }
        return item.getOrderNum() > 1;
    }

    @Install(to = "regionDataGrid.down", subject = "enabledRule")
    protected boolean regionDataGridDownEnabledRule() {
        ReportRegion item = regionDataGrid.getSingleSelectedItem();
        if (item == null) {
            return false;
        }
        return item.getOrderNum() < reportRegionsDc.getItems().size();
    }

    protected void editRegion() {
        ReportRegion selectedRegion = regionDataGrid.getSingleSelectedItem();
        if (selectedRegion != null) {
            showRegionEditor(selectedRegion, selectedRegion.getRegionPropertiesRootNode(), true, false, ReportTypeGenerate.LIST_OF_ENTITIES_WITH_QUERY == getReportTypeGenerate());
        }
    }

    @Subscribe("addTabulatedRegionBtn")
    public void onAddTabulatedRegionBtnClick(ClickEvent<Button> event) {
        openTabulatedRegionEditor(createReportRegion(true));
    }

    @Subscribe("addSimpleRegionBtn")
    public void onAddSimpleRegionBtnClick(ClickEvent<Button> event) {
        openRegionEditor(createReportRegion(false));
    }

    protected ReportRegion createReportRegion(boolean tabulated) {
        ReportRegion reportRegion = metadata.create(ReportRegion.class);
        reportRegion.setReportData(reportDataDc.getItem());
        reportRegion.setIsTabulatedRegion(tabulated);
        reportRegion.setOrderNum((long) reportDataDc.getItem().getReportRegions().size() + 1L);
        return reportRegion;
    }

    protected void showAddRegion() {
        if (reportRegionsDc.getItems().isEmpty()) {
            if (getReportTypeGenerate() != null && getReportTypeGenerate().isList()) {
                if (entityTreeHasSimpleAttrs) {
                    openTabulatedRegionEditor(createReportRegion(true));
                }
            } else {
                if (entityTreeHasSimpleAttrs) {
                    openRegionEditor(createReportRegion(false));
                } else if (entityTreeHasCollections) {
                    openTabulatedRegionEditor(createReportRegion(true));
                }
            }
        }
    }

    protected void openTabulatedRegionEditor(final ReportRegion item) {
        if (ReportTypeGenerate.SINGLE_ENTITY == getReportTypeGenerate()) {
            openRegionEditorOnlyWithNestedCollections(item);
        } else {
            openRegionEditor(item);
        }
    }

    protected void openRegionEditorOnlyWithNestedCollections(final ReportRegion item) {
        //show lookup for choosing parent collection for tabulated region
        DialogWindow<EntityTreeNodeListView> entityTreeListDialogWindow = dialogWindows
                .lookup(this, EntityTreeNode.class)
                .withViewClass(EntityTreeNodeListView.class)
                .build();
        EntityTreeNodeListView entityTreeNodeListView = entityTreeListDialogWindow.getView();
        entityTreeNodeListView.setParameters(
                reportDataDc.getItem().getEntityTreeRootNode(), false, true, false);
        entityTreeNodeListView.setSelectionHandler(items -> {
            if (items.size() == 1) {
                EntityTreeNode regionPropertiesRootNode = IterableUtils.get(items, 0);

                item.setRegionPropertiesRootNode(regionPropertiesRootNode);

                showRegionEditor(item, regionPropertiesRootNode, true, false, false);
            }
        });
        entityTreeListDialogWindow.open();
    }

    protected void openRegionEditor(ReportRegion item) {
        item.setRegionPropertiesRootNode(reportDataDc.getItem().getEntityTreeRootNode());

        showRegionEditor(item, reportDataDc.getItem().getEntityTreeRootNode(), true, false,
                ReportTypeGenerate.LIST_OF_ENTITIES_WITH_QUERY == getReportTypeGenerate());
    }

    protected void showRegionEditor(ReportRegion item, EntityTreeNode rootEntity, boolean scalarOnly, boolean collectionsOnly, boolean persistentOnly) {
        DialogWindow<ReportRegionWizardDetailView> regionDialogWindow = dialogWindows.detail(regionDataGrid)
                .withViewClass(ReportRegionWizardDetailView.class)
                .withContainer(reportRegionsDc)
                .editEntity(item)
                .build();

        ReportRegionWizardDetailView reportRegionWizardDetailView = regionDialogWindow.getView();
        reportRegionWizardDetailView.setParameters(rootEntity, scalarOnly, collectionsOnly, persistentOnly);
        reportRegionWizardDetailView.setShowSaveNotification(false);
        regionDialogWindow.open();
    }

    protected void normalizeRegionPropertiesOrderNum() {
        long normalizedIdx = 0;
        List<ReportRegion> allItems = reportRegionsDc.getMutableItems();
        for (ReportRegion item : allItems) {
            item.setOrderNum(++normalizedIdx); //first must be 1
        }
    }

    @Subscribe("regionDataGrid")
    protected void onRegionDataGridSelection(SelectionEvent<DataGrid<ReportRegion>, ReportRegion> event) {
        updateMoveButtons();
    }

    @Subscribe("regionDataGrid.up")
    protected void onRegionDataGridUp(ActionPerformedEvent event) {
        swapItems(true);
    }

    @Subscribe("regionDataGrid.down")
    protected void onRegionDataGridDown(ActionPerformedEvent event) {
        swapItems(false);
    }

    protected void swapItems(boolean up) {
        if (regionDataGrid.getSingleSelectedItem() != null) {
            List<ReportRegion> items = reportRegionsDc.getMutableItems();
            ReportRegion currentItem = regionDataGrid.getSingleSelectedItem();
            if ((up && currentItem.getOrderNum() != 1) ||
                    (!up && currentItem.getOrderNum() != items.size())) {
                ReportRegion itemToSwap = IterableUtils.find(items,
                        e -> e.getOrderNum().equals(currentItem.getOrderNum() - (up ? 1 : -1)));
                long currentPosition = currentItem.getOrderNum();

                currentItem.setOrderNum(itemToSwap.getOrderNum());
                itemToSwap.setOrderNum(currentPosition);

                Collections.swap(items,
                        currentItem.getOrderNum().intValue() - 1, itemToSwap.getOrderNum().intValue() - 1);

            }
        }
    }

    protected void updateRegionButtons() {
        ReportData item = reportDataDc.getItem();
        buttonsBox.removeAll();
        if (item.getReportTypeGenerate().isList()) {
            addTabulatedRegionBtn.setEnabled(entityTreeHasSimpleAttrs && item.getReportRegions().isEmpty());
            buttonsBox.add(addTabulatedRegionBtn);
        } else {
            if (entityTreeHasSimpleAttrs && entityTreeHasCollections) {
                buttonsBox.add(addSimpleRegionBtn);
                buttonsBox.add(addTabulatedRegionBtn);
            } else if (entityTreeHasSimpleAttrs) {
                buttonsBox.add(addSimpleRegionBtn);
            } else if (entityTreeHasCollections) {
                buttonsBox.add(addTabulatedRegionBtn);
            } else {
                buttonsBox.add(addRegionDisabledBtn);
            }
        }

        // We don't have a query yet.
        regionsRunBtn.setVisible(item.getReportTypeGenerate() != ReportTypeGenerate.LIST_OF_ENTITIES_WITH_QUERY);
    }

    @Subscribe("outputFileFormat")
    public void onOutputFileFormatComponentValueChange(final AbstractField.ComponentValueChangeEvent<JmixComboBox<ReportOutputType>, ReportOutputType> event) {
        if (event.getOldValue() != null && event.getValue() != null
                && StringUtils.isNotBlank(outputFileName.getValue())) {
            ReportOutputType prevValue = event.getOldValue();
            ReportOutputType value = event.getValue();
            String prevOutputFileName = outputFileName.getValue();
            if (prevOutputFileName.contains(prevValue.name())) {
                String newOutputFileName = prevOutputFileName.replace(prevValue.name(), value.name());
                outputFileName.setValue(newOutputFileName);
                updateDownloadTemplateFile();
            }
        }
    }

    protected void updateCorrectReportOutputType(TemplateFileType newValue) {
        Map<String, ReportOutputType> optionsMap = outputFormatTools
                .getOutputAvailableFormats(reportDataDc.getItem().getTemplateFileType());
        if (!optionsMap.isEmpty()) {
            ComponentUtils.setItemsMap(outputFileFormat, MapUtils.invertMap(optionsMap));

            if (optionsMap.containsKey(newValue.toString())) {
                ReportOutputType reportOutputType = optionsMap.get(newValue.toString());
                outputFileFormat.setValue(reportOutputType);
            }
            if (outputFileFormat.getValue() == null) {
                if (optionsMap.size() > 1) {
                    outputFileFormat.setValue(optionsMap.get(reportDataDc.getItem().getTemplateFileType().toString()));
                } else if (optionsMap.size() == 1) {
                    outputFileFormat.setValue(optionsMap.values().iterator().next());
                }
            }
        }
    }

    protected String generateOutputFileName(String fileExtension, ReportTypeGenerate reportTypeGenerate) {
        ReportData reportData = reportDataDc.getItem();
        if (StringUtils.isBlank(reportData.getName())) {
            MetaClass entityMetaClass = metadata.findClass(reportData.getEntityName());
            if (entityMetaClass != null) {
                if (ReportTypeGenerate.SINGLE_ENTITY == reportTypeGenerate) {
                    return messageTools.getEntityCaption(entityMetaClass) + "." + fileExtension;
                } else {
                    return messageBundle.formatMessage(
                            "downloadEntityListOutputFileNamePattern",
                            messageTools.getEntityCaption(entityMetaClass), fileExtension);
                }
            } else {
                return Strings.EMPTY;
            }
        } else {
            return reportData.getName() + "." + fileExtension;
        }
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        initRegionsDataGrid();
        updateFragmentChangeButtons();
    }

    protected void initRegionsDataGrid() {
        regionDataGrid.addColumn(reportRegion -> {
                    String messageKey = reportRegion.isTabulatedRegion() ? "ReportRegion.tabulatedName" : "ReportRegion.simpleName";
                    return messageBundle.formatMessage(messageKey, reportRegion.getOrderNum());
                }).setKey("name")
                .setHeader(messageBundle.getMessage("name"))
                .setSortable(false)
                .setResizable(true);

        regionDataGrid.addColumn(reportRegion ->
                        StringUtils.abbreviate(StringUtils.join(
                                        CollectionUtils.collect(reportRegion.getRegionProperties(),
                                                RegionProperty::getHierarchicalLocalizedNameExceptRoot), ", "),
                                MAX_ATTRS_BTN_CAPTION_WIDTH)
                ).setKey("attributes")
                .setHeader(messageBundle.getMessage("attributes"))
                .setSortable(false)
                .setResizable(true);

        regionDataGrid.addColumn(reportRegion ->
                        messageTools.getEntityCaption(metadata.getClass(reportRegion.getRegionPropertiesRootNode().getMetaClassName()))
                ).setKey("entity")
                .setHeader(messageBundle.getMessage("entity"))
                .setSortable(false)
                .setResizable(true);
    }

    public void updateDownloadTemplateFile() {
        String templateFileName = generateTemplateFileName(reportDataDc.getItem().getTemplateFileType().toString().toLowerCase());

        reportDataDc.getItem().setTemplateFileName(templateFileName);
    }

    public String generateTemplateFileName(String fileExtension) {
        ReportData reportData = reportDataDc.getItem();
        MetaClass entityMetaClass = metadata.findClass(reportData.getEntityName());

        if (entityMetaClass != null) {
            return messageBundle.formatMessage("downloadTemplateFileNamePattern",
                    reportData.getName(), fileExtension);
        } else {
            return Strings.EMPTY;
        }
    }

    @Subscribe("reportParameterDataGrid.generate")
    public void onReportParameterDataGridGenerate(ActionPerformedEvent event) {
        if (!queryParametersDc.getItems().isEmpty()) {
            dialogs.createOptionDialog()
                    .withHeader(messageBundle.getMessage("dialogConfirmation.header"))
                    .withText(messageBundle.getMessage("queryParameterDialog.clearQueryParameterConfirm.text"))
                    .withActions(
                            new DialogAction(DialogAction.Type.OK)
                                    .withVariant(ActionVariant.PRIMARY)
                                    .withHandler(e -> generateQueryParameters()),
                            new DialogAction(DialogAction.Type.CANCEL))
                    .open();
        } else {
            generateQueryParameters();
        }
    }

    protected void generateQueryParameters() {
        List<QueryParameter> queryParameterList = queryParametersDc.getMutableItems();
        queryParameterList.clear();

        String query = reportDataDc.getItem().getQuery();

        if (query != null) {
            QueryParser queryParser = queryTransformerFactory.parser(query);
            Set<String> paramNames = queryParser.getParamNames();

            for (String paramName : paramNames) {
                QueryParameter queryParameter = createQueryParameter(paramName);
                queryParameterList.add(queryParameter);
            }
        }
    }

    protected QueryParameter createQueryParameter(String name) {
        QueryParameter queryParameter = metadata.create(QueryParameter.class);
        queryParameter.setName(name);
        queryParameter.setParameterType(ParameterType.TEXT);
        queryParameter.setJavaClassName(String.class.getName());
        queryParameter.setDefaultValueString(null);

        return queryParameter;
    }

    @Subscribe("downloadTemplateFileBtn")
    public void onDownloadTemplateFileClick(ClickEvent<Button> event) {
        ReportData reportData = reportDataDc.getItem();
        try {
            TemplateFileType templateFileType = reportData.getTemplateFileType();
            byte[] newTemplate = reportWizardService.generateTemplate(reportData, templateFileType);

            downloader.download(new ByteArrayDownloadDataProvider(
                            newTemplate,
                            uiProperties.getSaveExportedByteArrayDataThresholdBytes(),
                            coreProperties.getTempDir()),
                    reportDataDc.getItem().getTemplateFileName(),
                    DownloadFormat.getByExtension(templateFileType.toString().toLowerCase()));
        } catch (TemplateGenerationException e) {
            notifications.create(messageBundle.getMessage("templateGenerationException.message"))
                    .withType(Notifications.Type.DEFAULT)
                    .withPosition(Notification.Position.BOTTOM_END)
                    .show();
        }
    }

    @Subscribe(id = "reportRegionsDc", target = Target.DATA_CONTAINER)
    public void onReportRegionsDcCollectionChange(CollectionContainer.CollectionChangeEvent<ReportData> event) {
        updateMoveButtons();
        regenerateQuery = event.getChangeType() == CollectionChangeType.ADD_ITEMS;
        if (event.getChangeType() == CollectionChangeType.ADD_ITEMS || event.getChangeType() == CollectionChangeType.REMOVE_ITEMS) {
            updateRegionButtons();
        }
    }

    protected void updateMoveButtons() {
        moveUpBtn.setEnabled(regionDataGridUpEnabledRule());
        moveDownBtn.setEnabled(regionDataGridDownEnabledRule());
    }

    @Subscribe(id = "regionPropertiesDc", target = Target.DATA_CONTAINER)
    public void onRegionPropertiesDcCollectionChange(CollectionContainer.CollectionChangeEvent<RegionProperty> event) {
        regenerateQuery = true;
    }

    @Subscribe("queryRunBtn")
    public void onQueryRunBtnClick(ClickEvent<Button> event) {
        lastGeneratedTmpReport = buildReport(true);

        if (lastGeneratedTmpReport != null) {
            FluentUiReportRunner fluentRunner = uiReportRunner.byReportEntity(lastGeneratedTmpReport)
                    .withParametersDialogShowMode(ParametersDialogShowMode.IF_REQUIRED);
            if (reportsClientProperties.getUseBackgroundReportProcessing()) {
                fluentRunner.inBackground(this);
            }
            fluentRunner.runAndShow();
        }
    }

    @Subscribe("fullScreenTransformationBtn")
    protected void onFullScreenTransformationBtnClick(ClickEvent<Icon> event) {
        reportScriptEditor.create(this)
                .withTitle(getScriptEditorDialogCaption())
                .withValue(reportDataDc.getItem().getQuery())
                .withEditorMode(CodeEditorMode.SQL)
                .withCloseOnClick(value -> reportDataDc.getItem().setQuery(value))
                .withHelpOnClick(this::openCodeEditorHelp)
                .open();
    }

    @Subscribe("queryCodeEditorHelpIcon")
    protected void onQueryCodeEditorHelpIconClick(ClickEvent<Icon> event) {
        openCodeEditorHelp();
    }

    protected void openCodeEditorHelp() {
        dialogs.createMessageDialog()
                .withHeader(messageBundle.getMessage("queryCodeEditor.dialog.title"))
                .withContent(new Html(messageBundle.getMessage("queryCodeEditor.dialog.content")))
                .withResizable(true)
                .withModal(false)
                .withWidth("50em")
                .open();
    }

    protected String getScriptEditorDialogCaption() {
        String reportName = reportDataDc.getItem().getName();
        String bandName = reportDataDc.getItem().getName();

        if (ObjectUtils.isNotEmpty(bandName) && ObjectUtils.isNotEmpty(reportName)) {
            return messageBundle.formatMessage(
                    "queryCodeEditor.expandIcon.dialog.header", reportName, bandName);
        }
        return StringUtils.EMPTY;
    }

    @Install(to = "reportParameterDataGrid.edit", subject = "afterSaveHandler")
    protected void reportParameterDataGridEditAfterSaveHandler(QueryParameter queryParameter) throws ClassNotFoundException {
        setDefaultValue(queryParameter);
        reportParameterDataGrid.deselectAll();
    }

    @Install(to = "reportParameterDataGrid.create", subject = "afterSaveHandler")
    protected void reportParameterDataGridCreateAfterSaveHandler(QueryParameter queryParameter) throws ClassNotFoundException {
        setDefaultValue(queryParameter);
    }


    protected void setDefaultValue(QueryParameter queryParameter) throws ClassNotFoundException {
        Object value = jmixObjectToStringConverter.convertFromString(Class.forName(queryParameter.getJavaClassName()), queryParameter.getDefaultValueString());
        queryParameter.setDefaultValue(value);
        queryParametersDc.replaceItem(queryParameter);
    }

    protected void initReportParameterDataGrid() {
        reportParameterDataGrid.addColumn(queryParameter -> {
                    Object defaultValue = queryParameter.getDefaultValue();
                    if (defaultValue != null) {
                        ParameterType parameterType = queryParameter.getParameterType();
                        switch (parameterType) {
                            case DATE -> {
                                String dateFormat = formatStringsRegistry.getFormatStrings(currentAuthentication.getLocale()).getDateFormat();
                                return new SimpleDateFormat(dateFormat).format(defaultValue);
                            }
                            case TIME -> {
                                String timeFormat = formatStringsRegistry.getFormatStrings(currentAuthentication.getLocale()).getTimeFormat();
                                return new SimpleDateFormat(timeFormat).format(defaultValue);
                            }
                            default -> {
                                return defaultValue;
                            }
                        }
                    }
                    return defaultValue;
                }).setHeader(messageBundle.getMessage("defaultValueString.header"))
                .setKey("defaultValueString")
                .setResizable(true)
                .setSortable(true);
    }
}