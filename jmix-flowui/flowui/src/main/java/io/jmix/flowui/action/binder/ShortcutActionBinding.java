package io.jmix.flowui.action.binder;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.KeyCombination;

import java.util.Optional;

public interface ShortcutActionBinding<C extends Component, A extends Action> extends ActionBinding<C, A> {

    Optional<KeyCombination> getKeyCombination();
}
