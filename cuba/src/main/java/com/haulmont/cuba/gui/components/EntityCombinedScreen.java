package com.haulmont.cuba.gui.components;

import com.haulmont.cuba.core.app.LockService;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.AbstractDatasource;
import io.jmix.core.ExtendedEntities;
import io.jmix.core.Entity;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.datatype.DatatypeFormatter;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.pessimisticlocking.LockInfo;
import io.jmix.core.pessimisticlocking.LockNotSupported;
import io.jmix.core.security.EntityOp;
import io.jmix.ui.component.*;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.TabSheet;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Base class for controllers of combined browser/editor screens.
 */
public class EntityCombinedScreen extends AbstractLookup {

    /**
     * Indicates that a new instance of entity is being created.
     */
    protected boolean creating;

    /**
     * Indicates that the screen is in editing mode.
     */
    protected boolean editing;

    /**
     * Indicates that edited entity is pessimistically locked.
     */
    protected boolean justLocked;

    /**
     * Returns the left container with browse components. Override if the container id differs from "lookupBox".
     */
    protected ComponentContainer getLookupBox() {
        return (ComponentContainer) getComponentNN("lookupBox");
    }

    /**
     * Returns the browse table. Override if the table id differs from "table".
     */
    protected ListComponent getTable() {
        return (ListComponent) getComponentNN("table");
    }

    /**
     * Returns the right container with edit components. Override if the container id differs from "editBox".
     */
    protected ComponentContainer getEditBox() {
        return (ComponentContainer) getComponentNN("editBox");
    }

    /**
     * Returns the tab sheet with edit components. Can be null if the screen contains a field group only.
     * Override if the tab sheet id differs from "tabSheet".
     */
    @Nullable
    protected TabSheet getTabSheet() {
        return (TabSheet) getComponent("tabSheet");
    }

    /**
     * Returns the field group. Override if the field group id differs from "fieldGroup".
     */
    protected FieldGroup getFieldGroup() {
        return (FieldGroup) getComponentNN("fieldGroup");
    }

    /**
     * Returns the container with edit actions (save, cancel). Override if the container id differs from "actionsPane".
     */
    protected ComponentContainer getActionsPane() {
        return (ComponentContainer) getComponentNN("actionsPane");
    }

    @Override
    public void init(Map<String, Object> params) {
        initBrowseItemChangeListener();
        initBrowseCreateAction();
        initBrowseEditAction();
        initBrowseRemoveAction();
        initShortcuts();

        disableEditControls();
    }

    /**
     * Adds a listener that reloads the selected record with the specified view and sets it to editDs.
     */
    @SuppressWarnings("unchecked")
    protected void initBrowseItemChangeListener() {
        CollectionDatasource browseDs = getTable().getDatasource();
        Datasource editDs = getFieldGroup().getDatasource();

        browseDs.addItemChangeListener(e -> {
            if (e.getItem() != null) {
                Entity reloadedItem = getDsContext().getDataSupplier().reload(
                        e.getDs().getItem(), editDs.getView(), null, e.getDs().getLoadDynamicAttributes());
                editDs.setItem(reloadedItem);
            }
        });
    }

    /**
     * Adds a CreateAction that removes selection in table, sets a newly created item to editDs
     * and enables controls for record editing.
     */
    protected void initBrowseCreateAction() {
        ListComponent table = getTable();
        table.addAction(new CreateAction(table) {
            @SuppressWarnings("unchecked")
            @Override
            protected void internalOpenEditor(CollectionDatasource datasource, Entity newItem, Datasource parentDs, Map<String, Object> params) {
                initNewItem(newItem);
                table.setSelected(Collections.emptyList());
                getFieldGroup().getDatasource().setItem(newItem);
                refreshOptionsForLookupFields();
                enableEditControls(true);
            }
        });
    }

