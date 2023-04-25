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

import io.jmix.core.DeletePolicy;
import io.jmix.core.FileRef;
import io.jmix.core.annotation.DeletedBy;
import io.jmix.core.annotation.DeletedDate;
import io.jmix.core.annotation.TenantId;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.PropertyDatatype;
import io.jmix.email.SendingStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Entity to store information about sending emails.
 *
 */
@Entity(name = "email_SendingMessage")
@Table(name = "EMAIL_SENDING_MESSAGE")
@SystemLevel
@JmixEntity
public class SendingMessage implements Serializable {

    private static final long serialVersionUID = -8156998515878702538L;

    public static final int SUBJECT_LENGTH = 500;
    public static final int BODY_CONTENT_TYPE_LENGTH = 50;
    public static final String HEADERS_SEPARATOR = "\n";

    @Id
    @Column(name = "ID", nullable = false)
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

    @Column(name = "ADDRESS_TO")
    protected String address;

    @Column(name = "ADDRESS_FROM")
    protected String from;

    @Column(name = "ADDRESS_CC")
    protected String cc;

    @Column(name = "ADDRESS_BCC")
    protected String bcc;

    @Column(name = "SUBJECT", length = SUBJECT_LENGTH)
    protected String subject;

    /**
     * Email body is stored either in this field or in {@link #contentTextFile}.
     */
    @Column(name = "CONTENT_TEXT")
    protected String contentText;

    @PropertyDatatype("fileRef")
    @Column(name = "CONTENT_TEXT_FILE")
    protected FileRef contentTextFile;

    @Column(name = "STATUS")
    protected Integer status;

    @Column(name = "DATE_SENT")
    protected Date dateSent;

    @Column(name = "ATTACHMENTS_NAME")
    protected String attachmentsName;

    @Column(name = "DEADLINE")
    protected Date deadline;

    @Column(name = "ATTEMPTS_LIMIT")
    protected Integer attemptsLimit;

    @Column(name = "ATTEMPTS_MADE")
    protected Integer attemptsMade;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToMany(mappedBy = "message")
    protected List<SendingAttachment> attachments;

    @Column(name = "EMAIL_HEADERS")
    protected String headers;

    @Column(name = "BODY_CONTENT_TYPE", length = BODY_CONTENT_TYPE_LENGTH)
    protected String bodyContentType;

    @TenantId
    @Column(name = "SYS_TENANT_ID")
    protected String sysTenantId;

    @Column(name = "IMPORTANT")
    protected Boolean important = false;

    @PrePersist
    protected void initLastAttemptTime() {
        if (getStatus() != null && getStatus() == SendingStatus.QUEUE && getAttemptsMade() == 0) {
            setUpdateTs(null);
            setUpdatedBy(null);
        }
    }

    public Boolean getImportant() {
        return important;
    }

    public void setImportant(Boolean important) {
        this.important = important;
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

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public SendingStatus getStatus() {
        return status == null ? null : SendingStatus.fromId(status);
    }

    public void setStatus(SendingStatus status) {
        this.status = status == null ? null : status.getId();
    }

    public Date getDateSent() {
        return dateSent;
    }

    public void setDateSent(Date dateSent) {
        this.dateSent = dateSent;
    }

    public Integer getAttemptsLimit() {
        return attemptsLimit;
    }

    public void setAttemptsLimit(Integer attemptsLimit) {
        this.attemptsLimit = attemptsLimit;
    }

    public String getAttachmentsName() {
        return attachmentsName;
    }

    public void setAttachmentsName(String attachmentsName) {
        this.attachmentsName = attachmentsName;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setSubject(String subject) {
        this.subject = StringUtils.substring(subject, 0, SendingMessage.SUBJECT_LENGTH);
    }

    public String getSubject() {
        return subject;
    }

    public List<SendingAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<SendingAttachment> attachments) {
        this.attachments = attachments;
    }

    public Integer getAttemptsMade() {
        return attemptsMade;
    }

    public void setAttemptsMade(Integer attemptsMade) {
        this.attemptsMade = attemptsMade;
    }

    public FileRef getContentTextFile() {
        return contentTextFile;
    }

    public void setContentTextFile(FileRef contentTextFile) {
        this.contentTextFile = contentTextFile;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getBodyContentType() {
        return bodyContentType;
    }

    public void setBodyContentType(String bodyContentType) {
        this.bodyContentType = bodyContentType;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getBcc() {
        return bcc;
    }

    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    public String getSysTenantId() {
        return sysTenantId;
    }

    public void setSysTenantId(String sysTenantId) {
        this.sysTenantId = sysTenantId;
    }
}
