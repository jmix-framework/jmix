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

package com.haulmont.cuba.core.entity;

import io.jmix.core.entity.EntityEntrySoftDelete;

import java.util.UUID;

/**
 * Interface to be implemented by entities that have a persistent attribute of {@link UUID} type.
 *
 * @deprecated {@link EntityEntrySoftDelete} will be automatically added on enhancing step instead
 */
@Deprecated
public interface HasUuid {

    UUID getUuid();

    void setUuid(UUID uuid);
}
