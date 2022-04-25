package io.jmix.flowui.xml.layout.loader;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

// TODO: gg, create base FlexComponentLoader
public class HorizontalLayoutLoader extends ContainerLoader<HorizontalLayout> {

    @Override
    public void createComponent() {
        // TODO: gg, use factory
        resultComponent = new HorizontalLayout();
        loadId(resultComponent, element);
        createSubComponents(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        loadSpacing(resultComponent, element);

        loadSubComponentsAndExpand(resultComponent, element);
        // TODO: gg, implement
    }
}
