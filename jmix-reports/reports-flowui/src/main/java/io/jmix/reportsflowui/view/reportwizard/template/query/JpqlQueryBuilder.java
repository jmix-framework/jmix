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

package io.jmix.reportsflowui.view.reportwizard.template.query;

import io.jmix.core.common.util.Preconditions;
import io.jmix.reports.entity.wizard.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class JpqlQueryBuilder {

    protected final StringBuilder joinsBuilder = new StringBuilder(" e ");
    protected final Map<String, String> entityNamesAndAliases = new HashMap<>();

    protected ReportData reportData;
    protected ReportRegion reportRegion;
    protected StringBuilder outputFieldsBuilder = new StringBuilder("\n");
    protected String result;

    public JpqlQueryBuilder(ReportData reportData, ReportRegion reportRegion) {
        this.reportData = reportData;
        this.reportRegion = reportRegion;
        this.result = reportData.getQuery();
    }

    public String buildInitialQuery() {
        Preconditions.checkNotNullArgument(reportData.getQuery(), "No query provided to build JPQL data set");

        for (RegionProperty regionProperty : reportRegion.getRegionProperties()) {
            String propertyPath = regionProperty.getHierarchicalNameExceptRoot();
            String nestedEntityAlias = null;
            if (propertyOfNestedEntity(propertyPath)) {
                nestedEntityAlias = resolveNestedEntityAlias(propertyPath);
            }

            addPropertyToQuery(propertyPath, nestedEntityAlias);
            addAliasToQuery(null);
        }

        addDefaultOrderBy();

        insertOutputFields(" e ");
        if (joinsExist()) {
            insertJoins();
        }

        return result.trim();
    }

    public String buildFinalQuery() {
        Preconditions.checkNotNullArgument(reportData.getQuery(), "No query provided to build JPQL data set");

        String outputFields = StringUtils.substringBetween(result, "select", "from");
        for (RegionProperty regionProperty : reportRegion.getRegionProperties()) {
            String propertyPath = regionProperty.getHierarchicalNameExceptRoot();
            String nestedEntityAlias = null;
            if (propertyOfNestedEntity(propertyPath)) {
                nestedEntityAlias = getNestedEntityAlias(propertyPath);
            }
            addPropertyToQuery(propertyPath, nestedEntityAlias);
            addAliasToQuery(propertyPath);
        }
        insertOutputFields(outputFields);

        if (CollectionUtils.isNotEmpty(reportData.getQueryParameters())) {
            for (QueryParameter queryParameter : reportData.getQueryParameters()) {
                result = result.replace(":" + queryParameter.getName(), "${" + queryParameter.getName() + "}");
            }
        }

        return result.trim();
    }

    protected void addDefaultOrderBy() {
        if (reportData.getReportTypeGenerate() == ReportTypeGenerate.LIST_OF_ENTITIES_WITH_QUERY) {
            if (reportData.getTemplateFileType() == TemplateFileType.CHART) {
                reportRegion.getRegionProperties().stream()
                        .findFirst()
                        .ifPresent(regionProperty -> {
                            String regionPropertyName = regionProperty.getEntityTreeNode().getName();
                            result += " order by e." + regionPropertyName;
                        });
            }
        }
    }

    protected void insertOutputFields(String toReplace) {
        outputFieldsBuilder.delete(outputFieldsBuilder.length() - 2, outputFieldsBuilder.length());
        outputFieldsBuilder.append("\n");
        result = result.replaceFirst(toReplace, outputFieldsBuilder.toString());
    }

    protected void insertJoins() {
        joinsBuilder.append(" \n");
        result = result.replaceFirst(" e([^\\.]|$)", joinsBuilder.toString() + "$1");
    }

    protected boolean joinsExist() {
        return joinsBuilder.toString().contains("left join");
    }

    protected boolean propertyOfNestedEntity(String propertyPath) {
        return propertyPath.contains(".");
    }

    protected void addAliasToQuery(@Nullable String alias) {
        if (alias == null) {
            outputFieldsBuilder.append(", ");
        } else {
            outputFieldsBuilder.append(" as \"").append(alias).append("\"")
                    .append(",\n");
        }
    }

    protected void addPropertyToQuery(String propertyPath, @Nullable String nestedEntityAlias) {
        if (nestedEntityAlias == null) {
            outputFieldsBuilder.append("e.").append(propertyPath);
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
            joinsBuilder.append(" \nleft join e.").append(entityName).append(" ").append(nestedEntityAlias);
            entityNamesAndAliases.put(entityName, nestedEntityAlias);
        } else {
            nestedEntityAlias = entityNamesAndAliases.get(entityName);
        }
        return nestedEntityAlias;
    }

    protected String getNestedEntityAlias(String propertyPath) {
        String entityName = StringUtils.substringBeforeLast(propertyPath, ".");
        return entityName.replaceAll("\\.", "_");
    }
}