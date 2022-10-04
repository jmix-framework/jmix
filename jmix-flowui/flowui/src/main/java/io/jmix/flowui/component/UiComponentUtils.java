/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.component;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.dialog.Dialog;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.sys.ValuePathHelper;
import io.jmix.flowui.view.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

public final class UiComponentUtils {

    private static final Logger log = LoggerFactory.getLogger(UiComponentUtils.class);

    private UiComponentUtils() {
    }

    public static Optional<Component> findComponent(View<?> view, String id) {
        Component content = view.getContent();
        if (isContainer(content)) {
            return findComponent(content, id);
        }

        throw new IllegalStateException(View.class.getSimpleName() + " content doesn't contain components");
    }

    public static Component getComponent(View<?> view, String id) {
        return findComponent(view, id)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Component with id '%s' not found", id)));
    }

    public static Optional<Component> findComponent(Component container, String id) {
        String[] elements = ValuePathHelper.parse(id);
        if (elements.length == 1) {
            Optional<Component> component = findOwnComponent(container, id);
            if (component.isPresent()) {
                return component;
            } else {
                return getComponentRecursively(getOwnComponents(container), id);
            }
        } else {
            Optional<Component> innerComponentOpt = findOwnComponent(container, elements[0]);
            if (innerComponentOpt.isEmpty()) {
                // FIXME: gg, is id correct?
                return getComponentRecursively(getOwnComponents(container), id);
            } else {
                Component innerComponent = innerComponentOpt.get();
                if (isContainer(innerComponent)) {
                    String subPath = ValuePathHelper.pathSuffix(elements);
                    return findComponent(innerComponent, subPath);
                }

                return Optional.empty();
            }
        }
    }

    private static Optional<Component> getComponentRecursively(Collection<Component> components, String id) {
        for (Component component : components) {
            if (sameId(component, id)) {
                return Optional.of(component);
            } else if (isContainer(component)) {
                Optional<Component> innerComponent =
                        getComponentRecursively(getOwnComponents(component), id);
                if (innerComponent.isPresent()) {
                    return innerComponent;
                }
            }
        }

        return Optional.empty();
    }

    public static Collection<Component> getOwnComponents(Component container) {
        if (container instanceof ComponentContainer) {
            return ((ComponentContainer) container).getOwnComponents();
        } else if (container instanceof HasComponents) {
            return container.getChildren().sequential().collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException(container.getClass().getSimpleName() +
                    " has no API to obtain component list");
        }
    }

    public static Collection<Component> getComponents(Component container) {
        // do not return LinkedHashSet, it uses much more memory than ArrayList
        Collection<Component> components = new ArrayList<>();

        fillChildComponents(container, components);

        if (components.isEmpty()) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableCollection(components);
    }

    public static Optional<Component> findOwnComponent(Component container, String id) {
        if (container instanceof ComponentContainer) {
            return ((ComponentContainer) container).findOwnComponent(id);
        } else if (container instanceof HasComponents) {
            return container.getChildren()
                    .filter(component -> sameId(component, id))
                    .findFirst();
        } else {
            throw new IllegalArgumentException(container.getClass().getSimpleName() +
                    " has no API to obtain component list");
        }
    }

    private static void fillChildComponents(Component container, Collection<Component> components) {
        Collection<Component> ownComponents = getOwnComponents(container);

        components.addAll(ownComponents);

        for (Component component : ownComponents) {
            if (isContainer(component)) {
                fillChildComponents(component, components);
            }
        }
    }

    public static boolean sameId(Component component, String id) {
        Optional<String> componentId = component.getId();
        return componentId.isPresent() && id.equals(componentId.get());
    }

    public static Optional<Focusable<?>> findFocusComponent(View<?> view, String componentId) {
        Optional<Component> optionalComponent = findComponent(view, componentId);
        if (optionalComponent.isEmpty()) {
            log.error("Can't find focus component: {}", componentId);
            return Optional.empty();
        }

        Component component = optionalComponent.get();
        if (canBeFocused(component)) {
            return Optional.of(((Focusable<?>) component));
        } else if (isContainer(component)) {
            return findFocusComponent(component);
        } else {
            return Optional.empty();
        }
    }

