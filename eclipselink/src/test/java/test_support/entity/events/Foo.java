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

package test_support.entity.events;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.Listeners;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import test_support.listeners.TestAllDataEventsListener;

import javax.persistence.*;
import java.util.UUID;

@JmixEntity
@Table(name = "TEST_EVENTS_FOO")
@Entity(name = "test_events_Foo")
@Listeners("test_TestAllDataEventsListener")
public class Foo {

    @JmixGeneratedValue
    @Id
    @Column(name = "ID", nullable = false)
    private UUID id;

    @Version
    @Column(name = "VERSION")
    private Integer version;

    @InstanceName
    @Column(name = "NAME")
    private String name;

    @Column(name = "AMOUNT")
    private Integer amount;

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @PrePersist
    public void prePersist() {
        TestAllDataEventsListener.allEvents.add(new TestAllDataEventsListener.EventInfo("JPA PrePersist", this));
    }

    @PostPersist
    public void postPersist() {
        TestAllDataEventsListener.allEvents.add(new TestAllDataEventsListener.EventInfo("JPA PostPersist", this));
    }

    @PreUpdate
    public void preUpdate() {
        TestAllDataEventsListener.allEvents.add(new TestAllDataEventsListener.EventInfo("JPA PreUpdate", this));
    }

    @PostUpdate
    public void postUpdate() {
        TestAllDataEventsListener.allEvents.add(new TestAllDataEventsListener.EventInfo("JPA PostUpdate", this));
    }

    @PreRemove
    public void preRemove() {
        TestAllDataEventsListener.allEvents.add(new TestAllDataEventsListener.EventInfo("JPA PreRemove", this));
    }

    @PostRemove
    public void postRemove() {
        TestAllDataEventsListener.allEvents.add(new TestAllDataEventsListener.EventInfo("JPA PostRemove", this));
    }

    @PostLoad
    public void postLoad() {
        TestAllDataEventsListener.allEvents.add(new TestAllDataEventsListener.EventInfo("JPA PostLoad", this));
    }
}