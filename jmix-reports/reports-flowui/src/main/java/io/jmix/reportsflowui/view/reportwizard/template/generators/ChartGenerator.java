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

package io.jmix.reportsflowui.view.reportwizard.template.generators;

import io.jmix.reportsflowui.view.reportwizard.template.Generator;
import freemarker.template.TemplateException;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.reports.entity.charts.*;
import io.jmix.reports.entity.wizard.RegionProperty;
import io.jmix.reports.entity.wizard.ReportData;
import io.jmix.reports.entity.wizard.ReportRegion;
import io.jmix.reports.exception.TemplateGenerationException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component("report_ChartGenerator")
public class ChartGenerator implements Generator {

    @Autowired
    protected Metadata metadata;

    @Override
    public byte[] generate(ReportData reportData) throws TemplateGenerationException, TemplateException, IOException {
        if (reportData.getChartType() == ChartType.SERIAL) {
            return generateSerialChart(reportData);
        } else if (reportData.getChartType() == ChartType.PIE) {
            return generatePieChart(reportData);
        }
        return new byte[0];
    }

    protected byte[] generateSerialChart(ReportData reportData) {
        if (CollectionUtils.isNotEmpty(reportData.getReportRegions())) {
            ReportRegion reportRegion = reportData.getReportRegions().get(0);
            SerialChartDescription serialChartDescription = metadata.create(SerialChartDescription.class);
            serialChartDescription.setBandName(reportRegion.getNameForBand());
            serialChartDescription.setValueAxisUnits("");
            serialChartDescription.setCategoryAxisCaption("");
            serialChartDescription.setValueAxisCaption("");

            List<RegionProperty> regionProperties = reportRegion.getRegionProperties();
            RegionProperty firstProperty = regionProperties.get(0);

            serialChartDescription.setCategoryField(firstProperty.getEntityTreeNode().getMetaPropertyName());
            serialChartDescription.setCategoryAxisCaption(firstProperty.getLocalizedName());
            if (regionProperties.size() > 1) {
                for (int i = 1; i < regionProperties.size(); i++) {
                    RegionProperty regionProperty = regionProperties.get(i);
                    MetaClass parentMetaClass = metadata.getClass(regionProperty.getEntityTreeNode().getParentMetaClassName());
                    MetaProperty metaProperty = parentMetaClass.getProperty(regionProperty.getEntityTreeNode().getMetaPropertyName());
                    Class<?> javaType = metaProperty.getJavaType();
                    if (Number.class.isAssignableFrom(javaType)) {
                        ChartSeries chartSeries = metadata.create(ChartSeries.class);
                        chartSeries.setName(regionProperty.getLocalizedName());
                        chartSeries.setValueField(metaProperty.getName());
                        chartSeries.setType(SeriesType.COLUMN);
                        chartSeries.setOrder(serialChartDescription.getSeries().size() + 1);
                        serialChartDescription.getSeries().add(chartSeries);
                    }
                }
            }

            return AbstractChartDescription.toJsonString(serialChartDescription).getBytes(StandardCharsets.UTF_8);
        }

        return new byte[0];
    }

    protected byte[] generatePieChart(ReportData reportData) {
        ReportRegion reportRegion = reportData.getReportRegions().get(0);
        PieChartDescription pieChartDescription = metadata.create(PieChartDescription.class);
        pieChartDescription.setBandName(reportRegion.getNameForBand());
        pieChartDescription.setUnits("");

        List<RegionProperty> regionProperties = reportRegion.getRegionProperties();
        RegionProperty firstProperty = regionProperties.get(0);
        pieChartDescription.setTitleField(firstProperty.getEntityTreeNode().getMetaPropertyName());
        if (regionProperties.size() > 1) {
            for (int i = 1; i < regionProperties.size(); i++) {
                RegionProperty regionProperty = regionProperties.get(i);
                MetaClass parentMetaClass = metadata.getClass(regionProperty.getEntityTreeNode().getParentMetaClassName());
                MetaProperty metaProperty = parentMetaClass.getProperty(regionProperty.getEntityTreeNode().getMetaPropertyName());
                Class<?> javaType = metaProperty.getJavaType();
                if (Number.class.isAssignableFrom(javaType)) {
                    pieChartDescription.setValueField(metaProperty.getName());
                    break;
                }
            }
        }

        return AbstractChartDescription.toJsonString(pieChartDescription).getBytes(StandardCharsets.UTF_8);
    }
}
