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
package com.haulmont.cuba.gui.components;

import java.util.function.Function;

/**
 * Interface defining method for formatting a value into string.
 * <br> Used by various UI components.
 *
 * @deprecated use {@link io.jmix.ui.component.formatter.Formatter}
 */
@Deprecated
public interface Formatter<T> extends Function<T, String> {
    @Override
    default String apply(T t) {
        return format(t);
    }

    String format(T t);
}