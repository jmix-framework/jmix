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

import io.jmix.core.*;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.security.EntityAttrAccess;
import io.jmix.core.security.EntityOp;
import io.jmix.core.security.Security;
import io.jmix.datatoolsui.screen.entityinspector.assistant.InspectorFetchPlanBuilder;
import io.jmix.datatoolsui.screen.entityinspector.assistant.InspectorFormBuilder;
import io.jmix.datatoolsui.screen.entityinspector.assistant.InspectorTableBuilder;
import io.jmix.ui.Actions;
import io.jmix.ui.UiComponents;
import io.jmix.ui.UiProperties;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.list.*;
import io.jmix.ui.component.*;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.model.*;
import io.jmix.ui.model.impl.NoopDataContext;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static io.jmix.core.metamodel.model.MetaProperty.Type.ASSOCIATION;
import static io.jmix.datatoolsui.screen.entityinspector.EntityFormUtils.*;

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
    protected Messages messages;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected DataComponents dataComponents;
    @Autowired
    protected Security security;
    @Autowired
    protected UiProperties uiProperties;
    @Autowired
    protected Actions actions;
    @Autowired
    protected Icons icons;

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
                dataContext = new NoopDataContext(getBeanLocator());
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
    public void setEntityToEdit(Entity entity) {
        super.setEntityToEdit(entity);
        container = initMainContainer(entity);
        isNew = entityStates.isNew(entity);
    }

    @Override
    protected InstanceContainer getEditedEntityContainer() {
        return container;
    }

    private InstanceContainer initMainContainer(Entity entity) {
        InstanceContainer container = dataComponents.createInstanceContainer(entity.getClass());
        if (!entityStates.isNew(entity)) {
            InstanceLoader loader = dataComponents.createInstanceLoader();
            loader.setDataContext(dataContext);
            loader.setFetchPlan(InspectorFetchPlanBuilder.of(getBeanLocator(), entity.getClass())
                    .withCollections(true)
                    .withEmbedded(true)
                    .withSystemProperties(true)
                    .build());
            loader.setEntityId(EntityValues.getId(entity));
            loader.setContainer(container);
            loader.load();
        } else {
            container.setItem(entity);
        }
        return container;
    }

    private void createForm(InstanceContainer container) {
        Form form = InspectorFormBuilder.from(getBeanLocator(), container)
                .withDisabledProperties(parentProperty)
                .build();

        MetaClass metaClass = container.getEntityMetaClass();
        Entity item = getEditedEntity();

        contentPane.add(form);

        for (MetaProperty metaProperty : metaClass.getProperties()) {
            switch (metaProperty.getType()) {
                case COMPOSITION:
                case ASSOCIATION:
                    if (isMany(metaProperty)) {
                        addTable(container, metaProperty);
                    } else {
                        if (isEmbedded(metaProperty)) {
                            Entity propertyValue = EntityValues.getValue(item, metaProperty.getName());
                            if (propertyValue != null) {
                                propertyValue = dataContext.merge(propertyValue);
                            }
                            InstanceContainer embeddedContainer = dataComponents.createInstanceContainer(
                                    metaProperty.getRange().asClass().getJavaClass(), container, metaProperty.getName());
                            embeddedContainer.setItem(propertyValue);
                            Form embeddedForm = InspectorFormBuilder.from(getBeanLocator(), embeddedContainer)
                                    .withCaption(getPropertyCaption(metaClass, metaProperty))
                                    .build();
                            contentPane.add(embeddedForm);
                        }
                    }
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
            Entity item = metadata.create(meta);
            setEntityToEdit(item);
        }
    }

    /**
     * Creates a table for the entities in ONE_TO_MANY or MANY_TO_MANY relation with the current one
     */
    protected void addTable(InstanceContainer parent, MetaProperty childMeta) {
        MetaClass meta = childMeta.getRange().asClass();

        //don't show empty table if the user don't have permissions on the attribute or the entity
        if (!security.isEntityAttrPermitted(parent.getEntityMetaClass(), childMeta.getName(), EntityAttrAccess.VIEW) ||
                !security.isEntityOpPermitted(meta, EntityOp.READ)) {
            return;
        }

        //vertical box for the table and its label
        BoxLayout vbox = uiComponents.create(VBoxLayout.class);
        vbox.setSizeFull();

        Table entitiesTable = InspectorTableBuilder.from(getBeanLocator(), createTableContainer(parent, childMeta, meta))
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
     * @return buttons panel
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
        addButton.setIcon(icons.get(JmixIcon.ADD_ACTION));
        return addButton;
    }

    private AddAction createAddAction(Table table, MetaProperty metaProperty) {
        AddAction addAction = actions.create(AddAction.class);
        addAction.setOpenMode(OpenMode.THIS_TAB);
        addAction.setTarget(table);
        addAction.setScreenClass(EntityInspectorBrowser.class);

        addAction.setScreenOptionsSupplier(() -> getPropertyLookupOptions(metaProperty));
        addAction.setShortcut(uiProperties.getTableAddShortcut());
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
        createButton.setIcon(icons.get(JmixIcon.CREATE_ACTION));
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
            Entity newItem = metadata.create(metaProperty.getRange().asClass());
            MetaProperty inverseProperty = metaProperty.getInverse();
            if (inverseProperty != null) {
                EntityValues.setValue(newItem, inverseProperty.getName(), getEditedEntity());
            }
            return newItem;
        });
        createAction.setShortcut(uiProperties.getTableInsertShortcut());
        return createAction;
    }

    private Button editButton(Table table, MetaProperty metaProperty) {
        Button editButton = uiComponents.create(Button.class);
        EditAction editAction = createEditAction(table, metaProperty);
        editButton.setAction(editAction);
        table.addAction(editAction);
        editButton.setIcon(icons.get(JmixIcon.EDIT_ACTION));
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
        editAction.setShortcut(uiProperties.getTableInsertShortcut());
        return editAction;
    }

    private Button removeButton(Table table, MetaProperty metaProperty) {
        Button removeButton = uiComponents.create(Button.class);
        Action removeAction = createRemoveAction(table, metaProperty);
        removeButton.setAction(removeAction);
        table.addAction(removeAction);
        removeButton.setIcon(icons.get(JmixIcon.REMOVE_ACTION));
        removeButton.setCaption(messages.getMessage("remove"));
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
        result.setShortcut(uiProperties.getTableRemoveShortcut());
        return result;
    }

}
