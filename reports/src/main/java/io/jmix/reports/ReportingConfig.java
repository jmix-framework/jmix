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

package io.jmix.reports;

import com.haulmont.cuba.core.config.Config;
import com.haulmont.cuba.core.config.Property;
import com.haulmont.cuba.core.config.Source;
import com.haulmont.cuba.core.config.SourceType;
import com.haulmont.cuba.core.config.defaults.DefaultBoolean;
import com.haulmont.cuba.core.config.defaults.DefaultInt;
import com.haulmont.cuba.core.config.defaults.DefaultInteger;
import com.haulmont.cuba.core.config.defaults.DefaultString;
import com.haulmont.cuba.core.config.type.CommaSeparatedStringListTypeFactory;
import com.haulmont.cuba.core.config.type.Factory;

import java.util.List;

/**
 * Reporting configuration interface.
 */
@Source(type = SourceType.APP)
public interface ReportingConfig extends Config {

    /**
     * @return Path to the installed OpenOffice
     */
    @Property("reporting.office.path")
    @DefaultString("/")
    String getOfficePath();

    /**
     * @return The list of ports to start OpenOffice on.
     */
    @Property("reporting.office.ports")
    @DefaultString("8100|8101|8102|8103")
    String getOfficePorts();

    /**
     * @return Request to OpenOffice timeout in seconds.
     */
    @Property("reporting.office.docFormatterTimeout")
    @DefaultInteger(20)
    Integer getDocFormatterTimeout();

    /**
     * @return Has to be false if using OpenOffice reporting formatter on a *nix server without X server running
     */
    @Property("reporting.displayDeviceUnavailable")
    @DefaultBoolean(false)
    boolean getDisplayDeviceAvailable();

    /**
     * @return Directory with fonts for generate PDF from HTML
     */
    @Property("reporting.fontsDir")
    String getPdfFontsDirectory();

    /**
     * @return The option which enforces standard data extractor to put empty row in each band if no data has been selected
     * In summary this option says - would table linked with empty band have at least one empty row or not.
     */
    @Property("reporting.putEmptyRowIfNoDataSelected")
    @Source(type = SourceType.DATABASE)
    @DefaultBoolean(true)
    Boolean getPutEmptyRowIfNoDataSelected();

    void setPutEmptyRowIfNoDataSelected(Boolean putEmptyRowIfNoDataSelected);

    /**
     * @return Default limit used if parameter prototype object does not specify limit itself
     */
    @Property("reporting.parameterPrototypeQueryLimit")
    @Source(type = SourceType.DATABASE)
    @DefaultInteger(1000)
    Integer getParameterPrototypeQueryLimit();

    /**
     * Return entities that will not be available for report wizard.
     * Note that if {@code reporting.wizardEntitiesWhiteList} is not empty, this list will be ignored
     *
     * @return list of ignored entities
     */
    @Property("reporting.wizardEntitiesBlackList")
    @Source(type = SourceType.DATABASE)
    @DefaultString("")
    String getWizardEntitiesBlackList();

    void setWizardEntitiesBlackList(String wizardEntitiesBlackList);

    /**
     * Entities that will be available for report wizard. All others entities will be ignored.
     * Note that even if {@code cuba.reporting.wizardEntitiesBlackList} is not empty, this list will be used anyway.
     *
     * @return list of entities that available for reportWizard
     */
    @Property("reporting.wizardEntitiesWhiteList")
    @Source(type = SourceType.DATABASE)
    @DefaultString("")
    String getWizardEntitiesWhiteList();

    void setWizardEntitiesWhiteList(String wizardEntitiesWhiteList);

    /**
     * JmixEntity properties that will not be available for report creation wizard. Format is like {@code BaseUuidEntity.id,BaseUuidEntity.createTs,ref$Car.id,...}<br>
     * Properties support inheritance, i.e. {@code BaseUuidEntity.id} will filter that field for all descendants, e.g. {@code ref$Car}.
     * To allow selection of a field for a concrete descendant (e.g. {@code ref$Car}), use
     * {@code reporting.wizardPropertiesExcludedBlackList} setting with value {@code ref$Car.id}.
     *
     * @return blacklisted properties that is not available
     */
    @Property("reporting.wizardPropertiesBlackList")
    @Source(type = SourceType.DATABASE)
    @DefaultString("")
    @Factory(factory = CommaSeparatedStringListTypeFactory.class)
    List<String> getWizardPropertiesBlackList();

