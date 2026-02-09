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

package io.jmix.core;

import io.jmix.core.annotation.Experimental;

/**
 * Interface to be implemented by custom update services.
 * The {@link SaveDelegate#save} method is called by generic framework mechanisms instead of {@code DataManager}
 * when saving entities of type {@code E}.
 *
 * @param <E> entity type
 */
@Experimental
public interface SaveDelegate<E> {

    /**
     * Called by generic framework mechanisms instead of {@code DataManager} when saving entities of type {@code E}.
     *
     * @param entity entity to save
     * @param saveContext the whole save context
     * @return saved entity
     */
    E save(E entity, SaveContext saveContext);

    /**
     * Convenience method to save an entity using {@code DataManager}. Can be used by custom update services.
     *
     * @param dataManager data manager to use
     * @param entity entity to save
     * @param saveContext the whole save context
     * @return saved entity
     */
    static <E> E save(UnconstrainedDataManager dataManager, E entity, SaveContext saveContext) {
        SaveContext context = new SaveContext()
                .saving(entity, saveContext.getFetchPlans().get(entity))
                .setHints(saveContext.getHints());
        return dataManager.save(context).get(entity);
    }
}
