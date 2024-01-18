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

package io.jmix.datatoolsflowui.view.entityinspector;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.impl.importexport.EntityImportPlanJsonBuilder;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import io.jmix.core.metamodel.model.Session;
import io.jmix.data.PersistenceHints;
import io.jmix.datatools.EntityRestore;
import io.jmix.datatoolsflowui.action.ShowEntityInfoAction;
import io.jmix.datatoolsflowui.view.entityinspector.assistant.InspectorDataGridBuilder;
import io.jmix.datatoolsflowui.view.entityinspector.assistant.InspectorFetchPlanBuilder;
import io.jmix.flowui.*;
import io.jmix.flowui.accesscontext.UiEntityContext;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.action.list.*;
import io.jmix.flowui.action.view.LookupSelectAction;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.upload.FileUploadField;
import io.jmix.flowui.data.grid.ContainerDataGridItems;
import io.jmix.flowui.download.ByteArrayDownloadDataProvider;
import io.jmix.flowui.download.DownloadFormat;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.facet.urlqueryparameters.GenericFilterUrlQueryParametersBinder;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.DataComponents;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.navigation.RouteSupport;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import io.jmix.gridexportflowui.action.ExcelExportAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

import static com.google.common.base.Strings.nullToEmpty;
import static io.jmix.flowui.download.DownloadFormat.JSON;
import static io.jmix.flowui.download.DownloadFormat.ZIP;

@SuppressWarnings({"rawtypes", "unchecked"})
@Route(value = "datatl/entityinspector", layout = DefaultMainViewParent.class)
@ViewController("datatl_entityInspectorListView")
@ViewDescriptor("entity-inspector-list-view.xml")
@LookupComponent("entitiesDataGrid")
@DialogMode(width = "50em")
public class EntityInspectorListView extends StandardListView<Object> {

    private static final Logger log = LoggerFactory.getLogger(EntityInspectorListView.class);

    protected static final String BASE_SELECT_QUERY = "select e from %s e";
    protected static final String DELETED_ONLY_SELECT_QUERY = "select e from %s e where e.%s is not null";
    protected static final String RESTORE_ACTION_ID = "restore";
    protected static final String WIPE_OUT_ACTION_ID = "wipeOut";

    protected static final String QUERY_PARAM_ENTITY = "entity";
    protected static final String QUERY_PARAM_MODE = "mode";

    protected static final Set<String> EXCLUDED_META_CLASS_NAMES = Set.of(
            "dummyAppSettingsEntity"
    );

    @ViewComponent
    protected HorizontalLayout lookupBox;
    @ViewComponent
    protected JmixComboBox<MetaClass> entitiesLookup;
    @ViewComponent
    protected HorizontalLayout buttonsPanel;
    @ViewComponent
    protected Select<ShowMode> showMode;
    @ViewComponent
    protected JmixButton selectButton;

    @Autowired
    protected Messages messages;
    @Autowired
    protected MessageBundle messageBundle;
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
    protected FetchPlans fetchPlans;
    @Autowired
    protected FetchPlanRepository fetchPlanRepository;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected UiProperties uiProperties;
    @Autowired
    protected CoreProperties coreProperties;
    @Autowired
    protected EntityImportExport entityImportExport;
    @Autowired
    protected EntityImportPlanJsonBuilder importPlanJsonBuilder;
    @Autowired
    protected EntityImportPlans entityImportPlans;
    @Autowired
    protected Actions actions;
    @Autowired
    protected Dialogs dialogs;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected EntityRestore entityRestore;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected UrlParamSerializer urlParamSerializer;
    @Autowired
    protected RouteSupport routeSupport;
    @Autowired
    protected Downloader downloader;

    protected DataGrid<Object> entitiesDataGrid;
    protected GenericFilter entitiesGenericFilter;
    protected Registration queryParametersChangeRegistration;
    protected QueryParameters initialParameters;

    protected MetaClass selectedMeta;
    protected CollectionLoader entitiesDl;
    protected CollectionContainer entitiesDc;

    protected String entityName;
    protected boolean isDialogMode = true;

