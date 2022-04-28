package io.jmix.flowui.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;

import java.util.Collection;
import java.util.Optional;

public interface EnhancedHasComponents extends HasComponents {

    Optional<Component> findComponent(String id);

    Component getComponent(String id);

    Optional<Component> findOwnComponent(String id);

    Component getOwnComponent(String id);

    Collection<Component> getOwnComponents();

    Collection<Component> getComponents();
}
