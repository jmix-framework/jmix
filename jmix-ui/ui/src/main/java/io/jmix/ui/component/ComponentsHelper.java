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
package io.jmix.ui.component;

import com.vaadin.ui.TabSheet;
import com.vaadin.ui.*;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.data.HasValueSource;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.component.impl.FrameImplementation;
import io.jmix.ui.screen.*;
import io.jmix.ui.sys.ValuePathHelper;
import io.jmix.ui.widget.JmixGroupBox;
import io.jmix.ui.widget.JmixScrollBoxLayout;
import org.apache.commons.collections4.iterators.ReverseListIterator;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

/**
 * Utility class working with GenericUI components.
 */
public abstract class ComponentsHelper {

    private ComponentsHelper() {
    }

    /**
     * Returns the collection of components within the specified container and all of its children.
     *
     * @param container container to start from
     * @return collection of components
     */
    public static Collection<Component> getComponents(HasComponents container) {
        // do not return LinkedHashSet, it uses much more memory than ArrayList
        Collection<Component> res = new ArrayList<>();

        fillChildComponents(container, res);

        if (res.isEmpty()) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableCollection(res);
    }

    /**
     * Visit all components below the specified container.
     *
     * @param container container to start from
     * @param visitor   visitor instance
     */
    public static void traverseComponents(HasComponents container, Consumer<Component> visitor) {
        container.getOwnComponentsStream()
                .forEach(c -> {
                    visitor.accept(c);

                    if (c instanceof HasComponents) {
                        traverseComponents((HasComponents) c, visitor);
                    }
                });
    }

    public static void traverseListComponents(HasComponents container, Consumer<ListComponent<?>> visitor) {
        container.getOwnComponentsStream()
                .forEach(c -> {
                    if(c instanceof ListComponent) {
                        visitor.accept((ListComponent<?>)c);
                    }

                    if (c instanceof HasComponents) {
                        traverseListComponents((HasComponents) c, visitor);
                    }
                });
    }

    /**
     * Visit all {@link Validatable} components below the specified container.
     *
     * @param container container to start from
     * @param visitor   visitor instance
     */
    public static void traverseValidatable(HasComponents container, Consumer<Validatable> visitor) {
        traverseComponents(container, c -> {
            if (c instanceof Validatable && ((Validatable) c).isValidateOnCommit()) {
                visitor.accept((Validatable) c);
            }
        });
    }

    @Nullable
    public static Component getWindowComponent(Window window, String id) {
        String[] elements = ValuePathHelper.parse(id);

        FrameImplementation frameImpl = (FrameImplementation) window;
        if (elements.length == 1) {
            return frameImpl.getRegisteredComponent(id);
        } else {
            Component innerComponent = frameImpl.getRegisteredComponent(elements[0]);
            if (innerComponent instanceof HasComponents) {

                String subPath = ValuePathHelper.pathSuffix(elements);
                return ((HasComponents) innerComponent).getComponent(subPath);
            } else if (innerComponent instanceof HasNamedComponents) {

                String subPath = ValuePathHelper.pathSuffix(elements);
                return ((HasNamedComponents) innerComponent).getComponent(subPath);
            }

            return null;
        }
    }

    @Nullable
    public static Component getFrameComponent(Frame frame, String id) {
        FrameImplementation frameImpl = (FrameImplementation) frame;
        String[] elements = ValuePathHelper.parse(id);
        if (elements.length == 1) {
            Component component = frameImpl.getRegisteredComponent(id);
            if (component == null && frame.getFrame() != null && frame.getFrame() != frame) {
                component = frame.getFrame().getComponent(id);
            }
            return component;
        } else {
            Component innerComponent = frameImpl.getRegisteredComponent(elements[0]);
            if (innerComponent instanceof HasComponents) {

                String subPath = ValuePathHelper.pathSuffix(elements);
                return ((HasComponents) innerComponent).getComponent(subPath);
            } else if (innerComponent instanceof HasNamedComponents) {

                String subPath = ValuePathHelper.pathSuffix(elements);
                return ((HasNamedComponents) innerComponent).getComponent(subPath);
            }

            return null;
        }
    }

