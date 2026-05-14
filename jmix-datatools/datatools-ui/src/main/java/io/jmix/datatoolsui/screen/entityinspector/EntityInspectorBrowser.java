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
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.jmix.core.*;
import io.jmix.core.accesscontext.ExportImportEntityContext;
import io.jmix.core.accesscontext.InMemoryCrudEntityContext;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.impl.importexport.EntityImportPlanJsonBuilder;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import io.jmix.core.metamodel.model.Session;
import io.jmix.core.security.EntityOp;
import io.jmix.data.PersistenceHints;
import io.jmix.datatools.EntityRestore;
import io.jmix.datatoolsui.action.ShowEntityInfoAction;
import io.jmix.datatoolsui.screen.entityinspector.assistant.InspectorFetchPlanBuilder;
import io.jmix.datatoolsui.screen.entityinspector.assistant.InspectorTableBuilder;
import io.jmix.ui.*;
import io.jmix.ui.accesscontext.UiEntityContext;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.action.ItemTrackingAction;
import io.jmix.ui.action.ListAction;
import io.jmix.ui.action.list.*;
import io.jmix.ui.component.LookupComponent;
import io.jmix.ui.component.*;
import io.jmix.ui.download.ByteArrayDataProvider;
import io.jmix.ui.download.DownloadFormat;
import io.jmix.ui.download.Downloader;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.model.DataComponents;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.screen.*;
import io.jmix.gridexportui.action.ExcelExportAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.google.common.base.Strings.nullToEmpty;
import static io.jmix.ui.download.DownloadFormat.JSON;
import static io.jmix.ui.download.DownloadFormat.ZIP;

@SuppressWarnings({"rawtypes", "unchecked"})
@Route("jmixEntityInspector")
@UiController("entityInspector.browse")
@UiDescriptor("entity-inspector-browser.xml")
@MultipleOpen
public class EntityInspectorBrowser extends StandardLookup<Object> {

    public static final int MAX_TEXT_LENGTH = 50;

    private static final String BASE_SELECT_QUERY = "select e from %s e";
    private static final String DELETED_ONLY_SELECT_QUERY = "select e from %s e where e.%s is not null";
    private static final String RESTORE_ACTION_ID = "restore";
    private static final String WIPE_OUT_ACTION_ID = "wipeOut";

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
    private DataManager dataManager;
    @Autowired
    private FetchPlanRepository fetchPlanRepository;
    @Autowired
    FetchPlans fetchPlans;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected UiProperties uiProperties;
    @Autowired
    protected UiComponentProperties componentProperties;
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
    protected BoxLayout filterBox;
    @Autowired
    protected EntityImportExport entityImportExport;
    @Autowired
    protected EntitySerialization entitySerialization;
    @Autowired
    protected EntityImportPlanJsonBuilder importPlanJsonBuilder;
    @Autowired
    protected EntityImportPlans entityImportPlans;
    @Autowired
    protected UnconstrainedDataManager unconstrainedDataManager;
    @Autowired
    private Dialogs dialogs;
    @Autowired
    private EntityRestore entityRestore;
    @Autowired
    private ComboBox<ShowMode> showMode;
    @Autowired
    private CoreProperties coreProperties;
    @Autowired
    private Downloader downloader;

    protected Filter filter;
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
    public void onInit(InitEvent event) {
        showMode.setValue(ShowMode.NON_REMOVED);
        ScreenOptions screenOptions = event.getOptions();
        String entityParam = screenOptions instanceof MapScreenOptions
                ? (String) ((MapScreenOptions) screenOptions).getParams().get("entity")
                : null;

        getScreenData().setDataContext(dataComponents.createDataContext());
        if (entityParam != null) {
            Session session = metadata.getSession();
            selectedMeta = session.getClass(entityParam);
            createEntitiesTable(selectedMeta);
            lookupBox.setVisible(false);
        } else {
            entitiesLookup.setOptionsMap(getEntitiesLookupFieldOptions());
            entitiesLookup.addValueChangeListener(e -> showEntities());
            showMode.addValueChangeListener(e -> showEntities());
        }
    }

