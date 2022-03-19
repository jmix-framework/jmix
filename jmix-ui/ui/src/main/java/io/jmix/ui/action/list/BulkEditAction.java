/*
 * Copyright (c) 2008-2018 Haulmont.
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

package io.jmix.ui.action.list;


import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.accesscontext.UiBulkEditContext;
import io.jmix.ui.action.Action;
import io.jmix.ui.bulk.BulkEditors;
import io.jmix.ui.Notifications;
import io.jmix.ui.Notifications.NotificationType;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.app.bulk.ColumnsMode;
import io.jmix.ui.app.bulk.FieldSorter;
import io.jmix.ui.bulk.BulkEditorBuilder;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.Window;
import io.jmix.ui.component.data.meta.EntityDataUnit;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.meta.StudioPropertiesItem;
import io.jmix.ui.screen.OpenMode;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.List;

import static io.jmix.ui.component.ComponentsHelper.getScreenContext;

/**
 * Standard action for changing attribute values for several entity instances at once.
 * <p>
 * Should be defined for a list component ({@code Table}, {@code DataGrid}, etc.) in a screen XML descriptor.
 * <p>
 * The action instance can be parameterized using the nested {@code properties} XML element or programmatically in the
 * screen controller.
 */
@StudioAction(
        target = "io.jmix.ui.component.ListComponent",
        description = "Opens an editor for changing attribute values for several entity instances at once")
@ActionType(BulkEditAction.ID)
public class BulkEditAction extends SecuredListAction implements Action.ExecutableAction {

    public static final String ID = "bulkEdit";


    protected Messages messages;
    protected BulkEditors bulkEditors;

    protected ColumnsMode columnsMode;
    protected String exclude;
    protected FieldSorter fieldSorter;
    protected List<String> includeProperties;
    protected OpenMode openMode;
    protected Boolean loadDynamicAttributes;
    protected Boolean useConfirmDialog;

    protected boolean visibleBySpecificUiPermission = true;

    public BulkEditAction() {
        this(ID);
    }

    public BulkEditAction(String id) {
        super(id);
    }

    /**
     * Returns the columns mode which defines the number of columns either it was set by {@link #setColumnsMode(ColumnsMode)}
     * or in the screen XML. Otherwise returns null.
     */
    @Nullable
    public ColumnsMode getColumnsMode() {
        return columnsMode;
    }

    /**
     * Sets the columns mode which defines the number of columns.
     *
     * @see ColumnsMode#ONE_COLUMN
     * @see ColumnsMode#TWO_COLUMNS
     */
    @StudioPropertiesItem(defaultValue = "TWO_COLUMNS")
    public void setColumnsMode(ColumnsMode columnsMode) {
        this.columnsMode = columnsMode;
    }

    /**
     * Returns a regular expression to exclude fields if it was set by {@link #setExclude(String)} or in the screen XML.
     * Otherwise returns null.
     */
    @Nullable
    public String getExclude() {
        return exclude;
    }

    /**
     * Sets a regular expression to exclude some fields explicitly
     * from the list of attributes available for editing.
     */
    @StudioPropertiesItem
    public void setExclude(String exclude) {
        this.exclude = exclude;
    }

    /**
     * Sets field sorter that allows you to sort fields by custom logic.
     */
    public void setFieldSorter(FieldSorter fieldSorter) {
        this.fieldSorter = fieldSorter;
    }

    /**
     * Returns a list entity attributes to be included to bulk editor window if it was set by {@link #setIncludeProperties(List)} or in the screen XML.
     * Otherwise returns null.
     */
    @Nullable
    public List<String> getIncludeProperties() {
        return includeProperties;
    }

    /**
     * Sets the entity attributes to be included to bulk editor window.
     * If set, other attributes will be ignored.
     */
    @StudioPropertiesItem(type = PropertyType.STRING)
    public void setIncludeProperties(List<String> includeProperties) {
        this.includeProperties = includeProperties;
    }

    /**
     * Returns the bulk editor screen open mode if it was set by {@link #setOpenMode(OpenMode)} or in the screen XML.
     * Otherwise returns null.
     */
    @Nullable
    public OpenMode getOpenMode() {
        return openMode;
    }

    /**
     * Sets the bulk editor screen open mode.
     */
    @StudioPropertiesItem(defaultValue = "DIALOG")
    public void setOpenMode(OpenMode openMode) {
        this.openMode = openMode;
    }

