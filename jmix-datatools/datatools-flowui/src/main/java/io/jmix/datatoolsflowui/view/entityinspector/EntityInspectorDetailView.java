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

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Route;
import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.data.PersistenceHints;
import io.jmix.datatoolsflowui.action.EntityInspectorAddAction;
import io.jmix.datatoolsflowui.action.EntityInspectorCreateAction;
import io.jmix.datatoolsflowui.action.EntityInspectorEditAction;
import io.jmix.datatoolsflowui.view.entityinspector.assistant.InspectorDataGridBuilder;
import io.jmix.datatoolsflowui.view.entityinspector.assistant.InspectorFetchPlanBuilder;
import io.jmix.datatoolsflowui.view.entityinspector.assistant.InspectorFormLayoutBuilder;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.accesscontext.FlowuiEntityAttributeContext;
import io.jmix.flowui.accesscontext.FlowuiEntityContext;
import io.jmix.flowui.action.list.*;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.*;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import static io.jmix.core.metamodel.model.MetaProperty.Type.ASSOCIATION;
import static io.jmix.core.metamodel.model.MetaProperty.Type.COMPOSITION;

@Route(value = "datatl/entityinspector/:entityName/:entityId", layout = DefaultMainViewParent.class)
@ViewController("datatl_entityInspectorDetailView")
@ViewDescriptor("entity-inspector-detail-view.xml")
@DialogMode(width = "50em", resizable = true)
public class EntityInspectorDetailView extends StandardDetailView<Object> {

    protected static final String BASE_SELECT_QUERY = "select e from %s e where e.%s.id = '%s'";
    protected static final String SOFT_DELETABLE_SELECT_QUERY = "select e from %s e where e.%s.id = '%s' and e.%s is null";
    protected static final String SINGLE_SELECT_QUERY = "select e from %s e where e.id = '%s'";
    public static final String ROUTE_PARAM_NAME = "entityName";
    public static final String ROUTE_PARAM_ID = "entityId";
    public static final String BUTTONS_PANEL_STYLE_NAME = "buttons-panel";

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected DataComponents dataComponents;
    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected Actions actions;
    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected MessageBundle messageBundle;
    @Autowired
    protected UrlParamSerializer urlParamSerializer;

    protected Tabs tabs;
    @Nullable
    protected DataContext parentDataContext;
    protected String parentProperty;
    protected DataContext dataContext;
    protected CollectionContainer<Object> parentDataContainer;

    protected HashMap<Tab, Component> tabToContentMap = new HashMap();

    protected Boolean isNew = true;
    protected InstanceContainer container;
    protected String metadataClassName;
    protected String metadataId;
    protected MetaClass metaClass;

    @Subscribe
    public void onInit(InitEvent event) {
        dataContext = dataComponents.createDataContext();
        getViewData().setDataContext(dataContext);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.getRouteParameters().get(ROUTE_PARAM_NAME)
                .ifPresent(this::setMetadataClassName);
        super.beforeEnter(event);
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        createContent();
    }

    @Subscribe
    protected void onAfterSave(AfterSaveEvent event) {
        if (parentDataContainer != null) {
            parentDataContainer.replaceItem(getEditedEntity());
        }
    }

    @Override
    protected void setupEntityToEdit(String serializedEntityId) {
        if (Strings.isNullOrEmpty(metadataId)) {
            metadataId = serializedEntityId;
        }

        if (metadataId.equalsIgnoreCase("new")) {
            createNewItemByMetaClass();
        } else {
            MetaProperty keyProperty = metadataTools.getPrimaryKeyProperty(metaClass);
            if (keyProperty != null) {
                Class<?> idType = keyProperty.getJavaType();
                Object deserializedId = urlParamSerializer.deserialize(idType, metadataId);

                String queryString = String.format(SINGLE_SELECT_QUERY, metaClass.getName(), deserializedId);
                Object entity = dataManager.load(metaClass.getJavaClass())
                        .query(queryString)
                        .hint(PersistenceHints.SOFT_DELETION, false)
                        .one();

                setEntityToEdit(entity);
            }
        }
    }

    protected void createNewItemByMetaClass() {
        if (metaClass != null) {
            Object item = dataContext.create(metaClass.getJavaClass());
            setEntityToEdit(item);
        }
    }

    @Override
    public void setEntityToEdit(Object entity) {
        super.setEntityToEdit(entity);
        container = initMainContainer(entity);
        isNew = entityStates.isNew(entity);
    }

