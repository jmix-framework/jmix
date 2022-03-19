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

package io.jmix.search.index.queue.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.search.index.queue.impl.EnqueueingSessionStatus;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

/**
 * Keeps progress of async enqueueing process.
 */
@JmixEntity
@Table(name = "SEARCH_ENQUEUEING_SESSION")
@Entity(name = "search_EnqueueingSession")
public class EnqueueingSession {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "STATUS", nullable = false)
    private String status;

    @InstanceName
    @NotNull
    @Column(name = "ENTITY_NAME", nullable = false)
    private String entityName;

    @NotNull
    @Column(name = "ORDERING_PROPERTY", nullable = false)
    private String orderingProperty;

    @Column(name = "LAST_PROCESSED_VALUE", length = 1000)
    private String lastProcessedValue;

    @CreatedBy
    @Column(name = "CREATED_BY")
    private String createdBy;

    @CreatedDate
    @Column(name = "CREATED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    public EnqueueingSessionStatus getStatus() {
        return status == null ? null : EnqueueingSessionStatus.fromId(status);
    }

    public void setStatus(EnqueueingSessionStatus status) {
        this.status = status == null ? null : status.getId();
    }

    public String getLastProcessedValue() {
        return lastProcessedValue;
    }

    public void setLastProcessedValue(String lastProcessedValue) {
        this.lastProcessedValue = lastProcessedValue;
    }

    public String getOrderingProperty() {
        return orderingProperty;
    }

    public void setOrderingProperty(String orderingProperty) {
        this.orderingProperty = orderingProperty;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}