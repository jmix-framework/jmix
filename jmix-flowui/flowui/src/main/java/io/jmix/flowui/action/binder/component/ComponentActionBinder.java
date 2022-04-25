package io.jmix.flowui.action.binder.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.action.binder.ActionBinder;
import io.jmix.flowui.action.binder.ActionBinding;
import io.jmix.flowui.kit.action.Action;

import java.util.function.BiFunction;

@SuppressWarnings("rawtypes")
public interface ComponentActionBinder<C extends Component> {

    boolean supports(Component component);

    <A extends Action> ActionBinding<C, A> bind(ActionBinder<C> binder, A action,
                                                BiFunction<C, ComponentEventListener, Registration> actionHandler,
                                                boolean overrideComponentProperties);
}
