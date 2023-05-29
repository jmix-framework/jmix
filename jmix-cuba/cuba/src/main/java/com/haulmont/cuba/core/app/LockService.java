/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */
package com.haulmont.cuba.core.app;


import io.jmix.core.Entity;
import io.jmix.core.pessimisticlocking.LockInfo;
import io.jmix.core.pessimisticlocking.LockNotSupported;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Service interface for pessimistic locking.
 *
 * @deprecated use only in legacy CUBA code. In new code, use {@link io.jmix.core.pessimisticlocking.LockManager}.
 */
@Deprecated
public interface LockService {

    String NAME = "cuba_LockService";

    /**
     * Try to lock an arbitrary object.
     *
     * @param name locking object name
     * @param id   locking object ID
     * @return - null in case of successful lock,<br>
     * - {@link LockNotSupported} instance in case of locking is not configured for this object,<br>
     * - {@link LockInfo} instance in case of this object is already locked by someone
     */
    @Nullable
    LockInfo lock(String name, String id);

    /**
     * Try to lock an entity.
     *
     * @param entity entity instance
     * @return - null in case of successful lock,<br>
     * - {@link LockNotSupported} instance in case of locking is not configured for this entity,<br>
     * - {@link LockInfo} instance in case of this entity is already locked by someone
     */
    @Nullable
    LockInfo lock(Entity entity);

    /**
     * Unlock an arbitrary object.
     *
     * @param name locking object name
     * @param id   locking object ID
     */
    void unlock(String name, String id);

    /**
     * Unlock an entity.
     *
     * @param entity entity instance
     */
    void unlock(Entity entity);

    /**
     * Get locking status for particular object
     *
     * @param name locking object name
     * @param id   locking object ID
     * @return - null in case of no lock,<br>
     * - {@link LockNotSupported} instance in case of locking is not configured for this object,<br>
     * - {@link LockInfo} instance in case of this object is locked by someone
     */
    @Nullable
    LockInfo getLockInfo(String name, String id);

    /**
     * List of current locks
     */
    List<LockInfo> getCurrentLocks();

    void reloadConfiguration();
}