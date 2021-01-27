/*
 * Copyright (c) 2008-2020 Haulmont.
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

package io.jmix.reports.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import javax.annotation.PostConstruct;
import javax.persistence.*;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

@Entity(name = "report_ReportExecution")
@Table(name = "REPORT_EXECUTION")
@JmixEntity
public class ReportExecution {
    private static final long serialVersionUID = -1714474379895234441L;

    @Id
    @Column(name = "ID")
    @JmixGeneratedValue
    protected UUID id;

    @Column(name = "CREATE_TS")
    @CreatedDate
    private Date createTs;

    @Column(name = "CREATED_BY", length = 50)
    @CreatedBy
    private String createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REPORT_ID")
    private Report report;

    /**
     * De-normalized field in case if report name will change in the future
     */
    @Column(name = "REPORT_NAME", nullable = false)
    private String reportName;

    /**
     * De-normalized field in case if report code will change in the future
     */
    @Column(name = "REPORT_CODE")
    private String reportCode;

    @Column(name = "PRINCIPAL")
    private String user;

    @Column(name = "START_TIME", nullable = false)
    private Date startTime;

    @Column(name = "FINISH_TIME")
    private Date finishTime;

    @Column(name = "IS_SUCCESS")
    private Boolean success;

    @Column(name = "CANCELLED")
    private Boolean cancelled;

    @Column(name = "FILE_URI")
    private URI fileUri;

    @Column(name = "PARAMS")
    @Lob
    private String params;

    @Column(name = "ERROR_MESSAGE")
    @Lob
    private String errorMessage;

    @Column(name = "SERVER_ID", length = 50)
    private String serverId;

    @PostConstruct
    public void postConstruct() {
        success = false;
        cancelled = false;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getReportCode() {
        return reportCode;
    }

    public void setReportCode(String reportCode) {
        this.reportCode = reportCode;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Boolean getCancelled() {
        return cancelled;
    }

    public void setCancelled(Boolean cancelled) {
        this.cancelled = cancelled;
    }

    public URI getFileUri() {
        return fileUri;
    }

    public void setFileUri(URI fileUri) {
        this.fileUri = fileUri;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    @JmixProperty
    @DependsOnProperties({"finishTime", "startTime"})
    public Long getExecutionTimeSec() {
        if (finishTime == null || startTime == null)
            return null;

        return (finishTime.getTime() - startTime.getTime()) / 1000;
    }
}
