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

import io.jmix.core.entity.annotation.Listeners;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.ModelProperty;
import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;
import io.jmix.core.entity.annotation.SystemLevel;
import com.haulmont.yarg.structure.ReportBand;
import com.haulmont.yarg.structure.ReportFieldFormat;
import io.jmix.security.model.Role;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.util.*;

/**
 * Attention! This entity should be detached for correct work. If you do not detach it please use logic as in
 * com.haulmont.reports.listener.ReportDetachListener#onBeforeDetach(com.haulmont.reports.entity.Report, com.haulmont.cuba.core.EntityManager)
 */
@Entity(name = "report_Report")
@Table(name = "REPORT_REPORT")
@NamePattern("%s|locName,name,localeNames")
@Listeners("report_ReportDetachListener")
@SuppressWarnings("unused")
public class Report extends StandardEntity implements com.haulmont.yarg.structure.Report {
    private static final long serialVersionUID = -2817764915661205093L;

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
    protected String sysTenantId;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "report")
    @Composition
    protected List<ReportTemplate> templates;

    @Column(name = "IS_SYSTEM")
    protected Boolean system = false;

    @Transient
    protected BandDefinition rootBandDefinition;

    @Transient @ModelProperty
    protected Set<BandDefinition> bands = new HashSet<>();

    @Transient @ModelProperty @Composition
    protected List<ReportInputParameter> inputParameters = new ArrayList<>();

    @Transient @ModelProperty @Composition
    protected List<ReportValueFormat> valuesFormats = new ArrayList<>();

    @Transient @ModelProperty
    protected List<ReportScreen> reportScreens = new ArrayList<>();

    @Transient @ModelProperty
    protected Set<Role> roles = new HashSet<>();

    @Transient
    protected String localeName;

    @Transient
    protected Boolean isTmp = Boolean.FALSE;

    @Transient
    @ModelProperty
    protected String validationScript;

    @Transient
    @ModelProperty
    protected Boolean validationOn = false;

    public Boolean getIsTmp() {
        return isTmp;
    }

    public void setIsTmp(Boolean isTmp) {
        this.isTmp = isTmp;
    }

    @ModelProperty
    public BandDefinition getRootBandDefinition() {
        if (rootBandDefinition == null && bands != null && bands.size() > 0) {
            rootBandDefinition = (BandDefinition) CollectionUtils.find(bands, new Predicate() {
                @Override
                public boolean evaluate(Object object) {
                    BandDefinition band = (BandDefinition) object;
                    return band.getParentBandDefinition() == null;
                }
            });
        }
        return rootBandDefinition;
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

    @ModelProperty
    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        if (roles == null) roles = Collections.emptySet();
        this.roles = roles;
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

    @ModelProperty
    public String getLocName() {
        if (localeName == null) {
            //TODO Locale helper
//            localeName = LocaleHelper.getLocalizedName(localeNames);
            if (localeName == null)
                localeName = name;
        }
        return localeName;
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
}