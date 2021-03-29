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
package io.jmix.reportsui.wizard;

import io.jmix.core.FetchPlan;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.reports.app.EntityTree;
import io.jmix.reports.app.service.ReportsWizard;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.wizard.EntityTreeNode;
import io.jmix.reports.entity.wizard.ReportData;
import io.jmix.reports.entity.wizard.ReportRegion;
import io.jmix.reports.entity.wizard.TemplateFileType;
import io.jmix.reports.exception.TemplateGenerationException;
import io.jmix.reportsui.wizard.template.TemplateGeneratorImpl;
import io.jmix.reportsui.wizard.template.TemplateGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.inject.Provider;
import java.util.List;

@Component("report_ReportWizardService")
public class ReportsWizardImpl implements ReportsWizard {
    @Autowired
    private ReportingWizard reportingWizard;
    @Autowired
    private Provider<EntityTreeModelBuilder> entityTreeModelBuilderApiProvider;

//    //todo prototype
//    @Autowired
//    protected TemplateGeneratorApi templateGeneratorApi(ReportData reportData, TemplateFileType templateFileType) {
//        return new TemplateGenerator(reportData, templateFileType);
//    }

    @Override
    public Report toReport(ReportData reportData, boolean temporary) {
        return reportingWizard.toReport(reportData, temporary);
    }

    @Override
    public FetchPlan createViewByReportRegions(EntityTreeNode entityTreeRootNode, List<ReportRegion> reportRegions) {
        return reportingWizard.createViewByReportRegions(entityTreeRootNode, reportRegions);
    }

    @Override
    public ReportRegion createReportRegionByView(EntityTree entityTree, boolean isTabulated, @Nullable FetchPlan view, @Nullable String collectionPropertyName) {
        return reportingWizard.createReportRegionByView(entityTree, isTabulated, view, collectionPropertyName);
    }

    @Override
    public boolean isEntityAllowedForReportWizard(MetaClass metaClass) {
        return reportingWizard.isEntityAllowedForReportWizard(metaClass);
    }

    @Override
    public boolean isPropertyAllowedForReportWizard(MetaClass metaClass, MetaProperty metaProperty) {
        return reportingWizard.isPropertyAllowedForReportWizard(metaClass, metaProperty);
    }

    @Override
    public byte[] generateTemplate(ReportData reportData, TemplateFileType templateFileType) throws TemplateGenerationException {
        TemplateGenerator templateGenerator = new TemplateGeneratorImpl(reportData, templateFileType);
        return templateGenerator.generateTemplate();
    }

    @Override
    public EntityTree buildEntityTree(MetaClass metaClass) {
        return entityTreeModelBuilderApiProvider.get().buildEntityTree(metaClass);
    }
}