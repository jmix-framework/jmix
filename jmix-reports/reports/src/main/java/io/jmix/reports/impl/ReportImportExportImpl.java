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
import io.jmix.reports.ReportImportExport;
import io.jmix.reports.ReportsPersistence;
import io.jmix.reports.ReportsSerialization;
import io.jmix.reports.converter.XStreamConverter;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportImportOption;
import io.jmix.reports.entity.ReportImportResult;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.exception.ReportingException;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.CRC32;

@Component("report_ReportImportExport")
public class ReportImportExportImpl implements ReportImportExport {
    public static final String ENCODING = "CP866";

    private static final Logger log = LoggerFactory.getLogger(ReportImportExportImpl.class);

    @Autowired
    protected ReportsPersistence reportsPersistence;

    @Autowired
    protected ReportsSerialization reportsSerialization;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected Metadata metadata;

    @Override
    public byte[] exportReports(Collection<Report> reports) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        ZipArchiveOutputStream zipOutputStream = new ZipArchiveOutputStream(byteArrayOutputStream);
        try {
            zipOutputStream.setMethod(ZipArchiveOutputStream.STORED);
            zipOutputStream.setEncoding(ENCODING);
            for (Report report : reports) {
                try {
                    byte[] reportBytes = exportReport(report);
                    ArchiveEntry singleReportEntry = newStoredEntry(replaceForbiddenCharacters(report.getName()) + ".zip", reportBytes);
                    zipOutputStream.putArchiveEntry(singleReportEntry);
                    zipOutputStream.write(reportBytes);
                    zipOutputStream.closeArchiveEntry();
                } catch (IOException e) {
                    throw new ReportingException(String.format("Exception occurred while exporting report [%s]", report.getName()), e);
                }
            }
        } finally {
            IOUtils.closeQuietly(zipOutputStream);
        }

        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public Collection<Report> importReports(byte[] zipBytes) {
        return importReports(zipBytes, null);
    }

    @Override
    public Collection<Report> importReports(byte[] zipBytes, @Nullable EnumSet<ReportImportOption> importOptions) {
        ReportImportResult importResult = importReportsWithResult(zipBytes, importOptions);
        return new ArrayList<>(importResult.getImportedReports());
    }

    @Override
    public Collection<Report> importReports(String path) {
        return importReports(path, null);
    }

    @Override
    public Collection<Report> importReports(String path, @Nullable EnumSet<ReportImportOption> importOptions) {
        File target = new File(path);
        if(target.isFile()) {
            byte[] zipBytes = readFileToByteArray(target);
            return importReports(zipBytes, importOptions);
        }

        if(target.isDirectory()) {
            File[] files = target.listFiles();
            if(files != null && files.length != 0) {
                Collection<Report> result = new ArrayList<>();
                for(File file : files) {
                    if(file.isFile()) {
                        byte[] zipBytes = readFileToByteArray(file);
                        result.addAll(importReports(zipBytes, importOptions));
                    }
                }
                return result;
            }
        }

        return Collections.emptyList();
    }

    public ReportImportResult importReportsWithResult(byte[] zipBytes, @Nullable EnumSet<ReportImportOption> importOptions) {
        log.info("Import started...");
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zipBytes);
        ReportImportResult importResult = new ReportImportResult();

        try (ZipArchiveInputStream archiveReader = new ZipArchiveInputStream(byteArrayInputStream)) {
            while (archiveReader.getNextZipEntry() != null) {
                final byte[] buffer = readBytesFromEntry(archiveReader);
                importReport(buffer, importOptions, importResult);
            }
        } catch (IOException e) {
            importResult.addException(e);
        }

