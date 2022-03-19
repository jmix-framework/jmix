/*
 * Copyright 2020 Haulmont.
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

import com.haulmont.cuba.gui.components.TextArea;
import io.jmix.ui.component.impl.TextAreaImpl;

import java.util.function.Consumer;

@Deprecated
public class WebTextArea<V> extends TextAreaImpl<V> implements TextArea<V> {

    protected int columns;

    @Override
    public void addValidator(Consumer<? super V> validator) {
        addValidator(validator::accept);
    }

    @Override
    public void removeValidator(Consumer<V> validator) {
        removeValidator(validator::accept);
    }

    @Override
    public int getColumns() {
        return columns;
    }

    @Override
    public void setColumns(int columns) {
        this.columns = columns;
        // See com.vaadin.v7.client.ui.VTextField.setColumns for formula
        component.setWidth(columns + "em");
    }

    @Override
    public void removeTextChangeListener(Consumer<TextChangeEvent> listener) {
        unsubscribe(TextChangeEvent.class, listener);
    }
}
