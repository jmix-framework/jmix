package io.jmix.flowui.action.binder.component;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.action.binder.ActionBinder;
import io.jmix.flowui.action.binder.ShortcutActionBinding;
import io.jmix.flowui.action.binder.ShortcutActionHandler;
import io.jmix.flowui.kit.action.Action;

public interface ComponentShortcutActionBinder<C extends Component> {

    boolean supports(Component component);

    <A extends Action> ShortcutActionBinding<C, A> bindShortcut(ActionBinder<C> binder, A action,
                                                                ShortcutActionHandler<C> actionHandler,
                                                                boolean overrideComponentProperties);
}
