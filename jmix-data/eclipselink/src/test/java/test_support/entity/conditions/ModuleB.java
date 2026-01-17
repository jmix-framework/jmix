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

package test_support.entity.conditions;


import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

import java.util.Set;

@JmixEntity
@Table(name = "TEST_CONDITIONS_MODULE_B")
@Entity(name = "test_ModuleB")
public class ModuleB extends BaseModuleDefinition {

    @Column(name = "WEIGHT")
    private Double weight;

    @Column(name = "MAX_COUNT")
    private Integer maxCount;

    @ManyToMany
    @JoinTable(name = "TEST_REC_A_C_LINK",
            joinColumns = @JoinColumn(name = "B_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "C_ID", referencedColumnName = "ID"))
    private Set<ModuleC> recommendedCs;

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Integer getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(Integer maxCount) {
        this.maxCount = maxCount;
    }

    public Set<ModuleC> getRecommendedCs() {
        return recommendedCs;
    }

    public void setRecommendedCs(Set<ModuleC> recommendedCs) {
        this.recommendedCs = recommendedCs;
    }
}
