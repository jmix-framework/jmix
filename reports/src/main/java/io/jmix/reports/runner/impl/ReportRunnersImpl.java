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

package io.jmix.reports.runner.impl;

import io.jmix.reports.entity.Report;
import io.jmix.reports.runner.ReportRunner;
import io.jmix.reports.runner.ReportRunners;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("report_ReportRunners")
public class ReportRunnersImpl implements ReportRunners {

    @Autowired
    private ObjectProvider<ReportRunner> reportRunnerObjectProvider;

    @Override
    public ReportRunner byReportEntity(Report reportEntity) {
        return reportRunnerObjectProvider.getObject(reportEntity);
    }

    @Override
    public ReportRunner byReportCode(String reportCode) {
        return reportRunnerObjectProvider.getObject(reportCode);
    }

}
