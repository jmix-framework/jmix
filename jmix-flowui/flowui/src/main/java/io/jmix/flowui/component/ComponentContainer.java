package io.jmix.flowui.component;

import com.vaadin.flow.component.Component;

import java.util.Collection;
import java.util.Optional;

public interface ComponentContainer {

    default Optional<Component> findComponent(String id) {
        return UiComponentUtils.findComponent(((Component) this), id);
    }

    default Component getComponent(String id) {
        return findComponent(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(String.format("Not found component with id: '%s'", id)));
    }

    Optional<Component> findOwnComponent(String id);

    default Component getOwnComponent(String id) {
        return findOwnComponent(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(String.format("Not found own component with id: '%s'", id)));
    }

    Collection<Component> getOwnComponents();

    default Collection<Component> getComponents() {
        return UiComponentUtils.getComponents(((Component) this));
    }
}
