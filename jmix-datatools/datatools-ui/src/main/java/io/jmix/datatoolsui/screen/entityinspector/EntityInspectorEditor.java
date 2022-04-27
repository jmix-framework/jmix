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

import io.jmix.core.AccessManager;
import io.jmix.core.EntityStates;
import io.jmix.core.MessageTools;
import io.jmix.core.Metadata;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.data.PersistenceHints;
import io.jmix.datatoolsui.screen.entityinspector.assistant.InspectorFetchPlanBuilder;
import io.jmix.datatoolsui.screen.entityinspector.assistant.InspectorFormBuilder;
import io.jmix.datatoolsui.screen.entityinspector.assistant.InspectorTableBuilder;
import io.jmix.ui.Actions;
import io.jmix.ui.UiComponentProperties;
import io.jmix.ui.UiComponents;
import io.jmix.ui.accesscontext.UiEntityAttributeContext;
import io.jmix.ui.accesscontext.UiEntityContext;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.list.*;
import io.jmix.ui.component.*;
import io.jmix.ui.model.*;
import io.jmix.ui.model.impl.NoopDataContext;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static io.jmix.core.metamodel.model.MetaProperty.Type.ASSOCIATION;
import static io.jmix.datatoolsui.screen.entityinspector.EntityFormUtils.isMany;

@SuppressWarnings({"rawtypes", "unchecked"})
@UiController("entityInspector.edit")
@UiDescriptor("entity-inspector-edit.xml")
public class EntityInspectorEditor extends StandardEditor {

    public static final String PARENT_CONTEXT_PARAM = "parentContext";
    public static final String PARENT_PROPERTY_PARAM = "parentProperty";

    public static final int CAPTION_MAX_LENGTH = 100;
    public static final int MAX_TEXT_LENGTH = 50;

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
    protected UiComponentProperties componentProperties;
    @Autowired
    protected Actions actions;

    @Autowired
    protected EntityStates entityStates;

    @Autowired
    protected BoxLayout contentPane;

    @Autowired
    protected TabSheet tablesTabSheet;

    private DataContext parentDataContext;
    private DataContext dataContext;

    protected Boolean isNew = true;
    protected String parentProperty;
    protected InstanceContainer container;

    @Subscribe
    protected void onInit(InitEvent initEvent) {
        if (initEvent.getOptions() instanceof MapScreenOptions) {
            MapScreenOptions screenOptions = (MapScreenOptions) initEvent.getOptions();
            Map<String, Object> params = screenOptions.getParams();
            if (params.get(PARENT_CONTEXT_PARAM) != null) {
                parentDataContext = (DataContext) params.get(PARENT_CONTEXT_PARAM);
                dataContext = new NoopDataContext(getApplicationContext());
            } else {
                dataContext = dataComponents.createDataContext();
            }
            parentProperty = (String) params.get(PARENT_PROPERTY_PARAM);
            createNewItemByMetaClass(params);
        } else {
            dataContext = dataComponents.createDataContext();
        }
        getScreenData().setDataContext(dataContext);
    }

    @Subscribe
    protected void beforeShow(BeforeShowEvent event) {
        createForm(getEditedEntityContainer());
        setWindowCaption();
    }

    @Subscribe
    protected void afterCommit(AfterCommitChangesEvent event) {
        if (parentDataContext != null) {
            parentDataContext.merge(getEditedEntity());
        }
    }

    @Override
    public void setEntityToEdit(Object entity) {
        super.setEntityToEdit(entity);
        container = initMainContainer(entity);
        isNew = entityStates.isNew(entity);
    }

    @Override
    protected InstanceContainer getEditedEntityContainer() {
        return container;
    }

    private InstanceContainer initMainContainer(Object entity) {
        InstanceContainer container = dataComponents.createInstanceContainer(entity.getClass());
        if (!entityStates.isNew(entity)) {
            InstanceLoader loader = dataComponents.createInstanceLoader();
            loader.setDataContext(dataContext);
            loader.setFetchPlan(InspectorFetchPlanBuilder.of(getApplicationContext(), entity.getClass())
                    .withCollections(true)
                    .withEmbedded(true)
                    .withSystemProperties(true)
                    .build());
            loader.setEntityId(EntityValues.getId(entity));
            loader.setHint(PersistenceHints.SOFT_DELETION, false);
            loader.setContainer(container);
            loader.load();
        } else {
            container.setItem(entity);
        }
        return container;
    }

