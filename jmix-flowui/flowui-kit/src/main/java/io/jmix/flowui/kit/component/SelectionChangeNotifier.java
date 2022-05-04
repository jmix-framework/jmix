package io.jmix.flowui.kit.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.shared.Registration;

public interface SelectionChangeNotifier<C extends Component, T> {

    Registration addSelectionListener(SelectionListener<C, T> listener);
}
