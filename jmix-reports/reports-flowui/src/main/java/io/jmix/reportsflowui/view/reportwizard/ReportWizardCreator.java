package io.jmix.reportsflowui.view.reportwizard;


import io.jmix.reportsflowui.ReportsUiHelper;
import io.jmix.reportsflowui.ReportsClientProperties;
import io.jmix.reportsflowui.runner.FluentUiReportRunner;
import io.jmix.reportsflowui.runner.ParametersDialogShowMode;
import io.jmix.reportsflowui.runner.UiReportRunner;
import io.jmix.reportsflowui.view.region.RegionDetailView;
import io.jmix.reports.yarg.util.converter.ObjectToStringConverter;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.data.QueryParser;
import io.jmix.data.QueryTransformerFactory;
import io.jmix.flowui.*;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.radiobuttongroup.JmixRadioButtonGroup;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.download.ByteArrayDownloadDataProvider;
import io.jmix.flowui.download.DownloadFormat;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButton;
import io.jmix.flowui.model.*;
import io.jmix.flowui.view.*;
import io.jmix.reports.app.EntityTree;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportGroup;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.wizard.*;
import io.jmix.reports.exception.TemplateGenerationException;
import io.jmix.reports.libintegration.JmixObjectToStringConverter;
import io.jmix.reports.runner.ReportRunner;
import io.jmix.reportsflowui.ReportsClientProperties;
import io.jmix.reportsflowui.ReportsUiHelper;
import io.jmix.reportsflowui.runner.FluentUiReportRunner;
import io.jmix.reportsflowui.runner.ParametersDialogShowMode;
import io.jmix.reportsflowui.runner.UiReportRunner;
import io.jmix.reportsflowui.view.entitytreelist.EntityTreeLookupView;
import io.jmix.reportsflowui.view.region.RegionDetailView;
import io.jmix.reportsflowui.view.reportwizard.template.query.JpqlQueryBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.util.*;

@Route(value = "reportWizard", layout = DefaultMainViewParent.class)
@ViewController("ReportWizardCreator")
@ViewDescriptor("report-wizard.xml")
public class ReportWizardCreator extends StandardView {
    public static final String FIELD_ICON_SIZE_CLASS_NAME = "reports-field-icon-size";
    protected static final String FIELD_ICON_CLASS_NAME = "template-detailview-field-icon";

    protected static final int MAX_ATTRS_BTN_CAPTION_WIDTH = 135;
    protected boolean regenerateQuery = false;
    protected boolean needUpdateEntityModel = false;
    protected Report lastGeneratedTmpReport;
    private List<Div> fragmentsList;
    protected int currentFragmentIdx = 0;
    @ViewComponent
    private InstanceContainer<ReportData> reportDataDc;
    @ViewComponent
    private DataContext dataContext;
    @Autowired
    private Messages messages;
    @Autowired
    private Dialogs dialogs;
    @ViewComponent
    private CollectionPropertyContainer<ReportRegion> reportRegionsDc;
    @ViewComponent
    private JmixButton nextBtn;
    @ViewComponent
    private JmixButton saveBtn;
    @ViewComponent
    private JmixButton backBtn;
    @Autowired
    private Notifications notifications;
    @Autowired
    private MessageTools messageTools;
    @Autowired
    private MetadataTools metadataTools;
    @Autowired
    private ExtendedEntities extendedEntities;
    @Autowired
    private Metadata metadata;
    @ViewComponent
    private JmixComboBox entityField;
    @Autowired
    private ReportWizard reportWizardService;
    @ViewComponent
    private JmixComboBox<TemplateFileType> templateFileTypeField;
    @ViewComponent
    private JmixComboBox<ReportOutputType> outputFileFormat;
    @ViewComponent
    private TypedTextField<String> outputFileName;
    @ViewComponent
    private JmixButton downloadTemplateFile;
    @Autowired
    private FlowuiProperties flowuiProperties;
    @Autowired
    private CoreProperties coreProperties;
    @ViewComponent
    private Div detailsDiv;
    @ViewComponent
    private Div saveDiv;
    @ViewComponent
    private Div queryDiv;

