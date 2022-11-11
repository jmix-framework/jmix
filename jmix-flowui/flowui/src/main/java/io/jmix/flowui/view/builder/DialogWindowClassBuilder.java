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

package io.jmix.flowui.view.builder;


import io.jmix.flowui.view.View;

import java.util.Optional;

/**
 * An interface to be implemented by builders which opens a
 * specific view in a {@link io.jmix.flowui.view.DialogWindow}.
 *
 * @param <V> a view type which is opened in a dialog window
 */
public interface DialogWindowClassBuilder<V extends View<?>> extends DialogWindowBuilder<V> {

    /**
     * @return opened view class
     */
    Optional<Class<V>> getViewClass();
}
