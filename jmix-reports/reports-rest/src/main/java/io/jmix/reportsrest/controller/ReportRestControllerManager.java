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

package io.jmix.reportsrest.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.haulmont.yarg.reporting.ReportOutputDocument;
import com.haulmont.yarg.util.converter.ObjectToStringConverter;
import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.reports.ParameterClassResolver;
import io.jmix.reports.ReportSecurityManager;
import io.jmix.reports.entity.*;
import io.jmix.reports.exception.FailedToConnectToOpenOfficeException;
import io.jmix.reports.exception.NoOpenOfficeFreePortsException;
import io.jmix.reports.exception.ReportingException;
import io.jmix.reports.runner.ReportRunContext;
import io.jmix.reports.runner.ReportRunner;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@Component("report_ReportRestControllerManager")
public class ReportRestControllerManager {
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected ReportRunner reportRunner;
    @Autowired
    protected ObjectToStringConverter objectToStringConverter;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected SecureOperations secureOperations;
    @Autowired
    protected ReportSecurityManager reportSecurityManager;
    @Autowired
    protected ParameterClassResolver parameterClassResolver;
    @Autowired
    protected PolicyStore policyStore;
    @Autowired
    protected FetchPlans fetchPlans;
    @Autowired
    protected FetchPlanRepository fetchPlanRepository;
    @Autowired
    protected CurrentUserSubstitution currentUserSubstitution;

    public String loadGroup(String entityId) {
        checkCanReadEntity(metadata.getClass(ReportGroup.class));

        LoadContext<ReportGroup> loadContext = new LoadContext(metadata.getClass(ReportGroup.class));

        FetchPlan fetchPlan = fetchPlans.builder(ReportGroup.class)
                .add("id")
                .add("title")
                .add("code")
                .build();
        loadContext.setFetchPlan(fetchPlan)
                .setId(getIdFromString(entityId, metadata.getClass(ReportGroup.class)));

        ReportGroup group = dataManager.load(loadContext);
        checkEntityIsNotNull(metadata.getClass(ReportGroup.class).getName(), entityId, group);

        GroupInfo info = new GroupInfo();
        //noinspection ConstantConditions
        info.id = group.getId().toString();
        info.code = group.getCode();
        info.title = group.getTitle();

        return createGson().toJson(info);
    }

    public String loadReportsList() {
        checkCanReadEntity(metadata.getClass(Report.class));

        LoadContext<Report> loadContext = new LoadContext(metadata.getClass(Report.class));
        FetchPlan fetchPlan = fetchPlans.builder(Report.class)
                .add("id")
                .add("name")
                .add("code")
                .add("group")
                .build();

        loadContext.setFetchPlan(fetchPlan)
                .setQueryString("select r from report_Report r where r.restAccess = true");
        reportSecurityManager.applySecurityPolicies(
                loadContext,
                null,
                currentUserSubstitution.getEffectiveUser());
        List<Report> reports = dataManager.loadList(loadContext);

        List<ReportInfo> objects = reports.stream()
                .map(this::mapToReportInfo)
                .collect(Collectors.toList());

        return createGson().toJson(objects);
    }

    public String loadReport(String entityId) {
        Report report = loadReportInternal(entityId);
        return createGson().toJson(mapToReportInfo(report));
    }