    protected InstanceContainer initMainContainer(Object entity) {
        InstanceContainer container = dataComponents.createInstanceContainer(entity.getClass());
        if (!entityStates.isNew(entity)) {
            InstanceLoader loader = dataComponents.createInstanceLoader();
            loader.setDataContext(dataContext);

            FetchPlan fetchPlan = InspectorFetchPlanBuilder.of(getApplicationContext(), entity.getClass())
                    .withCollections(true)
                    .withEmbedded(true)
                    .withSystemProperties(true)
                    .build();

            loader.setFetchPlan(fetchPlan);
            container.setFetchPlan(fetchPlan);

            Object id = EntityValues.getId(entity);
            if (id != null) {
                loader.setEntityId(id);
            }

            loader.setHint(PersistenceHints.SOFT_DELETION, false);
            loader.setContainer(container);
            loader.load();
        } else {
            container.setItem(entity);
        }
        return container;
    }

    @Override
    protected String getRouteParamName() {
        return ROUTE_PARAM_ID;
    }

    @Override
    protected InstanceContainer getEditedEntityContainer() {
        return container;
    }

    @Override
    public String getPageTitle() {
        return metaClass != null
                ? messageTools.getEntityCaption(metaClass)
                : messageBundle.getMessage("entityInspectorDetailView.defaultTitle");
    }

    protected void createContent() {
        InstanceContainer selectedContainer = getEditedEntityContainer();
        tabs = uiComponents.create(Tabs.class);

        tabs.addSelectedChangeListener(this::onSelectedTabChange);
        tabs.setVisible(false);
        tabs.setWidthFull();
        getContent().addComponentAsFirst(tabs);

        InspectorFormLayoutBuilder.from(getApplicationContext(), selectedContainer)
                .withDisabledProperties(parentProperty)
                .withOwnerComponent(getContent())
                .build();

        if (!isNew) {
            getEditedEntityLoader().load();
        }

        MetaClass metaClass = selectedContainer.getEntityMetaClass();
        Object item = selectedContainer.getItem();

        for (MetaProperty metaProperty : metaClass.getProperties()) {
            switch (metaProperty.getType()) {
                case COMPOSITION:
                case ASSOCIATION:
                    if (EntityFormLayoutUtils.isMany(metaProperty)) {
                        addDataGrid(selectedContainer, metaProperty);
                    }
                    break;
                case EMBEDDED:
                    Object propertyValue = EntityValues.getValue(item, metaProperty.getName());
                    if (propertyValue != null) {
                        propertyValue = dataContext.merge(propertyValue);
                    }
                    InstanceContainer embeddedContainer = dataComponents.createInstanceContainer(
                            metaProperty.getRange().asClass().getJavaClass(),
                            selectedContainer,
                            metaProperty.getName()
                    );
                    embeddedContainer.setItem(propertyValue);
                    FormLayout embeddedForm = InspectorFormLayoutBuilder.from(
                                    getApplicationContext(),
                                    embeddedContainer)
                            .build();
                    embeddedForm.setVisible(false);
                    Tab tab = uiComponents.create(Tab.class);
                    tab.setLabel(getPropertyTitle(selectedContainer.getEntityMetaClass(), metaProperty));
                    tabToContentMap.put(tab, embeddedForm);
                    tabs.add(tab);

                    addComponentBeforeDetailActions(embeddedForm);
                    break;
                default:
                    break;
            }
        }

        if (tabs.getComponentCount() != 0) {
            tabs.setVisible(true);
        }
    }

    protected void onSelectedTabChange(Tabs.SelectedChangeEvent selectedChangeEvent) {
        if (selectedChangeEvent.getPreviousTab() != null) {
            tabToContentMap.get(selectedChangeEvent.getPreviousTab()).setVisible(false);
        }
        tabToContentMap.get(selectedChangeEvent.getSelectedTab()).setVisible(true);
    }

