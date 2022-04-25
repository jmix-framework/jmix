package io.jmix.flowui.action.binder;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.kit.action.Action;

public interface ShortcutActionsHolderBinding<H extends Component, A extends Action, C extends Component>
        extends ActionsHolderBinding<H, A, C>, ShortcutActionBinding<C, A> {
}
