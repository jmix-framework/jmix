package io.jmix.flowui.action.binder.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.action.binder.ActionBinder;
import io.jmix.flowui.action.binder.ActionsHolderBinding;
import io.jmix.flowui.kit.action.Action;

import java.util.function.BiFunction;

public interface ComponentActionsHolderBinder<C extends Component> {

    boolean supports(Component component);

    <H extends Component, A extends Action> ActionsHolderBinding<H, A, C> bind(ActionBinder<H> binder,
                                                                               A action,
                                                                               C component,
                                                                               BiFunction<C, ComponentEventListener, Registration> actionHandler);
}
