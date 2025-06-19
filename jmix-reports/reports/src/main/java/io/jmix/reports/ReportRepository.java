/*
 * Copyright 2025 Haulmont.
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
import io.jmix.reports.entity.ReportGroupInfo;
import io.jmix.reports.entity.ReportTemplate;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Provides unified access to both types of reports: stored in database and defined in code.
 * Loading and modification operations apply registered security constraints.
 * <br/>
 * Application should generally should use this interface to load reports, instead of DataManager.
 */
public interface ReportRepository {

    /**
     * Load all known reports.
     * Returned entities aren't guaranteed to have all internal structure loaded, only basic properties.
     * When necessary, reload the entity using {@link #reloadForRunning(Report)} method.
     *
     * @return list of reports with only basic properties loaded
     */
    Collection<Report> getAllReports();

    /**
     * Load list of reports, with filtering, pagination and sorting options available.
     * Returned entities aren't guaranteed to have all internal structure loaded, only basic properties.
     * When necessary, reload the entity using {@link #reloadForRunning(Report)} method.
     *
     * @param loadContext context containing filtering, pagination and sorting options
     * @return list of reports with only basic properties loaded
     */
    List<Report> loadList(ReportLoadContext loadContext);

    /**
     * Calculate total count of reports that conform to passed filter.
     * @param filter object with filter values
     * @return count of suitable reports
     */
    int getTotalCount(ReportFilter filter);

    /**
     * Load by code a full report object with all details, suitable for passing it to the rendering engine.

     * @param reportCode report's unique code
     * @return full report object with all details, or null if no such report
     */
    @Nullable
    Report loadForRunningByCode(String reportCode);

    /**
     * Check if any reports are connected to the given group.
     *
     * @param group report group
     * @return true if there is a report connected to this group, false otherwise
     */
    boolean existsReportByGroup(ReportGroupInfo group);

    /**
     * Saves Report entity to the database.
     *
     * @param report report entity instance
     * @return saved instance
     */
    Report save(Report report);

    /*
     * Reload report if necessary.
     * Loads all details necessary for passing it to the rendering engine.
     */
    Report reloadForRunning(Report report);

    /*
     * Reload template if necessary.
     * Loads all details necessary for passing it to the rendering engine.
     */
    ReportTemplate reloadTemplateForRunning(ReportTemplate template);

}
