/*
 * Copyright 2019 Haulmont.
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

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
@JmixEntity
public class TestEntityKey {
    @Column(name = "TENANT")
    private Integer tenant;

    @Column(name = "ENTITY_ID")
    private Long entityId;

    public Integer getTenant() {
        return tenant;
    }

    public void setTenant(Integer tenant) {
        this.tenant = tenant;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestEntityKey)) return false;
        TestEntityKey entityKey = (TestEntityKey) o;
        return Objects.equals(tenant, entityKey.tenant) &&
                Objects.equals(entityId, entityKey.entityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenant, entityId);
    }
}
