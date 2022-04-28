package io.jmix.flowui.action.screen;

import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.Messages;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.kit.component.FlowUiComponentUtils;
import io.jmix.flowui.screen.PessimisticLockStatus;
import io.jmix.flowui.screen.StandardEditor;
import org.springframework.beans.factory.annotation.Autowired;

@ActionType(EditorCommitAction.ID)
public class EditorCommitAction<E> extends OperationResultScreenAction<EditorCommitAction<E>, StandardEditor<E>> {

    public static final String ID = "editor_commit";

    public EditorCommitAction() {
        this(ID);
    }

    public EditorCommitAction(String id) {
        super(id);
    }

    @Override
    protected void initAction() {
        super.initAction();

        this.icon = FlowUiComponentUtils.iconToSting(VaadinIcon.ARCHIVE);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.Save");
    }

    @Override
    public void execute() {
        checkTarget();

        operationResult = target.commit();

        super.execute();
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable() && target.getPessimisticLockStatus() != PessimisticLockStatus.FAILED;
    }
}
