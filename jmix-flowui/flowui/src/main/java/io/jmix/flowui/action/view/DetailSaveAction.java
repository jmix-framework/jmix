package io.jmix.flowui.action.view;

import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.Messages;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import io.jmix.flowui.view.PessimisticLockStatus;
import io.jmix.flowui.view.StandardDetailView;
import org.springframework.beans.factory.annotation.Autowired;

@ActionType(DetailSaveAction.ID)
public class DetailSaveAction<E> extends OperationResultViewAction<DetailSaveAction<E>, StandardDetailView<E>> {

    public static final String ID = "detail_save";

    public DetailSaveAction() {
        this(ID);
    }

    public DetailSaveAction(String id) {
        super(id);
    }

    @Override
    protected void initAction() {
        super.initAction();

        this.icon = FlowuiComponentUtils.convertToIcon(VaadinIcon.ARCHIVE);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.Save");
    }

    @Override
    public void execute() {
        checkTarget();

        operationResult = target.save();

        super.execute();
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable() && target.getPessimisticLockStatus() != PessimisticLockStatus.FAILED;
    }
}
