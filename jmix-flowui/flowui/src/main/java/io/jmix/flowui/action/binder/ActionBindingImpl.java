package io.jmix.flowui.action.binder;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.action.Action;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiFunction;

public class ActionBindingImpl<C extends Component, A extends Action>
        extends AbstractActionBindingImpl<C, A, C> {

    public ActionBindingImpl(ActionBinder<C> binder,
                             A action,
                             C component,
                             BiFunction<C, ComponentEventListener, Registration> actionHandler,
                             @Nullable List<Registration> registrations) {
        super(binder, action, component, registrations);
        this.registrations.add(actionHandler.apply(component, __ -> action.actionPerform(component)));
    }
}
