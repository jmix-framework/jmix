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
import org.springframework.lang.Nullable;

import java.util.Collection;

public interface ReportRepository {
    Collection<Report> getAllReports();

    /**
     * Load by code a full report object with all details, suitable for passing it to the running engine.

     * @param reportCode report's unique code
     * @return full report object with all details, or null if no such report
     */
    @Nullable
    Report loadFullReportByCode(String reportCode);

    /**
     * Check if any reports are connected to the given group.
     *
     * @param group report group
     * @return true if there is a report connected to this group, false otherwise
     */
    boolean existsReportByGroup(ReportGroupInfo group);
}
