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

package io.jmix.flowui.validation;

import io.jmix.flowui.component.validation.ValidationErrors;
import org.jspecify.annotations.NullMarked;

/**
 * Adds custom bean-group validation results to Flow UI view validation.
 */
@NullMarked
public interface BeanGroupValidationProvider {

    /**
     * Validates the item for the specified validation group.
     *
     * @param groupClass validation group class
     * @param item       item to validate
     * @return validation errors produced by the provider
     */
    ValidationErrors validateBeanGroup(Class<?> groupClass, Object item);
}
