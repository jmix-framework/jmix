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

package test_support.entity.entity_extension;

import io.jmix.core.metamodel.annotation.JmixEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.List;

@JmixEntity
@Table(name = "EXTTEST_BUS")
@Entity(name = "exttest_Bus")
public class Bus extends Vehicle {

    @OneToMany(mappedBy = "bus")
    @OrderBy("createdDate")
    protected List<Waybill> waybills;

    public List<Waybill> getRepairs() {
        return waybills;
    }

    public void setRepairs(List<Waybill> waybills) {
        this.waybills = waybills;
    }
}
