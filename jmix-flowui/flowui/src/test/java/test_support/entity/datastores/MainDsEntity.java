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

import io.jmix.core.entity.annotation.EmbeddedParameters;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import jakarta.persistence.*;

import java.util.UUID;

@JmixEntity
@Entity(name = "test_MainDsEntity")
@Table(name = "TEST_MAIN_DS_ENTITY")
public class MainDsEntity {

    @Id
    @Column(name = "ID", nullable = false)
    @JmixGeneratedValue
    private UUID id;

    @Column(name = "NAME")
    private String name;

    @SystemLevel
    @Column(name = "DB1_JPA_ENTITY_ID")
    private UUID db1JpaEntityId;

    @SystemLevel
    @Column(name = "MEM1_DTO_ENTITY_ID")
    private UUID mem1DtoEntityId;
    @DependsOnProperties({"db1JpaEntityId"})
    @JmixProperty
    @Transient
    private Db1JpaEntity db1JpaEntity;

    @DependsOnProperties({"mem1DtoEntityId"})
    @JmixProperty
    @Transient
    private Mem1DtoEntity mem1DtoEntity;

    @JmixProperty
    @Transient
    private NoStoreDtoEntity noStoreDtoEntity;

    @EmbeddedParameters(nullAllowed = false)
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "city", column = @Column(name = "EMBEDDED_CITY_ID")),
            @AttributeOverride(name = "city", column = @Column(name = "EMBEDDED_CITY"))
    })
    private MainDsEmbeddable embedded;

    public MainDsEmbeddable getEmbedded() {
        return embedded;
    }

    public void setEmbedded(MainDsEmbeddable embedded) {
        this.embedded = embedded;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getDb1JpaEntityId() {
        return db1JpaEntityId;
    }

    public void setDb1JpaEntityId(UUID db1JpaEntityId) {
        this.db1JpaEntityId = db1JpaEntityId;
    }

    public Db1JpaEntity getDb1JpaEntity() {
        return db1JpaEntity;
    }

    public void setDb1JpaEntity(Db1JpaEntity db1JpaEntity) {
        this.db1JpaEntity = db1JpaEntity;
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

    public NoStoreDtoEntity getNoStoreDtoEntity() {
        return noStoreDtoEntity;
    }

    public void setNoStoreDtoEntity(NoStoreDtoEntity noStoreDtoEntity) {
        this.noStoreDtoEntity = noStoreDtoEntity;
    }
}
