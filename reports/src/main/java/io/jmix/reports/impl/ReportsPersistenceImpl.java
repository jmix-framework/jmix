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

package io.jmix.reports.impl;

import io.jmix.core.*;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.core.security.EntityOp;
import io.jmix.data.DataProperties;
import io.jmix.data.PersistenceHints;
import io.jmix.data.exception.UniqueConstraintViolationException;
import io.jmix.data.persistence.DbmsSpecifics;
import io.jmix.dynattr.DynAttrQueryHints;
import io.jmix.reports.ReportsPersistence;
import io.jmix.reports.entity.*;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component("report_ReportsPersistence")
public class ReportsPersistenceImpl implements ReportsPersistence {

    public static final String REPORT_EDIT_FETCH_PLAN_NAME = "report.edit";
    protected static final String IDX_SEPARATOR = ",";

    @Autowired
    protected TransactionTemplate transaction;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected SecureOperations secureOperations;
    @Autowired
    protected PolicyStore policyStore;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected FetchPlanRepository fetchPlanRepository;
    @Autowired
    protected DbmsSpecifics dbmsSpecifics;
    @Autowired
    protected DataProperties dataProperties;
    @PersistenceContext
    protected EntityManager em;

    @Override
    public Report save(Report report) {
        checkPermission(report);

        Report savedReport = transaction.execute(action -> saveReport(report));

        FetchPlan reportEditFetchPlan = fetchPlanRepository.getFetchPlan(metadata.getClass(savedReport), REPORT_EDIT_FETCH_PLAN_NAME);
        return dataManager.load(Id.of(savedReport))
                .fetchPlan(reportEditFetchPlan)
                .hint(DynAttrQueryHints.LOAD_DYN_ATTR, true)
                .one();
    }

    @NotNull
    protected Report saveReport(Report report) {
        ReportTemplate defaultTemplate = report.getDefaultTemplate();
        List<ReportTemplate> loadedTemplates = report.getTemplates();
        List<ReportTemplate> savedTemplates = new ArrayList<>();

        report.setDefaultTemplate(null);
        report.setTemplates(null);

        if (report.getGroup() != null) {
            FetchPlan fetchPlan = fetchPlanRepository.getFetchPlan(ReportGroup.class, FetchPlan.INSTANCE_NAME);
            ReportGroup existingGroup = em.find(ReportGroup.class, report.getGroup().getId(),
                    PersistenceHints.builder().withFetchPlan(fetchPlan).build());
            if (existingGroup != null) {
                report.setGroup(existingGroup);
            } else {
                em.persist(report.getGroup());
            }
        }
        em.setProperty(PersistenceHints.SOFT_DELETION, false);
        Report existingReport;
        List<ReportTemplate> existingTemplates = null;
        try {
            FetchPlan fetchPlan = fetchPlanRepository.getFetchPlan(Report.class, "report.withTemplates");
            existingReport = em.find(Report.class, report.getId(),
                    PersistenceHints.builder().withFetchPlan(fetchPlan).build());
            storeIndexFields(report);

            if (existingReport != null) {
                report.setVersion(existingReport.getVersion());
                report = em.merge(report);
                if (existingReport.getTemplates() != null) {
                    existingTemplates = existingReport.getTemplates();
                }
                if (existingReport.getDeleteTs() != null) {
                    existingReport.setDeleteTs(null);
                    existingReport.setDeletedBy(null);
                }
                report.setDefaultTemplate(null);
                report.setTemplates(null);
            } else {
                report.setVersion(0);
                report = em.merge(report);
            }

            if (loadedTemplates != null) {
                if (existingTemplates != null) {
                    for (ReportTemplate template : existingTemplates) {
                        if (!loadedTemplates.contains(template)) {
                            em.remove(template);
                        }
                    }
                }

                for (ReportTemplate loadedTemplate : loadedTemplates) {
                    ReportTemplate existingTemplate = em.find(ReportTemplate.class, loadedTemplate.getId());
                    if (existingTemplate != null) {
                        loadedTemplate.setVersion(existingTemplate.getVersion());
                        if (entityStates.isNew(loadedTemplate)) {
                            entityStates.makeDetached(loadedTemplate);
                        }
                    } else {
                        loadedTemplate.setVersion(0);
                    }

                    loadedTemplate.setReport(report);
                    savedTemplates.add(em.merge(loadedTemplate));
                }
            }

            em.flush();
        } catch (PersistenceException e) {
            Pattern pattern = getUniqueConstraintViolationPattern();
            Matcher matcher = pattern.matcher(e.toString());
            if (matcher.find()) {
                throw new UniqueConstraintViolationException(e.getMessage(), resolveConstraintName(matcher), e);
            }
            throw e;
        } finally {
            em.setProperty(PersistenceHints.SOFT_DELETION, true);
        }

        for (ReportTemplate savedTemplate : savedTemplates) {
            if (savedTemplate.equals(defaultTemplate)) {
                defaultTemplate = savedTemplate;
                break;
            }
        }
        report.setDefaultTemplate(defaultTemplate);
        report.setTemplates(savedTemplates);
        return report;
    }

