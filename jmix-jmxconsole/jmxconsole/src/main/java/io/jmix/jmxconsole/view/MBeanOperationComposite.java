/*
 * Copyright 2022 Haulmont.
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

package io.jmix.jmxconsole.view;

import io.jmix.jmxconsole.AttributeComponentProvider;
import io.jmix.jmxconsole.model.ManagedBeanAttribute;
import io.jmix.jmxconsole.model.ManagedBeanOperation;
import io.jmix.jmxconsole.model.ManagedBeanOperationParameter;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.core.Messages;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.details.JmixDetails;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.DataComponents;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.View;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.jmix.jmxconsole.AttributeHelper.convertTypeToReadableName;

/**
 * Represents a composite UI component for displaying and invoking
 * MBean operations. The component includes all relevant controls
 * and logic for handling JMX MBean operations.
 */
public class MBeanOperationComposite extends Composite<JmixDetails>
        implements ApplicationContextAware, HasSize, HasEnabled, InitializingBean, HasComponents {

    private final Logger log = LoggerFactory.getLogger(MBeanOperationComposite.class);

    protected ApplicationContext applicationContext;

    protected UiComponents uiComponents;
    protected Notifications notifications;
    protected Messages messages;
    protected DataComponents dataComponents;
    protected DialogWindows dialogWindows;

    protected CollectionContainer<ManagedBeanAttribute> attributeDc;
    protected CollectionLoader<ManagedBeanAttribute> attributeDl;
    protected VerticalLayout operationVbox;
    protected VerticalLayout verticalLayout;
    protected H4 nameLabel;
    protected Span descriptionSpan;
    protected JmixDetails form;
    protected JmixButton invokeBtn;
    protected ManagedBeanOperation operation;
    protected AttributeComponentProvider attributeComponentProvider;
    protected Map<Component, String> attributeFieldsTypeMap = new HashMap<>();

    @Override
    public void afterPropertiesSet() {
        initComponent();
    }

    protected void initComponent() {
        this.uiComponents = applicationContext.getBean(UiComponents.class);
        this.notifications = applicationContext.getBean(Notifications.class);
        this.messages = applicationContext.getBean(Messages.class);
        this.dataComponents = applicationContext.getBean(DataComponents.class);
        this.dialogWindows = applicationContext.getBean(DialogWindows.class);
        this.attributeComponentProvider = applicationContext.getBean(AttributeComponentProvider.class);
    }

    @Override
    protected JmixDetails initContent() {
        attributeDc = dataComponents.createCollectionContainer(ManagedBeanAttribute.class);
        attributeDl = dataComponents.createCollectionLoader();
        attributeDl.setContainer(attributeDc);

        verticalLayout = uiComponents.create(VerticalLayout.class);
        verticalLayout.addClassName("py-0");
        verticalLayout.setSpacing(false);

        operationVbox = uiComponents.create(VerticalLayout.class);
        operationVbox.setPadding(false);
        operationVbox.setMargin(false);
        operationVbox.setSpacing(false);
        operationVbox.setVisible(false);

        nameLabel = uiComponents.create(H4.class);

        descriptionSpan = uiComponents.create(Span.class);
        descriptionSpan.setVisible(false);

        invokeBtn = uiComponents.create(JmixButton.class);
        invokeBtn.setText(messages.getMessage(getClass(), "invokeButton.text"));
        invokeBtn.addClickListener(this::onInvokeButtonClick);

        verticalLayout.add(nameLabel, descriptionSpan, operationVbox, invokeBtn);

        form = uiComponents.create(JmixDetails.class);
        form.setWidthFull();
        form.setHeightFull();
        form.addContent(verticalLayout);
        form.setOpened(false);

        initComponents();

        return form;
    }

    protected void initComponents() {
        form.setSummaryText(String.format(" %s():%s - %s",
                operation.getName(), convertTypeToReadableName(operation.getReturnType()), operation.getDescription()));

        List<ManagedBeanOperationParameter> parameters = operation.getParameters();

        if (CollectionUtils.isNotEmpty(parameters)) {
            operationVbox.setVisible(true);

            for (ManagedBeanOperationParameter param : parameters) {
                Component attributeField = attributeComponentProvider
                        .builder()
                        .withType(param.getType())
                        .build();
                attributeFieldsTypeMap.put(attributeField, param.getType());
                ((HasSize) attributeField).setWidthFull();
                if (StringUtils.isNotBlank(param.getDescription())) {
                    ((HasHelper) attributeField).setHelperText(param.getDescription());
                }
                ((HasSize) attributeField).setWidth("25em");
                ((HasLabel) attributeField).setLabel(param.getName() + ":" + convertTypeToReadableName(param.getType()));
                operationVbox.add(attributeField);
            }
        }
    }


    protected void onInvokeButtonClick(ClickEvent<Button> buttonClickEvent) {
        Object[] paramValues;
        try {
            paramValues = attributeFieldsTypeMap.entrySet()
                    .stream()
                    .map(componentTypePair -> attributeComponentProvider
                            .getFieldConvertedValue((HasValueAndElement<?, ?>) componentTypePair.getKey(),
                                    componentTypePair.getValue(),
                                    true))
                    .toArray();
        } catch (Exception e) {
            log.error("Conversion error", e);
            notifications.create(messages.getMessage(getClass(), "invokeOperation.conversionError"))
                    .withType(Notifications.Type.DEFAULT)
                    .show();
            return;
        }

        DialogWindow<MBeanOperationResultView> jmxConsoleOperationResult = dialogWindows
                .view(new View<>(), MBeanOperationResultView.class)
                .build();
        jmxConsoleOperationResult.getView().setParameters(operation, paramValues);

        jmxConsoleOperationResult.open();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setOperation(ManagedBeanOperation operation) {
        this.operation = operation;
    }
}
