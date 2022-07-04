package io.jmix.flowui.component.layout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.flowui.component.EnhancedHasComponents;
import io.jmix.flowui.component.UiComponentUtils;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.jmix.flowui.component.UiComponentUtils.sameId;


public class ViewLayout extends VerticalLayout implements EnhancedHasComponents {

    @Override
    public Optional<Component> findComponent(String id) {
        return UiComponentUtils.findComponent(this, id);
    }

    @Override
    public Component getComponent(String id) {
        return findComponent(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(String.format("Not found component with id: '%s'", id)));
    }

    @Override
    public Optional<Component> findOwnComponent(String id) {
        return getChildren()
                .filter(component -> sameId(component, id))
                .findFirst();
    }

    @Override
    public Component getOwnComponent(String id) {
        return findOwnComponent(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(String.format("Not found own component with id: '%s'", id)));
    }

    @Override
    public Collection<Component> getOwnComponents() {
        return getChildren().sequential().collect(Collectors.toList());
    }

    @Override
    public Collection<Component> getComponents() {
        return UiComponentUtils.getComponents(this);
    }
}
