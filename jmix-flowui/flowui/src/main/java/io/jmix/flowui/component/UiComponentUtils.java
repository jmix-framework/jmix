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
import com.vaadin.flow.component.page.PendingJavaScriptResult;
import com.vaadin.flow.component.shared.HasPrefix;
import com.vaadin.flow.component.shared.HasSuffix;
import com.vaadin.flow.server.StreamResource;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorageLocator;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentUtils;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.HasActions;
import io.jmix.flowui.kit.component.HasSubParts;
import io.jmix.flowui.sys.ValuePathHelper;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewChildrenVisitResult;
import io.jmix.flowui.view.ViewControllerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Utility class working with Jmix UI component specifics.
 *
 * @see io.jmix.flowui.kit.component.ComponentUtils
 */
public final class UiComponentUtils {

    private static final Logger log = LoggerFactory.getLogger(UiComponentUtils.class);

    private UiComponentUtils() {
    }

    /**
     * Returns an {@link Optional} describing the component with given id,
     * or an empty {@link Optional}.
     *
     * @param view view to find component from
     * @param id   component id
     * @return an {@link Optional} describing the component,
     * or an empty {@link Optional}
     * @throws IllegalStateException if view content is not a container
     */
    public static Optional<Component> findComponent(View<?> view, String id) {
        Component content = view.getContent();
        if (isContainer(content)) {
            return findComponent(content, id, UiComponentUtils::sameId);
        }

        throw new IllegalStateException(View.class.getSimpleName() + " content doesn't contain components");
    }

    /**
     * Returns the component with given id.
     *
     * @param view view to find component from
     * @param id   component id
     * @return the component with given id
     * @throws IllegalStateException    if view content is not a container
     * @throws IllegalArgumentException if a component with given id is not found
     */
    public static Component getComponent(View<?> view, String id) {
        return findComponent(view, id)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Component with id '%s' not found", id)));
    }

    /**
     * Returns an {@link Optional} describing the component with given id,
     * or an empty {@link Optional}.
     *
     * @param component component to find inner component
     * @param id        component id to find
     * @return an {@link Optional} describing the found component,
     * or an empty {@link Optional}
     */
    public static Optional<Component> findComponent(Component component, String id) {
        if (component instanceof View<?> view) {
            return UiComponentUtils.findComponent(view, id);
        } else if (component instanceof Fragment<?> fragment) {
            return FragmentUtils.findComponent(fragment, id);
        } else if (UiComponentUtils.isContainer(component)) {
            return UiComponentUtils.findComponent(component, id,
                    findFragment(component) == null ? UiComponentUtils::sameId : FragmentUtils::sameId);
        }
        return Optional.empty();
    }

    /**
     * Returns an {@link Optional} describing the component with given id,
     * or an empty {@link Optional}.
     *
     * @param container    container to find component
     * @param id           component id to find
     * @param idComparator function that checks if a child component has the same id as passed
     * @return an {@link Optional} describing the found component,
     * or an empty {@link Optional}
     */
    public static Optional<Component> findComponent(Component container, String id,
                                                    BiPredicate<Component, String> idComparator) {
        String[] elements = ValuePathHelper.parse(id);
        if (elements.length == 1) {
            Optional<Component> component = findOwnComponent(container, id, idComparator);
            if (component.isPresent()) {
                return component;
            } else {
                if (container instanceof HasSubParts subPartsContainer) {
                    Optional<Component> subPart = findSubPart(subPartsContainer, id);

                    if (subPart.isPresent()) {
                        return subPart;
                    }
                }

                return getComponentRecursively(getOwnComponents(container), id, idComparator);
            }
        } else {
            Optional<Component> innerComponentOpt = findOwnComponent(container, elements[0], idComparator);
            if (innerComponentOpt.isEmpty()) {
                return getComponentRecursively(getOwnComponents(container), id, idComparator);
            } else {
                Component innerComponent = innerComponentOpt.get();
                if (isContainer(innerComponent) || innerComponent instanceof Fragment) {
                    String subPath = ValuePathHelper.pathSuffix(elements);
                    return findComponent(innerComponent, subPath);
                }

                return Optional.empty();
            }
        }
    }

