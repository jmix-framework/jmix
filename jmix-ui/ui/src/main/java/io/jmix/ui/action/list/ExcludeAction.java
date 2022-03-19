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
import io.jmix.ui.RemoveOperation;
import io.jmix.ui.accesscontext.UiEntityAttributeContext;
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
 * Standard action for excluding entity instances from the list. The excluded entities are not deleted.
 * <p>
 * Should be defined for a list component ({@code Table}, {@code DataGrid}, etc.) connected to a nested data container.
 * <p>
 * The action instance can be parameterized using the nested {@code properties} XML element or programmatically in the
 * screen controller.
 */
@StudioAction(
        target = "io.jmix.ui.component.ListComponent",
        description = "Excludes entities from the list. The excluded entities are not deleted.",
        availableInScreenWizard = true)
@ActionType(ExcludeAction.ID)
public class ExcludeAction<E> extends SecuredListAction implements Action.AdjustWhenScreenReadOnly,
        Action.ExecutableAction {

    public static final String ID = "exclude";

    protected RemoveOperation removeOperation;

    protected Boolean confirmation;
    protected String confirmationMessage;
    protected String confirmationTitle;
    protected Consumer<RemoveOperation.AfterActionPerformedEvent<E>> afterActionPerformedHandler;
    protected Consumer<RemoveOperation.ActionCancelledEvent<E>> actionCancelledHandler;

    public ExcludeAction() {
        super(ID);
    }

    public ExcludeAction(String id) {
        super(id);
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
     * Sets the handler to be invoked after excluding entities.
     * <p>
     * The preferred way to set the handler is using a controller method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petsTable.exclude", subject = "afterActionPerformedHandler")
     * protected void petsTableExcludeAfterActionPerformedHandler(RemoveOperation.AfterActionPerformedEvent event) {
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
     * &#64;Install(to = "petsTable.exclude", subject = "actionCancelledHandler")
     * protected void petsTableExcludeActionCancelledHandler(RemoveOperation.ActionCancelledEvent event) {
     *     System.out.println("Cancelled");
     * }
     * </pre>
     */
    public void setActionCancelledHandler(Consumer<RemoveOperation.ActionCancelledEvent<E>> actionCancelledHandler) {
        this.actionCancelledHandler = actionCancelledHandler;
    }

    @Autowired
    protected void setIcons(Icons icons) {
        this.icon = icons.get(JmixIcon.EXCLUDE_ACTION);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.caption = messages.getMessage("actions.Exclude");
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
        if (target == null || !(target.getItems() instanceof ContainerDataUnit)) {
            return false;
        }

        ContainerDataUnit<E> containerDataUnit = (ContainerDataUnit) target.getItems();

        MetaClass metaClass = containerDataUnit.getEntityMetaClass();
        if (metaClass == null) {
            return false;
        }

        if (containerDataUnit.getContainer() instanceof Nested) {
            Nested nestedContainer = (Nested) containerDataUnit.getContainer();

            MetaClass masterMetaClass = nestedContainer.getMaster().getEntityMetaClass();
            MetaProperty metaProperty = masterMetaClass.getProperty(nestedContainer.getProperty());

            UiEntityAttributeContext attributeContext =
                    new UiEntityAttributeContext(masterMetaClass, metaProperty.getName());
            accessManager.applyRegisteredConstraints(attributeContext);

            boolean attrPermitted = attributeContext.canModify();
            if (!attrPermitted) {
                return false;
            }
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
        if (target == null) {
            throw new IllegalStateException("ExcludeAction target is not set");
        }

        if (!(target.getItems() instanceof ContainerDataUnit)) {
            throw new IllegalStateException("ExcludeAction target items is null or does not implement ContainerDataUnit");
        }

        ContainerDataUnit<E> items = (ContainerDataUnit) target.getItems();
        CollectionContainer<E> container = items.getContainer();
        if (container == null) {
            throw new IllegalStateException("ExcludeAction target is not bound to CollectionContainer");
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

        builder.exclude();
    }
}
