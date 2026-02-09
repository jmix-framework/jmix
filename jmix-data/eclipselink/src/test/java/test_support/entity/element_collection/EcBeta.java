/*
 * Copyright 2025 Haulmont.
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

package test_support.entity.element_collection;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Table(name = "TEST_EC_BETA")
@Entity(name = "test_EcBeta")
@JmixEntity
public class EcBeta {

    @Id
    @JmixGeneratedValue
    @Column(name = "UUID")
    private UUID id;

    @Column(name = "NAME")
    @InstanceName
    private String name;

    @OneToMany(mappedBy = "beta")
    private Set<EcAlpha> alphas = new HashSet<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID uuid) {
        this.id = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<EcAlpha> getAlphas() {
        return alphas;
    }

    public void setAlphas(Set<EcAlpha> alphas) {
        this.alphas = alphas;
    }
}