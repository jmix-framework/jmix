package io.jmix.flowui.kit.component.valuepicker;

import com.google.common.base.Strings;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.action.Action;

import javax.annotation.Nullable;
import java.util.Objects;

public class ValuePickerButtonActionSupport {

    protected final ValuePickerButton button;

    protected Action action;

    protected Registration registration;
    protected Registration actionPropertyChangeRegistration;

    public ValuePickerButtonActionSupport(ValuePickerButton button) {
        this.button = button;
    }

    public void setAction(@Nullable Action action, boolean overrideComponentProperties) {
        if (Objects.equals(this.action, action)) {
            return;
        }

        removeRegistrations();

        this.action = action;

        if (action != null && overrideComponentProperties) {
            button.setTitle(generateTitle(action));
            button.setEnabled(action.isEnabled());
            button.setVisible(action.isVisible());
            button.setShortcutCombination(action.getShortcutCombination());
            updateIcon(action.getIcon());

            registration = button.addClickListener(event -> action.actionPerform(event.getSource()));
            actionPropertyChangeRegistration = addPropertyChangeListener();
        }
    }

    @Nullable
    public Action getAction() {
        return action;
    }

    protected void removeRegistrations() {
        if (action != null) {
            if (registration != null) {
                registration.remove();
                registration = null;
            }

            if (actionPropertyChangeRegistration != null) {
                actionPropertyChangeRegistration.remove();
                actionPropertyChangeRegistration = null;
            }
        }
    }

    protected Registration addPropertyChangeListener() {
        return action.addPropertyChangeListener(event -> {
            String propertyName = event.getPropertyName();
            switch (propertyName) {
                case Action.PROP_TEXT:
                    button.setTitle(generateTitle(action));
                    break;
                case Action.PROP_ENABLED:
                    button.setEnabled(action.isEnabled());
                    break;
                case Action.PROP_VISIBLE:
                    button.setVisible(action.isVisible());
                    break;
                case Action.PROP_ICON:
                    updateIcon(action.getIcon());
                    break;
                case Action.PROP_SHORTCUT:
                    button.setShortcutCombination(action.getShortcutCombination());
                    button.setTitle(generateTitle(action));
                    break;
                default:
            }
        });
    }

    protected void updateIcon(@Nullable String icon) {
        if (Strings.isNullOrEmpty(icon)) {
            button.setIcon(null);
        } else {
            button.setIcon(new Icon(icon));
        }
    }

    @Nullable
    protected String generateTitle(Action action) {
        String text = action.getText();
        String shortcutCombination = action.getShortcutCombination() != null
                ? action.getShortcutCombination().format()
                : null;

        if (!Strings.isNullOrEmpty(text)) {
            return Strings.isNullOrEmpty(shortcutCombination)
                    ? text
                    : String.format("%s (%s)", text, shortcutCombination);
        } else if (!Strings.isNullOrEmpty(shortcutCombination)) {
            return shortcutCombination;
        } else {
            return null;
        }
    }
}
