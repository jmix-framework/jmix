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
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.imap.flags.FlagsConverter;
import io.jmix.imap.flags.ImapFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.mail.Flags;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Table(name = "IMAP_MESSAGE")
@Entity(name = "imap_Message")
@JmixEntity
public class ImapMessage {

    private final static Logger log = LoggerFactory.getLogger(ImapMessage.class);

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

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FOLDER_ID")
    protected ImapFolder folder;

    @Lob
    @Column(name = "FLAGS")
    protected String flags;

    @NotNull
    @Column(name = "IS_ATL", nullable = false)
    protected Boolean attachmentsLoaded = false;

    @NotNull
    @Column(name = "MSG_UID", nullable = false)
    protected Long msgUid;

    @NotNull
    @Column(name = "MSG_NUM", nullable = false)
    protected Integer msgNum;

    @Column(name = "THREAD_ID")
    protected Long threadId;

    @Lob
    @Column(name = "REFERENCE_ID")
    protected String referenceId;

    @Lob
    @Column(name = "MESSAGE_ID")
    protected String messageId;

    @Lob
    @NotNull
    @Column(name = "CAPTION", nullable = false)
    protected String caption;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "RECEIVED_DATE")
    protected Date receivedDate;

    @Transient
    protected List<ImapFlag> internalFlags = Collections.emptyList();

    public Date getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(Date receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getFlags() {
        return flags;
    }

    public void setFlags(String flags) {
        this.flags = flags;
    }

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

    public void setImapFlags(Flags flags) {
        List<ImapFlag> internalFlags = FlagsConverter.convertToImapFlags(flags);
        if (!internalFlags.equals(this.internalFlags)) {
            log.debug("Convert imap flags {} to raw string", internalFlags);
            setFlags(FlagsConverter.convertToString(internalFlags));
            this.internalFlags = internalFlags;
        }
    }

    public Flags getImapFlags() {
        log.debug("Parse imap flags from raw string {}", flags);
        if (flags == null) {
            return new Flags();
        }
        this.internalFlags = FlagsConverter.convertToImapFlags(flags);

        return FlagsConverter.convertToFlags(this.internalFlags);
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setAttachmentsLoaded(Boolean attachmentsLoaded) {
        this.attachmentsLoaded = attachmentsLoaded;
    }

    public Boolean getAttachmentsLoaded() {
        return attachmentsLoaded;
    }

    public ImapFolder getFolder() {
        return folder;
    }

    public void setFolder(ImapFolder folder) {
        this.folder = folder;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    public Long getThreadId() {
        return threadId;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public void setMsgUid(Long msgUid) {
        this.msgUid = msgUid;
    }

    public Long getMsgUid() {
        return msgUid;
    }

    public Integer getMsgNum() {
        return msgNum;
    }

    public void setMsgNum(Integer msgNum) {
        this.msgNum = msgNum;
    }

    @InstanceName
    @DependsOnProperties({"caption", "msgNum"})
    public String getInstanceName() {
        return String.format("%s (#%d)", caption, msgNum);
    }
}