/*
 * Copyright 2023 Haulmont.
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

package test_support.entity.lazyloading.self_ref_nested_loading;


import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

@JmixEntity
@Table(name = "TEST_LL_O2M_SELF_REF_ENTITY")
@Entity(name = "test_ll_O2MSelfRefEntity")
public class O2MSelfRefEntity {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MANAGER_ID")
    private O2MSelfRefEntity manager;


    @OneToMany(mappedBy = "manager", fetch = FetchType.LAZY)
    private List<O2MSelfRefEntity> report;


    @InstanceName
    @Column(name = "NAME")
    private String name;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public O2MSelfRefEntity getManager() {
        return manager;
    }

    public void setManager(O2MSelfRefEntity manager) {
        this.manager = manager;
    }

    public List<O2MSelfRefEntity> getReport() {
        return report;
    }

    public void setReport(List<O2MSelfRefEntity> report) {
        this.report = report;
    }
}
