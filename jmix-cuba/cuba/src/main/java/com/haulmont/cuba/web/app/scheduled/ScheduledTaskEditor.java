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

package com.haulmont.cuba.web.app.scheduled;

import com.haulmont.cuba.core.app.SchedulingService;
import com.haulmont.cuba.core.app.scheduled.MethodInfo;
import com.haulmont.cuba.core.app.scheduled.MethodParameterInfo;
import com.haulmont.cuba.core.entity.ScheduledTask;
import com.haulmont.cuba.core.entity.ScheduledTaskDefinedBy;
import com.haulmont.cuba.core.entity.SchedulingType;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.web.gui.components.WebSuggestionPickerField;
import io.jmix.core.Entity;
import io.jmix.core.security.UserRepository;
import io.jmix.ui.component.ComponentContainer;
import io.jmix.ui.component.LinkButton;
import io.jmix.ui.theme.ThemeConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.userdetails.UserDetails;

import javax.inject.Inject;
import java.util.*;

public class ScheduledTaskEditor<T extends Entity & UserDetails> extends AbstractEditor<ScheduledTask> {

    @Inject
    protected LookupField<String> beanNameField;

    @Inject
    protected LookupField<MethodInfo> methodNameField;

    @Inject
    protected WebSuggestionPickerField<T> userNameField;

    @Inject
    protected UserRepository userRepository;

    @Inject
    protected OptionsGroup<ScheduledTaskDefinedBy, ScheduledTaskDefinedBy> definedByField;

    @Inject
    protected TextField<String> classNameField;

    @Inject
    protected TextField<String> scriptNameField;

    @Inject
    protected Label<String> beanNameLabel;
    @Inject
    protected Label<String> methodNameLabel;
    @Inject
    protected Label<String> classNameLabel;
    @Inject
    protected Label<String> scriptNameLabel;

    @Inject
    protected ComponentContainer methodParamsBox;

    @Inject
    protected Datasource<ScheduledTask> taskDs;

    @Inject
    protected SchedulingService service;

    @Inject
    protected UiComponents uiComponents;

    @Inject
    protected OptionsGroup<SchedulingType, SchedulingType> schedulingTypeField;

    @Inject
    protected TextField<String> cronField;
    @Inject
    protected TextField<Integer> periodField;

    @Inject
    protected DateField<Date> startDateField;

    @Inject
    protected Label<String> cronLabel;
    @Inject
    protected Label<String> periodLabel;
    @Inject
    protected Label<String> startDateLabel;

    @Inject
    protected ThemeConstants themeConstants;

    @Inject
    protected LinkButton cronHelpButton;

    @Inject
    protected BoxLayout cronHbox;

    @Inject
    protected BoxLayout methodNameHbox;

    @Inject
    protected CheckBox logStartField;
    @Inject
    protected CheckBox logFinishField;

    //List holds an information about methods of selected bean
    protected List<MethodInfo> availableMethods = new ArrayList<>();

    protected void show(io.jmix.ui.component.Component... components) {
        for (io.jmix.ui.component.Component component : components) {
            component.setVisible(true);
        }
    }

    protected void hide(io.jmix.ui.component.Component... components) {
        for (io.jmix.ui.component.Component component : components) {
            component.setVisible(false);
        }
    }

    protected void hideAll() {
        hide(classNameField, classNameLabel, scriptNameField, scriptNameLabel, beanNameField, beanNameLabel,
                methodNameField, methodNameLabel, methodNameHbox, methodParamsBox);
    }

    protected void clear(Field... fields) {
        for (Field component : fields) {
            component.setValue(null);
        }
    }

