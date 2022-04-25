package io.jmix.flowui.action.binder;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.action.Action;

import javax.annotation.Nullable;
import java.util.List;

public class ShortcutActionsHolderBindingImpl<H extends Component, A extends Action, C extends Component>
        extends AbstractShortcutActionBindingImpl<H, A, C> implements ShortcutActionsHolderBinding<H, A, C> {

    protected final H holder;

    public ShortcutActionsHolderBindingImpl(ActionBinder<H> binder, H holder, A action, C component,
                                            ShortcutActionHandler<C> actionHandler,
                                            @Nullable List<Registration> registrations) {
        super(binder, action, component, actionHandler, registrations);
        this.holder = holder;
    }

    @Override
    public H getHolder() {
        return holder;
    }
}
