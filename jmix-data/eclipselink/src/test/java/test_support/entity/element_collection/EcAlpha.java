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

import java.util.List;
import java.util.UUID;

@Table(name = "TEST_EC_ALPHA")
@Entity(name = "test_EcAlpha")
@JmixEntity
public class EcAlpha {

    @Id
    @JmixGeneratedValue
    @Column(name = "UUID")
    private UUID id;

    @Column(name = "NAME")
    @InstanceName
    private String name;

    @ElementCollection
    @CollectionTable(name = "TEST_EC_ALPHA_TAGS", joinColumns = @JoinColumn(name = "ALPHA_ID"))
    private List<String> tags;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BETA_ID")
    private EcBeta beta;

    @OneToMany(mappedBy = "alpha")
    private List<EcGamma> gammas;

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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public EcBeta getBeta() {
        return beta;
    }

    public void setBeta(EcBeta bar) {
        this.beta = bar;
    }

    public List<EcGamma> getGammas() {
        return gammas;
    }

    public void setGammas(List<EcGamma> gammas) {
        this.gammas = gammas;
    }
}