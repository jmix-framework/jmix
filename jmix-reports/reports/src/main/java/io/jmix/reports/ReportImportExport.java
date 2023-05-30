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

package io.jmix.reports;

import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportImportOption;
import io.jmix.reports.entity.ReportImportResult;

import org.springframework.lang.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;

/**
 * Provides methods to import and export reports
 */
public interface ReportImportExport {

    /**
     * Exports all reports and their templates into one zip archive. Each report is exported into a separate zip
     * archive with 2 files (report.xml and a template file (for example MyReport.doc)).
     * For example:
     * return byte[] (bytes of zip arhive)
     * -- MegaReport.zip
     * ---- report.xml
     * ---- Mega report.xls
     * -- Other report.zip
     * ---- report.xml
     * ---- other report.odt
     *
     * @param reports Collection of Report objects to be exported.
     * @return ZIP byte array with zip archives inside.
     */
    byte[] exportReports(Collection<Report> reports);

    /**
     * Imports reports from ZIP archive. Archive file format is described in exportReports method.
     *
     * @param zipBytes ZIP archive as a byte array.
     * @return Collection of imported reports.
     */
    Collection<Report> importReports(byte[] zipBytes);

    /**
     * Imports reports from ZIP archive. Archive file format is described in exportReports method.
     *
     * @param zipBytes      ZIP archive as a byte array.
     * @param importOptions - report import options
     * @return Collection of imported reports.
     */
    Collection<Report> importReports(byte[] zipBytes, @Nullable EnumSet<ReportImportOption> importOptions);

    /**
     * Imports reports from ZIP archive(s) by path.
     * Path can point to the ZIP file directly or to some directory with ZIP files.
     * Archive file format is the same as produced by exportReports.
     *
     * @param path path to archive file or directory
     * @return Collection of imported reports.
     */
    Collection<Report> importReports(String path);

    /**
     * Imports reports from ZIP archive(s) by path.
     * Path can point to the archive file directly or to some directory with archive files.
     * Archive file format is the same as produced by exportReports.
     *
     * @param path          path to archive file or directory
     * @param importOptions report import options
     * @return Collection of imported reports.
     */
    Collection<Report> importReports(String path, @Nullable EnumSet<ReportImportOption> importOptions);

    /**
     * Imports reports from ZIP archive. Archive file format is described in exportReports method.
     *
     * @param zipBytes      ZIP archive as a byte array.
     * @param importOptions report - import options
     * @return import result - collection of updated, created reports
     */
    ReportImportResult importReportsWithResult(byte[] zipBytes, @Nullable EnumSet<ReportImportOption> importOptions);

    /**
     * Import all reports from the specified folder.
     * Folder should have the following structure, in other cases RuntimeException will be thrown
     * <p>
     * folder
     * sub-folder1
     * report.structure
     * template.doc
     * sub-folder2
     * report.structure
     * template.docx
     *
     * @param path to folder with reports
     * @return collection of imported reports
     * @throws IOException if any I/O error occurs
     */
    Collection<Report> importReportsFromPath(String path) throws IOException;
}