    @Override
    public void init(Map<String, Object> params) {
        schedulingTypeField.setOptionsList(Arrays.asList(SchedulingType.values()));
        schedulingTypeField.addValueChangeListener(e -> setSchedulingTypeField(e.getValue()));

        definedByField.setOptionsList(Arrays.asList(ScheduledTaskDefinedBy.values()));
        definedByField.addValueChangeListener(e -> {
            if (ScheduledTaskDefinedBy.BEAN == e.getValue()) {
                clear(classNameField, scriptNameField);
                hideAll();
                show(beanNameField, beanNameLabel, methodNameField, methodNameLabel, methodNameHbox);
            } else if (ScheduledTaskDefinedBy.CLASS == e.getValue()) {
                clear(beanNameField, methodNameField, scriptNameField);
                hideAll();
                show(classNameField, classNameLabel);
            } else if (ScheduledTaskDefinedBy.SCRIPT == e.getValue()) {
                clear(beanNameField, methodNameField, classNameField);
                hideAll();
                show(scriptNameField, scriptNameLabel);
            } else {
                clear(beanNameField, methodNameField, classNameField, scriptNameField);
                hideAll();
            }
        });

        Map<String, List<MethodInfo>> availableBeans = service.getAvailableBeans();
        beanNameField.setOptionsList(new ArrayList<>(availableBeans.keySet()));
        beanNameField.addValueChangeListener(e -> {
            methodNameField.setValue(null);
            hide(methodParamsBox);
            if (e.getValue() == null) {
                methodNameField.setOptionsList(Collections.emptyList());
            } else {
                availableMethods = availableBeans.get(e.getValue());

                if (availableMethods != null) {
                    Map<String, MethodInfo> optionsMap = new HashMap<>();
                    for (MethodInfo availableMethod : availableMethods) {
                        optionsMap.put(availableMethod.getMethodSignature(), availableMethod);
                    }
                    methodNameField.setOptionsMap(optionsMap);
                }
            }
        });

        methodNameField.addValueChangeListener(e -> {
            clearMethodParamsGrid();
            if (e.getValue() != null) {
                createMethodParamsGrid(e.getValue());
                if (methodParamsBox.getComponents().size() > 1) {
                    show(methodParamsBox);
                } else {
                    hide(methodParamsBox);
                }
            }

            String methodName = (e.getValue() != null) ? e.getValue().getName() : null;
            taskDs.getItem().setMethodName(methodName);

            List<MethodParameterInfo> methodParams = (e.getValue() != null) ?
                    e.getValue().getParameters() : Collections.emptyList();
            taskDs.getItem().updateMethodParameters(methodParams);
        });

        initUserNameField();

        logFinishField.addValueChangeListener(e -> {
            if (Boolean.TRUE.equals(e.getValue())) {
                logStartField.setValue(true);
                logStartField.setEditable(false);
            } else {
                logStartField.setEditable(true);
            }
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void initUserNameField() {
        userNameField.setOptionStyleProvider(item -> item.getUsername());
        userNameField.addValueChangeListener(e -> taskDs.getItem().setUserName(e.getValue() == null ? null : e.getValue().getUsername()));
        userNameField.setSearchExecutor((searchString, searchParams) -> new ArrayList(userRepository.getByUsernameLike(searchString)));
    }

    protected void setSchedulingTypeField(SchedulingType value) {
        if (SchedulingType.CRON == value) {
            hide(periodField, periodLabel, startDateField, startDateLabel);
            clear(periodField, startDateField);
            show(cronField, cronLabel, cronHelpButton, cronHbox);
        } else {
            hide(cronField, cronLabel, cronHelpButton, cronHbox);
            clear(cronField);
            show(periodField, periodLabel, startDateField, startDateLabel);
        }
    }

    @Override
    protected void initNewItem(ScheduledTask item) {
        item.setDefinedBy(ScheduledTaskDefinedBy.BEAN);
        item.setSchedulingType(SchedulingType.PERIOD);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setItem(Entity item) {
        super.setItem(item);

        if (StringUtils.isNotEmpty(getItem().getMethodName())) {
            setInitialMethodNameValue(getItem());
        }
        if (StringUtils.isNotEmpty(getItem().getUserName())) {
            userNameField.setValue((T) userRepository.loadUserByUsername(getItem().getUserName()));
        }
    }

    /**
     * Method reads values of methodName and parameters from item,
     * finds appropriate MethodInfo object in methodInfoField's optionsList
     * and sets found value to methodInfoField
     */
    protected void setInitialMethodNameValue(ScheduledTask task) {
        if (availableMethods == null)
            return;

        List<MethodParameterInfo> methodParamInfos = task.getMethodParameters();
        MethodInfo currentMethodInfo = new MethodInfo(task.getMethodName(), methodParamInfos);
        for (MethodInfo availableMethod : availableMethods) {
            if (currentMethodInfo.definitionEquals(availableMethod)) {
                availableMethod.setParameters(currentMethodInfo.getParameters());
                methodNameField.setValue(availableMethod);
                break;
            }
        }
    }

    protected void createMethodParamsGrid(MethodInfo methodInfo) {
        GridLayout methodParamsGrid = uiComponents.create(GridLayout.class);
        methodParamsGrid.setWidth("100%");
        methodParamsGrid.setSpacing(true);
        methodParamsGrid.setColumns(2);
        methodParamsGrid.setColumnExpandRatio(1, 1);

        int rowsCount = 0;

        for (MethodParameterInfo parameterInfo : methodInfo.getParameters()) {
            Label<String> nameLabel = uiComponents.create(Label.NAME);
            nameLabel.setValue(parameterInfo.getType().getSimpleName() + " " + parameterInfo.getName());

            TextField<Object> valueTextField = uiComponents.create(TextField.NAME);
            valueTextField.setWidth("100%");
            valueTextField.setValue(parameterInfo.getValue());

            valueTextField.addValueChangeListener(e -> {
                parameterInfo.setValue(e.getValue());
                MethodInfo selectedMethod = methodNameField.getValue();
                taskDs.getItem().updateMethodParameters(selectedMethod.getParameters());
            });

            methodParamsGrid.setRows(++rowsCount);
            methodParamsGrid.add(nameLabel, 0, rowsCount - 1);
            methodParamsGrid.add(valueTextField, 1, rowsCount - 1);
        }
        methodParamsBox.add(methodParamsGrid);
    }

    protected void clearMethodParamsGrid() {
        for (io.jmix.ui.component.Component component : methodParamsBox.getComponents()) {
            methodParamsBox.remove(component);
        }
    }

    public void getCronHelp() {
        showMessageDialog("Cron", messages.getMainMessage("cronDescription"),
                MessageType.CONFIRMATION_HTML
                        .setModal(false)
                        .setWidth("500px"));
    }
}