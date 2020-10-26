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

package io.jmix.reportsui.gui.datasource;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.reports.entity.Report;
import io.jmix.reportsui.gui.ReportGuiManager;
import io.jmix.ui.model.impl.CollectionContainerImpl;
import org.springframework.beans.factory.annotation.Autowired;

public class AvailableForUserReportsDatasource extends CollectionContainerImpl<Report> {

    @Autowired
    protected ReportGuiManager reportGuiManager;

    @Autowired
    protected CurrentAuthentication currentAuthentication;

    public AvailableForUserReportsDatasource(MetaClass metaClass) {
        super(metaClass);
        collection = reportGuiManager.getAvailableReports(null, currentAuthentication.getUser(), null);
    }
}
