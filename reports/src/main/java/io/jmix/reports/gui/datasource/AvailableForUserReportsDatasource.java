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

package io.jmix.reports.gui.datasource;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.data.impl.GroupDatasourceImpl;
import io.jmix.reports.entity.Report;
import io.jmix.reports.gui.ReportGuiManager;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AvailableForUserReportsDatasource extends GroupDatasourceImpl {
    @Override
    protected void afterLoadData(Map params, LoadContext context, Collection entities) {

        ReportGuiManager reportGuiManager = AppBeans.get("cuba_ReportGuiManager");
        UserSessionSource userSessionSource = AppBeans.get(UserSessionSource.NAME);

        List<Report> reports = reportGuiManager.getAvailableReports(null, SecurityContextHolder.getContext().getAuthentication().getPrincipal(), null);
        entities.retainAll(reports);

        super.afterLoadData(params, context, entities);
    }
}
