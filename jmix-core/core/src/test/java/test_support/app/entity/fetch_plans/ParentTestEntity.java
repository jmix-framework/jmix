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

package test_support.app.entity.fetch_plans;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;


@Entity(name = "app_ParentTestEntity")
@JmixEntity
public class ParentTestEntity {
    @Id
    @Column(name = "UUID")
    @JmixGeneratedValue
    private UUID uuid;

    @Column(name = "NAME")
    protected String name;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FIRSTBORN_ID")
    protected ChildTestEntity firstborn;

    @OneToMany(mappedBy = "parent")
    protected List<ChildTestEntity> youngerChildren;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ChildTestEntity getFirstborn() {
        return firstborn;
    }

    public void setFirstborn(ChildTestEntity firstborn) {
        this.firstborn = firstborn;
    }

    public List<ChildTestEntity> getYoungerChildren() {
        return youngerChildren;
    }

    public void setYoungerChildren(List<ChildTestEntity> youngerChildren) {
        this.youngerChildren = youngerChildren;
    }
}
