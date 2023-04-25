/*
 * Copyright 2021 Haulmont.
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

package test_support.entity.auditing;

import io.jmix.core.annotation.DeletedDate;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Table(name = "TEST_IRREGULAR_TYPES_ENTITY")
@JmixEntity
@Entity(name = "test_IRREGULAR_TYPES_ENTITY")
public class IrregularAuditTypesEntity {

    @Id
    @Column(name = "ID")
    @JmixGeneratedValue
    private UUID id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "BIRTH_DATE")
    @CreatedDate
    private OffsetDateTime createdDate;

    @Column(name = "TOUCH_DATE")
    @LastModifiedDate
    private LocalDate touchDate;

    @DeletedDate
    @Column(name = "WHEN_DELETED")
    protected LocalDateTime whenDeleted;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "DELETE_TS")

    public OffsetDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(OffsetDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDate getTouchDate() {
        return touchDate;
    }

    public void setTouchDate(LocalDate touchDate) {
        this.touchDate = touchDate;
    }

    public LocalDateTime getWhenDeleted() {
        return whenDeleted;
    }

    public void setWhenDeleted(LocalDateTime whenDeleted) {
        this.whenDeleted = whenDeleted;
    }
}
