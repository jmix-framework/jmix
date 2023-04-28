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

package io.jmix.emailtemplates.entity;


import io.jmix.core.DeletePolicy;
import io.jmix.core.annotation.DeletedBy;
import io.jmix.core.annotation.DeletedDate;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.reports.entity.Report;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Table(name = "EMLTMP_EMAIL_TEMPLATE")
@Entity(name = "emltmp_EmailTemplate")
@JmixEntity
@DiscriminatorColumn(name = "DTYPE", discriminatorType = DiscriminatorType.STRING, length = 100)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class EmailTemplate {

    @Id
    @Column(name = "ID")
    @JmixGeneratedValue
    protected UUID id;

    @Version
    @Column(name = "VERSION", nullable = false)
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

    @Column(name = "USE_REPORT_SUBJECT")
    protected Boolean useReportSubject;

    @NotNull
    @Column(name = "NAME", nullable = false)
    protected String name;

    @OnDeleteInverse(DeletePolicy.UNLINK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GROUP_ID")
    protected TemplateGroup group;

    @NotNull
    @Column(name = "TYPE_", nullable = false)
    protected String type;

    @NotNull
    @Column(name = "CODE", nullable = false, unique = true)
    protected String code;

    @Email
    @Column(name = "FROM_")
    protected String from;

    @Lob
    @Column(name = "TO_")
    protected String to;

    @Lob
    @Column(name = "CC")
    protected String cc;

    @Lob
    @Column(name = "BCC")
    protected String bcc;

    @Column(name = "SUBJECT")
    protected String subject;

    @Composition
    @OneToMany(mappedBy = "emailTemplate")
    protected List<EmailTemplateAttachment> attachedFiles;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToMany(mappedBy = "emailTemplate", cascade = CascadeType.ALL)
    protected List<TemplateReport> attachedTemplateReports;

    public void setAttachedTemplateReports(List<TemplateReport> attachedTemplateReports) {
        this.attachedTemplateReports = attachedTemplateReports;
    }

    public List<TemplateReport> getAttachedTemplateReports() {
        return attachedTemplateReports;
    }

    public void setAttachedFiles(List<EmailTemplateAttachment> attachedFiles) {
        this.attachedFiles = attachedFiles;
    }

    public List<EmailTemplateAttachment> getAttachedFiles() {
        return attachedFiles;
    }

    public void setType(TemplateType type) {
        this.type = type == null ? null : type.getId();
    }

    public TemplateType getType() {
        return type == null ? null : TemplateType.fromId(type);
    }

    @InstanceName
    @DependsOnProperties({"name", "code"})
    public String getInstanceName() {
        return String.format("%s (%s)", name, code);
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getFrom() {
        return from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getTo() {
        return to;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getCc() {
        return cc;
    }

    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    public String getBcc() {
        return bcc;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSubject() {
        return subject;
    }

    public void setGroup(TemplateGroup group) {
        this.group = group;
    }

    public TemplateGroup getGroup() {
        return group;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public abstract Report getReport();

    public abstract void setEmailBodyReport(TemplateReport emailBodyReport);

    public abstract TemplateReport getEmailBodyReport();

    public Boolean getUseReportSubject() {
        return useReportSubject;
    }

    public void setUseReportSubject(Boolean useReportSubject) {
        this.useReportSubject = useReportSubject;
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

    public void setType(String type) {
        this.type = type;
    }


}