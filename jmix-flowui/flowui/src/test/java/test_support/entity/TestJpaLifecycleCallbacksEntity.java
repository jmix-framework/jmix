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

package test_support.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;

@Entity(name = "test_JpaLifecycleCallbacksEntity")
@JmixEntity
@Table(name = "TEST_JPA_LIFECYCLE_CALLBACKS_ENTITY")
public class TestJpaLifecycleCallbacksEntity extends TestBaseEntity {

    private static final long serialVersionUID = -4655299292272704686L;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "PRE_PERSIST_COUNTER")
    private Integer prePersistCounter;

    @Column(name = "PRE_UPDATE_COUNTER")
    private Integer preUpdateCounter;

    @PrePersist
    private void onPrePersist() {
        if (prePersistCounter == null) {
            prePersistCounter = 0;
        }
//        setPrePersistCounter(getPrePersistCounter() + 1);
        prePersistCounter++;
    }

    @PreUpdate
    private void onPreUpdate() {
        if (preUpdateCounter == null) {
            preUpdateCounter = 0;
        }
        preUpdateCounter++;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPrePersistCounter() {
        return prePersistCounter;
    }

    public void setPrePersistCounter(Integer prePersistCounter) {
        this.prePersistCounter = prePersistCounter;
    }

    public Integer getPreUpdateCounter() {
        return preUpdateCounter;
    }

    public void setPreUpdateCounter(Integer preUpdateCounter) {
        this.preUpdateCounter = preUpdateCounter;
    }
}
