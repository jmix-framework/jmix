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

package io.jmix.reports.gui.report;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.CommitContext;
import com.haulmont.cuba.gui.data.impl.GenericDataSupplier;
import io.jmix.core.EntitySet;
import io.jmix.core.JmixEntity;
import io.jmix.reports.app.service.ReportService;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportTemplate;

import java.util.HashSet;
import java.util.Set;

public class ReportDataSupplier extends GenericDataSupplier {
    @Override
    public EntitySet commit(CommitContext context) {
        Set<JmixEntity> result = new HashSet<>();
        ReportService reportService = AppBeans.get(ReportService.NAME, ReportService.class);
        Report reportToStore = null;
        for (JmixEntity entity : context.getCommitInstances()) {
            if (entity instanceof Report) {
                reportToStore = (Report) entity;
            } else if (entity instanceof ReportTemplate) {
                reportToStore = ((ReportTemplate) entity).getReport();
            }
        }

        if (reportToStore != null) {
            result.add(reportService.storeReportEntity(reportToStore));
        }

        return EntitySet.of(result);
    }
}
