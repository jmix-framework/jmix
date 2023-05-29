/*
 * Copyright 2021 Haulmont.
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

package io.jmix.data.impl.jpql.generator;

import io.jmix.core.querycondition.Condition;

import jakarta.annotation.Nullable;

/**
 * Modifies parts of JPQL query
 */
public interface ConditionGenerator {

    /**
     * Checks whether the condition generator supports the given {@code context}.
     *
     * @param context condition generation context
     * @return true if the condition generator supports the given context, or false otherwise
     */
    boolean supports(ConditionGenerationContext context);

    /**
     * Returns a JPQL 'join' clause modified according to the given context.
     *
     * @param context condition generation context
     * @return a JPQL 'join' clause modified according to the given context
     */
    String generateJoin(ConditionGenerationContext context);

    /**
     * Returns a JPQL 'where' clause modified according to the given context.
     *
     * @param context condition generation context
     * @return a JPQL 'where' clause modified according to the given context
     */
    String generateWhere(ConditionGenerationContext context);

    /**
     * Returns a parameter value modified according to the given condition.
     *
     * @param condition      a condition
     * @param parameterValue parameter value
     * @param entityName     entity name
     * @return a modified parameter value
     */
    @Nullable
    Object generateParameterValue(@Nullable Condition condition, @Nullable Object parameterValue, @Nullable String entityName);
}
