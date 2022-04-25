package io.jmix.flowui.action.screen;

import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.Messages;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.kit.component.FlowUiComponentUtils;
import io.jmix.flowui.screen.PessimisticLockStatus;
import io.jmix.flowui.screen.StandardEditor;
import org.springframework.beans.factory.annotation.Autowired;

@ActionType(EditorEnableEditingAction.ID)
public class EditorEnableEditingAction<E>
        extends OperationResultScreenAction<EditorEnableEditingAction<E>, StandardEditor<E>> {

    public static final String ID = "editor_enableEditing";

    public EditorEnableEditingAction() {
        this(ID);
    }

    public EditorEnableEditingAction(String id) {
        super(id);
    }

    @Override
    protected void initAction() {
        super.initAction();

        this.icon = FlowUiComponentUtils.iconToSting(VaadinIcon.PENCIL);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.EnableEditing");
    }

    @Override
    public void execute() {
        checkTarget();

        target.setReadOnly(false);

        super.execute();
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable()
                && target.isReadOnly()
                && target.getPessimisticLockStatus() != PessimisticLockStatus.FAILED;
    }
}
