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

package test_support.entity.datastores;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.Store;

import java.util.UUID;

@Store(name = "mem2")
@JmixEntity(name = "test_Mem2DtoEntity")
public class Mem2DtoEntity {

    @JmixId
    @JmixGeneratedValue
    private UUID id;

    private String name;

    @SystemLevel
    private UUID mem1DtoEntityId;

    @DependsOnProperties({"mem1DtoEntityId"})
    private Mem1DtoEntity mem1DtoEntity;

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

    public UUID getMem1DtoEntityId() {
        return mem1DtoEntityId;
    }

    public void setMem1DtoEntityId(UUID mem1DtoEntityId) {
        this.mem1DtoEntityId = mem1DtoEntityId;
    }

    public Mem1DtoEntity getMem1DtoEntity() {
        return mem1DtoEntity;
    }

    public void setMem1DtoEntity(Mem1DtoEntity mem1DtoEntity) {
        this.mem1DtoEntity = mem1DtoEntity;
    }
}
