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

package test_support.app.entity.instance_name_inheritance;


import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

@JmixEntity
@Table(name = "DEMO_RESOURCE_CLASS")
@Entity
public class ExtExtEntity extends ExtEntity {
    @JoinColumn(name = "PARENT_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    ExtEntity parent;

    public ExtEntity getParent() {
        return parent;
    }

    public void setParent(ExtExtEntity parent) {
        this.parent = parent;
    }
}