    @Nullable
    public static Component getComponent(HasComponents container, String id) {
        String[] elements = ValuePathHelper.parse(id);
        if (elements.length == 1) {
            Component component = container.getOwnComponent(id);

            if (component == null) {
                return getComponentByIteration(container, id);
            } else {
                return component;
            }

        } else {
            Component innerComponent = container.getOwnComponent(elements[0]);

            if (innerComponent == null) {
                return getComponentByIteration(container, id);
            } else {
                if (innerComponent instanceof HasComponents) {
                    String subPath = ValuePathHelper.pathSuffix(elements);
                    return ((HasComponents) innerComponent).getComponent(subPath);
                } else if (innerComponent instanceof HasNamedComponents) {

                    String subPath = ValuePathHelper.pathSuffix(elements);
                    return ((HasNamedComponents) innerComponent).getComponent(subPath);
                }

                return null;
            }
        }
    }

    @Nullable
    private static Component getComponentByIteration(HasComponents container, String id) {
        return getComponentByIterationInternal(container.getOwnComponents(), id);
    }

    @Nullable
    private static Component getComponentByIterationInternal(Collection<Component> components, String id) {
        for (Component component : components) {
            if (id.equals(component.getId())) {
                return component;
            } else if (component instanceof HasComponents) {
                Collection<Component> ownComponents = ((HasComponents) component).getOwnComponents();
                Component innerComponent = getComponentByIterationInternal(ownComponents, id);
                if (innerComponent != null) {
                    return innerComponent;
                }
            } else if (component instanceof HasInnerComponents) {
                Collection<Component> innerComponents = ((HasInnerComponents) component).getInnerComponents();
                Component innerComponent = getComponentByIterationInternal(innerComponents, id);
                if (innerComponent != null) {
                    return innerComponent;
                }
            }
        }
        return null;
    }

    private static void fillChildComponents(HasComponents container, Collection<Component> components) {
        Collection<Component> ownComponents = container.getOwnComponents();
        components.addAll(ownComponents);

        for (Component component : ownComponents) {
            if (component instanceof HasComponents) {
                fillChildComponents((HasComponents) component, components);
            }
        }
    }

