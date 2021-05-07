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

import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.security.EntityOp;
import io.jmix.ui.RemoveOperation;
import io.jmix.ui.accesscontext.UiEntityAttributeContext;
import io.jmix.ui.accesscontext.UiEntityContext;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.data.meta.ContainerDataUnit;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.meta.StudioPropertiesItem;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.Nested;
import io.jmix.ui.UiComponentProperties;
import io.jmix.ui.screen.Install;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Standard action for removing an entity instance from the list and from the database.
 * <p>
 * Should be defined for a list component ({@code Table}, {@code DataGrid}, etc.) in a screen XML descriptor.
 * <p>
 * The action instance can be parameterized using the nested {@code properties} XML element or programmatically in the
 * screen controller.
 */
@StudioAction(
        target = "io.jmix.ui.component.ListComponent",
        description = "Removes an entity instance from the list and from the database",
        availableInScreenWizard = true)
@ActionType(RemoveAction.ID)
public class RemoveAction<E> extends SecuredListAction implements Action.AdjustWhenScreenReadOnly,
        Action.ExecutableAction {

    public static final String ID = "remove";

    protected RemoveOperation removeOperation;

    protected Boolean confirmation;
    protected String confirmationMessage;
    protected String confirmationTitle;
    protected Consumer<RemoveOperation.AfterActionPerformedEvent<E>> afterActionPerformedHandler;
    protected Consumer<RemoveOperation.ActionCancelledEvent<E>> actionCancelledHandler;

    public RemoveAction() {
        super(ID);
    }

    public RemoveAction(String id) {
        super(id);
        super.setConstraintEntityOp(EntityOp.DELETE);
    }

    /**
     * Returns true/false if the confirmation flag was set by {@link #setConfirmation(Boolean)} or in the screen XML.
     * Otherwise returns null.
     */
    @Nullable
    public Boolean getConfirmation() {
        return confirmation;
    }

    /**
     * Sets whether to ask confirmation from the user.
     */
    @StudioPropertiesItem(defaultValue = "true")
    public void setConfirmation(Boolean confirmation) {
        this.confirmation = confirmation;
    }

    /**
     * Returns confirmation dialog message if it was set by {@link #setConfirmationMessage(String)} or in the screen XML.
     * Otherwise returns null.
     */
    @Nullable
    public String getConfirmationMessage() {
        return confirmationMessage;
    }

    /**
     * Sets confirmation dialog message.
     */
    @StudioPropertiesItem
    public void setConfirmationMessage(String confirmationMessage) {
        this.confirmationMessage = confirmationMessage;
    }

    /**
     * Returns confirmation dialog title if it was set by {@link #setConfirmationTitle(String)} or in the screen XML.
     * Otherwise returns null.
     */
    @Nullable
    public String getConfirmationTitle() {
        return confirmationTitle;
    }

    /**
     * Sets confirmation dialog title.
     */
    @StudioPropertiesItem
    public void setConfirmationTitle(String confirmationTitle) {
        this.confirmationTitle = confirmationTitle;
    }

    /**
     * Sets the handler to be invoked after removing entities.
     * <p>
     * The preferred way to set the handler is using a controller method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petsTable.remove", subject = "afterActionPerformedHandler")
     * protected void petsTableRemoveAfterActionPerformedHandler(RemoveOperation.AfterActionPerformedEvent event) {
     *     System.out.println("Removed " + event.getItems());
     * }
     * </pre>
     */
    public void setAfterActionPerformedHandler(Consumer<RemoveOperation.AfterActionPerformedEvent<E>> afterActionPerformedHandler) {
        this.afterActionPerformedHandler = afterActionPerformedHandler;
    }

    /**
     * Sets the handler to be invoked if the action was cancelled by the user.
     * <p>
     * The preferred way to set the handler is using a controller method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petsTable.remove", subject = "actionCancelledHandler")
     * protected void petsTableRemoveActionCancelledHandler(RemoveOperation.ActionCancelledEvent event) {
     *     System.out.println("Cancelled");
     * }
     * </pre>
     */
    public void setActionCancelledHandler(Consumer<RemoveOperation.ActionCancelledEvent<E>> actionCancelledHandler) {
        this.actionCancelledHandler = actionCancelledHandler;
    }

    @Autowired
    protected void setIcons(Icons icons) {
        this.icon = icons.get(JmixIcon.REMOVE_ACTION);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.caption = messages.getMessage("actions.Remove");
    }

    @Autowired
    protected void setUiComponentProperties(UiComponentProperties componentProperties) {
        setShortcut(componentProperties.getTableRemoveShortcut());
    }

    @Autowired
    public void setRemoveOperation(RemoveOperation removeOperation) {
        this.removeOperation = removeOperation;
    }

    @Override
    protected boolean isPermitted() {
        if (!checkRemovePermission()) {
            return false;
        }

        return super.isPermitted();
    }

    protected boolean checkRemovePermission() {
        if (target == null || !(target.getItems() instanceof ContainerDataUnit)) {
            return false;
        }

        ContainerDataUnit<E> containerDataUnit = (ContainerDataUnit) target.getItems();

        MetaClass metaClass = containerDataUnit.getEntityMetaClass();
        if (metaClass == null) {
            return false;
        }

        UiEntityContext entityContext = new UiEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(entityContext);

        if (!entityContext.isDeletePermitted()) {
            return false;
        }

        if (containerDataUnit.getContainer() instanceof Nested) {
            Nested nestedContainer = (Nested) containerDataUnit.getContainer();

            MetaClass masterMetaClass = nestedContainer.getMaster().getEntityMetaClass();
            MetaProperty metaProperty = masterMetaClass.getProperty(nestedContainer.getProperty());

            UiEntityAttributeContext entityAttributeContext =
                    new UiEntityAttributeContext(masterMetaClass, metaProperty.getName());
            accessManager.applyRegisteredConstraints(entityAttributeContext);

            if (!entityAttributeContext.canModify()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void actionPerform(Component component) {
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
        if (target == null) {
            throw new IllegalStateException("RemoveAction target is not set");
        }

        if (!(target.getItems() instanceof ContainerDataUnit)) {
            throw new IllegalStateException("RemoveAction target items is null or does not implement ContainerDataUnit");
        }

        ContainerDataUnit<E> items = (ContainerDataUnit) target.getItems();
        CollectionContainer<E> container = items.getContainer();
        if (container == null) {
            throw new IllegalStateException("RemoveAction target is not bound to CollectionContainer");
        }

        RemoveOperation.RemoveBuilder<E> builder = removeOperation.builder(target);

        if (confirmation != null) {
            builder = builder.withConfirmation(confirmation);
        } else {
            builder = builder.withConfirmation(true);
        }

        if (confirmationMessage != null) {
            builder = builder.withConfirmationMessage(confirmationMessage);
        }

        if (confirmationTitle != null) {
            builder = builder.withConfirmationTitle(confirmationTitle);
        }

        if (afterActionPerformedHandler != null) {
            builder = builder.afterActionPerformed(afterActionPerformedHandler);
        }

        if (actionCancelledHandler != null) {
            builder = builder.onCancel(actionCancelledHandler);
        }

        builder.remove();
    }
}