    private void createForm(InstanceContainer container) {
        Form form = InspectorFormBuilder.from(getApplicationContext(), container)
                .withDisabledProperties(parentProperty)
                .withOwnerComponent(contentPane)
                .build();

        MetaClass metaClass = container.getEntityMetaClass();
        Object item = getEditedEntity();

        contentPane.add(form);

        for (MetaProperty metaProperty : metaClass.getProperties()) {
            switch (metaProperty.getType()) {
                case COMPOSITION:
                case ASSOCIATION:
                    if (isMany(metaProperty)) {
                        addTable(container, metaProperty);
                    }
                    break;
                case EMBEDDED:
                    Object propertyValue = EntityValues.getValue(item, metaProperty.getName());
                    if (propertyValue != null) {
                        propertyValue = dataContext.merge(propertyValue);
                    }
                    InstanceContainer embeddedContainer = dataComponents.createInstanceContainer(
                            metaProperty.getRange().asClass().getJavaClass(), container, metaProperty.getName());
                    embeddedContainer.setItem(propertyValue);
                    Form embeddedForm = InspectorFormBuilder.from(getApplicationContext(), embeddedContainer)
                            .withCaption(getPropertyCaption(metaClass, metaProperty))
                            .withOwnerComponent(contentPane)
                            .build();
                    contentPane.add(embeddedForm);
                    break;
                default:
                    break;
            }
        }
    }

    private void setWindowCaption() {
        MetaClass metaClass = metadata.getClass(getEditedEntity());
        getWindow().setCaption(messageTools.getEntityCaption(metaClass));
    }

    private void createNewItemByMetaClass(Map<String, Object> params) {
        if (params.get("metaClass") != null) {
            MetaClass meta = (MetaClass) params.get("metaClass");
            Object item = metadata.create(meta);
            setEntityToEdit(item);
        }
    }

    /**
     * Creates a table for the entities in ONE_TO_MANY or MANY_TO_MANY relation with the current one
     */
    protected void addTable(InstanceContainer parent, MetaProperty childMeta) {
        MetaClass meta = childMeta.getRange().asClass();

        UiEntityContext entityContext = new UiEntityContext(meta);
        accessManager.applyRegisteredConstraints(entityContext);


        UiEntityAttributeContext attributeContext =
                new UiEntityAttributeContext(parent.getEntityMetaClass(), childMeta.getName());
        accessManager.applyRegisteredConstraints(attributeContext);

        //don't show empty table if the user don't have permissions on the attribute or the entity
        if (!attributeContext.canView() ||
                !entityContext.isViewPermitted()) {
            return;
        }

        //vertical box for the table and its label
        BoxLayout vbox = uiComponents.create(VBoxLayout.class);
        vbox.setSizeFull();

        Table entitiesTable = InspectorTableBuilder.from(getApplicationContext(), createTableContainer(parent, childMeta, meta))
                .withMaxTextLength(MAX_TEXT_LENGTH)
                .withSystem(true)
                .withButtons(table -> createButtonsPanel(table, childMeta))
                .build();

        vbox.add(entitiesTable);
        vbox.expand(entitiesTable);
        vbox.setMargin(true);

        TabSheet.Tab tab = tablesTabSheet.addTab(childMeta.toString(), vbox);
        tab.setCaption(getPropertyCaption(parent.getEntityMetaClass(), childMeta));
    }

    private CollectionContainer createTableContainer(InstanceContainer parent, MetaProperty childMeta, MetaClass meta) {
        CollectionLoader loader = dataComponents.createCollectionLoader();
        CollectionContainer container = dataComponents.createCollectionContainer(meta.getJavaClass(), parent, childMeta.getName());
        loader.setContainer(container);
//        TODO replace to query
        loader.setLoadDelegate(loadContext -> {
            Collection<?> value = EntityValues.getValue(parent.getItem(), childMeta.getName());
            return value != null ? new ArrayList<>(value) : new ArrayList<>();
        });
        loader.load();
        return container;
    }

    protected String getPropertyCaption(MetaClass metaClass, MetaProperty metaProperty) {
        String caption = messageTools.getPropertyCaption(metaClass, metaProperty.getName());
        if (caption.length() < CAPTION_MAX_LENGTH)
            return caption;
        else
            return caption.substring(0, CAPTION_MAX_LENGTH);
    }

    /**
     * Creates a buttons panel managing table's content.
     *
     * @param table        table
     * @param metaProperty property representing table's data
     */
    protected void createButtonsPanel(Table table, MetaProperty metaProperty) {
        ButtonsPanel propertyButtonsPanel = table.getButtonsPanel();

        propertyButtonsPanel.add(createButton(table, metaProperty));
        if (metaProperty.getType() == ASSOCIATION) {
            propertyButtonsPanel.add(addButton(table, metaProperty));
        }
        propertyButtonsPanel.add(editButton(table, metaProperty));
        propertyButtonsPanel.add(removeButton(table, metaProperty));
    }

