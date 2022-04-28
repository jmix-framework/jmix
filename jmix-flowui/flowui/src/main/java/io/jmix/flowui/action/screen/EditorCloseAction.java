package io.jmix.flowui.action.screen;

import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.Messages;
import io.jmix.flowui.FlowUiScreenProperties;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.kit.component.FlowUiComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.screen.StandardEditor;
import io.jmix.flowui.screen.StandardOutcome;
import io.jmix.flowui.screen.UiControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;

@ActionType(EditorCloseAction.ID)
public class EditorCloseAction<E> extends OperationResultScreenAction<EditorCloseAction<E>, StandardEditor<E>> {

    public static final String ID = "editor_close";

    public EditorCloseAction() {
        this(ID);
    }

    public EditorCloseAction(String id) {
        super(id);
    }

    @Override
    protected void initAction() {
        super.initAction();

        this.icon = FlowUiComponentUtils.iconToSting(VaadinIcon.BAN);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.Cancel");
    }

    @Autowired
    protected void setFlowUiScreenProperties(FlowUiScreenProperties flowUiScreenProperties) {
        this.shortcutCombination = KeyCombination.create(flowUiScreenProperties.getCloseShortcut());
    }

    @Override
    public void execute() {
        checkTarget();

        operationResult = target.close(UiControllerUtils.isCommitActionPerformed(target)
                ? StandardOutcome.COMMIT
                : StandardOutcome.CLOSE);

        super.execute();
    }
}
