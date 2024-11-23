/*
 * Copyright 2024 Haulmont.
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
package test_support.entity.importexport;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import jakarta.persistence.*;

import java.util.List;

@JmixEntity
@Entity(name = "testimportexport_Car")
@Table(name = "TESTIMPORTEXPORT_CAR")
public class Car extends StandardEntity {

    @Column(name = "VIN")
    private String vin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MODEL_ID")
    private Model model;

    @OneToMany(mappedBy = "car")
    @OrderBy("createTs")
    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    private List<Repair> repairs;

    @JmixProperty
    @Transient
    protected Integer repairCost;

    @Transient
    protected Integer repairPrice;

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public List<Repair> getRepairs() {
        return repairs;
    }

    public void setRepairs(List<Repair> repairs) {
        this.repairs = repairs;
    }

    public Integer getRepairCost() {
        return repairCost;
    }

    public void setRepairCost(Integer repairCost) {
        this.repairCost = repairCost;
    }

    public Integer getRepairPrice() {
        return repairPrice;
    }

    public void setRepairPrice(Integer repairPrice) {
        this.repairPrice = repairPrice;
    }
}