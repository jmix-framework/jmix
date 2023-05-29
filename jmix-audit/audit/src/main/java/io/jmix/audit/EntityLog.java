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

package io.jmix.audit;

import io.jmix.core.event.AttributeChanges;

import org.springframework.lang.Nullable;

/**
 * Allows to log entity lifecycle events: create, modify, delete.
 * <br>
 * Configured by {@link io.jmix.audit.entity.LoggedEntity} and
 * {@link io.jmix.audit.entity.LoggedAttribute} entities.
 */
public interface EntityLog {

    boolean isEnabled();

    void setEnabled(boolean enabled);

    /**
     * Logs creation of an entity which is configured for manual logging (LoggedEntity.auto == false).
     */
    void registerCreate(Object entity);

    /**
     * Logs creation of an entity which is configured for auto or manual logging
     * (depending on the {@code auto} parameter).
     */
    void registerCreate(Object entity, boolean auto);

    /**
     * Logs modification of an entity which is configured for manual logging (LoggedEntity.auto == false).
     */
    void registerModify(Object entity);

    /**
     * Logs modification of an entity which is configured for auto or manual logging
     * (depending on the {@code auto} parameter).
     */
    void registerModify(Object entity, boolean auto);


    /**
     * Logs modification of an entity which is configured for auto or manual logging
     * (depending on the {@code auto} parameter).
     *
     * @param changes attribute changes provided by caller
     */
    void registerModify(Object entity, boolean auto, @Nullable AttributeChanges changes);

    /**
     * Logs deletion of an entity which is configured for manual logging (LoggedEntity.auto == false).
     */
    void registerDelete(Object entity);

    /**
     * Logs deletion of an entity which is configured for auto or manual logging
     * (depending on the {@code auto} parameter).
     */
    void registerDelete(Object entity, boolean auto);

    /**
     * Invalidates configuration cache.
     * The configuration will be recreated from the database on next lifecycle event.
     */
    void invalidateCache();

    /**
     * Disables/enables entity logging for current thread.
     * Enabled by default.
     *
     * @param enabled entity logging disabled if false, enabled otherwise.
     */
    void processLoggingForCurrentThread(boolean enabled);

    /**
     * @return whether logging for the current thread is enabled
     * @see #processLoggingForCurrentThread(boolean)
     */
    boolean isLoggingForCurrentThread();

    /**
     * Flush records accumulated by invocations of {@link #registerCreate(Object)} and other registration methods
     * to the database.
     */
    void flush(String storeName);
}
