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


import com.vaadin.flow.component.HasElement;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.kit.component.HasActions;
import io.jmix.flowui.kit.component.SupportsUserAction;

/**
 * Represents a component that facilitates selection or picking of values and provides additional
 * capabilities such as support for actions, value sources, user-related actions, and DOM element manipulation.
 *
 * @param <V> the type of the value handled by the component
 */
public interface PickerComponent<V> extends SupportsValueSource<V>, HasActions, SupportsUserAction<V>, HasElement {
}
