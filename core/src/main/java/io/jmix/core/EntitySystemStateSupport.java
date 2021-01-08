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

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.function.BiFunction;

@Component("core_EntitySystemStateSupport")
public class EntitySystemStateSupport {

    public void copySystemState(Entity src, Entity dst) {
        dst.__getEntityEntry().copy(src.__getEntityEntry());
    }

    public void mergeSystemState(Entity src, Entity dst) {
        dst.__getEntityEntry().copy(src.__getEntityEntry());
    }

    public void mergeLazyLoadingState(Entity src, Entity dst, BiFunction<Collection<Object>, Object, Object> collectionWrapper) {
    }
}
