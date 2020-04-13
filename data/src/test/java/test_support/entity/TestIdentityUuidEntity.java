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

import io.jmix.core.entity.HasUuid;
import io.jmix.data.entity.BaseIdentityIdEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

@Entity(name = "test_IdentityUuidEntity")
@Table(name = "TEST_IDENTITY_UUID_ENTITY")
public class TestIdentityUuidEntity extends BaseIdentityIdEntity implements HasUuid {

    private static final long serialVersionUID = 146993228195491648L;

    @Column(name = "NAME")
    private String name;

    @Column(name = "UUID")
    private UUID uuid;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
