package io.jmix.flowui.action.view;

import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.Messages;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.kit.component.FlowUiComponentUtils;
import io.jmix.flowui.view.PessimisticLockStatus;
import io.jmix.flowui.view.StandardDetailView;
import org.springframework.beans.factory.annotation.Autowired;

@ActionType(DetailEnableEditingAction.ID)
public class DetailEnableEditingAction<E>
        extends OperationResultViewAction<DetailEnableEditingAction<E>, StandardDetailView<E>> {

    public static final String ID = "detail_enableEditing";

    public DetailEnableEditingAction() {
        this(ID);
    }

    public DetailEnableEditingAction(String id) {
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
