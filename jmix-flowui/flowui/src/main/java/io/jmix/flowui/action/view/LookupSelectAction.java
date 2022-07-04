package io.jmix.flowui.action.view;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.Messages;
import io.jmix.flowui.FlowUiViewProperties;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.component.FlowUiComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.kit.component.SelectionChangeNotifier;
import io.jmix.flowui.view.StandardListView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;

@ActionType(LookupSelectAction.ID)
public class LookupSelectAction<E> extends OperationResultViewAction<LookupSelectAction<E>, StandardListView<E>> {

    private static final Logger log = LoggerFactory.getLogger(LookupSelectAction.class);

    public static final String ID = "lookup_select";

    protected Registration selectionListenerRegistration;

    public LookupSelectAction() {
        this(ID);
    }

    public LookupSelectAction(String id) {
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
        this.text = messages.getMessage("actions.Select");
    }

    @Autowired
    protected void setFlowUiViewProperties(FlowUiViewProperties flowUiViewProperties) {
        this.shortcutCombination = KeyCombination.create(flowUiViewProperties.getCommitShortcut());
    }

    @Override
    public void setTarget(@Nullable StandardListView<E> target) {
        super.setTarget(target);

        attachSelectionListener();
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable()
                && hasSelectedItems();
    }

    protected boolean hasSelectedItems() {
        return target.findLookupComponent()
                .map(comp -> !comp.getSelectedItems().isEmpty())
                .orElse(false);
    }

    @Override
    public void execute() {
        checkTarget();

        operationResult = target.handleSelection();

        super.execute();
    }

    @SuppressWarnings("unchecked")
    protected void attachSelectionListener() {
        if (selectionListenerRegistration != null) {
            selectionListenerRegistration.remove();
            selectionListenerRegistration = null;
        }

        target.findLookupComponent().ifPresentOrElse(
                lookupComponent -> {
                    if (lookupComponent instanceof SelectionChangeNotifier) {
                        selectionListenerRegistration = ((SelectionChangeNotifier<?, E>) lookupComponent)
                                .addSelectionListener(this::onSelectionChange);
                    } else if (lookupComponent instanceof HasValue) {
                        selectionListenerRegistration = ((HasValue<?, E>) lookupComponent)
                                .addValueChangeListener(this::onValueChange);
                    }
                }, () -> log.info("{} does not have lookup component", target.getClass().getName()));
    }

    protected void onSelectionChange(SelectionEvent<?, E> event) {
        refreshState();
    }

    protected void onValueChange(HasValue.ValueChangeEvent<E> event) {
        refreshState();
    }
}