    //to handle the usage of entityName public setter
    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        if (entityName != null) {
            Session session = metadata.getSession();
            selectedMeta = session.getClass(entityName);
            createEntitiesTable(selectedMeta);
            lookupBox.setVisible(false);
        }
    }

    @Override
    public LookupComponent<Object> getLookupComponent() {
        return entitiesTable;
    }

    protected Map<String, MetaClass> getEntitiesLookupFieldOptions() {
        Map<String, MetaClass> options = new TreeMap<>();

        for (MetaClass metaClass : metadataTools.getAllJpaEntityMetaClasses()) {
            if (readPermitted(metaClass)) {
                options.put(messageTools.getEntityCaption(metaClass) + " (" + metaClass.getName() + ")", metaClass);
            }
        }

        return options;
    }

    private void showEntities() {
        selectedMeta = entitiesLookup.getValue();
        if (selectedMeta != null) {
            createEntitiesTable(selectedMeta);
            getWindow().setCaption(messageTools.getEntityCaption(selectedMeta));
        }
    }

    protected void createEntitiesTable(MetaClass meta) {
        if (entitiesTable != null)
            tableBox.remove(entitiesTable);
        if (filter != null) {
            filterBox.remove(filter);
        }

        entitiesTable = InspectorTableBuilder.from(getApplicationContext(), createContainer(meta))
                .withMaxTextLength(MAX_TEXT_LENGTH)
                .withSystem(true)
                .withButtons(this::createButtonsPanel)
                .build();

        tableBox.add(entitiesTable);
        tableBox.expand(entitiesTable);

        createFilter();
    }

    protected boolean hasSelectionWithSoftDeletionState(Collection<?> selectedItems, boolean softDeleted) {
        if (selectedItems.isEmpty()) {
            return false;
        }

        for (Object item : selectedItems) {
            if (!(item instanceof Entity) || EntityValues.isSoftDeleted(item) != softDeleted) {
                return false;
            }
        }

        return true;
    }

    protected boolean isEntityOpPermitted(Collection<?> selectedItems, EntityOp entityOp) {
        Map<MetaClass, UiEntityContext> uiAccessCache = new HashMap<>();
        Map<MetaClass, InMemoryCrudEntityContext> inMemoryAccessCache = new HashMap<>();

        for (Object item : selectedItems) {
            if (!(item instanceof Entity)) {
                return false;
            }

            MetaClass metaClass = metadata.getClass(item);
            UiEntityContext uiEntityContext = uiAccessCache.computeIfAbsent(metaClass, key -> {
                UiEntityContext context = new UiEntityContext(key);
                accessManager.applyRegisteredConstraints(context);
                return context;
            });
            if (!isUiEntityOpPermitted(uiEntityContext, entityOp)) {
                return false;
            }

            InMemoryCrudEntityContext inMemoryEntityContext = inMemoryAccessCache.computeIfAbsent(metaClass, key -> {
                InMemoryCrudEntityContext context = new InMemoryCrudEntityContext(key, getApplicationContext());
                accessManager.applyRegisteredConstraints(context);
                return context;
            });

            if (!isInMemoryEntityOpPermitted(inMemoryEntityContext, item, entityOp)) {
                return false;
            }
        }

        return true;
    }

    protected boolean isUiEntityOpPermitted(UiEntityContext entityContext, EntityOp entityOp) {
        if (entityOp == EntityOp.UPDATE) {
            return entityContext.isEditPermitted();
        } else if (entityOp == EntityOp.DELETE) {
            return entityContext.isDeletePermitted();
        } else {
            throw new IllegalArgumentException("Unsupported entity operation: " + entityOp);
        }
    }

    protected boolean isInMemoryEntityOpPermitted(InMemoryCrudEntityContext entityContext, Object item, EntityOp entityOp) {
        if (entityOp == EntityOp.UPDATE) {
            return entityContext.updatePredicate() == null || entityContext.isUpdatePermitted(item);
        } else if (entityOp == EntityOp.DELETE) {
            return entityContext.deletePredicate() == null || entityContext.isDeletePermitted(item);
        } else {
            throw new IllegalArgumentException("Unsupported entity operation: " + entityOp);
        }
    }

    private CollectionContainer createContainer(MetaClass meta) {
        entitiesDc = dataComponents.createCollectionContainer(meta.getJavaClass());
        FetchPlan fetchPlan = InspectorFetchPlanBuilder.of(getApplicationContext(), meta.getJavaClass())
                .withSystemProperties(true)
                .build();
        entitiesDc.setFetchPlan(fetchPlan);

        entitiesDl = dataComponents.createCollectionLoader();
        entitiesDl.setFetchPlan(fetchPlan);
        entitiesDl.setContainer(entitiesDc);

        switch (Objects.requireNonNull(showMode.getValue())) {
            case ALL:
                entitiesDl.setHint(PersistenceHints.SOFT_DELETION, false);
                entitiesDl.setQuery(String.format(BASE_SELECT_QUERY, meta.getName()));
                break;
            case NON_REMOVED:
                entitiesDl.setHint(PersistenceHints.SOFT_DELETION, true);
                entitiesDl.setQuery(String.format(BASE_SELECT_QUERY, meta.getName()));
                break;
            case REMOVED:
                if (metadataTools.isSoftDeletable(meta.getJavaClass())) {
                    entitiesDl.setHint(PersistenceHints.SOFT_DELETION, false);
                    entitiesDl.setQuery(
                            String.format(
                                    DELETED_ONLY_SELECT_QUERY,
                                    meta.getName(),
                                    metadataTools.findDeletedDateProperty(meta.getJavaClass())
                            )
                    );
                } else {
                    entitiesDl.setLoadDelegate(loadContext -> Collections.emptyList());
                }
                break;
            default:
        }

        return entitiesDc;
    }

    protected void createFilter() {
        filter = uiComponents.create(Filter.NAME);
        filter.setId("filter");
        filter.setFrame(getWindow().getFrame());

        filterBox.add(filter);

        filter.setDataLoader(entitiesDl);
        filter.apply();
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

        Button bulkEditButton = uiComponents.create(Button.class);
        BulkEditAction bulkEditAction = createBulkEditAction(table);
        table.addAction(bulkEditAction);
        bulkEditButton.setAction(bulkEditAction);
        bulkEditButton.setIcon(icons.get(JmixIcon.BULK_EDIT_ACTION));

        Button removeButton = uiComponents.create(Button.class);
        RemoveAction removeAction = createRemoveAction(table);
        if (metadataTools.isSoftDeletable(selectedMeta.getJavaClass()) &&
                ShowMode.ALL.equals(showMode.getValue())) {
            removeAction.setAfterActionPerformedHandler(removedItems -> entitiesDl.load());
        }
        table.addAction(removeAction);
        removeButton.setAction(removeAction);
        removeButton.setIcon(icons.get(JmixIcon.REMOVE_ACTION));
        removeButton.setFrame(getWindow().getFrame());

        Button excelButton = uiComponents.create(Button.class);
        ExcelExportAction excelExportAction = createExcelExportAction(table);
        excelButton.setAction(excelExportAction);
        excelButton.setFrame(getWindow().getFrame());

        Button refreshButton = uiComponents.create(Button.class);
        RefreshAction refreshAction = createRefreshAction(table);
        refreshButton.setAction(refreshAction);
        refreshButton.setIcon(icons.get(JmixIcon.REFRESH_ACTION));
        refreshButton.setFrame(getWindow().getFrame());

        PopupButton exportPopupButton = uiComponents.create(PopupButton.class);
        exportPopupButton.setCaption(messages.getMessage(EntityInspectorBrowser.class, "export"));
        exportPopupButton.setIcon(icons.get(JmixIcon.DOWNLOAD));

        ExportAction exportJSONAction = new ExportAction("exportJSON");
        exportJSONAction.setFormat(JSON);
        exportJSONAction.setTable(table);
        exportJSONAction.setMetaClass(selectedMeta);
        exportPopupButton.addAction(exportJSONAction);

        ExportAction exportZIPAction = new ExportAction("exportZIP");
        exportZIPAction.setFormat(ZIP);
        exportZIPAction.setTable(table);
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
                Collection<Object> importedEntities;
                if (JSON.getFileExt().equals(Files.getFileExtension(fileName))) {
                    String content = new String(fileBytes, StandardCharsets.UTF_8);
                    importedEntities = entityImportExport.importEntitiesFromJson(content, createEntityImportPlan(content, selectedMeta));
                } else {
                    importedEntities = entityImportExport.importEntitiesFromZIP(fileBytes, createEntityImportPlan(selectedMeta));
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

        Button restoreButton = uiComponents.create(Button.class);
        Action restoreAction = createRestoreAction(table);
        table.addAction(restoreAction);
        restoreButton.setAction(restoreAction);

        Action showEntityInfoAction = createShowEntityInfoAction(table);
        table.addAction(showEntityInfoAction);

        Button wipeOutButton = null;
        if (metadataTools.isSoftDeletable(selectedMeta.getJavaClass())) {
            wipeOutButton = uiComponents.create(Button.class);
            Action wipeOutAction = createWipeOutAction(table);
            table.addAction(wipeOutAction);
            wipeOutButton.setAction(wipeOutAction);
        }

        buttonsPanel.add(createButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(bulkEditButton);
        buttonsPanel.add(removeButton);
        buttonsPanel.add(refreshButton);
        buttonsPanel.add(excelButton);
        buttonsPanel.add(exportPopupButton);
        buttonsPanel.add(importUpload);
        buttonsPanel.add(restoreButton);
        if (wipeOutButton != null) {
            buttonsPanel.add(wipeOutButton);
        }
    }

    private ExcelExportAction createExcelExportAction(Table table) {
        ExcelExportAction excelExportAction = actions.create(ExcelExportAction.ID);
        excelExportAction.setTarget(table);
        return excelExportAction;
    }

    private ShowEntityInfoAction createShowEntityInfoAction(Table table) {
        ShowEntityInfoAction showEntityInfoAction = actions.create(ShowEntityInfoAction.ID);
        showEntityInfoAction.setTarget(table);
        return showEntityInfoAction;
    }

    private RefreshAction createRefreshAction(Table table) {
        RefreshAction refreshAction = actions.create(RefreshAction.ID);
        refreshAction.setTarget(table);
        return refreshAction;
    }

    private RemoveAction createRemoveAction(Table table) {
        RemoveAction removeAction = actions.create(RemoveAction.ID);
        removeAction.setTarget(table);
        removeAction.addEnabledRule(() -> hasSelectionWithSoftDeletionState(table.getSelected(), false));
        return removeAction;
    }

    private CreateAction createCreateAction(Table table) {
        CreateAction createAction = actions.create(CreateAction.ID);
        createAction.setOpenMode(OpenMode.THIS_TAB);
        createAction.setTarget(table);
        createAction.setScreenClass(EntityInspectorEditor.class);
        createAction.setNewEntitySupplier(() -> metadata.create(selectedMeta));
        createAction.setShortcut(componentProperties.getTableInsertShortcut());
        if (Modifier.isAbstract(selectedMeta.getJavaClass().getModifiers())) {
            createAction.setEnabled(false);
        }

        return createAction;
    }

    private EditAction createEditAction(Table table) {
        EditAction editAction = actions.create(EditAction.ID);
        editAction.setOpenMode(OpenMode.THIS_TAB);
        editAction.setTarget(table);
        editAction.setScreenClass(EntityInspectorEditor.class);
        editAction.setShortcut(componentProperties.getTableInsertShortcut());
        editAction.addEnabledRule(() -> table.getSelected().size() == 1);

        return editAction;
    }

    private BulkEditAction createBulkEditAction(Table table) {
        BulkEditAction bulkEditAction = actions.create(BulkEditAction.ID);
        bulkEditAction.setOpenMode(OpenMode.THIS_TAB);
        bulkEditAction.setTarget(table);
        bulkEditAction.addEnabledRule(() -> table.getSelected().size() > 1);

        return bulkEditAction;
    }

    private Action createRestoreAction(Table table) {
        ListAction action = new ItemTrackingAction(RESTORE_ACTION_ID)
                .withCaption(messages.getMessage(EntityInspectorBrowser.class, "restore"))
                .withHandler(event ->
                        showRestoreDialog()
                );
        action.setTarget(table);
        action.addEnabledRule(() -> {
            Collection<?> selectedItems = table.getSelected();
            return hasSelectionWithSoftDeletionState(selectedItems, true)
                    && isEntityOpPermitted(selectedItems, EntityOp.UPDATE);
        });
        return action;
    }

    private Action createWipeOutAction(Table table) {
        ListAction action = new ItemTrackingAction(WIPE_OUT_ACTION_ID)
                .withCaption(messages.getMessage(EntityInspectorBrowser.class, "wipeOut"))
                .withHandler(event ->
                        showWipeOutDialog()
                );
        action.setTarget(table);
        action.addEnabledRule(() -> {
            Collection<?> selectedItems = table.getSelected();
            return !selectedItems.isEmpty()
                    && isEntityOpPermitted(selectedItems, EntityOp.DELETE);
        });
        return action;
    }

    protected EntityImportPlan createEntityImportPlan(String content, MetaClass metaClass) {
        JsonElement rootElement = JsonParser.parseString(content);
        EntityImportPlan entityImportPlan = rootElement.isJsonArray()
                ? importPlanJsonBuilder.buildFromJsonArray(rootElement.getAsJsonArray(), metaClass)
                : importPlanJsonBuilder.buildFromJson(rootElement.toString(), metaClass);

        for (MetaProperty metaProperty : metaClass.getProperties()) {
            if (!metadataTools.isJpa(metaProperty)) {
                continue;
            }

            switch (metaProperty.getType()) {
                case ASSOCIATION:
                case EMBEDDED:
                case COMPOSITION:
                    EntityImportPlanProperty property = entityImportPlan.getProperty(metaProperty.getName());
                    if (property != null)
                        property.setReferenceImportBehaviour(ReferenceImportBehaviour.IGNORE_MISSING);
                    break;
                default:
            }
        }
        return entityImportPlan;
    }

    protected FetchPlan createEntityExportPlan(MetaClass metaClass) {
        FetchPlanBuilder fetchPlanBuilder = fetchPlans.builder(metaClass.getJavaClass());
        ExportImportEntityContext exportContext = new ExportImportEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(exportContext);

        for (MetaProperty metaProperty : metaClass.getProperties()) {
            if (!exportContext.canExported(metaProperty.getName())) {
                continue;
            }

            switch (metaProperty.getType()) {
                case DATATYPE:
                case ENUM:
                    fetchPlanBuilder.add(metaProperty.getName());
                    break;
                case ASSOCIATION:
                case COMPOSITION:
                case EMBEDDED:
                    fetchPlanBuilder.add(metaProperty.getName(),
                            builder -> builder.addFetchPlan(createNestedEntityExportPlan(metaProperty.getRange().asClass())));
                    break;
                default:
                    throw new IllegalStateException("unknown property type");
            }
        }
        return fetchPlanBuilder.build();
    }

    /**
     * Preserves {@link FetchPlan#LOCAL} semantics for nested entities by filtering the repository-provided LOCAL plan.
     */
    protected FetchPlan createNestedEntityExportPlan(MetaClass metaClass) {
        FetchPlan localFetchPlan = fetchPlanRepository.getFetchPlan(metaClass, FetchPlan.LOCAL);
        FetchPlanBuilder fetchPlanBuilder = fetchPlans.builder(metaClass.getJavaClass());
        ExportImportEntityContext exportContext = new ExportImportEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(exportContext);

        for (FetchPlanProperty fetchPlanProperty : localFetchPlan.getProperties()) {
            if (!exportContext.canExported(fetchPlanProperty.getName())) {
                continue;
            }
            fetchPlanBuilder.add(fetchPlanProperty.getName());
        }

        return fetchPlanBuilder.build();
    }

    protected EntityImportPlan createEntityImportPlan(MetaClass metaClass) {
        EntityImportPlanBuilder planBuilder = entityImportPlans.builder(metaClass.getJavaClass());
        ExportImportEntityContext importContext = new ExportImportEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(importContext);

        for (MetaProperty metaProperty : metaClass.getProperties()) {
            if (!metadataTools.isJpa(metaProperty) || !importContext.canImported(metaProperty.getName())) {
                continue;
            }

            switch (metaProperty.getType()) {
                case DATATYPE:
                case ENUM:
                    planBuilder.addLocalProperty(metaProperty.getName());
                    break;
                case EMBEDDED:
                case ASSOCIATION:
                case COMPOSITION:
                    Range.Cardinality cardinality = metaProperty.getRange().getCardinality();
                    if (cardinality == Range.Cardinality.MANY_TO_ONE) {
                        planBuilder.addManyToOneProperty(metaProperty.getName(), ReferenceImportBehaviour.IGNORE_MISSING);
                    } else if (cardinality == Range.Cardinality.ONE_TO_ONE) {
                        planBuilder.addOneToOneProperty(metaProperty.getName(), ReferenceImportBehaviour.IGNORE_MISSING);
                    } else if (cardinality == Range.Cardinality.ONE_TO_MANY) {
                        planBuilder.addOneToOneProperty(metaProperty.getName(), ReferenceImportBehaviour.IGNORE_MISSING);
                    } else if (cardinality == Range.Cardinality.NONE) {
                        planBuilder.addOneToOneProperty(metaProperty.getName(), ReferenceImportBehaviour.IGNORE_MISSING);
                    }
                    break;
                default:
                    throw new IllegalStateException("unknown property type");
            }
        }
        return planBuilder.build();
    }

    /**
     * Reloads entities with the export fetch plan using constrained DataManager semantics and serializes them with
     * denied-property filtering.
     */
    protected String exportEntitiesToJson(Collection<Object> entities, FetchPlan fetchPlan) {
        Collection<Object> reloadedEntities = reloadEntitiesForExport(entities, fetchPlan);
        return entitySerialization.toJson(reloadedEntities, null,
                EntitySerializationOption.COMPACT_REPEATED_ENTITIES,
                EntitySerializationOption.PRETTY_PRINT,
                EntitySerializationOption.DO_NOT_SERIALIZE_DENIED_PROPERTY);
    }

    protected byte[] exportEntitiesToZip(Collection<Object> entities, FetchPlan fetchPlan) {
        Collection<Object> reloadedEntities = reloadEntitiesForExport(entities, fetchPlan);
        String json = entitySerialization.toJson(reloadedEntities, null,
                EntitySerializationOption.COMPACT_REPEATED_ENTITIES,
                EntitySerializationOption.DO_NOT_SERIALIZE_DENIED_PROPERTY);
        byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream, StandardCharsets.UTF_8)) {
            zipOutputStream.putNextEntry(new ZipEntry("entities.json"));
            zipOutputStream.write(jsonBytes);
            zipOutputStream.closeEntry();
            zipOutputStream.finish();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error on creating zip archive during entities export", e);
        }
    }

    protected Collection<Object> reloadEntitiesForExport(Collection<Object> entities, FetchPlan fetchPlan) {
        List<Object> ids = new ArrayList<>(entities.size());
        for (Object entity : entities) {
            ids.add(EntityValues.getId(entity));
        }

        MetaClass metaClass = metadata.getClass(fetchPlan.getEntityClass());
        LoadContext.Query query = new LoadContext.Query("select e from " + metaClass.getName() + " e where e.id in :ids")
                .setParameter("ids", ids);
        LoadContext context = new LoadContext(metadata.getClass(fetchPlan.getEntityClass()))
                .setQuery(query)
                .setFetchPlan(fetchPlan);

        return dataManager.loadList(context);
    }

    protected boolean readPermitted(MetaClass metaClass) {
        UiEntityContext entityContext = new UiEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(entityContext);
        return entityContext.isViewPermitted();
    }

    protected void showWipeOutDialog() {
        Set<Object> entityList = entitiesTable.getSelected();
        if (!entityList.isEmpty()) {
            dialogs.createOptionDialog()
                    .withCaption(messages.getMessage("dialogs.Confirmation"))
                    .withMessage(messages.getMessage(EntityInspectorBrowser.class, "wipeout.dialog.confirmation"))
                    .withActions(
                            new DialogAction(DialogAction.Type.OK).withHandler(event -> {
                                dataManager.save(
                                        new SaveContext()
                                                .removing(entityList)
                                                .setHint(PersistenceHints.SOFT_DELETION, false)
                                );
                                entitiesDl.load();
                                entitiesTable.focus();
                            }),
                            new DialogAction(DialogAction.Type.CANCEL).withHandler(event -> {
                                entitiesTable.focus();
                            }))
                    .show();
        } else {
            notifications.create(Notifications.NotificationType.HUMANIZED)
                    .withDescription(messages.getMessage(EntityInspectorBrowser.class, "wipeout.dialog.empty")).show();
        }
    }

    protected void showRestoreDialog() {
        Set<Object> entityList = entitiesTable.getSelected();
        Object entity = entitiesDc.getItemOrNull();
        if (entity != null && entityList.size() > 0) {
            if (EntityValues.isSoftDeletionSupported(entity)) {
                dialogs.createOptionDialog()
                        .withCaption(messages.getMessage("dialogs.Confirmation"))
                        .withMessage(messages.getMessage(EntityInspectorBrowser.class, "restore.dialog.confirmation"))
                        .withActions(
                                new DialogAction(DialogAction.Type.OK).withHandler(event -> {
                                    int restored = entityRestore.restoreEntities(entityList);
                                    entitiesDl.load();
                                    entitiesTable.focus();
                                    fireSelectionEventOnTable(entityList);
                                    notifications.create(Notifications.NotificationType.TRAY)
                                            .withDescription(
                                                    messages.formatMessage(
                                                            EntityInspectorBrowser.class,
                                                            "restore.restored",
                                                            restored
                                                    )
                                            )
                                            .show();

                                }),
                                new DialogAction(DialogAction.Type.CANCEL).withHandler(event -> {
                                    entitiesTable.focus();
                                }))
                        .show();
            }
        } else {
            notifications.create(Notifications.NotificationType.HUMANIZED)
                    .withDescription(messages.getMessage(EntityInspectorBrowser.class, "restore.dialog.empty")).show();
        }
    }

    private void fireSelectionEventOnTable(Collection items) {
        entitiesTable.setSelected(Collections.EMPTY_LIST);
        if (entitiesDc.getItems().containsAll(items)) {
            entitiesTable.setSelected(items);
        }
    }

    public class ExportAction extends ItemTrackingAction {

        protected DownloadFormat format;
        protected MetaClass metaClass;
        protected Table table;

        public ExportAction(String id) {
            super(id);
        }

        public void setFormat(DownloadFormat format) {
            this.format = format;
        }

        public void setMetaClass(MetaClass metaClass) {
            this.metaClass = metaClass;
        }

        public void setTable(Table table) {
            this.table = table;
        }

        @Override
        public void actionPerform(Component component) {
            Collection<Object> selected = table.getSelected();
            if (selected.isEmpty()
                    && table.getItems() != null) {
                selected = table.getItems().getItems();
            }

            try {
                int saveExportedByteArrayDataThresholdBytes = uiProperties.getSaveExportedByteArrayDataThresholdBytes();
                String tempDir = coreProperties.getTempDir();
                FetchPlan exportPlan = createEntityExportPlan(selectedMeta);
                if (format == ZIP) {
                    byte[] data = exportEntitiesToZip(selected, exportPlan);
                    String resourceName = metaClass.getJavaClass().getSimpleName() + ".zip";
                    downloader.download(
                            new ByteArrayDataProvider(data, saveExportedByteArrayDataThresholdBytes, tempDir), resourceName, ZIP);
                } else if (format == JSON) {
                    byte[] data = exportEntitiesToJson(selected, exportPlan)
                            .getBytes(StandardCharsets.UTF_8);
                    String resourceName = metaClass.getJavaClass().getSimpleName() + ".json";
                    downloader.download(
                            new ByteArrayDataProvider(data, saveExportedByteArrayDataThresholdBytes, tempDir), resourceName, JSON);
                }
            } catch (Exception e) {
                ScreenContext screenContext = ComponentsHelper.getScreenContext(table);
                Notifications notifications = screenContext.getNotifications();
                notifications.create(Notifications.NotificationType.ERROR)
                        .withCaption(messages.getMessage(EntityInspectorBrowser.class, "exportFailed"))
                        .withDescription(e.getMessage())
                        .show();
                log.error("Entities export failed", e);
            }
        }

        @Nullable
        @Override
        public String getCaption() {
            return messages.getMessage("io.jmix.datatoolsui.screen.entityinspector/" + id);
        }
    }
}