    @ViewComponent
    private DataGrid<ReportRegion> regionsTable;
    @ViewComponent
    private JmixButton regionsRunBtn;
    @ViewComponent
    private HorizontalLayout buttonsBox;
    @ViewComponent
    private JmixButton addRegionDisabledBtn;
    @ViewComponent
    private JmixRadioButtonGroup<ReportTypeGenerate> reportTypeGenerateField;
    @ViewComponent
    private JmixTextArea reportQueryCodeEditor;
    @ViewComponent
    private CollectionPropertyContainer<QueryParameter> queryParametersDc;
    @ViewComponent
    private Div regionsDiv;
    @Autowired
    private ReportRunner reportRunner;
    @Autowired
    private QueryTransformerFactory queryTransformerFactory;
    @Autowired
    protected Downloader downloader;
    @Autowired
    protected OutputFormatTools outputFormatTools;
    @Autowired
    private JmixObjectToStringConverter jmixObjectToStringConverter;
    @Autowired
    private ReportsUiHelper reportsUiHelper;
    @Autowired
    private UiReportRunner uiReportRunner;
    @Autowired
    private ReportsClientProperties reportsClientProperties;
    @Autowired
    private DialogWindows dialogWindows;
    @Autowired
    private UiComponents uiComponents;
    @ViewComponent
    private JmixButton addTabulatedRegionBtn;
    @ViewComponent
    private DropdownButton addRegionPopupBtn;
    @ViewComponent
    private JmixButton addSimpleRegionBtn;

    @Subscribe
    public void onInit(InitEvent event) {
        reportDataDc.setItem(dataContext.create(ReportData.class));

        initFragments();
        fragmentsList = getFragmentsList();
        //details step
        initReportTypeOptionGroup();
        initTemplateFormatLookupField();
        initEntityLookupField();

        //save step

        //query step
        //todo AN
//        initQueryReportSourceCode();
        initReportQueryCodeEditorScript();
    }

