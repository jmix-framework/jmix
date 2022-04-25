package io.jmix.flowui.action.binder;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.kit.action.Action;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component("flowui_ActionBinder")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ActionBinder<H extends Component> {

    protected final H holder;
    protected final List<Action> actions = new ArrayList<>();
    protected final Multimap<Action, ActionBinding> bindingsMap = ArrayListMultimap.create();

    protected ActionBindingProcessor actionBindingProcessor;
    protected ActionsHolderBindingProcessor actionsHolderProcessor;

    protected ActionBinder(H holder) {
        this.holder = holder;
    }

    @Autowired
    public void setActionBindingProcessor(ActionBindingProcessor actionBindingProcessor) {
        this.actionBindingProcessor = actionBindingProcessor;
    }

    @Autowired
    public void setActionsHolderProcessor(ActionsHolderBindingProcessor actionsHolderProcessor) {
        this.actionsHolderProcessor = actionsHolderProcessor;
    }

    public H getHolder() {
        return holder;
    }

    public void addAction(Action action) {
        addAction(action, actions.size());
    }

    public void addAction(Action action, int index) {
        Preconditions.checkNotNullArgument(action, "Action must be non null");

        addActionInternal(action, index);
    }

    public void removeAction(Action action) {
        Preconditions.checkNotNullArgument(action, "Action must be non null");

        getBindings(action).forEach(ActionBinding::unbind);
        actions.remove(action);
    }

    public Optional<Action> getAction(String id) {
        return getActions().stream()
                .filter(action -> Objects.equals(action.getId(), id))
                .findFirst();
    }

    public int indexOf(Action action) {
        return ImmutableList.copyOf(actions).indexOf(action);
    }

    public Collection<Action> getActions() {
        return ImmutableList.copyOf(actions);
    }

    public Collection<ActionBinding> getBindings(Action action) {
        if (!actions.contains(action)) {
            return Collections.emptyList();
        }

        return ImmutableList.copyOf(bindingsMap.get(action));
    }

    public Collection<ActionBinding> getBindings(Component component) {
        return ImmutableList.copyOf(bindingsMap.asMap().values().stream()
                .flatMap(Collection::stream)
                .filter(binding -> binding.getComponent().equals(component))
                .collect(Collectors.toList()));
    }

    public <A extends Action> ActionBinding<H, A> createActionBinding(A action,
                                                                      BiFunction<H, ComponentEventListener, Registration> handler) {
        return createActionBinding(action, handler, true);
    }

    public <A extends Action> ActionBinding<H, A> createActionBinding(A action,
                                                                      BiFunction<H, ComponentEventListener, Registration> handler,
                                                                      boolean overrideComponentProperties) {
        Preconditions.checkNotNullArgument(action, "Action must be non null");
        Preconditions.checkNotNullArgument(handler, "Handler must be non null");

        ActionBinding<H, A> binding = actionBindingProcessor.bind(action, this, handler, overrideComponentProperties);
        addBindingInternal(binding, actions.size());
        return binding;
    }

    public <A extends Action> ShortcutActionBinding<H, A> createShortcutActionBinding(A action,
                                                                                      ShortcutActionHandler<H> handler) {
        return createShortcutActionBinding(action, handler, true);
    }

    public <A extends Action> ShortcutActionBinding<H, A> createShortcutActionBinding(A action,
                                                                                      ShortcutActionHandler<H> handler,
                                                                                      boolean overrideComponentProperties) {
        Preconditions.checkNotNullArgument(action, "Action must be non null");
        Preconditions.checkNotNullArgument(handler, "Handler must be non null");

        ShortcutActionBinding<H, A> binding = actionBindingProcessor.bindShortcut(action, this, handler, overrideComponentProperties);
        addBindingInternal(binding, actions.size());
        return binding;
    }

    public <A extends Action, C extends Component> ActionsHolderBinding<H, A, C> createActionsHolderBinding(A action,
                                                                                                            C component,
                                                                                                            BiFunction<C, ComponentEventListener, Registration> handler) {
        return createActionsHolderBinding(action, component, handler, actions.size());
    }

    public <A extends Action, C extends Component> ActionsHolderBinding<H, A, C> createActionsHolderBinding(A action,
                                                                                                            C component,
                                                                                                            BiFunction<C, ComponentEventListener, Registration> handler,
                                                                                                            int index) {
        Preconditions.checkNotNullArgument(action, "Action must be non null");
        Preconditions.checkNotNullArgument(component, "Component must be non null");
        Preconditions.checkNotNullArgument(handler, "Handler must be non null");

        ActionsHolderBinding<H, A, C> binding = actionsHolderProcessor.bind(this, action, component, handler);
        addBindingInternal(binding, index);
        return binding;
    }

    public <A extends Action, C extends Component> ShortcutActionsHolderBinding<H, A, C> createShortcutActionsHolderBinding(A action,
                                                                                                                            C component,
                                                                                                                            ShortcutActionHandler<C> handler) {
        return createShortcutActionsHolderBinding(action, component, handler, actions.size());
    }

    public <A extends Action, C extends Component> ShortcutActionsHolderBinding<H, A, C> createShortcutActionsHolderBinding(A action,
                                                                                                                            C component,
                                                                                                                            ShortcutActionHandler<C> handler,
                                                                                                                            int index) {
        Preconditions.checkNotNullArgument(action, "Action must be non null");
        Preconditions.checkNotNullArgument(component, "Component must be non null");
        Preconditions.checkNotNullArgument(handler, "Handler must be non null");

        ShortcutActionsHolderBinding<H, A, C> binding = actionsHolderProcessor.bindShortcut(this, action, component, handler);
        addBindingInternal(binding, index);
        return binding;
    }

    protected void addBindingInternal(ActionBinding binding, int index) {
        Action action = binding.getAction();
        addActionInternal(action, index);
        bindingsMap.put(action, binding);
    }

    protected void addActionInternal(Action action, int index) {
        int oldIndex = findActionIndexById(action.getId());
        if (oldIndex >= 0) {
            Action oldAction = actions.get(oldIndex);
            if (oldAction == action) {
                return;
            }

            removeAction(oldAction);
            if (index > actions.size()) {
                index--;
            }
        }

        actions.add(index, action);
    }

    protected int findActionIndexById(String actionId) {
        int index = -1;
        for (int i = 0; i < actions.size(); i++) {
            Action a = actions.get(i);
            if (Objects.equals(a.getId(), actionId)) {
                index = i;
                break;
            }
        }
        return index;
    }

    protected void removeBindingInternal(ActionBinding binding) {
        Action action = binding.getAction();
        if (actions.contains(action)) {
            if (binding instanceof ActionsHolderBinding) {
                actionsHolderProcessor.unbind((ActionsHolderBinding) binding);
            } else {
                actionBindingProcessor.unbind(binding);
            }

            bindingsMap.remove(action, binding);

            if (getBindings(action).isEmpty()) {
                actions.remove(action);
            }
        }
    }
}
