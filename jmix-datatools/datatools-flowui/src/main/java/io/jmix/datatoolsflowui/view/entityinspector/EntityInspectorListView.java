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
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.Session;
import io.jmix.data.PersistenceHints;
import io.jmix.datatools.EntityRestore;
import io.jmix.datatoolsflowui.action.ShowEntityInfoAction;
import io.jmix.datatoolsflowui.view.entityinspector.assistant.InspectorDataGridBuilder;
import io.jmix.datatoolsflowui.view.entityinspector.assistant.InspectorFetchPlanBuilder;
import io.jmix.flowui.Actions;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.accesscontext.FlowuiEntityContext;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.action.list.CreateAction;
import io.jmix.flowui.action.list.EditAction;
import io.jmix.flowui.action.list.ItemTrackingAction;
import io.jmix.flowui.action.list.RefreshAction;
import io.jmix.flowui.action.list.RemoveAction;
import io.jmix.flowui.action.view.LookupSelectAction;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.DataComponents;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.navigation.RouteSupport;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;

@SuppressWarnings({"rawtypes", "unchecked"})
@Route(value = "datatl/entityinspector", layout = DefaultMainViewParent.class)
@ViewController("datatl_entityInspectorListView")
@ViewDescriptor("entity-inspector-list-view.xml")
@LookupComponent("entitiesDataGrid")
@DialogMode(width = "50em", height = "37.5em")
public class EntityInspectorListView extends StandardListView<Object> {

    protected static final String BASE_SELECT_QUERY = "select e from %s e";
    protected static final String DELETED_ONLY_SELECT_QUERY = "select e from %s e where e.%s is not null";
    protected static final String RESTORE_ACTION_ID = "restore";
    protected static final String WIPE_OUT_ACTION_ID = "wipeOut";

    protected static final String QUERY_PARAM_ENTITY = "entity";
    protected static final String QUERY_PARAM_MODE = "mode";

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
    protected UiComponents uiComponents;
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

    protected DataGrid<Object> entitiesDataGrid;
    protected MetaClass selectedMeta;
    protected CollectionLoader entitiesDl;
    protected CollectionContainer entitiesDc;
    protected String entityName;
    protected boolean isDialogMode = true;

    @Subscribe
    public void onInit(InitEvent event) {
        showMode.setValue(ShowMode.NON_REMOVED);
        getViewData().setDataContext(dataComponents.createDataContext());
        FlowuiComponentUtils.setItemsMap(entitiesLookup, getEntitiesLookupFieldOptions());

        entitiesLookup.addValueChangeListener(e -> showEntities());
        showMode.addValueChangeListener(e -> showEntities());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Map<String, List<String>> parameters = event.getLocation().getQueryParameters().getParameters();

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

        entitiesDataGrid.addSelectionListener(this::entitiesDataGridSelectionListener);
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

        Action showEntityInfoAction = createShowEntityInfoAction(dataGrid);
        dataGrid.addAction(showEntityInfoAction);

        buttonsPanel.add(createButton, editButton, removeButton, refreshButton);

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
        LookupSelectAction lookupSelectAction = actions.create(LookupSelectAction.class);
        lookupSelectAction.setTarget(this);
        return lookupSelectAction;
    }

    protected ShowEntityInfoAction createShowEntityInfoAction(DataGrid<Object> dataGrid) {
        ShowEntityInfoAction showEntityInfoAction = actions.create(ShowEntityInfoAction.class);
        showEntityInfoAction.setTarget(dataGrid);
        showEntityInfoAction.addEnabledRule(() -> dataGrid.getSelectedItems().size() == 1);
        return showEntityInfoAction;
    }

    protected RefreshAction createRefreshAction(DataGrid<Object> dataGrid) {
        RefreshAction refreshAction = actions.create(RefreshAction.class);
        refreshAction.setTarget(dataGrid);
        return refreshAction;
    }

    protected RemoveAction createRemoveAction(DataGrid<Object> dataGrid) {
        RemoveAction removeAction = actions.create(RemoveAction.class);
        removeAction.setTarget(dataGrid);
        return removeAction;
    }

    protected CreateAction createCreateAction(DataGrid<Object> dataGrid) {
        CreateAction createAction = actions.create(CreateAction.class);
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
        EditAction editAction = actions.create(EditAction.class);
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
        ItemTrackingAction restoreAction = actions.create(ItemTrackingAction.class, RESTORE_ACTION_ID);

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
        ItemTrackingAction wipeOutAction = actions.create(ItemTrackingAction.class, WIPE_OUT_ACTION_ID);

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
        if (entityList.size() > 0) {
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
            String queryParamValue = valueChangeEvent.getValue().getName();

            routeSupport.setQueryParameter(
                    ui,
                    QUERY_PARAM_ENTITY,
                    queryParamValue
            );
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

    protected void showNotification(String message) {
        notifications.create(messages.getMessage(EntityInspectorListView.class,
                        message))
                .withType(Notifications.Type.DEFAULT)
                .withPosition(Notification.Position.BOTTOM_END)
                .show();
    }

    protected boolean readPermitted(MetaClass metaClass) {
        FlowuiEntityContext entityContext = new FlowuiEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(entityContext);
        return entityContext.isViewPermitted();
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

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    protected interface DialogConsumer<T, U> {
        void accept(T t, U u);
    }
}