    /**
     * Creates a dataGrid for the entities in ONE_TO_MANY or MANY_TO_MANY relation with the current one
     */
    protected void addDataGrid(InstanceContainer parent, MetaProperty childMeta) {
        MetaClass meta = childMeta.getRange().asClass();

        FlowuiEntityContext entityContext = new FlowuiEntityContext(meta);
        accessManager.applyRegisteredConstraints(entityContext);


        FlowuiEntityAttributeContext attributeContext =
                new FlowuiEntityAttributeContext(parent.getEntityMetaClass(), childMeta.getName());
        accessManager.applyRegisteredConstraints(attributeContext);

        //don't show empty dataGrid if the user don't have permissions on the attribute or the entity
        if (!attributeContext.canView() ||
                !entityContext.isViewPermitted()) {
            return;
        }

        Tab tab = uiComponents.create(Tab.class);
        CollectionContainer dataGridContainer = createDataGridContainer(parent, childMeta, meta);

        DataGrid<?> entitiesGrid = InspectorDataGridBuilder.from(
                        getApplicationContext(),
                        dataGridContainer)
                .withSystem(true)
                .build();

        HorizontalLayout buttonsPanel = createButtonsPanel(entitiesGrid, childMeta, dataGridContainer);

        VerticalLayout vbox = uiComponents.create(VerticalLayout.class);

        vbox.setWidthFull();
        vbox.setPadding(false);
        vbox.setVisible(false);
        vbox.add(buttonsPanel, entitiesGrid);

        tab.setLabel(getPropertyTitle(parent.getEntityMetaClass(), childMeta));
        tabToContentMap.put(tab, vbox);
        tabs.add(tab);

        addComponentBeforeDetailActions(vbox);
    }

    protected CollectionContainer createDataGridContainer(
            InstanceContainer parent,
            MetaProperty childMeta,
            MetaClass meta) {
        CollectionLoader loader = dataComponents.createCollectionLoader();
        CollectionContainer container = dataComponents.createCollectionContainer(
                meta.getJavaClass(),
                parent,
                childMeta.getName());

        loader.setContainer(container);
        loader.setDataContext(dataContext);
        loader.setLoadDelegate(loadContext -> {
            String queryString = metadataTools.isSoftDeletable(meta.getJavaClass())
                    ? SOFT_DELETABLE_SELECT_QUERY
                    : BASE_SELECT_QUERY;

            String query = String.format(queryString,
                    meta.getName(),
                    childMeta.getInverse().getName(),
                    EntityValues.getId(parent.getItem()),
                    metadataTools.findDeletedDateProperty(meta.getJavaClass()));

            Collection<?> loadedChild = dataManager.load(meta.getJavaClass())
                    .query(query)
                    .list();
            return new ArrayList<>(loadedChild);
        });

        loader.load();
        dataContext.setModified(parent.getItem(), false);
        return container;
    }

    protected String getPropertyTitle(MetaClass metaClass, MetaProperty metaProperty) {
        return messageTools.getPropertyCaption(metaClass, metaProperty.getName());
    }

    protected HorizontalLayout createButtonsPanel(DataGrid<?> dataGrid, MetaProperty metaProperty, CollectionContainer dataGridContainer) {
        HorizontalLayout propertyButtonsPanel = uiComponents.create(HorizontalLayout.class);
        propertyButtonsPanel.setClassName(BUTTONS_PANEL_STYLE_NAME);

        propertyButtonsPanel.add(createButton(dataGrid, metaProperty));
        if (metaProperty.getType() == ASSOCIATION) {
            propertyButtonsPanel.add(addButton(dataGrid, metaProperty));
        }
        propertyButtonsPanel.add(editButton(dataGrid, metaProperty, dataGridContainer));
        propertyButtonsPanel.add(removeButton(dataGrid, metaProperty));
        return propertyButtonsPanel;
    }

    protected JmixButton addButton(DataGrid<?> dataGrid, MetaProperty metaProperty) {
        JmixButton addButton = uiComponents.create(JmixButton.class);
        AddAction addAction = createAddAction(dataGrid, metaProperty);
        addButton.setAction(addAction);
        dataGrid.addAction(addAction);
        return addButton;
    }

    protected AddAction createAddAction(DataGrid<?> dataGrid, MetaProperty metaProperty) {
        EntityInspectorAddAction addAction = actions.create(EntityInspectorAddAction.class);
        addAction.setTarget(dataGrid);
        addAction.setViewClass(EntityInspectorListView.class);
        addAction.setEntityNameParameter(getMetaPropertyClass(metaProperty).getName());
        return addAction;
    }

    protected JmixButton createButton(DataGrid<?> dataGrid, MetaProperty metaProperty) {
        JmixButton createButton = uiComponents.create(JmixButton.class);
        CreateAction createAction = createCreateAction(dataGrid, metaProperty);
        createButton.setAction(createAction);
        dataGrid.addAction(createAction);
        return createButton;
    }

