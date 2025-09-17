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
 * Logs lifecycle events (create, modify, delete) of JPA entities.
 * <p>
 * Configured by {@link io.jmix.audit.entity.LoggedEntity} and
 * {@link io.jmix.audit.entity.LoggedAttribute} entities.
 */
public interface EntityLog {

    /**
     * @return whether entity logging is enabled globally and for the current thread (see {@link #processLoggingForCurrentThread(boolean)})
     */
    boolean isEnabled();

    /**
     * Enables/disables entity logging globally.
     * By default, takes the value from the {@code jmix.audit.enabled} application property.
     */
    void setEnabled(boolean enabled);

    /**
     * Logs creation of an entity which is configured for manual logging (LoggedEntity.auto == false).
     * Should be called within an active transaction.
     *
     * @param entity entity instance
     */
    void registerCreate(Object entity);

    /**
     * Logs creation of an entity which is configured for auto or manual logging.
     * Should be called within an active transaction.
     *
     * @param entity entity instance
     * @param auto   should match the entity configuration for auto (true) or manual (false) logging
     */
    void registerCreate(Object entity, boolean auto);

    /**
     * Logs modification of an entity which is configured for manual logging (LoggedEntity.auto == false).
     * Should be called within an active transaction.
     *
     * @param entity entity instance in <b>managed state</b>
     */
    void registerModify(Object entity);

    /**
     * Logs modification of an entity which is configured for auto or manual logging.
     * Should be called within an active transaction.
     *
     * @param entity entity instance in <b>managed state</b>
     * @param auto   should match the entity configuration for auto (true) or manual (false) logging
     */
    void registerModify(Object entity, boolean auto);


    /**
     * Logs modification of an entity which is configured for auto or manual logging.
     * Should be called within an active transaction.
     *
     * @param entity entity instance in <b>managed state</b>
     * @param auto   should match the entity configuration for auto (true) or manual (false) logging
     * @param changes attribute changes provided by caller
     */
    void registerModify(Object entity, boolean auto, @Nullable AttributeChanges changes);

    /**
     * Logs deletion of an entity which is configured for manual logging (LoggedEntity.auto == false).
     * Should be called within an active transaction.
     */
    void registerDelete(Object entity);

    /**
     * Logs deletion of an entity which is configured for auto or manual logging
     * Should be called within an active transaction.
     *
     * @param entity entity instance
     * @param auto   should match the entity configuration for auto logging
     *               (true) or manual logging (false)
     */
    void registerDelete(Object entity, boolean auto);

    /**
     * Invalidates configuration cache.
     * The configuration will be recreated from the database on next lifecycle event.
     */
    void invalidateCache();

    /**
     * Disables/enables entity logging for the current thread.
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
