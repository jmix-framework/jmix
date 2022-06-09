package io.jmix.flowui.component.valuepicker;

import com.vaadin.flow.component.HasElement;
import io.jmix.flowui.action.valuepicker.PickerAction;
import io.jmix.flowui.component.PickerComponent;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.valuepicker.ValuePickerActionSupport;

public class JmixValuePickerActionSupport extends ValuePickerActionSupport {

    public JmixValuePickerActionSupport(HasElement component) {
        super(component);
    }

    public JmixValuePickerActionSupport(PickerComponent<?> component,
                                        String actionsSlot, String hasActionsAttribute) {
        super(component, actionsSlot, hasActionsAttribute);
    }

    @Override
    protected void addActionInternal(Action action, int index) {
        super.addActionInternal(action, index);

        attachAction(action);
    }

    @Override
    protected void removeActionInternal(Action action) {
        super.removeActionInternal(action);

        detachAction(action);
    }

    @SuppressWarnings("unchecked")
    protected void attachAction(Action action) {
        if (action instanceof PickerAction) {
            ((PickerAction<?, PickerComponent<?>, ?>) action)
                    .setTarget(((PickerComponent<?>) component));
        }
    }

    @SuppressWarnings("unchecked")
    protected void detachAction(Action action) {
        if (action instanceof PickerAction) {
            ((PickerAction<?, PickerComponent<?>, ?>) action)
                    .setTarget(null);
        }
    }
}
