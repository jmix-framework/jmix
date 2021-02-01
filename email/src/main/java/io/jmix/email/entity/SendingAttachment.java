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

package io.jmix.email.entity;

import io.jmix.core.FileRef;
import io.jmix.core.annotation.DeletedBy;
import io.jmix.core.annotation.DeletedDate;
import io.jmix.core.annotation.TenantId;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity(name = "email_SendingAttachment")
@Table(name = "EMAIL_SENDING_ATTACHMENT")
@SystemLevel
@JmixEntity
public class SendingAttachment implements Serializable {
    private static final long serialVersionUID = -8253918579521701435L;

    @Id
    @Column(name = "ID")
    @JmixGeneratedValue
    private UUID id;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @CreatedDate
    @Column(name = "CREATE_TS")
    private Date createTs;

    @CreatedBy
    @Column(name = "CREATED_BY", length = 50)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "UPDATE_TS")
    private Date updateTs;

    @LastModifiedBy
    @Column(name = "UPDATED_BY", length = 50)
    private String updatedBy;

    @DeletedDate
    @Column(name = "DELETE_TS")
    private Date deleteTs;

    @DeletedBy
    @Column(name = "DELETED_BY", length = 50)
    private String deletedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MESSAGE_ID")
    protected SendingMessage message;

    /**
     * Attachment data is stored either in this field or in {@link #contentFile}
     */
    @Column(name = "CONTENT")
    protected byte[] content;

    @Column(name = "CONTENT_FILE")
    protected FileRef contentFile;

    @Column(name = "NAME", length = 500)
    protected String name;

    @Column(name = "CONTENT_ID", length = 50)
    protected String contentId;

    @Column(name = "DISPOSITION", length = 50)
    protected String disposition;

    @Column(name = "TEXT_ENCODING", length = 50)
    protected String encoding;

    @TenantId
    @Column(name = "SYS_TENANT_ID")
    protected String sysTenantId;

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

    public SendingMessage getMessage() {
        return message;
    }

    public void setMessage(SendingMessage message) {
        this.message = message;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public String getDisposition() {
        return disposition;
    }

    public void setDisposition(String disposition) {
        this.disposition = disposition;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public FileRef getContentFile() {
        return contentFile;
    }

    public void setContentFile(FileRef contentFile) {
        this.contentFile = contentFile;
    }

    public String getSysTenantId() {
        return sysTenantId;
    }

    public void setSysTenantId(String sysTenantId) {
        this.sysTenantId = sysTenantId;
    }
}