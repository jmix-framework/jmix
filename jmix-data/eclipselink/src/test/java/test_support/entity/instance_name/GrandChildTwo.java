/*
 * Copyright 2026 Haulmont.
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

package test_support.entity.instance_name;

import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@JmixEntity
@Entity
public class GrandChildTwo extends ChildTwo {
    @Column(name = "GRAND_CHILD_CODE")
    private String grandChildCode;

    @Column(name = "NUMBER_")
    private Long number;

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public String getGrandChildCode() {
        return grandChildCode;
    }

    public void setGrandChildCode(String grandChildCode) {
        this.grandChildCode = grandChildCode;
    }

    @InstanceName
    @DependsOnProperties({"grandChildCode", "number"})
    private String getInstanceName() {
        return "[GrandChildTwo-" + this.grandChildCode + "-" + this.number + "]";
    }

}