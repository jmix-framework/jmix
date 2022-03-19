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
package com.haulmont.cuba.core.model.common;

import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.core.global.AppBeans;
import io.jmix.core.TimeSource;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import java.util.Date;

/**
 * Describes a pessimistic lock of an object.
 */
@JmixEntity(name = "sys$LockInfo")
@SystemLevel
public class LockInfo extends BaseUuidEntity {

    private static final long serialVersionUID = -1991047219638006414L;

    private final String entityName;
    private final String entityId;
    private final Date since;
    private final User user;

    public LockInfo(User user, String entityName, String entityId) {
        this.entityName = entityName;
        this.entityId = entityId;
        TimeSource timeSource = AppBeans.get(TimeSource.class);
        this.since = timeSource.currentTimestamp();
        this.user = user;
    }

    /**
     * @return locked object id
     */
    @JmixProperty
    public String getEntityId() {
        return entityId;
    }

    /**
     * @return locked object type
     */
    @JmixProperty
    public String getEntityName() {
        return entityName;
    }

    /**
     * @return when the lock occurred
     */
    @JmixProperty
    public Date getSince() {
        return since;
    }

    /**
     * @return a user which holds the lock
     */
    @JmixProperty
    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return entityName + "/" + entityId + ", user=" + user + ", since=" + since;
    }
}
