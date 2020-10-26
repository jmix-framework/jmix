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

package io.jmix.reports.wizard;

import io.jmix.core.FetchPlan;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import com.haulmont.cuba.core.global.View;
import io.jmix.reports.app.EntityTree;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.wizard.EntityTreeNode;
import io.jmix.reports.entity.wizard.ReportData;
import io.jmix.reports.entity.wizard.ReportRegion;

import javax.annotation.Nullable;
import java.util.List;

/**
 * API for report wizard
 *
 */
public interface ReportingWizardApi {
    String NAME = "report_ReportingWizardApi";

    Report toReport(ReportData reportData, boolean temporary);

    View createViewByReportRegions(EntityTreeNode entityTreeRootNode, List<ReportRegion> reportRegions);

    ReportRegion createReportRegionByView(EntityTree entityTree, boolean isTabulated, @Nullable FetchPlan view, @Nullable String collectionPropertyName);

    boolean isEntityAllowedForReportWizard(MetaClass metaClass);

    boolean isPropertyAllowedForReportWizard(MetaClass metaClass, MetaProperty metaProperty);
}
