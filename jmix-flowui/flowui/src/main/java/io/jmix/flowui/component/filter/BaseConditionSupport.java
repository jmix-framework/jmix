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

package io.jmix.flowui.component.filter;

import io.jmix.core.annotation.Internal;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import org.jspecify.annotations.Nullable;

import java.util.function.UnaryOperator;

/**
 * Shared composition of a composite filter's output on top of the data loader's base condition, used
 * by {@code GenericFilter} and {@code GroupFilter}. Keeps the base-condition capture heuristic and the
 * {@code base AND output} composition in a single place so the two components cannot drift apart.
 */
@Internal
public final class BaseConditionSupport {

    private BaseConditionSupport() {
    }

    /**
     * The recomputed state: the (possibly re-captured) base condition and the resulting loader condition.
     *
     * @param baseCondition    the base condition to keep (re-captured if the loader condition was replaced)
     * @param loaderCondition  the composed condition to set on the data loader
     */
    public record Result(@Nullable Condition baseCondition, LogicalCondition loaderCondition) {
    }

    /**
     * Recomputes the data loader condition as {@code baseCondition AND filterCondition}. The base
     * condition is re-captured only when the loader condition was replaced externally (a different
     * object than the filter's last output); the filter never adopts its own output as the base.
     *
     * @param currentLoaderCondition   the loader's current condition
     * @param baseCondition            the base condition captured so far (may be {@code null})
     * @param baseConditionInitialized whether a base condition has already been captured
     * @param lastConditionSetByFilter the last condition this filter set on the loader
     * @param filterCondition          the filter's own output condition
     * @param copy                     the component's condition-copy function
     * @return the base condition to keep and the composed loader condition
     */
    public static Result recompose(@Nullable Condition currentLoaderCondition,
                                   @Nullable Condition baseCondition,
                                   boolean baseConditionInitialized,
                                   @Nullable Condition lastConditionSetByFilter,
                                   LogicalCondition filterCondition,
                                   UnaryOperator<Condition> copy) {
        Condition base = baseCondition;
        if (!baseConditionInitialized
                || (lastConditionSetByFilter != null && currentLoaderCondition != lastConditionSetByFilter)) {
            base = currentLoaderCondition != null ? copy.apply(currentLoaderCondition) : null;
        }

        LogicalCondition loaderCondition;
        if (base instanceof LogicalCondition logicalBase) {
            loaderCondition = (LogicalCondition) copy.apply(logicalBase);
            loaderCondition.add(filterCondition);
        } else if (base != null) {
            loaderCondition = LogicalCondition.and()
                    .add(base)
                    .add(filterCondition);
        } else {
            loaderCondition = filterCondition;
        }

        return new Result(base, loaderCondition);
    }
}