    protected CreateAction createCreateAction(DataGrid<?> dataGrid, MetaProperty metaProperty) {
        EntityInspectorCreateAction createAction = actions.create(EntityInspectorCreateAction.class);
        MetaProperty inverseProperty = metaProperty.getInverse();

        createAction.setOpenMode(OpenMode.DIALOG);
        createAction.setTarget(dataGrid);
        createAction.setViewClass(EntityInspectorDetailView.class);
        createAction.setParentDataContext(parentDataContext != null ? parentDataContext : dataContext);

        if (inverseProperty != null) {
            createAction.setParentProperty(inverseProperty.getName());
        }

        MetaClass metaPropertyClass = getMetaPropertyClass(metaProperty);
        createAction.setEntityMetaClass(metaPropertyClass.getName());
        createAction.setEntityId("new");

        createAction.setNewEntitySupplier(() -> {
            Object newItem = metadata.create(metaPropertyClass);
            if (inverseProperty != null) {
                EntityValues.setValue(
                        newItem,
                        inverseProperty.getName(),
                        getEditedEntityContainer().getItem()
                );
            }
            return newItem;
        });

        return createAction;
    }

    protected JmixButton editButton(DataGrid<?> dataGrid,
                                    MetaProperty metaProperty,
                                    CollectionContainer dataGridContainer) {
        JmixButton editButton = uiComponents.create(JmixButton.class);
        EditAction editAction = createEditAction(dataGrid, metaProperty, dataGridContainer);
        editButton.setAction(editAction);
        dataGrid.addAction(editAction);
        return editButton;
    }

    protected EditAction createEditAction(DataGrid<?> dataGrid,
                                          MetaProperty metaProperty,
                                          CollectionContainer dataGridContainer) {
        EntityInspectorEditAction editAction = actions.create(EntityInspectorEditAction.class);

        editAction.setTarget(dataGrid);
        editAction.setOpenMode(OpenMode.DIALOG);
        editAction.setViewClass(EntityInspectorDetailView.class);
        editAction.setParentDataContainer(dataGridContainer);
        editAction.addEnabledRule(() -> dataGrid.getSelectedItems().size() == 1);
        if (metaProperty.getType() == COMPOSITION) {
            editAction.setParentDataContext(dataContext);
        }

        MetaProperty inverseProperty = metaProperty.getInverse();
        if (inverseProperty != null) {
            editAction.setParentProperty(inverseProperty.getName());
        }

        dataGrid.addSelectionListener(event -> {
            Object selectedItem = dataGrid.getSingleSelectedItem();
            Object id = selectedItem != null ? EntityValues.getId(selectedItem) : null;
            if (id != null) {
                editAction.setEntityId(urlParamSerializer.serialize(id));
            }
        });

        editAction.setEntityMetaClass(getMetaPropertyClass(metaProperty).getName());
        return editAction;
    }

    protected JmixButton removeButton(DataGrid<?> dataGrid, MetaProperty metaProperty) {
        JmixButton removeButton = uiComponents.create(JmixButton.class);
        Action removeAction = createRemoveAction(dataGrid, metaProperty);
        removeButton.setAction(removeAction);
        dataGrid.addAction(removeAction);
        return removeButton;
    }

    /**
     * Creates either Remove or Exclude action depending on property type
     */
    protected SecuredListDataComponentAction createRemoveAction(DataGrid<?> dataGrid, MetaProperty metaProperty) {
        SecuredListDataComponentAction result;
        switch (metaProperty.getType()) {
            case COMPOSITION:
                result = actions.create(RemoveAction.class);
                result.setTarget(dataGrid);
                break;
            case ASSOCIATION:
                result = actions.create(ExcludeAction.class);
                result.setTarget(dataGrid);
                break;
            default:
                throw new IllegalArgumentException("property must contain an entity");
        }
        return result;
    }

    protected void addComponentBeforeDetailActions(Component component) {
        Component detailActions = getContent().getComponent("detailActions");
        getContent().addComponentAtIndex(getContent().indexOf(detailActions), component);
    }

    public void setMetadataClassName(String metadataClassName) {
        this.metadataClassName = metadataClassName;
        this.metaClass = metadata.getClass(metadataClassName);
    }

    public void setMetadataId(String metadataId) {
        this.metadataId = metadataId;
    }

    public void setParentDataContext(@Nullable DataContext parentDataContext) {
        this.parentDataContext = parentDataContext;
    }

    public void setParentProperty(String parentProperty) {
        this.parentProperty = parentProperty;
    }

    public void setParentDataContainer(CollectionContainer<Object> dataContainer) {
        this.parentDataContainer = dataContainer;
    }

    private MetaClass getMetaPropertyClass(MetaProperty metaProperty) {
        return metaProperty.getRange().asClass();
    }
}
