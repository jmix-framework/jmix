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
import io.jmix.data.PersistenceHints;
import io.jmix.dynattr.DynAttrQueryHints;
import io.jmix.reports.ReportsPersistence;
import io.jmix.reports.entity.*;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    private MetadataTools metadataTools;

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
        ReportTemplate incomingDefaultTemplate = report.getDefaultTemplate();
        List<ReportTemplate> incomingTemplates = report.getTemplates();

        report.setDefaultTemplate(null);
        report.setTemplates(null);

        SaveContext saveContext = new SaveContext();

        manageGroup(report, saveContext);
        storeIndexFields(report);
        incomingDefaultTemplate = manageTemplates(report, incomingTemplates, incomingDefaultTemplate, saveContext);
        saveContext.saving(report);

        EntitySet saved = dataManager.save(saveContext);
        Report savedReport = saved.get(report);
        ReportTemplate savedDefaultTemplate = saved.get(incomingDefaultTemplate);

        savedReport.setDefaultTemplate(savedDefaultTemplate);
        return dataManager.save(savedReport);
    }

    protected void manageGroup(Report report, SaveContext saveContext) {
        ReportGroup group = report.getGroup();
        if (group != null) {
            FetchPlan fetchPlan = fetchPlanRepository.getFetchPlan(ReportGroup.class, FetchPlan.LOCAL);
            List<ReportGroup> existingGroups = dataManager.load(ReportGroup.class)
                    .query("select g from report_ReportGroup g where g.title = :title")
                    .parameter("title", group.getTitle())
                    .fetchPlan(fetchPlan).list();
            ReportGroup existingGroup;
            if (CollectionUtils.isEmpty(existingGroups)) {
                existingGroup = dataManager.load(ReportGroup.class)
                        .id(report.getGroup().getId())
                        .hint(PersistenceHints.SOFT_DELETION, false)
                        .optional()
                        .orElse(null);
            } else {
                existingGroup = existingGroups.get(0);
            }
            if (existingGroup != null) {
                if (!entityStates.isDeleted(existingGroup)) {
                    report.setGroup(existingGroup);
                } else {
                    group = dataManager.create(ReportGroup.class);
                    UUID newId = group.getId();
                    group = metadataTools.copy(existingGroup);
                    group.setVersion(0);
                    group.setDeleteTs(null);
                    group.setDeletedBy(null);
                    group.setId(newId);
                    report.setGroup(group);
                    saveContext.saving(group);
                }
            } else {
                saveContext.saving(group);
            }
        }
    }

    protected ReportTemplate manageTemplates(Report report, @Nullable List<ReportTemplate> incomingTemplates, ReportTemplate incomingDefaultTemplate, SaveContext saveContext) {
        FetchPlan fetchPlan = fetchPlanRepository.getFetchPlan(Report.class, "report.withTemplates");
        Report existingReport = dataManager.load(Report.class)
                .id(report.getId())
                .fetchPlan(fetchPlan)
                .optional()
                .orElse(null);

        List<ReportTemplate> existingTemplates = null;
        if (existingReport != null) {
            report.setVersion(existingReport.getVersion());
            if (existingReport.getTemplates() != null) {
                existingTemplates = existingReport.getTemplates();
            }
            if (existingReport.getDeleteTs() != null) {
                existingReport.setDeleteTs(null);
                existingReport.setDeletedBy(null);
            }
        } else {
            report.setVersion(0);
        }

        List<ReportTemplate> savedTemplates = new ArrayList<>();
        if (incomingTemplates != null) {
            if (existingTemplates != null) {
                for (ReportTemplate existingTemplate : existingTemplates) {
                    if (!incomingTemplates.contains(existingTemplate)) {
                        saveContext.removing(existingTemplate);
                    }
                }
            }

            for (ReportTemplate incomingTemplate : incomingTemplates) {
                ReportTemplate existingTemplate = dataManager.load(ReportTemplate.class)
                        .id(incomingTemplate.getId())
                        .optional()
                        .orElse(null);
                if (existingTemplate != null) {
                    incomingTemplate.setVersion(existingTemplate.getVersion());
                    if (entityStates.isNew(incomingTemplate)) {
                        entityStates.makeDetached(incomingTemplate);
                    }
                } else {
                    incomingTemplate.setVersion(0);
                }

                incomingTemplate.setReport(report);
                saveContext.saving(incomingTemplate);
                savedTemplates.add(incomingTemplate);
            }
        }

        ReportTemplate effectiveDefaultTemplate = incomingDefaultTemplate;
        for (ReportTemplate savedTemplate : savedTemplates) {
            if (savedTemplate.equals(incomingDefaultTemplate)) {
                effectiveDefaultTemplate = savedTemplate;
                break;
            }
        }
        return effectiveDefaultTemplate;
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
}