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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.MessageTools;
import io.jmix.core.Metadata;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.details.JmixDetails;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import io.jmix.jmxconsole.JmxControl;
import io.jmix.jmxconsole.model.ManagedBeanAttribute;
import io.jmix.jmxconsole.model.ManagedBeanInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "jmxconsole/mbeaninfo/:mbean", layout = DefaultMainViewParent.class)
@ViewController("jmxcon_ManagedBeanInfo.detail")
@ViewDescriptor("mbean-detail-view.xml")
@EditedEntityContainer("mbeanDc")
public class MBeanInfoDetailView extends StandardDetailView<ManagedBeanInfo> {

    public static final String MBEAN_ROUTE_PARAM_NAME = "mbean";

    @ViewComponent
    protected DataGrid<ManagedBeanAttribute> attributesDataGrid;
    @ViewComponent
    protected InstanceContainer<ManagedBeanInfo> mbeanDc;
    @ViewComponent
    protected CollectionContainer<ManagedBeanAttribute> attrDc;
    @ViewComponent
    protected VerticalLayout operations;
    @ViewComponent
    protected TypedTextField<String> operationsSearchField;

    @Autowired
    protected JmxControl jmxControl;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected MessageBundle messageBundle;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected DialogWindows dialogWindows;
    @Autowired
    protected UrlParamSerializer urlParamSerializer;

    @Override
    protected void initExistingEntity(String mbeanObjectName) {
        String objectName = urlParamSerializer.deserialize(String.class, mbeanObjectName);
        ManagedBeanInfo managedBean = jmxControl.getManagedBean(objectName);

        mbeanDc.setItem(managedBean);

        initComponents();
    }

    private void initMbeanFormLayout() {
        FormLayout formLayout = uiComponents.create(FormLayout.class);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        Span mbeanName = createSpan(getEditedEntity().getObjectName());
        Span mbeanClassName = createSpan(getEditedEntity().getClassName());
        Span mbeanDescription = createSpan(getEditedEntity().getDescription());

        formLayout.addFormItem(mbeanName, messageBundle.getMessage("mbean.name"));
        formLayout.addFormItem(mbeanClassName, messageBundle.getMessage("mbean.className"));
        formLayout.addFormItem(mbeanDescription, messageBundle.getMessage("mbean.description"));

        getContent().addComponentAsFirst(formLayout);
    }

    protected Span createSpan(String text) {
        Span span = uiComponents.create(Span.class);
        span.setText(text);
        return span;
    }

    @Override
    public String getPageTitle() {
        String objectName = getEditedEntity().getObjectName();
        return StringUtils.isNotEmpty(objectName)
                ? objectName.substring(objectName.indexOf("=") + 1)
                : super.getPageTitle();
    }

    @Override
    protected String getRouteParamName() {
        return MBEAN_ROUTE_PARAM_NAME;
    }

    protected void initComponents() {
        jmxControl.loadAttributes(getEditedEntity());

        initMbeanFormLayout();
        initOperationsLayout();
        initDataGridColumns();
        initSearchField();
    }

    protected void initSearchField() {
        operationsSearchField.addTypedValueChangeListener(
                valueChangeEvent -> reloadOperations(operationsSearchField.getValue()));
    }

    protected void reloadOperations(String objectName) {
        if (StringUtils.isNotEmpty(objectName)) {
            operations.getChildren()
                    .flatMap(Component::getChildren)
                    .forEach(component -> {
                                boolean isInSearch = StringUtils
                                        .containsIgnoreCase(((JmixDetails) component).getSummaryText(), objectName);
                                component.setVisible(isInSearch);
                                ((JmixDetails) component).setOpened(isInSearch);
                            }
                    );
        } else {
            operations.getChildren()
                    .flatMap(Component::getChildren)
                    .forEach(component -> component.setVisible(true));
        }
    }

    protected void initDataGridColumns() {
        attributesDataGrid.addColumn(createStatusComponentRenderer())
                .setHeader(messageTools.getPropertyCaption(
                        metadata.getClass(ManagedBeanAttribute.class), "readableWriteable"));
    }

    protected ComponentRenderer<Span, ManagedBeanAttribute> createStatusComponentRenderer() {
        return new ComponentRenderer<>(this::createReadableWritableComponent, this::readableWritableComponentUpdater);
    }

    protected Span createReadableWritableComponent() {
        Span span = uiComponents.create(Span.class);
        span.getElement().getThemeList().add("badge");

        return span;
    }

    protected void readableWritableComponentUpdater(Span span, ManagedBeanAttribute attribute) {
        if (attribute.getReadableWriteable() != null) {
            span.setText(attribute.getReadableWriteable());
            if (attribute.getWriteable()) {
                span.getElement().getThemeList().add("success");
            } else {
                span.getElement().getThemeList().add("contrast");
            }
        } else {
            span.setText("No data");
        }
    }

    @Override
    public boolean hasUnsavedChanges() {
        return false;
    }

    @Install(to = "attributesDataGrid.edit", subject = "enabledRule")
    protected boolean attributesDataGridEditEnabledRule() {
        ManagedBeanAttribute managedBeanAttribute = attributesDataGrid.getSingleSelectedItem();
        return managedBeanAttribute != null && managedBeanAttribute.getWriteable();
    }

    @Subscribe("attributesDataGrid.edit")
    public void onAttributesDataGridEdit(final ActionPerformedEvent event) {
        showAttributeDetail();
    }

    protected void showAttributeDetail() {
        ManagedBeanAttribute managedBeanAttribute = attributesDataGrid.getSingleSelectedItem();
        if (managedBeanAttribute == null) {
            return;
        }
        if (!managedBeanAttribute.getWriteable()) {
            return;
        }

        dialogWindows.detail(attributesDataGrid)
                .withViewClass(MBeanAttributeDetailView.class)
                .withContainer(attrDc)
                .editEntity(managedBeanAttribute)
                .withAfterCloseListener(event -> reloadAttribute(event.getView().getEditedEntity()))
                .build()
                .open();
    }

    @Subscribe("attributesDataGrid.refresh")
    public void reloadAttributes(ActionPerformedEvent event) {
        jmxControl.loadAttributes(getEditedEntity());
    }

    protected void reloadAttribute(ManagedBeanAttribute attribute) {
        jmxControl.loadAttributeValue(attribute);
        attrDc.replaceItem(attribute);
    }

    protected void initOperationsLayout() {
        ManagedBeanInfo mbean = getEditedEntity();
        if (CollectionUtils.isEmpty(mbean.getOperations())) {
            Span span = uiComponents.create(Span.class);
            span.setText(messageBundle.getMessage("mbean.operations.none"));
            operations.add(span);
        } else {
            mbean.getOperations().forEach(managedBeanOperation -> {
                MBeanOperationComposite mBeanOperationComposite = uiComponents.create(MBeanOperationComposite.class);
                mBeanOperationComposite.setOperation(managedBeanOperation);
                operations.add(mBeanOperationComposite);
            });
        }
    }
}
