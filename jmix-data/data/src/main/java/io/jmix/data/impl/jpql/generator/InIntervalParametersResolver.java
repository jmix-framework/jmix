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

package io.jmix.data.impl.jpql.generator;

import io.jmix.core.common.datastruct.Pair;
import io.jmix.core.querycondition.PropertyCondition;

import java.util.List;

/**
 * Interface for resolving parameters for a {@link PropertyCondition}
 * with {@link PropertyCondition.Operation#IN_INTERVAL} operation.
 */
public interface InIntervalParametersResolver {

    /**
     * Resolves parameters from the provided {@link PropertyCondition}.
     *
     * @param condition the {@link PropertyCondition}
     * @return a list of {@link Pair} instances, where each pair contains a parameter name and its corresponding value
     */
    List<Pair<String, Object>> resolveParameters(PropertyCondition condition);
}
