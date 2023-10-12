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

package io.jmix.bulkeditor.action;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.bulkeditor.view.builder.BulkEditorBuilder;
import io.jmix.bulkeditor.view.builder.BulkEditors;
import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.action.list.SecuredListDataComponentAction;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.data.EntityDataUnit;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.view.View;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Standard action for changing attribute values for several entity instances at once.
 * <p>
 * Should be defined for a list data component in a screen XML descriptor.
 * <p>
 * The action instance can be parameterized using the nested {@code properties} XML element or programmatically in the
 * screen controller.
 *
 * @param <E> entity type
 */
@ActionType(BulkEditAction.ID)
public class BulkEditAction<E> extends SecuredListDataComponentAction<BulkEditAction<E>, E> implements ExecutableAction {

    public static final String ID = "bulked_edit";

    protected BulkEditors bulkEditors;

    protected String exclude;
    protected Function<List<MetaProperty>, Map<MetaProperty, Integer>> fieldSorter;
    protected List<String> includeProperties;
    protected boolean useConfirmDialog = true;

    protected boolean visibleBySpecificUiPermission = true;

    public BulkEditAction() {
        this(ID);
    }

    public BulkEditAction(String id) {
        super(id);
    }

    @Override
    protected void initAction() {
        super.initAction();

        this.icon = ComponentUtils.convertToIcon(VaadinIcon.TABLE);
    }

    /**
     * @return regular expression to exclude fields if it was set by {@link #setExclude(String)} or in the screen XML.
     * Otherwise, returns null
     */
    @Nullable
    public String getExclude() {
        return exclude;
    }

    /**
     * @param exclude regular expression to exclude some fields explicitly
     *                from the list of attributes available for editing
     */
    public void setExclude(String exclude) {
        this.exclude = exclude;
    }

    /**
     * @param fieldSorter function to sort fields by custom logic
     */
    public void setFieldSorter(Function<List<MetaProperty>, Map<MetaProperty, Integer>> fieldSorter) {
        this.fieldSorter = fieldSorter;
    }

    /**
     * @return list entity attributes to be included to bulk editor window if it was set by
     * {@link #setIncludeProperties(List)} or in the screen XML. Otherwise, returns null
     */
    @Nullable
    public List<String> getIncludeProperties() {
        return includeProperties;
    }

    /**
     * @param includeProperties entity attributes to be included to bulk editor window.
     *                          If set, other attributes will be ignored
     */
    public void setIncludeProperties(List<String> includeProperties) {
        this.includeProperties = includeProperties;
    }

    /**
     * @return true/false whether confirmation dialog {@link #setUseConfirmDialog(boolean)} is used.
     */
    public boolean getUseConfirmDialog() {
        return useConfirmDialog;
    }

    /**
     * @param useConfirmDialog flag whether the confirmation dialog should be displayed to
     *                         the user before saving the changes. The default value is true
     */
    public void setUseConfirmDialog(boolean useConfirmDialog) {
        this.useConfirmDialog = useConfirmDialog;
    }

    @Autowired
    protected void setMessages(Messages messages) {
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

        UiBulkEditContext context = new UiBulkEditContext();
        accessManager.applyRegisteredConstraints(context);

        if (!context.isPermitted()) {
            return false;
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

        View<?> origin = UiComponentUtils.getView((Component) target);

        BulkEditorBuilder<?> builder = bulkEditors.builder(metaClass, target.getSelectedItems(), origin)
                .withListDataComponent(target);

        if (exclude != null) {
            builder = builder.withExclude(exclude);
        }

        if (fieldSorter != null) {
            builder = builder.withFieldSorter(fieldSorter);
        }

        if (includeProperties != null) {
            builder = builder.withIncludeProperties(includeProperties);
        }

        builder = builder.withUseConfirmDialog(useConfirmDialog);
        builder.open();
    }

    /**
     * @see #setExclude(String)
     */
    public BulkEditAction<E> withExclude(String exclude) {
        setExclude(exclude);
        return this;
    }

    /**
     * @see #setFieldSorter(Function)
     */
    public BulkEditAction<E> withFieldSorter(Function<List<MetaProperty>, Map<MetaProperty, Integer>> fieldSorter) {
        setFieldSorter(fieldSorter);
        return this;
    }

    /**
     * @see #setIncludeProperties(List)
     */
    public BulkEditAction<E> withIncludeProperties(List<String> includeProperties) {
        setIncludeProperties(includeProperties);
        return this;
    }

    /**
     * @see #setUseConfirmDialog(boolean)
     */
    public BulkEditAction<E> withUseConfirmDialog(boolean useConfirmDialog) {
        setUseConfirmDialog(useConfirmDialog);
        return this;
    }
}
