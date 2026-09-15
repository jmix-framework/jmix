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

package test_support.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.Store;
import test_support.CustomStoreDescriptorProvider;

import java.util.List;
import java.util.UUID;

@JmixEntity(name = "test_CustomStoreEntity")
@Store(name = CustomStoreDescriptorProvider.STORE_NAME)
public class CustomStoreEntity {

    @JmixGeneratedValue
    @JmixId
    private UUID id;

    private String name;

    private List<CustomStoreChild> children;

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

    public List<CustomStoreChild> getChildren() {
        return children;
    }

    public void setChildren(List<CustomStoreChild> children) {
        this.children = children;
    }
}
