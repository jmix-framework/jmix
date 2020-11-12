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

import com.google.common.base.Preconditions;
import com.haulmont.cuba.gui.screen.OpenMode;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.app.bulk.BulkEditorWindow;
import io.jmix.ui.app.bulk.ColumnsMode;
import io.jmix.ui.app.bulk.FieldSorter;
import io.jmix.ui.bulk.BulkEditorBuilder;
import io.jmix.ui.component.ListComponent;
import io.jmix.ui.component.validation.Validator;
import io.jmix.ui.screen.FrameOwner;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A bean that creates an instance of {@link BulkEditorBuilder}.
 *
 * @deprecated Use {@link io.jmix.ui.bulk.BulkEditors} instead
 */
@Deprecated
public class BulkEditors extends io.jmix.ui.bulk.BulkEditors {

    @Override
    public <E> EditorBuilder<E> builder(MetaClass metaClass, Collection<E> entities, FrameOwner origin) {
        return new EditorBuilder<>(super.builder(metaClass, entities, origin));
    }

    @Deprecated
    public static class EditorBuilder<E> extends BulkEditorBuilder<E> {

        public EditorBuilder(BulkEditorBuilder<E> builder) {
            super(builder);
        }

        public EditorBuilder(MetaClass metaClass, Collection<E> entities, FrameOwner origin,
                             Function<BulkEditorBuilder<E>, BulkEditorWindow<E>> handler) {
            super(metaClass, entities, origin, handler);
        }

        /**
         * Sets screen launch mode.
         *
         * @param launchMode the launch mode to set
         * @return this builder
         * @deprecated Use {@link #withOpenMode(io.jmix.ui.screen.OpenMode)} instead
         */
        @Deprecated
        public EditorBuilder<E> withLaunchMode(Screens.LaunchMode launchMode) {
            Preconditions.checkArgument(launchMode instanceof OpenMode,
                    "Unsupported LaunchMode " + launchMode);

            withOpenMode(((OpenMode) launchMode).getOpenMode());
            return this;
        }

        @Override
        public EditorBuilder<E> withOpenMode(io.jmix.ui.screen.OpenMode openMode) {
            super.withOpenMode(openMode);
            return this;
        }

        @Override
        public EditorBuilder<E> withListComponent(ListComponent<E> listComponent) {
            super.withListComponent(listComponent);
            return this;
        }

        @Override
        public EditorBuilder<E> withExclude(String exclude) {
            super.withExclude(exclude);
            return this;
        }

        @Override
        public EditorBuilder<E> withIncludeProperties(List<String> includeProperties) {
            super.withIncludeProperties(includeProperties);
            return this;
        }

        @Override
        public EditorBuilder<E> withFieldValidators(Map<String, Validator<?>> fieldValidators) {
            super.withFieldValidators(fieldValidators);
            return this;
        }

        @Override
        public EditorBuilder<E> withModelValidators(List<Validator<?>> modelValidators) {
            super.withModelValidators(modelValidators);
            return this;
        }

        @Override
        public EditorBuilder<E> withUseConfirmDialog(Boolean useConfirmDialog) {
            super.withUseConfirmDialog(useConfirmDialog);
            return this;
        }

        @Override
        public EditorBuilder<E> withFieldSorter(FieldSorter fieldSorter) {
            super.withFieldSorter(fieldSorter);
            return this;
        }

        @Override
        public EditorBuilder<E> withColumnsMode(ColumnsMode columnsMode) {
            super.withColumnsMode(columnsMode);
            return this;
        }

        /**
         * Returns launch mode set by {@link #withLaunchMode(Screens.LaunchMode)}.
         *
         * @deprecated Use {@link #getOpenMode()} instead
         */
        @Deprecated
        public Screens.LaunchMode getLaunchMode() {
            return OpenMode.from(getOpenMode());
        }
    }
}
