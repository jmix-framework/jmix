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
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.imap.flags.FlagsConverter;
import io.jmix.imap.flags.ImapFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.Flags;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Table(name = "IMAP_MESSAGE_SYNC")
@Entity(name = "imap_MessageSync")
@JmixEntity
public class ImapMessageSync {
    private final static Logger log = LoggerFactory.getLogger(ImapMessageSync.class);

    @Id
    @Column(name = "ID")
    @JmixGeneratedValue
    protected UUID id;

    @NotNull
    @OnDeleteInverse(DeletePolicy.CASCADE)
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "", optional = false)
    @JoinColumn(name = "MESSAGE_ID", unique = true)
    protected ImapMessage message;

    @Lob
    @Column(name = "FLAGS")
    protected String flags;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FOLDER_ID")
    protected ImapFolder folder;

    @NotNull
    @Column(name = "STATUS", nullable = false)
    protected String status;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OLD_FOLDER_ID")
    protected ImapFolder oldFolder;

    @Column(name = "UPDATE_TS")
    protected Date updateTs;

    @Column(name = "UPDATED_BY", length = 50)
    protected String updatedBy;

    @Column(name = "CREATE_TS")
    protected Date createTs;

    @Column(name = "CREATED_BY", length = 50)
    protected String createdBy;

    @Version
    @Column(name = "VERSION", nullable = false)
    protected Integer version;

    @Transient
    protected List<ImapFlag> internalFlags = Collections.emptyList();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFlags() {
        return flags;
    }

    void setFlags(String flags) {
        this.flags = flags;
    }

    public void setImapFlags(Flags flags) {
        List<ImapFlag> internalFlags = FlagsConverter.convertToImapFlags(flags);
        if (!internalFlags.equals(this.internalFlags)) {
            log.debug("Convert imap flags {} to raw string", internalFlags);
            this.flags = FlagsConverter.convertToString(internalFlags);
            this.internalFlags = internalFlags;
        }
    }

    public Flags getImapFlags() {
        log.debug("Parse imap flags from raw string {}", flags);
        if (flags == null) {
            return new Flags();
        }
        this.internalFlags = FlagsConverter.convertToImapFlags(this.flags);

        return FlagsConverter.convertToFlags(internalFlags);
    }


    public void setUpdateTs(Date updateTs) {
        this.updateTs = updateTs;
    }

    public Date getUpdateTs() {
        return updateTs;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setCreateTs(Date createTs) {
        this.createTs = createTs;
    }

    public Date getCreateTs() {
        return createTs;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getVersion() {
        return version;
    }

    public void setMessage(ImapMessage message) {
        this.message = message;
    }

    public ImapMessage getMessage() {
        return message;
    }

    public void setStatus(ImapSyncStatus status) {
        this.status = status == null ? null : status.getId();
    }

    public ImapSyncStatus getStatus() {
        return status == null ? null : ImapSyncStatus.fromId(status);
    }

    public ImapFolder getFolder() {
        return folder;
    }

    public void setFolder(ImapFolder folder) {
        this.folder = folder;
    }

    public ImapFolder getOldFolder() {
        return oldFolder;
    }

    public void setOldFolder(ImapFolder oldFolder) {
        this.oldFolder = oldFolder;
    }

    @InstanceName
    @DependsOnProperties({"message", "status"})
    public String getInstanceName() {
        return String.format("%s with %s", message, status);
    }
}