    @Subscribe
    public void onInit(InitEvent event) {
        showMode.setValue(ShowMode.NON_REMOVED);
        getViewData().setDataContext(dataComponents.createDataContext());
        ComponentUtils.setItemsMap(entitiesLookup, getEntitiesLookupFieldOptions());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        initialParameters = event.getLocation().getQueryParameters();
        Map<String, List<String>> parameters = initialParameters.getParameters();

        if (parameters.containsKey(QUERY_PARAM_ENTITY)) {
            parameters.get(QUERY_PARAM_ENTITY).stream()
                    .findAny()
                    .ifPresent(this::setEntityName);
        }

        if (parameters.containsKey(QUERY_PARAM_MODE)) {
            parameters.get(QUERY_PARAM_MODE).stream()
                    .findAny()
                    .ifPresent(this::setShowMode);
        }

        isDialogMode = false;

        super.beforeEnter(event);
    }

    //to handle the usage of entityName public setter
    @Subscribe
    public void beforeShow(BeforeShowEvent event) {
        if (entityName != null) {
            Session session = metadata.getSession();
            selectedMeta = session.getClass(entityName);

            entitiesLookup.setValue(selectedMeta);
            createEntitiesDataGrid(selectedMeta);
            entitiesDl.load();
        }

        lookupBox.setVisible(!isDialogMode);
    }

    @Subscribe
    public void onReady(ReadyEvent event) {
        if (!isDialogMode) {
            entitiesLookup.addValueChangeListener(this::entityChangeListener);
            showMode.addValueChangeListener(this::showModeChangeListener);
        }

        entitiesLookup.addValueChangeListener(e -> showEntities());
        showMode.addValueChangeListener(e -> showEntities());
    }

    @Override
    public io.jmix.flowui.component.LookupComponent<Object> getLookupComponent() {
        return entitiesDataGrid;
    }

    @Override
    public Optional<io.jmix.flowui.component.LookupComponent<Object>> findLookupComponent() {
        return Optional.ofNullable(entitiesDataGrid);
    }

    protected Map<MetaClass, String> getEntitiesLookupFieldOptions() {
        Map<MetaClass, String> options = new TreeMap<>(Comparator.comparing(MetaClass::getName));

        for (MetaClass metaClass : metadataTools.getAllJpaEntityMetaClasses()) {
            if (readPermitted(metaClass)) {
                options.put(metaClass,
                        messageTools.getEntityCaption(metaClass) + " (" + metaClass.getName() + ")");
            }
        }

        return options;
    }

    protected void createEntitiesDataGrid(MetaClass meta) {
        if (entitiesDataGrid != null) {
            getContent().remove(entitiesDataGrid);
        }

        entitiesDataGrid = InspectorDataGridBuilder.from(getApplicationContext(), createContainer(meta))
                .withSystem(true)
                .build();

        updateButtonsPanel(entitiesDataGrid);
        updateSelectAction(entitiesDataGrid);

        Component lookupActionsLayout = getContent().getComponent(LOOKUP_ACTIONS_LAYOUT_DEFAULT_ID);

        getContent().addComponentAtIndex(
                getContent().indexOf(lookupActionsLayout),
                entitiesDataGrid);

        if (entitiesGenericFilter != null) {
            getContent().remove(entitiesGenericFilter);
        }

        entitiesGenericFilter = createGenericFilter();

        getContent().addComponentAtIndex(
                getContent().indexOf(buttonsPanel),
                entitiesGenericFilter
        );

        entitiesDataGrid.addSelectionListener(this::entitiesDataGridSelectionListener);
    }

    protected GenericFilter createGenericFilter() {
        GenericFilter genericFilter = uiComponents.create(GenericFilter.class);
        genericFilter.setId("genericFilter");
        genericFilter.setDataLoader(entitiesDl);

        initQueryParametersBinder(genericFilter);

        return genericFilter;
    }

    protected void initQueryParametersBinder(GenericFilter genericFilter) {
        GenericFilterUrlQueryParametersBinder genericFilterBinder = createQueryParametersBinder(genericFilter);

        if (queryParametersChangeRegistration != null) {
            queryParametersChangeRegistration.remove();
            queryParametersChangeRegistration = null;
        }

        queryParametersChangeRegistration =
                ViewControllerUtils.addQueryParametersChangeListener(this, event -> {
                    if (UiComponentUtils.isComponentAttachedToDialog(this)) {
                        return;
                    }

                    genericFilterBinder.updateState(event.getQueryParameters());
                });

        // Lazy update of query parameters for backward navigation
        if (initialParameters != null) {
            genericFilterBinder.updateState(initialParameters);
            initialParameters = null;
        }
    }

