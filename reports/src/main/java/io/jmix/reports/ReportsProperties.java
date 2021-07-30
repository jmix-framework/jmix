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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Reporting configuration interface.
 */
@ConfigurationProperties(prefix = "jmix.reports")
@ConstructorBinding
public class ReportsProperties {

    String officePath;

    List<Integer> officePorts;

    int docFormatterTimeout;

    boolean displayDeviceAvailable;

    String pdfFontsDirectory;

    boolean putEmptyRowIfNoDataSelected;

    int parameterPrototypeQueryLimit;

    List<String> wizardEntitiesBlackList;

    List<String> wizardEntitiesWhiteList;

    List<String> wizardPropertiesBlackList;

    List<String> wizardPropertiesExcludedBlackList;

    int entityTreeModelMaxDepth;

    int htmlExternalResourcesTimeoutSec;

    String curlPath;

    String curlParams;

    int curlTimeout;

    boolean historyRecordingEnabled;

    boolean saveOutputDocumentsToHistory;

    int historyCleanupMaxDays;

    int historyCleanupMaxItemsPerReport;

    int countOfRetry;

    boolean useOfficeForDocumentConversion;

    public ReportsProperties(@DefaultValue("/") String officePath,
                             @DefaultValue({"8100", "8101", "8102", "8103"}) List<Integer> officePorts,
                             @DefaultValue("20") int docFormatterTimeout,
                             @DefaultValue("false") boolean displayDeviceAvailable,
                             String pdfFontsDirectory,
                             @DefaultValue("true") boolean putEmptyRowIfNoDataSelected,
                             @DefaultValue("1000") int parameterPrototypeQueryLimit,
                             @Nullable List<String> wizardEntitiesBlackList,
                             @Nullable List<String> wizardEntitiesWhiteList,
                             @Nullable List<String> wizardPropertiesBlackList,
                             @Nullable List<String> wizardPropertiesExcludedBlackList,
                             @DefaultValue("3") int entityTreeModelMaxDepth,
                             @DefaultValue("5") int htmlExternalResourcesTimeoutSec,
                             @DefaultValue("curl") String curlPath,
                             @DefaultValue("") String curlParams,
                             @DefaultValue("10") int curlTimeout,
                             @DefaultValue("false") boolean historyRecordingEnabled,
                             @DefaultValue("false") boolean saveOutputDocumentsToHistory,
                             @DefaultValue("730") int historyCleanupMaxDays,
                             @DefaultValue("1000") int historyCleanupMaxItemsPerReport,
                             @DefaultValue("3") int countOfRetry,
                             @DefaultValue("false") boolean useOfficeForDocumentConversion) {
        this.officePath = officePath;
        this.officePorts = officePorts;
        this.docFormatterTimeout = docFormatterTimeout;
        this.displayDeviceAvailable = displayDeviceAvailable;
        this.pdfFontsDirectory = pdfFontsDirectory;
        this.putEmptyRowIfNoDataSelected = putEmptyRowIfNoDataSelected;
        this.parameterPrototypeQueryLimit = parameterPrototypeQueryLimit;
        this.wizardEntitiesBlackList = wizardEntitiesBlackList == null ? Collections.emptyList() : wizardEntitiesBlackList;
        this.wizardEntitiesWhiteList = wizardEntitiesWhiteList == null ? Collections.emptyList() : wizardEntitiesWhiteList;
        this.wizardPropertiesBlackList = wizardPropertiesBlackList == null ? Collections.emptyList() : wizardPropertiesBlackList;
        this.wizardPropertiesExcludedBlackList = wizardPropertiesExcludedBlackList == null ? Collections.emptyList() : wizardPropertiesExcludedBlackList;
        this.entityTreeModelMaxDepth = entityTreeModelMaxDepth;
        this.htmlExternalResourcesTimeoutSec = htmlExternalResourcesTimeoutSec;
        this.curlPath = curlPath;
        this.curlParams = curlParams;
        this.curlTimeout = curlTimeout;
        this.historyRecordingEnabled = historyRecordingEnabled;
        this.saveOutputDocumentsToHistory = saveOutputDocumentsToHistory;
        this.historyCleanupMaxDays = historyCleanupMaxDays;
        this.historyCleanupMaxItemsPerReport = historyCleanupMaxItemsPerReport;
        this.countOfRetry = countOfRetry;
        this.useOfficeForDocumentConversion = useOfficeForDocumentConversion;
    }

    /**
     * @return Path to the installed OpenOffice
     */
    public String getOfficePath() {
        return officePath;
    }

    /**
     * @return The list of ports to start OpenOffice on.
     */
    public List<Integer> getOfficePorts() {
        return officePorts;
    }

