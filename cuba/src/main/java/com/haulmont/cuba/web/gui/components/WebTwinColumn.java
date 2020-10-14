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

import com.haulmont.cuba.gui.components.TwinColumn;
import io.jmix.ui.component.impl.TwinColumnImpl;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;

@Deprecated
public class WebTwinColumn<V> extends TwinColumnImpl<V> implements TwinColumn<V> {

    protected int columns;

    @Override
    public int getColumns() {
        return columns;
    }

    @Override
    public void setColumns(int columns) {
        this.columns = columns;
        // see Vaadin 7 com.vaadin.ui.TwinColSelect#setColumns(int) for formula
        component.setWidth((columns * 2 + 4) + columns + "em");
    }

    @Override
    public void addValidator(Consumer<? super Collection<V>> validator) {
        addValidator(validator::accept);
    }

    @Override
    public void removeValidator(Consumer<Collection<V>> validator) {
        removeValidator(validator::accept);
    }

    @Override
    public void setOptionStyleProvider(@Nullable OptionStyleProvider<V> optionStyleProvider) {
        setOptionStyleProvider((item) -> optionStyleProvider.getStyleName(item, component.isSelected(item)));
    }
}
