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

package io.jmix.flowui.action.list;

import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.flowui.FlowuiComponentProperties;
import io.jmix.flowui.accesscontext.FlowuiEntityAttributeContext;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.AdjustWhenViewReadOnly;
import io.jmix.flowui.data.ContainerDataUnit;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.Nested;
import io.jmix.flowui.util.RemoveOperation;
import io.jmix.flowui.util.RemoveOperation.ActionCancelledEvent;
import io.jmix.flowui.util.RemoveOperation.AfterActionPerformedEvent;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@ActionType(ExcludeAction.ID)
public class ExcludeAction<E> extends SecuredListDataComponentAction<ExcludeAction<E>, E>
        implements AdjustWhenViewReadOnly {

    public static final String ID = "exclude";

    protected RemoveOperation removeOperation;

    protected boolean confirmation = true;
    protected String confirmationText;
    protected String confirmationHeader;
    protected Consumer<AfterActionPerformedEvent<E>> afterActionPerformedHandler;
    protected Consumer<ActionCancelledEvent<E>> actionCancelledHandler;

    public ExcludeAction() {
        this(ID);
    }

    public ExcludeAction(String id) {
        super(id);
    }

    @Override
    protected void initAction() {
        super.initAction();

        variant = ActionVariant.DANGER;
        icon = FlowuiComponentUtils.convertToIcon(VaadinIcon.CLOSE);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.Exclude");
    }

    @Autowired
    public void setRemoveOperation(RemoveOperation removeOperation) {
        this.removeOperation = removeOperation;
    }

    @Autowired
    protected void setFlowUiComponentProperties(FlowuiComponentProperties flowUiComponentProperties) {
        this.shortcutCombination = KeyCombination.create(flowUiComponentProperties.getGridRemoveShortcut());
    }

    /**
     * @return whether to ask confirmation from the user
     */
    public boolean isConfirmation() {
        return confirmation;
    }

    /**
     * Sets whether to ask confirmation from the user.
     */
    public void setConfirmation(boolean confirmation) {
        this.confirmation = confirmation;
    }

    /**
     * Returns confirmation dialog text if it was set by {@link #setConfirmationText(String)} or in the view XML.
     * Otherwise, returns null.
     */
    @Nullable
    public String getConfirmationText() {
        return confirmationText;
    }

    /**
     * Sets confirmation dialog text.
     */
    public void setConfirmationText(@Nullable String confirmationText) {
        this.confirmationText = confirmationText;
    }

    /**
     * Returns confirmation dialog header if it was set by {@link #setConfirmationHeader(String)} or in the view XML.
     * Otherwise, returns null.
     */
    @Nullable
    public String getConfirmationHeader() {
        return confirmationHeader;
    }

    /**
     * Sets confirmation dialog header.
     */
    public void setConfirmationHeader(@Nullable String confirmationHeader) {
        this.confirmationHeader = confirmationHeader;
    }

    /**
     * Sets the handler to be invoked after removing entities.
     */
    public void setAfterActionPerformedHandler(@Nullable Consumer<AfterActionPerformedEvent<E>> afterActionPerformedHandler) {
        this.afterActionPerformedHandler = afterActionPerformedHandler;
    }

    /**
     * Sets the handler to be invoked if the action was cancelled by the user.
     */
    public void setActionCancelledHandler(@Nullable Consumer<ActionCancelledEvent<E>> actionCancelledHandler) {
        this.actionCancelledHandler = actionCancelledHandler;
    }

    @Override
    protected boolean isPermitted() {
        return checkExcludePermission() && super.isPermitted();
    }

    protected boolean checkExcludePermission() {
        if (target == null || !(target.getItems() instanceof ContainerDataUnit)) {
            return false;
        }

        ContainerDataUnit<E> containerDataUnit = (ContainerDataUnit<E>) target.getItems();

        MetaClass metaClass = containerDataUnit.getEntityMetaClass();
        if (metaClass == null) {
            return false;
        }

        if (containerDataUnit.getContainer() instanceof Nested) {
            Nested nestedContainer = (Nested) containerDataUnit.getContainer();

            MetaClass masterMetaClass = nestedContainer.getMaster().getEntityMetaClass();
            MetaProperty metaProperty = masterMetaClass.getProperty(nestedContainer.getProperty());

            FlowuiEntityAttributeContext entityAttributeContext =
                    new FlowuiEntityAttributeContext(masterMetaClass, metaProperty.getName());
            accessManager.applyRegisteredConstraints(entityAttributeContext);

            if (!entityAttributeContext.canModify()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void execute() {
        checkTarget();
        checkTargetItems(ContainerDataUnit.class);

        CollectionContainer container = ((ContainerDataUnit) target.getItems()).getContainer();
        if (container == null) {
            throw new IllegalStateException(String.format("%s target is not bound to %s",
                    getClass().getSimpleName(), CollectionContainer.class.getSimpleName()));
        }

        RemoveOperation.RemoveBuilder<E> builder = removeOperation.builder(target)
                .withConfirmation(confirmation);

        if (confirmationText != null) {
            builder = builder.withConfirmationMessage(confirmationText);
        }

        if (confirmationHeader != null) {
            builder = builder.withConfirmationTitle(confirmationHeader);
        }

        if (afterActionPerformedHandler != null) {
            builder = builder.afterActionPerformed(afterActionPerformedHandler);
        }

        if (actionCancelledHandler != null) {
            builder = builder.onCancel(actionCancelledHandler);
        }

        builder.exclude();
    }

    public ExcludeAction<E> withConfirmation(boolean confirmation) {
        setConfirmation(confirmation);
        return this;
    }

    public ExcludeAction<E> withConfirmationText(@Nullable String confirmationText) {
        setConfirmationText(confirmationText);
        return this;
    }

    public ExcludeAction<E> withConfirmationHeader(@Nullable String confirmationHeader) {
        setConfirmationHeader(confirmationHeader);
        return this;
    }

    public ExcludeAction<E> withAfterActionPerformedHandler(
            @Nullable Consumer<AfterActionPerformedEvent<E>> afterActionPerformedHandler) {
        setAfterActionPerformedHandler(afterActionPerformedHandler);
        return this;
    }

    public ExcludeAction<E> withAfterActionCancelledHandler(
            @Nullable Consumer<ActionCancelledEvent<E>> actionCancelledHandler) {
        setActionCancelledHandler(actionCancelledHandler);
        return this;
    }
}