    void setWizardPropertiesBlackList(List<String> wizardPropertiesBlackList);

    /**
     * JmixEntity properties that will not to be excluded by {@code reporting.wizardPropertiesBlackList} setting
     * @see ReportingConfig#getWizardPropertiesBlackList()
     */
    @Property("reporting.wizardPropertiesExcludedBlackList")
    @Source(type = SourceType.DATABASE)
    @DefaultString("")
    @Factory(factory =  CommaSeparatedStringListTypeFactory.class)
    List<String> getWizardPropertiesExcludedBlackList();

    void setWizardPropertiesExcludedBlackList(List<String> wizardPropertiesExcludedBlackList);

    /**
     * Maximum depth of entity model that is used in report wizard and report dataset view editor.
     */
    @Property("reporting.entityTreeModelMaxDepth")
    @Source(type = SourceType.DATABASE)
    @DefaultInteger(3)
    Integer getEntityTreeModelMaxDeep();

    void setEntityTreeModelMaxDeep(Integer entityTreeModelMaxDeep);


    @Property("reporting.html.externalResourcesTimeoutSec")
    @DefaultInteger(5)
    Integer getHtmlExternalResourcesTimeoutSec();

    void setHtmlExternalResourcesTimeoutSec(Integer externalResourcesTimeoutSec);

    /**
     * Reporting uses CURL tool to generate reports from URL. This is the system path to the tool.
     */
    @Property("reporting.curl.path")
    @DefaultString("curl")
    String getCurlPath();
    void setCurlPath(String value);

    /**
     * Reporting uses CURL tool to generate reports from URL. This the string with parameters used while calling CURL.
     */
    @Property("reporting.curl.params")
    @DefaultString("")
    String getCurlParams();
    void setCurlParams(String value);

    @Property("reporting.curl.timeoutSec")
    @DefaultInteger(10)
    Integer getCurlTimeout();
    void setCurlTimeout(Integer value);

    /**
     * Toggle for Groovy dataset's transactions. If true, transactions are read-only.
     */
    @Property("reporting.useReadOnlyTransactionForGroovy")
    @Source(type = SourceType.DATABASE)
    @DefaultBoolean(true)
    Boolean getUseReadOnlyTransactionForGroovy();

    void setUseReadOnlyTransactionForGroovy(Boolean useReadOnlyTransactionForGroovy);

    /**
     * Flag to enable execution history recording.
     */
    @Property("reporting.executionHistory.enabled")
    @DefaultBoolean(false)
    @Source(type = SourceType.DATABASE)
    boolean isHistoryRecordingEnabled();
    void setHistoryRecordingEnabled(boolean historyRecordingEnabled);

    /**
     * If enabled - then save all output documents to file storage, so they can be downloaded later.
     * Note that ReportExecution stores file that is independent from the one created by ReportingApi#createAndSaveReport methods.
     */
    @Property("reporting.executionHistory.saveOutputDocument")
    @DefaultBoolean(false)
    @Source(type = SourceType.DATABASE)
    boolean isSaveOutputDocumentsToHistory();
    void setSaveOutputDocumentsToHistory(boolean saveOutputDocumentsToHistory);

    /**
     * Report execution history deletes all history items older than this number of days.
     * Value == 0 means no cleanup by this criteria.
     */
    @Property("reporting.executionHistory.cleanup.days")
    @DefaultInt(2 * 365)
    @Source(type = SourceType.DATABASE)
    int getHistoryCleanupMaxDays();
    void setHistoryCleanupMaxDays(int historyCleanupMaxDays);

    /**
     * Report execution cleanup leaves only this number of execution history items for each report,
     * deleting all older items.
     * Value == 0 means no cleanup by this criteria.
     */
    @Property("reporting.executionHistory.cleanup.itemsPerReport")
    @DefaultInt(1000)
    @Source(type = SourceType.DATABASE)
    int getHistoryCleanupMaxItemsPerReport();
    void setHistoryCleanupMaxItemsPerReport(int historyCleanupMaxItemsPerReport);
}