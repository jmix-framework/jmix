package ${project_rootPackage}.client;

import ${project_rootPackage}.ColorButton;
import com.google.gwt.dom.client.Style;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.shared.ui.Connect;
import io.jmix.ui.widget.client.button.JmixButtonConnector;

@Connect(ColorButton.class)
public class ColorButtonConnector extends JmixButtonConnector {

    @Override
    public ColorButtonState getState() {
        return (ColorButtonState) super.getState();
    }

    @Override
    public void onStateChanged(StateChangeEvent event) {
        super.onStateChanged(event);

        if (event.hasPropertyChanged("color")) {
            Style style = getWidget().getElement().getStyle();
            style.setBackgroundColor(getState().color);
            style.setBackgroundImage("none");
        }
    }
}
