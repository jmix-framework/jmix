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

package io.jmix.core;

import io.jmix.core.EntityStates.PropertyLoadedState;
import io.jmix.core.annotation.Internal;

@Internal
public interface PersistentAttributesLoadChecker {

    /**
     * Checks whether the property is loaded from the data store. This may trigger fetching of the property if its state
     * cannot be determined using standard JPA mechanisms.
     *
     * @see io.jmix.core.EntityStates#isLoaded(Object, String)
     */
    boolean isLoaded(Object entity, String property);

    /**
     * Checks whether the property is loaded from the data store without risking fetching it.
     *
     * @see io.jmix.core.EntityStates#isLoaded(Object, String)
     */
    PropertyLoadedState isLoadedSafe(Object entity, String property);
}