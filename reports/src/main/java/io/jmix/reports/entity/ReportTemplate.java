/*
 * Copyright (c) 2008-2019 Haulmont.
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

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.yarg.formatters.CustomReport;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.reports.entity.charts.AbstractChartDescription;
import io.jmix.reports.entity.pivottable.PivotTableDescription;
import io.jmix.reports.entity.table.TemplateTableDescription;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Template for {@link Report}
 *
 */
@Entity(name = "report_ReportTemplate")
@Table(name = "REPORT_TEMPLATE")
@SystemLevel
@NamePattern("#getCaption|code,name,customDefinition,custom,alterable")
@SuppressWarnings("unused")
public class ReportTemplate extends StandardEntity implements com.haulmont.yarg.structure.ReportTemplate {
    private static final long serialVersionUID = 3692751073234357754L;

    public static final String DEFAULT_TEMPLATE_CODE = "DEFAULT";

    public static final String NAME_FORMAT = "(%s) %s";

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
    public com.haulmont.yarg.structure.ReportOutputType getOutputType() {
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

    public String getCaption() {
        if (isCustom()) {
            return String.format(NAME_FORMAT, code, customDefinition);
        } else {
            return String.format(NAME_FORMAT, code, name);
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
            setContent(TemplateTableDescription.toJsonString(description).getBytes(StandardCharsets.UTF_8));
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