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

import org.springframework.lang.Nullable;
import java.util.function.Consumer;

/**
 * A functional interface for validating user input or other potentially invalid data.
 *
 * @param <T> value type
 */
@FunctionalInterface
public interface Validator<T> extends Consumer<T> {

    /**
     * Checks a value.
     *
     * @param value a value
     */
    @Override
    void accept(@Nullable T value);
}
