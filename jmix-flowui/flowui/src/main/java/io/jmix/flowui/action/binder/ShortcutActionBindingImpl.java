package io.jmix.flowui.action.binder;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.action.Action;

import javax.annotation.Nullable;
import java.util.List;

public class ShortcutActionBindingImpl<C extends Component, A extends Action>
        extends AbstractShortcutActionBindingImpl<C, A, C> {

    public ShortcutActionBindingImpl(ActionBinder<C> binder,
                                     A action,
                                     C component,
                                     ShortcutActionHandler<C> actionHandler,
                                     @Nullable List<Registration> registrations) {
        super(binder, action, component, actionHandler, registrations);
    }
}