    private GenericFilterUrlQueryParametersBinder createQueryParametersBinder(GenericFilter genericFilter) {
        GenericFilterUrlQueryParametersBinder genericFilterBinder =
                new GenericFilterUrlQueryParametersBinder(genericFilter, urlParamSerializer, getApplicationContext());

        genericFilterBinder.addUrlQueryParametersChangeListener(event -> {
            if (UiComponentUtils.isComponentAttachedToDialog(this)) {
                return;
            }

            getUI().ifPresent(ui -> {
                // Required to collect query parameters from fields, since the current
                // listener is called immediately after changing any query parameters by the
                // field (see entityChangeListener, showModeChangeListener).
                // At this point, the previous state is not pushed to the UI, and fields query parameters is not
                // applied to the URL
                QueryParameters queryParameters = routeSupport.mergeQueryParameters(
                        collectFieldsQueryParameters(),
                        event.getQueryParameters()
                );

                routeSupport.setQueryParameters(ui, queryParameters);
            });
        });

        return genericFilterBinder;
    }

    protected void showEntities() {
        selectedMeta = entitiesLookup.getValue();
        if (selectedMeta != null) {
            createEntitiesDataGrid(selectedMeta);
            entitiesDl.load();
        }
    }

    protected void entitiesDataGridSelectionListener(SelectionEvent<Grid<Object>, Object> event) {
        boolean removeEnabled = true;
        boolean restoreEnabled = true;

        if (!event.getAllSelectedItems().isEmpty()) {
            for (Object o : event.getAllSelectedItems()) {
                if (o instanceof Entity) {
                    if (EntityValues.isSoftDeleted(o)) {
                        removeEnabled = false;
                    } else {
                        restoreEnabled = false;
                    }

                    if (!removeEnabled && !restoreEnabled) {
                        break;
                    }
                }
            }
        }

        Action removeAction = entitiesDataGrid.getAction(RemoveAction.ID);
        if (removeAction != null) {
            removeAction.setEnabled(removeEnabled);
        }

        Action restoreAction = entitiesDataGrid.getAction(RESTORE_ACTION_ID);
        if (restoreAction != null) {
            restoreAction.setEnabled(restoreEnabled);
        }
    }

