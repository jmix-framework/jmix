package io.jmix.flowui.action.binder;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ShortcutEventListener;
import com.vaadin.flow.component.ShortcutRegistration;
import io.jmix.flowui.kit.component.KeyCombination;

@FunctionalInterface
public interface ShortcutActionHandler<C extends Component> {

    ShortcutRegistration handle(C component, ShortcutEventListener listener, KeyCombination keyCombination);
}
