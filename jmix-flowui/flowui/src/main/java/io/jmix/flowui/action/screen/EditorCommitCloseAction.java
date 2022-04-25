package io.jmix.flowui.action.screen;

import io.jmix.flowui.FlowUiScreenProperties;
import io.jmix.flowui.action.ActionType;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.Messages;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.component.FlowUiComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.screen.PessimisticLockStatus;
import io.jmix.flowui.screen.StandardEditor;
import org.springframework.beans.factory.annotation.Autowired;

@ActionType(EditorCommitCloseAction.ID)
public class EditorCommitCloseAction<E>
        extends OperationResultScreenAction<EditorCommitCloseAction<E>, StandardEditor<E>> {

    public static final String ID = "editor_commitClose";

    public EditorCommitCloseAction() {
        this(ID);
    }

    public EditorCommitCloseAction(String id) {
        super(id);
    }

    @Override
    protected void initAction() {
        super.initAction();

        this.icon = FlowUiComponentUtils.iconToSting(VaadinIcon.CHECK);
        this.variant = ActionVariant.PRIMARY;
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.Ok");
    }

    @Autowired
    protected void setFlowUiScreenProperties(FlowUiScreenProperties flowUiScreenProperties) {
        this.shortcutCombination = KeyCombination.create(flowUiScreenProperties.getCommitShortcut());
    }

    @Override
    public void execute() {
        checkTarget();

        operationResult = target.closeWithCommit();

        super.execute();
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable() && target.getPessimisticLockStatus() != PessimisticLockStatus.FAILED;
    }
}
