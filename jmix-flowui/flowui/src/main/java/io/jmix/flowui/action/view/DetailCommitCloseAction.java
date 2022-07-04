package io.jmix.flowui.action.view;

import io.jmix.flowui.FlowUiViewProperties;
import io.jmix.flowui.action.ActionType;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.Messages;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.component.FlowUiComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.view.PessimisticLockStatus;
import io.jmix.flowui.view.StandardDetailView;
import org.springframework.beans.factory.annotation.Autowired;

@ActionType(DetailCommitCloseAction.ID)
public class DetailCommitCloseAction<E>
        extends OperationResultViewAction<DetailCommitCloseAction<E>, StandardDetailView<E>> {

    public static final String ID = "detail_commitClose";

    public DetailCommitCloseAction() {
        this(ID);
    }

    public DetailCommitCloseAction(String id) {
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
    protected void setFlowUiViewProperties(FlowUiViewProperties flowUiViewProperties) {
        this.shortcutCombination = KeyCombination.create(flowUiViewProperties.getCommitShortcut());
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
