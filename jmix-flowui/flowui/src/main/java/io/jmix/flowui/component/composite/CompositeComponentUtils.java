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

package io.jmix.flowui.component.composite;

import com.google.common.base.Strings;
import com.googlecode.gentyref.GenericTypeReflector;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.server.Attributes;
import io.jmix.core.DevelopmentException;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.view.View;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Optional;

/**
 * Utility class working with Jmix composite component specifics.
 */
public final class CompositeComponentUtils {

    public static final String ID_KEY = "compositeId";

    private CompositeComponentUtils() {
    }

    /**
     * Gets the composite id of the passed component.
     *
     * @param component the component to get composite id
     * @return the composite id of the passed component
     * @apiNote Composite id is stored in the component {@link Attributes} field
     * instead of setting it as actual id using {@link Component#setId(String)}.
     * This is done to avoid id duplicates in components tree when several
     * composite components of the same type is used simultaneously or composite
     * inner components have the same id as other components in the components tree
     */
    public static Optional<String> getComponentId(Component component) {
        Object data = ComponentUtil.getData(component, ID_KEY);
        return data instanceof String ? Optional.of((String) data) : Optional.empty();
    }

    /**
     * Sets the composite id to the passed component.
     * <p>
     *
     * @param component component to set composite id
     * @param id        id to set
     * @apiNote Composite id is stored in the component {@link Attributes} field
     * instead of setting it as actual id using {@link Component#setId(String)}.
     * This is done to avoid id duplicates in components tree when several
     * composite components of the same type is used simultaneously or composite
     * inner components have the same id as other components in the components tree
     */
    public static void setComponentId(Component component, String id) {
        ComponentUtil.setData(component, ID_KEY, id);
    }

    public static CompositeComponentActions getCompositeComponentActions(CompositeComponent<?> compositeComponent) {
        return compositeComponent.getActions();
    }

    public static void setCompositeComponentActions(CompositeComponent<?> compositeComponent,
                                                    CompositeComponentActions actions) {
        compositeComponent.setActions(actions);
    }

    /**
     * Returns whether the component has the same composite id as passed.
     *
     * @param component component to compare id
     * @param id        id to compare
     * @return {@code true} if the component has the same composite id as passed,
     * {@code false} otherwise
     * @see #getComponentId(Component)
     */
    public static boolean sameId(Component component, String id) {
        Optional<String> componentId = getComponentId(component);
        return componentId.isPresent() && id.equals(componentId.get());
    }

    /**
     * Returns an {@link Optional} describing the component with given composite id,
     * or an empty {@link Optional}.
     *
     * @param compositeComponent composite component to find an inner component
     * @param id                 component's composite id to find
     * @return an {@link Optional} describing the found component,
     * or an empty {@link Optional}
     * @see #getComponentId(Component)
     */
    public static Optional<Component> findComponent(CompositeComponent<?> compositeComponent, String id) {
        Component content = compositeComponent.getContent();
        if (CompositeComponentUtils.sameId(content, id)) {
            return Optional.of(content);
        }

        if (UiComponentUtils.isContainer(content)) {
            return UiComponentUtils.findComponent(content, id, CompositeComponentUtils::sameId);
        }

        throw new IllegalStateException(View.class.getSimpleName() + " content doesn't contain components");
    }

    /**
     * Returns a class that represents the type of the composite component content
     *
     * @param compositeClass the composite component class to get the type of content
     * @return a class that represents the type of the composite component content
     * @throws IllegalStateException if either composite component is used as raw type
     *                               or it is impossible to determine the composite content type
     */
    public static Class<? extends Component> findContentType(Class<? extends CompositeComponent<?>> compositeClass) {
        Type type = GenericTypeReflector.getTypeParameter(
                compositeClass.getGenericSuperclass(),
                CompositeComponent.class.getTypeParameters()[0]);
        if (type instanceof Class || type instanceof ParameterizedType) {
            return GenericTypeReflector.erase(type).asSubclass(Component.class);
        }
        throw new IllegalStateException(getExceptionMessage(type));
    }

    private static String getExceptionMessage(@Nullable Type type) {
        if (type == null) {
            return "Composite component is used as raw type: either add type information or override initContent().";
        }

        if (type instanceof TypeVariable) {
            return String.format(
                    "Could not determine the composite content type for TypeVariable '%s'. "
                            + "Either specify exact type or override initContent().",
                    type.getTypeName());
        }
        return String.format(
                "Could not determine the composite content type for %s. Override initContent().",
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
     * in the package of the composite component class.
     *
     * @param componentClass a component class for which to resolve XML descriptor path
     * @return a path to the XML descriptor
     */
    @Nullable
    public static String resolveDescriptorPath(Class<? extends Component> componentClass) {
        CompositeDescriptor descriptor = componentClass.getAnnotation(CompositeDescriptor.class);
        if (descriptor == null) {
            return null;
        }

        String descriptorPath = descriptor.value();
        if (Strings.isNullOrEmpty(descriptorPath)) {
            throw new DevelopmentException("Composite Component class annotated with @" +
                    CompositeDescriptor.class.getSimpleName() + " without template: " + componentClass);
        }

        if (!descriptorPath.startsWith("/")) {
            String packageName = CompositeComponentUtils.getPackage(componentClass);
            if (!Strings.isNullOrEmpty(packageName)) {
                String relativePath = packageName.replace('.', '/');
                descriptorPath = "/" + relativePath + "/" + descriptorPath;
            }
        }

        return descriptorPath;
    }
}
