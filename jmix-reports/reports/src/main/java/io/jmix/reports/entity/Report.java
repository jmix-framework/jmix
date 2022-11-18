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

import com.haulmont.yarg.structure.ReportBand;
import com.haulmont.yarg.structure.ReportFieldFormat;
import io.jmix.core.annotation.DeletedBy;
import io.jmix.core.annotation.DeletedDate;
import io.jmix.core.annotation.TenantId;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.Listeners;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.*;
import io.jmix.reports.util.MsgBundleTools;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Attention! This entity should be detached for correct work. If you do not detach it please use logic as in
 * {@link io.jmix.reports.listener.ReportDetachListener#onBeforeDetach(Report)}
 */
@Entity(name = "report_Report")
@Table(name = "REPORT_REPORT")
@Listeners("report_ReportDetachListener")
@JmixEntity
@SuppressWarnings("unused")
public class Report implements com.haulmont.yarg.structure.Report {
    private static final long serialVersionUID = -2817764915661205093L;
    protected static final String IDX_SEPARATOR = ",";

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

    @Column(name = "NAME", length = 255, nullable = false, unique = true)
    protected String name;

    @Column(name = "LOCALE_NAMES")
    protected String localeNames;

    @Column(name = "CODE", length = 255)
    protected String code;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "GROUP_ID")
    protected ReportGroup group;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEFAULT_TEMPLATE_ID")
    protected ReportTemplate defaultTemplate;

    @Column(name = "REPORT_TYPE")
    protected Integer reportType;

    @Column(name = "DESCRIPTION", length = 500)
    protected String description;

    @Column(name = "XML")
    @Lob
    protected String xml;

    @Column(name = "ROLES_IDX", length = 1000)
    protected String rolesIdx;

    @Column(name = "SCREENS_IDX", length = 1000)
    protected String screensIdx;

    @Column(name = "INPUT_ENTITY_TYPES_IDX", length = 1000)
    protected String inputEntityTypesIdx;

    @Column(name = "REST_ACCESS")
    protected Boolean restAccess;

    @SystemLevel
    @Column(name = "SYS_TENANT_ID")
    @TenantId
    protected String sysTenantId;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "report")
    @Composition
    protected List<ReportTemplate> templates;

    @Column(name = "IS_SYSTEM")
    protected Boolean system = false;

    @Transient
    @JmixProperty
    protected BandDefinition rootBandDefinition;

    @Transient
    @JmixProperty
    @Composition
    protected Set<BandDefinition> bands = new LinkedHashSet<>();

    @Transient
    @JmixProperty
    @Composition
    protected List<ReportInputParameter> inputParameters = new ArrayList<>();

    @Transient
    @JmixProperty
    @Composition
    protected List<ReportValueFormat> valuesFormats = new ArrayList<>();

    @Transient
    @JmixProperty
    @Composition
    protected List<ReportScreen> reportScreens = new ArrayList<>();

    @Transient
    @JmixProperty
    @Composition
    protected Set<ReportRole> reportRoles = new HashSet<>();

    @Transient
    protected Boolean isTmp = Boolean.FALSE;

    @Transient
    @JmixProperty
    protected String validationScript;

    @Transient
    @JmixProperty
    protected Boolean validationOn = false;

    public Boolean getIsTmp() {
        return isTmp;
    }

    public void setIsTmp(Boolean isTmp) {
        this.isTmp = isTmp;
    }

    public BandDefinition getRootBandDefinition() {
        if (rootBandDefinition == null && bands != null && bands.size() > 0) {
            rootBandDefinition = IterableUtils.find(bands, band ->
                    band.getParentBandDefinition() == null
            );
        }
        return rootBandDefinition;
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
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ReportInputParameter> getInputParameters() {
        return inputParameters;
    }

    public void setInputParameters(List<ReportInputParameter> inputParameters) {
        if (inputParameters == null) inputParameters = Collections.emptyList();
        this.inputParameters = inputParameters;
    }

    public List<ReportValueFormat> getValuesFormats() {
        return valuesFormats;
    }

    public void setValuesFormats(List<ReportValueFormat> valuesFormats) {
        if (valuesFormats == null) valuesFormats = Collections.emptyList();
        this.valuesFormats = valuesFormats;
    }

    public ReportType getReportType() {
        return reportType != null ? ReportType.fromId(reportType) : null;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType != null ? reportType.getId() : null;
    }

    public Set<ReportRole> getReportRoles() {
        return reportRoles;
    }

    public void setReportRoles(Set<ReportRole> reportRoles) {
        if (reportRoles == null) reportRoles = Collections.emptySet();
        this.reportRoles = reportRoles;
    }

    public List<ReportScreen> getReportScreens() {
        return reportScreens;
    }

    public void setReportScreens(List<ReportScreen> reportScreens) {
        if (reportScreens == null) reportScreens = Collections.emptyList();
        this.reportScreens = reportScreens;
    }

    public List<ReportTemplate> getTemplates() {
        return templates;
    }

    public void setTemplates(List<ReportTemplate> templates) {
        this.templates = templates;
    }

    public Boolean getSystem() {
        return system;
    }

    public void setSystem(Boolean system) {
        this.system = system;
    }

    public ReportTemplate getDefaultTemplate() {
        return defaultTemplate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDefaultTemplate(ReportTemplate defaultTemplate) {
        this.defaultTemplate = defaultTemplate;
    }

    public ReportTemplate getTemplateByCode(String templateCode) {
        ReportTemplate template = null;
        if (templates != null) {
            Iterator<ReportTemplate> iter = templates.iterator();
            while (iter.hasNext() && template == null) {
                ReportTemplate temp = iter.next();
                if (StringUtils.equalsIgnoreCase(temp.getCode(), templateCode)) {
                    template = temp;
                }
            }
        }
        return template;
    }

    public ReportGroup getGroup() {
        return group;
    }

    public void setGroup(ReportGroup group) {
        this.group = group;
    }

    public Set<BandDefinition> getBands() {
        return bands;
    }

    public void setBands(Set<BandDefinition> bands) {
        if (bands == null) bands = Collections.emptySet();
        this.bands = bands;
    }

    public String getLocaleNames() {
        return localeNames;
    }

    public void setLocaleNames(String localeNames) {
        this.localeNames = localeNames;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public String getRolesIdx() {
        return rolesIdx;
    }

    public void setRolesIdx(String rolesIdx) {
        this.rolesIdx = rolesIdx;
    }

    public String getScreensIdx() {
        return screensIdx;
    }

    public void setScreensIdx(String screensIdx) {
        this.screensIdx = screensIdx;
    }

    public String getInputEntityTypesIdx() {
        return inputEntityTypesIdx;
    }

    public void setInputEntityTypesIdx(String inputEntityTypesIdx) {
        this.inputEntityTypesIdx = inputEntityTypesIdx;
    }

    public Boolean getRestAccess() {
        return restAccess;
    }

    public void setRestAccess(Boolean restAccess) {
        this.restAccess = restAccess;
    }

    public String getSysTenantId() {
        return sysTenantId;
    }

    public void setSysTenantId(String sysTenantId) {
        this.sysTenantId = sysTenantId;
    }

    @Override
    public Map<String, com.haulmont.yarg.structure.ReportTemplate> getReportTemplates() {
        Map<String, com.haulmont.yarg.structure.ReportTemplate> templateMap = new HashMap<>();
        for (ReportTemplate template : templates) {
            templateMap.put(template.getCode(), template);
        }

        return templateMap;
    }

    @Override
    public List<com.haulmont.yarg.structure.ReportParameter> getReportParameters() {
        return (List) inputParameters;
    }

    @Override
    public List<ReportFieldFormat> getReportFieldFormats() {
        return (List) valuesFormats;
    }

    @Override
    public ReportBand getRootBand() {
        return getRootBandDefinition();
    }

    public String getValidationScript() {
        return validationScript;
    }

    public void setValidationScript(String validationScript) {
        this.validationScript = validationScript;
    }

    public Boolean getValidationOn() {
        return validationOn;
    }

    public void setValidationOn(Boolean validationOn) {
        this.validationOn = validationOn;
    }

    @InstanceName
    @DependsOnProperties({"localeNames", "name"})
    public String getInstanceName(MsgBundleTools msgBundleTools) {
        return msgBundleTools.getLocalizedValue(localeNames, name);
    }
}