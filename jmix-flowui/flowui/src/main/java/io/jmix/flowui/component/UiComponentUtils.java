package io.jmix.flowui.component;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dialog.Dialog;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.view.View;
import io.jmix.flowui.sys.ValuePathHelper;

import javax.annotation.Nullable;
import java.util.*;
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

    public static Optional<Component> findComponent(View<?> view, String id) {
        Component content = view.getContent();
        if (!(content instanceof HasComponents)) {
            throw new IllegalStateException(View.class.getSimpleName() + " content doesn't contain components");
        }

        return findComponent(((HasComponents) content), id);
    }

    public static Component findComponentOrElseThrow(View<?> view, String id) {
        Component content = view.getContent();
        if (!(content instanceof HasComponents)) {
            throw new IllegalStateException(View.class.getSimpleName() + " content doesn't contain components");
        }

        return findComponent(((HasComponents) content), id)
                .orElseThrow(() -> new IllegalStateException(
                        String.format("Component with id '%s' not found", id)));
    }

    // todo rework using Component class
    public static Optional<Component> findComponent(AppLayout appLayout, String id) {
        List<Component> components = appLayout.getChildren().collect(Collectors.toList());

        String[] elements = ValuePathHelper.parse(id);
        if (elements.length == 1) {
            Optional<Component> component = components.stream()
                    .filter(c -> sameId(c, id))
                    .findFirst();

            if (component.isPresent()) {
                return component;
            } else {
                return Optional.ofNullable(getComponentByIteration(components, id));
            }
        } else {
            Optional<Component> innerComponentOpt = components.stream()
                    .filter(c -> sameId(c, elements[0]))
                    .findFirst();

            if (innerComponentOpt.isEmpty()) {
                return Optional.ofNullable(getComponentByIteration(components, id));
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
