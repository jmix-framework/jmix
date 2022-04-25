package io.jmix.flowui.component.delegate;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.component.HasActions;
import io.jmix.flowui.kit.action.Action;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public interface HasActionsDelegate<H extends Component & HasActions> {

    void addAction(Action action, int index);

    void removeAction(Action action);

    Collection<Action> getActions();

    Optional<Action> getAction(String id);

    <C extends Component> void addBinding(Action action,
                                          int index,
                                          Class<C> componentClass,
                                          BiFunction<C, ComponentEventListener, Registration> handler,
                                          Function<Integer, C> createComponentHandler,
                                          @Nullable Consumer<C> removeComponentHandler);

    <C extends Component> List<C> getComponentsByAction(Class<C> componentClass, Action action);
}
