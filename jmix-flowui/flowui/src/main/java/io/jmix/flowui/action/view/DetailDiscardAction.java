package io.jmix.flowui.action.view;

import io.jmix.flowui.action.ActionType;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.Messages;
import io.jmix.flowui.kit.component.FlowUiComponentUtils;
import io.jmix.flowui.view.StandardDetailView;
import org.springframework.beans.factory.annotation.Autowired;

@ActionType(DetailDiscardAction.ID)
public class DetailDiscardAction<E> extends OperationResultViewAction<DetailDiscardAction<E>, StandardDetailView<E>> {

    public static final String ID = "detail_discard";

    public DetailDiscardAction() {
        this(ID);
    }

    public DetailDiscardAction(String id) {
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

    @Override
    public void execute() {
        checkTarget();

        operationResult = target.closeWithDiscard();

        super.execute();
    }
}
