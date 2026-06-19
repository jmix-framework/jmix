/*
 * Copyright 2026 Haulmont.
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

package io.jmix.reports.yarg.structure;

import org.jspecify.annotations.NullMarked;

/**
 * Custom implementation of the result field formatting.
 *
 * @param <T> field type
 * @see ReportFieldFormat
 */
@FunctionalInterface
@NullMarked
public interface CustomValueFormatter<T> {

    /**
     * Convert the given value into desired string representation.
     *
     * @param value value of the result field
     * @return formatted value
     */
    String format(T value);
}
