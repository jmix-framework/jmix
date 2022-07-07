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

package io.jmix.emailtemplatesui.screen.emailtemplate.json.parameters;

import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.Sort;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetadataObject;
import io.jmix.emailtemplates.entity.JsonEmailTemplate;
import io.jmix.reports.ParameterClassResolver;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportInputParameter;
import io.jmix.ui.Notifications;
import io.jmix.ui.RemoveOperation;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.*;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@UiController("emltmp_ParametersFragment")
@UiDescriptor("parameters-fragment.xml")
public class ParametersFragment extends ScreenFragment {
    private static final Pattern SIMPLE_FIELD_PATTERN = Pattern.compile("\\$\\{([a-zA-Z0-9]+)([?][^}]*|)}");
    private static final Pattern ENTITY_FIELD_PATTERN = Pattern.compile("\\$\\{([a-zA-Z0-9_]+)\\.([a-zA-Z0-9]+)([?][^}]*|)}");

    @Autowired
    protected Table inputParametersTable;

    @Autowired
    protected Button moveUpBtn;

    @Autowired
    protected Button moveDownBtn;

    @Autowired
    protected GroupBoxLayout validationScriptGroupBox;

    @Autowired
    protected CollectionContainer<ReportInputParameter> parametersDc;

    @Autowired
    private Notifications notifications;

    @Autowired
    private InstanceContainer<Report> reportDc;

    @Autowired
    private Metadata metadata;

    @Autowired
    protected InstanceContainer<JsonEmailTemplate> emailTemplateDc;

    @Autowired
    protected Messages messages;

    @Autowired
    private ParameterClassResolver parameterClassResolver;

    @Install(to = "inputParametersTable.remove", subject = "afterActionPerformedHandler")
    protected void inputParametersTableRemoveAfterActionPerformedHandler(RemoveOperation.AfterActionPerformedEvent<ReportInputParameter> event) {
        orderParameters();
    }

    @Install(to = "inputParametersTable.create", subject = "initializer")
    protected void inputParametersTableCreateInitializer(ReportInputParameter parameter) {
        orderParameters();
        parameter.setPosition(parametersDc.getItems().size());
        parameter.setReport(reportDc.getItem());
    }

    @Subscribe("inputParametersTable.createFromTemplate")
    protected void inputParametersTableCreateFromTemplate(Action.ActionPerformedEvent event) {
        createParameters();
        orderParameters();
    }

    @Subscribe("inputParametersTable.up")
    protected void inputParametersTableMoveUp(Action.ActionPerformedEvent event) {
        ReportInputParameter parameter = (ReportInputParameter) inputParametersTable.getSingleSelected();
        if (parameter != null) {
            List<ReportInputParameter> inputParameters = reportDc.getItem().getInputParameters();
            int index = parameter.getPosition();
            if (index > 0) {
                ReportInputParameter previousParameter = null;
                for (ReportInputParameter inputParameter : inputParameters) {
                    if (inputParameter.getPosition() == index - 1) {
                        previousParameter = inputParameter;
                        break;
                    }
                }
                if (previousParameter != null) {
                    parameter.setPosition(previousParameter.getPosition());
                    previousParameter.setPosition(index);

                    sortParametersByPosition();
                }
            }
        }
    }

    @Subscribe("inputParametersTable.down")
    protected void inputParametersTableMoveDown(Action.ActionPerformedEvent event) {
        ReportInputParameter parameter = (ReportInputParameter) inputParametersTable.getSingleSelected();
        if (parameter != null) {
            List<ReportInputParameter> inputParameters = reportDc.getItem().getInputParameters();
            int index = parameter.getPosition();
            if (index < parametersDc.getItems().size() - 1) {
                ReportInputParameter nextParameter = null;
                for (ReportInputParameter inputParameter : inputParameters) {
                    if (inputParameter.getPosition() == index + 1) {
                        nextParameter = inputParameter;
                        break;
                    }
                }
                if (nextParameter != null) {
                    parameter.setPosition(nextParameter.getPosition());
                    nextParameter.setPosition(index);

                    sortParametersByPosition();
                }
            }
        }
    }

    @Subscribe(id = "reportDc", target = Target.DATA_CONTAINER)
    protected void reportDcOnItemPropertyChangeEvent(InstanceContainer.ItemPropertyChangeEvent<Report> e) {
        boolean validationOnChanged = e.getProperty().equalsIgnoreCase("validationOn");

        if (validationOnChanged) {
            setValidationScriptGroupBoxCaption(e.getItem().getValidationOn());
        }
    }

    @Subscribe(id = "reportDc", target = Target.DATA_CONTAINER)
    protected void reportDcOnItemChangeEvent(InstanceContainer.ItemChangeEvent<Report> e) {
        if (e.getItem() != null) {
            setValidationScriptGroupBoxCaption(e.getItem().getValidationOn());
            sortParametersByPosition();
        }
    }

    @Subscribe(id = "parametersDc", target = Target.DATA_CONTAINER)
    protected void parametersDcOnItemPropertyChangeEvent(CollectionContainer.ItemPropertyChangeEvent<ReportInputParameter> e) {
        if ("position".equals(e.getProperty())) {
            parametersDc.replaceItem(e.getItem());
        }
    }

    @Subscribe(id = "parametersDc", target = Target.DATA_CONTAINER)
    protected void parametersDcOnItemChangeEvent(CollectionContainer.ItemChangeEvent<ReportInputParameter> e) {
        ReportInputParameter item = e.getItem();
        if (item != null) {
            moveUpBtn.setEnabled(item.getPosition() > 0);
            moveDownBtn.setEnabled(item.getPosition() < parametersDc.getItems().size() - 1);
        }
    }

