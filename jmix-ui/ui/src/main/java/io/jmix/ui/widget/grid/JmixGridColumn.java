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

package io.jmix.ui.widget.grid;

import com.vaadin.data.ValueProvider;
import com.vaadin.ui.Grid;
import com.vaadin.ui.renderers.Renderer;

public class JmixGridColumn<T, V> extends Grid.Column<T, V> {

    public <P> JmixGridColumn(ValueProvider<T, V> valueProvider,
                              ValueProvider<V, P> presentationProvider,
                              Renderer<? super P> renderer) {
        super(valueProvider, presentationProvider, renderer);
    }

    @Override
    public Grid.Column<T, V> setEditable(boolean editable) {
        // Removed check that editorBinding is not null,
        // because we don't use Vaadin binding.
        getState().editable = editable;
        return this;
    }
}