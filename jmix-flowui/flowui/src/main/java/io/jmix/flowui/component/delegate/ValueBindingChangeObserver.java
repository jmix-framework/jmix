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

import io.jmix.flowui.component.delegate.ValueBindingDelegate.ValueBindingChangeEvent;

import java.util.function.Consumer;

public interface ValueBindingChangeObserver<T> extends Consumer<ValueBindingChangeEvent<T>> {

    void valueBindingChanged(ValueBindingChangeEvent<? extends T> event);

    @Override
    default void accept(ValueBindingChangeEvent<T> event) {
        valueBindingChanged(event);
    }
}
