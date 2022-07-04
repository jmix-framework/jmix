package io.jmix.flowui.action.view;

import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.Messages;
import io.jmix.flowui.FlowUiViewProperties;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.kit.component.FlowUiComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.view.StandardListView;
import org.springframework.beans.factory.annotation.Autowired;

@ActionType(LookupDiscardAction.ID)
public class LookupDiscardAction<E> extends OperationResultViewAction<LookupDiscardAction<E>, StandardListView<E>> {

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
    protected void setFlowUiViewProperties(FlowUiViewProperties flowUiViewProperties) {
        this.shortcutCombination = KeyCombination.create(flowUiViewProperties.getCloseShortcut());
    }

    @Override
    public void execute() {
        checkTarget();

        operationResult = target.closeWithDiscard();

        super.execute();
    }
}
