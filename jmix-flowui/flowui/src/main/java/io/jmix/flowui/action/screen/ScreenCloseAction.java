package io.jmix.flowui.action.screen;

import io.jmix.flowui.FlowUiScreenProperties;
import io.jmix.flowui.action.ActionType;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.Messages;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.kit.component.FlowUiComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.screen.Screen;
import io.jmix.flowui.screen.StandardOutcome;
import org.springframework.beans.factory.annotation.Autowired;

@ActionType(ScreenCloseAction.ID)
public class ScreenCloseAction extends OperationResultScreenAction<ScreenCloseAction, Screen> {

    public static final String ID = "screen_close";

    protected StandardOutcome outcome = StandardOutcome.CLOSE;

    public ScreenCloseAction() {
        this(ID);
    }

    public ScreenCloseAction(String id) {
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

    public void setOutcome(StandardOutcome outcome) {
        this.outcome = outcome;
    }

    public ScreenCloseAction withOutcome(StandardOutcome outcome) {
        Preconditions.checkNotNullArgument(outcome);
        setOutcome(outcome);
        return this;
    }

    @Override
    public void execute() {
        checkTarget();

        operationResult = target.close(outcome);

        super.execute();
    }
}
