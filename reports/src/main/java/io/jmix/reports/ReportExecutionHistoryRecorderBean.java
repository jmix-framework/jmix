/*
 * Copyright (c) 2008-2019 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.reports;

import com.google.common.collect.Sets;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.app.FileStorageAPI;
import io.jmix.core.JmixEntity;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.entity.contracts.Id;
import com.haulmont.cuba.core.global.*;
import io.jmix.core.TimeSource;
import io.jmix.reports.entity.CubaReportOutputType;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportExecution;
import com.haulmont.yarg.reporting.ReportOutputDocument;
import com.haulmont.yarg.structure.ReportOutputType;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;
import java.util.stream.Collectors;

@Component(ReportExecutionHistoryRecorder.NAME)
public class ReportExecutionHistoryRecorderBean implements ReportExecutionHistoryRecorder {
    private static Logger log = LoggerFactory.getLogger(ReportExecutionHistoryRecorderBean.class);

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected UserSessionSource userSessionSource;
    @Autowired
    protected TimeSource timeSource;
    //TODO Server info API
//    @Autowired
//    protected ServerInfoAPI serverInfoAPI;
    @Autowired
    protected ReportingConfig reportingConfig;
    @Autowired
    protected Persistence persistence;
    @Autowired
    protected FileStorageAPI fileStorageAPI;
    @Autowired
    protected Authentication authentication;

    @Override
    public ReportExecution startExecution(Report report, Map<String, Object> params) {
        ReportExecution execution = metadata.create(ReportExecution.class);

        execution.setReport(report);
        execution.setReportName(report.getName());
        execution.setReportCode(report.getCode());
        execution.setUser(userSessionSource.getUserSession().getUser());
        execution.setStartTime(timeSource.currentTimestamp());
        //TODO server info api
//        execution.setServerId(serverInfoAPI.getServerId());
        setParametersString(execution, params);
        handleNewReportEntity(execution);

        execution = dataManager.commit(execution);
        return execution;
    }

    @Override
    public void markAsSuccess(ReportExecution execution, ReportOutputDocument document) {
        handleSessionExpired(() -> {
            execution.setSuccess(true);
            execution.setFinishTime(timeSource.currentTimestamp());
            if (shouldSaveDocument(execution, document)) {
                try {
                    FileDescriptor documentFile = saveDocument(document);
                    execution.setOutputDocument(documentFile);
                } catch (FileStorageException e) {
                    log.error("Failed to save output document", e);
                }
            }
            dataManager.commit(execution);
        });
    }

    @Override
    public void markAsCancelled(ReportExecution execution) {
        handleSessionExpired(() -> {
            execution.setCancelled(true);
            execution.setFinishTime(timeSource.currentTimestamp());
            dataManager.commit(execution);
        });
    }

    @Override
    public void markAsError(ReportExecution execution, Exception e) {
        handleSessionExpired(() -> {
            execution.setSuccess(false);
            execution.setFinishTime(timeSource.currentTimestamp());
            execution.setErrorMessage(e.getMessage());

            dataManager.commit(execution);
        });
    }

    protected void setParametersString(ReportExecution reportExecution, Map<String, Object> params) {
        if (params.size() <= 0) {
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            Object value = (entry.getValue() instanceof JmixEntity)
                    ? String.format("%s-%s", metadata.getClass(entry.getValue().getClass()), Id.of((JmixEntity)entry.getValue()).getValue())
                    : entry.getValue();
            builder.append(String.format("key: %s, value: %s", entry.getKey(), value)).append("\n");
        }
        reportExecution.setParams(builder.toString());
    }

    private void handleNewReportEntity(ReportExecution entity) {
        Report report = entity.getReport();

        // handle case when user runs report that isn't saved yet from Report Editor
        if (PersistenceHelper.isNew(report)) {
            Report reloaded = dataManager.load(Id.of(report))
                    .view(View.MINIMAL)
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
                CubaReportOutputType.chart.getId(),
                CubaReportOutputType.table.getId(),
                CubaReportOutputType.pivot.getId());
        return reportingConfig.isSaveOutputDocumentsToHistory() && !outputTypesWithoutDocument.contains(type.getId());
    }

    protected FileDescriptor saveDocument(ReportOutputDocument document) throws FileStorageException {
        String ext = FilenameUtils.getExtension(document.getDocumentName());
        FileDescriptor file = metadata.create(FileDescriptor.class);
        file.setCreateDate(timeSource.currentTimestamp());
        file.setName(document.getDocumentName());
        file.setExtension(ext);
        file.setSize((long) document.getContent().length);

        fileStorageAPI.saveFile(file, document.getContent());
        file = dataManager.commit(file);
        return file;
    }

    /**
     * It is not rare for large reports to execute longer than {@link ServerConfig#getUserSessionExpirationTimeoutSec()}.
     * In this case when report is finished - user session is already expired and can't be used to make changes to database.
     */
    protected void handleSessionExpired(Runnable action) {
        boolean userSessionIsValid = userSessionSource.checkCurrentUserSession();
        if (userSessionIsValid) {
            action.run();
        } else {
            log.debug("No valid user session, record history under system user");
            //TODO with system user
//            authentication.withSystemUser(() -> {
//                action.run();
//                return null;
//            });
        }
    }

    @Override
    public String cleanupHistory() {
        int deleted = 0;

        deleted += deleteHistoryByDays();
        deleted += deleteHistoryGroupedByReport();

        return deleted > 0 ? String.valueOf(deleted) : null;
    }

    private int deleteHistoryByDays() {
        int historyCleanupMaxDays = reportingConfig.getHistoryCleanupMaxDays();
        if (historyCleanupMaxDays <= 0) {
            return 0;
        }

        Date borderDate = DateUtils.addDays(timeSource.currentTimestamp(), -historyCleanupMaxDays);
        log.debug("Deleting report executions older than {}", borderDate);

        List<UUID> fileIds = new ArrayList<>();

        int deleted = persistence.callInTransaction(em -> {
            List<UUID> ids = em.createQuery("select e.outputDocument.id from report$ReportExecution e"
                    + " where e.outputDocument is not null and e.startTime < :borderDate", UUID.class)
                    .setParameter("borderDate", borderDate)
                    .getResultList();
            fileIds.addAll(ids);

            em.setSoftDeletion(false);
            return em.createQuery("delete from report$ReportExecution e where e.startTime < :borderDate")
                    .setParameter("borderDate", borderDate)
                    .executeUpdate();
        });

        deleteFileDescriptorsAndFiles(fileIds);
        return deleted;
    }

    private void deleteFileDescriptorsAndFiles(List<UUID> fileIds) {
        if (!fileIds.isEmpty()) {
            log.debug("Deleting {} output document files", fileIds.size());
        }

        for (UUID fileId : fileIds) {
            FileDescriptor file = dataManager.load(FileDescriptor.class)
                    .id(fileId)
                    .optional()
                    .orElse(null);

            if (file != null) {
                try {
                    fileStorageAPI.removeFile(file);
                } catch (FileStorageException e) {
                    log.error("Failed to remove document from storage " + file, e);
                }
                dataManager.remove(file);
            }
        }
    }

    private int deleteHistoryGroupedByReport() {
        int maxItemsPerReport = reportingConfig.getHistoryCleanupMaxItemsPerReport();
        if (maxItemsPerReport <= 0) {
            return 0;
        }

        List<UUID> allReportIds = dataManager.loadValues("select r.id from report$Report r")
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
        List<UUID> fileIds = new ArrayList<>();
        int deleted = persistence.callInTransaction(em -> {
            em.setSoftDeletion(false);
            int rows = 0;
            Date borderStartTime = em.createQuery(
                    "select e.startTime from report$ReportExecution e"
                            + " where e.report.id = :reportId"
                            + " order by e.startTime desc", Date.class)
                    .setParameter("reportId", reportId)
                    .setFirstResult(maxItemsPerReport)
                    .setMaxResults(1)
                    .getFirstResult();

            if (borderStartTime != null) {
                List<UUID> ids = em.createQuery("select e.outputDocument.id from report$ReportExecution e"
                        + " where e.outputDocument is not null and e.report.id = :reportId and e.startTime <= :borderTime", UUID.class)
                        .setParameter("reportId", reportId)
                        .setParameter("borderTime", borderStartTime)
                        .getResultList();
                fileIds.addAll(ids);

                rows = em.createQuery("delete from report$ReportExecution e"
                        + " where e.report.id = :reportId and e.startTime <= :borderTime")
                        .setParameter("reportId", reportId)
                        .setParameter("borderTime", borderStartTime)
                        .executeUpdate();
            }
            return rows;
        });
        deleteFileDescriptorsAndFiles(fileIds);
        return deleted;
    }
}
