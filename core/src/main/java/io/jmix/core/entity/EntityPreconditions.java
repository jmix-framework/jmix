/*
 * Copyright 2020 Haulmont.
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

package io.jmix.core.entity;

import io.jmix.core.Entity;

public class EntityPreconditions {

    public static void checkEntityType(Object entity) {
        if (!(entity instanceof Entity)) {
            throw new IllegalStateException(String.format("Unsupported entity type %s", entity.getClass()));
        }
    }

    public static void checkEntityType(Object entity, String arg) {
        if (!(entity instanceof Entity)) {
            throw new IllegalStateException(String.format("Argument %s has unsupported entity type %s", arg, entity.getClass()));
        }
    }
}
