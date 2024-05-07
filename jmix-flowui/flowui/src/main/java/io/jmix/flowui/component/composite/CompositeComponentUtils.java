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

import com.googlecode.gentyref.GenericTypeReflector;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.ReflectTools;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.view.View;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Optional;

public final class CompositeComponentUtils {

    public static final String ID_KEY = "compositeId";

    private CompositeComponentUtils() {
    }

    public static Optional<String> getComponentId(Component component) {
        Object data = ComponentUtil.getData(component, ID_KEY);
        return data instanceof String ? Optional.of((String) data) : Optional.empty();
    }

    public static void setComponentId(Component component, String id) {
        ComponentUtil.setData(component, ID_KEY, "id");
    }

    public static boolean sameId(Component component, String id) {
        Optional<String> componentId = getComponentId(component);
        return componentId.isPresent() && id.equals(componentId.get());
    }

    public static Optional<Component> findComponent(CompositeComponent<?> compositeComponent, String id) {
        Component content = compositeComponent.getContent();

        if (CompositeComponentUtils.sameId(content, id)) {
            return Optional.of(content);
        }

        if (UiComponentUtils.isContainer(content)) {
            // TODO: gg, re-implement
            return UiComponentUtils.findComponent(content, id);
        }

        throw new IllegalStateException(View.class.getSimpleName() + " content doesn't contain components");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void setContent(CompositeComponent<?> compositeComponent, Component content) {
        ((CompositeComponent) compositeComponent).setContent(content);
    }

    @SuppressWarnings("unchecked")
    public static <C extends Component> C createContent(Class<? extends CompositeComponent<C>> compositeClass) {
        return (C) ReflectTools.createInstance(findContentType(compositeClass));
    }

    private static Class<? extends Component> findContentType(Class<? extends CompositeComponent<?>> compositeClass) {
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
            return "Composite is used as raw type: either add type information or override initContent().";
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

    public static String getPackage(Class<? extends Component> componentClass) {
        Package javaPackage = componentClass.getPackage();
        return javaPackage != null ? javaPackage.getName() : "";
    }

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
     * Defines a mapping between this element and the given {@link Component}.
     * <p>
     * An element can only be mapped to one component and the mapping cannot be
     * changed. The only exception is {@link CompositeComponent} which can
     * overwrite the mapping for its content.
     *
     * @param element   the element to map to the component
     * @param component the component this element is attached to
     */
    // CAUTION: copied from com.vaadin.flow.dom.ElementUtil.setComponent [last update Vaadin 24.3.3]
    // TODO: gg, remove?
    public static void setComponent(Element element, Component component) {
        Preconditions.checkNotNullArgument(element, "Element must not be null");
        Preconditions.checkNotNullArgument(component, "Component must not be null");

        Optional<Component> currentComponent = element.getComponent();
        if (currentComponent.isPresent()) {
            // Composite can replace its content
            boolean isCompositeReplacingItsContent = component instanceof CompositeComponent<?>
                    && component.getChildren().findFirst().get() == currentComponent.get();
            if (!isCompositeReplacingItsContent) {
                throw new IllegalStateException("A component of type "
                        + currentComponent.get().getClass().getName()
                        + " is already attached to this element");
            }
        }
        element.getStateProvider().setComponent(element.getNode(), component);
    }
}
