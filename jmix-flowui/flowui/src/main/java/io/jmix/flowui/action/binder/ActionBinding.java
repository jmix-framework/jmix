package io.jmix.flowui.action.binder;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.kit.action.Action;

public interface ActionBinding<C extends Component, A extends Action> {

    C getComponent();

    A getAction();

    void unbind();
}
