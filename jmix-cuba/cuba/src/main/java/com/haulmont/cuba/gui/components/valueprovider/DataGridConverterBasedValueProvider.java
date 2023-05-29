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

package com.haulmont.cuba.gui.components.valueprovider;

import com.haulmont.cuba.gui.components.DataGrid;
import com.vaadin.data.ValueProvider;

public class DataGridConverterBasedValueProvider implements ValueProvider {

    protected DataGrid.Converter converter;

    public DataGridConverterBasedValueProvider(DataGrid.Converter converter) {
        this.converter = converter;
    }

    @Override
    public Object apply(Object value) {
        //noinspection unchecked
        return converter.convertToPresentation(value, Object.class, null);
    }

    public DataGrid.Converter getConverter() {
        return converter;
    }
}