    private Button addButton(Table table, MetaProperty metaProperty) {
        Button addButton = uiComponents.create(Button.class);
        AddAction addAction = createAddAction(table, metaProperty);
        addButton.setAction(addAction);
        table.addAction(addAction);
        return addButton;
    }

    private AddAction createAddAction(Table table, MetaProperty metaProperty) {
        AddAction addAction = actions.create(AddAction.class);
        addAction.setOpenMode(OpenMode.THIS_TAB);
        addAction.setTarget(table);
        addAction.setScreenClass(EntityInspectorBrowser.class);

        addAction.setScreenOptionsSupplier(() -> getPropertyLookupOptions(metaProperty));
        addAction.setShortcut(componentProperties.getTableAddShortcut());
        return addAction;
    }

    protected Object getPropertyLookupOptions(MetaProperty metaProperty) {
        return new MapScreenOptions(ParamsMap.of("entity", metaProperty.getRange().asClass().getName()));
    }

    private Button createButton(Table table, MetaProperty metaProperty) {
        Button createButton = uiComponents.create(Button.class);
        CreateAction createAction = createCreateAction(table, metaProperty);
        createButton.setAction(createAction);
        table.addAction(createAction);
        return createButton;
    }

    private CreateAction createCreateAction(Table table, MetaProperty metaProperty) {
        CreateAction createAction = actions.create(CreateAction.class);
        createAction.setOpenMode(OpenMode.THIS_TAB);
        createAction.setTarget(table);
        createAction.setScreenClass(EntityInspectorEditor.class);

        createAction.setScreenOptionsSupplier(() -> {
            Map<String, Object> editorParams = new HashMap<>();
            MetaProperty inverseProperty = metaProperty.getInverse();
            if (inverseProperty != null) {
                editorParams.put(PARENT_PROPERTY_PARAM, inverseProperty.getName());
            }
            editorParams.put(PARENT_CONTEXT_PARAM, parentDataContext != null ? parentDataContext : dataContext);
            return new MapScreenOptions(editorParams);

        });
        createAction.setNewEntitySupplier(() -> {
            Object newItem = metadata.create(metaProperty.getRange().asClass());
            MetaProperty inverseProperty = metaProperty.getInverse();
            if (inverseProperty != null) {
                EntityValues.setValue(newItem, inverseProperty.getName(), getEditedEntity());
            }
            return newItem;
        });
        createAction.setShortcut(componentProperties.getTableInsertShortcut());
        return createAction;
    }

    private Button editButton(Table table, MetaProperty metaProperty) {
        Button editButton = uiComponents.create(Button.class);
        EditAction editAction = createEditAction(table, metaProperty);
        editButton.setAction(editAction);
        table.addAction(editAction);
        return editButton;
    }

    private EditAction createEditAction(Table table, MetaProperty metaProperty) {
        EditAction editAction = actions.create(EditAction.class);
        editAction.setOpenMode(OpenMode.THIS_TAB);
        editAction.setTarget(table);
        editAction.setScreenClass(EntityInspectorEditor.class);

        editAction.setScreenOptionsSupplier(() -> {
            Map<String, Object> editorParams = new HashMap<>();
            MetaProperty inverseProperty = metaProperty.getInverse();
            if (inverseProperty != null) {
                editorParams.put(PARENT_PROPERTY_PARAM, inverseProperty.getName());
            }
            editorParams.put(PARENT_CONTEXT_PARAM, dataContext);
            return new MapScreenOptions(editorParams);

        });
        editAction.setShortcut(componentProperties.getTableInsertShortcut());
        return editAction;
    }

    private Button removeButton(Table table, MetaProperty metaProperty) {
        Button removeButton = uiComponents.create(Button.class);
        Action removeAction = createRemoveAction(table, metaProperty);
        removeButton.setAction(removeAction);
        table.addAction(removeAction);
        return removeButton;
    }

    /**
     * Creates either Remove or Exclude action depending on property type
     */
    protected Action.HasTarget createRemoveAction(Table table, MetaProperty metaProperty) {
        Action.HasTarget result;
        switch (metaProperty.getType()) {
            case COMPOSITION:
                result = actions.create(RemoveAction.class);
                result.setTarget(table);
                break;
            case ASSOCIATION:
                result = actions.create(ExcludeAction.class);
                result.setTarget(table);
                break;
            default:
                throw new IllegalArgumentException("property must contain an entity");
        }
        result.setShortcut(componentProperties.getTableRemoveShortcut());
        return result;
    }

}
