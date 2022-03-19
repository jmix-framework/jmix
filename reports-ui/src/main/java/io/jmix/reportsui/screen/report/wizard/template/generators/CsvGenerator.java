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

import com.google.common.base.Joiner;
import io.jmix.reports.entity.wizard.RegionProperty;
import io.jmix.reports.entity.wizard.ReportData;
import io.jmix.reports.entity.wizard.ReportRegion;
import io.jmix.reportsui.screen.report.wizard.template.Generator;
import io.jmix.reportsui.screen.report.wizard.template.ReportTemplatePlaceholder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component("report_CsvGenerator")
public class CsvGenerator implements Generator {

    protected static final String SEPARATOR = ";";
    protected static final String WRAPPER = "\"";

    @Autowired
    protected ReportTemplatePlaceholder reportTemplatePlaceholder;

    @Override
    public byte[] generate(ReportData reportData) {
        String templateContent = generateTemplate(reportData);
        return templateContent.getBytes(StandardCharsets.UTF_8);
    }

    protected String generateTemplate(ReportData reportData) {
        List<String> headers = new ArrayList<>();
        List<String> aliases = new ArrayList<>();

        List<ReportRegion> reportRegions = reportData.getReportRegions();
        for (ReportRegion reportRegion : reportRegions) {
            List<String> propertyHeaders = new ArrayList<>();
            List<String> propertyAliases = new ArrayList<>();

            List<RegionProperty> regionProperties = reportRegion.getRegionProperties();
            for (RegionProperty regionProperty : regionProperties) {
                propertyHeaders.add(wrapField(regionProperty.getHierarchicalLocalizedNameExceptRoot()));

                String placeholderValue = reportTemplatePlaceholder.getPlaceholderValue(regionProperty.getHierarchicalNameExceptRoot(), reportRegion);
                propertyAliases.add(wrapField(placeholderValue));
            }

            headers.add(Joiner.on(SEPARATOR).join(propertyHeaders));
            aliases.add(Joiner.on(SEPARATOR).join(propertyAliases));
        }

        return Joiner.on(SEPARATOR).join(headers) + "\n" + Joiner.on(SEPARATOR).join(aliases);
    }

    protected static String wrapField(String fieldValue) {
        return WRAPPER + fieldValue + WRAPPER;
    }
}
