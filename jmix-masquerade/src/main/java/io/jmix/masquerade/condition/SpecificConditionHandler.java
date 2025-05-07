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

import com.codeborne.selenide.CheckResult;

/**
 * Interface for checking {@link SpecificCondition Specific Conditions}.
 * <p>
 * Handling of {@link SpecificCondition Specific Conditions} depends on the component type and state.
 */
public interface SpecificConditionHandler {

    /**
     * Checks whether the current handler matches the passed {@link SpecificCondition}.
     *
     * @param condition condition to check
     * @return {@link CheckResult} that contains {@link  CheckResult.Verdict#ACCEPT} if the current handler matches the
     * passed {@link SpecificCondition}, {@link  CheckResult.Verdict#REJECT} otherwise
     */
    CheckResult check(SpecificCondition condition);
}
