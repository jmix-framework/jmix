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
 * Thrown to indicate that the {@link SpecificCondition} is not supported
 * for the passed {@link SpecificConditionHandler}.
 */
public class UnsupportedConditionException extends UnsupportedOperationException {

    public static final String EXCEPTION_MESSAGE = "The '%s' condition is not supported for '%s' handler";

    protected SpecificCondition specificCondition;
    protected SpecificConditionHandler handler;

    public UnsupportedConditionException(SpecificCondition specificCondition, SpecificConditionHandler handler) {
        super(EXCEPTION_MESSAGE.formatted(specificCondition, handler.getClass().getSimpleName()));

        this.specificCondition = specificCondition;
        this.handler = handler;
    }

    /**
     * @return {@link SpecificCondition} that was tried to be checked
     */
    public SpecificCondition getSpecificCondition() {
        return specificCondition;
    }

    /**
     * @return {@link SpecificConditionHandler} which checks the condition
     */
    public SpecificConditionHandler getHandler() {
        return handler;
    }
}
