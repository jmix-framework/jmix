package io.jmix.flowui.component.delegate.impl;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.action.binder.ActionBinder;
import io.jmix.flowui.action.binder.ActionBinding;
import io.jmix.flowui.component.delegate.HasActionDelegate;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.HasAction;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiFunction;

@org.springframework.stereotype.Component("flowui_BaseHasActionDelegate")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BaseHasActionDelegate<C extends Component & HasAction> implements HasActionDelegate<C> {

    protected ActionBinder<C> actionBinder;

    public BaseHasActionDelegate(ActionBinder<C> actionBinder) {
        this.actionBinder = actionBinder;
    }

    @Nullable
    @Override
    public Action getAction() {
        return getActionBinding()
                .map(ActionBinding::getAction)
                .orElse(null);
    }

    @Override
    public void setAction(@Nullable Action action,
                          BiFunction<C, ComponentEventListener, Registration> handler,
                          boolean overrideComponentProperties) {
        getActionBinding()
                .filter(binding -> action == null || binding.getAction() != action)
                .ifPresent(ActionBinding::unbind);

        if (action != null && getActionBinding().isEmpty()) {
            createActionBinding(action, handler, overrideComponentProperties);
        }
    }

    protected Optional<ActionBinding> getActionBinding() {
        Collection<Action> actions = actionBinder.getActions();
        if (actions.isEmpty()) {
            return Optional.empty();
        }

        return actionBinder.getBindings(actions.iterator().next()).stream().findFirst();
    }

    protected void createActionBinding(Action action,
                                       BiFunction<C, ComponentEventListener, Registration> handler,
                                       boolean overrideComponentProperties) {
        actionBinder.createActionBinding(action, handler, overrideComponentProperties);
    }
}
