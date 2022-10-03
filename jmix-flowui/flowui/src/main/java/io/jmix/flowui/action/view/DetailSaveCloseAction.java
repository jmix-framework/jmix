package io.jmix.flowui.action.view;

import io.jmix.flowui.FlowuiViewProperties;
import io.jmix.flowui.action.ActionType;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.Messages;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.view.PessimisticLockStatus;
import io.jmix.flowui.view.StandardDetailView;
import org.springframework.beans.factory.annotation.Autowired;

@ActionType(DetailSaveCloseAction.ID)
public class DetailSaveCloseAction<E>
        extends OperationResultViewAction<DetailSaveCloseAction<E>, StandardDetailView<E>> {

    public static final String ID = "detail_saveClose";

    public DetailSaveCloseAction() {
        this(ID);
    }

    public DetailSaveCloseAction(String id) {
        super(id);
    }

    @Override
    protected void initAction() {
        super.initAction();

        this.icon = FlowuiComponentUtils.convertToIcon(VaadinIcon.CHECK);
        this.variant = ActionVariant.PRIMARY;
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.Ok");
    }

    @Autowired
    protected void setFlowUiViewProperties(FlowuiViewProperties flowUiViewProperties) {
        this.shortcutCombination = KeyCombination.create(flowUiViewProperties.getSaveShortcut());
    }

    @Override
    public void execute() {
        checkTarget();

        operationResult = target.closeWithSave();

        super.execute();
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable() && target.getPessimisticLockStatus() != PessimisticLockStatus.FAILED;
    }
}
