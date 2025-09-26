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

import com.google.common.collect.Sets;
import io.jmix.core.*;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.data.PersistenceHints;
import io.jmix.reports.ReportExecutionHistoryRecorder;
import io.jmix.reports.ReportRepository;
import io.jmix.reports.ReportsProperties;
import io.jmix.reports.entity.JmixReportOutputType;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportExecution;
import io.jmix.reports.entity.ReportSource;
import io.jmix.reports.yarg.reporting.ReportOutputDocument;
import io.jmix.reports.yarg.structure.ReportOutputType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.ByteArrayInputStream;
import java.util.*;

@Component("report_ExecutionHistoryRecorder")
public class ReportExecutionHistoryRecorderImpl implements ReportExecutionHistoryRecorder {
    private static final Logger log = LoggerFactory.getLogger(ReportExecutionHistoryRecorderImpl.class);

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected CurrentAuthentication currentAuthentication;
    @Autowired
    protected TimeSource timeSource;
    @Autowired
    protected ReportsProperties reportsProperties;
    @Autowired
    protected TransactionTemplate transaction;
    @PersistenceContext
    protected EntityManager entityManager;
    @Autowired
    protected FileStorageLocator fileStorageLocator;
    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected SystemAuthenticator systemAuthenticator;
    @Autowired
    protected ReportRepository reportRepository;

    protected FileStorage fileStorage;

    @Override
    public ReportExecution startExecution(Report report, Map<String, Object> params) {
        ReportExecution execution = metadata.create(ReportExecution.class);

        execution.setReportName(report.getName());
        execution.setReportCode(report.getCode());
        execution.setUsername(currentAuthentication.getUser().getUsername());
        execution.setStartTime(timeSource.currentTimestamp());
        setParametersString(execution, params);
        assignLinkToReportEntity(execution, report);

        execution = dataManager.save(execution);
        return execution;
    }

    @Override
    public void markAsSuccess(ReportExecution execution, ReportOutputDocument document) {
        handleSessionExpired(() -> {
            execution.setSuccess(true);
            execution.setFinishTime(timeSource.currentTimestamp());
            if (shouldSaveDocument(execution, document)) {
                try {
                    FileRef reference = saveDocument(document);
                    execution.setOutputDocument(reference);
                } catch (FileStorageException e) {
                    log.error("Failed to save output document", e);
                }
            }
            dataManager.save(execution);
        });
    }

    @Override
    public void markAsCancelled(ReportExecution execution) {
        handleSessionExpired(() -> {
            execution.setCancelled(true);
            execution.setFinishTime(timeSource.currentTimestamp());
            dataManager.save(execution);
        });
    }

    @Override
    public void markAsError(ReportExecution execution, Exception e) {
        handleSessionExpired(() -> {
            execution.setSuccess(false);
            execution.setFinishTime(timeSource.currentTimestamp());
            execution.setErrorMessage(e.getMessage());

            dataManager.save(execution);
        });
    }

    protected void setParametersString(ReportExecution reportExecution, Map<String, Object> params) {
        if (params.size() <= 0) {
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            Object value = (entry.getValue() instanceof Entity)
                    ? String.format("%s-%s", metadata.getClass(entry.getValue()), Id.of((Entity) entry.getValue()).getValue())
                    : entry.getValue();
            builder.append(String.format("key: %s, value: %s", entry.getKey(), value)).append("\n");
        }
        reportExecution.setParams(builder.toString());
    }

    private void assignLinkToReportEntity(ReportExecution entity, Report report) {
        if (report.getSource() == ReportSource.ANNOTATED_CLASS) {
            return;
        }
        // reload the entity to avoid problems with cascade persisting of possibly not yet saved entities
        // when user runs report that isn't saved yet from Report Editor
        Report reloaded = dataManager.load(Id.of(report))
                .fetchPlan(FetchPlan.INSTANCE_NAME)
                .optional()
                .orElse(null);
        entity.setReport(reloaded);
    }

    // can be used as extension point
    @SuppressWarnings("unused")
    protected boolean shouldSaveDocument(ReportExecution execution, ReportOutputDocument document) {
        ReportOutputType type = document.getReportOutputType();
        Set<String> outputTypesWithoutDocument = Sets.newHashSet(
                JmixReportOutputType.chart.getId(),
                JmixReportOutputType.table.getId(),
                JmixReportOutputType.pivot.getId());
        return reportsProperties.isSaveOutputDocumentsToHistory() && !outputTypesWithoutDocument.contains(type.getId());
    }

    protected FileRef saveDocument(ReportOutputDocument document) throws FileStorageException {
        return getFileStorage().saveStream(document.getDocumentName(), new ByteArrayInputStream(document.getContent()));
    }

    /**
     * It is not rare for large reports to execute for a long time.
     * In this case when report is finished - user session is already expired and can't be used to make changes to database.
     *
     * @param action action for the execution
     */
    protected void handleSessionExpired(Runnable action) {
        boolean userSessionIsValid = currentAuthentication.isSet();
        if (userSessionIsValid) {
            action.run();
        } else {
            log.debug("No valid user session, record history under system user");
            systemAuthenticator.runWithSystem(action);
        }
    }

    @Override
    public String cleanupHistory() {
        int deleted = 0;

        deleted += deleteHistoryByDays();
        deleted += deleteHistoryGroupedByAnnotatedReport();
        deleted += deleteHistoryGroupedByDatabaseReport();

        return deleted > 0 ? String.valueOf(deleted) : StringUtils.EMPTY;
    }

