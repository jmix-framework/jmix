/*
 * Copyright 2020 Haulmont.
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

package io.jmix.datatoolsui.screen.entityinspector;

import com.google.common.collect.Sets;
import com.google.common.io.Files;
import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import io.jmix.core.metamodel.model.Session;
import io.jmix.datatoolsui.action.ExportAction;
import io.jmix.datatoolsui.screen.entityinspector.assistant.InspectorFetchPlanBuilder;
import io.jmix.datatoolsui.screen.entityinspector.assistant.InspectorTableBuilder;
import io.jmix.ui.Actions;
import io.jmix.ui.Notifications;
import io.jmix.ui.UiComponents;
import io.jmix.ui.UiProperties;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.list.CreateAction;
import io.jmix.ui.action.list.EditAction;
import io.jmix.ui.action.list.RefreshAction;
import io.jmix.ui.action.list.RemoveAction;
import io.jmix.ui.component.LookupComponent;
import io.jmix.ui.component.*;
import io.jmix.ui.context.UiEntityContext;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.model.DataComponents;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.screen.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import static com.google.common.base.Strings.nullToEmpty;
import static io.jmix.ui.download.DownloadFormat.JSON;
import static io.jmix.ui.download.DownloadFormat.ZIP;

@SuppressWarnings({"rawtypes", "unchecked"})
@Route("jmixEntityInspector")
@UiController("entityInspector.browse")
@UiDescriptor("entity-inspector-browser.xml")
public class EntityInspectorBrowser extends StandardLookup<JmixEntity> {

    public static final int MAX_TEXT_LENGTH = 50;

    protected static final Logger log = LoggerFactory.getLogger(EntityInspectorBrowser.class);

    @Autowired
    protected Messages messages;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected DataComponents dataComponents;

    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected UiProperties uiProperties;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected Icons icons;
    @Autowired
    protected Actions actions;

    @Autowired
    protected BoxLayout lookupBox;
    @Autowired
    protected BoxLayout tableBox;

    @Autowired
    protected ComboBox<MetaClass> entitiesLookup;

    @Autowired
    protected CheckBox removedRecords;

    @Autowired
    protected CheckBox textSelection;

    @Autowired
    protected BoxLayout filterBox;

    @Autowired
    protected EntityImportExport entityImportExport;

    @Autowired
    protected EntityImportViews entityImportViews;

    //TODO filter implementation component (Filter in Table/DataGrid #221)
    protected Component filter;
    protected Table entitiesTable;

    protected MetaClass selectedMeta;
    private CollectionLoader entitiesDl;
    private CollectionContainer entitiesDc;

    protected String entityName;

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        getScreenData().setDataContext(dataComponents.createDataContext());
        if (entityName != null) {
            Session session = metadata.getSession();
            selectedMeta = session.getClass(entityName);
            createEntitiesTable(selectedMeta);
            lookupBox.setVisible(false);
        } else {
            entitiesLookup.setOptionsMap(getEntitiesLookupFieldOptions());
            entitiesLookup.addValueChangeListener(e -> showEntities());
            removedRecords.addValueChangeListener(e -> showEntities());
        }
        textSelection.addValueChangeListener(e -> changeTableTextSelectionEnabled());
    }

    @Override
    protected LookupComponent<JmixEntity> getLookupComponent() {
        return entitiesTable;
    }

    protected Map<String, MetaClass> getEntitiesLookupFieldOptions() {
        Map<String, MetaClass> options = new TreeMap<>();

        for (MetaClass metaClass : metadata.getClasses()) {
            if (readPermitted(metaClass)) {
                Class javaClass = metaClass.getJavaClass();
                if (JmixEntity.class.isAssignableFrom(javaClass) && isNotAbstract(javaClass)) {
                    options.put(messageTools.getEntityCaption(metaClass) + " (" + metaClass.getName() + ")", metaClass);
                }
            }
        }

        return options;
    }

    private boolean isNotAbstract(Class javaClass) {
        return !javaClass.isInterface() && !Modifier.isAbstract(javaClass.getModifiers());
    }

    private void showEntities() {
        selectedMeta = entitiesLookup.getValue();
        if (selectedMeta != null) {
            createEntitiesTable(selectedMeta);
            getWindow().setCaption(messageTools.getEntityCaption(selectedMeta));
        }
    }

    protected void changeTableTextSelectionEnabled() {
        entitiesTable.setTextSelectionEnabled(textSelection.isChecked());
    }

    protected void createEntitiesTable(MetaClass meta) {
        if (entitiesTable != null)
            tableBox.remove(entitiesTable);
        if (filter != null) {
            filterBox.remove(filter);
        }
        textSelection.setVisible(true);

        entitiesTable = InspectorTableBuilder.from(getBeanLocator(), createContainer(meta))
                .withMaxTextLength(MAX_TEXT_LENGTH)
                .withSystem(true)
                .withButtons(this::createButtonsPanel)
                .build();

        tableBox.add(entitiesTable);
        tableBox.expand(entitiesTable);

        addSelectionListener();
        createFilter();
    }

    private void addSelectionListener() {
        entitiesTable.addSelectionListener(event -> {
            Table.SelectionEvent selectionEvent = (Table.SelectionEvent) event;
            boolean removeEnabled = true;
            if (!selectionEvent.getSelected().isEmpty()) {
                for (Object o : selectionEvent.getSelected()) {
                    if (o instanceof JmixEntity) {
                        if (EntityValues.isSoftDeleted((JmixEntity) o)) {
                            removeEnabled = false;
                        }
                    }
                }
            }
            Action removeAction = entitiesTable.getAction(RemoveAction.ID);
            if (removeAction != null) {
                removeAction.setEnabled(removeEnabled && removeAction.isEnabled());
            }
        });
    }

    private CollectionContainer createContainer(MetaClass meta) {
        entitiesDc = dataComponents.createCollectionContainer(meta.getJavaClass());
        FetchPlan fetchPlan = InspectorFetchPlanBuilder.of(getBeanLocator(), meta.getJavaClass())
                .withSystemProperties(true)
                .build();
        entitiesDc.setFetchPlan(fetchPlan);

        entitiesDl = dataComponents.createCollectionLoader();
        entitiesDl.setFetchPlan(fetchPlan);
        entitiesDl.setContainer(entitiesDc);
        entitiesDl.setLoadDynamicAttributes(true);
        entitiesDl.setSoftDeletion(!removedRecords.isChecked());
        entitiesDl.setQuery(String.format("select e from %s e", meta.getName()));
        entitiesDl.load();
        return entitiesDc;
    }

    //TODO create filter component (Filter in Table/DataGrid #221)
    protected void createFilter() {

    }

    protected void createButtonsPanel(Table table) {
        ButtonsPanel buttonsPanel = table.getButtonsPanel();

        Button createButton = uiComponents.create(Button.class);
        CreateAction createAction = createCreateAction(table);
        table.addAction(createAction);
        createButton.setAction(createAction);
        createButton.setIcon(icons.get(JmixIcon.CREATE_ACTION));

        Button editButton = uiComponents.create(Button.class);
        EditAction editAction = createEditAction(table);
        table.addAction(editAction);
        editButton.setAction(editAction);
        editButton.setIcon(icons.get(JmixIcon.EDIT_ACTION));

        Button removeButton = uiComponents.create(Button.class);
        RemoveAction removeAction = createRemoveAction(table);
        table.addAction(removeAction);
        removeButton.setAction(removeAction);
        removeButton.setIcon(icons.get(JmixIcon.REMOVE_ACTION));
        removeButton.setFrame(getWindow().getFrame());

        //TODO excel action
//        excelButton = uiComponents.create(Button.class);
//        excelButton.setCaption(messages.getMessage(com.haulmont.cuba.gui.app.core.entityinspector.EntityInspectorBrowse.class, "excel"));
//        excelButton.setAction(new ExcelAction(entitiesTable));
//        excelButton.setIcon(icons.get(CubaIcon.EXCEL_ACTION));
//        excelButton.setFrame(frame);

        Button refreshButton = uiComponents.create(Button.class);
        RefreshAction refreshAction = createRefreshAction(table);
        refreshButton.setAction(refreshAction);
        refreshButton.setIcon(icons.get(JmixIcon.REFRESH_ACTION));
        refreshButton.setFrame(getWindow().getFrame());

        PopupButton exportPopupButton = uiComponents.create(PopupButton.class);
        exportPopupButton.setCaption(messages.getMessage(EntityInspectorBrowser.class, "export"));
        exportPopupButton.setIcon(icons.get(JmixIcon.DOWNLOAD));

        ExportAction exportJSONAction = (ExportAction) actions.create(ExportAction.ID, "exportJSON");
        exportJSONAction.setFormat(JSON);
        exportJSONAction.setTable(entitiesTable);
        exportJSONAction.setMetaClass(selectedMeta);
        exportPopupButton.addAction(exportJSONAction);

        ExportAction exportZIPAction = (ExportAction) actions.create(ExportAction.ID, "exportZIP");
        exportZIPAction.setFormat(ZIP);
        exportZIPAction.setTable(entitiesTable);
        exportZIPAction.setMetaClass(selectedMeta);
        exportPopupButton.addAction(exportZIPAction);

        FileUploadField importUpload = uiComponents.create(FileUploadField.class);
        importUpload.setPasteZone(tableBox);
        importUpload.setPermittedExtensions(Sets.newHashSet(".json", ".zip"));
        importUpload.setUploadButtonIcon(icons.get(JmixIcon.UPLOAD));
        importUpload.setUploadButtonCaption(messages.getMessage(EntityInspectorBrowser.class, "import"));

        importUpload.addFileUploadSucceedListener(event -> {
            byte[] fileBytes = importUpload.getValue();
            String fileName = event.getFileName();
            try {
                Collection<JmixEntity> importedEntities;
                if (JSON.getFileExt().equals(Files.getFileExtension(fileName))) {
                    String content = new String(fileBytes, StandardCharsets.UTF_8);
                    importedEntities = entityImportExport.importEntitiesFromJson(content, createEntityImportView(selectedMeta));
                } else {
                    importedEntities = entityImportExport.importEntitiesFromZIP(fileBytes, createEntityImportView(selectedMeta));
                }

                notifications.create(Notifications.NotificationType.HUMANIZED)
                        .withDescription(messages.formatMessage(EntityInspectorBrowser.class, "importSuccessful", importedEntities.size()))
                        .show();
            } catch (Exception e) {
                notifications.create(Notifications.NotificationType.ERROR)
                        .withCaption(messages.getMessage(EntityInspectorBrowser.class, "importFailed"))
                        .withDescription(messages.formatMessage(
                                EntityInspectorBrowser.class, "importFailedMessage",
                                fileName, nullToEmpty(e.getMessage())))
                        .show();
                log.error("Entities import error", e);
            }
            entitiesDl.load();
        });

        buttonsPanel.add(createButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(removeButton);
//        buttonsPanel.add(excelButton);
        buttonsPanel.add(refreshButton);
        buttonsPanel.add(exportPopupButton);
        buttonsPanel.add(importUpload);
    }

    private RefreshAction createRefreshAction(Table table) {
        RefreshAction refreshAction = actions.create(RefreshAction.class);
        refreshAction.setTarget(table);
        return refreshAction;
    }

    private RemoveAction createRemoveAction(Table table) {
        RemoveAction removeAction = actions.create(RemoveAction.class);
        removeAction.setTarget(table);
        return removeAction;
    }

    private CreateAction createCreateAction(Table table) {
        CreateAction createAction = actions.create(CreateAction.class);
        createAction.setOpenMode(OpenMode.THIS_TAB);
        createAction.setTarget(table);
        createAction.setScreenClass(EntityInspectorEditor.class);
        createAction.setNewEntitySupplier(() -> metadata.create(selectedMeta));
        createAction.setShortcut(uiProperties.getTableInsertShortcut());
        return createAction;
    }

    private EditAction createEditAction(Table table) {
        EditAction editAction = actions.create(EditAction.class);
        editAction.setOpenMode(OpenMode.THIS_TAB);
        editAction.setTarget(table);
        editAction.setScreenClass(EntityInspectorEditor.class);
        editAction.setShortcut(uiProperties.getTableInsertShortcut());
        return editAction;
    }

    protected EntityImportView createEntityImportView(MetaClass metaClass) {
        EntityImportViewBuilder viewBuilder = entityImportViews.builder(metaClass.getJavaClass());

        for (MetaProperty metaProperty : metaClass.getProperties()) {
            if (!metadataTools.isPersistent(metaProperty)) {
                continue;
            }

            switch (metaProperty.getType()) {
                case DATATYPE:
                case ENUM:
                    viewBuilder.addLocalProperty(metaProperty.getName());
                    break;
                case ASSOCIATION:
                case COMPOSITION:
                    Range.Cardinality cardinality = metaProperty.getRange().getCardinality();
                    if (cardinality == Range.Cardinality.MANY_TO_ONE) {
                        viewBuilder.addManyToOneProperty(metaProperty.getName(), ReferenceImportBehaviour.IGNORE_MISSING);
                    } else if (cardinality == Range.Cardinality.ONE_TO_ONE) {
                        viewBuilder.addOneToOneProperty(metaProperty.getName(), ReferenceImportBehaviour.IGNORE_MISSING);
                    }
                    break;
                default:
                    throw new IllegalStateException("unknown property type");
            }
        }
        return viewBuilder.build();
    }

    protected boolean readPermitted(MetaClass metaClass) {
        UiEntityContext entityContext = new UiEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(new UiEntityContext(metaClass));
        return entityContext.isViewPermitted();
    }
}