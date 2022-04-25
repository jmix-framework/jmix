package io.jmix.flowui.xml.layout.loader;

import com.vaadin.flow.component.textfield.TextField;

// TODO: gg, create base AbstractTextFieldLoader
public class TextFieldLoader extends AbstractComponentLoader<TextField> {

    @Override
    public void createComponent() {
        // TODO: gg, use factory
        resultComponent = new TextField();
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        // TODO: gg, implement
        loadData(resultComponent, element);
    }
}
