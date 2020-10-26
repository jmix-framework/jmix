/*
 * Copyright (c) 2008-2019 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.reports.entity;

import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.ModelProperty;
import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.core.entity.Creatable;
import com.haulmont.cuba.core.entity.FileDescriptor;

import javax.annotation.PostConstruct;
import javax.persistence.*;
import java.util.Date;

@Entity(name = "report_ReportExecution")
@Table(name = "REPORT_EXECUTION")
public class ReportExecution extends BaseUuidEntity implements Creatable {
    private static final long serialVersionUID = -1714474379895234441L;

    @Column(name = "CREATE_TS")
    private Date createTs;

    @Column(name = "CREATED_BY", length = 50)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OUTPUT_DOCUMENT_ID")
    private FileDescriptor outputDocument;

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

    @Override
    public Date getCreateTs() {
        return createTs;
    }

    @Override
    public void setCreateTs(Date createTs) {
        this.createTs = createTs;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
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

    public FileDescriptor getOutputDocument() {
        return outputDocument;
    }

    public void setOutputDocument(FileDescriptor outputDocument) {
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

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    @ModelProperty
    @DependsOnProperties({"finishTime", "startTime"})
    public Long getExecutionTimeSec() {
        if (finishTime == null || startTime == null)
            return null;

        return (finishTime.getTime() - startTime.getTime()) / 1000;
    }
}
