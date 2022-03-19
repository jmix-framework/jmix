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

package io.jmix.reportsui.screen.report.wizard.template.generators;

import io.jmix.core.DataManager;
import io.jmix.reports.entity.table.TemplateTableBand;
import io.jmix.reports.entity.table.TemplateTableColumn;
import io.jmix.reports.entity.table.TemplateTableDescription;
import io.jmix.reports.entity.wizard.RegionProperty;
import io.jmix.reports.entity.wizard.ReportData;
import io.jmix.reports.entity.wizard.ReportRegion;
import io.jmix.reportsui.screen.report.wizard.template.Generator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

@Component("report_TableGenerator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TableGenerator implements Generator {

    @Autowired
    private DataManager dataManager;

    @Override
    public byte[] generate(ReportData reportData) {
        TemplateTableDescription templateTableDescription = dataManager.create(TemplateTableDescription.class);
        List<TemplateTableBand> bands = new LinkedList<>();

        for (int i = 0; i < reportData.getReportRegions().size(); i++) {
            ReportRegion reportRegion = reportData.getReportRegions().get(i);
            TemplateTableBand band = dataManager.create(TemplateTableBand.class);
            band.setPosition(i);
            band.setBandName(reportRegion.getNameForBand());

            List<TemplateTableColumn> columns = new LinkedList<>();
            for (int j = 0; j < reportRegion.getRegionProperties().size(); j++) {
                RegionProperty regionProperty = reportData.getReportRegions().get(i).getRegionProperties().get(j);

                String caption = regionProperty.getHierarchicalLocalizedNameExceptRoot().replace('.', ' ');

                TemplateTableColumn column = dataManager.create(TemplateTableColumn.class);
                column.setPosition(j);
                column.setKey(regionProperty.getHierarchicalNameExceptRoot());
                column.setCaption(caption);

                columns.add(column);
            }
            band.setColumns(columns);
            bands.add(band);
        }

        templateTableDescription.setTemplateTableBands(bands);

        return TemplateTableDescription.toJsonString(templateTableDescription).getBytes(StandardCharsets.UTF_8);
    }
}
