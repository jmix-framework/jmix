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

package io.jmix.pessimisticlock.entity;

import io.jmix.core.UuidProvider;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import org.springframework.lang.Nullable;
import jakarta.persistence.Id;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Describes a pessimistic lock of an object.
 */
@JmixEntity(name = "sys_LockInfo")
@SystemLevel
public class LockInfo implements Serializable {

    private static final long serialVersionUID = -1991047219638006414L;

    private final String objectType;
    private final String objectId;
    private final Date since;
    private final String username;

    @Id
    @JmixProperty
    @JmixGeneratedValue
    protected UUID id;

    LockInfo() {
        this.objectType = null;
        this.objectId = null;
        this.since = null;
        this.username = null;
    }

    public LockInfo(@Nullable String username, String objectType, String objectId,
                    Date since) {
        this.id = UuidProvider.createUuid();
        this.objectType = objectType;
        this.objectId = objectId;
        this.since = since;
        this.username = username;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * @return locked object id
     */
    public String getObjectId() {
        return objectId;
    }

    /**
     * @return locked object type
     */
    public String getObjectType() {
        return objectType;
    }

    /**
     * @return when the lock occurred
     */
    public Date getSince() {
        return since;
    }

    /**
     * @return username of the user which holds the lock
     */
    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return objectType + "/" + objectId + ", userName=" + username + ", since=" + since;
    }
}
