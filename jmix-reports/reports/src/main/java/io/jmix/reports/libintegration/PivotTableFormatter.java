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

import io.jmix.reports.yarg.formatters.factory.FormatterFactoryInput;
import io.jmix.reports.yarg.formatters.impl.AbstractFormatter;
import io.jmix.reports.yarg.structure.BandData;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.impl.StandardSerialization;
import io.jmix.reports.entity.PivotTableData;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.entity.pivottable.PivotTableDescription;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component("report_PivotTableFormatter")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PivotTableFormatter extends AbstractFormatter {

    @Autowired
    private BeanFactory beanFactory;

    public PivotTableFormatter(FormatterFactoryInput formatterFactoryInput) {
        super(formatterFactoryInput);
        this.rootBand = formatterFactoryInput.getRootBand();
        this.reportTemplate = formatterFactoryInput.getReportTemplate();
    }

    @Override
    public void renderDocument() {
        PivotTableDescription pivotTableDescription = ((ReportTemplate) reportTemplate).getPivotTableDescription();
        beanFactory.getBean(StandardSerialization.class).serialize(new PivotTableData(PivotTableDescription.toJsonString(pivotTableDescription), getEntries(pivotTableDescription)), outputStream);
    }

    protected List<KeyValueEntity> getEntries(PivotTableDescription configuration) {
        List<BandData> childrenByName = rootBand.getChildrenByName(configuration.getBandName());
        if (childrenByName == null)
            return Collections.emptyList();
        return childrenByName.stream()
                .filter(band -> band.getData() != null && !band.getData().isEmpty())
                .map(band -> {
                    KeyValueEntity entity = new KeyValueEntity();
                    band.getData().forEach(entity::setValue);
                    return entity;
                })
                .collect(Collectors.toList());
    }
}
