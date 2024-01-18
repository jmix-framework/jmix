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

import io.jmix.core.annotation.DeletedBy;
import io.jmix.core.annotation.DeletedDate;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;
import java.util.UUID;

@JmixEntity
@MappedSuperclass
public class BaseEntity {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @Column(name = "CODE", nullable = false)
    @NotNull
    private String code;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "VERSION", nullable = false)
    @Version
    private Integer version;

    @CreatedBy
    @Column(name = "CREATED_BY")
    private String createdBy;

    @CreatedDate
    @Column(name = "CREATED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @LastModifiedBy
    @Column(name = "LAST_MODIFIED_BY")
    private String lastModifiedBy;

    @LastModifiedDate
    @Column(name = "LAST_MODIFIED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;

    @DeletedBy
    @Column(name = "DELETED_BY")
    private String deletedBy;
    @DeletedDate
    @Column(name = "DELETED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deletedDate;


    public final UUID getId() {
        return this.id;
    }

    public final void setId(@Nullable UUID id) {
        this.id = id;
    }


    public final String getCode() {
        return this.code;
    }

    public final void setCode(@Nullable String code) {
        this.code = code;
    }

    public final String getName() {
        return this.name;
    }

    public final void setName(@Nullable String name) {
        this.name = name;
    }

    public final String getDescription() {
        return this.description;
    }

    public final void setDescription(@Nullable String description) {
        this.description = description;
    }

    public final Integer getVersion() {
        return this.version;
    }

    public final void setVersion(@Nullable Integer version) {
        this.version = version;
    }

    public final String getCreatedBy() {
        return this.createdBy;
    }

    public final void setCreatedBy(@Nullable String createdBy) {
        this.createdBy = createdBy;
    }

    public final Date getCreatedDate() {
        return this.createdDate;
    }

    public final void setCreatedDate(@Nullable Date createdDate) {
        this.createdDate = createdDate;
    }

    public final String getLastModifiedBy() {
        return this.lastModifiedBy;
    }

    public final void setLastModifiedBy(@Nullable String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public final Date getLastModifiedDate() {
        return this.lastModifiedDate;
    }

    public final void setLastModifiedDate(@Nullable Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public final String getDeletedBy() {
        return this.deletedBy;
    }

    public final void setDeletedBy(@Nullable String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public final Date getDeletedDate() {
        return this.deletedDate;
    }

    public final void setDeletedDate(@Nullable Date deletedDate) {
        this.deletedDate = deletedDate;
    }
}