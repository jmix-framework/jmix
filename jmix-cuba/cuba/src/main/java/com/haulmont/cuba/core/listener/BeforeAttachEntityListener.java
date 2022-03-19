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
package com.haulmont.cuba.core.listener;

import io.jmix.core.Entity;

/**
 * Defines the contract for handling entities right before they are attached to an EntityManager on merge operation.
 *
 * @deprecated use only in legacy CUBA code. In new code, use {@link io.jmix.data.listener.BeforeAttachEntityListener}.
 */
@Deprecated
public interface BeforeAttachEntityListener<T extends Entity> extends io.jmix.data.listener.BeforeAttachEntityListener<T> {

    /**
     * Executes before the object is attached to an EntityManager on merge operation.
     *
     * @param entity        detached entity
     */
    void onBeforeAttach(T entity);
}
