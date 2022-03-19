/*
 * Copyright 2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jmix.ui.component.impl;

import com.vaadin.event.LayoutEvents;
import com.vaadin.event.ShortcutListener;
import com.vaadin.shared.Registration;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.AbstractOrderedLayout;
import io.jmix.core.DevelopmentException;
import io.jmix.core.common.event.Subscription;
import io.jmix.ui.component.*;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;
import static io.jmix.ui.component.impl.WrapperUtils.toVaadinAlignment;

public abstract class AbstractBox<T extends AbstractOrderedLayout>
        extends AbstractComponent<T> implements BoxLayout {

    protected List<Component> ownComponents = new ArrayList<>();
    protected Registration layoutClickRegistration;
    protected Map<ShortcutAction, ShortcutListener> shortcuts;

    @Override
    public void add(Component childComponent) {
        add(childComponent, ownComponents.size());
    }

    @Override
    public void add(Component childComponent, int index) {
        if (childComponent.getParent() != null && childComponent.getParent() != this) {
            throw new IllegalStateException("Component already has parent");
        }

        com.vaadin.ui.Component vComponent = childComponent.unwrapComposition(com.vaadin.ui.Component.class);
        if (ownComponents.contains(childComponent)) {
            int existingIndex = component.getComponentIndex(vComponent);
            if (index > existingIndex) {
                index--;
            }

            remove(childComponent);
        }

        component.addComponent(vComponent, index);
        component.setComponentAlignment(vComponent, toVaadinAlignment(childComponent.getAlignment()));

        if (frame != null) {
            if (childComponent instanceof BelongToFrame
                    && ((BelongToFrame) childComponent).getFrame() == null) {
                ((BelongToFrame) childComponent).setFrame(frame);
            } else {
                attachToFrame(childComponent);
            }
        }

        if (index == ownComponents.size()) {
            ownComponents.add(childComponent);
        } else {
            ownComponents.add(index, childComponent);
        }

        childComponent.setParent(this);
    }

    @Override
    public int indexOf(Component childComponent) {
        return ownComponents.indexOf(childComponent);
    }

    @Nullable
    @Override
    public Component getComponent(int index) {
        return ownComponents.get(index);
    }

    protected void attachToFrame(Component childComponent) {
        ((FrameImplementation) frame).registerComponent(childComponent);
    }

    @Override
    public void remove(Component childComponent) {
        component.removeComponent(childComponent.unwrapComposition(com.vaadin.ui.Component.class));
        ownComponents.remove(childComponent);

        childComponent.setParent(null);
    }

    @Override
    public void removeAll() {
        component.removeAllComponents();

        Component[] components = ownComponents.toArray(new Component[0]);
        ownComponents.clear();

        for (Component childComponent : components) {
            childComponent.setParent(null);
        }
    }

    @Override
    public void setFrame(@Nullable Frame frame) {
        super.setFrame(frame);

        if (frame != null) {
            for (Component childComponent : ownComponents) {
                if (childComponent instanceof BelongToFrame
                        && ((BelongToFrame) childComponent).getFrame() == null) {
                    ((BelongToFrame) childComponent).setFrame(frame);
                }
            }
        }
    }

    @Nullable
    @Override
    public Component getOwnComponent(String id) {
        checkNotNullArgument(id);

        return ownComponents.stream()
                .filter(component -> Objects.equals(id, component.getId()))
                .findFirst()
                .orElse(null);
    }

    @Nullable
    @Override
    public Component getComponent(String id) {
        return ComponentsHelper.getComponent(this, id);
    }

    @Override
    public Collection<Component> getOwnComponents() {
        return Collections.unmodifiableCollection(ownComponents);
    }

    @Override
    public Stream<Component> getOwnComponentsStream() {
        return ownComponents.stream();
    }

    @Override
    public Collection<Component> getComponents() {
        return ComponentsHelper.getComponents(this);
    }

    @Override
    public void expand(Component childComponent) {
        component.setExpandRatio(childComponent.unwrapComposition(com.vaadin.ui.Component.class), 1);

        if (getExpandDirection() == ExpandDirection.VERTICAL) {
            childComponent.setHeightFull();
        } else {
            childComponent.setWidthFull();
        }
    }

    @Override
    public void resetExpanded() {
        for (com.vaadin.ui.Component child : component) {
            component.setExpandRatio(child, 0);
        }
    }

    @Override
    public boolean isExpanded(Component component) {
        return ownComponents.contains(component) && ComponentsHelper.isComponentExpanded(component);
    }

    @Override
    public void setMargin(io.jmix.ui.component.MarginInfo marginInfo) {
        MarginInfo vMargin = new MarginInfo(marginInfo.hasTop(), marginInfo.hasRight(), marginInfo.hasBottom(),
                marginInfo.hasLeft());
        component.setMargin(vMargin);
    }

    @Override
    public io.jmix.ui.component.MarginInfo getMargin() {
        MarginInfo vMargin = component.getMargin();
        return new io.jmix.ui.component.MarginInfo(vMargin.hasTop(), vMargin.hasRight(), vMargin.hasBottom(),
                vMargin.hasLeft());
    }

    @Override
    public void setSpacing(boolean enabled) {
        component.setSpacing(enabled);
    }

    @Override
    public boolean getSpacing() {
        return component.isSpacing();
    }

    @Override
    public Subscription addLayoutClickListener(Consumer<LayoutClickEvent> listener) {
        if (layoutClickRegistration == null) {
            LayoutEvents.LayoutClickListener layoutClickListener = event -> {
                Component childComponent = findChildComponent(event.getChildComponent());
                Component clickedComponent = findChildComponent(event.getClickedComponent());
                MouseEventDetails mouseEventDetails = WrapperUtils.toMouseEventDetails(event);

                LayoutClickEvent layoutClickEvent =
                        new LayoutClickEvent(this, childComponent, clickedComponent, mouseEventDetails);

                publish(LayoutClickEvent.class, layoutClickEvent);
            };
            layoutClickRegistration = component.addLayoutClickListener(layoutClickListener);
        }

        getEventHub().subscribe(LayoutClickEvent.class, listener);
        return () -> internalRemoveLayoutClickListener(listener);
    }

    @Nullable
    protected Component findChildComponent(com.vaadin.ui.Component childComponent) {
        for (Component component : getComponents()) {
            if (component.unwrapComposition(com.vaadin.ui.Component.class) == childComponent) {
                return component;
            }
        }
        return null;
    }

    protected void internalRemoveLayoutClickListener(Consumer<LayoutClickEvent> listener) {
        unsubscribe(LayoutClickEvent.class, listener);

        if (!hasSubscriptions(LayoutClickEvent.class)) {
            layoutClickRegistration.remove();
            layoutClickRegistration = null;
        }
    }

    @Override
    public void addShortcutAction(ShortcutAction action) {
        KeyCombination keyCombination = action.getShortcutCombination();
        ShortcutListener shortcut =
                new ContainerShortcutActionWrapper(action, this, keyCombination);
        component.addShortcutListener(shortcut);

        if (shortcuts == null) {
            shortcuts = new HashMap<>(4);
        }
        shortcuts.put(action, shortcut);
    }

    @Override
    public void removeShortcutAction(ShortcutAction action) {
        if (shortcuts != null) {
            component.removeShortcutListener(shortcuts.remove(action));

            if (shortcuts.isEmpty()) {
                shortcuts = null;
            }
        }
    }

    @Override
    public void setExpandRatio(Component component, float ratio) {
        if (ratio < 0) {
            throw new DevelopmentException(
                    String.format("Expand ratio must be greater than or equal to 0 in component: %s.",
                            component.getId()));
        }

        com.vaadin.ui.Component vComponent = component.unwrapComposition(com.vaadin.ui.Component.class);

        this.component.setExpandRatio(vComponent, ratio);
    }

    @Override
    public float getExpandRatio(Component component) {
        com.vaadin.ui.Component vComponent = component.unwrap(com.vaadin.ui.Component.class);

        return this.component.getExpandRatio(vComponent);
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return component.isRequiredIndicatorVisible();
    }

    @Override
    public void setRequiredIndicatorVisible(boolean visible) {
        component.setRequiredIndicatorVisible(visible);
    }

    @Override
    public void attached() {
        super.attached();

        for (Component component : ownComponents) {
            ((AttachNotifier) component).attached();
        }
    }

    @Override
    public void detached() {
        super.detached();

        for (Component component : ownComponents) {
            ((AttachNotifier) component).detached();
        }
    }
}
