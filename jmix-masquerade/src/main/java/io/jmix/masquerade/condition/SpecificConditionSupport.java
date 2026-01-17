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
 * Utility class for checking {@link SpecificCondition Specific Conditions}.
 */
public class SpecificConditionSupport {

    protected static final ThreadLocal<SpecificConditionHandler> holder = new ThreadLocal<>();

    /**
     * Accepts the passed condition callback for the passed {@link SpecificConditionHandler}.
     *
     * @param handler                handler
     * @param checkConditionCallback callback for checking condition
     */
    public static void acceptFor(SpecificConditionHandler handler, Runnable checkConditionCallback) {
        holder.set(handler);

        try {
            checkConditionCallback.run();
        } finally {
            holder.remove();
        }
    }

    /**
     * @return returns the current {@link SpecificConditionHandler} for which the condition checks
     */
    public static SpecificConditionHandler getHandler() {
        return holder.get();
    }
}
