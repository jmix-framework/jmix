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
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.reports.entity.table.TemplateTableBand;
import io.jmix.reports.entity.table.TemplateTableColumn;
import io.jmix.reports.entity.table.TemplateTableDescription;
import io.jmix.reports.entity.wizard.EntityTreeNode;
import io.jmix.reports.entity.wizard.RegionProperty;
import io.jmix.reports.entity.wizard.ReportData;
import io.jmix.reports.entity.wizard.ReportRegion;
import org.apache.commons.lang3.StringUtils;
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

    @Autowired
    private Metadata metadata;

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
                RegionProperty regionProperty = reportRegion.getRegionProperties().get(j);

                String caption = regionProperty.getHierarchicalLocalizedNameExceptRoot().replace('.', ' ');
                String key = resolveKey(regionProperty);

                TemplateTableColumn column = dataManager.create(TemplateTableColumn.class);
                column.setPosition(j);
                column.setKey(key);
                column.setCaption(caption);

                columns.add(column);
            }
            band.setColumns(columns);
            bands.add(band);
        }

        templateTableDescription.setTemplateTableBands(bands);

        return TemplateTableDescription.toJsonString(templateTableDescription).getBytes(StandardCharsets.UTF_8);
    }

    protected String resolveKey(RegionProperty regionProperty) {
        String hierarchicalNameExceptRoot = regionProperty.getHierarchicalNameExceptRoot();
        if (!hierarchicalNameExceptRoot.contains(".")) {
            return hierarchicalNameExceptRoot;
        }

        //Remove first property from full property path if it's a collection to match further band data
        EntityTreeNode childNode = regionProperty.getEntityTreeNode();
        EntityTreeNode parentNode;
        String parentMetaClassName = null;
        String childPropertyName = null;
        while (childNode.getParent() != null) {
            parentNode = childNode.getParent();
            parentMetaClassName = parentNode.getMetaClassName();
            childPropertyName = childNode.getMetaPropertyName();
            childNode = parentNode;
        }

        String key = hierarchicalNameExceptRoot;
        if (StringUtils.isNotEmpty(parentMetaClassName) && StringUtils.isNotEmpty(childPropertyName)) {
            MetaClass rootMetaClass = metadata.getClass(parentMetaClassName);
            MetaPropertyPath childProperty = rootMetaClass.getPropertyPath(childPropertyName);
            if (childProperty != null) {
                boolean many = childProperty.getRange().getCardinality().isMany();
                if (many) {
                    String[] propertyParts = hierarchicalNameExceptRoot.split("\\.", 2);
                    if (propertyParts.length == 2) {
                        key = propertyParts[1];
                    }
                }
            }
        }
        return key;
    }
}
