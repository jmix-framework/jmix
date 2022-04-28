package io.jmix.flowui.action.screen;

import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.Messages;
import io.jmix.flowui.FlowUiScreenProperties;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.kit.component.FlowUiComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.screen.StandardLookup;
import org.springframework.beans.factory.annotation.Autowired;

@ActionType(LookupDiscardAction.ID)
public class LookupDiscardAction<E> extends OperationResultScreenAction<LookupDiscardAction<E>, StandardLookup<E>> {

    public static final String ID = "lookup_discard";

    public LookupDiscardAction() {
        this(ID);
    }

    public LookupDiscardAction(String id) {
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

        operationResult = target.closeWithDiscard();

        super.execute();
    }
}
