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

package test_support.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.util.UUID;

@Table(name = "TEST_INSPECTOR_COMPOSITION_ITEM")
@Entity(name = "test_InspectorCompositionItem")
@JmixEntity
public class InspectorCompositionItem {

    @Id
    @Column(name = "ID", nullable = false)
    @JmixGeneratedValue
    protected UUID id;

    @Version
    @Column(name = "VERSION", nullable = false)
    protected Integer version;

    @InstanceName
    @Column(name = "NAME")
    protected String name;

    @Column(name = "AMOUNT")
    protected Integer amount;

    @JoinColumn(name = "MASTER_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    protected InspectorCompositionMaster master;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public InspectorCompositionMaster getMaster() {
        return master;
    }

    public void setMaster(InspectorCompositionMaster master) {
        this.master = master;
    }
}
