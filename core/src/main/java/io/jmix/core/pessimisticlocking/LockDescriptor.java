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

import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import javax.persistence.Id;

/**
 * The {@code LockDescriptor} contains configuration information about a pessimistic lock.
 */
@JmixEntity(name = "sys_LockDescriptor")
@SystemLevel
public class LockDescriptor {

    @Id
    @JmixProperty(mandatory = true)
    private String name;

    @JmixProperty(mandatory = true)
    private Integer timeoutSec;

    LockDescriptor() {
        this.name = null;
        this.timeoutSec = null;
    }

    public LockDescriptor(String name, Integer timeoutSec) {
        this.name = name;
        this.timeoutSec = timeoutSec;
    }

    public String getId() {
        return name;
    }

    public void setId(String id) {
        this.name = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTimeoutSec() {
        return timeoutSec;
    }

    public void setTimeoutSec(Integer timeoutSec) {
        this.timeoutSec = timeoutSec;
    }
}
