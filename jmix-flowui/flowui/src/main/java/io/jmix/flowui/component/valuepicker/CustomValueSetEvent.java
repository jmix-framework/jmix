package io.jmix.flowui.component.valuepicker;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;

/**
 * The event that is fired when a user inputs value manually.
 *
 * @param <V> field value type
 */
public class CustomValueSetEvent<C extends Component, V> extends ComponentEvent<C> {

    protected final String text;

    public CustomValueSetEvent(C source, String text) {
        super(source, true);

        this.text = text;
    }

    /**
     * @return entered text
     */
    public String getText() {
        return text;
    }
}
