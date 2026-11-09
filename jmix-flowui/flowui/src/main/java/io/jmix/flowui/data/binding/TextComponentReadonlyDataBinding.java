/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.data.binding;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.InstanceContainer;
import org.jspecify.annotations.NullMarked;

import java.util.function.Consumer;

/**
 * Binds a component with {@link ValueSource} or {@link InstanceContainer} to display entity property values.
 * Components implementing {@link HasText} use their text setter; other components can supply a custom setter.
 */
@NullMarked
public interface TextComponentReadonlyDataBinding {

    /**
     * Binds a component with {@link ValueSource} to show {@link ValueSource#getValue()} as the component text.
     *
     * @param component   component to show the value in
     * @param valueSource data component holding a typed value
     * @param <C>         type of the component
     * @return a registration object for removing an event listener
     */
    <C extends Component & HasText> Registration bind(C component, ValueSource<?> valueSource);

    /**
     * Binds a component with a value source using a custom setter for the formatted text.
     *
     * @param component   component whose attribute view permissions are checked
     * @param valueSource data component holding a typed value
     * @param textSetter  setter receiving the formatted value, or an empty string for a null value
     * @return a registration object for removing the binding's event listeners
     */
    Registration bind(Component component, ValueSource<?> valueSource, Consumer<String> textSetter);

    /**
     * Binds a component with {@link InstanceContainer} to show {@link InstanceContainer#getItem()} property
     * as the component text.
     * <p>
     * A {@link CollectionContainer} can be passed as well, in which case the property of its current item is shown.
     *
     * @param component     component to show the value in
     * @param dataContainer instance container
     * @param property      name of an entity property whose value is shown in the component
     * @param <C>           type of the component
     * @return a registration object for removing an event listener
     */
    <C extends Component & HasText> Registration bind(C component, InstanceContainer<?> dataContainer, String property);

    /**
     * Binds a component with a container property using a custom setter for the formatted text.
     * A collection container supplies the property of its current item.
     *
     * @param component     component whose attribute view permissions are checked
     * @param dataContainer instance or collection container
     * @param property      name of an entity property whose value is displayed
     * @param textSetter    setter receiving the formatted value, or an empty string for a null value or absent item
     * @return a registration object for removing the binding's event listeners
     */
    Registration bind(Component component, InstanceContainer<?> dataContainer, String property,
                      Consumer<String> textSetter);
}