        log.info("Import successfully completed. Created reports {}, updated {}.",
                importResult.getCreatedReports().size(), importResult.getUpdatedReports().size());
        return importResult;
    }

    @Override
    public Collection<Report> importReportsFromPath(String path) throws IOException {
        File directory = new File(path);
        if (directory.exists() && directory.isDirectory()) {
            File[] subDirectories = directory.listFiles();
            if (subDirectories != null) {
                Map<String, Object> map = new HashMap<>();

                for (File subDirectory : subDirectories) {
                    if (subDirectory.isDirectory()) {
                        if (!subDirectory.getName().startsWith(".")) {
                            File[] files = subDirectory.listFiles();
                            if (files != null) {
                                byte[] bytes = zipSingleReportFiles(files);
                                String name = replaceForbiddenCharacters(subDirectory.getName()) + ".zip";
                                map.put(name, bytes);
                            }
                        }
                    } else {
                        throw new ReportingException("Report deployment failed. Root folder should have special structure.");
                    }
                }
                return importReports(zipContent(map));
            }
        }

        return Collections.emptyList();
    }

    /**
     * Exports single report to ZIP archive with name {@code <report name>.zip}.
     * There are 2 files in archive: report.structure and a template file (odt, xls or other..)
     *
     * @param report Report object that must be exported.
     * @return ZIP archive as a byte array.
     * @throws IOException if any I/O error occurs
     */
    protected byte[] exportReport(Report report) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipArchiveOutputStream zipOutputStream = new ZipArchiveOutputStream(byteArrayOutputStream);
        zipOutputStream.setMethod(ZipArchiveOutputStream.STORED);
        zipOutputStream.setEncoding(ENCODING);

        report = reloadReport(report);

        if (report != null) {
            String xml = report.getXml();
            byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_8);
            ArchiveEntry zipEntryReportObject = newStoredEntry("report.structure", xmlBytes);
            zipOutputStream.putArchiveEntry(zipEntryReportObject);
            zipOutputStream.write(xmlBytes);

            Report xmlReport = reportsSerialization.convertToReport(xml);
            if (report.getTemplates() != null && xmlReport.getTemplates() != null) {
                for (int i = 0; i < report.getTemplates().size(); i++) {
                    ReportTemplate xmlTemplate = xmlReport.getTemplates().get(i);
                    ReportTemplate template = null;
                    for (ReportTemplate it : report.getTemplates()) {
                        if (xmlTemplate.equals(it)) {
                            template = it;
                            break;
                        }
                    }
                    if (template != null && template.getContent() != null) {
                        byte[] fileBytes = template.getContent();
                        ArchiveEntry zipEntryTemplate = newStoredEntry(
                                "templates/" + i + "/" + template.getName(), fileBytes);
                        zipOutputStream.putArchiveEntry(zipEntryTemplate);
                        zipOutputStream.write(fileBytes);
                    }
                }
            }
        }

        zipOutputStream.closeArchiveEntry();
        zipOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }


    protected void importReport(byte[] zipBytes, @Nullable EnumSet<ReportImportOption> importOptions, ReportImportResult importResult) throws IOException {
        Report report = fromByteArray(zipBytes);

        if (report == null) {
            importResult.addException(new ReportingException("Unable to convert data from archive to report"));
            return;
        }

        updateReportTemplate(report, zipBytes);
        withReportOptions(report, importOptions);

        Optional<Report> existingReport = dataManager
                .load(Report.class)
                .id(report.getId())
                .fetchPlan(FetchPlan.INSTANCE_NAME)
                .optional();

        report = reportsPersistence.save(report);
        importResult.addImportedReport(report);
        if (existingReport.isPresent()) {
            importResult.addUpdatedReport(report);
            log.info("Existing report {} updated", report);
        } else {
            importResult.addCreatedReport(report);
            log.info("New report {} imported", report);
        }
    }

    @Nullable
    protected Report fromByteArray(byte[] zipBytes) throws IOException {
        Report report = null;

        try (ZipArchiveInputStream archiveReader = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipArchiveEntry archiveEntry;
            // importing report.xml to report object
            while (((archiveEntry = archiveReader.getNextZipEntry()) != null) && (report == null)) {
                if (isReportsStructureFile(archiveEntry.getName())) {
                    String xml = new String(readBytesFromEntry(archiveReader), StandardCharsets.UTF_8);

                    if (xml.startsWith("<")) {//previous xml structure version
                        XStreamConverter xStreamConverter = new XStreamConverter();
                        report = xStreamConverter.convertToReport(xml);
                    } else {//current json structure
                        report = reportsSerialization.convertToReport(xml);
                    }
                    report.setXml(xml);
                }
            }
        }
        return report;
    }

    protected byte[] readFileToByteArray(File file) {
        try {
            return FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            throw new ReportingException("Unable to read file '" + file.getName() + "'");
        }
    }

    protected void updateReportTemplate(Report report, byte[] zipBytes) throws IOException {
        // importing template files
        // not using zipInputStream.reset here because marks not supported.

        try (ZipArchiveInputStream archiveReader = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipArchiveEntry archiveEntry;
            if (report.getTemplates() != null) {
                // unpack templates
                int i = 0;
                while ((archiveEntry = archiveReader.getNextZipEntry()) != null
                        && (i < report.getTemplates().size())) {

                    if (!isReportsStructureFile(archiveEntry.getName())
                            && !archiveEntry.isDirectory()) {
                        String[] namePaths = archiveEntry.getName().split("/");
                        int index = Integer.parseInt(namePaths[1]);

                        if (index >= 0) {
                            ReportTemplate template = report.getTemplates().get(index);
                            template.setContent(readBytesFromEntry(archiveReader));
                            if (StringUtils.isBlank(template.getName())) {
                                template.setName(namePaths[2]);
                            }
                        }
                        i++;
                    }
                }
            }
        }
    }

    protected void withReportOptions(Report report, @Nullable EnumSet<ReportImportOption> importOptions) {
        if (importOptions != null) {
            for (ReportImportOption option : importOptions) {
                if (ReportImportOption.DO_NOT_IMPORT_ROLES == option) {
                    Report dbReport = null;
                    try {
                        dbReport = reloadReport(report);
                    } catch (EntityAccessException e) {
                        //Do nothing
                    }
                    if (dbReport != null) {
                        report.setReportRoles(dbReport.getReportRoles());
                    } else {
                        report.setReportRoles(Collections.emptySet());
                    }
                    report.setXml(reportsSerialization.convertToString(report));
                }
            }
        }
    }

    protected byte[] zipSingleReportFiles(File[] files) throws IOException {
        Map<String, Object> map = new HashMap<>();
        int templatesCount = 0;
        for (File file : files) {
            if (!file.isDirectory()) {
                byte[] data = FileUtils.readFileToByteArray(file);
                String name;
                if (isReportsStructureFile(file.getName())) {
                    name = file.getName();
                } else {
                    name = "templates/" + templatesCount++ + "/" + file.getName();
                }

                map.put(name, data);
            }
        }

        return zipContent(map);
    }

    protected byte[] zipContent(Map<String, Object> stringObjectMap) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipArchiveOutputStream zipOutputStream = new ZipArchiveOutputStream(byteArrayOutputStream);
        zipOutputStream.setMethod(ZipArchiveOutputStream.STORED);
        zipOutputStream.setEncoding(ENCODING);

        for (Map.Entry<String, Object> entry : stringObjectMap.entrySet()) {
            byte[] data = (byte[]) entry.getValue();
            ArchiveEntry archiveEntry = newStoredEntry(entry.getKey(), data);
            zipOutputStream.putArchiveEntry(archiveEntry);
            zipOutputStream.write(data);
            zipOutputStream.closeArchiveEntry();
        }

        zipOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    protected ArchiveEntry newStoredEntry(String name, byte[] data) {
        ZipArchiveEntry zipEntry = new ZipArchiveEntry(name);
        zipEntry.setSize(data.length);
        zipEntry.setCompressedSize(zipEntry.getSize());
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        zipEntry.setCrc(crc32.getValue());
        return zipEntry;
    }

    protected String replaceForbiddenCharacters(String fileName) {
        return fileName.replaceAll("[\\,/,:,\\*,\",<,>,\\|]", "");
    }

    protected byte[] readBytesFromEntry(ZipArchiveInputStream archiveReader) throws IOException {
        return IOUtils.toByteArray(archiveReader);
    }

    @Nullable
    protected Report reloadReport(Report report) {
        return dataManager.load(Id.of(report))
                .fetchPlan("report.edit")
                .optional()
                .orElse(null);
    }

    protected boolean isReportsStructureFile(String name) {
        return name.equals("report.xml") || name.equals("report.structure");
    }
}