    public static Optional<Focusable<?>> findFocusComponent(View<?> view) {
        Component content = view.getContent();
        if (isContainer(content)) {
            return findFocusComponent(content);
        }

        throw new IllegalStateException(View.class.getSimpleName() + " content doesn't contain components");
    }

    public static Optional<Focusable<?>> findFocusComponent(Component container) {
        Collection<Component> ownComponents = getOwnComponents(container);
        return findFocusComponent(ownComponents);
    }

    public static Optional<Focusable<?>> findFocusComponent(Collection<Component> components) {
        for (Component child : components) {
            if (child instanceof Accordion
                /*|| child instanceof TabSheet*/) {
                // we don't know about selected tab after request
                // may be focused component located on not selected tab
                // it may break component tree
                continue;
            }

            if (canBeFocused(child)) {
                return Optional.of(((Focusable<?>) child));
            }

            if (isContainer(child)) {
                return findFocusComponent(child);
            }
        }

        return Optional.empty();
    }

    public static boolean isContainer(Component component) {
        return component instanceof ComponentContainer
                || component instanceof HasComponents;
    }

    private static boolean canBeFocused(Component component) {
        if (!(component instanceof Focusable)
                || !((Focusable<?>) component).isEnabled()) {
            return false;
        }

        if (component instanceof HasValue
                && ((HasValue<?, ?>) component).isReadOnly()) {
            return false;
        }

        return component.isVisible();
    }

    @Nullable
    public static View<?> findView(Component component) {
        if (component instanceof View) {
            return (View<?>) component;
        }

        Optional<Component> parent = component.getParent();
        return parent.map(UiComponentUtils::findView).orElse(null);
    }

    /**
     * Focuses component (or its nearest focusable parent).
     *
     * @param component component to focus
     */
    @SuppressWarnings("unchecked")
    public static void focusComponent(Component component) {
        Component parent = component;
        while (parent != null && !(parent instanceof Focusable)) {
            parent = parent.getParent().orElse(null);
        }
        if (parent != null) {
            ((Focusable<Component>) parent).focus();
        }
    }

    /**
     * Tests if component visible and its container visible.
     *
     * @param child component
     * @return component visibility
     */
    public static boolean isComponentVisible(Component child) {
        Preconditions.checkNotNullArgument(child);

        return child.isVisible()
                && (child.getParent().isEmpty()
                || isComponentVisible(child.getParent().get()));
    }

    /**
     * Tests if component enabled and its container enabled.
     *
     * @param child component
     * @return component enabled state
     */
    public static boolean isComponentEnabled(Component child) {
        Preconditions.checkNotNullArgument(child);

        if (!(child instanceof HasEnabled)) {
            return true;
        }

        return ((HasEnabled) child).isEnabled()
                && (child.getParent().isEmpty()
                || isComponentEnabled(child.getParent().get()));
    }

    public static boolean isComponentAttachedToDialog(Component component) {
        Preconditions.checkNotNullArgument(component);

        View<?> parent = UiComponentUtils.findView(component);
        if (parent == null) {
            return false;
        }
        return isDialog(parent);
    }

    private static boolean isDialog(Component component) {
        if (component instanceof Dialog) {
            return true;
        }
        if (component.getParent().isPresent()) {
            return isDialog(component.getParent().get());
        } else {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <V> V getEmptyValue(Component component) {
        return component instanceof HasValue
                ? ((HasValue<?, V>) component).getEmptyValue()
                : null;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <V> V getValue(HasValue<?, V> component) {
        return component instanceof SupportsTypedValue
                ? ((SupportsTypedValue<?, ?, V, ?>) component).getTypedValue()
                : component.getValue();
    }

    @SuppressWarnings("unchecked")
    public static <V> void setValue(HasValue<?, V> component, @Nullable V value) {
        if (component instanceof SupportsTypedValue) {
            ((SupportsTypedValue<?, ?, V, ?>) component).setTypedValue(value);
        } else {
            component.setValue(value);
        }
    }
}
