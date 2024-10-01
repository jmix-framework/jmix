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

@Table(name = "TEST_CITY")
@Entity(name = "test_City")
@JmixEntity
public class City extends TestBaseEntity {

    @Column(name = "NAME", nullable = false)
    @NotNull
    private String name;

    @JoinTable(name = "TEST_CITY_ZOO_LINK",
            joinColumns = @JoinColumn(name = "CITY_ID"),
            inverseJoinColumns = @JoinColumn(name = "ANIMAL_ID"))
    @ManyToMany
    private List<Zoo> zoos;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Zoo> getZoos() {
        return zoos;
    }

    public void setZoos(List<Zoo> zoos) {
        this.zoos = zoos;
    }
}
