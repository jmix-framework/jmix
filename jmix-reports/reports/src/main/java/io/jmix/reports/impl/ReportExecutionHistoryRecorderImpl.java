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
import com.haulmont.yarg.reporting.ReportOutputDocument;
import com.haulmont.yarg.structure.ReportOutputType;
import io.jmix.core.*;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.data.PersistenceHints;
import io.jmix.reports.ReportExecutionHistoryRecorder;
import io.jmix.reports.ReportsProperties;
import io.jmix.reports.entity.JmixReportOutputType;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportExecution;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

@Component("report_ExecutionHistoryRecorder")
public class ReportExecutionHistoryRecorderImpl implements ReportExecutionHistoryRecorder {
    private static Logger log = LoggerFactory.getLogger(ReportExecutionHistoryRecorderImpl.class);

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

    protected FileStorage fileStorage;

    @Override
    public ReportExecution startExecution(Report report, Map<String, Object> params) {
        ReportExecution execution = metadata.create(ReportExecution.class);

        execution.setReport(report);
        execution.setReportName(report.getName());
        execution.setReportCode(report.getCode());
        execution.setUsername(currentAuthentication.getUser().getUsername());
        execution.setStartTime(timeSource.currentTimestamp());
        setParametersString(execution, params);
        handleNewReportEntity(execution);

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

    private void handleNewReportEntity(ReportExecution entity) {
        Report report = entity.getReport();

        // handle case when user runs report that isn't saved yet from Report Editor
        if (entityStates.isNew(report)) {
            Report reloaded = dataManager.load(Id.of(report))
                    .fetchPlan(FetchPlan.INSTANCE_NAME)
                    .optional()
                    .orElse(null);
            entity.setReport(reloaded);
        }
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
        deleted += deleteHistoryGroupedByReport();

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

    private int deleteHistoryGroupedByReport() {
        int maxItemsPerReport = reportsProperties.getHistoryCleanupMaxItemsPerReport();
        if (maxItemsPerReport <= 0) {
            return 0;
        }

        List<UUID> allReportIds = dataManager.loadValues("select r.id from report_Report r")
                .properties("id")
                .list()
                .stream()
                .map(kve -> (UUID) kve.getValue("id"))
                .collect(Collectors.toList());

        log.debug("Deleting report executions for every report, older than {}th execution", maxItemsPerReport);
        int total = 0;
        for (UUID reportId : allReportIds) {
            int deleted = deleteForOneReport(reportId, maxItemsPerReport);
            total += deleted;
        }
        return total;
    }

    private int deleteForOneReport(UUID reportId, int maxItemsPerReport) {
        List<FileRef> fileRefs = new ArrayList<>();
        Integer deleted = transaction.execute(status -> {
            try {
                entityManager.setProperty(PersistenceHints.SOFT_DELETION, false);
                int rows = 0;
                List<Date> datesList = entityManager.createQuery(
                        "select e.startTime from report_ReportExecution e"
                                + " where e.report.id = :reportId"
                                + " order by e.startTime desc", Date.class)
                        .setParameter("reportId", reportId)
                        .setFirstResult(maxItemsPerReport)
                        .setMaxResults(1)
                        .getResultList();

                Date borderStartTime = CollectionUtils.isNotEmpty(datesList) ? datesList.get(0) : null;

                if (borderStartTime != null) {
                    List<FileRef> fileRefs1 = entityManager.createQuery("select e.outputDocument from report_ReportExecution e"
                            + " where e.outputDocument is not null and e.report.id = :reportId and e.startTime <= :borderTime", FileRef.class)
                            .setParameter("reportId", reportId)
                            .setParameter("borderTime", borderStartTime)
                            .getResultList();
                    fileRefs.addAll(fileRefs1);

                    rows = entityManager.createQuery("delete from report_ReportExecution e"
                            + " where e.report.id = :reportId and e.startTime <= :borderTime")
                            .setParameter("reportId", reportId)
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
