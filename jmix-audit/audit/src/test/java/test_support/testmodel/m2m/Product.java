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

package test_support.testmodel.m2m;


import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

@Entity(name = "test_Product")
@JmixEntity
@Table(name = "TEST_PRODUCT")
public class Product {

    @Id
    @Column(name = "ID", nullable = false)
    @JmixGeneratedValue
    protected UUID id;

    @InstanceName
    @Column(name = "NAME")
    private String name;

    @JoinTable(name = "PRODUCT_TAG_LINK",
            joinColumns = @JoinColumn(name = "PRODUCT_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "TAG_ID", referencedColumnName = "ID"))
    @ManyToMany
    private List<Tag> tags;

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

