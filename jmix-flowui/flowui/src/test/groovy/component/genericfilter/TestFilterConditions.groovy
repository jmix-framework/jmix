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

package component.genericfilter

import io.jmix.core.querycondition.Condition
import io.jmix.core.querycondition.LogicalCondition
import io.jmix.core.querycondition.PropertyCondition

/**
 * Shared assertions over a query {@link Condition} tree for generic-filter tests.
 */
class TestFilterConditions {

    static boolean hasPropertyConditionOn(Condition condition, String property) {
        if (condition instanceof PropertyCondition) {
            return property == condition.property
        }
        if (condition instanceof LogicalCondition) {
            return condition.conditions.any { hasPropertyConditionOn(it, property) }
        }
        return false
    }

    static int countPropertyConditions(Condition condition) {
        if (condition instanceof PropertyCondition) {
            return 1
        }
        if (condition instanceof LogicalCondition) {
            return condition.conditions.inject(0) { acc, c -> acc + countPropertyConditions(c) }
        }
        return 0
    }
}