    /**
     * Hook to be implemented in subclasses. Called when the screen turns into editing mode
     * for a new entity instance. Enables additional initialization of the new entity instance
     * before setting it into the datasource.
     * @param item  new entity instance
     */
    protected void initNewItem(Entity item) {
    }

    /**
     * Adds an EditAction that enables controls for editing.
     */
    protected void initBrowseEditAction() {
        ListComponent table = getTable();
        table.addAction(new EditAction(table) {
            @Override
            public void actionPerform(Component component) {
                if (table.getSelected().size() == 1) {
                    if (lockIfNeeded((Entity) table.getSelected().iterator().next())) {
                        super.actionPerform(component);
                    }
                }
            }

            @Override
            protected void internalOpenEditor(CollectionDatasource datasource, Entity existingItem, Datasource parentDs, Map<String, Object> params) {
                refreshOptionsForLookupFields();
                enableEditControls(false);
            }

            @Override
            public void refreshState() {
                if (target != null) {
                    CollectionDatasource ds = target.getDatasource();
                    if (ds != null && !captionInitialized) {
                        setCaption(messages.getMainMessage("actions.Edit"));
                    }
                }
                super.refreshState();
            }

            @Override
            protected boolean isPermitted() {
                CollectionDatasource ownerDatasource = target.getDatasource();
                boolean entityOpPermitted = security.isEntityOpPermitted(ownerDatasource.getMetaClass(), EntityOp.UPDATE);
                if (!entityOpPermitted) {
                    return false;
                }
                return super.isPermitted();
            }
        });
    }

    /**
     * Pessimistic lock before start of editing, if it is configured for the entity.
     */
    protected boolean lockIfNeeded(Entity entity) {
        MetaClass metaClass = getMetaClassForLocking(entity);

        LockInfo lockInfo = getApplicationContext().getBean(LockService.class).lock(metaClass.getName(), EntityValues.getId(entity).toString());
        if (lockInfo == null) {
            justLocked = true;
        } else if (!(lockInfo instanceof LockNotSupported)) {
            showNotification(
                    messages.getMainMessage("entityLocked.msg"),
                    String.format(messages.getMainMessage("entityLocked.desc"),
                            lockInfo.getUsername(),
                            getApplicationContext().getBean(DatatypeFormatter.class).formatDateTime(lockInfo.getSince())
                    ),
                    NotificationType.HUMANIZED
            );
            return false;
        }
        return true;
    }

    /**
     * Release pessimistic lock if it was applied.
     */
    protected void releaseLock() {
        if (justLocked) {
            Datasource ds = getFieldGroup().getDatasource();
            Entity entity = ds.getItem();
            if (entity != null) {
                MetaClass metaClass = getMetaClassForLocking(entity);
                getApplicationContext().getBean(LockService.class).unlock(metaClass.getName(), EntityValues.getId(entity).toString());
            }
        }
    }

    protected MetaClass getMetaClassForLocking(Entity item) {
        Metadata metadata = getApplicationContext().getBean(Metadata.class);
        // lock original metaClass, if any, because by convention all the configuration is based on original entities
        MetaClass metaClass = getApplicationContext().getBean(ExtendedEntities.class).getOriginalMetaClass(metadata.getClass(item));
        if (metaClass == null) {
            metaClass = getTable().getDatasource().getMetaClass();
        }
        return metaClass;
    }

    /**
     * Adds AfterRemoveHandler for table's Remove action to reset the record contained in editDs.
     */
    @SuppressWarnings("unchecked")
    protected void initBrowseRemoveAction() {
        ListComponent table = getTable();
        Datasource editDs = getFieldGroup().getDatasource();
        RemoveAction removeAction = (RemoveAction) table.getAction(RemoveAction.ACTION_ID);
        if (removeAction != null)
            removeAction.setAfterRemoveHandler(removedItems -> editDs.setItem(null));
    }

