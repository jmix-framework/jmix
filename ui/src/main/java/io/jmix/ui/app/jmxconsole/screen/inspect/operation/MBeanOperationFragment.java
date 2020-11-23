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

package io.jmix.ui.app.jmxconsole.screen.inspect.operation;

import io.jmix.core.Messages;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiComponents;
import io.jmix.ui.app.jmxconsole.JmxControl;
import io.jmix.ui.app.jmxconsole.model.ManagedBeanAttribute;
import io.jmix.ui.app.jmxconsole.model.ManagedBeanOperation;
import io.jmix.ui.app.jmxconsole.model.ManagedBeanOperationParameter;
import io.jmix.ui.app.jmxconsole.screen.inspect.attribute.AttributeComponentProvider;
import io.jmix.ui.component.*;
import io.jmix.ui.executor.BackgroundTask;
import io.jmix.ui.executor.TaskLifeCycle;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.jmix.ui.app.jmxconsole.AttributeHelper.convertTypeToReadableName;

@UiController("ui_MBeanOperationFragment")
@UiDescriptor("mbean-operation-fragment.xml")
public class MBeanOperationFragment extends ScreenFragment {
    private final Logger log = LoggerFactory.getLogger(MBeanOperationFragment.class);

    @Autowired
    protected UiComponents uiComponents;

    @Autowired
    protected Label<String> nameLabel;

    @Autowired
    protected JmxControl jmxControl;

    @Autowired
    protected Messages messages;

    @Autowired
    protected Notifications notifications;

    @Autowired
    protected Dialogs dialogs;

    @Autowired
    protected ScreenBuilders screenBuilders;

    @Autowired
    protected GridLayout operationParameters;

    @Autowired
    protected Label<String> descriptionLabel;

    @Autowired
    protected ObjectProvider<AttributeComponentProvider> attributeComponentProviders;

    @Autowired
    protected CollectionLoader<ManagedBeanAttribute> attrLoader;

    protected ManagedBeanOperation operation;

    protected List<AttributeComponentProvider> attrProviders = new ArrayList<>();

    @Subscribe
    public void afterInit(AfterInitEvent afterInitEvent) {
        nameLabel.setValue(String.format("%s %s()", convertTypeToReadableName(operation.getReturnType()), operation.getName()));

        String description = operation.getDescription();
        if (StringUtils.isNotEmpty(description)) {
            descriptionLabel.setVisible(true);
            descriptionLabel.setValue(description);
        }

        List<ManagedBeanOperationParameter> parameters = operation.getParameters();
        if (CollectionUtils.isNotEmpty(parameters)) {
            operationParameters.setVisible(true);
            operationParameters.setRows(parameters.size());
            int row = 0;

            for (ManagedBeanOperationParameter param : parameters) {
                Label<String> pnameLbl = createLabel(param.getName());

                Label<String> ptypeLbl = createLabel(convertTypeToReadableName(param.getType()));

                AttributeComponentProvider prov = attributeComponentProviders.getObject()
                        .withFrame(getFragment().getFrame())
                        .withType(param.getType())
                        .build();
                attrProviders.add(prov);
                Component editField = prov.getComponent();

                Component editComposition = editField;

                if (StringUtils.isNotBlank(param.getDescription())) {
                    Label<String> pdescrLbl = createLabel(param.getDescription());

                    BoxLayout editorLayout = uiComponents.create(VBoxLayout.class);
                    editorLayout.add(editField, pdescrLbl);

                    editComposition = editorLayout;
                }

                operationParameters.add(pnameLbl, 0, row);
                operationParameters.add(ptypeLbl, 1, row);
                operationParameters.add(editComposition, 2, row);
                row++;
            }
        }
    }

    @Subscribe("invokeBtn")
    protected void invokeOperation(Button.ClickEvent clickEvent) {
        Object[] paramValues;
        try {
            paramValues = attrProviders.stream()
                    .map(attributeComponentProvider -> attributeComponentProvider.getAttributeValue(true))
                    .toArray();
        } catch (Exception e) {
            log.error("Conversion error", e);
            notifications.create()
                    .withCaption(messages.getMessage(getClass(), "invokeOperation.conversionError"))
                    .withType(Notifications.NotificationType.HUMANIZED)
                    .show();
            return;
        }

        if (operation.getRunAsync()) {
            runAsynchronously(operation, paramValues, operation.getTimeout());
        } else {
            runSynchronously(operation, paramValues);
        }
    }


    protected void runSynchronously(ManagedBeanOperation operation, Object[] paramValues) {
        Map<String, Object> resultMap = Collections.emptyMap();
        try {
            Object res = jmxControl.invokeOperation(operation, paramValues);
            if (res != null) {
                resultMap = getResultMap(operation, res);
            }
        } catch (Exception ex) {
            log.error("Error occurs while performing JMX operation {}", operation.getName(), ex);

            resultMap = getExceptionMap(operation, ex);
        }

        openResultScreen(resultMap, this);
    }

    protected void runAsynchronously(ManagedBeanOperation operation, Object[] paramValues, Long timeout) {
        BackgroundTask<Long, Object> task = new BackgroundTask<Long, Object>(timeout, TimeUnit.MILLISECONDS, getHostScreen()) {
            @Override
            public Object run(TaskLifeCycle<Long> taskLifeCycle) throws Exception {
                return jmxControl.invokeOperation(operation, paramValues);
            }

            @Override
            public void done(Object result) {
                openResultScreen(getResultMap(operation, result), getOwnerScreen());
            }

            @Override
            public boolean handleException(Exception ex) {
                openResultScreen(getExceptionMap(operation, ex), getOwnerScreen());
                return true;
            }
        };

        dialogs.createBackgroundWorkDialog(this, task)
                .withCancelAllowed(true)
                .build()
                .show();
    }


    protected Label<String> createLabel(String value) {
        Label<String> label = uiComponents.create(Label.TYPE_DEFAULT);
        label.setValue(value);
        return label;
    }

    protected Map<String, Object> getResultMap(ManagedBeanOperation operation, Object res) {
        return ParamsMap.of(
                "result", res,
                "beanName", operation.getMbean().getClassName(),
                "methodName", operation.getName());
    }

    protected Map<String, Object> getExceptionMap(ManagedBeanOperation operation, Exception e) {
        return ParamsMap.of(
                "exception", e,
                "beanName", operation.getMbean().getClassName(),
                "methodName", operation.getName());
    }

    protected void openResultScreen(Map<String, Object> params, FrameOwner frameOwner) {
        Screen jmxConsoleOperationResult = screenBuilders.screen(frameOwner)
                .withScreenClass(MBeanOperationResultScreen.class)
                .withOpenMode(OpenMode.DIALOG)
                .withOptions(new MapScreenOptions(params))
                .build();

        jmxConsoleOperationResult.addAfterCloseListener(afterCloseEvent -> attrLoader.load());

        jmxConsoleOperationResult.show();
    }

    public void setOperation(ManagedBeanOperation operation) {
        this.operation = operation;
    }
}
