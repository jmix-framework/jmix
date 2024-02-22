/*
 * Copyright 2019 Haulmont.
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

package io.jmix.core.querycondition;

import io.jmix.core.CoreProperties;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

/**
 * The tree of {@code Condition}s represents an optional part of a query that is added if the corresponding parameters
 * are present.
 */
public interface Condition extends Serializable {

    /**
     * Returns parameters specified in the condition.
     */
    Collection<String> getParameters();

    /**
     * Checks whether the argument contains all parameters specified in the condition.
     *
     * @param defaultSkipNullOrEmpty default value for {@link SkippableCondition#skipNullOrEmpty},
     *                               should be obtained using {@link CoreProperties#isSkipNullOrEmptyConditionsByDefault}
     *
     * @return the condition if {@link SkippableCondition#skipNullOrEmpty} is {@code false} or if {@code actualParameters}
     * contains all parameters specified in the condition and these parameters are not null or empty.
     * Otherwise returns {@code null}.
     */
    @Nullable
    Condition actualize(Set<String> actualParameters, boolean defaultSkipNullOrEmpty);

    /**
     * Returns a deep copy of this condition.
     */
    Condition copy();

    Set<String> getExcludedParameters(Set<String> actualParameters);
}
