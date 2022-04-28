package io.jmix.flowui.component.delegate.impl;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.action.binder.ActionBinder;
import io.jmix.flowui.action.binder.ActionBinding;
import io.jmix.flowui.component.HasActions;
import io.jmix.flowui.component.delegate.HasActionsDelegate;
import io.jmix.flowui.kit.action.Action;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component("flowui_BaseHasActionsDelegate")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BaseHasActionsDelegate<H extends Component & HasActions> implements HasActionsDelegate<H> {

    protected ActionBinder<H> actionBinder;

    public BaseHasActionsDelegate(ActionBinder<H> actionBinder) {
        this.actionBinder = actionBinder;
    }

    @Override
    public void addAction(Action action, int index) {
        actionBinder.addAction(action, index);
    }

    @Override
    public void removeAction(Action action) {
        actionBinder.removeAction(action);
    }

    @Override
    public Collection<Action> getActions() {
        return actionBinder.getActions();
    }

    @Override
    public Optional<Action> getAction(String id) {
        return actionBinder.getAction(id);
    }

    @Override
    public <C extends Component> void addBinding(Action action, int index,
                                                 Class<C> componentClass, BiFunction<C, ComponentEventListener, Registration> handler,
                                                 Function<Integer, C> createComponentHandler,
                                                 @Nullable Consumer<C> removeComponentHandler) {
        if (removeComponentHandler != null) {
            getAction(action.getId())
                    .filter(oldAction -> oldAction != action)
                    .ifPresent(oldAction ->
                            getComponentsByAction(componentClass, oldAction).forEach(removeComponentHandler));
        }

        addAction(action, index);
        int newIndex = actionBinder.indexOf(action);
        actionBinder.createActionsHolderBinding(action, createComponentHandler.apply(newIndex), handler, index);
    }

    @Override
    public <C extends Component> List<C> getComponentsByAction(Class<C> componentClass, Action action) {
        return actionBinder.getBindings(action).stream()
                .map(ActionBinding::getComponent)
                .filter(component -> componentClass.isAssignableFrom(component.getClass()))
                .map(component -> (C) component)
                .collect(Collectors.toList());
    }
}
