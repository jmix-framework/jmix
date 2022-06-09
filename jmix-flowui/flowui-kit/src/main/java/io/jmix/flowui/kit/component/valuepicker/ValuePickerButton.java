package io.jmix.flowui.kit.component.valuepicker;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.HasAction;
import io.jmix.flowui.kit.component.HasShortcutCombination;
import io.jmix.flowui.kit.component.HasTitle;
import io.jmix.flowui.kit.component.KeyCombination;

import javax.annotation.Nullable;
import java.util.Objects;


@Tag("jmix-value-picker-button")
@JsModule("./src/value-picker-button/jmix-value-picker-button.js")
public class ValuePickerButton extends Component
        implements ClickNotifier<ValuePickerButton>, Focusable<ValuePickerButton>,
        HasStyle, HasTheme, HasTitle, HasAction, HasShortcutCombination {


    protected ValuePickerButtonActionSupport actionSupport;

    protected Component iconComponent;
    protected ShortcutRegistration shortcutRegistration;

    @Override
    public void setAction(@Nullable Action action, boolean overrideComponentProperties) {
        getActionSupport().setAction(action, overrideComponentProperties);
    }

    @Nullable
    @Override
    public Action getAction() {
        return getActionSupport().getAction();
    }

    protected ValuePickerButtonActionSupport getActionSupport() {
        if (actionSupport == null) {
            actionSupport = createActionSupport();
        }

        return actionSupport;
    }

    protected ValuePickerButtonActionSupport createActionSupport() {
        return new ValuePickerButtonActionSupport(this);
    }

    public Component getIcon() {
        Preconditions.checkState(iconComponent != null, "No icon set");

        return iconComponent;
    }

    public void setIcon(Component icon) {
        Preconditions.checkNotNull(icon, "Icon cannot be null");

        if (icon.getElement().isTextNode()) {
            throw new IllegalArgumentException("Text node can't be used as an icon.");
        }

        if (iconComponent != null) {
            remove(iconComponent);
        }

        iconComponent = icon;

        add(icon);
    }

    protected void add(Component component) {
        getElement().appendChild(component.getElement());
        component.getElement().setAttribute("slot", "icon");
    }

    protected void remove(Component component) {
        if (getElement().equals(component.getElement().getParent())) {
            component.getElement().removeAttribute("slot");
            getElement().removeChild(component.getElement());
        } else {
            throw new IllegalArgumentException("The given component ("
                    + component + ") is not a child of this component");
        }
    }

    @Nullable
    @Override
    public KeyCombination getShortcutCombination() {
        if (shortcutRegistration == null) {
            return null;
        }

        KeyModifier[] keyModifiers = shortcutRegistration.getModifiers().stream()
                .filter(key -> key instanceof KeyModifier)
                .map(key -> (KeyModifier) key)
                .toArray(KeyModifier[]::new);

        return KeyCombination.create(shortcutRegistration.getKey(), keyModifiers);
    }

    @Override
    public void setShortcutCombination(@Nullable KeyCombination shortcutCombination) {
        KeyCombination oldValue = getShortcutCombination();
        if (!Objects.equals(oldValue, shortcutCombination)) {
            if (shortcutRegistration != null) {
                shortcutRegistration.remove();
                shortcutRegistration = null;
            }

            if (shortcutCombination != null) {
                shortcutRegistration = addClickShortcut(shortcutCombination.getKey(),
                        shortcutCombination.getKeyModifiers());
            }
        }
    }
}
