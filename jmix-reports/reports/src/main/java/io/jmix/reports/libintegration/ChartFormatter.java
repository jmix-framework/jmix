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

package io.jmix.reports.libintegration;

import io.jmix.core.MetadataTools;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.entity.charts.*;
import io.jmix.reports.yarg.formatters.factory.FormatterFactoryInput;
import io.jmix.reports.yarg.formatters.impl.AbstractFormatter;
import io.jmix.reports.yarg.structure.BandData;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component("report_ChartFormatter")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ChartFormatter extends AbstractFormatter {

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    private MetadataTools metadataTools;

    public ChartFormatter(FormatterFactoryInput formatterFactoryInput) {
        super(formatterFactoryInput);
        this.rootBand = formatterFactoryInput.getRootBand();
        this.reportTemplate = formatterFactoryInput.getReportTemplate();
    }

    @Override
    public void renderDocument() {
        String chartJson = null;
        AbstractChartDescription chartDescription = ((ReportTemplate) reportTemplate).getChartDescription();
        if (chartDescription != null) {
            if (chartDescription.getType() == ChartType.PIE) {
                chartJson = convertPieChart((PieChartDescription) chartDescription);
            } else if (chartDescription.getType() == ChartType.SERIAL) {
                chartJson = convertSerialChart((SerialChartDescription) chartDescription);
            }
        }
        try {
            IOUtils.write(chartJson, outputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while rendering chart", e);
        }
    }

    private String convertSerialChart(SerialChartDescription description) {
        List<Map<String, Object>> data = new ArrayList<>();
        List<BandData> childrenByName = rootBand.getChildrenByName(description.getBandName());
        for (BandData bandData : childrenByName) {
            data.add(bandData.getData());
        }

        return beanFactory.getBean(ChartToJsonConverter.class, metadataTools.getInstanceName(((ReportTemplate) reportTemplate).getReport()))
                .convertSerialChart(description, data);
    }

    protected String convertPieChart(PieChartDescription description) {
        List<Map<String, Object>> data = new ArrayList<>();
        List<BandData> childrenByName = rootBand.getChildrenByName(description.getBandName());
        for (BandData bandData : childrenByName) {
            data.add(bandData.getData());
        }

        return beanFactory.getBean(ChartToJsonConverter.class, metadataTools.getInstanceName(((ReportTemplate) reportTemplate).getReport()))
                .convertPieChart(description, data);
    }
}
