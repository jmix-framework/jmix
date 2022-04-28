package io.jmix.flowui.component;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.dom.Element;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.screen.Screen;
import io.jmix.flowui.sys.ValuePathHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

public final class UiComponentUtils {

    private UiComponentUtils() {
    }

    public static Optional<Component> findOwnComponent(HasComponents container, String id) {
        if (container instanceof EnhancedHasComponents) {
            return ((EnhancedHasComponents) container).findOwnComponent(id);
        } else if (container instanceof HasOrderedComponents) {
            return ((HasOrderedComponents) container).getChildren()
                    .filter(component -> sameId(component, id))
                    .findFirst();
        } else if (container instanceof Component) {
            return ((Component) container).getChildren()
                    .filter(component -> sameId(component, id))
                    .findFirst();
        } else {
            throw new IllegalArgumentException(container.getClass().getSimpleName() +
                    " have no API to obtain component list");
        }
    }

    public static Optional<Component> findComponent(HasComponents container, String id) {
        String[] elements = ValuePathHelper.parse(id);
        if (elements.length == 1) {
            Optional<Component> component = findOwnComponent(container, id);
            if (component.isPresent()) {
                return component;
            } else {
                return getComponentByIteration(container, id);
            }
        } else {
            Optional<Component> innerComponentOpt = findOwnComponent(container, elements[0]);
            if (innerComponentOpt.isEmpty()) {
                return getComponentByIteration(container, id);
            } else {
                Component innerComponent = innerComponentOpt.get();
                if (innerComponent instanceof HasComponents) {
                    String subPath = ValuePathHelper.pathSuffix(elements);
                    return findComponent(((HasComponents) innerComponent), subPath);
                }

                return Optional.empty();
            }
        }
    }

    private static Optional<Component> getComponentByIteration(HasComponents container, String id) {
        return Optional.ofNullable(getComponentByIteration(getOwnComponents(container), id));
    }

    @Nullable
    private static Component getComponentByIteration(Collection<Component> components, String id) {
        for (Component component : components) {
            if (sameId(component, id)) {
                return component;
            } else if (component instanceof HasComponents) {
                Collection<Component> ownComponents = getOwnComponents((HasComponents) component);
                Component innerComponent = getComponentByIteration(ownComponents, id);
                if (innerComponent != null) {
                    return innerComponent;
                }
            }
        }

        return null;
    }

    public static Collection<Component> getOwnComponents(HasComponents container) {
        if (container instanceof EnhancedHasComponents) {
            return ((EnhancedHasComponents) container).getOwnComponents();
        } else if (container instanceof HasOrderedComponents) {
            return ((HasOrderedComponents) container).getChildren().sequential().collect(Collectors.toList());
        } else if (container instanceof Component) {
            return ((Component) container).getChildren().sequential().collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException(container.getClass().getSimpleName() +
                    " have no API to obtain component list");
        }
    }

    public static Collection<Component> getComponents(HasComponents container) {
        // do not return LinkedHashSet, it uses much more memory than ArrayList
        Collection<Component> components = new ArrayList<>();

        fillChildComponents(container, components);

        if (components.isEmpty()) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableCollection(components);
    }

    private static void fillChildComponents(HasComponents container, Collection<Component> components) {
        Collection<Component> ownComponents;
        if (container instanceof EnhancedHasComponents) {
            ownComponents = ((EnhancedHasComponents) container).getOwnComponents();
        } else if (container instanceof HasOrderedComponents) {
            ownComponents = ((HasOrderedComponents) container).getChildren().sequential().collect(Collectors.toList());
        } else if (container instanceof Component) {
            ownComponents = ((Component) container).getChildren().sequential().collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException(container.getClass().getSimpleName() +
                    " have no API to obtain component list");
        }

        components.addAll(ownComponents);

        for (Component component : ownComponents) {
            if (component instanceof HasComponents) {
                fillChildComponents((HasComponents) component, components);
            }
        }
    }

    public static boolean sameId(Component component, String id) {
        Optional<String> componentId = component.getId();
        return componentId.isPresent() && id.equals(componentId.get());
    }

    @Nullable
    public static Screen findScreen(Component component) {
        if (component instanceof Screen) {
            return (Screen) component;
        }

        Optional<Component> parent = component.getParent();
        return parent.map(UiComponentUtils::findScreen).orElse(null);
    }

    public static void addComponentsToSlot(Element element, String slot, Component... components) {
        for (Component component : components) {
            component.getElement().setAttribute("slot", slot);
            element.appendChild(component.getElement());
        }
    }

    public static void clearSlot(Element element, String slot) {
        element.getChildren()
                .filter(child -> slot.equals(child.getAttribute("slot")))
                .forEach(element::removeChild);
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

        Screen parent = UiComponentUtils.findScreen(component);
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
}