    /**
     * Returns true/false if the flag was set by {@link #setLoadDynamicAttributes(Boolean)} or in the screen XML.
     * Otherwise returns null.
     */
    @Nullable
    public Boolean getLoadDynamicAttributes() {
        return loadDynamicAttributes;
    }

    /**
     * Sets whether dynamic attributes of the edited entity should be displayed on
     * the entity's bulk editor screen. The default value is true.
     */
    @StudioPropertiesItem(defaultValue = "true")
    public void setLoadDynamicAttributes(Boolean loadDynamicAttributes) {
        this.loadDynamicAttributes = loadDynamicAttributes;
    }

    /**
     * Returns true/false if the flag was set by {@link #setUseConfirmDialog(Boolean)} or in the screen XML.
     * Otherwise returns null.
     */
    @Nullable
    public Boolean getUseConfirmDialog() {
        return useConfirmDialog;
    }

    /**
     * Sets whether or not the confirmation dialog should be displayed to
     * the user before saving the changes. The default value is true.
     */
    @StudioPropertiesItem(defaultValue = "true")
    public void setUseConfirmDialog(Boolean useConfirmDialog) {
        this.useConfirmDialog = useConfirmDialog;
    }

    @Autowired
    protected void setIcons(Icons icons) {
        this.icon = icons.get(JmixIcon.BULK_EDIT_ACTION);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.messages = messages;
        this.caption = messages.getMessage("actions.BulkEdit");
    }

    @Autowired
    @Override
    protected void setAccessManager(AccessManager accessManager) {
        super.setAccessManager(accessManager);

        UiBulkEditContext context = new UiBulkEditContext();
        accessManager.applyRegisteredConstraints(context);

        visibleBySpecificUiPermission = context.isPermitted();
    }

    @Override
    public boolean isVisibleByUiPermissions() {
        return visibleBySpecificUiPermission
                && super.isVisibleByUiPermissions();
    }

    @Autowired
    public void setBulkEditors(BulkEditors bulkEditors) {
        this.bulkEditors = bulkEditors;
    }

    @Override
    protected boolean isPermitted() {
        if (target == null
                || !(target.getItems() instanceof EntityDataUnit)) {
            return false;
        }

        MetaClass metaClass = ((EntityDataUnit) target.getItems()).getEntityMetaClass();
        if (metaClass == null) {
            return true;
        }

        return super.isPermitted();
    }

    @Override
    public void actionPerform(Component component) {
        // if standard behaviour
        if (!hasSubscriptions(ActionPerformedEvent.class)) {
            execute();
        } else {
            super.actionPerform(component);
        }
    }

    /**
     * Executes the action.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void execute() {
        if (!(target.getItems() instanceof EntityDataUnit)) {
            throw new IllegalStateException("BulkEditAction target Items is null " +
                    "or does not implement EntityDataUnit");
        }

        MetaClass metaClass = ((EntityDataUnit) target.getItems()).getEntityMetaClass();
        if (metaClass == null) {
            throw new IllegalStateException("Target is not bound to entity");
        }

        UiBulkEditContext context = new UiBulkEditContext();
        accessManager.applyRegisteredConstraints(context);
        if (!context.isPermitted()) {
            if (target.getFrame() != null) {
                Notifications notifications = getScreenContext(target.getFrame()).getNotifications();
                notifications.create(NotificationType.ERROR)
                        .withCaption(messages.getMessage("accessDenied.message"))
                        .show();
            }
            return;
        }

        if (target.getSelected().isEmpty()
                && target.getFrame() != null) {
            Notifications notifications = getScreenContext(target.getFrame()).getNotifications();
            notifications.create(NotificationType.ERROR)
                    .withCaption(messages.getMessage("actions.BulkEdit.emptySelection"))
                    .show();
            return;
        }

        Window window = ComponentsHelper.getWindowNN(target);

        BulkEditorBuilder<?> builder = bulkEditors.builder(metaClass,
                target.getSelected(), window.getFrameOwner())
                .withListComponent(target);

        if (columnsMode != null) {
            builder = builder.withColumnsMode(columnsMode);
        }

        if (exclude != null) {
            builder = builder.withExclude(exclude);
        }

        if (fieldSorter != null) {
            builder = builder.withFieldSorter(fieldSorter);
        }

        if (includeProperties != null) {
            builder = builder.withIncludeProperties(includeProperties);
        }

        if (openMode != null) {
            builder = builder.withOpenMode(openMode);
        }

        if (useConfirmDialog != null) {
            builder = builder.withUseConfirmDialog(useConfirmDialog);
        }

        builder.create().show();
    }
}
