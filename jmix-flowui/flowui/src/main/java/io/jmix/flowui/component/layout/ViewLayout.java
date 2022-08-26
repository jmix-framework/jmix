package io.jmix.flowui.component.layout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.flowui.component.ComponentContainer;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.jmix.flowui.component.UiComponentUtils.sameId;


public class ViewLayout extends VerticalLayout implements ComponentContainer {

    @Override
    public Optional<Component> findOwnComponent(String id) {
        return getChildren()
                .filter(component -> sameId(component, id))
                .findFirst();
    }

    @Override
    public Collection<Component> getOwnComponents() {
        return getChildren().sequential().collect(Collectors.toList());
    }
}