    @Subscribe(id = "reportDataDc", target = Target.DATA_CONTAINER)
    public void onReportDataDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<ReportData> event) {
        if (event.getProperty().equals("reportTypeGenerate")) {
            List<Div> stepFragments = new ArrayList<>(getFragmentsList());
            if (Objects.equals(event.getValue(), ReportTypeGenerate.LIST_OF_ENTITIES_WITH_QUERY)) {
                stepFragments.add(2, queryDiv);
            }

            fragmentsList = stepFragments;
        }
//SAVE STEP
        if (event.getProperty().equals("entityName") || event.getProperty().equals("templateFileType")) {
            updateCorrectReportOutputType();
            updateDownloadTemplateFile();
        }
    }

    protected ArrayList<Div> getFragmentsList() {
        ArrayList<Div> fragments = new ArrayList<>();
        fragments.add(detailsDiv);
        fragments.add(regionsDiv);
        fragments.add(saveDiv);
        return fragments;
    }

    protected void initFragments() {
        detailsDiv.setVisible(true);
        saveDiv.setVisible(false);
        regionsDiv.setVisible(false);
        queryDiv.setVisible(false);
    }

    protected void nextFragment() {
        if (currentFragmentIdx < fragmentsList.size() - 1) {
            fragmentsList.get(currentFragmentIdx).setVisible(false);
            currentFragmentIdx++;
            beforeShowFragments();
            fragmentsList.get(currentFragmentIdx).setVisible(true);
        }
        updateButtons();
    }

    protected void beforeShowFragments() {
        Div div = fragmentsList.get(currentFragmentIdx);

        if (div.equals(detailsDiv)) {

        } else if (div.equals(regionsDiv)) {
            updateRegionButtons();
            showAddRegion();

            updateButtons();

            regionsRunBtn.setVisible(getReportTypeGenerate() != ReportTypeGenerate.LIST_OF_ENTITIES_WITH_QUERY);
        } else if (div.equals(saveDiv)) {
            if (StringUtils.isEmpty(outputFileName.getValue())) {
                ReportData reportData = reportDataDc.getItem();
                outputFileName.setValue(generateOutputFileName(reportData.getTemplateFileType().toString().toLowerCase()));
            }
        } else if (div.equals(queryDiv)) {
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

    protected void updateButtons() {
        if (currentFragmentIdx == 0) {
            backBtn.setVisible(false);
            saveBtn.setVisible(false);
        } else if (currentFragmentIdx == fragmentsList.size() - 1) {
            saveBtn.setVisible(true);
            nextBtn.setVisible(false);
        } else {
            backBtn.setVisible(true);
            nextBtn.setVisible(true);
            saveBtn.setVisible(false);
        }
    }

    protected void prevFragment() {
        if (currentFragmentIdx > 0) {
            fragmentsList.get(currentFragmentIdx).setVisible(false);
            currentFragmentIdx--;
            beforeShowFragments();
            fragmentsList.get(currentFragmentIdx).setVisible(true);
        }
        updateButtons();
    }

    public boolean isNeedUpdateEntityModel() {
        return needUpdateEntityModel;
    }

    public void setNeedUpdateEntityModel(boolean needUpdateEntityModel) {
        this.needUpdateEntityModel = needUpdateEntityModel;
    }

    @Subscribe("nextBtn")
    public void onNextBtnClick(ClickEvent<Button> event) {
        MetaClass metaClass = metadata.findClass(getItem().getEntityName());

        if (metaClass == null) {
            notifications.create(messages.getMessage("fillEntityMsg"))
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }
        if (!validateFragment()) {
            return;
        }

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

    protected Div getCurrentFragment() {
        return fragmentsList.get(currentFragmentIdx);
    }

    protected boolean validateFragment() {
        List<String> validationErrors = validateCurrentFragment();
        if (!validationErrors.isEmpty()) {
            notifications.create(org.springframework.util.StringUtils.arrayToDelimitedString(validationErrors.toArray(), "\n"))
                    .withType(Notifications.Type.WARNING)
                    .show();
            return false;
        }

        return true;
    }

    protected List<String> validateCurrentFragment() {
        Div currentFragment = getCurrentFragment();
        List<String> errors = new ArrayList<>();
        for (Component c : currentFragment.getChildren().toList()) {
            if (c instanceof SupportsValidation<?>) {
                SupportsValidation validatable = (SupportsValidation) c;
                validatable.executeValidators();
            }
        }
        if (currentFragment.equals(regionsDiv)) {
            if (reportDataDc.getItem().getReportRegions().isEmpty()) {
                errors.add(messages.getMessage(getClass(), "addRegionsWarn"));
            }
        }
        return errors;
    }

    @Subscribe("saveBtn")
    public void onSaveBtnClick(ClickEvent<Button> event) {
        if (reportDataDc.getItem().getReportRegions().isEmpty()) {
            dialogs.createOptionDialog()
                    .withHeader(messages.getMessage("dialogs.Confirmation"))
                    .withText(messages.getMessage("confirmSaveWithoutRegions"))
                    .withActions(
                            new DialogAction(DialogAction.Type.OK).withHandler(handle ->
                                    convertToReportAndForceCloseWizard()
                            ),
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
            notifications.create(messages.getMessage(ReportWizardCreator.class, "templateGenerationException"))
                    .withType(Notifications.Type.WARNING)
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
                    .withHeader(messages.getMessage("dialogs.Confirmation"))
                    .withText(messages.getMessage(getClass(), "interruptConfirm"))
                    .withActions(
                            new DialogAction(DialogAction.Type.OK).withHandler(handle ->
                                    close(StandardOutcome.DISCARD)
                            ),
                            new DialogAction(DialogAction.Type.NO)
                    ).open();
            event.preventClose();
        }
    }

    protected void initEntityLookupField() {
        FlowuiComponentUtils.setItemsMap(entityField, MapUtils.invertMap(getAvailableEntities()));
    }

    @Subscribe("reportTypeGenerateField")
    public void onReportTypeGenerateFieldComponentValueChange(AbstractField.ComponentValueChangeEvent<JmixRadioButtonGroup, Object> event) {
        ReportData reportData = reportDataDc.getItem();
        ReportTypeGenerate currentType = (ReportTypeGenerate) event.getValue();
        updateReportTypeGenerate(reportData, currentType);
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
        needUpdateEntityModel = true;
        setReportName(reportData, prevValue, value);

        reportRegionsDc.getMutableItems().clear();
        reportData.setEntityName(value.getName());

        clearQuery();
    }

    @Subscribe("groupField")
    public void onGroupFieldComponentValueChange(AbstractField.ComponentValueChangeEvent<EntityComboBox<ReportGroup>, ReportGroup> event) {
        ReportData reportData = reportDataDc.getItem();
        ReportGroup group = event.getValue();
        updateReportGroup(reportData, group);
    }

    protected void updateReportGroup(ReportData reportData, @Nullable ReportGroup group) {
        reportData.setGroup(group);
        clearQuery();
    }

    protected void setReportName(ReportData reportData, @Nullable MetaClass prevValue, MetaClass value) {
        String oldName = reportData.getName();
        if (StringUtils.isBlank(oldName)) {
            reportData.setName(messages.formatMessage("reportNamePattern", messageTools.getEntityCaption(value)));
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
                    if (!oldName.equals(messages.formatMessage("reportNamePattern", prevEntityCaption))) {
                        //if user changed auto generated report name and we have changed it, we show message to him
                        notifications.create(messages.getMessage(getClass(), "reportNameChanged"))
                                .withType(Notifications.Type.WARNING)
                                .show();
                    }
                }
            }
        }
    }


    protected void initTemplateFormatLookupField() {
        FlowuiComponentUtils.setItemsMap(templateFileTypeField, getAvailableTemplateFormats());
        templateFileTypeField.setAllowCustomValue(false);
        templateFileTypeField.setValue(TemplateFileType.DOCX);
    }

    protected void initReportTypeOptionGroup() {
        reportTypeGenerateField.setItems(ReportTypeGenerate.SINGLE_ENTITY, ReportTypeGenerate.LIST_OF_ENTITIES, ReportTypeGenerate.LIST_OF_ENTITIES_WITH_QUERY);
        reportTypeGenerateField.setItemLabelGenerator(item -> {
            switch (item) {
                case SINGLE_ENTITY -> {
                    return messages.getMessage(getClass(), "singleEntityReport");
                }
                case LIST_OF_ENTITIES -> {
                    return messages.getMessage(getClass(), "listOfEntitiesReport");
                }
                case LIST_OF_ENTITIES_WITH_QUERY -> {
                    return messages.getMessage(getClass(), "listOfEntitiesReportWithQuery");
                }
                default -> {
                    return "";
                }
            }
        });
        reportTypeGenerateField.setValue(ReportTypeGenerate.SINGLE_ENTITY);
    }

    protected Map<String, ReportTypeGenerate> getListedReportOptionsMap() {
        Map<String, ReportTypeGenerate> result = new LinkedHashMap<>(3);
        result.put(messages.getMessage(getClass(), "singleEntityReport"), ReportTypeGenerate.SINGLE_ENTITY);
        result.put(messages.getMessage(getClass(), "listOfEntitiesReport"), ReportTypeGenerate.LIST_OF_ENTITIES);
        result.put(messages.getMessage(getClass(), "listOfEntitiesReportWithQuery"), ReportTypeGenerate.LIST_OF_ENTITIES_WITH_QUERY);
        return result;
    }

    protected Map<TemplateFileType, String> getAvailableTemplateFormats() {
        Map<TemplateFileType, String> result = new LinkedHashMap<>(4);
        result.put(TemplateFileType.XLSX, messages.getMessage(TemplateFileType.XLSX));
        result.put(TemplateFileType.DOCX, messages.getMessage(TemplateFileType.DOCX));
        result.put(TemplateFileType.HTML, messages.getMessage(TemplateFileType.HTML));
        result.put(TemplateFileType.CSV, messages.getMessage(TemplateFileType.CSV));
        result.put(TemplateFileType.TABLE, messages.getMessage(TemplateFileType.TABLE));

        return result;
    }

    protected Map<String, MetaClass> getAvailableEntities() {
        Map<String, MetaClass> result = new TreeMap<>(String::compareTo);
        Collection<MetaClass> classes = metadataTools.getAllJpaEntityMetaClasses();
        for (MetaClass metaClass : classes) {
            MetaClass effectiveMetaClass = extendedEntities.getEffectiveMetaClass(metaClass);
            if (!reportWizardService.isEntityAllowedForReportWizard(effectiveMetaClass)) {
                continue;
            }
            result.put(messageTools.getEntityCaption(effectiveMetaClass) + " (" + effectiveMetaClass.getName() + ")", effectiveMetaClass);
        }
        return result;
    }

    protected void clearQuery() {
        ReportData reportData = reportDataDc.getItem();
        reportData.setQuery(null);
        reportData.setQueryParameters(null);
    }

    protected boolean entityTreeHasSimpleAttrs;
    protected boolean entityTreeHasCollections;

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

    @Subscribe("regionsTable.remove")
    public void onRegionsTableRemoveItemAction(ActionPerformedEvent event) {
        for (ReportRegion item : regionsTable.getSelectedItems()) {
            reportRegionsDc.getMutableItems().remove(item);
            normalizeRegionPropertiesOrderNum();
        }
    }

    @Install(to = "regionsTable.down", subject = "enabledRule")
    private boolean regionsTableDownEnabledRule() {
        ReportRegion item = regionsTable.getSingleSelectedItem();
        if (item == null) {
            return false;
        }
        return item.getOrderNum() < reportRegionsDc.getItems().size();
    }


    @Install(to = "regionsTable.up", subject = "enabledRule")
    private boolean regionsTableUpEnabledRule() {
        ReportRegion item = regionsTable.getSingleSelectedItem();
        if (item == null) {
            return false;
        }
        return item.getOrderNum() > 1;
    }

    //todo AN return click listener
    //todo exception NullPointerException: Cannot invoke "io.jmix.reports.entity.wizard.EntityTreeNode.getLocalizedName()" because "this.rootEntity" is null
    protected void editRegion() {
        ReportRegion selectedRegion = regionsTable.getSingleSelectedItem();
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

    protected void openRegionEditorOnlyWithNestedCollections(final ReportRegion item) {//show lookup for choosing parent collection for tabulated region
        DialogWindow<EntityTreeLookupView> entityTreeListDialogWindow = dialogWindows.lookup(this, EntityTreeNode.class)
                .withViewClass(EntityTreeLookupView.class)
                .build();
        EntityTreeLookupView entityTreeLookupView = entityTreeListDialogWindow.getView();
        entityTreeLookupView.setParameters(reportDataDc.getItem().getEntityTreeRootNode(), false, true, false);
        entityTreeLookupView.setSelectionHandler(items -> {
            if (items.size() == 1) {
                EntityTreeNode regionPropertiesRootNode = IterableUtils.get(items, 0);

                item.setRegionPropertiesRootNode(regionPropertiesRootNode);

                showRegionEditor(item, regionPropertiesRootNode, false, true, false);
            }
        });
        entityTreeListDialogWindow.open();
    }

    protected void openRegionEditor(ReportRegion item) {
        item.setRegionPropertiesRootNode(reportDataDc.getItem().getEntityTreeRootNode());

        showRegionEditor(item, reportDataDc.getItem().getEntityTreeRootNode(), true, false, ReportTypeGenerate.LIST_OF_ENTITIES_WITH_QUERY == getReportTypeGenerate());
    }

    protected void showRegionEditor(ReportRegion item, EntityTreeNode rootEntity, boolean scalarOnly, boolean collectionsOnly, boolean persistentOnly) {
        DialogWindow<RegionDetailView> regionDialogWindow = dialogWindows.detail(this, ReportRegion.class)
                .withViewClass(RegionDetailView.class)
                .withContainer(reportRegionsDc)
                .build();
        RegionDetailView regionDetailView = regionDialogWindow.getView();
        regionDetailView.setEntityToEdit(item);
        regionDetailView.setParameters(rootEntity, scalarOnly, collectionsOnly, persistentOnly);
        regionDetailView.setShowSaveNotification(false);
        regionDialogWindow.open();
    }

    @Subscribe("regionsRunBtn")
    public void onRegionsRunBtnClick(ClickEvent<Button> event) {
        if (reportDataDc.getItem().getReportRegions().isEmpty()) {
            notifications.create(messages.getMessage(getClass(), "addRegionsWarn"))
                    .withType(Notifications.Type.WARNING)
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

    @Subscribe("regionsEditBtn")
    public void onRegionsEditBtnClick(ClickEvent<Button> event) {
        editRegion();
    }

    @Subscribe("regionsTable.remove")
    public void onRegionsTableRemove(ActionPerformedEvent event) {
        normalizeRegionPropertiesOrderNum();
    }

    protected void normalizeRegionPropertiesOrderNum() {
        long normalizedIdx = 0;
        List<ReportRegion> allItems = new ArrayList<>(reportRegionsDc.getItems());
        for (ReportRegion item : allItems) {
            item.setOrderNum(++normalizedIdx); //first must be 1
        }
    }

    @Subscribe("regionsTable.up")
    protected void onRegionsTableUp(ActionPerformedEvent event) {
        replaceParameters(true);
    }

    @Subscribe("regionsTable.down")
    protected void onRegionsTableDown(ActionPerformedEvent event) {
        replaceParameters(false);
    }

    protected void replaceParameters(boolean up) {
        if (regionsTable.getSingleSelectedItem() != null) {
            List<ReportRegion> items = reportRegionsDc.getMutableItems();
            int currentPosition = items.indexOf(regionsTable.getSingleSelectedItem());
            if ((up && currentPosition != 0)
                    || (!up && currentPosition != items.size() - 1)) {
                int itemToSwapPosition = currentPosition - (up ? 1 : -1);

                Collections.swap(items, itemToSwapPosition, currentPosition);
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
                //todo AN return dropdown button
//                buttonsBox.add(addRegionPopupBtn);
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
    }

    @Subscribe("outputFileName")
    public void onOutputFileNameComponentValueChange(AbstractField.ComponentValueChangeEvent<TextField, String> event) {
        reportDataDc.getItem().setOutputNamePattern(event.getValue());
    }

    //todo AN
//    @Install(to = "outputFileName", subject = "contextHelpIconClickHandler")
//    protected void outputFileNameContextHelpIconClickHandler(HasContextHelp.ContextHelpIconClickEvent contextHelpIconClickEvent) {
//        dialogs.createMessageDialog()
//                .withCaption(messages.getMessage("template.namePatternText"))
//                .withMessage(messages.getMessage("template.namePatternTextHelp"))
//                .withContentMode(ContentMode.HTML)
//                .withModal(false)
//                .withWidth("560px")
//                .show();
//    }


    protected void updateCorrectReportOutputType() {
        ReportOutputType outputFileFormatPrevValue = outputFileFormat.getValue();
        outputFileFormat.setValue(null);
        Map<String, ReportOutputType> optionsMap = outputFormatTools.getOutputAvailableFormats(reportDataDc.getItem().getTemplateFileType());
        FlowuiComponentUtils.setItemsMap(outputFileFormat, MapUtils.invertMap(optionsMap));

        if (outputFileFormatPrevValue != null) {
            if (optionsMap.containsKey(outputFileFormatPrevValue.toString())) {
                outputFileFormat.setValue(outputFileFormatPrevValue);
            }
        }
        if (outputFileFormat.getValue() == null) {
            if (optionsMap.size() > 1) {
                outputFileFormat.setValue(optionsMap.get(reportDataDc.getItem().getTemplateFileType().toString()));
            } else if (optionsMap.size() == 1) {
                outputFileFormat.setValue(optionsMap.values().iterator().next());
            }
        }
    }

    protected String generateOutputFileName(String fileExtension) {
        ReportData reportData = reportDataDc.getItem();
        if (StringUtils.isBlank(reportData.getName())) {
            MetaClass entityMetaClass = metadata.findClass(reportData.getEntityName());
            return entityMetaClass != null ?
                    messages.formatMessage(getClass(),
                            "downloadOutputFileNamePattern", messageTools.getEntityCaption(entityMetaClass), fileExtension) :
                    "";
        } else {
            return reportData.getName() + "." + fileExtension;
        }
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        regionsTable.addColumn(reportRegion -> {
                    String messageKey = reportRegion.isTabulatedRegion() ? "ReportRegion.tabulatedName" : "ReportRegion.simpleName";
                    return messages.formatMessage(getClass(), messageKey, reportRegion.getOrderNum());
                }).setKey("name")
                .setHeader(messages.getMessage(getClass(), "name"))
                .setSortable(true)
                .setResizable(true);
        regionsTable.addColumn(reportRegion ->
                        messageTools.getEntityCaption(metadata.getClass(reportRegion.getRegionPropertiesRootNode().getMetaClassName()))
                ).setKey("entity")
                .setHeader(messages.getMessage(getClass(), "entity"))
                .setSortable(true)
                .setResizable(true);
        regionsTable.addColumn(reportRegion ->
                        StringUtils.abbreviate(StringUtils.join(
                                        CollectionUtils.collect(reportRegion.getRegionProperties(),
                                                RegionProperty::getHierarchicalLocalizedNameExceptRoot), ", "),
                                MAX_ATTRS_BTN_CAPTION_WIDTH)
                ).setKey("attributes")
                .setHeader(messages.getMessage(getClass(), "attributes"))
                .setSortable(true)
                .setResizable(true);
    }

    public void updateDownloadTemplateFile() {
        String templateFileName = generateTemplateFileName(reportDataDc.getItem().getTemplateFileType().toString().toLowerCase());

        downloadTemplateFile.setText(templateFileName);
        reportDataDc.getItem().setTemplateFileName(templateFileName);
    }

    public String generateTemplateFileName(String fileExtension) {
        ReportData reportData = reportDataDc.getItem();
        MetaClass entityMetaClass = metadata.findClass(reportData.getEntityName());
        return entityMetaClass != null ?
                messages.formatMessage(getClass(), "downloadTemplateFileNamePattern", reportData.getName(), fileExtension) :
                "";
    }

    @Subscribe("downloadTemplateFile")
    public void onDownloadTemplateFileClick(ClickEvent<Button> event) {
        ReportData reportData = reportDataDc.getItem();
        try {
            TemplateFileType templateFileType = reportData.getTemplateFileType();
            byte[] newTemplate = reportWizardService.generateTemplate(reportData, templateFileType);

            downloader.download(new ByteArrayDownloadDataProvider(
                            newTemplate,
                            flowuiProperties.getSaveExportedByteArrayDataThresholdBytes(),
                            coreProperties.getTempDir()),
                    downloadTemplateFile.getText(),
                    DownloadFormat.getByExtension(templateFileType.toString().toLowerCase()));
        } catch (TemplateGenerationException e) {
            notifications.create(messages.getMessage(getClass(), "templateGenerationException"))
                    .withType(Notifications.Type.WARNING)
                    .show();
        }
    }


    @Subscribe("reportParameterTable.generate")
    public void onReportParameterTableGenerate(ActionPerformedEvent event) {

    }

    @Subscribe(id = "reportRegionsDc", target = Target.DATA_CONTAINER)
    public void onReportRegionsDcCollectionChange(CollectionContainer.CollectionChangeEvent<ReportData> event) {
        regenerateQuery = event.getChangeType() == CollectionChangeType.ADD_ITEMS;
    }

    @Subscribe(id = "regionPropertiesDc", target = Target.DATA_CONTAINER)
    public void onRegionPropertiesDcCollectionChange(CollectionContainer.CollectionChangeEvent<RegionProperty> event) {
        regenerateQuery = true;
    }

//    @Subscribe("queryRunBtn")
//    public void onQueryRunBtnClick(ClickEvent<Button> event) {
//        lastGeneratedTmpReport = this.buildReport(true);
//
//        if (lastGeneratedTmpReport != null) {
//            FluentReportRunner fluentReportRunner = reportRunner.byReportEntity(lastGeneratedTmpReport)
//                    .withParams();
//            FluentUiReportRunner fluentRunner =  fluentReportRunner
//                    .withParametersDialogShowMode(ParametersDialogShowMode.IF_REQUIRED);
//            if (reportsClientProperties.getUseBackgroundReportProcessing()) {
//                fluentReportRunner.inBackground(getFragment().getFrameOwner());
//            }
//            fluentReportRunner.run();
//        }
//    }

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


    private QueryParameter createQueryParameter(String name) {
        QueryParameter queryParameter = metadata.create(QueryParameter.class);
        queryParameter.setName(name);
        queryParameter.setParameterType(ParameterType.TEXT);
        queryParameter.setJavaClassName(String.class.getName());
        queryParameter.setDefaultValueString(null);

        return queryParameter;
    }
//todo AN
//    protected void initQueryReportSourceCode() {
//        reportQueryCodeEditor.setHighlightActiveLine(false);
//        reportQueryCodeEditor.setShowGutter(false);
//        reportQueryCodeEditor.setMode(SourceCodeEditor.Mode.SQL);
//        reportQueryCodeEditor.setSuggester(this);
//    }
//todo AN code editor
//    @Install(to = "reportQueryCodeEditor", subject = "contextHelpIconClickHandler")
//    private void reportQueryCodeEditorContextHelpIconClickHandler(HasContextHelp.ContextHelpIconClickEvent contextHelpIconClickEvent) {
//        dialogs.createMessageDialog()
//                .withHeader(messages.getMessage("reportQueryHelpCaption"))
//                .withContent(messages.getMessage("reportQueryHelp"))
//                .withResizable(true)
//                .withModal(false)
//                .withWidth("50em")
//                .open();
//    }

    protected void initReportQueryCodeEditorScript() {
        Icon expandIcon = VaadinIcon.EXPAND_SQUARE.create();
        expandIcon.addClassNames(FIELD_ICON_SIZE_CLASS_NAME, FIELD_ICON_CLASS_NAME);
        expandIcon.addClickListener(this::onReportQueryCodeEditorExpandIconClick);

        Icon helpIcon = VaadinIcon.QUESTION_CIRCLE.create();
        helpIcon.addClassNames(FIELD_ICON_SIZE_CLASS_NAME, FIELD_ICON_CLASS_NAME);
        helpIcon.addClickListener(this::onReportQueryCodeEditorHelpIconClick);

        reportQueryCodeEditor.setSuffixComponent(new Div(expandIcon, helpIcon));
    }


    protected void onReportQueryCodeEditorHelpIconClick(ClickEvent<Icon> event) {
        dialogs.createMessageDialog()
                .withHeader(messages.getMessage(getClass(), "reportQueryHelpCaption"))
                .withContent(new Html(messages.getMessage(getClass(), "reportQueryHelp")))
                .withResizable(true)
                .withModal(false)
                .withWidth("50em")
                .open();
    }

    protected void onReportQueryCodeEditorExpandIconClick(ClickEvent<Icon> event) {
        reportsUiHelper.showScriptEditorDialog(
                getScriptEditorDialogCaption(),
                reportDataDc.getItem().getQuery(),
                value -> reportDataDc.getItem().setQuery(value),
                this::onReportQueryCodeEditorHelpIconClick
        );
    }

    protected String getScriptEditorDialogCaption() {
        String reportName = reportDataDc.getItem().getName();
        String bandName = reportDataDc.getItem().getName();

        if (ObjectUtils.isNotEmpty(bandName) && ObjectUtils.isNotEmpty(reportName)) {
            return messages.formatMessage(
                    "bandsTab.dataSetTypeLayout.jsonGroovyCodeEditor.expandIcon.dialog.header", reportName, bandName);
        }
        return StringUtils.EMPTY;
    }

    @Install(to = "reportParameterTable.edit", subject = "afterSaveHandler")
    private void reportParameterTableEditAfterSaveHandler(QueryParameter queryParameter) {
        setDefaultValue(queryParameter);
    }

    @Install(to = "reportParameterTable.create", subject = "afterSaveHandler")
    private void reportParameterTableCreateAfterSaveHandler(QueryParameter queryParameter) {
        setDefaultValue(queryParameter);
    }


    protected void setDefaultValue(QueryParameter queryParameter) {
        try {
            Object value = jmixObjectToStringConverter.convertFromString(Class.forName(queryParameter.getJavaClassName()), queryParameter.getDefaultValueString());
            queryParameter.setDefaultValue(value);
            queryParametersDc.replaceItem(queryParameter);
        } catch (ClassNotFoundException e) {
//todo add logging
        }
    }

//todo AN
//    @Override
//    public List<Suggestion> getSuggestions(AutoCompleteSupport source, String text, int cursorPosition) {
//        if (StringUtils.isBlank(text)) {
//            return Collections.emptyList();
//        }
//        int queryPosition = cursorPosition - 1;
//
//        return jpqlUiSuggestionProvider.getSuggestions(text, queryPosition, source);
//    }
//@Install(to = "reportParameterTable.defaultValueString", subject = "valueProvider")
//protected Object reportParameterTableDefaultStringValueProvider(QueryParameter queryParameter) {
//    Object defaultValue = queryParameter.getDefaultValue();
//    if (defaultValue != null) {
//        ParameterType parameterType = queryParameter.getParameterType();
//        switch (parameterType) {
//            case DATE:
//                String dateFormat = formatStringsRegistry.getFormatStrings(currentAuthentication.getLocale()).getDateFormat();
//                return new SimpleDateFormat(dateFormat).format(defaultValue);
//            case TIME:
//                String timeFormat = formatStringsRegistry.getFormatStrings(currentAuthentication.getLocale()).getTimeFormat();
//                return new SimpleDateFormat(timeFormat).format(defaultValue);
//            default:
//                return defaultValue;
//        }
//    }
//
//    return StringUtils.EMPTY;
//}

}