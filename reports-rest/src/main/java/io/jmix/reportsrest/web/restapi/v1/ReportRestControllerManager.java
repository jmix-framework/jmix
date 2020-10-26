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

package io.jmix.reportsrest.web.restapi.v1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import io.jmix.core.EntityStates;
import io.jmix.core.JmixEntity;
import io.jmix.core.metamodel.model.MetaClass;
import com.haulmont.cuba.core.global.*;
import io.jmix.core.security.EntityOp;
import io.jmix.reports.ParameterClassResolver;
import io.jmix.reports.ReportSecurityManager;
import io.jmix.reports.app.service.ReportService;
import io.jmix.reports.entity.*;
import io.jmix.reports.exception.FailedToConnectToOpenOfficeException;
import io.jmix.reports.exception.NoOpenOfficeFreePortsException;
import io.jmix.reports.exception.ReportingException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Component("report_ReportRestControllerManager")
public class ReportRestControllerManager {
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected ReportService reportService;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected Security security;
    @Autowired
    protected ReportSecurityManager reportSecurityManager;
    @Autowired
    protected UserSessionSource userSessionSource;
    @Autowired
    protected ParameterClassResolver parameterClassResolver;

    public String loadGroup(String entityId) {
        checkCanReadEntity(metadata.getClassNN(ReportGroup.class));

        LoadContext<ReportGroup> loadContext = new LoadContext<>(ReportGroup.class);
        loadContext.setView(
                new View(ReportGroup.class)
                        .addProperty("id")
                        .addProperty("title")
                        .addProperty("code"))
                .setId(getIdFromString(entityId, metadata.getClassNN(ReportGroup.class)));

        ReportGroup group = dataManager.load(loadContext);
        checkEntityIsNotNull(metadata.getClassNN(ReportGroup.class).getName(), entityId, group);

        GroupInfo info = new GroupInfo();
        //noinspection ConstantConditions
        info.id = group.getId().toString();
        info.code = group.getCode();
        info.title = group.getTitle();

        return createGson().toJson(info);
    }

