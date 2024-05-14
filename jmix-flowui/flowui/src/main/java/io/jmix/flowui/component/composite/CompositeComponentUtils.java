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
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.view.View;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Optional;

// TODO: gg, JavaDoc
public final class CompositeComponentUtils {

    public static final String ID_KEY = "compositeId";

    private CompositeComponentUtils() {
    }

    public static Optional<String> getComponentId(Component component) {
        Object data = ComponentUtil.getData(component, ID_KEY);
        return data instanceof String ? Optional.of((String) data) : Optional.empty();
    }

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
            return UiComponentUtils.findComponent(content, id, CompositeComponentUtils::sameId);
        }

        throw new IllegalStateException(View.class.getSimpleName() + " content doesn't contain components");
    }

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
}
