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

package io.jmix.flowui.component.delegate;

import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.binding.ValueBinding;

import javax.annotation.Nullable;
import java.util.EventObject;
import java.util.function.Consumer;

public interface ValueBindingDelegate<T> {

    ValueBinding<T> getValueBinding();

    Registration addValueBindingChangeListener(Consumer<ValueBindingChangeEvent<T>> listener);

    class ValueBindingChangeEvent<T> extends EventObject {

        protected final ValueBinding<T> valueBinding;

        public ValueBindingChangeEvent(ValueBindingDelegate<T> source, @Nullable ValueBinding<T> valueBinding) {
            super(source);
            this.valueBinding = valueBinding;
        }

        @SuppressWarnings("unchecked")
        @Override
        public ValueBindingDelegate<T> getSource() {
            return (ValueBindingDelegate<T>) super.getSource();
        }

        @Nullable
        public ValueSource<T> getValueSource() {
            return valueBinding != null
                    ? valueBinding.getValueSource()
                    : null;
        }

        @Nullable
        public ValueBinding<T> getValueBinding() {
            return valueBinding;
        }
    }
}
