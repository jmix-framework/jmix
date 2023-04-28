/*
 * Copyright 2020 Haulmont.
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

package io.jmix.imap.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.annotation.DeletedBy;
import io.jmix.core.annotation.DeletedDate;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Table(name = "IMAP_FOLDER")
@Entity(name = "imap_Folder")
@JmixEntity
public class ImapFolder {
    @Id
    @Column(name = "ID")
    @JmixGeneratedValue
    protected UUID id;

    @Version
    @Column(name = "VERSION")
    protected Integer version;

    @CreatedDate
    @Column(name = "CREATE_TS")
    protected Date createTs;

    @CreatedBy
    @Column(name = "CREATED_BY", length = 50)
    protected String createdBy;

    @LastModifiedDate
    @Column(name = "UPDATE_TS")
    protected Date updateTs;

    @LastModifiedBy
    @Column(name = "UPDATED_BY", length = 50)
    protected String updatedBy;

    @DeletedDate
    @Column(name = "DELETE_TS")
    protected Date deleteTs;

    @DeletedBy
    @Column(name = "DELETED_BY", length = 50)
    protected String deletedBy;

    @Column(name = "NAME", nullable = false)
    @InstanceName
    protected String name;

    @OnDelete(DeletePolicy.CASCADE)
    @OneToMany(mappedBy = "folder")
    @Composition
    @OrderBy("event")
    protected List<ImapFolderEvent> events;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MAIL_BOX_ID")
    protected ImapMailBox mailBox;

    @Column(name = "ENABLED", nullable = false)
    protected Boolean enabled = false;

    @Column(name = "CAN_HOLD_MESSAGES", nullable = false)
    protected Boolean canHoldMessages = false;

    @Column(name = "DELETED")
    protected Boolean deleted;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_FOLDER_ID")
    protected ImapFolder parent;

    @Transient
    @JmixProperty
    protected Boolean unregistered = false;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Date getCreateTs() {
        return createTs;
    }

    public void setCreateTs(Date createTs) {
        this.createTs = createTs;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getUpdateTs() {
        return updateTs;
    }

    public void setUpdateTs(Date updateTs) {
        this.updateTs = updateTs;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getDeleteTs() {
        return deleteTs;
    }

    public void setDeleteTs(Date deleteTs) {
        this.deleteTs = deleteTs;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public ImapMailBox getMailBox() {
        return mailBox;
    }

    public void setMailBox(ImapMailBox mailBox) {
        this.mailBox = mailBox;
    }

    public List<ImapFolderEvent> getEvents() {
        return events;
    }

    public void setEvents(List<ImapFolderEvent> events) {
        this.events = events;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ImapFolderEvent getEvent(ImapEventType eventType) {
        if (CollectionUtils.isNotEmpty(events)) {
            return events.stream()
                    .filter(e -> e.getEvent() == eventType)
                    .findFirst().orElse(null);
        }

        return null;
    }

    public boolean hasEvent(ImapEventType eventType) {
        ImapFolderEvent event = getEvent(eventType);
        return event != null && BooleanUtils.isTrue(event.getEnabled());
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getCanHoldMessages() {
        return canHoldMessages;
    }

    public void setCanHoldMessages(Boolean canHoldMessages) {
        this.canHoldMessages = canHoldMessages;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public ImapFolder getParent() {
        return parent;
    }

    public void setParent(ImapFolder parent) {
        this.parent = parent;
    }

    public Boolean getUnregistered() {
        return unregistered;
    }

    public void setUnregistered(Boolean unregistered) {
        this.unregistered = unregistered;
    }

    @JmixProperty
    @DependsOnProperties({"events"})
    public String getEventsInfo() {
        String eventsInfo = "";
        if (CollectionUtils.isNotEmpty(events)) {
            eventsInfo = events.stream()
                    .map(imapFolderEvent -> imapFolderEvent.getEvent().toString())
                    .collect(Collectors.joining(", "));
        }

        return eventsInfo;
    }

}