/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.action.list;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.accesscontext.UiBulkEditContext;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.app.bulk.ColumnsMode;
import io.jmix.flowui.app.bulk.FieldSorter;
import io.jmix.flowui.bulk.BulkEditorBuilder;
import io.jmix.flowui.bulk.BulkEditors;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.data.EntityDataUnit;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.view.View;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.List;

@ActionType(BulkEditAction.ID)
public class BulkEditAction<E> extends SecuredListDataComponentAction<BulkEditAction<E>, E> implements ExecutableAction {

    public static final String ID = "list_bulkEdit";

    protected Messages messages;
    protected BulkEditors bulkEditors;
    protected DialogWindows dialogWindows;

    protected ColumnsMode columnsMode;
    protected String exclude;
    protected FieldSorter fieldSorter;
    protected List<String> includeProperties;
    protected Boolean loadDynamicAttributes;
    protected Boolean useConfirmDialog;

    protected boolean visibleBySpecificUiPermission = true;

    public BulkEditAction() {
        this(ID);
    }

    public BulkEditAction(String id) {
        super(id);
        this.icon = ComponentUtils.convertToIcon(VaadinIcon.TABLE);
    }

    /**
     * Returns the columns mode which defines the number of columns either it was set by {@link #setColumnsMode(ColumnsMode)}
     * or in the screen XML. Otherwise, returns null.
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
    public void setColumnsMode(ColumnsMode columnsMode) {
        this.columnsMode = columnsMode;
    }

    /**
     * Returns a regular expression to exclude fields if it was set by {@link #setExclude(String)} or in the screen XML.
     * Otherwise, returns null.
     */
    @Nullable
    public String getExclude() {
        return exclude;
    }

    /**
     * Sets a regular expression to exclude some fields explicitly
     * from the list of attributes available for editing.
     */
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
     * Otherwise, returns null.
     */
    @Nullable
    public List<String> getIncludeProperties() {
        return includeProperties;
    }

    /**
     * Sets the entity attributes to be included to bulk editor window.
     * If set, other attributes will be ignored.
     */
    public void setIncludeProperties(List<String> includeProperties) {
        this.includeProperties = includeProperties;
    }

    /**
     * Returns true/false if the flag was set by {@link #setLoadDynamicAttributes(Boolean)} or in the screen XML.
     * Otherwise, returns null.
     */
    @Nullable
    public Boolean getLoadDynamicAttributes() {
        return loadDynamicAttributes;
    }

    /**
     * Sets whether dynamic attributes of the edited entity should be displayed on
     * the entity's bulk editor screen. The default value is true.
     */
    public void setLoadDynamicAttributes(Boolean loadDynamicAttributes) {
        this.loadDynamicAttributes = loadDynamicAttributes;
    }

    /**
     * Returns true/false if the flag was set by {@link #setUseConfirmDialog(Boolean)} or in the screen XML.
     * Otherwise, returns null.
     */
    @Nullable
    public Boolean getUseConfirmDialog() {
        return useConfirmDialog;
    }

    /**
     * Sets whether the confirmation dialog should be displayed to
     * the user before saving the changes. The default value is true.
     */
    public void setUseConfirmDialog(Boolean useConfirmDialog) {
        this.useConfirmDialog = useConfirmDialog;
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.messages = messages;
        this.text = messages.getMessage("actions.BulkEdit");
    }

    @Autowired
    @Override
    protected void setAccessManager(AccessManager accessManager) {
        super.setAccessManager(accessManager);

        UiBulkEditContext context = new UiBulkEditContext();
        accessManager.applyRegisteredConstraints(context);

        visibleBySpecificUiPermission = context.isPermitted();
    }

    @Autowired
    public void setDialogWindows(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
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
        if (target == null || !(target.getItems() instanceof EntityDataUnit)) {
            return false;
        }

        MetaClass metaClass = ((EntityDataUnit) target.getItems()).getEntityMetaClass();
        //noinspection ConstantValue
        if (metaClass == null) {
            return true;
        }

        return super.isPermitted();
    }

    @Override
    public void actionPerform(Component component) {
        if (!hasListener(ActionPerformedEvent.class)) {
            execute();
        } else {
            super.actionPerform(component);
        }
    }

    /**
     * Executes the action.
     */
    @Override
    public void execute() {
        checkTarget();
        checkTargetItems(EntityDataUnit.class);

        //noinspection DataFlowIssue
        MetaClass metaClass = ((EntityDataUnit) target.getItems()).getEntityMetaClass();
        //noinspection ConstantValue
        if (metaClass == null) {
            throw new IllegalStateException("Target is not bound to entity");
        }

        UiBulkEditContext context = new UiBulkEditContext();
        accessManager.applyRegisteredConstraints(context);

        View<?> origin = UiComponentUtils.getView((Component) target);

        BulkEditorBuilder<?> builder = bulkEditors.builder(metaClass, target.getSelectedItems(), origin)
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

        if (useConfirmDialog != null) {
            builder = builder.withUseConfirmDialog(useConfirmDialog);
        }
        builder.create().open();
    }

    public BulkEditAction<E> withColumnsMode(ColumnsMode columnsMode) {
        setColumnsMode(columnsMode);
        return this;
    }

    public BulkEditAction<E> withExclude(String exclude) {
        setExclude(exclude);
        return this;
    }

    public BulkEditAction<E> withFieldSorter(FieldSorter fieldSorter) {
        setFieldSorter(fieldSorter);
        return this;
    }

    public BulkEditAction<E> withIncludeProperties(List<String> includeProperties) {
        setIncludeProperties(includeProperties);
        return this;
    }

    public BulkEditAction<E> withLoadDynamicAttributes(boolean loadDynamicAttributes) {
        setLoadDynamicAttributes(loadDynamicAttributes);
        return this;
    }

    public BulkEditAction<E> withUseConfirmDialog(boolean useConfirmDialog) {
        setUseConfirmDialog(useConfirmDialog);
        return this;
    }
}
