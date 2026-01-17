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

package io.jmix.reports.test_support;

import io.jmix.core.UnconstrainedDataManager;
import io.jmix.reports.entity.ReportGroup;
import io.jmix.reports.entity.ReportSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReportGroupUtil {

    public static final String SIMPLE_RUNTIME_REPORT_GROUP_NAME = "Simple runtime report";
    public static final String SIMPLE_RUNTIME_REPORT_GROUP_CODE = "SIMPLE_REPORT_GROUP_CODE";

    @Autowired
    protected UnconstrainedDataManager unconstrainedDataManager;
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    public ReportGroup constructSimpleReportGroup() {
        ReportGroup reportGroup = unconstrainedDataManager.create(ReportGroup.class);

        reportGroup.setSource(ReportSource.DATABASE);
        reportGroup.setCode(SIMPLE_RUNTIME_REPORT_GROUP_CODE);
        reportGroup.setTitle(SIMPLE_RUNTIME_REPORT_GROUP_NAME);

        return reportGroup;
    }

    public ReportGroup createAndSaveSimpleReportGroup() {
        ReportGroup reportGroup = constructSimpleReportGroup();

        return unconstrainedDataManager.save(reportGroup);
    }

    public void cleanupDatabaseReportGroups() {
        jdbcTemplate.update("delete from REPORT_GROUP");
    }
}
