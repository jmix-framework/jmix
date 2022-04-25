package io.jmix.flowui.action.binder;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.action.Action;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiFunction;

public class ActionsHolderBindingImpl<H extends Component, A extends Action, C extends Component>
        extends AbstractActionBindingImpl<H, A, C> implements ActionsHolderBinding<H, A, C> {

    protected final H holder;

    public ActionsHolderBindingImpl(ActionBinder<H> binder, H holder, A action, C component,
                                    BiFunction<C, ComponentEventListener, Registration> actionHandler,
                                    @Nullable List<Registration> registrations) {
        super(binder, action, component, registrations);
        this.holder = holder;
        this.registrations.add(actionHandler.apply(component, __ -> action.actionPerform(component)));
    }

    @Override
    public H getHolder() {
        return holder;
    }
}
