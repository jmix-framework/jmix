/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.fragment;

import com.google.common.base.Strings;
import com.googlecode.gentyref.GenericTypeReflector;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.server.Attributes;
import io.jmix.core.DevelopmentException;
import io.jmix.flowui.component.UiComponentUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Optional;

/**
 * Utility class working with Jmix fragment specifics.
 */
public final class FragmentUtils {

    public static final String ID_KEY = "fragmentId";

    private FragmentUtils() {
    }

    /**
     * Gets the fragment id of the passed component.
     * <p>
     * Note: Fragment id is stored in the component {@link Attributes} field
     * instead of setting it as actual id using {@link Component#setId(String)}.
     * This is done to avoid id duplicates in components tree when several
     * fragments of the same type is used simultaneously or fragment inner
     * components have the same id as other components in the components tree
     *
     * @param component the component to get fragment id
     * @return the fragment id of the passed component
     */
    public static Optional<String> getComponentId(Component component) {
        Object data = ComponentUtil.getData(component, ID_KEY);
        return data instanceof String ? Optional.of((String) data) : Optional.empty();
    }

    /**
     * Sets the fragment id to the passed component.
     * <p>
     * Note: Fragment id is stored in the component {@link Attributes} field
     * instead of setting it as actual id using {@link Component#setId(String)}.
     * This is done to avoid id duplicates in components tree when several
     * fragments of the same type is used simultaneously or fragment inner
     * components have the same id as other components in the components tree
     *
     * @param component component to set fragment id
     * @param id        id to set
     */
    public static void setComponentId(Component component, String id) {
        ComponentUtil.setData(component, ID_KEY, id);
    }

    /**
     * Gets {@link FragmentData} object from passed {@link Fragment}.
     *
     * @param fragment fragment to get data holder
     * @return {@link FragmentData} object from passed {@link Fragment}
     */
    public static FragmentData getFragmentData(Fragment<?> fragment) {
        return fragment.getFragmentData();
    }

    /**
     * Sets {@link FragmentData} object to passed {@link Fragment}.
     *
     * @param fragment fragment to set data holder
     * @param data     data holder to set
     */
    public static void setFragmentData(Fragment<?> fragment, FragmentData data) {
        fragment.setFragmentData(data);
    }

    /**
     * Gets {@link FragmentActions} object from passed {@link Fragment}.
     *
     * @param fragment fragment to get actions holder
     * @return {@link FragmentActions} object from passed {@link Fragment}
     */
    public static FragmentActions getFragmentActions(Fragment<?> fragment) {
        return fragment.getFragmentActions();
    }

    /**
     * Sets {@link FragmentActions} object to passed {@link Fragment}.
     *
     * @param fragment fragment to set actions holder
     * @param actions  actions  holder to set
     */
    public static void setFragmentActions(Fragment<?> fragment,
                                          FragmentActions actions) {
        fragment.setFragmentActions(actions);
    }

    /**
     * Gets the owner of the passed {@link Fragment}.
     *
     * @param fragment fragment to get the owner
     * @return the owner of the passed fragment
     */
    public static FragmentOwner getParentController(Fragment<?> fragment) {
        return fragment.getParentController();
    }

    /**
     * Sets the owner to the passed {@link Fragment}.
     *
     * @param fragment         fragment to set the owner
     * @param parentController the owner
     */
    public static void setParentController(Fragment<?> fragment, FragmentOwner parentController) {
        fragment.setParentController(parentController);
    }

    /**
     * Returns whether the component has the same fragment id as passed.
     *
     * @param component component to compare id
     * @param id        id to compare
     * @return {@code true} if the component has the same fragment id as passed,
     * {@code false} otherwise
     * @see #getComponentId(Component)
     */
    public static boolean sameId(Component component, String id) {
        Optional<String> componentId = getComponentId(component);
        return componentId.isPresent() && id.equals(componentId.get());
    }

    /**
     * Returns an {@link Optional} describing the component with given fragment id,
     * or an empty {@link Optional}.
     *
     * @param fragment fragment to find an inner component
     * @param id       component's fragment id to find
     * @return an {@link Optional} describing the found component, or an empty {@link Optional}
     * @see #getComponentId(Component)
     */
    public static Optional<Component> findComponent(Fragment<?> fragment, String id) {
        Component content = fragment.getContent();
        if (FragmentUtils.sameId(content, id)) {
            return Optional.of(content);
        }

        if (UiComponentUtils.isContainer(content)) {
            return UiComponentUtils.findComponent(content, id, FragmentUtils::sameId);
        }

        throw new IllegalStateException(Fragment.class.getSimpleName() + " content doesn't contain components");
    }