    protected CollectionContainer createContainer(MetaClass meta) {
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

    protected void updateButtonsPanel(DataGrid<Object> dataGrid) {
        buttonsPanel.removeAll();

        buttonsPanel.setVisible(lookupBox.isVisible());
        JmixButton createButton = uiComponents.create(JmixButton.class);
        CreateAction createAction = createCreateAction(dataGrid);
        dataGrid.addAction(createAction);
        createButton.setAction(createAction);

        JmixButton editButton = uiComponents.create(JmixButton.class);
        EditAction editAction = createEditAction(dataGrid);
        dataGrid.addAction(editAction);
        editButton.setAction(editAction);

        JmixButton removeButton = uiComponents.create(JmixButton.class);
        RemoveAction removeAction = createRemoveAction(dataGrid);
        if (metadataTools.isSoftDeletable(selectedMeta.getJavaClass()) &&
                ShowMode.ALL.equals(showMode.getValue())) {
            removeAction.setAfterActionPerformedHandler(removedItems -> {
                dataGrid.deselectAll();
                entitiesDl.load();
            });
        }
        dataGrid.addAction(removeAction);
        removeButton.setAction(removeAction);

        JmixButton refreshButton = uiComponents.create(JmixButton.class);
        RefreshAction refreshAction = createRefreshAction(dataGrid);
        dataGrid.addAction(refreshAction);
        refreshButton.setAction(refreshAction);

        JmixButton excelExportButton = uiComponents.create(JmixButton.class);
        ExcelExportAction excelExportAction = createExcelExportAction(dataGrid);
        excelExportButton.setAction(excelExportAction);

        DropdownButton exportDropdownButton = uiComponents.create(DropdownButton.class);
        exportDropdownButton.setText(messages.getMessage(EntityInspectorListView.class, "export"));
        exportDropdownButton.setIcon(VaadinIcon.DOWNLOAD.create());

        ExportAction exportJsonAction = new ExportAction("exportJSON");
        exportJsonAction.setFormat(JSON);
        exportJsonAction.setDataGrid(dataGrid);
        exportJsonAction.setMetaClass(selectedMeta);
        exportJsonAction.setIcon(VaadinIcon.FILE_CODE.create());
        exportDropdownButton.addItem("exportJson", exportJsonAction);

        ExportAction exportZipAction = new ExportAction("exportZIP");
        exportZipAction.setFormat(ZIP);
        exportZipAction.setDataGrid(dataGrid);
        exportZipAction.setMetaClass(selectedMeta);
        exportZipAction.setIcon(VaadinIcon.FILE_ZIP.create());
        exportDropdownButton.addItem("exportZip", exportZipAction);

        FileUploadField importUpload = uiComponents.create(FileUploadField.class);
        importUpload.setAcceptedFileTypes(".json", ".zip");
        importUpload.setUploadIcon(VaadinIcon.UPLOAD.create());
        importUpload.setUploadText(messages.getMessage(EntityInspectorListView.class, "import"));

        importUpload.addFileUploadSucceededListener(event -> {
            byte[] fileBytes = importUpload.getValue();
            String fileName = event.getFileName();

            try {
                Collection<Object> importedEntities;

                if (JSON.getFileExt().equals(Files.getFileExtension(fileName))) {
                    String content = new String(Objects.requireNonNull(fileBytes), StandardCharsets.UTF_8);
                    importedEntities = entityImportExport.importEntitiesFromJson(content,
                            createEntityImportPlan(content, selectedMeta));
                } else {
                    importedEntities = entityImportExport.importEntitiesFromZIP(Objects.requireNonNull(fileBytes),
                            createEntityImportPlan(selectedMeta));
                }

                String importSuccessfulMessage = messages.formatMessage(EntityInspectorListView.class,
                        "importSuccessful", importedEntities.size());

                notifications.create(importSuccessfulMessage)
                        .withType(Notifications.Type.SUCCESS)
                        .show();

            } catch (Exception e) {
                String importFailedHeader = messages.getMessage(EntityInspectorListView.class, "importFailedHeader");
                String importFailedMessage = messages.formatMessage(EntityInspectorListView.class, "importFailedMessage",
                        fileName, nullToEmpty(e.getMessage()));

                notifications.create(importFailedHeader, importFailedMessage)
                        .withType(Notifications.Type.ERROR)
                        .show();

                log.error("Entities import error", e);
            }

            entitiesDl.load();
        });

        Action showEntityInfoAction = createShowEntityInfoAction(dataGrid);
        dataGrid.addAction(showEntityInfoAction);

        buttonsPanel.add(createButton, editButton, removeButton, refreshButton, excelExportButton,
                exportDropdownButton, importUpload);

        if (metadataTools.isSoftDeletable(selectedMeta.getJavaClass())) {
            JmixButton restoreButton = createRestoreButton(dataGrid);
            JmixButton wipeOutButton = createWipeOutButton(dataGrid);
            buttonsPanel.add(restoreButton, wipeOutButton);
        }

    }

    protected void updateSelectAction(DataGrid<Object> dataGrid) {
        LookupSelectAction selectAction = createLookupSelectAction(dataGrid);
        selectButton.setAction(selectAction);
    }

    protected LookupSelectAction createLookupSelectAction(DataGrid<Object> dataGrid) {
        LookupSelectAction lookupSelectAction = actions.create(LookupSelectAction.ID);
        lookupSelectAction.setTarget(this);
        return lookupSelectAction;
    }

    protected ExcelExportAction createExcelExportAction(DataGrid<Object> dataGrid) {
        ExcelExportAction excelExportAction = actions.create(ExcelExportAction.ID);
        excelExportAction.setTarget(dataGrid);

        return excelExportAction;
    }

    protected ShowEntityInfoAction createShowEntityInfoAction(DataGrid<Object> dataGrid) {
        ShowEntityInfoAction showEntityInfoAction = actions.create(ShowEntityInfoAction.ID);
        showEntityInfoAction.setTarget(dataGrid);
        showEntityInfoAction.addEnabledRule(() -> dataGrid.getSelectedItems().size() == 1);
        return showEntityInfoAction;
    }

    protected RefreshAction createRefreshAction(DataGrid<Object> dataGrid) {
        RefreshAction refreshAction = actions.create(RefreshAction.ID);
        refreshAction.setTarget(dataGrid);
        return refreshAction;
    }

    protected RemoveAction createRemoveAction(DataGrid<Object> dataGrid) {
        RemoveAction removeAction = actions.create(RemoveAction.ID);
        removeAction.setTarget(dataGrid);
        return removeAction;
    }

    protected CreateAction createCreateAction(DataGrid<Object> dataGrid) {
        CreateAction createAction = actions.create(CreateAction.ID);
        createAction.setOpenMode(OpenMode.NAVIGATION);
        createAction.setTarget(dataGrid);
        createAction.setRouteParametersProvider(() -> {
            ImmutableMap<String, String> routeParameterMap = ImmutableMap.of(
                    EntityInspectorDetailView.ROUTE_PARAM_NAME, selectedMeta.getName(),
                    EntityInspectorDetailView.ROUTE_PARAM_ID, "new"
            );
            return new RouteParameters(routeParameterMap);
        });
        createAction.setViewClass(EntityInspectorDetailView.class);
        createAction.setNewEntitySupplier(() -> metadata.create(selectedMeta));
        if (Modifier.isAbstract(selectedMeta.getJavaClass().getModifiers())) {
            createAction.setEnabled(false);
        }

        return createAction;
    }

    protected EditAction createEditAction(DataGrid<Object> dataGrid) {
        EditAction editAction = actions.create(EditAction.ID);
        editAction.setOpenMode(OpenMode.NAVIGATION);
        editAction.setTarget(dataGrid);

        editAction.setRouteParametersProvider(() -> {
            Object selectedItem = dataGrid.getSingleSelectedItem();
            Object id = EntityValues.getId(selectedItem);
            if (selectedItem != null && id != null) {
                String serializedEntityName = urlParamSerializer.serialize(selectedMeta.getName());
                String serializedId = urlParamSerializer.serialize(id);
                ImmutableMap<String, String> routeParameterMap = ImmutableMap.of(
                        EntityInspectorDetailView.ROUTE_PARAM_NAME, serializedEntityName,
                        EntityInspectorDetailView.ROUTE_PARAM_ID, serializedId
                );
                return new RouteParameters(routeParameterMap);
            }
            return null;
        });

        editAction.setViewClass(EntityInspectorDetailView.class);
        editAction.addEnabledRule(() -> dataGrid.getSelectedItems().size() == 1);

        return editAction;
    }

    protected JmixButton createRestoreButton(DataGrid<Object> dataGrid) {
        JmixButton restoreButton = uiComponents.create(JmixButton.class);
        ItemTrackingAction restoreAction = actions.create(ItemTrackingAction.ID, RESTORE_ACTION_ID);

        restoreAction.setText(messages.getMessage(EntityInspectorListView.class, "restore"));
        restoreAction.addActionPerformedListener(event -> showRestoreDialog());
        restoreAction.setTarget(dataGrid);
        restoreAction.setIcon(new Icon("lumo", "undo"));

        restoreButton.setAction(restoreAction);
        dataGrid.addAction(restoreAction);
        return restoreButton;
    }

    protected JmixButton createWipeOutButton(DataGrid<?> dataGrid) {
        JmixButton wipeOutButton = uiComponents.create(JmixButton.class);
        ItemTrackingAction wipeOutAction = actions.create(ItemTrackingAction.ID, WIPE_OUT_ACTION_ID);

        wipeOutAction.setText(messages.getMessage(EntityInspectorListView.class, "wipeOut"));
        wipeOutAction.addActionPerformedListener(event -> showWipeOutDialog());
        wipeOutAction.setTarget(dataGrid);
        wipeOutAction.setVariant(ActionVariant.DANGER);
        wipeOutAction.setIcon(VaadinIcon.ERASER.create());

        wipeOutButton.setAction(wipeOutAction);
        dataGrid.addAction(wipeOutAction);
        return wipeOutButton;
    }

    protected void showWipeOutDialog() {
        Set<Object> entityList = entitiesDataGrid.getSelectedItems();
        if (!entityList.isEmpty()) {
            showOptionDialog(
                    messages.getMessage("dialogs.Confirmation"),
                    messages.getMessage(EntityInspectorListView.class,
                            "wipeout.dialog.confirmation"),
                    this::wipeOutOkHandler,
                    this::standardCancelHandler,
                    entityList
            );
        } else {
            showNotification("wipeout.dialog.empty");
        }
    }

    protected void showRestoreDialog() {
        Set<Object> entityList = entitiesDataGrid.getSelectedItems();
        if (!entityList.isEmpty()) {
            if (metadataTools.isSoftDeletable(selectedMeta.getJavaClass())) {
                showOptionDialog(
                        messages.getMessage("dialogs.Confirmation"),
                        messages.getMessage(EntityInspectorListView.class,
                                "restore.dialog.confirmation"),
                        this::restoreOkHandler,
                        this::standardCancelHandler,
                        entityList
                );
            }
        } else {
            showNotification("restore.dialog.empty");
        }
    }

    protected void showOptionDialog(String header,
                                    String text,
                                    DialogConsumer<ActionPerformedEvent, Set<Object>> okEvent,
                                    Consumer<ActionPerformedEvent> cancelEvent,
                                    Set<Object> entityList) {
        dialogs.createOptionDialog()
                .withHeader(header)
                .withText(text)
                .withActions(
                        new DialogAction(DialogAction.Type.OK)
                                .withVariant(ActionVariant.PRIMARY)
                                .withHandler(event -> okEvent.accept(event, entityList)),
                        new DialogAction(DialogAction.Type.CANCEL)
                                .withHandler(cancelEvent))
                .open();
    }

    protected void wipeOutOkHandler(ActionPerformedEvent actionPerformedEvent, Set<Object> entityList) {
        dataManager.save(
                new SaveContext()
                        .removing(entityList)
                        .setHint(PersistenceHints.SOFT_DELETION, false)
        );
        entitiesDl.load();
        entitiesDataGrid.focus();
    }

    protected void restoreOkHandler(ActionPerformedEvent actionPerformedEvent, Set<Object> entityList) {
        int restored = entityRestore.restoreEntities(entityList);
        entitiesDataGrid.deselectAll();
        entitiesDl.load();
        showNotification(messages.formatMessage(
                EntityInspectorListView.class,
                "restore.restored",
                restored
        ));
    }

    protected void standardCancelHandler(ActionPerformedEvent actionPerformedEvent) {
        entitiesDataGrid.focus();
    }

    protected void entityChangeListener(
            AbstractField.ComponentValueChangeEvent<ComboBox<MetaClass>, MetaClass> valueChangeEvent) {
        getUI().ifPresent(ui -> {
            MetaClass metaClass = valueChangeEvent.getValue();

            if (metaClass == null) {
                getContent().remove(entitiesDataGrid);
                getContent().remove(entitiesGenericFilter);
                buttonsPanel.removeAll();

                //to remove the current entityName param and restore showMode param
                routeSupport.fetchCurrentLocation(ui, location -> {
                    Map<String, List<String>> parametersMap =
                            new HashMap<>(location.getQueryParameters().getParameters());
                    parametersMap.remove(QUERY_PARAM_ENTITY);
                    parametersMap.remove(GenericFilterUrlQueryParametersBinder.DEFAULT_CONFIGURATION_PARAM);
                    parametersMap.remove(GenericFilterUrlQueryParametersBinder.DEFAULT_CONDITION_PARAM);
                    routeSupport.setQueryParameters(ui, new QueryParameters(parametersMap));
                });
            } else {
                routeSupport.setQueryParameter(ui, QUERY_PARAM_ENTITY, metaClass.getName());
            }
        });
    }

    protected void showModeChangeListener(
            AbstractField.ComponentValueChangeEvent<Select<ShowMode>, ShowMode> valueChangeEvent) {
        getUI().ifPresent(ui -> {
            String queryParamValue = valueChangeEvent.getValue().getId();

            routeSupport.setQueryParameter(
                    ui,
                    QUERY_PARAM_MODE,
                    queryParamValue
            );
        });
    }

    protected QueryParameters collectFieldsQueryParameters() {
        Map<String, List<String>> parametersMap = new HashMap<>();

        MetaClass value = entitiesLookup.getValue();
        if (value != null) {
            parametersMap.put(QUERY_PARAM_ENTITY, Collections.singletonList(value.getName()));
        }

        parametersMap.put(QUERY_PARAM_MODE, Collections.singletonList(showMode.getValue().getId()));

        return new QueryParameters(parametersMap);
    }

    protected void showNotification(String message) {
        notifications.create(messages.getMessage(EntityInspectorListView.class,
                        message))
                .withType(Notifications.Type.DEFAULT)
                .withPosition(Notification.Position.BOTTOM_END)
                .show();
    }

    protected boolean readPermitted(MetaClass metaClass) {
        UiEntityContext entityContext = new UiEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(entityContext);
        return entityContext.isViewPermitted()
                && !EXCLUDED_META_CLASS_NAMES.contains(metaClass.getName());
    }

    protected void setShowMode(String showModeParameter) {
        ShowMode showModeValue = ShowMode.fromId(showModeParameter);

        if (showModeValue != null) {
            showMode.setValue(showModeValue);
        } else {
            showMode.setValue(ShowMode.NON_REMOVED);

            String title = messageBundle.getMessage("showMode.invalidQueryParameterTitle");
            String message = messageBundle.getMessage("showMode.invalidQueryParameterMessage");

            notifications.create(title, message)
                    .withType(Notifications.Type.WARNING)
                    .show();
        }
    }

    protected FetchPlan createEntityExportPlan(MetaClass metaClass) {
        FetchPlanBuilder fetchPlanBuilder = fetchPlans.builder(metaClass.getJavaClass());

        for (MetaProperty metaProperty : metaClass.getProperties()) {
            switch (metaProperty.getType()) {
                case DATATYPE:
                case ENUM:
                    fetchPlanBuilder.add(metaProperty.getName());
                    break;
                case ASSOCIATION:
                case COMPOSITION:
                case EMBEDDED:
                    FetchPlan local = fetchPlanRepository.getFetchPlan(metaProperty.getRange().asClass(),
                            FetchPlan.LOCAL);
                    fetchPlanBuilder.add(metaProperty.getName(), local.getName());
                    break;
                default:
                    throw new IllegalStateException("unknown property type");
            }
        }

        return fetchPlanBuilder.build();
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

                    if (property != null) {
                        property.setReferenceImportBehaviour(ReferenceImportBehaviour.IGNORE_MISSING);
                    }
                    break;
                default:
            }
        }

