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

package test_support.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Table(name = "TEST_ZOO")
@Entity(name = "test_Zoo")
@JmixEntity
public class Zoo extends TestBaseEntity {

    private static final long serialVersionUID = 5682981871475199801L;

    @Column(name = "NAME", nullable = false)
    @NotNull
    private String name;

    @Column(name = "ADDRESS", nullable = false)
    @NotNull
    private String address;

    @JoinTable(name = "TEST_ZOO_ANIMAL_LINK",
            joinColumns = @JoinColumn(name = "ZOO_ID"),
            inverseJoinColumns = @JoinColumn(name = "ANIMAL_ID"))
    @ManyToMany
    private List<Animal> animals;

    @JoinColumn(name = "CITY_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private City city;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<Animal> getAnimals() {
        return animals;
    }

    public void setAnimals(List<Animal> animals) {
        this.animals = animals;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }
}
