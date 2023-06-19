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

import io.jmix.reports.yarg.reporting.ReportOutputDocument;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportExecution;

import java.util.Map;

public interface ReportExecutionHistoryRecorder {

    ReportExecution startExecution(Report report, Map<String, Object> params);

    void markAsSuccess(ReportExecution execution, ReportOutputDocument document);

    void markAsError(ReportExecution execution, Exception e);

    void markAsCancelled(ReportExecution execution);

    /**
     * Should be invoked as scheduled task.
     * @return count of deleted {@link ReportExecution} entities
     */
    String cleanupHistory();
}
