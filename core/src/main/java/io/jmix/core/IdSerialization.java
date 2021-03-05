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

package io.jmix.core;

/**
 * Provides conversion of {@link Id} to and from the string representation.
 * <p>
 * Format of the serialized id is {@code <entityName>.<idJson>}
 * where {@code <entityName>} is the entity name and {@code <idJson>} is JSON representation of the entity's id.
 * <p>
 * For example:
 * <pre>
 *     app_UuidEntity."4e4c5ca2-9a6e-43aa-8e67-3572b674f7c0"
 *
 *     app_LongIdEntity.1234
 *
 *     app_CompositeKeyEntity.{"entityId":10,"tenant":"abc"}
 * </pre>
 */
public interface IdSerialization {

    /**
     * Converts {@code Id} to string.
     */
    String idToString(Id<?> entityId);

    /**
     * Restores {@code Id} from string.
     */
    <T> Id<T> stringToId(String ref);
}
