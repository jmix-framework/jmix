package io.jmix.flowui.action.binder.component;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.action.binder.ActionBinder;
import io.jmix.flowui.action.binder.ShortcutActionHandler;
import io.jmix.flowui.action.binder.ShortcutActionsHolderBinding;
import io.jmix.flowui.kit.action.Action;

public interface ComponentShortcutActionsHolderBinder<C extends Component> {

    boolean supports(Component component);

    <H extends Component, A extends Action> ShortcutActionsHolderBinding<H, A, C> bindShortcut(ActionBinder<H> binder,
                                                                                               A action,
                                                                                               C component,
                                                                                               ShortcutActionHandler<C> actionHandler);
}