    /**
     * Searches for a component by identifier, down by the hierarchy of frames.
     *
     * @param frame frame to start from
     * @param id    component identifier
     * @return component instance or null if not found
     */
    @Nullable
    public static Component findComponent(Frame frame, String id) {
        Component find = frame.getComponent(id);
        if (find != null) {
            return find;
        } else {
            for (Component c : frame.getComponents()) {
                if (c instanceof Frame) {
                    Component nestedComponent = findComponent((Frame) c, id);
                    if (nestedComponent != null) {
                        return nestedComponent;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Visit all components below the specified container.
     *
     * @param container container to start from
     * @param visitor   visitor instance
     */
    public static void walkComponents(HasComponents container, ComponentVisitor visitor) {
        __walkComponents(container, visitor, "");
    }

    private static void __walkComponents(HasComponents container, ComponentVisitor visitor, String path) {
        for (Component component : container.getOwnComponents()) {
            __walkThroughComponent(component, visitor, path);
        }
    }

    private static void __walkInnerComponents(HasInnerComponents innerComponents, ComponentVisitor visitor, String path) {
        for (Component component : innerComponents.getInnerComponents()) {
            __walkThroughComponent(component, visitor, path);
        }
    }

    private static void __walkThroughComponent(Component component, ComponentVisitor visitor, String path) {
        String id = component.getId();
        if (id == null && component instanceof ActionOwner
                && ((ActionOwner) component).getAction() != null) {
            id = ((ActionOwner) component).getAction().getId();
        }
        if (id == null) {
            id = component.getClass().getSimpleName();
        }

        visitor.visit(component, path + id);

        String p = component instanceof Frame ?
                path + id + "." :
                path;

        if (component instanceof HasComponents) {
            __walkComponents(((HasComponents) component), visitor, p);
        } else if (component instanceof HasInnerComponents) {
            __walkInnerComponents((HasInnerComponents) component, visitor, p);
        }
    }

    /**
     * Iterates over all components applying finder instance.
     * Stops when the component is found and returns {@code true}.
     * If no component is found returns {@code false}.
     *
     * @param container container to start from
     * @param finder    finder instance
     * @return {@code true} if component has been found, {@code false} otherwise
     */
    public static boolean walkComponents(HasComponents container,
                                         ComponentFinder finder) {
        return __walkComponents(container, finder);
    }

    private static boolean __walkComponents(HasComponents container,
                                            ComponentFinder finder) {
        for (Component component : container.getOwnComponents()) {
            if (finder.visit(component)) {
                return true;
            }

            if (component instanceof HasComponents) {
                if (__walkComponents(((HasComponents) component), finder)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the topmost window for the specified component.
     *
     * @param component component instance
     * @return topmost window in the hierarchy of frames for this component.
     * <br>Can be null only if the component wasn't properly initialized.
     */
    @Nullable
    public static Window getWindow(Component.BelongToFrame component) {
        Frame frame = component.getFrame();
        while (frame != null) {
            if (frame instanceof Window && frame.getFrame() == frame) {
                return (Window) frame;
            }
            frame = frame.getFrame();
        }
        return null;
    }

    public static Window getWindowNN(Component.BelongToFrame component) {
        Window window = getWindow(component);

        if (window == null) {
            throw new IllegalStateException("Unable to find window for component " +
                    (component.getId() != null ? component.getId() : component.getClass()));
        }

        return window;
    }

    /**
     * Get screen context for UI component.
     *
     * @param component component
     * @return screen context
     * @throws IllegalStateException in case window cannot be inferred
     */
    public static ScreenContext getScreenContext(Component.BelongToFrame component) {
        Window window = getWindowNN(component);

        return UiControllerUtils.getScreenContext(window.getFrameOwner());
    }

    @Nullable
    public static Screen getScreen(ScreenFragment frameOwner) {
        Frame frame = frameOwner.getFragment();
        while (frame != null) {
            if (frame instanceof Window && frame.getFrame() == frame) {
                return ((Window) frame).getFrameOwner();
            }
            frame = frame.getFrame();
        }
        return null;
    }

    @Nullable
    public static Window getParentWindow(ScreenFragment frameOwner) {
        Frame frame = frameOwner.getFragment();
        while (frame != null) {
            if (frame instanceof Window && frame.getFrame() == frame) {
                return (Window) frame;
            }
            frame = frame.getFrame();
        }
        return null;
    }

    /**
     * Get the topmost window for the specified component.
     *
     * @param component component instance
     * @return topmost client specific window in the hierarchy of frames for this component.
     *
     * <br>Can be null only if the component wasn't properly initialized.
     */
    @Nullable
    public static Window getWindowImplementation(Component.BelongToFrame component) {
        Frame frame = component.getFrame();
        while (frame != null) {
            if (frame instanceof Window && frame.getFrame() == frame) {
                return (Window) frame;
            }
            frame = frame.getFrame();
        }
        return null;
    }

    public static String getFullFrameId(Frame frame) {
        if (frame instanceof Window) {
            return frame.getId();
        }

        List<String> frameIds = new ArrayList<>(4);
        frameIds.add(frame.getId());
        while (frame != null && !(frame instanceof Window) && frame != frame.getFrame()) {
            frame = frame.getFrame();
            if (frame != null) {
                frameIds.add(frame.getId());
            }
        }

        return StringUtils.join(new ReverseListIterator<>(frameIds), '.');
    }

    /**
     * Searches for an action by name.
     *
     * @param actionName action name, can be a path to an action contained in some {@link ActionsHolder}
     * @param frame      current frame
     * @return action instance or null if there is no action with the specified name
     * @throws IllegalStateException if the component denoted by the path doesn't exist or is not an ActionsHolder
     */
    @Nullable
    public static Action findAction(String actionName, Frame frame) {
        String[] elements = ValuePathHelper.parse(actionName);
        if (elements.length > 1) {
            String id = elements[elements.length - 1];

            String prefix = ValuePathHelper.pathPrefix(elements);
            Component component = frame.getComponent(prefix);
            if (component != null) {
                if (component instanceof ActionsHolder) {
                    return ((ActionsHolder) component).getAction(id);
                } else {
                    throw new IllegalArgumentException(
                            String.format("Component '%s' can't contain actions", prefix));
                }
            } else {
                throw new IllegalArgumentException(
                        String.format("Can't find component '%s'", prefix));
            }
        } else if (elements.length == 1) {
            String id = elements[0];
            return frame.getAction(id);
        } else {
            throw new IllegalArgumentException("Invalid action name: " + actionName);
        }
    }

    public static String getComponentPath(Component c) {
        StringBuilder sb = new StringBuilder(c.getId() == null ? "" : c.getId());
        if (c instanceof Component.BelongToFrame) {
            Frame frame = ((Component.BelongToFrame) c).getFrame();
            while (frame != null) {
                sb.insert(0, ".");
                String s = frame.getId();
                if (s.contains(".")) {
                    s = "[" + s + "]";
                }
                sb.insert(0, s);
                if (frame instanceof Window) {
                    break;
                }
                frame = frame.getFrame();
            }
        }
        return sb.toString();
    }

    public static String getComponentWidth(Component c) {
        float width = c.getWidth();
        SizeUnit widthUnit = c.getWidthSizeUnit();
        return width + widthUnit.getSymbol();
    }

    public static String getComponentHeight(Component c) {
        float height = c.getHeight();
        SizeUnit heightUnit = c.getHeightSizeUnit();
        return height + heightUnit.getSymbol();
    }

    /**
     * Place component with error message to validation errors container.
     *
     * @param component validatable component
     * @param e         exception
     * @param errors    errors container
     */
    public static void fillErrorMessages(Validatable component, ValidationException e,
                                         ValidationErrors errors) {
        if (e instanceof ValidationException.HasRelatedComponent) {
            errors.add(((ValidationException.HasRelatedComponent) e).getComponent(), e.getMessage());
        } else if (e instanceof CompositeValidationException) {
            for (CompositeValidationException.ViolationCause cause : ((CompositeValidationException) e).getCauses()) {
                errors.add((Component) component, cause.getMessage());
            }
        } else {
            errors.add((Component) component, e.getMessage());
        }
    }

    public static int findActionById(List<Action> actionList, String actionId) {
        int oldIndex = -1;
        for (int i = 0; i < actionList.size(); i++) {
            Action a = actionList.get(i);
            if (Objects.equals(a.getId(), actionId)) {
                oldIndex = i;
                break;
            }
        }
        return oldIndex;
    }

    /**
     * Focus component (or its nearest focusable parent) and activate all its parents,
     * for instance: select Tab, expand GroupBox.
     *
     * @param component component
     */
    public static void focusComponent(Component component) {
        Component parent = component;
        Component previousComponent = null;

        // activate all parent containers, select Tab in TabSheet, expand GroupBox
        while (parent != null) {
            if (parent instanceof Collapsable
                    && ((Collapsable) parent).isCollapsable()
                    && !((Collapsable) parent).isExpanded()) {
                ((Collapsable) parent).setExpanded(true);
            }

            if (parent instanceof SupportsChildrenSelection
                    && previousComponent != null) {
                ((SupportsChildrenSelection) parent).setChildSelected(previousComponent);
            }

            previousComponent = parent;
            parent = parent.getParent();
        }

        // focus first focusable parent
        parent = component;
        while (parent != null && !(parent instanceof Component.Focusable)) {
            parent = parent.getParent();
        }
        if (parent != null) {
            ((Component.Focusable) parent).focus();
        }
    }

    @Nullable
    public static Component.Focusable focusChildComponent(Component container) {
        if (!container.isEnabledRecursive()) {
            return null;
        }
        if (!container.isVisibleRecursive()) {
            return null;
        }

        Collection<Component> components;
        if (container instanceof HasComponents) {
            components = ((HasComponents) container).getOwnComponents();
        } else if (container instanceof HasInnerComponents) {
            components = ((HasInnerComponents) container).getInnerComponents();
        } else {
            return null;
        }

        for (Component component : components) {
            if (component.isVisible()
                    && component.isEnabled()) {

                boolean reachable = true;
                if (component.getParent() instanceof SupportsChildrenSelection) {
                    reachable = ((SupportsChildrenSelection) component.getParent()).isChildSelected(component);
                } else if (component.getParent() instanceof Collapsable) {
                    reachable = ((Collapsable) component.getParent()).isExpanded();
                }

                if (reachable) {
                    if (component instanceof Component.Focusable) {
                        Component.Focusable focusable = (Component.Focusable) component;

                        focusable.focus();
                        return focusable;
                    }

                    if (component instanceof HasComponents
                            || component instanceof HasInnerComponents) {
                        Component.Focusable focused = focusChildComponent(component);
                        if (focused != null) {
                            return focused;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Null-safe helper analog of {@link Component#addStyleName(String)} and {@link Component#removeStyleName(String)}
     * <p>
     * Adds or removes a style name. Multiple styles can be specified as a space-separated list of style names.
     * <p>
     * If the {@code add} parameter is true, the style name is added to the component.
     * If the {@code add} parameter is false, the style name is removed from the component.
     *
     * @param component component
     * @param styleName style name to add or remove
     * @param add       add style name, or remove
     */
    public static void setStyleName(@Nullable Component component, String styleName, boolean add) {
        if (component == null) {
            return;
        }
        if (add) {
            component.addStyleName(styleName);
        } else {
            component.removeStyleName(styleName);
        }
    }

    /**
     * Searches for action with the given {@code actionId} inside of {@code frame}.
     *
     * @param frame    frame
     * @param actionId action id
     * @return action instance or null if action not found
     */
    @Nullable
    public static Action findAction(Frame frame, String actionId) {
        Action action = frame.getAction(actionId);

        if (action == null) {
            String postfixActionId = null;
            int dotIdx = actionId.indexOf('.');
            if (dotIdx > 0) {
                postfixActionId = actionId.substring(dotIdx + 1);
            }

            for (io.jmix.ui.component.Component c : frame.getComponents()) {
                if (c instanceof ActionsHolder) {
                    ActionsHolder actionsHolder = (ActionsHolder) c;
                    action = actionsHolder.getAction(actionId);
                    if (action == null) {
                        action = actionsHolder.getAction(postfixActionId);
                    }
                    if (action != null) {
                        break;
                    }
                }
            }
        }

        return action;
    }

    @SuppressWarnings("unchecked")
    public static <T extends com.vaadin.ui.Component> Collection<T> getComponents(com.vaadin.ui.HasComponents container, Class<T> aClass) {
        List<T> res = new ArrayList<>();
        for (Object aContainer : container) {
            com.vaadin.ui.Component component = (com.vaadin.ui.Component) aContainer;
            if (aClass.isAssignableFrom(component.getClass())) {
                res.add((T) component);
            } else if (com.vaadin.ui.HasComponents.class.isAssignableFrom(component.getClass())) {
                res.addAll(getComponents((com.vaadin.ui.HasComponents) component, aClass));
            }
        }

        return res;
    }

    /**
     * Returns underlying Vaadin component implementation.
     *
     * @param component GUI component
     * @return Vaadin component
     * @see #getComposition(io.jmix.ui.component.Component)
     */
    public static com.vaadin.ui.Component unwrap(io.jmix.ui.component.Component component) {
        Object comp = component;
        while (comp instanceof io.jmix.ui.component.Component.Wrapper) {
            comp = ((io.jmix.ui.component.Component.Wrapper) comp).getComponent();
        }

        return comp instanceof io.jmix.ui.component.Component
                ? ((io.jmix.ui.component.Component) comp).unwrapComposition(com.vaadin.ui.Component.class)
                : (com.vaadin.ui.Component) comp;
    }

    /**
     * Returns underlying Vaadin component, which serves as the outermost container for the supplied GUI component.
     * For simple components like {@link io.jmix.ui.component.Button} this method returns the same
     * result as {@link #unwrap(io.jmix.ui.component.Component)}.
     *
     * @param component GUI component
     * @return Vaadin component
     * @see #unwrap(io.jmix.ui.component.Component)
     */
    public static com.vaadin.ui.Component getComposition(io.jmix.ui.component.Component component) {
        Object comp = component;
        while (comp instanceof io.jmix.ui.component.Component.Wrapper) {
            comp = ((io.jmix.ui.component.Component.Wrapper) comp).getComposition();
        }

        return comp instanceof io.jmix.ui.component.Component
                ? ((io.jmix.ui.component.Component) comp).unwrapComposition(com.vaadin.ui.Component.class)
                : (com.vaadin.ui.Component) comp;
    }

    /**
     * Checks if the component should be visible to the client. Returns false if
     * the child should not be sent to the client, true otherwise.
     *
     * @param child The child to check
     * @return true if the child is visible to the client, false otherwise
     */
    public static boolean isComponentVisibleToClient(com.vaadin.ui.Component child) {
        if (!child.isVisible()) {
            return false;
        }
        com.vaadin.ui.HasComponents parent = child.getParent();

        if (parent instanceof SelectiveRenderer) {
            if (!((SelectiveRenderer) parent).isRendered(child)) {
                return false;
            }
        }

        if (parent != null) {
            return isComponentVisibleToClient(parent);
        } else {
            // 'true' - UI has no parent and visibility was checked above,
            // otherwise - component which is not attached to any UI
            return child instanceof UI;

        }
    }

    /**
     * Tests if component visible and its container visible.
     *
     * @param child component
     * @return component visibility
     */
    public static boolean isComponentVisible(com.vaadin.ui.Component child) {
        if (child.getParent() instanceof com.vaadin.ui.TabSheet) {
            com.vaadin.ui.TabSheet tabSheet = (com.vaadin.ui.TabSheet) child.getParent();
            com.vaadin.ui.TabSheet.Tab tab = tabSheet.getTab(child);
            if (!tab.isVisible()) {
                return false;
            }
        }

        if (child.getParent() instanceof JmixGroupBox) {
            // ignore groupbox content container visibility
            return isComponentVisible(child.getParent());
        }
        return child.isVisible() && (child.getParent() == null
                || isComponentVisible(child.getParent()));
    }

    /**
     * Tests if component enabled and visible and its container enabled.
     *
     * @param child component
     * @return component enabled state
     */
    public static boolean isComponentEnabled(com.vaadin.ui.Component child) {
        if (child.getParent() instanceof com.vaadin.ui.TabSheet) {
            com.vaadin.ui.TabSheet tabSheet = (com.vaadin.ui.TabSheet) child.getParent();
            TabSheet.Tab tab = tabSheet.getTab(child);
            if (!tab.isEnabled()) {
                return false;
            }
        }

        return child.isEnabled() && (child.getParent() == null
                || isComponentEnabled(child.getParent())) && isComponentVisible(child);
    }

    public static boolean isComponentExpanded(io.jmix.ui.component.Component component) {
        com.vaadin.ui.Component vComponent = component.unwrapComposition(com.vaadin.ui.Component.class);
        if (vComponent.getParent() instanceof AbstractOrderedLayout) {
            AbstractOrderedLayout layout = (AbstractOrderedLayout) vComponent.getParent();
            return (int) layout.getExpandRatio(vComponent) == 1;
        }

        return false;
    }

    public static ShortcutTriggeredEvent getShortcutEvent(io.jmix.ui.component.Component source,
                                                          com.vaadin.ui.Component target) {
        com.vaadin.ui.Component vaadinSource = getVaadinSource(source);

        if (vaadinSource == target) {
            return new ShortcutTriggeredEvent(source, source);
        }

        if (source instanceof io.jmix.ui.component.HasComponents) {
            io.jmix.ui.component.HasComponents container = (io.jmix.ui.component.HasComponents) source;
            io.jmix.ui.component.Component childComponent =
                    findChildComponent(container, target);
            return new ShortcutTriggeredEvent(source, childComponent);
        }

        return new ShortcutTriggeredEvent(source, null);
    }

    public static com.vaadin.ui.Component getVaadinSource(io.jmix.ui.component.Component source) {
        com.vaadin.ui.Component component = source.unwrapComposition(com.vaadin.ui.Component.class);
        if (component instanceof AbstractSingleComponentContainer) {
            return ((AbstractSingleComponentContainer) component).getContent();
        }


        if (component instanceof JmixScrollBoxLayout) {
            return ((JmixScrollBoxLayout) component).getComponent(0);
        }

        return component;
    }

    @Nullable
    public static io.jmix.ui.component.Component findChildComponent(io.jmix.ui.component.HasComponents container,
                                                                    com.vaadin.ui.Component target) {
        com.vaadin.ui.Component vaadinSource = getVaadinSource(((io.jmix.ui.component.Component) container));
        Collection<io.jmix.ui.component.Component> components = container.getOwnComponents();

        return findChildComponent(components, vaadinSource, target);
    }

    @Nullable
    public static io.jmix.ui.component.Component findChildComponent(
            Collection<io.jmix.ui.component.Component> components,
            com.vaadin.ui.Component vaadinSource, com.vaadin.ui.Component target) {
        com.vaadin.ui.Component targetComponent = getDirectChildComponent(target, vaadinSource);

        for (io.jmix.ui.component.Component component : components) {
            com.vaadin.ui.Component unwrapped = component.unwrapComposition(com.vaadin.ui.Component.class);
            if (unwrapped == targetComponent) {
                io.jmix.ui.component.Component child = null;

                if (component instanceof io.jmix.ui.component.HasComponents) {
                    child = findChildComponent((io.jmix.ui.component.HasComponents) component, target);
                }


                if (component instanceof HasButtonsPanel) {
                    ButtonsPanel buttonsPanel = ((HasButtonsPanel) component).getButtonsPanel();
                    if (buttonsPanel != null) {
                        if (getVaadinSource(buttonsPanel) == target) {
                            return buttonsPanel;
                        } else {
                            child = findChildComponent(buttonsPanel, target);
                        }
                    }
                }

                return child != null ? child : component;
            }
        }
        return null;
    }

    /**
     * @return the direct child component of the layout which contains the component involved to event
     */
    @Nullable
    public static com.vaadin.ui.Component getDirectChildComponent(com.vaadin.ui.Component targetComponent,
                                                                  com.vaadin.ui.Component vaadinSource) {
        while (targetComponent != null
                && targetComponent.getParent() != vaadinSource) {
            targetComponent = targetComponent.getParent();
        }

        return targetComponent;
    }

    public static void setClickShortcut(com.vaadin.ui.Button button, String shortcut) {
        KeyCombination closeCombination = KeyCombination.create(shortcut);
        int[] closeModifiers = KeyCombination.Modifier.codes(closeCombination.getModifiers());
        int closeCode = closeCombination.getKey().getCode();

        button.setClickShortcut(closeCode, closeModifiers);
    }

    @Nullable
    public static MetaPropertyPath getMetaPropertyPath(HasValueSource<?> component) {
        ValueSource<?> valueSource = component.getValueSource();
        if (valueSource instanceof EntityValueSource) {
            return ((EntityValueSource<?, ?>) valueSource).getMetaPropertyPath();
        }

        return null;
    }

    @Nullable
    public static MetaProperty getMetaProperty(HasValueSource<?> component) {
        MetaPropertyPath metaPropertyPath = getMetaPropertyPath(component);
        if (metaPropertyPath != null) {
            return metaPropertyPath.getMetaProperty();
        }

        return null;
    }
}