    /**
     * @return Request to OpenOffice timeout in seconds.
     */
    public Integer getDocFormatterTimeout() {
        return docFormatterTimeout;
    }

    /**
     * @return Has to be false if using OpenOffice reporting formatter on a *nix server without X server running
     */
    public boolean getDisplayDeviceAvailable() {
        return displayDeviceAvailable;
    }

    /**
     * @return Directory with fonts for generate PDF from HTML
     */
    public String getPdfFontsDirectory() {
        return pdfFontsDirectory;
    }

    /**
     * @return The option which enforces standard data extractor to put empty row in each band if no data has been selected
     * In summary this option says - would table linked with empty band have at least one empty row or not.
     */
    public Boolean getPutEmptyRowIfNoDataSelected() {
        return putEmptyRowIfNoDataSelected;
    }

    /**
     * @return Default limit used if parameter prototype object does not specify limit itself
     */
    public Integer getParameterPrototypeQueryLimit() {
        return parameterPrototypeQueryLimit;
    }

    /**
     * Return entities that will not be available for report wizard.
     * Note that if {@code jmix.reports.wizardEntitiesWhiteList} is not empty, this list will be ignored
     *
     * @return list of ignored entities
     */
    public List<String> getWizardEntitiesBlackList() {
        return wizardEntitiesBlackList;
    }

    /**
     * Entities that will be available for report wizard. All others entities will be ignored.
     * Note that even if {@code jmix.reports.wizardEntitiesBlackList} is not empty, this list will be used anyway.
     *
     * @return list of entities that available for reportWizard
     */
    public List<String> getWizardEntitiesWhiteList() {
        return wizardEntitiesWhiteList;
    }

    /**
     * Entity properties that will not be available for report creation wizard. Format is like {@code BaseUuidEntity.id,BaseUuidEntity.createTs,ref$Car.id,...}<br>
     * Properties support inheritance, i.e. {@code BaseUuidEntity.id} will filter that field for all descendants, e.g. {@code ref$Car}.
     * To allow selection of a field for a concrete descendant (e.g. {@code ref$Car}), use
     * {@code reporting.wizardPropertiesExcludedBlackList} setting with value {@code ref$Car.id}.
     *
     * @return blacklisted properties that is not available
     */
    public List<String> getWizardPropertiesBlackList() {
        return wizardPropertiesBlackList;
    }

    /**
     * @return Entity properties that will not to be excluded by {@code jmix.reports.wizardPropertiesBlackList} setting
     *
     * @see ReportsProperties#getWizardPropertiesBlackList()
     */
    public List<String> getWizardPropertiesExcludedBlackList() {
        return wizardPropertiesExcludedBlackList;
    }

    /**
     * @return Maximum depth of entity model that is used in report wizard and report dataset fetchPlan editor.
     */
    public Integer getEntityTreeModelMaxDepth() {
        return entityTreeModelMaxDepth;
    }


    public Integer getHtmlExternalResourcesTimeoutSec() {
        return htmlExternalResourcesTimeoutSec;
    }

    /**
     * Reporting uses cURL tool to generate reports from URL. This is the system path to the tool.
     * @return path to the cURL tool to generate report from URL
     */
    public String getCurlPath() {
        return curlPath;
    }

    /**
     * Reporting uses cURL tool to generate reports from URL. This the string with parameters used while calling cURL.
     * @return parameters used while calling cURL to generate reports from URL
     */
    public String getCurlParams() {
        return curlParams;
    }

    public Integer getCurlTimeout() {
        return curlTimeout;
    }

    /**
     * @return Flag to enable execution history recording.
     */
    public boolean isHistoryRecordingEnabled() {
        return historyRecordingEnabled;
    }

    /**
     * If enabled - then save all output documents to file storage, so they can be downloaded later.
     * @return if enabled - then save all output documents to file storage
     */
    public boolean isSaveOutputDocumentsToHistory() {
        return saveOutputDocumentsToHistory;
    }

    /**
     * Report execution history deletes all history items older than this number of days.
     * Value == 0 means no cleanup by this criteria.
     * @return max available number of days for execution history
     */
    public int getHistoryCleanupMaxDays() {
        return historyCleanupMaxDays;
    }

    /**
     * Report execution cleanup leaves only this number of execution history items for each report,
     * deleting all older items.
     * Value == 0 means no cleanup by this criteria.
     * @return max available number of execution history items per report
     */
    public int getHistoryCleanupMaxItemsPerReport() {
        return historyCleanupMaxItemsPerReport;
    }

    public int getCountOfRetry() {
        return countOfRetry;
    }

    public boolean isUseOfficeForDocumentConversion() {
        return useOfficeForDocumentConversion;
    }
}