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

package io.jmix.flowui.model;

import io.jmix.core.querycondition.Condition;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Supplies a condition to a {@link DataLoader} the contributor is registered on. The loader polls
 * every registered contributor when it builds a load context, so the query condition is composed
 * of the loader's own condition and the current contribution of each contributor at the moment of
 * loading. The loader takes a copy of the returned condition: a contribution never becomes a
 * shared mutable node of the loader's condition tree, and editing a previously returned condition
 * takes effect on the next load.
 *
 * <p>Contributors let several independent parties filter one loader without competing for the
 * single {@link DataLoader#setCondition(Condition)} slot: the slot stays with the application,
 * each contributor owns its contribution.
 *
 * @see DataLoader#addConditionContributor(ConditionContributor)
 */
@NullMarked
@FunctionalInterface
public interface ConditionContributor {

    /**
     * Returns the current contribution of this contributor, or {@code null} if it currently
     * contributes nothing.
     */
    @Nullable
    Condition getCondition();
}
