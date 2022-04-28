package io.jmix.flowui.action.binder.component;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.kit.action.Action;

public interface ComponentActionsHolderUnbinder<C extends Component> {

    boolean supports(Component component);

    <H extends Component, A extends Action> void unbind(H holder, A action, C component);
}
