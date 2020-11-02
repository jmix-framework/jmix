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

package com.haulmont.cuba.gui.components;

import io.jmix.core.common.event.Subscription;
import io.jmix.ui.component.Resource;

import java.util.function.Consumer;

/**
 * @deprecated Use {@link io.jmix.ui.component.ResourceView} instead
 */
@Deprecated
public interface ResourceView extends io.jmix.ui.component.ResourceView {

    /**
     * Creates resource implementation by its type.
     *
     * @param type resource class to be created
     * @param <R>  {@link Resource} inheritor
     * @return new resource instance with given type
     */
    @Deprecated
    <R extends Resource> R createResource(Class<R> type);

    /**
     * Removes a listener that will be notified when a source is changed.
     *
     * @param listener a listener to remove
     * @deprecated Use {@link Subscription} instead
     */
    @Deprecated
    void removeSourceChangeListener(Consumer<SourceChangeEvent> listener);
}
