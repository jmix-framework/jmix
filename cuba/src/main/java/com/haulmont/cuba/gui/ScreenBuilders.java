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

package com.haulmont.cuba.gui;

import com.haulmont.cuba.gui.builders.EditorBuilder;
import com.haulmont.cuba.gui.builders.LookupBuilder;
import com.haulmont.cuba.gui.builders.ScreenBuilder;
import io.jmix.ui.component.EntityPicker;
import io.jmix.ui.component.ListComponent;
import io.jmix.ui.screen.FrameOwner;

/**
 * @deprecated Use {@link io.jmix.ui.ScreenBuilders} instead
 */
@Deprecated
public class ScreenBuilders extends io.jmix.ui.ScreenBuilders {

    @Override
    public <E> EditorBuilder<E> editor(Class<E> entityClass, FrameOwner origin) {
        return new EditorBuilder<>(super.editor(entityClass, origin));
    }

    @Override
    public <E> EditorBuilder<E> editor(ListComponent<E> listComponent) {
        return new EditorBuilder<>(super.editor(listComponent));
    }

    @Override
    public <E> EditorBuilder<E> editor(EntityPicker<E> field) {
        return new EditorBuilder<>(super.editor(field));
    }

    @Override
    public <E> LookupBuilder<E> lookup(Class<E> entityClass, FrameOwner origin) {
        return new LookupBuilder<>(super.lookup(entityClass, origin));
    }

    @Override
    public <E> LookupBuilder<E> lookup(ListComponent<E> listComponent) {
        return new LookupBuilder<>(super.lookup(listComponent));
    }

    @Override
    public <E> LookupBuilder<E> lookup(EntityPicker<E> field) {
        return new LookupBuilder<>(super.lookup(field));
    }

    @Override
    public ScreenBuilder screen(FrameOwner origin) {
        return new ScreenBuilder(super.screen(origin));
    }
}
