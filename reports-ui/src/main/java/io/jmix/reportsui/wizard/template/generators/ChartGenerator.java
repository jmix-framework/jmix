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

package io.jmix.reportsui.wizard.template.generators;

import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.reports.entity.charts.*;
import io.jmix.reports.entity.wizard.RegionProperty;
import io.jmix.reports.entity.wizard.ReportData;
import io.jmix.reports.entity.wizard.ReportRegion;
import io.jmix.reports.exception.TemplateGenerationException;
import io.jmix.reportsui.wizard.template.Generator;
import freemarker.template.TemplateException;
import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ChartGenerator implements Generator {
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
            SerialChartDescription serialChartDescription = new SerialChartDescription();
            serialChartDescription.setBandName(reportRegion.getNameForBand());
            serialChartDescription.setValueAxisUnits("");
            serialChartDescription.setCategoryAxisCaption("");
            serialChartDescription.setValueAxisCaption("");

            List<RegionProperty> regionProperties = reportRegion.getRegionProperties();
            RegionProperty firstProperty = regionProperties.get(0);
            serialChartDescription.setCategoryField(firstProperty.getEntityTreeNode().getWrappedMetaProperty().getName());
            serialChartDescription.setCategoryAxisCaption(firstProperty.getLocalizedName());
            if (regionProperties.size() > 1) {
                for (int i = 1; i < regionProperties.size(); i++) {
                    RegionProperty regionProperty = regionProperties.get(i);
                    MetaProperty wrappedMetaProperty = regionProperty.getEntityTreeNode().getWrappedMetaProperty();
                    Class<?> javaType = wrappedMetaProperty.getJavaType();
                    if (Number.class.isAssignableFrom(javaType)) {
                        ChartSeries chartSeries = new ChartSeries();
                        chartSeries.setName(regionProperty.getLocalizedName());
                        chartSeries.setValueField(wrappedMetaProperty.getName());
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
        PieChartDescription pieChartDescription = new PieChartDescription();
        pieChartDescription.setBandName(reportRegion.getNameForBand());
//        pieChartDescription.setShowLegend(true);
        pieChartDescription.setUnits("");

        List<RegionProperty> regionProperties = reportRegion.getRegionProperties();
        RegionProperty firstProperty = regionProperties.get(0);
        pieChartDescription.setTitleField(firstProperty.getEntityTreeNode().getWrappedMetaProperty().getName());
        if (regionProperties.size() > 1) {
            for (int i = 1; i < regionProperties.size(); i++) {
                RegionProperty regionProperty = regionProperties.get(i);
                MetaProperty wrappedMetaProperty = regionProperty.getEntityTreeNode().getWrappedMetaProperty();
                Class<?> javaType = wrappedMetaProperty.getJavaType();
                if (Number.class.isAssignableFrom(javaType)) {
                    pieChartDescription.setValueField(wrappedMetaProperty.getName());
                    break;
                }
            }
        }

        return AbstractChartDescription.toJsonString(pieChartDescription).getBytes(StandardCharsets.UTF_8);
    }
}
