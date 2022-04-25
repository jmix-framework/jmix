package io.jmix.flowui.component.valuepicker;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;
import io.jmix.flowui.component.HasTitle;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

@Tag("jmix-value-picker-button")
@JsModule("./src/value-picker-button/jmix-value-picker-button.js")
public class ValuePickerButton extends Component
        implements ClickNotifier<ValuePickerButton>, Focusable<ValuePickerButton>,
        HasStyle, HasTheme, HasTitle {

    protected Component iconComponent;

    public Component getIcon() {
        Preconditions.checkState(iconComponent != null, "No icon set");

        return iconComponent;
    }

    public void setIcon(Component icon) {
        checkNotNullArgument(icon);

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
}
