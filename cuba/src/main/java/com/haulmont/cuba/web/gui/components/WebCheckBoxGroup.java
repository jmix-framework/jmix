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

import com.haulmont.cuba.gui.components.CheckBoxGroup;
import io.jmix.ui.component.impl.CheckBoxGroupImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.function.Consumer;

@Deprecated
public class WebCheckBoxGroup<I> extends CheckBoxGroupImpl<I> implements CheckBoxGroup<I> {
    @Override
    public void addValidator(Consumer<? super Collection<I>> validator) {
        addValidator(validator::accept);
    }

    @Override
    public void removeValidator(Consumer<Collection<I>> validator) {
        removeValidator(validator::accept);
    }

    @Override
    public Collection<I> getLookupSelectedItems() {
        Collection<I> value = getValue();
        return value != null
                ? Collections.unmodifiableSet(new LinkedHashSet<>(value))
                : Collections.emptySet();
    }

    @Override
    public void setLookupSelectHandler(Consumer<Collection<I>> selectHandler) {
        // do nothing
    }
}