    public String loadReportsList() {
        checkCanReadEntity(metadata.getClassNN(Report.class));

        LoadContext<Report> loadContext = new LoadContext<>(Report.class);
        loadContext.setView(
                new View(Report.class)
                        .addProperty("id")
                        .addProperty("name")
                        .addProperty("code")
                        .addProperty("group"))
                .setQueryString("select r from report$Report r where r.restAccess = true");
        reportSecurityManager.applySecurityPolicies(loadContext, null, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
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
                return new ReportRestResult(reportService.createReport(report, body.template, preparedValues), body.attachment);
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
                return new ReportRestResult(reportService.createReport(report, preparedValues), body.attachment);
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
        checkCanReadEntity(metadata.getClassNN(Report.class));

        LoadContext<Report> loadContext = new LoadContext<>(Report.class);
        loadContext.setView(ReportService.MAIN_VIEW_NAME)
                .setQueryString("select r from report$Report r where r.id = :id and r.restAccess = true")
                .setParameter("id", getReportIdFromString(entityId));
        reportSecurityManager.applySecurityPolicies(loadContext, null, SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        Report report = dataManager.load(loadContext);

        checkEntityIsNotNull(metadata.getClassNN(Report.class).getName(), entityId, report);
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


    protected Object prepareValue(ReportInputParameter inputParam, ParameterValueInfo paramValue) {
        ParameterType parameterType = inputParam.getType();
        if (parameterType == ParameterType.ENTITY) {
            if (paramValue.value != null) {
                MetaClass entityClass = metadata.getClassNN(inputParam.getEntityMetaClass());
                checkCanReadEntity(entityClass);
                Object entityId = getIdFromString(paramValue.value, entityClass);
                //noinspection unchecked
                JmixEntity entity = dataManager.load(entityClass.getJavaClass())
                        .view(View.MINIMAL)
                        .id(entityId).optional().orElse(null);
                checkEntityIsNotNull(entityClass.getName(), paramValue.value, entity);
                return entity;
            }
        } else if (parameterType == ParameterType.ENTITY_LIST) {
            if (paramValue.values != null) {
                MetaClass entityClass = metadata.getClassNN(inputParam.getEntityMetaClass());
                checkCanReadEntity(entityClass);
                List<JmixEntity> entities = new ArrayList<>();
                for (String value : paramValue.values) {
                    Object entityId = getIdFromString(value, entityClass);
                    //noinspection unchecked
                    JmixEntity entity = (JmixEntity) dataManager.load(entityClass.getJavaClass())
                            .view(View.MINIMAL)
                            .id(entityId).optional().orElse(null);
                    checkEntityIsNotNull(entityClass.getName(), value, entity);
                    entities.add(entity);
                }
                return entities;
            }
        } else if (paramValue.value != null) {
            Class paramClass = resolveDatatypeActualClass(inputParam);
            return reportService.convertFromString(paramClass, paramValue.value);
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
            case ENTITY:
                EntityLoadInfo info = EntityLoadInfo.parse(parameter.getDefaultValue());
                if (info != null) return info.getId().toString();
                break;
            case DATE:
            case TIME:
                Object defParamValue = reportService.convertFromString(parameter.getParameterClass(), parameter.getDefaultValue());
                return reportService.convertToString(resolveDatatypeActualClass(parameter), defParamValue);
        }
        return parameter.getDefaultValue();
    }

    protected UUID getReportIdFromString(String entityId) {
        return (UUID) getIdFromString(entityId, metadata.getClassNN(Report.class));
    }

    protected Object getIdFromString(String entityId, MetaClass metaClass) {
        try {
            //TODO get ID
//            if (BaseDbGeneratedIdEntity.class.isAssignableFrom(metaClass.getJavaClass())) {
//                if (BaseIdentityIdEntity.class.isAssignableFrom(metaClass.getJavaClass())) {
//                    return IdProxy.of(Long.valueOf(entityId));
//                } else if (BaseIntIdentityIdEntity.class.isAssignableFrom(metaClass.getJavaClass())) {
//                    return IdProxy.of(Integer.valueOf(entityId));
//                } else {
                    Class<?> clazz = metaClass.getJavaClass();
                    while (clazz != null) {
                        Method[] methods = clazz.getDeclaredMethods();
                        for (Method method : methods) {
                            if (method.getName().equals("getDbGeneratedId")) {
                                Class<?> idClass = method.getReturnType();
                                if (Long.class.isAssignableFrom(idClass)) {
                                    return Long.valueOf(entityId);
                                } else if (Integer.class.isAssignableFrom(idClass)) {
                                    return Integer.valueOf(entityId);
                                } else if (Short.class.isAssignableFrom(idClass)) {
                                    return Long.valueOf(entityId);
                                } else if (UUID.class.isAssignableFrom(idClass)) {
                                    return UUID.fromString(entityId);
                                }
                            }
                        }
                        clazz = clazz.getSuperclass();
//                    }
                }
//                throw new UnsupportedOperationException("Unsupported ID type in entity " + metaClass.getName());
//            } else {
                //noinspection unchecked
                Method getIdMethod = metaClass.getJavaClass().getMethod("getId");
                Class<?> idClass = getIdMethod.getReturnType();
                if (UUID.class.isAssignableFrom(idClass)) {
                    return UUID.fromString(entityId);
                } else if (Integer.class.isAssignableFrom(idClass)) {
                    return Integer.valueOf(entityId);
                } else if (Long.class.isAssignableFrom(idClass)) {
                    return Long.valueOf(entityId);
                } else {
                    return entityId;
                }
//            }
        } catch (Exception e) {
            throw new RestAPIException("Invalid entity ID",
                    String.format("Cannot convert %s into valid entity ID", entityId),
                    HttpStatus.BAD_REQUEST,
                    e);
        }
    }

    protected void checkCanReadEntity(MetaClass metaClass) {
        if (!security.isEntityOpPermitted(metaClass, EntityOp.READ)) {
            throw new RestAPIException("Reading forbidden",
                    String.format("Reading of the %s is forbidden", metaClass.getName()),
                    HttpStatus.FORBIDDEN);
        }
    }

    protected void checkEntityIsNotNull(String entityName, String entityId, JmixEntity entity) {
        if (entity == null) {
            throw new RestAPIException("JmixEntity not found",
                    String.format("JmixEntity %s with id %s not found", entityName, entityId),
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

    protected class ReportInfo {
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
