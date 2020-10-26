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
package io.jmix.reportsui.gui.report.edit;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.haulmont.cuba.gui.components.FileUploadField;
import com.haulmont.cuba.gui.components.GroupBoxLayout;
import com.haulmont.cuba.gui.components.ListComponent;
import com.haulmont.cuba.gui.components.SourceCodeEditor;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.TextArea;
import com.haulmont.cuba.gui.components.Tree;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.components.actions.ExcludeAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import io.jmix.core.CoreProperties;
import io.jmix.core.Id;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.JmixEntity;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.app.core.file.FileUploadDialog;
import com.haulmont.cuba.gui.components.*;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.UiComponents;
import io.jmix.ui.UiProperties;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.action.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.HierarchicalDatasource;
import com.haulmont.cuba.gui.data.impl.CollectionPropertyDatasourceImpl;
import com.haulmont.cuba.gui.data.impl.DatasourceImpl;
import com.haulmont.cuba.gui.data.impl.DatasourceImplementation;
import com.haulmont.cuba.gui.data.impl.HierarchicalPropertyDatasourceImpl;
import com.haulmont.cuba.gui.export.ByteArrayDataProvider;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import io.jmix.core.security.EntityOp;
import io.jmix.reports.app.service.ReportService;
import io.jmix.reports.entity.*;
import io.jmix.reports.ReportPrintHelper;
import io.jmix.reportsui.gui.definition.edit.BandDefinitionEditor;
import com.haulmont.yarg.structure.BandOrientation;
import io.jmix.ui.component.*;
import io.jmix.ui.gui.OpenType;
import io.jmix.ui.sys.ScreensHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ReportEditor extends AbstractEditor<Report> {

    @Named("generalFrame.propertiesFieldGroup")
    protected FieldGroup propertiesFieldGroup;

    @Named("generalFrame.bandEditor")
    protected BandDefinitionEditor bandEditor;

    @Named("securityFrame.screenIdLookup")
    protected LookupField<String> screenIdLookup;

    @Named("securityFrame.screenTable")
    protected Table<ReportScreen> screenTable;

    @Named("templatesFrame.templatesTable")
    protected Table<ReportTemplate> templatesTable;

    @Named("localesFrame.localeTextField")
    protected TextArea localesTextField;

    @Named("run")
    protected Button run;

    @Named("generalFrame.createBandDefinition")
    protected Button createBandDefinitionButton;

    @Named("generalFrame.removeBandDefinition")
    protected Button removeBandDefinitionButton;

    @Named("generalFrame.up")
    protected Button bandUpButton;

    @Named("generalFrame.down")
    protected Button bandDownButton;

    @Named("securityFrame.addReportScreenBtn")
    protected Button addReportScreenBtn;

    @Named("securityFrame.addRoleBtn")
    protected Button addRoleBtn;

    @Named("securityFrame.rolesTable")
    //TODO roles table
    protected Table rolesTable;

    @Named("parametersFrame.inputParametersTable")
    protected Table<ReportInputParameter> parametersTable;

    @Named("formatsFrame.valuesFormatsTable")
    protected Table<ReportValueFormat> formatsTable;

    @Named("parametersFrame.up")
    protected Button paramUpButton;

    @Named("parametersFrame.down")
    protected Button paramDownButton;

    @Named("generalFrame.serviceTree")
    protected Tree<BandDefinition> bandTree;

    @Named("generalFrame.invisibleFileUpload")
    protected FileUploadField invisibleFileUpload;

    @Named("generalFrame.reportFields")
    protected HBoxLayout reportFields;

    @Named("parametersFrame.validationScriptGroupBox")
    protected GroupBoxLayout validationScriptGroupBox;

    @Named("parametersFrame.validationScriptCodeEditor")
    protected SourceCodeEditor validationScriptCodeEditor;

    @Autowired
    protected WindowConfig windowConfig;

    @Autowired
    protected Datasource<Report> reportDs;

    @Autowired
    protected CollectionDatasource<ReportGroup, UUID> groupsDs;

    @Autowired
    protected CollectionDatasource.Sortable<ReportInputParameter, UUID> parametersDs;

    @Autowired
    protected CollectionDatasource<ReportScreen, UUID> reportScreensDs;

    //TODO roles ds
    @Autowired
    protected CollectionDatasource rolesDs;

    //TODO roles ds
    @Autowired
    protected CollectionDatasource lookupRolesDs;

    @Autowired
    protected CollectionDatasource<DataSet, UUID> dataSetsDs;

    @Autowired
    protected HierarchicalDatasource<BandDefinition, UUID> treeDs;

    @Autowired
    protected CollectionDatasource<ReportTemplate, UUID> templatesDs;

    @Autowired
    protected UiComponents uiComponents;

    @Autowired
    protected FileUploadingAPI fileUpload;

    @Autowired
    protected ReportService reportService;

    @Autowired
    protected CollectionDatasource<BandDefinition, UUID> bandsDs;

    @Autowired
    protected CollectionDatasource<BandDefinition, UUID> availableParentBandsDs;

    @Autowired
    protected ScreensHelper screensHelper;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected UuidSource uuidSource;

    @Autowired
    protected Security security;

    @Autowired
    protected Messages messages;

    @Autowired
    protected UiProperties uiProperties;

    @Autowired
    protected CoreProperties coreProperties;

    @Override
    protected void initNewItem(Report report) {
        report.setReportType(ReportType.SIMPLE);

        BandDefinition rootDefinition = metadata.create(BandDefinition.class);
        rootDefinition.setName("Root");
        rootDefinition.setPosition(0);
        report.setBands(new HashSet<>());
        report.getBands().add(rootDefinition);

        rootDefinition.setReport(report);

        groupsDs.refresh();
        Collection<UUID> reportGroupIds = groupsDs.getItemIds();
        if (reportGroupIds != null && !reportGroupIds.isEmpty()) {
            UUID id = reportGroupIds.iterator().next();
            report.setGroup(groupsDs.getItem(id));
        }
    }

    @Override
    public void ready() {
        super.ready();

        if (!StringUtils.isEmpty(getItem().getName())) {
            setCaption(AppBeans.get(Messages.class).formatMessage(getClass(), "reportEditor.format", getItem().getName()));
        }
    }

    @Override
    protected void postInit() {
        super.postInit();

        ((CollectionPropertyDatasourceImpl) treeDs).setModified(false);
        ((DatasourceImpl) reportDs).setModified(false);

        bandTree.getDatasource().refresh();
        bandTree.expandTree();
        bandTree.setSelected(reportDs.getItem().getRootBandDefinition());

        bandEditor.setBandDefinition(bandTree.getSingleSelected());
        if (bandTree.getSingleSelected() == null) {
            bandEditor.setEnabled(false);
        }

        setupDropZoneForTemplate();

        initValidationScriptGroupBoxCaption();
    }

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        initGeneral();
        initTemplates();
        initParameters();
        initRoles();
        initScreens();
        initValuesFormats();
        initHelpButtons();
    }

    protected void initParameters() {
        parametersTable.addAction(
                new CreateAction(parametersTable, OpenType.DIALOG) {
                    @Override
                    public Map<String, Object> getInitialValues() {
                        Map<String, Object> params = new HashMap<>();
                        params.put("position", parametersDs.getItemIds().size());
                        params.put("report", getItem());
                        return params;
                    }

                    @Override
                    public void actionPerform(Component component) {
                        orderParameters();
                        super.actionPerform(component);
                    }
                }
        );

        parametersTable.addAction(new RemoveAction(parametersTable, false) {
            @Override
            protected void afterRemove(Set selected) {
                super.afterRemove(selected);
                orderParameters();
            }
        });
        parametersTable.addAction(new EditAction(parametersTable, OpenType.DIALOG));

        paramUpButton.setAction(new ListAction("generalFrame.up") {
            @Override
            public void actionPerform(Component component) {
                ReportInputParameter parameter = (ReportInputParameter) target.getSingleSelected();
                if (parameter != null) {
                    List<ReportInputParameter> inputParameters = getItem().getInputParameters();
                    int index = parameter.getPosition();
                    if (index > 0) {
                        ReportInputParameter previousParameter = null;
                        for (ReportInputParameter _param : inputParameters) {
                            if (_param.getPosition() == index - 1) {
                                previousParameter = _param;
                                break;
                            }
                        }
                        if (previousParameter != null) {
                            parameter.setPosition(previousParameter.getPosition());
                            previousParameter.setPosition(index);

                            sortParametersByPosition();
                        }
                    }
                }
            }

            @Override
            protected boolean isApplicable() {
                if (target != null) {
                    ReportInputParameter item = (ReportInputParameter) target.getSingleSelected();
                    if (item != null && parametersDs.getItem() == item) {
                        return item.getPosition() > 0 && isUpdatePermitted();
                    }
                }

                return false;
            }
        });

        paramDownButton.setAction(new ListAction("generalFrame.down") {
            @Override
            public void actionPerform(Component component) {
                ReportInputParameter parameter = (ReportInputParameter) target.getSingleSelected();
                if (parameter != null) {
                    List<ReportInputParameter> inputParameters = getItem().getInputParameters();
                    int index = parameter.getPosition();
                    if (index < parametersDs.getItemIds().size() - 1) {
                        ReportInputParameter nextParameter = null;
                        for (ReportInputParameter _param : inputParameters) {
                            if (_param.getPosition() == index + 1) {
                                nextParameter = _param;
                                break;
                            }
                        }
                        if (nextParameter != null) {
                            parameter.setPosition(nextParameter.getPosition());
                            nextParameter.setPosition(index);

                            sortParametersByPosition();
                        }
                    }
                }
            }

            @Override
            protected boolean isApplicable() {
                if (target != null) {
                    ReportInputParameter item = (ReportInputParameter) target.getSingleSelected();
                    if (item != null && parametersDs.getItem() == item) {
                        return item.getPosition() < parametersDs.size() - 1 && isUpdatePermitted();
                    }
                }

                return false;
            }
        });

        parametersTable.addAction(paramUpButton.getAction());
        parametersTable.addAction(paramDownButton.getAction());

        parametersDs.addItemPropertyChangeListener(e -> {
            if ("position".equals(e.getProperty())) {
                ((DatasourceImplementation) parametersDs).modified(e.getItem());
            }
        });
    }

    protected boolean isUpdatePermitted() {
        return security.isEntityOpPermitted(metadata.getClassNN(Report.class), EntityOp.UPDATE);
    }

    protected void sortParametersByPosition() {
        MetaClass metaClass = metadata.getClassNN(ReportInputParameter.class);
        MetaPropertyPath mpp = new MetaPropertyPath(metaClass, metaClass.getProperty("position"));

        CollectionDatasource.Sortable.SortInfo<MetaPropertyPath> sortInfo = new CollectionDatasource.Sortable.SortInfo<>();
        sortInfo.setOrder(CollectionDatasource.Sortable.Order.ASC);
        sortInfo.setPropertyPath(mpp);

        parametersDs.sort(new CollectionDatasource.Sortable.SortInfo[]{sortInfo});
    }

    protected void initValuesFormats() {
        CreateAction formatCreateAction = CreateAction.create(formatsTable, OpenType.DIALOG);
        formatCreateAction.setInitialValuesSupplier(() ->
                ParamsMap.of("report", getItem())
        );
        formatsTable.addAction(formatCreateAction);

        formatsTable.addAction(new RemoveAction(formatsTable, false));
        formatsTable.addAction(new EditAction(formatsTable, OpenType.DIALOG));
    }

    protected void initRoles() {
        rolesTable.addAction(new ExcludeAction(rolesTable, false, true) {
            @Override
            public boolean isEnabled() {
                return super.isEnabled() && isUpdatePermitted();
            }
        });

        addRoleBtn.setAction(new AbstractAction("actions.Add") {
            @Override
            public void actionPerform(Component component) {
                if (lookupRolesDs.getItem() != null && !rolesDs.containsItem(Id.of(lookupRolesDs.getItem()).getValue())) {
                    rolesDs.addItem(lookupRolesDs.getItem());
                }
            }

            @Override
            public boolean isEnabled() {
                return super.isEnabled() && isUpdatePermitted();
            }
        });
    }

    protected void initHelpButtons(){
        localesTextField.setContextHelpIconClickHandler(e ->
                showMessageDialog(getMessage("localeText"), getMessage("report.localeTextHelp"),
                        MessageType.CONFIRMATION_HTML
                                .modal(false)
                                .width(600f)));
        validationScriptCodeEditor.setContextHelpIconClickHandler(e ->
                showMessageDialog(getMessage("validationScript"), getMessage("crossFieldValidationScriptHelp"),
                        MessageType.CONFIRMATION_HTML
                                .modal(false)
                                .width(600f)));
    }

    protected void initScreens() {
        screenTable.addAction(new RemoveAction(screenTable, false));
        List<WindowInfo> windowInfoCollection = new ArrayList<>(windowConfig.getWindows());
        // sort by screenId
        screensHelper.sortWindowInfos(windowInfoCollection);

        Map<String, String> screens = new LinkedHashMap<>();
        for (WindowInfo windowInfo : windowInfoCollection) {
            String id = windowInfo.getId();
            String menuId = "menu-config." + id;
            String localeMsg = AppBeans.get(Messages.class).getMessage(messages.getMainMessage(menuId));
            String title = menuId.equals(localeMsg) ? id : id + " ( " + localeMsg + " )";
            screens.put(title, id);
        }
        screenIdLookup.setOptionsMap(screens);

        addReportScreenBtn.setAction(new AbstractAction("actions.Add") {
            @Override
            public void actionPerform(Component component) {
                if (screenIdLookup.getValue() != null) {
                    String screenId = screenIdLookup.getValue();

                    boolean exists = false;
                    for (UUID id : reportScreensDs.getItemIds()) {
                        ReportScreen item = reportScreensDs.getItem(id);
                        if (screenId.equalsIgnoreCase(item.getScreenId())) {
                            exists = true;
                            break;
                        }
                    }

                    if (!exists) {
                        ReportScreen reportScreen = metadata.create(ReportScreen.class);
                        reportScreen.setReport(getItem());
                        reportScreen.setScreenId(screenId);
                        reportScreensDs.addItem(reportScreen);
                    }
                }
            }

            @Override
            public boolean isEnabled() {
                return super.isEnabled() && isUpdatePermitted();
            }
        });
    }

    private boolean isChildOrEqual(BandDefinition definition, BandDefinition child) {
        if (definition.equals(child)) {
            return true;
        } else if (child != null) {
            return isChildOrEqual(definition, child.getParentBandDefinition());
        } else {
            return false;
        }
    }

    protected void initGeneral() {
        invisibleFileUpload.addFileUploadSucceedListener(invisibleUpload -> {
            final ReportTemplate defaultTemplate = getItem().getDefaultTemplate();
            if (defaultTemplate != null) {
                if (!isTemplateWithoutFile(defaultTemplate)) {
                    File file = fileUpload.getFile(invisibleFileUpload.getFileId());
                    try {
                        byte[] data = FileUtils.readFileToByteArray(file);
                        defaultTemplate.setContent(data);
                        defaultTemplate.setName(invisibleFileUpload.getFileName());
                        templatesDs.modifyItem(defaultTemplate);
                    } catch (IOException e) {
                        throw new RuntimeException(String.format(
                                "An error occurred while uploading file for template [%s]",
                                defaultTemplate.getCode()));
                    }
                } else {
                    showNotification(getMessage("notification.fileIsNotAllowedForSpecificTypes"), NotificationType.HUMANIZED);
                }
            } else {
                showNotification(getMessage("notification.defaultTemplateIsEmpty"), NotificationType.HUMANIZED);
            }
        });

        treeDs.addItemChangeListener(e -> {
            bandEditor.setBandDefinition(e.getItem());
            bandEditor.setEnabled(e.getItem() != null);
            availableParentBandsDs.clear();
            if (e.getItem() != null) {
                for (BandDefinition bandDefinition : bandsDs.getItems()) {
                    if (!isChildOrEqual(e.getItem(), bandDefinition) ||
                            Objects.equals(e.getItem().getParentBandDefinition(), bandDefinition)) {
                        availableParentBandsDs.addItem(bandDefinition);
                    }
                }
            }
        });

        bandEditor.getBandDefinitionDs().addItemPropertyChangeListener(e -> {
            if ("parentBandDefinition".equals(e.getProperty())) {
                BandDefinition previousParent = (BandDefinition) e.getPrevValue();
                BandDefinition parent = (BandDefinition) e.getValue();

                if (e.getValue() == e.getItem()) {
                    e.getItem().setParentBandDefinition(previousParent);
                } else {
                    treeDs.refresh();
                    previousParent.getChildrenBandDefinitions().remove(e.getItem());
                    parent.getChildrenBandDefinitions().add(e.getItem());
                }

                if (e.getPrevValue() != null) {
                    orderBandDefinitions(previousParent);
                }

                if (e.getValue() != null) {
                    orderBandDefinitions(parent);
                }
            }
            treeDs.modifyItem(e.getItem());
        });

        propertiesFieldGroup.addCustomField("defaultTemplate", new FieldGroup.CustomFieldGenerator() {
            @Override
            public Component generateField(Datasource datasource, String propertyId) {
                LookupPickerField lookupPickerField = uiComponents.create(LookupPickerField.class);

                lookupPickerField.setOptionsDatasource(templatesDs);
                lookupPickerField.setDatasource(datasource, propertyId);

                lookupPickerField.addAction(new AbstractAction("download") {

                    @Override
                    public String getDescription() {
                        return getMessage("description.downloadTemplate");
                    }

                    @Override
                    public String getCaption() {
                        return null;
                    }

                    @Override
                    public String getIcon() {
                        return "icons/reports-template-download.png";
                    }

                    @Override
                    public void actionPerform(Component component) {
                        ReportTemplate defaultTemplate = getItem().getDefaultTemplate();
                        if (defaultTemplate != null) {
                            if (defaultTemplate.isCustom()) {
                                showNotification(getMessage("unableToSaveTemplateWhichDefinedWithClass"), NotificationType.HUMANIZED);
                            } else if (isTemplateWithoutFile(defaultTemplate)) {
                                showNotification(getMessage("notification.fileIsNotAllowedForSpecificTypes"), NotificationType.HUMANIZED);
                            } else {
                                ExportDisplay exportDisplay = AppBeans.getPrototype(ExportDisplay.class);
                                byte[] reportTemplate = defaultTemplate.getContent();
                                exportDisplay.show(new ByteArrayDataProvider(reportTemplate, uiProperties.getSaveExportedByteArrayDataThresholdBytes(), coreProperties.getTempDir()),
                                        defaultTemplate.getName(), ExportFormat.getByExtension(defaultTemplate.getExt()));
                            }
                        } else {
                            showNotification(getMessage("notification.defaultTemplateIsEmpty"), NotificationType.HUMANIZED);
                        }

                        lookupPickerField.focus();
                    }
                });

                lookupPickerField.addAction(new AbstractAction("upload") {
                    @Override
                    public String getDescription() {
                        return getMessage("description.uploadTemplate");
                    }

                    @Override
                    public String getCaption() {
                        return null;
                    }

                    @Override
                    public String getIcon() {
                        return "icons/reports-template-upload.png";
                    }

                    @Override
                    public void actionPerform(Component component) {
                        final ReportTemplate defaultTemplate = getItem().getDefaultTemplate();
                        if (defaultTemplate != null) {
                            if (!isTemplateWithoutFile(defaultTemplate)) {
                                FileUploadDialog dialog = (FileUploadDialog) openWindow("fileUploadDialog", OpenType.DIALOG);
                                dialog.addCloseListener(actionId -> {
                                    if (COMMIT_ACTION_ID.equals(actionId)) {
                                        File file = fileUpload.getFile(dialog.getFileId());
                                        try {
                                            byte[] data = FileUtils.readFileToByteArray(file);
                                            defaultTemplate.setContent(data);
                                            defaultTemplate.setName(dialog.getFileName());
                                            templatesDs.modifyItem(defaultTemplate);
                                        } catch (IOException e) {
                                            throw new RuntimeException(String.format(
                                                    "An error occurred while uploading file for template [%s]",
                                                    defaultTemplate.getCode()));
                                        }
                                    }
                                    lookupPickerField.focus();
                                });
                            } else {
                                showNotification(getMessage("notification.fileIsNotAllowedForSpecificTypes"), NotificationType.HUMANIZED);
                            }
                        } else {
                            showNotification(getMessage("notification.defaultTemplateIsEmpty"), NotificationType.HUMANIZED);
                        }
                    }

                    @Override
                    public boolean isEnabled() {
                        return super.isEnabled() && isUpdatePermitted();
                    }
                });

                lookupPickerField.addAction(new AbstractAction("create") {

                    @Override
                    public String getDescription() {
                        return getMessage("description.createTemplate");
                    }

                    @Override
                    public String getIcon() {
                        return "icons/plus-btn.png";
                    }

                    @Override
                    public void actionPerform(Component component) {
                        ReportTemplate template = metadata.create(ReportTemplate.class);
                        template.setReport(getItem());

                        Editor editor = openEditor("report$ReportTemplate.edit", template, OpenType.DIALOG, templatesDs);
                        editor.addCloseListener(actionId -> {
                            if (COMMIT_ACTION_ID.equals(actionId)) {
                                ReportTemplate item = (ReportTemplate) editor.getItem();
                                templatesDs.addItem(item);
                                getItem().setDefaultTemplate(item);
                                //Workaround to disable button after default template setting
                                Action defaultTemplate = templatesTable.getActionNN("defaultTemplate");
                                defaultTemplate.refreshState();
                            }
                            lookupPickerField.focus();
                        });
                    }

                    @Override
                    public boolean isEnabled() {
                        return super.isEnabled() && isUpdatePermitted();
                    }
                });

                lookupPickerField.addAction(new AbstractAction("edit") {
                    @Override
                    public String getDescription() {
                        return getMessage("description.editTemplate");
                    }

                    @Override
                    public String getIcon() {
                        return "icons/reports-template-view.png";
                    }

                    @Override
                    public void actionPerform(Component component) {
                        ReportTemplate defaultTemplate = getItem().getDefaultTemplate();
                        if (defaultTemplate != null) {
                            Editor editor = openEditor("report$ReportTemplate.edit",
                                    defaultTemplate, OpenType.DIALOG, templatesDs);

                            editor.addCloseListener(actionId -> {
                                if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                                    ReportTemplate item = (ReportTemplate) editor.getItem();
                                    getItem().setDefaultTemplate(item);
                                    templatesDs.modifyItem(item);
                                }
                                lookupPickerField.focus();
                            });
                        } else {
                            showNotification(getMessage("notification.defaultTemplateIsEmpty"), NotificationType.HUMANIZED);
                        }
                    }

                    @Override
                    public boolean isEnabled() {
                        return super.isEnabled() && isUpdatePermitted();
                    }
                });

                lookupPickerField.addValueChangeListener(event -> {
                    setupDropZoneForTemplate();
                });

                lookupPickerField.setEditable(isUpdatePermitted());

                return lookupPickerField;
            }
        });


        ((HierarchicalPropertyDatasourceImpl) treeDs).setSortPropertyName("position");

        createBandDefinitionButton.setAction(new AbstractAction("create") {
            @Override
            public String getDescription() {
                return getMessage("description.createBand");
            }

            @Override
            public String getCaption() {
                return "";
            }

            @Override
            public void actionPerform(Component component) {
                BandDefinition parentDefinition = treeDs.getItem();
                Report report = getItem();
                // Use root band as parent if no items selected
                if (parentDefinition == null) {
                    parentDefinition = report.getRootBandDefinition();
                }
                if (parentDefinition.getChildrenBandDefinitions() == null) {
                    parentDefinition.setChildrenBandDefinitions(new ArrayList<>());
                }

                //
                orderBandDefinitions(parentDefinition);

                BandDefinition newBandDefinition = metadata.create(BandDefinition.class);
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

                treeDs.addItem(newBandDefinition);

                treeDs.refresh();
                bandTree.expandTree();
                bandTree.setSelected(newBandDefinition);//let's try and see if it increases usability

                bandTree.focus();
            }

            @Override
            public boolean isEnabled() {
                return super.isEnabled() && isUpdatePermitted();
            }
        });

        removeBandDefinitionButton.setAction(new RemoveAction((ListComponent) bandTree, false, "generalFrame.removeBandDefinition") {
            @Override
            public String getDescription() {
                return getMessage("description.removeBand");
            }

            @Override
            public String getCaption() {
                return "";
            }

            @Override
            protected boolean isApplicable() {
                if (target != null) {
                    JmixEntity selectedItem = target.getSingleSelected();
                    if (selectedItem != null) {
                        return !Objects.equals(getItem().getRootBandDefinition(), selectedItem);
                    }
                }

                return false;
            }

            @Override
            protected void doRemove(Set selected, boolean autocommit) {
                if (selected != null) {
                    removeChildrenCascade(selected);
                    for (Object object : selected) {
                        BandDefinition definition = (BandDefinition) object;
                        if (definition.getParentBandDefinition() != null) {
                            orderBandDefinitions(((BandDefinition) object).getParentBandDefinition());
                        }
                    }
                }
                bandTree.focus();
            }

            private void removeChildrenCascade(Collection selected) {
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
                        treeDs.setItem(definition);
                        for (DataSet dataSet : new ArrayList<>(definition.getDataSets())) {
                            if (PersistenceHelper.isNew(dataSet)) {
                                dataSetsDs.removeItem(dataSet);
                            }
                        }
                    }
                    treeDs.removeItem(definition);
                }
            }
        });

        bandUpButton.setAction(new ListAction("generalFrame.up") {
            @Override
            public String getDescription() {
                return getMessage("description.moveUp");
            }

            @Override
            public String getCaption() {
                return "";
            }

            @Override
            public void actionPerform(Component component) {
                BandDefinition definition = (BandDefinition) target.getSingleSelected();
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

                        treeDs.refresh();
                    }
                }
            }

            @Override
            protected boolean isApplicable() {
                if (target != null) {
                    BandDefinition selectedItem = (BandDefinition) target.getSingleSelected();
                    return selectedItem != null && selectedItem.getPosition() > 0 && isUpdatePermitted();
                }

                return false;
            }
        });

        bandDownButton.setAction(new ListAction("generalFrame.down") {
            @Override
            public String getDescription() {
                return getMessage("description.moveDown");
            }

            @Override
            public String getCaption() {
                return "";
            }

            @Override
            public void actionPerform(Component component) {
                BandDefinition definition = (BandDefinition) target.getSingleSelected();
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

                        treeDs.refresh();
                    }
                }
            }

            @Override
            protected boolean isApplicable() {
                if (target != null) {
                    BandDefinition bandDefinition = (BandDefinition) target.getSingleSelected();
                    if (bandDefinition != null) {
                        BandDefinition parent = bandDefinition.getParentBandDefinition();
                        return parent != null &&
                                parent.getChildrenBandDefinitions() != null &&
                                bandDefinition.getPosition() < parent.getChildrenBandDefinitions().size() - 1
                                && isUpdatePermitted();
                    }
                }
                return false;
            }
        });

        bandTree.addAction(createBandDefinitionButton.getAction());
        bandTree.addAction(removeBandDefinitionButton.getAction());
        bandTree.addAction(bandUpButton.getAction());
        bandTree.addAction(bandDownButton.getAction());

        run.setAction(new AbstractAction("button.run") {
            @Override
            public void actionPerform(Component component) {
                if (validateAll()) {
                    getItem().setIsTmp(true);
                    Window runWindow = openWindow("report$inputParameters",
                            OpenType.DIALOG, ParamsMap.of("report", getItem()));

                    runWindow.addCloseListener(actionId -> {
                        bandTree.focus();
                    });
                }
            }
        });
    }

    protected void setupDropZoneForTemplate() {
        final ReportTemplate defaultTemplate = getItem().getDefaultTemplate();
        if (defaultTemplate != null) {
            invisibleFileUpload.setDropZone(new UploadField.DropZone(reportFields));
        } else {
            invisibleFileUpload.setDropZone(null);
        }
    }

    @Override
    public boolean validateAll() {
        return super.validateAll() && validateInputOutputFormats();
    }

    protected boolean validateInputOutputFormats() {
        ReportTemplate template = getItem().getDefaultTemplate();
        if (template != null && !template.isCustom()
                && template.getReportOutputType() != ReportOutputType.CHART
                && template.getReportOutputType() != ReportOutputType.TABLE
                && template.getReportOutputType() != ReportOutputType.PIVOT_TABLE) {
            String inputType = template.getExt();
            if (!ReportPrintHelper.getInputOutputTypesMapping().containsKey(inputType) ||
                    !ReportPrintHelper.getInputOutputTypesMapping().get(inputType).contains(template.getReportOutputType())) {
                showNotification(getMessage("inputOutputTypesError"), NotificationType.TRAY);
                return false;
            }
        }
        return true;
    }

    protected void initTemplates() {
        CreateAction templateCreateAction = CreateAction.create(templatesTable, OpenType.DIALOG);

        templateCreateAction.setInitialValuesSupplier(() ->
                ParamsMap.of("report", getItem())
        );
        templateCreateAction.setAfterCommitHandler(entity -> {
            ReportTemplate reportTemplate = (ReportTemplate) entity;
            ReportTemplate defaultTemplate = getItem().getDefaultTemplate();
            if (defaultTemplate == null) {
                getItem().setDefaultTemplate(reportTemplate);
            }
        });

        templatesTable.addAction(templateCreateAction);

        templatesTable.addAction(new EditAction(templatesTable, OpenType.DIALOG) {
            @Override
            protected void afterCommit(JmixEntity entity) {
                ReportTemplate reportTemplate = (ReportTemplate) entity;
                ReportTemplate defaultTemplate = getItem().getDefaultTemplate();
                if (defaultTemplate != null && defaultTemplate.equals(reportTemplate)) {
                    getItem().setDefaultTemplate(reportTemplate);
                }
            }
        });

        templatesTable.addAction(new RemoveAction(templatesTable, false) {
            @Override
            protected void afterRemove(Set selected) {
                super.afterRemove(selected);

                Report report = getItem();
                ReportTemplate defaultTemplate = report.getDefaultTemplate();
                if (defaultTemplate != null && selected.contains(defaultTemplate)) {
                    ReportTemplate newDefaultTemplate = null;

                    if (templatesDs.getItems().size() == 1) {
                        newDefaultTemplate = templatesDs.getItems().iterator().next();
                    }

                    report.setDefaultTemplate(newDefaultTemplate);
                }
            }
        });

        templatesTable.addAction(new ListAction("defaultTemplate") {
            @Override
            public String getCaption() {
                return getMessage("report.defaultTemplate");
            }

            @Override
            public void actionPerform(Component component) {
                ReportTemplate template = (ReportTemplate) target.getSingleSelected();
                if (template != null) {
                    getItem().setDefaultTemplate(template);
                }

                refreshState();

                templatesTable.focus();
            }

            @Override
            protected boolean isApplicable() {
                if (target != null) {
                    JmixEntity selectedItem = target.getSingleSelected();
                    if (selectedItem != null) {
                        return !Objects.equals(getItem().getDefaultTemplate(), selectedItem);
                    }
                }

                return false;
            }

            @Override
            public boolean isEnabled() {
                return super.isEnabled() && isUpdatePermitted();
            }
        });
        templatesTable.addAction(new ItemTrackingAction("copy") {
            @Override
            public void actionPerform(Component component) {
                ReportTemplate template = (ReportTemplate) target.getSingleSelected();
                if (template != null) {

                    ReportTemplate copy = metadata.getTools().copy(template);
                    copy.setId(uuidSource.createUuid());

                    String copyNamingPattern = getMessage("template.copyNamingPattern");
                    String copyCode = String.format(copyNamingPattern, StringUtils.isEmpty(copy.getCode()) ? StringUtils.EMPTY : copy.getCode());
                    //noinspection unchecked
                    List<String> codes = (List<String>) ((ListComponent)target).getDatasource().getItems().stream()
                            .map(o -> ((ReportTemplate) o).getCode())
                            .filter(o -> !StringUtils.isEmpty((String) o))
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

                    //noinspection unchecked
                    ((ListComponent)target).getDatasource().addItem(copy);
                }
            }

            @Override
            public boolean isEnabled() {
                return super.isEnabled() && isUpdatePermitted();
            }
        });
    }

    protected void orderParameters() {
        if (getItem().getInputParameters() == null) {
            getItem().setInputParameters(new ArrayList<>());
        }

        for (int i = 0; i < getItem().getInputParameters().size(); i++) {
            getItem().getInputParameters().get(i).setPosition(i);
        }
    }

    protected void orderBandDefinitions(BandDefinition parent) {
        if (parent.getChildrenBandDefinitions() != null) {
            List<BandDefinition> childrenBandDefinitions = parent.getChildrenBandDefinitions();
            for (int i = 0, childrenBandDefinitionsSize = childrenBandDefinitions.size(); i < childrenBandDefinitionsSize; i++) {
                BandDefinition bandDefinition = childrenBandDefinitions.get(i);
                bandDefinition.setPosition(i);
            }
        }
    }

    @Override
    protected boolean preCommit() {
        addCommitListeners();

        if (PersistenceHelper.isNew(getItem())) {
            ((CollectionPropertyDatasourceImpl) treeDs).setModified(true);
        }

        return true;
    }

    protected void addCommitListeners() {
        String xml = reportService.convertToString(getItem());
        getItem().setXml(xml);

        reportDs.getDsContext().addBeforeCommitListener(context -> {
            context.getCommitInstances()
                    .removeIf(entity ->
                            !(entity instanceof Report || entity instanceof ReportTemplate)
                    );
        });
    }

    @Override
    protected void postValidate(ValidationErrors errors) {
        if (getItem().getRootBand() == null) {
            errors.add(getMessage("error.rootBandNull"));
        }

        if (CollectionUtils.isNotEmpty(getItem().getRootBandDefinition().getChildrenBandDefinitions())) {
            Multimap<String, BandDefinition> names = ArrayListMultimap.create();
            names.put(getItem().getRootBand().getName(), getItem().getRootBandDefinition());

            for (BandDefinition band : getItem().getRootBandDefinition().getChildrenBandDefinitions()) {
                validateBand(errors, band, names);
            }

            checkForNameDuplication(errors, names);
        }
    }

    protected void checkForNameDuplication(ValidationErrors errors, Multimap<String, BandDefinition> names) {
        for (String name : names.keySet()) {
            Collection<BandDefinition> bandDefinitionsWithsSameNames = names.get(name);
            if (bandDefinitionsWithsSameNames != null && bandDefinitionsWithsSameNames.size() > 1) {
                errors.add(formatMessage("error.bandNamesDuplicated", name));
            }
        }
    }

    protected void validateBand(ValidationErrors errors, BandDefinition band, Multimap<String, BandDefinition> names) {
        names.put(band.getName(), band);

        if (StringUtils.isBlank(band.getName())) {
            errors.add(getMessage("error.bandNameNull"));
        }

        if (band.getBandOrientation() == BandOrientation.UNDEFINED) {
            errors.add(formatMessage("error.bandOrientationNull", band.getName()));
        }

        if (CollectionUtils.isNotEmpty(band.getDataSets())) {
            for (DataSet dataSet : band.getDataSets()) {
                if (StringUtils.isBlank(dataSet.getName())) {
                    errors.add(getMessage("error.dataSetNameNull"));
                }

                if (dataSet.getType() == null) {
                    errors.add(formatMessage("error.dataSetTypeNull", dataSet.getName()));
                }

                if (dataSet.getType() == DataSetType.GROOVY
                        || dataSet.getType() == DataSetType.SQL
                        || dataSet.getType() == DataSetType.JPQL) {
                    if (StringUtils.isBlank(dataSet.getScript())) {
                        errors.add(formatMessage("error.dataSetScriptNull", dataSet.getName()));
                    }
                } else if (dataSet.getType() == DataSetType.JSON) {
                    if (StringUtils.isBlank(dataSet.getJsonSourceText()) && dataSet.getJsonSourceType() != JsonSourceType.PARAMETER) {
                        errors.add(formatMessage("error.jsonDataSetScriptNull", dataSet.getName()));
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

    protected void initValidationScriptGroupBoxCaption() {
        setValidationScriptGroupBoxCaption(reportDs.getItem().getValidationOn());

        reportDs.addItemPropertyChangeListener(e -> {
            boolean validationOnChanged = e.getProperty().equalsIgnoreCase("validationOn");

            if (validationOnChanged) {
                setValidationScriptGroupBoxCaption(e.getItem().getValidationOn());
            }
        });
    }

    protected void setValidationScriptGroupBoxCaption(Boolean onOffFlag) {
        if (BooleanUtils.isTrue(onOffFlag)) {
            validationScriptGroupBox.setCaption(getMessage("report.validationScriptOn"));
        } else {
            validationScriptGroupBox.setCaption(getMessage("report.validationScriptOff"));
        }
    }

    protected boolean isTemplateWithoutFile(ReportTemplate template) {
        return template.getOutputType() == CubaReportOutputType.chart ||
                template.getOutputType() == CubaReportOutputType.table ||
                template.getOutputType() == CubaReportOutputType.pivot;


    }
}