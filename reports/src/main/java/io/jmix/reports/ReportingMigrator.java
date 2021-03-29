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


import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.reports.entity.Report;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("reporting_ReportingMigrator")
public class ReportingMigrator implements io.jmix.reports.ReportingMigratorMBean {

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected Reports reports;

    @Autowired
    protected FetchPlanRepository fetchPlanRepository;

    //TODO authenticated
//    @Authenticated
    public String updateSecurityIndex() {
        MetaClass metaClass = metadata.getClass(Report.class);
        FetchPlan fetchPlan = fetchPlanRepository.getFetchPlan(metaClass, "report.edit");

        LoadContext<Report> ctx = new LoadContext(metaClass);
        //todo dynamic
        //ctx.setLoadDynamicAttributes(true);
        ctx.setFetchPlan(fetchPlan);
        ctx.setQueryString("select r from report_Report r");
        List<Report> resultList = dataManager.loadList(ctx);
        for (Report report : resultList) {
            reports.storeReportEntity(report);
        }
        return "Index migrated successfully";
    }
}
