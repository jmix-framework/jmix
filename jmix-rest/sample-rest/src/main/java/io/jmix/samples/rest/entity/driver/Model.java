/*
 * Copyright 2019 Haulmont.
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

package io.jmix.samples.rest.entity.driver;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.samples.rest.entity.StandardEntity;

import javax.annotation.PostConstruct;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.Set;

@Entity(name = "ref$Model")
@JmixEntity
@Table(name = "REF_MODEL")
public class Model extends StandardEntity {

    private static final long serialVersionUID = -645455241169768567L;

    @Column(name = "NAME")
    private String name;

    @Column(name = "MANUFACTURER")
    private String manufacturer;

    @ManyToMany(mappedBy = "models")
    @OnDeleteInverse(DeletePolicy.DENY)
    protected Set<Plant> plants;

    @Column(name = "NUMBER_OF_SEATS")
    private Integer numberOfSeats;

    @PostConstruct
    public void init() {
        setNumberOfSeats(4);
    }

    @PostConstruct
    public void initName() {
        setName("Default Model name");
    }

    @InstanceName
    public String getCaption() {
        return String.format("%s: (%s)", getManufacturer(), getName());
    }

    public Integer getNumberOfSeats() {
        return numberOfSeats;
    }

    public void setNumberOfSeats(Integer numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Set<Plant> getPlants() {
        return plants;
    }

    public void setPlants(Set<Plant> plants) {
        this.plants = plants;
    }
}

