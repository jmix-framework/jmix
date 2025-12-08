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

package io.jmix.flowui.view;

/**
 * An interface for views supporting entity locking functionality. This interface extends {@link HasEditedEntity}
 * to provide methods for accessing the currently edited entity and includes functionality related to entity locking.
 *
 * @param <E> the type of entity being edited
 */
public interface SupportEntityLock<E> extends HasEditedEntity<E> {

    /**
     * Returns the lock status of the currently edited entity instance.
     *
     * @return the lock status of the currently edited entity instance. Possible variants:
     * <ul>
     *     <li>{@link LockStatus#NOT_SUPPORTED} - if the entity does not support lock.</li>
     *     <li>{@link LockStatus#LOCKED} - if the entity instance is successfully locked.</li>
     *     <li>{@link LockStatus#FAILED} - if the entity instance has been locked when the view is
     *         opened.</li>
     * </ul>
     */
    LockStatus getLockStatus();
}
