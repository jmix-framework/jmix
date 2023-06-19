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

import io.jmix.reports.yarg.formatters.CustomReport;
import io.jmix.core.annotation.DeletedBy;
import io.jmix.core.annotation.DeletedDate;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.reports.entity.charts.AbstractChartDescription;
import io.jmix.reports.entity.pivottable.PivotTableDescription;
import io.jmix.reports.entity.table.TemplateTableDescription;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import org.springframework.lang.Nullable;
import jakarta.persistence.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Template for {@link Report}
 */
@Entity(name = "report_ReportTemplate")
@Table(name = "REPORT_TEMPLATE")
@SystemLevel
@JmixEntity
@SuppressWarnings("unused")
public class ReportTemplate implements io.jmix.reports.yarg.structure.ReportTemplate {
    private static final long serialVersionUID = 3692751073234357754L;

    public static final String DEFAULT_TEMPLATE_CODE = "DEFAULT";

    public static final String NAME_FORMAT = "(%s) %s";

    @JmixGeneratedValue
    @Id
    @Column(name = "ID", nullable = false)
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "REPORT_ID")
    protected Report report;

    @Column(name = "OUTPUT_TYPE")
    protected Integer reportOutputType;

    @Column(name = "CODE")
    protected String code;

    @Column(name = "IS_GROOVY")
    protected Boolean groovy = false;

    @Column(name = "IS_CUSTOM")
    protected Boolean custom = false;

    @Column(name = "IS_ALTERABLE_OUTPUT")
    protected Boolean alterable = false;

    @Column(name = "CUSTOM_CLASS")
    protected String customDefinition;

    @Column(name = "CUSTOM_DEFINED_BY")
    protected Integer customDefinedBy = CustomTemplateDefinedBy.CLASS.getId();

    @Column(name = "OUTPUT_NAME_PATTERN", length = 255)
    protected String outputNamePattern;

    @Column(name = "NAME", length = 500)
    protected String name;

    @Column(name = "CONTENT")
    protected byte[] content;

    @Transient
    protected transient CustomReport customReport;

    public ReportOutputType getReportOutputType() {
        return ReportOutputType.fromId(reportOutputType);
    }

    public void setReportOutputType(ReportOutputType reportOutputType) {
        this.reportOutputType = reportOutputType != null ? reportOutputType.getId() : null;
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

    @Override
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public Boolean getCustom() {
        return custom;
    }

    public void setCustom(Boolean custom) {
        this.custom = custom;
    }

    public Boolean getAlterable() {
        return alterable;
    }

    public void setAlterable(Boolean alterable) {
        this.alterable = alterable;
    }

    public Boolean getGroovy() {
        return groovy;
    }

    public void setGroovy(Boolean groovy) {
        this.groovy = groovy;
    }

    public String getCustomDefinition() {
        return customDefinition;
    }

    public void setCustomDefinition(String customDefinition) {
        this.customDefinition = customDefinition;
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

    public String getExt() {
        return StringUtils.substringAfterLast(name, ".");
    }

    public CustomTemplateDefinedBy getCustomDefinedBy() {
        return CustomTemplateDefinedBy.fromId(customDefinedBy);
    }

    public void setCustomDefinedBy(CustomTemplateDefinedBy customDefinedBy) {
        this.customDefinedBy = CustomTemplateDefinedBy.getId(customDefinedBy);
    }

    @Override
    public String getDocumentName() {
        return name;
    }

    @Override
    public String getDocumentPath() {
        return name;
    }

    @Override
    public InputStream getDocumentContent() {
        if (getContent() == null) {
            return null;
        }

        return new ByteArrayInputStream(getContent());
    }

    @Override
    public io.jmix.reports.yarg.structure.ReportOutputType getOutputType() {
        return getReportOutputType() != null ? getReportOutputType().getOutputType() : null;
    }

    public void setOutputNamePattern(String outputNamePattern) {
        this.outputNamePattern = outputNamePattern;
    }

    @Override
    public String getOutputNamePattern() {
        return outputNamePattern;
    }

    @Override
    public boolean isGroovy() {
        return Boolean.TRUE.equals(groovy);
    }

    @Override
    public boolean isCustom() {
        return Boolean.TRUE.equals(custom);
    }

    @Override
    public CustomReport getCustomReport() {
        return customReport;
    }

    public void setCustomReport(CustomReport customReport) {
        this.customReport = customReport;
    }

    @InstanceName
    @DependsOnProperties({"code", "name", "customDefinition", "custom"})
    public String getCaption() {
        if (isCustom()) {
            return String.format(NAME_FORMAT, this.code, this.customDefinition);
        } else {
            return String.format(NAME_FORMAT, this.code, this.name);
        }
    }

    @Nullable
    public AbstractChartDescription getChartDescription() {
        if (getContent() == null) {
            return null;
        }
        return AbstractChartDescription.fromJsonString(new String(getContent(), StandardCharsets.UTF_8));
    }

    public void setChartDescription(@Nullable AbstractChartDescription chartDescription) {
        if (chartDescription != null && getReportOutputType() == ReportOutputType.CHART) {
            String jsonString = AbstractChartDescription.toJsonString(chartDescription);
            setContent(jsonString.getBytes(StandardCharsets.UTF_8));
            setName(".chart");
        }
    }

    public TemplateTableDescription getTemplateTableDescription() {
        if (getContent() == null)
            return null;

        return TemplateTableDescription.fromJsonString(new String(getContent(), StandardCharsets.UTF_8));
    }

    public void setTemplateTableDescription(TemplateTableDescription description) {
        if (description != null && getReportOutputType() == ReportOutputType.TABLE) {
            byte[] template = TemplateTableDescription.toJsonString(description).getBytes(StandardCharsets.UTF_8);

            setContent(template);
            setName(".table");
        }
    }


    public PivotTableDescription getPivotTableDescription() {
        if (getContent() == null)
            return null;
        return PivotTableDescription.fromJsonString(new String(getContent(), StandardCharsets.UTF_8));
    }

    public void setPivotTableDescription(PivotTableDescription description) {
        if (description != null && getReportOutputType() == ReportOutputType.PIVOT_TABLE) {
            setContent(PivotTableDescription.toJsonString(description).getBytes(StandardCharsets.UTF_8));
            setName(".pivot");
        }
    }
}