    private static Optional<Component> getComponentRecursively(Collection<Component> components, String id,
                                                               BiPredicate<Component, String> idComparator) {
        for (Component component : components) {
            if (idComparator.test(component, id)) {
                return Optional.of(component);
            } else if (isContainer(component)) {
                Optional<Component> innerComponent =
                        getComponentRecursively(getOwnComponents(component), id, idComparator);
                if (innerComponent.isPresent()) {
                    return innerComponent;
                }
            }

            if (component instanceof HasSubParts hasSubPartsComponent) {
                Optional<Component> innerComponent = findSubPart(hasSubPartsComponent, id);

                if (innerComponent.isPresent()) {
                    return innerComponent;
                }

                String[] elements = ValuePathHelper.parse(id);
                if (idComparator.test(component, elements[0])) {
                    innerComponent = findSubPart(hasSubPartsComponent, ValuePathHelper.pathSuffix(elements));

                    if (innerComponent.isPresent()) {
                        return innerComponent;
                    }
                }
            }

            if (component instanceof HasPrefix hasPrefixComponent
                    && hasPrefixComponent.getPrefixComponent() != null) {
                Optional<Component> innerComponent =
                        getComponentRecursively(Collections.singleton(hasPrefixComponent.getPrefixComponent()),
                                id, idComparator);

                if (innerComponent.isPresent()) {
                    return innerComponent;
                }
            }

            if (component instanceof HasSuffix hasSuffixComponent
                    && hasSuffixComponent.getSuffixComponent() != null) {
                Optional<Component> innerComponent =
                        getComponentRecursively(Collections.singleton(hasSuffixComponent.getSuffixComponent()),
                                id, idComparator);

                if (innerComponent.isPresent()) {
                    return innerComponent;
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Returns a collection of direct children of passed component.
     *
     * @param container the container to get own components
     * @return a collection of own components
     * @throws IllegalArgumentException if passed component has no API to obtain component list
     */
    public static Collection<Component> getOwnComponents(Component container) {
        if (container instanceof ComponentContainer) {
            return ((ComponentContainer) container).getOwnComponents();
        } else if (container instanceof HasComponents) {
            return container.getChildren().sequential().collect(Collectors.toList());
        } else if (container instanceof View<?>) {
            return container.getChildren().collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException(container.getClass().getSimpleName() +
                    " has no API to obtain component list");
        }
    }

    /**
     * Returns a collection of all child components.
     *
     * @param container the container to get child components
     * @return a collection of all child components
     */
    public static Collection<Component> getComponents(Component container) {
        // do not return LinkedHashSet, it uses much more memory than ArrayList
        Collection<Component> components = new ArrayList<>();

        fillChildComponents(container, components);

        if (components.isEmpty()) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableCollection(components);
    }

    /**
     * Returns an {@link Optional} describing the direct component with given id,
     * or an empty {@link Optional}.
     *
     * @param container the container to find own component
     * @param id        component id to find
     * @return an {@link Optional} describing the found own component,
     * or an empty {@link Optional}
     */
    public static Optional<Component> findOwnComponent(Component container, String id) {
        return findOwnComponent(container, id, UiComponentUtils::sameId);
    }

    /**
     * Returns an {@link Optional} describing the direct component with given id,
     * or an empty {@link Optional}.
     *
     * @param container    the container to find own component
     * @param id           component id to find
     * @param idComparator function that checks if a child component has the same id as passed
     * @return an {@link Optional} describing the found own component,
     * or an empty {@link Optional}
     */
    public static Optional<Component> findOwnComponent(Component container, String id,
                                                       BiPredicate<Component, String> idComparator) {
        if (container instanceof ComponentContainer) {
            // Don't use 'ComponentContainer.findOwnComponent' because it compares ids
            // using 'UiComponentUtils.sameId' only
            return ((ComponentContainer) container).getOwnComponents()
                    .stream()
                    .filter(component -> idComparator.test(component, id))
                    .findFirst();
        } else if (container instanceof HasComponents) {
            return container.getChildren()
                    .filter(component -> idComparator.test(component, id))
                    .findFirst();
        } else {
            throw new IllegalArgumentException(container.getClass().getSimpleName() +
                    " has no API to obtain component list");
        }
    }

    /**
     * Returns an {@link Optional} describing the component with given id
     * from {@link HasSubParts} component, or an empty {@link Optional}.
     *
     * @param container the container to find component
     * @param id        component id to find
     * @return an {@link Optional} describing the found component,
     * or an empty {@link Optional}
     */
    public static Optional<Component> findSubPart(HasSubParts container, String id) {
        Object subPart = container.getSubPart(id);

        if (subPart instanceof Component subPartComponent) {
            return Optional.of(subPartComponent);
        }

        return Optional.empty();
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

    /**
     * Returns whether the component has the same id as passed.
     *
     * @param component component to compare id
     * @param id        id to compare
     * @return {@code true} if the component has the same id as passed,
     * {@code false} otherwise
     */
    public static boolean sameId(Component component, String id) {
        Optional<String> componentId = component.getId();
        return componentId.isPresent() && id.equals(componentId.get());
    }

    /**
     * Returns an {@link Optional} describing the focusable component of passed
     * view content with given id, or an empty {@link Optional}.
     *
     * @param view        view to find focusable component from
     * @param componentId component id
     * @return an {@link Optional} describing the focusable component,
     * or an empty {@link Optional}
     * @throws IllegalStateException if view content is not a container
     */
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

    /**
     * Returns an {@link Optional} describing the first focusable component of passed
     * view content, or an empty {@link Optional}.
     *
     * @param view view to find focusable component from
     * @return an {@link Optional} describing the first focusable component,
     * or an empty {@link Optional}
     * @throws IllegalStateException if view content is not a container
     */
    public static Optional<Focusable<?>> findFocusComponent(View<?> view) {
        Component content = view.getContent();
        if (isContainer(content)) {
            return findFocusComponent(content);
        }

        throw new IllegalStateException(View.class.getSimpleName() + " content doesn't contain components");
    }

    /**
     * Returns an {@link Optional} describing the first focusable component of passed
     * container component, or an empty {@link Optional}. If the child components of
     * container contains containers then their child components are also checked.
     *
     * @param container container to find focusable component from
     * @return an {@link Optional} describing the first focusable component,
     * or an empty {@link Optional}
     */
    public static Optional<Focusable<?>> findFocusComponent(Component container) {
        Collection<Component> ownComponents = getOwnComponents(container);
        return findFocusComponent(ownComponents);
    }

    /**
     * Returns an {@link Optional} describing the first focusable component of passed
     * component collection, or an empty {@link Optional}. If the passed component
     * collection contains containers then their child components are also checked.
     *
     * @param components component collection to find focusable component from
     * @return an {@link Optional} describing the first focusable component,
     * or an empty {@link Optional}
     */
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

    /**
     * Returns whether the passed component is a container, i.e. can have child components.
     *
     * @param component the component to test
     * @return Returns {@code true} if the passed component is a container,
     * {@code false} otherwise
     */
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

    /**
     * Returns a dialog to which the passed component is attached, {@code null} otherwise.
     *
     * @param component the component to find a parent dialog
     * @return a dialog to which the passed component is attached, {@code null} otherwise
     */
    @Nullable
    public static Dialog findDialog(Component component) {
        if (component instanceof Dialog) {
            return (Dialog) component;
        }

        Optional<Component> parent = component.getParent();
        return parent.map(UiComponentUtils::findDialog).orElse(null);
    }

    /**
     * Returns a view to which the passed component is attached.
     *
     * @param component the component to find a parent view
     * @return a view to which the passed component is attached
     * @throws IllegalStateException if a component isn't attached to a view
     */
    public static View<?> getView(Component component) {
        View<?> view = findView(component);
        if (view == null) {
            throw new IllegalStateException(String.format("A component '%s' is not attached to a view",
                    component.getClass().getSimpleName()));
        }

        return view;
    }

    /**
     * Returns a view to which the passed component is attached, {@code null} otherwise.
     *
     * @param component the component to find a parent view
     * @return a view to which the passed component is attached, {@code null} otherwise
     */
    @Nullable
    public static View<?> findView(Component component) {
        if (component instanceof View) {
            return (View<?>) component;
        }

        Optional<Component> parent = component.getParent();
        return parent.map(UiComponentUtils::findView).orElse(null);
    }

    /**
     * Returns a fragment which the passed component is attached.
     *
     * @param component the component to find a parent fragment
     * @return a fragment to which the passed component is attached
     * @throws IllegalStateException if a component isn't attached to a fragment
     */
    public static Fragment<?> getFragment(Component component) {
        Fragment<?> fragment = findFragment(component);

        if (fragment == null) {
            throw new IllegalStateException("A component '%s' is not attached to a fragment"
                    .formatted(component.getClass().getSimpleName()));
        }

        return fragment;
    }

    /**
     * Returns a fragment to which the passed component is attached, {@code null} otherwise.
     *
     * @param component the component ti find a parent fragment
     * @return a fragment to which the passed component is attached, {@code null} otherwise
     */
    @Nullable
    public static Fragment<?> findFragment(Component component) {
        if (component instanceof Fragment) {
            return (Fragment<?>) component;
        }

        Optional<Component> parent = component.getParent();
        return parent.map(UiComponentUtils::findFragment).orElse(null);
    }

    /**
     * Returns the currently active view shown in this UI.
     * <p>
     * Note, that the current route might not be initialized if this method
     * is called while still building the view chain, for example in the
     * constructor of layouts. Thus, consider postponing the usage of this
     * method to for example {@link io.jmix.flowui.view.View.ReadyEvent}.
     *
     * @return the currently active view instance if available
     * @throws IllegalStateException if current view is not yet available or is not {@link View}
     */
    public static View<?> getCurrentView() {
        Component currentView = UI.getCurrent().getCurrentView();
        if (!(currentView instanceof View<?>)) {
            throw new IllegalStateException(String.format("A component '%s' is not a %s",
                    currentView.getClass().getSimpleName(), View.class.getSimpleName()));
        }
        return (View<?>) currentView;
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
     * Tests if component is visible considering the visibility of
     * its parent container recursively.
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
     * Tests if component is enabled considering the enabled state of
     * its parent container recursively.
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

    /**
     * Returns {@code true} if the component is attached to a dialog
     * window, {@code false} otherwise
     *
     * @param component the component to test
     * @return {@code true} if the component is attached to a dialog
     * window, {@code false} otherwise
     */
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

    /**
     * Returns the value that represents an empty value of the passed component
     * if it supports it.
     *
     * @param component the component to get empty value
     * @param <V>       value type
     * @return empty value
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public static <V> V getEmptyValue(Component component) {
        return component instanceof HasValue
                ? ((HasValue<?, V>) component).getEmptyValue()
                : null;
    }

    /**
     * Returns the current value of the passed component considering {@link SupportsTypedValue}.
     *
     * @param component the component to get value
     * @param <V>       value type
     * @return the current component value
     */
    @Nullable
    public static <V> V getValue(HasValue<?, V> component) {
        return component instanceof SupportsTypedValue
                ? ((SupportsTypedValue<?, ?, V, ?>) component).getTypedValue()
                : component.getValue();
    }

    /**
     * Sets the current value of the passed component considering {@link SupportsTypedValue}.
     *
     * @param component the component to set value
     * @param value     the value to set
     * @param <V>       value type
     */
    public static <V> void setValue(HasValue<?, V> component, @Nullable V value) {
        if (component instanceof SupportsTypedValue) {
            ((SupportsTypedValue<?, ?, V, ?>) component).setTypedValue(value);
        } else {
            component.setValue(value);
        }
    }

    /**
     * Copies the value to the clipboard using an asynchronous JavaScript function call from the UI DOM element.
     *
     * @param valueToCopy the value to copy
     */
    public static PendingJavaScriptResult copyToClipboard(String valueToCopy) {
        return UI.getCurrent().getElement().executeJs(getCopyToClipboardScript(), valueToCopy);
    }

    /**
     * Creates a resources from the passed object.
     *
     * @param value              the object from which the resource will be created
     * @param fileStorageLocator fileStorageLocator
     * @param <V>                type of resource value
     * @return created resource or {@code null} if the value is of an unsupported type
     * @throws IllegalArgumentException if the URI resource can't be converted to URL
     */
    @Nullable
    public static <V> Object createResource(@Nullable V value, FileStorageLocator fileStorageLocator) {
        if (value == null) {
            return new StreamResource(UUID.randomUUID().toString(), InputStream::nullInputStream);
        }
        if (value instanceof byte[] byteValue) {
            return new StreamResource(UUID.randomUUID().toString(), () -> new ByteArrayInputStream(byteValue));
        }
        if (value instanceof FileRef fileRef) {
            return new StreamResource(fileRef.getFileName(), () ->
                    fileStorageLocator.getByName(fileRef.getStorageName()).openStream(fileRef));
        }
        if (value instanceof String) {
            return value;
        }
        if (value instanceof URI uri) {
            try {
                return uri.toURL().toString();
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Cannot convert provided URI `" + uri + "' to URL", e);
            }
        }

        return null;
    }

    /**
     * Gets JavaScript function for copying a value to the clipboard. A temporary invisible
     * {@code textarea} DOM element is used for copying.
     *
     * @return JavaScript copy function script
     */
    private static String getCopyToClipboardScript() {
        return """
                   const textarea = document.createElement("textarea");
                   textarea.value = $0;
                
                   textarea.style.position = "absolute";
                   textarea.style.opacity = "0";
                
                   document.body.appendChild(textarea);
                   textarea.select();
                   document.execCommand("copy");
                   document.body.removeChild(textarea);
                """;
    }

    /**
     * @deprecated Use {@link #traverseComponents(Component, Consumer)} instead.
     */
    @Deprecated(since = "2.6", forRemoval = true)
    public static void walkComponents(View<?> view, Consumer<ViewChildrenVisitResult> viewChildrenVisitResultConsumer) {
        __walkComponentsInternal(view, UiComponentUtils.getComponents(view), viewChildrenVisitResultConsumer, new HashSet<Component>());
    }

    @Deprecated(since = "2.6", forRemoval = true)
    private static void __walkComponentsInternal(View<?> view,
                                                 Collection<Component> currentChildrenComponents,
                                                 Consumer<ViewChildrenVisitResult> callback,
                                                 Set<Component> treeComponents) {
        for (Component component : currentChildrenComponents) {
            if (treeComponents.contains(component)) {
                break;
            }
            ViewChildrenVisitResult visitResult = new ViewChildrenVisitResult();
            visitResult.setComponent(component);
            visitResult.setView(view);
            visitResult.setComponentId(component.getId().orElse(null));
            callback.accept(visitResult);

            treeComponents.add(component);

            __walkComponentsInternal(view, UiComponentUtils.getComponents(view), callback, treeComponents);
        }
    }

    /**
     * Visit all components below the specified container.
     *
     * @param container container to start from
     * @param visitor   visitor instance
     */
    public static void traverseComponents(Component container, Consumer<Component> visitor) {
        getOwnComponents(container)
                .forEach(component -> {
                    visitor.accept(component);

                    if (isContainer(component)) {
                        traverseComponents(component, visitor);
                    }
                });
    }

    /**
     * Calls {@link Action#refreshState()} for all actions of the passed object.
     *
     * @param actionsHolder object containing {@link Action Actions}
     */
    public static void refreshActionsState(HasActions actionsHolder) {
        actionsHolder.getActions().forEach(Action::refreshState);
    }

    /**
     * Calls {@link Action#refreshState()} for all actions of the passed
     * component and its children.
     *
     * @param component component to refresh actions
     */
    public static void refreshActionsState(Component component) {
        if (component instanceof HasActions actionsHolder) {
            refreshActionsState(actionsHolder);
        }

        if (UiComponentUtils.isContainer(component)) {
            UiComponentUtils.traverseComponents(component, child -> {
                if (child instanceof HasActions actionsHolder) {
                    refreshActionsState(actionsHolder);
                }
            });
        }
    }

    /**
     * Returns the application event listeners associated with the specified component.
     * This method identifies the type of the component and delegates the retrieval of
     * event listeners to the appropriate utility class based on whether the component
     * is a View or a Fragment. If the component type is unsupported, an exception is thrown.
     *
     * @param component the component for which to retrieve application event listeners.
     *                  Must be an instance of {@link View} or {@link Fragment}
     * @return a list of application event listeners associated with the specified component
     * @throws IllegalArgumentException if the component type is unsupported
     */
    public static List<ApplicationListener<?>> getApplicationEventListeners(Component component) {
        if (component instanceof View<?> view) {
            return ViewControllerUtils.getApplicationEventListeners(view);
        } else if (component instanceof Fragment<?> fragment) {
            return FragmentUtils.getApplicationEventListeners(fragment);
        } else {
            throw new IllegalArgumentException(component.getClass().getSimpleName() +
                    " has no API to set application event listeners");
        }
    }

    /**
     * Sets the application event listeners for the provided component if it supports event
     * listener registration.
     *
     * @param component the component for which the application event listeners will be set.
     *                  Must be an instance of {@link View} or {@link Fragment}
     * @param listeners a list of application listeners to set for the given component
     * @throws IllegalArgumentException if the component type is unsupported
     */
    public static void setApplicationEventListeners(Component component,
                                                    @Nullable List<ApplicationListener<?>> listeners) {
        if (component instanceof View<?> view) {
            ViewControllerUtils.setApplicationEventListeners(view, listeners);
        } else if (component instanceof Fragment<?> fragment) {
            FragmentUtils.setApplicationEventListeners(fragment, listeners);
        } else {
            throw new IllegalArgumentException(component.getClass().getSimpleName() +
                    " has no API to set application event listeners");
        }
    }
}