    /**
     * Returns a class that represents the type of the fragment content
     *
     * @param fragmentClass the fragment class to get the type of content
     * @return a class that represents the type of the fragment content
     * @throws IllegalStateException if either fragment is used as raw type
     *                               or it is impossible to determine the fragment content type
     */
    public static Class<? extends Component> findContentType(Class<? extends Fragment<?>> fragmentClass) {
        Type type = GenericTypeReflector.getTypeParameter(
                fragmentClass.getGenericSuperclass(),
                Fragment.class.getTypeParameters()[0]);
        if (type instanceof Class || type instanceof ParameterizedType) {
            return GenericTypeReflector.erase(type).asSubclass(Component.class);
        }
        throw new IllegalStateException(getExceptionMessage(type));
    }

    private static String getExceptionMessage(@Nullable Type type) {
        if (type == null) {
            return Fragment.class.getSimpleName() + " is used as raw type: either add " +
                    "type information or override initContent().";
        }

        if (type instanceof TypeVariable) {
            return String.format(
                    "Could not determine the fragment content type for TypeVariable '%s'. "
                            + "Either specify exact type or override initContent().",
                    type.getTypeName());
        }
        return String.format(
                "Could not determine the fragment content type for %s. Override initContent().",
                type.getTypeName());
    }

    /**
     * Gets the package of the passed class.
     *
     * @param componentClass a class to get package
     * @return the package of the passed class
     */
    public static String getPackage(Class<? extends Component> componentClass) {
        Package javaPackage = componentClass.getPackage();
        return javaPackage != null ? javaPackage.getName() : "";
    }

    /**
     * Gets message group from the passed descriptor path.
     *
     * @param descriptorPath descriptor path to get message group
     * @return message group from the passed descriptor path
     */
    public static String getMessageGroup(String descriptorPath) {
        if (descriptorPath.contains("/")) {
            descriptorPath = StringUtils.substring(descriptorPath, 0, descriptorPath.lastIndexOf("/"));
        }

        String messageGroup = descriptorPath.replace("/", ".");
        int start = messageGroup.startsWith(".") ? 1 : 0;
        messageGroup = messageGroup.substring(start);
        return messageGroup;
    }

    /**
     * Resolves a path to the XML descriptor. If the value contains a file name
     * only (i.e. don't start with '/'), it is assumed that the file is located
     * in the package of the fragment class.
     *
     * @param componentClass a component class for which to resolve XML descriptor path
     * @return a path to the XML descriptor
     */
    @Nullable
    public static String resolveDescriptorPath(Class<? extends Component> componentClass) {
        FragmentDescriptor descriptor = componentClass.getAnnotation(FragmentDescriptor.class);
        if (descriptor == null) {
            return null;
        }

        String descriptorPath = descriptor.value();
        if (Strings.isNullOrEmpty(descriptorPath)) {
            throw new DevelopmentException(Fragment.class.getSimpleName() + " class annotated with @" +
                    FragmentDescriptor.class.getSimpleName() + " without template: " + componentClass);
        }

        if (!descriptorPath.startsWith("/")) {
            String packageName = FragmentUtils.getPackage(componentClass);
            if (!Strings.isNullOrEmpty(packageName)) {
                String relativePath = packageName.replace('.', '/');
                descriptorPath = "/" + relativePath + "/" + descriptorPath;
            }
        }

        return descriptorPath;
    }

    /**
     * Returns the list of application event listeners associated with the specified {@link Fragment}.
     *
     * @param fragment the {@link Fragment} for which to retrieve the application event listeners
     * @return a list of application event listeners associated with the specified {@link Fragment}
     */
    public static List<ApplicationListener<?>> getApplicationEventListeners(Fragment<?> fragment) {
        return fragment.getApplicationEventListeners();
    }

    /**
     * Sets the application event listeners for the specified {@link Fragment}.
     *
     * @param fragment  the {@link Fragment} for which to set the application event listeners
     * @param listeners a list of application event listeners to be set for the {@link Fragment},
     *                  or {@code null} if no listeners should be associated
     */
    public static void setApplicationEventListeners(Fragment<?> fragment,
                                                    @Nullable List<ApplicationListener<?>> listeners) {
        fragment.setApplicationEventListeners(listeners);
    }
}