        return entityImportPlan;
    }

    protected EntityImportPlan createEntityImportPlan(MetaClass metaClass) {
        EntityImportPlanBuilder planBuilder = entityImportPlans.builder(metaClass.getJavaClass());

        for (MetaProperty metaProperty : metaClass.getProperties()) {
            if (!metadataTools.isJpa(metaProperty)) {
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

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    protected class ExportAction extends ItemTrackingAction {

        protected DownloadFormat format;
        protected MetaClass metaClass;
        protected DataGrid<Object> dataGrid;

        public ExportAction(String id) {
            super(id);
        }

        @Override
        protected void initAction() {
            super.initAction();

            text = messages.getMessage(EntityInspectorListView.class, id);
        }

        public void setFormat(DownloadFormat format) {
            this.format = format;
        }

        public void setMetaClass(MetaClass metaClass) {
            this.metaClass = metaClass;
        }

        public void setDataGrid(DataGrid<Object> dataGrid) {
            this.dataGrid = dataGrid;
        }

        @Override
        public void execute() {
            Collection<Object> selected = dataGrid.getSelectedItems();
            if (selected.isEmpty()
                    && dataGrid.getItems() != null
                    && dataGrid.getItems() instanceof ContainerDataGridItems) {
                selected = ((ContainerDataGridItems<Object>) dataGrid.getItems()).getContainer().getItems();
            }

            int saveExportedByteArrayDataThresholdBytes = uiProperties.getSaveExportedByteArrayDataThresholdBytes();
            String tempDir = coreProperties.getTempDir();

            try {
                if (format == ZIP) {
                    byte[] data = entityImportExport.exportEntitiesToZIP(selected, createEntityExportPlan(selectedMeta));
                    String resourceName = metaClass.getJavaClass().getSimpleName() + ".zip";
                    ByteArrayDownloadDataProvider dataProvider =
                            new ByteArrayDownloadDataProvider(data, saveExportedByteArrayDataThresholdBytes, tempDir);

                    downloader.download(dataProvider, resourceName, ZIP);

                } else if (format == JSON) {
                    byte[] data = entityImportExport.exportEntitiesToJSON(selected, createEntityExportPlan(selectedMeta))
                            .getBytes(StandardCharsets.UTF_8);
                    String resourceName = metaClass.getJavaClass().getSimpleName() + ".json";
                    ByteArrayDownloadDataProvider dataProvider =
                            new ByteArrayDownloadDataProvider(data, saveExportedByteArrayDataThresholdBytes, tempDir);

                    downloader.download(dataProvider, resourceName, JSON);
                }
            } catch (Exception e) {
                notifications.create(messages.getMessage(EntityInspectorListView.class, "exportFailed"), e.getMessage())
                        .withType(Notifications.Type.ERROR)
                        .show();

                log.error("Entities export failed", e);
            }
        }
    }

    protected interface DialogConsumer<T, U> {
        void accept(T t, U u);
    }
}