    public ReportRestResult runReport(String entityId, String bodyJson) {
        Report report = loadReportInternal(entityId);
        final ReportRunRestBody body;
        try {
            body = createGson().fromJson(bodyJson, ReportRunRestBody.class);
        } catch (JsonSyntaxException e) {
            throw new RestAPIException("Invalid JSON body",
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST,
                    e);
        }
        if (body.template != null) {
            ReportTemplate reportTemplate = report.getTemplates().stream()
                    .filter(t -> Objects.equals(t.getCode(), body.template))
                    .findFirst()
                    .orElseThrow(() -> new RestAPIException("Template not found",
                            String.format("Template with code %s not found for report %s", body.template, entityId), HttpStatus.BAD_REQUEST));
            checkReportOutputType(reportTemplate);
        } else {
            checkReportOutputType(report.getDefaultTemplate());
        }
        Map<String, Object> preparedValues = prepareValues(report, body.parameters);
        if (body.template != null) {
            try {
                ReportOutputDocument document = reportRunner.byReportEntity(report)
                        .withTemplateCode(body.template)
                        .withParams(preparedValues)
                        .run();
                return new ReportRestResult(document, body.attachment);
            } catch (FailedToConnectToOpenOfficeException e) {
                throw new RestAPIException("Run report error", "Couldn't find LibreOffice instance",
                        HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (NoOpenOfficeFreePortsException e) {
                throw new RestAPIException("Run report error", "Couldn't connect to LibreOffice instance. No free ports available.",
                        HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (ReportingException e) {
                throw new RestAPIException("Run report error",
                        e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            try {
                return new ReportRestResult(reportRunner.run(new ReportRunContext(report).setParams(preparedValues)), body.attachment);
            } catch (FailedToConnectToOpenOfficeException e) {
                throw new RestAPIException("Run report error", "Couldn't find LibreOffice instance",
                        HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (NoOpenOfficeFreePortsException e) {
                throw new RestAPIException("Run report error", "Couldn't connect to LibreOffice instance. No free ports available.",
                        HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (ReportingException e) {
                throw new RestAPIException("Run report error",
                        e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    protected Report loadReportInternal(String entityId) {
        checkCanReadEntity(metadata.getClass(Report.class));

        LoadContext<Report> loadContext = new LoadContext(metadata.getClass(Report.class));
        FetchPlan fetchPlan = fetchPlanRepository.getFetchPlan(Report.class, "report.edit");
        loadContext.setFetchPlan(fetchPlan)
                .setQueryString("select r from report_Report r where r.id = :id and r.restAccess = true")
                .setParameter("id", getReportIdFromString(entityId));

        reportSecurityManager.applySecurityPolicies(loadContext, null, currentUserSubstitution.getEffectiveUser());

        Report report = dataManager.load(loadContext);

        checkEntityIsNotNull(metadata.getClass(Report.class).getName(), entityId, report);
        return report;
    }

    protected Map<String, Object> prepareValues(Report report, List<ParameterValueInfo> paramValues) {
        Map<String, Object> preparedValues = new HashMap<>();
        if (paramValues != null) {
            for (ReportInputParameter inputParam : report.getInputParameters()) {
                paramValues.stream().filter(paramValue -> Objects.equals(paramValue.name, inputParam.getAlias()))
                        .findFirst()
                        .ifPresent(paramValue -> preparedValues.put(paramValue.name, prepareValue(inputParam, paramValue)));
            }
        }
        return preparedValues;
    }

    protected Gson createGson() {
        return new GsonBuilder().create();
    }

    @Nullable
    protected Object prepareValue(ReportInputParameter inputParam, ParameterValueInfo paramValue) {
        ParameterType parameterType = inputParam.getType();
        if (parameterType == ParameterType.ENTITY) {
            if (paramValue.value != null) {
                MetaClass entityClass = metadata.getClass(inputParam.getEntityMetaClass());
                checkCanReadEntity(entityClass);
                Object entityId = getIdFromString(paramValue.value, entityClass);
                //noinspection unchecked
                Entity entity = (Entity) dataManager.load(entityClass.getJavaClass())
                        .id(entityId)
                        .fetchPlan(FetchPlan.INSTANCE_NAME)
                        .optional().orElse(null);
                checkEntityIsNotNull(entityClass.getName(), paramValue.value, entity);
                return entity;
            }
        } else if (parameterType == ParameterType.ENTITY_LIST) {
            if (paramValue.values != null) {
                MetaClass entityClass = metadata.getClass(inputParam.getEntityMetaClass());
                checkCanReadEntity(entityClass);
                List<Entity> entities = new ArrayList<>();
                for (String value : paramValue.values) {
                    Object entityId = getIdFromString(value, entityClass);
                    //noinspection unchecked
                    Entity entity = (Entity) dataManager.load(entityClass.getJavaClass())
                            .id(entityId)
                            .fetchPlan(FetchPlan.INSTANCE_NAME)
                            .optional().orElse(null);
                    checkEntityIsNotNull(entityClass.getName(), value, entity);
                    entities.add(entity);
                }
                return entities;
            }
        } else if (paramValue.value != null) {
            Class paramClass = resolveDatatypeActualClass(inputParam);
            return objectToStringConverter.convertFromString(paramClass, paramValue.value);
        }
        return null;
    }

    protected Class resolveDatatypeActualClass(ReportInputParameter inputParam) {
        switch (inputParam.getType()) {
            case DATE:
                return java.sql.Date.class;
            case TIME:
                return java.sql.Time.class;
            default:
                return parameterClassResolver.resolveClass(inputParam);
        }
    }

    protected ReportInfo mapToReportInfo(Report report) {
        ReportInfo reportInfo = new ReportInfo();
        reportInfo.id = report.getId().toString();
        reportInfo.code = report.getCode();
        reportInfo.name = report.getName();
        reportInfo.group = report.getGroup().getId().toString();

        if (entityStates.isLoaded(report, "templates")) {
            if (report.getTemplates() != null) {
                reportInfo.templates = report.getTemplates().stream()
                        .map(this::mapTemplateInfo)
                        .collect(Collectors.toList());
            }
        }

        if (entityStates.isLoaded(report, "xml")) {
            if (report.getInputParameters() != null) {
                reportInfo.inputParameters = report.getInputParameters().stream()
                        .map(this::mapInputParameterInfo)
                        .collect(Collectors.toList());
            }
        }
        return reportInfo;
    }

    protected TemplateInfo mapTemplateInfo(ReportTemplate template) {
        TemplateInfo templateInfo = new TemplateInfo();
        templateInfo.code = template.getCode();
        templateInfo.outputType = template.getReportOutputType().toString();
        templateInfo.isDefault = Objects.equals(template.getReport().getDefaultTemplate(), template);
        return templateInfo;
    }

    protected InputParameterInfo mapInputParameterInfo(ReportInputParameter parameter) {
        InputParameterInfo inputParameterInfo = new InputParameterInfo();
        inputParameterInfo.name = parameter.getName();
        inputParameterInfo.alias = parameter.getAlias();
        if (parameter.getType() != null) {
            inputParameterInfo.type = parameter.getType().toString();
        }
        inputParameterInfo.required = Boolean.TRUE.equals(parameter.getRequired());
        inputParameterInfo.hidden = Boolean.TRUE.equals(parameter.getHidden());
        if (parameter.getEntityMetaClass() != null) {
            inputParameterInfo.entityMetaClass = parameter.getEntityMetaClass();
        }
        if (parameter.getEnumerationClass() != null) {
            inputParameterInfo.enumerationClass = parameter.getEnumerationClass();
        }

        if (parameter.getDefaultValue() != null) {
            inputParameterInfo.defaultValue = transformDefaultValue(parameter);
        }
        return inputParameterInfo;
    }

    protected String transformDefaultValue(ReportInputParameter parameter) {
        switch (parameter.getType()) {
            case DATE:
            case TIME:
                Object defParamValue = objectToStringConverter.convertFromString(parameter.getParameterClass(), parameter.getDefaultValue());
                return objectToStringConverter.convertToString(resolveDatatypeActualClass(parameter), defParamValue);
        }
        return parameter.getDefaultValue();
    }

    protected UUID getReportIdFromString(String entityId) {
        return (UUID) getIdFromString(entityId, metadata.getClass(Report.class));
    }

    protected Object getIdFromString(String entityId, MetaClass metaClass) {
        try {
            MetaProperty primaryKeyProperty = Objects.requireNonNull(metadataTools.getPrimaryKeyProperty(metaClass));
            Class<?> idClass = primaryKeyProperty.getJavaType();

            if (UUID.class.isAssignableFrom(idClass)) {
                return UUID.fromString(entityId);
            } else if (Integer.class.isAssignableFrom(idClass)) {
                return Integer.valueOf(entityId);
            } else if (Long.class.isAssignableFrom(idClass)) {
                return Long.valueOf(entityId);
            } else {
                return entityId;
            }
        } catch (Exception e) {
            throw new RestAPIException("Invalid entity ID",
                    String.format("Cannot convert %s into valid entity ID", entityId),
                    HttpStatus.BAD_REQUEST,
                    e);
        }
    }

    protected void checkCanReadEntity(MetaClass metaClass) {
        if (!secureOperations.isEntityReadPermitted(metaClass, policyStore)) {
            throw new RestAPIException("Reading forbidden",
                    String.format("Reading of the %s is forbidden", metaClass.getName()),
                    HttpStatus.FORBIDDEN);
        }
    }

    protected void checkEntityIsNotNull(String entityName, String entityId, @Nullable Object entity) {
        if (entity == null) {
            throw new RestAPIException("Entity not found",
                    String.format("Entity %s with id %s not found", entityName, entityId),
                    HttpStatus.NOT_FOUND);
        }
    }

    protected void checkReportOutputType(ReportTemplate reportTemplate) {
        if (reportTemplate != null) {
            ReportOutputType outputType = reportTemplate.getReportOutputType();
            if (outputType == ReportOutputType.CHART || outputType == ReportOutputType.TABLE
                    || outputType == ReportOutputType.PIVOT_TABLE) {
                throw new RestAPIException("Run report error",
                        String.format("%s report output type is not supported by Reporting REST API", outputType.toString()),
                        HttpStatus.BAD_REQUEST);
            }
        }
    }

    protected static class ReportInfo {
        protected String id;
        protected String name;
        protected String code;
        protected String group;

        protected List<TemplateInfo> templates;
        protected List<InputParameterInfo> inputParameters;
    }

    protected static class GroupInfo {
        protected String id;
        protected String title;
        protected String code;
    }

    protected static class TemplateInfo {
        protected String code;
        protected String outputType;
        @SerializedName(value = "default")
        protected Boolean isDefault;
    }

    protected static class InputParameterInfo {
        protected String name;
        protected String alias;
        protected String type;
        protected boolean required;
        protected boolean hidden;
        protected String entityMetaClass;
        protected String enumerationClass;
        protected String defaultValue;
    }

    protected static class ParameterValueInfo {
        protected String name;
        protected String value;
        protected List<String> values;
    }

    protected static class ReportRunRestBody {
        protected String template;
        protected boolean attachment;
        protected List<ParameterValueInfo> parameters;
    }
}
