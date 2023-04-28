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

package io.jmix.reports.entity;

import io.jmix.core.FileRef;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity(name = "report_ReportExecution")
@Table(name = "REPORT_EXECUTION")
@JmixEntity
public class ReportExecution {
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

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "START_TIME", nullable = false)
    private Date startTime;

    @Column(name = "FINISH_TIME")
    private Date finishTime;

    @Column(name = "IS_SUCCESS")
    private Boolean success;

    @Column(name = "CANCELLED")
    private Boolean cancelled;

    @Column(name = "OUTPUT_DOCUMENT", length = 4000)
    private FileRef outputDocument;

    @Column(name = "PARAMS")
    @Lob
    private String params;

    @Column(name = "ERROR_MESSAGE")
    @Lob
    private String errorMessage;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public FileRef getOutputDocument() {
        return outputDocument;
    }

    public void setOutputDocument(FileRef outputDocument) {
        this.outputDocument = outputDocument;
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

    @JmixProperty
    @DependsOnProperties({"finishTime", "startTime"})
    public Long getExecutionTimeSec() {
        if (finishTime == null || startTime == null)
            return null;

        return (finishTime.getTime() - startTime.getTime()) / 1000;
    }
}
