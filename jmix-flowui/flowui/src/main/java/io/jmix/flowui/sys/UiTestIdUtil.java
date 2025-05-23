/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowui.sys;

import com.vaadin.flow.component.Component;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.data.ContainerDataUnit;
import io.jmix.flowui.data.DataUnit;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.value.ContainerValueSource;
import org.springframework.lang.Nullable;

import static org.apache.commons.lang3.StringUtils.deleteWhitespace;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

/**
 * Utility class for generating and normalizing a UI test ID.
 */
public final class UiTestIdUtil {

    private UiTestIdUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param component component to normalize UI test ID
     * @return uncapitalized UI test ID without whitespaces with a component class name as a suffix
     */
    public static String getNormalizedTestId(Component component) {
        return getNormalizedTestId(component.getId().orElse(""), component);
    }

    /**
     * @param id        UI test ID
     * @param component component to normalize UI test ID
     * @return uncapitalized UI test ID without whitespaces with a component class name as a suffix
     */
    public static String getNormalizedTestId(String id, Component component) {
        return deleteWhitespace(uncapitalize(id + getComponentClassNameSuffix(component)));
    }

    /**
     * Computes the UI test ID for a UI component based on its {@link ValueSource}.
     *
     * @param valueSource UI component value source
     * @return computed UI test ID
     */
    @Nullable
    public static String getCalculatedTestId(ValueSource<?> valueSource) {
        if (valueSource instanceof ContainerValueSource<?, ?> containerValueSource) {
            return String.join("_", containerValueSource.getMetaPropertyPath().getPropertyNames());
        }

        return null;
    }

    /**
     * Computes the UI test ID for a UI components based on its {@link DataUnit}.
     *
     * @param dataUnit  UI component data unit
     * @param component UI component for computing ID suffix
     * @return computed UI test ID
     */
    @Nullable
    public static String getCalculatedTestId(DataUnit dataUnit, Component component) {
        if (dataUnit instanceof ContainerDataUnit<?> containerDataUnit) {
            MetaClass entityMetaClass = containerDataUnit.getEntityMetaClass();

            return entityMetaClass.getName() + getComponentClassNameSuffix(component);
        }

        return null;
    }

    private static String getComponentClassNameSuffix(Component component) {
        return component.getClass().getSimpleName();
    }
}
