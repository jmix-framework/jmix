/*
 * Copyright 2022 Haulmont.
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

import io.jmix.core.pessimisticlocking.PessimisticLock;
import io.jmix.flowui.util.OperationResult;

/**
 * Interface of views that display an entity instance and can save changes made by the user.
 *
 * @param <E> type of entity
 */
public interface DetailView<E> extends ChangeTracker {

    /**
     * Saves changes.
     */
    OperationResult save();

    /**
     * Saves changes and closes the view.
     */
    OperationResult closeWithSave();

    /**
     * Discards changes and closes the view.
     */
    OperationResult closeWithDiscard();

    /**
     * @return currently edited entity instance
     * @throws IllegalStateException if the edited entity isn't initialized yet, for example in {@link View.InitEvent}
     */
    E getEditedEntity();

    /**
     * Sets entity instance to the view.
     *
     * @param entity entity to edit
     */
    void setEntityToEdit(E entity);

    /**
     * @return lock status of the currently edited entity instance. Possible variants:
     * <ul>
     *     <li>{@link PessimisticLockStatus#NOT_SUPPORTED} - if the entity does not support pessimistic lock.</li>
     *     <li>{@link PessimisticLockStatus#LOCKED} - if the entity instance is successfully locked.</li>
     *     <li>{@link PessimisticLockStatus#FAILED} - if the entity instance has been locked when the view is
     *         opened.</li>
     * </ul>
     * @see PessimisticLock
     */
    PessimisticLockStatus getPessimisticLockStatus();
}