    private int deleteHistoryByDays() {
        int historyCleanupMaxDays = reportsProperties.getHistoryCleanupMaxDays();
        if (historyCleanupMaxDays <= 0) {
            return 0;
        }

        Date borderDate = DateUtils.addDays(timeSource.currentTimestamp(), -historyCleanupMaxDays);
        log.debug("Deleting report executions older than {}", borderDate);

        List<FileRef> fileRefs = new ArrayList<>();

        Integer deleted = transaction.execute(status -> {
            try {
                List<FileRef> fileRefs1 = entityManager.createQuery("select e.outputDocument from report_ReportExecution e"
                        + " where e.outputDocument is not null and e.startTime < :borderDate", FileRef.class)
                        .setParameter("borderDate", borderDate)
                        .getResultList();
                fileRefs.addAll(fileRefs1);

                entityManager.setProperty(PersistenceHints.SOFT_DELETION, false);
                return entityManager.createQuery("delete from report_ReportExecution e where e.startTime < :borderDate")
                        .setParameter("borderDate", borderDate)
                        .executeUpdate();
            } finally {
                entityManager.setProperty(PersistenceHints.SOFT_DELETION, true);
            }
        });

        deleteFiles(fileRefs);
        return Optional.ofNullable(deleted).orElse(0);
    }

    private void deleteFiles(List<FileRef> fileRefs) {
        if (!fileRefs.isEmpty()) {
            log.debug("Deleting {} output document files", fileRefs.size());
        }

        for (FileRef path : fileRefs) {
            try {
                getFileStorage().removeFile(path);
            } catch (FileStorageException e) {
                log.error("Failed to remove document from storage " + path, e);
            }
        }
    }

    private int deleteHistoryGroupedByAnnotatedReport() {
        int maxItemsPerReport = reportsProperties.getHistoryCleanupMaxItemsPerReport();
        if (maxItemsPerReport <= 0) {
            return 0;
        }

        List<String> annotatedReportCodes = reportRepository.getAllReports()
                .stream()
                .filter(r -> r.getSource() == ReportSource.ANNOTATED_CLASS && r.getCode() != null)
                .map(Report::getCode)
                .toList();

        log.debug("Deleting annotated reports' executions for every report, older than {}th execution", maxItemsPerReport);
        int total = 0;
        for (String reportCode : annotatedReportCodes) {
            int deleted = deleteForOneReport(maxItemsPerReport,
                    "e.report is null and e.reportCode = :reportCode", "reportCode", reportCode);
            total += deleted;
        }
        return total;
    }

    private int deleteHistoryGroupedByDatabaseReport() {
        int maxItemsPerReport = reportsProperties.getHistoryCleanupMaxItemsPerReport();
        if (maxItemsPerReport <= 0) {
            return 0;
        }

        List<UUID> allReportIds = dataManager.loadValues("select r.id from report_Report r")
                .properties("id")
                .list()
                .stream()
                .map(kve -> (UUID) kve.getValue("id"))
                .toList();

        log.debug("Deleting runtime reports' executions for every report, older than {}th execution", maxItemsPerReport);
        int total = 0;
        for (UUID reportId : allReportIds) {
            int deleted = deleteForOneReport(maxItemsPerReport,
                    "e.report.id = :reportId", "reportId", reportId);
            total += deleted;
        }
        return total;
    }

    private int deleteForOneReport(int maxItemsPerReport, String whereCondition,
                                   String parameterName, Object parameterValue) {
        List<FileRef> fileRefs = new ArrayList<>();
        Integer deleted = transaction.execute(status -> {
            try {
                entityManager.setProperty(PersistenceHints.SOFT_DELETION, false);
                int rows = 0;
                List<Date> datesList = entityManager.createQuery(
                        "select e.startTime from report_ReportExecution e"
                                + " where"
                                + " " + whereCondition
                                + " order by e.startTime desc", Date.class)
                        .setParameter(parameterName, parameterValue)
                        .setFirstResult(maxItemsPerReport)
                        .setMaxResults(1)
                        .getResultList();

                Date borderStartTime = CollectionUtils.isNotEmpty(datesList) ? datesList.get(0) : null;

                if (borderStartTime != null) {
                    List<FileRef> fileRefs1 = entityManager.createQuery("select e.outputDocument from report_ReportExecution e"
                            + " where e.outputDocument is not null"
                            + " and " + whereCondition
                            + " and e.startTime <= :borderTime", FileRef.class)
                            .setParameter(parameterName, parameterValue)
                            .setParameter("borderTime", borderStartTime)
                            .getResultList();
                    fileRefs.addAll(fileRefs1);

                    rows = entityManager.createQuery("delete from report_ReportExecution e"
                            + " where"
                            + " " + whereCondition
                            + " and e.startTime <= :borderTime")
                            .setParameter(parameterName, parameterValue)
                            .setParameter("borderTime", borderStartTime)
                            .executeUpdate();
                }

                return rows;
            } finally {
                entityManager.setProperty(PersistenceHints.SOFT_DELETION, true);
            }
        });
        deleteFiles(fileRefs);
        return Optional.ofNullable(deleted).orElse(0);
    }

    protected FileStorage getFileStorage() {
        if (fileStorage == null) {
            fileStorage = fileStorageLocator.getDefault();
        }
        return fileStorage;
    }
}
