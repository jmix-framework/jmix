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

package io.jmix.datatools.datamodel;

import io.jmix.core.common.util.Preconditions;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum describes the types of relationships needed to build an entity model and relationships between entities
 */
public enum RelationType {

    MANY_TO_ONE,
    ONE_TO_MANY,
    ONE_TO_ONE,
    MANY_TO_MANY;

    private final static Map<RelationType, RelationType> relationsTypesWithReverse = new HashMap<>() {
        {
            put(MANY_TO_ONE, ONE_TO_MANY);
            put(ONE_TO_MANY, MANY_TO_ONE);
            put(MANY_TO_MANY, MANY_TO_MANY);
            put(ONE_TO_ONE, ONE_TO_ONE);
        }
    };

    /**
     * Returns the reverse of the specified {@code RelationType}.
     *
     * @param relationType the {@code RelationType} whose reverse is to be retrieved; must not be null
     * @return the reverse {@code RelationType}, or {@code null} if no reverse mapping exists
     * @throws IllegalArgumentException if {@code relationType} is null
     */
    public static RelationType getReverseRelation(RelationType relationType) {
        Preconditions.checkNotNullArgument(relationType);
        return relationsTypesWithReverse.get(relationType);
    }
}
