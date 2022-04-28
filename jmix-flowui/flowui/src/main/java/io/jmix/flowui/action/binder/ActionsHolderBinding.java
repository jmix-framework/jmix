package io.jmix.flowui.action.binder;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.kit.action.Action;

public interface ActionsHolderBinding<H extends Component, A extends Action, C extends Component>
        extends ActionBinding<C, A> {

    H getHolder();
}
