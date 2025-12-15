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

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

import java.util.Date;
import java.util.Set;

@JmixEntity
@Table(name = "TEST_CONDITIONS_MODULE_A")
@Entity(name = "test_ModuleA")
public class ModuleA extends BaseModuleDefinition {

    @ManyToMany
    @JoinTable(name = "TEST_COMP_A_B_LINK",
            joinColumns = @JoinColumn(name = "A_ID",referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "B_ID",referencedColumnName = "ID"))
    private Set<ModuleB> compatibleBs;

    @ManyToMany
    @JoinTable(name = "TEST_COMP_A_C_LINK",
            joinColumns = @JoinColumn(name = "A_ID",referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "B_ID",referencedColumnName = "ID"))
    private Set<ModuleC> compatibleCs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEFAULT_B_ID")
    private ModuleB defaultB;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEFAULT_C_ID")
    private ModuleC defaultC;


    public Set<ModuleB> getCompatibleBs() {
        return compatibleBs;
    }

    public void setCompatibleBs(Set<ModuleB> compatibleBs) {
        this.compatibleBs = compatibleBs;
    }

    public Set<ModuleC> getCompatibleCs() {
        return compatibleCs;
    }

    public void setCompatibleCs(Set<ModuleC> compatibleCs) {
        this.compatibleCs = compatibleCs;
    }

    public ModuleB getDefaultB() {
        return defaultB;
    }

    public void setDefaultB(ModuleB defaultB) {
        this.defaultB = defaultB;
    }

    public ModuleC getDefaultC() {
        return defaultC;
    }

    public void setDefaultC(ModuleC defaultC) {
        this.defaultC = defaultC;
    }
}
