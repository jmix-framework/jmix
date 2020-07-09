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

package io.jmix.core.pessimisticlocking;

import io.jmix.core.AppBeans;
import io.jmix.core.JmixEntity;
import io.jmix.core.TimeSource;
import io.jmix.core.UuidProvider;
import io.jmix.core.entity.HasUuid;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.ModelObject;
import io.jmix.core.metamodel.annotation.ModelProperty;

import javax.annotation.Nullable;
import javax.persistence.Id;
import java.util.Date;
import java.util.UUID;

/**
 * Describes a pessimistic lock of an object.
 */
@ModelObject(name = "sys_LockInfo")
@SystemLevel
public class LockInfo implements JmixEntity, HasUuid {

    private static final long serialVersionUID = -1991047219638006414L;

    private final String objectType;
    private final String objectId;
    private final Date since;
    private final String userKey;
    private final String userName;


    @Id
    @ModelProperty
    protected UUID id;

    LockInfo() {
        this.id = UuidProvider.createUuid();
        this.objectType = null;
        this.objectId = null;
        TimeSource timeSource = AppBeans.get(TimeSource.NAME);
        this.since = timeSource.currentTimestamp();
        this.userKey = null;
        this.userName = null;
    }

    public LockInfo(@Nullable String userKey, @Nullable String userName, String objectType, String objectId) {
        this.id = UuidProvider.createUuid();
        this.objectType = objectType;
        this.objectId = objectId;
        TimeSource timeSource = AppBeans.get(TimeSource.NAME);
        this.since = timeSource.currentTimestamp();
        this.userKey = userKey;
        this.userName = userName;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public UUID getUuid() {
        return id;
    }

    public void setUuid(UUID uuid) {
        this.id = uuid;
    }

    /**
     * @return locked object id
     */
    @ModelProperty
    public String getObjectId() {
        return objectId;
    }

    /**
     * @return locked object type
     */
    @ModelProperty
    public String getObjectType() {
        return objectType;
    }

    /**
     * @return when the lock occurred
     */
    @ModelProperty
    public Date getSince() {
        return since;
    }

    /**
     * @return unique representation of the user which holds the lock
     */
    @ModelProperty
    public String getUserKey() {
        return userKey;
    }

    /**
     * @return username of the user which holds the lock
     */
    @ModelProperty
    public String getUserName() {
        return userName;
    }

    @Override
    public String toString() {
        return objectType + "/" + objectId + ", userKey=" + userKey
                + ", userName=" + userName + ", since=" + since;
    }
}
