package io.jmix.flowui.action.binder;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.KeyCombination;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public abstract class AbstractShortcutActionBindingImpl<H extends Component, A extends Action, C extends Component>
        extends AbstractActionBindingImpl<H, A, C> implements ShortcutActionBinding<C, A> {

    protected ShortcutRegistration shortcutRegistration;
    protected KeyCombination shortcutCombination;

    public AbstractShortcutActionBindingImpl(ActionBinder<H> binder,
                                             A action,
                                             C component,
                                             ShortcutActionHandler<C> actionHandler,
                                             @Nullable List<Registration> registrations) {
        super(binder, action, component, registrations);

        addShortcutRegistrationIfNeeded(action.getShortcutCombination(), actionHandler);

        this.registrations.add(action.addPropertyChangeListener(propertyChangeEvent -> {
            if (Action.PROP_SHORTCUT.equals(propertyChangeEvent.getPropertyName())) {
                if (shortcutRegistration != null) {
                    this.registrations.remove(shortcutRegistration);
                    shortcutRegistration.remove();
                    shortcutRegistration = null;
                    shortcutCombination = null;
                }

                KeyCombination newShortcutCombination = (KeyCombination) propertyChangeEvent.getNewValue();
                addShortcutRegistrationIfNeeded(newShortcutCombination, actionHandler);
            }
        }));
    }

    protected void addShortcutRegistrationIfNeeded(@Nullable KeyCombination shortcutCombination,
                                                   ShortcutActionHandler<C> actionHandler) {
        if (shortcutCombination != null) {
            shortcutRegistration =
                    actionHandler.handle(component, __ -> action.actionPerform(component), shortcutCombination);
            this.shortcutCombination = shortcutCombination;
            this.registrations.add(shortcutRegistration);
        }
    }

    @Override
    public Optional<KeyCombination> getKeyCombination() {
        return Optional.ofNullable(shortcutCombination);
    }
}
