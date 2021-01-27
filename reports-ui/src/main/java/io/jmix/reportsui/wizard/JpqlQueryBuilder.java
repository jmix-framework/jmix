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

import io.jmix.core.common.util.Preconditions;
import io.jmix.reports.entity.wizard.RegionProperty;
import io.jmix.reports.entity.wizard.ReportData;
import io.jmix.reports.entity.wizard.ReportRegion;
import io.jmix.reports.entity.wizard.TemplateFileType;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class JpqlQueryBuilder {
    protected ReportData reportData;
    protected ReportRegion reportRegion;
    protected StringBuilder outputFieldsBuilder = new StringBuilder("\n");
    private final StringBuilder joinsBuilder = new StringBuilder(" queryEntity ");
    private final Map<String, String> entityNamesAndAliases = new HashMap<>();
    private String result;

    public JpqlQueryBuilder(ReportData reportData, ReportRegion reportRegion) {
        this.reportData = reportData;
        this.reportRegion = reportRegion;
        this.result = reportData.getQuery();
    }

    public String buildQuery() {
        Preconditions.checkNotNullArgument(reportData.getQuery(), "No query provided to build JPQL data set");

        for (RegionProperty regionProperty : reportRegion.getRegionProperties()) {
            String propertyPath = regionProperty.getHierarchicalNameExceptRoot();
            String nestedEntityAlias = null;
            if (propertyOfNestedEntity(propertyPath)) {
                nestedEntityAlias = resolveNestedEntityAlias(propertyPath);
            }

            addPropertyToQuery(propertyPath, nestedEntityAlias);
            addAliasToQuery(propertyPath);

        }

        addDefaultOrderBy();

        insertOutputFields();
        if (joinsExist()) {
            insertJoins();
        }

        return result.trim().replace("queryEntity", "e");
    }

    protected void addDefaultOrderBy() {
        if (reportData.getReportType() == ReportData.ReportType.LIST_OF_ENTITIES_WITH_QUERY) {
            if (reportData.getTemplateFileType() == TemplateFileType.CHART) {
                reportRegion.getRegionProperties().stream()
                        .findFirst()
                        .ifPresent(regionProperty -> {
                            String regionPropertyName = regionProperty.getEntityTreeNode().getName();
                            result += " order by queryEntity." + regionPropertyName;
                        });
            }
        }
    }

    protected void insertOutputFields() {
        outputFieldsBuilder.delete(outputFieldsBuilder.length() - 2, outputFieldsBuilder.length());
        outputFieldsBuilder.append("\n");
        result = result.replaceFirst(" queryEntity ", outputFieldsBuilder.toString());
    }

    protected void insertJoins() {
        joinsBuilder.append(" \n");
        result = result.replaceFirst(" queryEntity([^\\.]|$)", joinsBuilder.toString() + "$1");
    }

    protected boolean joinsExist() {
        return joinsBuilder.toString().contains("left join");
    }

    protected boolean propertyOfNestedEntity(String propertyPath) {
        return propertyPath.contains(".");
    }

    protected void addAliasToQuery(String propertyPath) {
        outputFieldsBuilder.append(" as \"").append(propertyPath).append("\"")
                .append(",\n");
    }

    protected void addPropertyToQuery(String propertyPath, @Nullable String nestedEntityAlias) {
        if (nestedEntityAlias == null) {
            outputFieldsBuilder.append("queryEntity.").append(propertyPath);
        } else {
            String propertyName = StringUtils.substringAfterLast(propertyPath, ".");
            outputFieldsBuilder.append(nestedEntityAlias).append(".").append(propertyName);
        }
    }

    protected String resolveNestedEntityAlias(String propertyPath) {
        String nestedEntityAlias;
        String entityName = StringUtils.substringBeforeLast(propertyPath, ".");

        if (!entityNamesAndAliases.containsKey(entityName)) {
            nestedEntityAlias = entityName.replaceAll("\\.", "_");
            joinsBuilder.append(" \nleft join queryEntity.").append(entityName).append(" ").append(nestedEntityAlias);
            entityNamesAndAliases.put(entityName, nestedEntityAlias);
        } else {
            nestedEntityAlias = entityNamesAndAliases.get(entityName);
        }
        return nestedEntityAlias;
    }
}