/*
 * Copyright 2019 Haulmont.
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

package com.haulmont.cuba.web.gui.components;

import com.haulmont.cuba.gui.components.TextField;
import io.jmix.ui.component.impl.TextFieldImpl;

import java.util.function.Consumer;

@Deprecated
public class WebTextField<V>
        extends TextFieldImpl<V>
        implements TextField<V> {
    @Override
    public void addValidator(Consumer<? super V> validator) {
        addValidator(validator::accept);
    }

    @Override
    public void removeValidator(Consumer<V> validator) {
        removeValidator(validator::accept);
    }

    @Override
    public void removeTextChangeListener(Consumer<TextChangeEvent> listener) {
        unsubscribe(TextChangeEvent.class, listener);
    }

    @Override
    public void removeEnterPressListener(Consumer<EnterPressEvent> listener) {
        internalRemoveEnterPressListener(listener);
    }
}
