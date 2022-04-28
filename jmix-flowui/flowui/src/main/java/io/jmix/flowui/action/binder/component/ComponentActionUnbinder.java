package io.jmix.flowui.action.binder.component;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.kit.action.Action;

public interface ComponentActionUnbinder<C extends Component> {

    boolean supports(Component component);

    <A extends Action> void unbind(C component, A action);
}