    private List<ReportInputParameter> createSimpleParameters() {
        List<ReportInputParameter> newParameters = new ArrayList<>();
        extractParams(newParameters, emailTemplateDc.getItem().getHtml());
        extractParams(newParameters, emailTemplateDc.getItem().getSubject());
        return newParameters;
    }

    private void extractParams(List<ReportInputParameter> newParameters, String parameterSource) {
        if (StringUtils.isNotBlank(parameterSource)) {
            Matcher m = SIMPLE_FIELD_PATTERN.matcher(parameterSource);
            while (m.find()) {
                String field = m.group(1);
                ReportInputParameter parameter = getParameter(field);
                if (parameter == null) {
                    parameter = initNewParameter(field);
                    parameter.setType(ParameterType.TEXT);
                    parameter.setParameterClass(parameterClassResolver.resolveClass(parameter));
                    newParameters.add(parameter);
                }
            }
        }
    }


    private void createParameters() {
        List<ReportInputParameter> newParameters = new ArrayList<>();
        newParameters.addAll(createEntityParameters());
        newParameters.addAll(createSimpleParameters());
        if (CollectionUtils.isNotEmpty(newParameters)) {
            parametersDc.getMutableItems().addAll(newParameters);

            List<String> newParamNames = newParameters.stream()
                    .map(ReportInputParameter::getName)
                    .collect(Collectors.toList());
            notifications.create(Notifications.NotificationType.TRAY)
                    .withCaption(messages.formatMessage(ParametersFragment.class, "newParametersCreated", StringUtils.join(newParamNames, ", ")))
                    .show();
        }
    }

    private List<ReportInputParameter> createEntityParameters() {
        List<ReportInputParameter> newParameters = new ArrayList<>();
        Map<String, List<String>> entityWithProperties = new HashMap<>();
        extractEntityParams(entityWithProperties, emailTemplateDc.getItem().getHtml());
        extractEntityParams(entityWithProperties, emailTemplateDc.getItem().getSubject());
        for (Map.Entry<String, List<String>> entry : entityWithProperties.entrySet()) {
            String entityAlias = entry.getKey();
            ReportInputParameter parameter = getParameter(entityAlias);
            if (parameter == null) {
                parameter = initNewParameter(entityAlias);
                parameter.setType(ParameterType.ENTITY);
                newParameters.add(parameter);
            }
            if (parameter.getEntityMetaClass() == null) {
                List<String> fields = entry.getValue();
                MetaClass metaClass = findMetaClassForFields(fields);
                if (metaClass != null) {
                    parameter.setEntityMetaClass(metaClass.getName());
                }
            }

            if (parameter.getParameterClass() == null) {
                parameter.setParameterClass(parameterClassResolver.resolveClass(parameter));
            }
        }
        return newParameters;
    }

    protected void orderParameters() {
        if (reportDc.getItem().getInputParameters() == null) {
            reportDc.getItem().setInputParameters(new ArrayList<>());
        }

        for (int i = 0; i < reportDc.getItem().getInputParameters().size(); i++) {
            reportDc.getItem().getInputParameters().get(i).setPosition(i);
        }
    }

    protected void setValidationScriptGroupBoxCaption(Boolean onOffFlag) {
        if (BooleanUtils.isTrue(onOffFlag)) {
            validationScriptGroupBox.setCaption(messages.getMessage("io.jmix.reportsui.screen.report.edit/report.validationScriptOn"));
        } else {
            validationScriptGroupBox.setCaption(messages.getMessage("io.jmix.reportsui.screen.report.edit/report.validationScriptOff"));
        }
    }

    private void extractEntityParams(Map<String, List<String>> entityWithProperties, String parameterSource) {
        if (StringUtils.isNotBlank(parameterSource)) {
            Matcher m = ENTITY_FIELD_PATTERN.matcher(parameterSource);
            while (m.find()) {
                String entityAlias = m.group(1);
                String field = m.group(2);
                if (!entityWithProperties.containsKey(entityAlias)) {
                    entityWithProperties.put(entityAlias, new ArrayList<>());
                }
                entityWithProperties.get(entityAlias).add(field);
            }
        }
    }

    private ReportInputParameter initNewParameter(String entityAlias) {
        ReportInputParameter parameter;
        parameter = metadata.create(ReportInputParameter.class);
        parameter.setName(splitCamelCase(entityAlias));
        parameter.setAlias(entityAlias);
        parameter.setReport(reportDc.getItem());
        parameter.setPosition(0);
        return parameter;
    }

    private MetaClass findMetaClassForFields(List<String> fields) {
        for (MetaClass metaClass : metadata.getSession().getClasses()) {
            List<String> parameters = metaClass.getProperties().stream()
                    .map(MetadataObject::getName)
                    .collect(Collectors.toList());
            if (parameters.containsAll(fields)) {
                return metaClass;
            }
        }
        return null;
    }

    protected String splitCamelCase(String s) {
        return StringUtils.capitalize(StringUtils.join(
                StringUtils.splitByCharacterTypeCamelCase(s), ' '));
    }


    private ReportInputParameter getParameter(String alias) {
        return reportDc.getItem().getInputParameters().stream()
                .filter(p -> alias.equals(p.getAlias()))
                .findFirst()
                .orElse(null);
    }

    protected void sortParametersByPosition() {
        parametersDc.getSorter().sort(Sort.by("position"));
    }
}
