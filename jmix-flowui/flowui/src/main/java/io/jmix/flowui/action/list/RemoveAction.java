package io.jmix.flowui.action.list;

import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.Messages;
import io.jmix.core.security.EntityOp;
import io.jmix.flowui.FlowUiComponentProperties;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.AdjustWhenViewReadOnly;
import io.jmix.flowui.data.ContainerDataUnit;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.component.FlowUiComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.util.RemoveOperation;
import io.jmix.flowui.util.RemoveOperation.ActionCancelledEvent;
import io.jmix.flowui.util.RemoveOperation.AfterActionPerformedEvent;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@ActionType(RemoveAction.ID)
public class RemoveAction<E> extends SecuredListDataComponentAction<RemoveAction<E>, E>
        implements AdjustWhenViewReadOnly {

    public static final String ID = "remove";

    protected RemoveOperation removeOperation;

    protected boolean confirmation = true;
    protected String confirmationText;
    protected String confirmationHeader;
    protected Consumer<AfterActionPerformedEvent<E>> afterActionPerformedHandler;
    protected Consumer<ActionCancelledEvent<E>> actionCancelledHandler;

    public RemoveAction() {
        this(ID);
    }

    public RemoveAction(String id) {
        super(id);
    }

    @Override
    protected void initAction() {
        super.initAction();

        setConstraintEntityOp(EntityOp.DELETE);

        variant = ActionVariant.DANGER;
        icon = FlowUiComponentUtils.iconToSting(VaadinIcon.CLOSE);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.Remove");
    }

    @Autowired
    public void setRemoveOperation(RemoveOperation removeOperation) {
        this.removeOperation = removeOperation;
    }

    @Autowired
    protected void setFlowUiComponentProperties(FlowUiComponentProperties flowUiComponentProperties) {
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
        if (!checkRemovePermission()) {
            return false;
        }

        return super.isPermitted();
    }

    protected boolean checkRemovePermission() {
        // TODO: add security
/*        if (target == null || !(target.getItems() instanceof ContainerDataUnit)) {
            return false;
        }

        ContainerDataUnit<E> containerDataUnit = (ContainerDataUnit<E>) target.getItems();

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
        }*/

        return true;
    }

    /**
     * Executes the action.
     */
    @Override
    public void execute() {
        checkTarget();

        if (!(target.getItems() instanceof ContainerDataUnit)) {
            throw new IllegalStateException(String.format("%s target items is null or does not implement %s",
                    getClass().getSimpleName(), ContainerDataUnit.class.getSimpleName()));
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

        builder.remove();
    }

    public RemoveAction<E> withConfirmation(boolean confirmation) {
        setConfirmation(confirmation);
        return this;
    }

    public RemoveAction<E> withConfirmationText(@Nullable String confirmationText) {
        setConfirmationText(confirmationText);
        return this;
    }

    public RemoveAction<E> withConfirmationHeader(@Nullable String confirmationHeader) {
        setConfirmationHeader(confirmationHeader);
        return this;
    }

    public RemoveAction<E> withAfterActionPerformedHandler(
            @Nullable Consumer<AfterActionPerformedEvent<E>> afterActionPerformedHandler) {
        setAfterActionPerformedHandler(afterActionPerformedHandler);
        return this;
    }

    public RemoveAction<E> withAfterActionCancelledHandler(
            @Nullable Consumer<ActionCancelledEvent<E>> actionCancelledHandler) {
        setActionCancelledHandler(actionCancelledHandler);
        return this;
    }
}
