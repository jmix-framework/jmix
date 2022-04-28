package io.jmix.flowui.component.delegate;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.HasAction;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

public interface HasActionDelegate<C extends Component & HasAction> {

    @Nullable
    Action getAction();

    void setAction(@Nullable Action action,
                   BiFunction<C, ComponentEventListener, Registration> handler,
                   boolean overrideComponentProperties);
}