    /**
     * Adds ESCAPE shortcut that invokes cancel() method.
     */
    protected void initShortcuts() {
        ComponentContainer editBox = getEditBox();
        if (editBox instanceof ShortcutNotifier) {
            ((ShortcutNotifier) editBox).addShortcutAction(
                    new ShortcutAction(new KeyCombination(KeyCombination.Key.ESCAPE),
                            shortcutTriggeredEvent -> cancel()));
        }
    }

    protected void refreshOptionsForLookupFields() {
        for (Component component : getFieldGroup().getOwnComponents()) {
            if (component instanceof LookupField) {
                CollectionDatasource optionsDatasource = ((LookupField) component).getOptionsDatasource();
                if (optionsDatasource != null) {
                    optionsDatasource.refresh();
                }
            }
        }
    }

    /**
     * Enables controls for editing.
     * @param creating indicates that a new instance is being created
     */
    protected void enableEditControls(boolean creating) {
        this.editing = true;
        this.creating = creating;
        initEditComponents(true);
        getFieldGroup().requestFocus();
    }

    /**
     * Disables edit controls.
     */
    protected void disableEditControls() {
        this.editing = false;
        initEditComponents(false);
        ((Focusable) getTable()).focus();
    }

    /**
     * Initializes edit controls, depending on if they should be enabled or disabled.
     * @param enabled if true - enables edit controls and disables controls on the left side of the splitter
     *                if false - vice versa
     */
    protected void initEditComponents(boolean enabled) {
        TabSheet tabSheet = getTabSheet();
        if (tabSheet != null) {
            ComponentsHelper.walkComponents(tabSheet, (component, name) -> {
                if (component instanceof FieldGroup) {
                    ((FieldGroup) component).setEditable(enabled);
                } else if (component instanceof Table) {
                    ((Table) component).getActions().forEach(action -> action.setEnabled(enabled));
                } else if (!(component instanceof HasComponents)) {
                    component.setEnabled(enabled);
                }
            });
        }
        getFieldGroup().setEditable(enabled);
        getActionsPane().setVisible(enabled);
        getLookupBox().setEnabled(!enabled);
    }

    /**
     * Validates editor fields.
     *
     * @return true if all fields are valid or false otherwise
     */
    protected boolean validateEditor() {
        FieldGroup fieldGroup = getFieldGroup();
        List<Validatable> components = new ArrayList<>();
        for (Component component: fieldGroup.getComponents()) {
            if (component instanceof Validatable) {
                components.add((Validatable)component);
            }
        }
        return validate(components);
    }

    /**
     * Method that is invoked by clicking Ok button after editing an existing or creating a new record.
     */
    @SuppressWarnings("unchecked")
    public void save() {
        if (!editing)
            return;

        if (!validateEditor()) {
            return;
        }
        getDsContext().commit();

        ListComponent table = getTable();
        CollectionDatasource browseDs = table.getDatasource();
        Entity editedItem = getFieldGroup().getDatasource().getItem();
        if (creating) {
            browseDs.includeItem(editedItem);
        } else {
            browseDs.updateItem(editedItem);
        }
        table.setSelected(editedItem);

        releaseLock();
        disableEditControls();
    }

    /**
     * Method that is invoked by clicking Cancel button, discards changes and disables controls for editing.
     */
    @SuppressWarnings("unchecked")
    public void cancel() {
        CollectionDatasource browseDs = getTable().getDatasource();
        Datasource editDs = getFieldGroup().getDatasource();

        Entity selectedItem = browseDs.getItem();
        if (selectedItem != null) {
            Entity reloadedItem = getDsContext().getDataSupplier().reload(
                    selectedItem, editDs.getView(), null, editDs.getLoadDynamicAttributes());
            browseDs.setItem(reloadedItem);
        } else {
            editDs.setItem(null);
        }

        for (Datasource dataSource : getDsContext().getAll()) {
            if (AbstractDatasource.class.isAssignableFrom(dataSource.getClass())) {
                ((AbstractDatasource) dataSource).clearCommitLists();
            }
        }

        releaseLock();
        disableEditControls();
    }
}
