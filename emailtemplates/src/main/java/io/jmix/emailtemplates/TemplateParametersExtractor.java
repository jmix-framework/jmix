/*
 * Copyright 2020 Haulmont.
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

package io.jmix.emailtemplates;

import com.google.common.collect.ImmutableMap;
import com.haulmont.yarg.util.converter.ObjectToStringConverter;
import io.jmix.core.ClassManager;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.emailtemplates.dto.ReportWithParams;
import io.jmix.emailtemplates.entity.EmailTemplate;
import io.jmix.emailtemplates.entity.ParameterValue;
import io.jmix.emailtemplates.entity.TemplateReport;
import io.jmix.emailtemplates.exception.ReportParameterTypeChangedException;
import io.jmix.reports.ReportsSerialization;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportInputParameter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;


@Component("emltmp_TemplateParametersExtractor")
public class TemplateParametersExtractor {

    protected Map<ParameterType, Class> primitiveParameterTypeMapping = new ImmutableMap.Builder<ParameterType, Class>()
            .put(ParameterType.BOOLEAN, Boolean.class)
            .put(ParameterType.DATE, Date.class)
            .put(ParameterType.DATETIME, Date.class)
            .put(ParameterType.TEXT, String.class)
            .put(ParameterType.NUMERIC, Double.class)
            .put(ParameterType.TIME, Date.class)
            .build();

    @Autowired
    protected ClassManager classManager;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected EmailTemplates emailTemplates;

    @Autowired
    protected ReportsSerialization reportsSerialization;

    @Autowired
    protected ObjectToStringConverter objectToStringConverter;

    public List<ReportWithParams> getTemplateDefaultValues(EmailTemplate emailTemplate) throws ReportParameterTypeChangedException {
        List<TemplateReport> templateReports = createParamsCollectionByTemplate(emailTemplate);
        List<ReportWithParams> reportWithParams = new ArrayList<>();
        List<String> exceptionMessages = new ArrayList<>();
        for (TemplateReport templateReport : templateReports) {
            try {
                List<ParameterValue> parameterValues = null;
                if (templateReport != null) {
                    parameterValues = templateReport.getParameterValues();
                    reportWithParams.add(getReportDefaultValues(templateReport.getReport(), parameterValues));
                }
            } catch (ReportParameterTypeChangedException e) {
                exceptionMessages.add(e.getMessage());
            }
        }
        if (!exceptionMessages.isEmpty()) {
            StringBuilder messages = new StringBuilder();
            exceptionMessages.forEach(m -> {
                messages.append(m);
                messages.append("\n");
            });
            throw new ReportParameterTypeChangedException(messages.toString());
        }
        return reportWithParams;
    }

    public ReportWithParams getReportDefaultValues(Report report, List<ParameterValue> parameterValues) throws ReportParameterTypeChangedException {
        ReportWithParams paramsData = new ReportWithParams(report);
        List<String> exceptionMessages = new ArrayList<>();
        if (parameterValues != null) {
            Report reportFromXml = reportsSerialization.convertToReport(report.getXml());
            for (ParameterValue paramValue : parameterValues) {
                String alias = paramValue.getAlias();
                String stringValue = paramValue.getDefaultValue();

                ReportInputParameter inputParameter = reportFromXml.getInputParameters().stream()
                        .filter(e -> e.getAlias().equals(alias))
                        .findFirst()
                        .orElse(null);
                if (inputParameter != null) {
                    try {
                        emailTemplates.checkParameterTypeChanged(inputParameter, paramValue);
                    } catch (ReportParameterTypeChangedException e) {
                        exceptionMessages.add(e.getMessage());
                    }
                    Class parameterClass = resolveClass(inputParameter);
                    Object value = convertFromString(inputParameter.getType(), parameterClass, stringValue);
                    paramsData.put(alias, value);
                }
            }
        }
        if (!exceptionMessages.isEmpty()) {
            StringBuilder messages = new StringBuilder();
            exceptionMessages.forEach(m -> {
                messages.append(m);
                messages.append("\n");
            });
            throw new ReportParameterTypeChangedException(messages.toString());
        }
        return paramsData;
    }

    protected List<TemplateReport> createParamsCollectionByTemplate(EmailTemplate emailTemplate) {
        List<TemplateReport> templateReports = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(emailTemplate.getAttachedTemplateReports())) {
            templateReports.addAll(emailTemplate.getAttachedTemplateReports());
        }
        templateReports.add(emailTemplate.getEmailBodyReport());
        return templateReports;
    }

    public Class resolveClass(ReportInputParameter parameter) {
        Class aClass = primitiveParameterTypeMapping.get(parameter.getType());
        if (aClass == null) {
            if (parameter.getType() == ParameterType.ENTITY || parameter.getType() == ParameterType.ENTITY_LIST) {
                MetaClass metaClass = metadata.getSession().getClass(parameter.getEntityMetaClass());
                if (metaClass != null) {
                    return metaClass.getJavaClass();
                } else {
                    return null;
                }
            } else if (parameter.getType() == ParameterType.ENUMERATION) {
                if (StringUtils.isNotBlank(parameter.getEnumerationClass())) {
                    return classManager.loadClass(parameter.getEnumerationClass());
                }
            }
        }
        return aClass;
    }

    public String convertToString(ParameterType parameterType, Class parameterClass, Object paramValue) {
        if (paramValue != null) {
            if (ParameterType.ENTITY_LIST == parameterType) {
                if (paramValue instanceof Collection) {
                    return (String) ((Collection) paramValue).stream()
                            .map(e -> objectToStringConverter.convertToString(parameterClass, e))
                            .collect(Collectors.joining(","));
                }
            } else {
                return objectToStringConverter.convertToString(parameterClass, paramValue);

            }
        }
        return null;
    }

    public Object convertFromString(ParameterType parameterType, Class parameterClass, String paramValueStr) {
        if (ParameterType.ENTITY_LIST == parameterType) {
            if (StringUtils.isBlank(paramValueStr)) {
                return null;
            }
            String[] strValues = paramValueStr.split(",");
            List tokenListValues = new ArrayList();
            for (String s : strValues) {
                if (StringUtils.isNotBlank(s)) {
                    Object colValue = objectToStringConverter.convertFromString(parameterClass, s);
                    if (colValue != null) {
                        tokenListValues.add(colValue);
                    }
                }
            }
            return tokenListValues;
        } else if (paramValueStr != null) {
            return objectToStringConverter.convertFromString(parameterClass, paramValueStr);
        }
        return null;
    }
}
