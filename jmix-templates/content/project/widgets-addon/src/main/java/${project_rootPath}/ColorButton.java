package ${project_rootPackage};

import ${project_rootPackage}.client.ColorButtonState;
import io.jmix.ui.widget.JmixButton;

import java.util.Objects;

public class ColorButton extends JmixButton {

    @Override
    protected ColorButtonState getState() {
        return (ColorButtonState) super.getState();
    }

    @Override
    protected ColorButtonState getState(final boolean markAsDirty) {
        return (ColorButtonState) super.getState(markAsDirty);
    }

    public String getColor() {
        return getState(false).color;
    }

    public void setColor(final String color) {
        if (!Objects.equals(getState(false).color, color)) {
            getState().color = color;
        }
    }
}