    protected void checkPermission(Report report) {
        if (entityStates.isNew(report)) {
            if (!secureOperations.isEntityCreatePermitted(metadata.getClass(Report.class), policyStore))
                throw new AccessDeniedException("entity", metadata.getClass(Report.class).getName(), EntityOp.UPDATE.getId());
        } else {
            if (!secureOperations.isEntityUpdatePermitted(metadata.getClass(Report.class), policyStore))
                throw new AccessDeniedException("entity", metadata.getClass(Report.class).getName(), EntityOp.UPDATE.getId());
        }
    }

    protected void storeIndexFields(Report report) {
        if (entityStates.isLoaded(report, "xml")) {
            StringBuilder entityTypes = new StringBuilder(IDX_SEPARATOR);
            if (report.getInputParameters() != null) {
                for (ReportInputParameter parameter : report.getInputParameters()) {
                    if (isNotBlank(parameter.getEntityMetaClass())) {
                        entityTypes.append(parameter.getEntityMetaClass())
                                .append(IDX_SEPARATOR);
                    }
                }
            }
            report.setInputEntityTypesIdx(entityTypes.length() > 1 ? entityTypes.toString() : null);

            StringBuilder screens = new StringBuilder(IDX_SEPARATOR);
            if (report.getReportScreens() != null) {
                for (ReportScreen reportScreen : report.getReportScreens()) {
                    screens.append(reportScreen.getScreenId())
                            .append(IDX_SEPARATOR);
                }
            }
            report.setScreensIdx(screens.length() > 1 ? screens.toString() : null);

            StringBuilder roles = new StringBuilder(IDX_SEPARATOR);
            if (report.getReportRoles() != null) {
                for (ReportRole reportRole : report.getReportRoles()) {
                    roles.append(reportRole.getRoleCode()).append(IDX_SEPARATOR);
                }
            }
            report.setRolesIdx(roles.length() > 1 ? roles.toString() : null);
        }
    }

    protected Pattern getUniqueConstraintViolationPattern() {
        String defaultPatternExpression = dbmsSpecifics.getDbmsFeatures().getUniqueConstraintViolationPattern();
        String patternExpression = dataProperties.getUniqueConstraintViolationPattern();

        Pattern pattern;
        if (StringUtils.isBlank(patternExpression)) {
            pattern = Pattern.compile(defaultPatternExpression);
        } else {
            try {
                pattern = Pattern.compile(patternExpression);
            } catch (PatternSyntaxException e) {
                pattern = Pattern.compile(defaultPatternExpression);
            }
        }
        return pattern;
    }

    protected String resolveConstraintName(Matcher matcher) {
        String constraintName = "";
        if (matcher.groupCount() == 1) {
            constraintName = matcher.group(1);
        } else {
            for (int i = 1; i < matcher.groupCount(); i++) {
                if (StringUtils.isNotBlank(matcher.group(i))) {
                    constraintName = matcher.group(i);
                    break;
                }
            }
        }
        return constraintName.toUpperCase();
    }
}