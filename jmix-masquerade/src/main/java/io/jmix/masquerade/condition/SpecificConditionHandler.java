/*
 * Copyright 2025 Haulmont.
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

package io.jmix.masquerade.condition;

/**
 * Interface for checking {@link SpecificCondition Specific Conditions}.
 * <p>
 * Handling of {@link SpecificCondition Specific Conditions} depends on the component type and state.
 */
public interface SpecificConditionHandler {

    /**
     * Resolves the passed {@link SpecificCondition} into a {@link SpecificCheck} that describes how it must be
     * checked for the current handler. The actual verdict is computed centrally by {@link SpecificCondition#check}.
     *
     * @param condition condition to resolve
     * @return a {@link SpecificCheck} describing how to check the passed condition
     */
    SpecificCheck resolve(SpecificCondition condition);
}
