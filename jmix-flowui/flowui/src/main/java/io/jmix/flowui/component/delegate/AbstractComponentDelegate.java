package io.jmix.flowui.component.delegate;

import com.vaadin.flow.component.Component;

public abstract class AbstractComponentDelegate<C extends Component> {

    protected C component;

    public AbstractComponentDelegate(C component) {
        this.component = component;
    }

    public C getComponent() {
        return component;
    }
}
