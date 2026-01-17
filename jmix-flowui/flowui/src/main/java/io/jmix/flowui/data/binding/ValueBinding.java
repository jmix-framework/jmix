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

package io.jmix.flowui.data.binding;

import com.vaadin.flow.component.HasValue;
import io.jmix.flowui.data.ValueSource;

/**
 * Represents a binding between a UI component and a {@link ValueSource}.
 * This interface allows synchronization of the component's value with the
 * underlying value source and provides methods to manage this binding.
 *
 * @param <V> the type of the value being bound
 */
public interface ValueBinding<V> extends JmixBinding {

    /**
     * Returns the {@link ValueSource} associated with this binding.
     *
     * @return the value source used for this binding
     */
    ValueSource<V> getValueSource();

    /**
     * Returns the component associated with this binding.
     *
     * @return the component associated with this binding
     */
    HasValue<?, V> getComponent();

    /**
     * Activates binding between a UI component and a {@link ValueSource}.
     */
    void activate();
}
