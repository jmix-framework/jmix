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

package io.jmix.flowui.component.validation;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Marker interface to indicate that the implementing class can be used as a validator.
 *
 * @param <T> value type
 */
public interface Validator<T> extends Consumer<T> {

    /**
     * Checks a value.
     *
     * @param value a value
     */
    @Override
    void accept(@Nullable T value);
}
