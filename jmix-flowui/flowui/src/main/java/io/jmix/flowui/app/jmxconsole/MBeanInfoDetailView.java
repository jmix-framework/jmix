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

package io.jmix.flowui.app.jmxconsole;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Route;
import io.jmix.core.MessageTools;
import io.jmix.core.Metadata;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.app.jmxconsole.model.ManagedBeanAttribute;
import io.jmix.flowui.app.jmxconsole.model.ManagedBeanInfo;
import io.jmix.flowui.component.details.JmixDetails;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "system/mbeaninfo", layout = DefaultMainViewParent.class)
@ViewController("ui_ManagedBeanInfo.detail")
@ViewDescriptor("mbean-detail-view.xml")
@EditedEntityContainer("mbeanDc")
public class MBeanInfoDetailView extends StandardDetailView<ManagedBeanInfo> {
    @ViewComponent
    protected DataGrid<ManagedBeanAttribute> attributesDataGrid;
    @ViewComponent
    protected CollectionContainer<ManagedBeanAttribute> attrDc;
    @ViewComponent
    protected VerticalLayout operations;
    @ViewComponent
    protected Span mbeanClassName;
    @ViewComponent
    protected Span mbeanDescription;
    @ViewComponent
    protected TypedTextField<String> operationsSearchField;
    @ViewComponent
    protected Span mbeanName;

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

    protected Icon searchIcon;

    @Override
    public void setEntityToEdit(ManagedBeanInfo entity) {
        super.setEntityToEdit(entity);
        initComponents();
    }

    protected void initComponents() {
        jmxControl.loadAttributes(getEditedEntity());

        initMbeanSpans();
        initOperationsLayout();
        initDatagridColumns();
        initSearchField();
    }

    protected void initSearchField() {
        createSearchIcon();
        operationsSearchField.setSuffixComponent(searchIcon);
        operationsSearchField.addKeyPressListener(Key.ENTER,
                keyPressEvent -> reloadOperations(operationsSearchField.getValue()));
    }

    protected void createSearchIcon() {
        searchIcon = new Icon(VaadinIcon.SEARCH);
        searchIcon.addClickListener(event -> reloadOperations(operationsSearchField.getValue()));
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

    protected void initDatagridColumns() {
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
    protected void findEntityId(BeforeEnterEvent event) {
        // Because DTO entity cannot be loaded by Id, we need to prevent Id parsing from route parameters
    }

    @Override
    public boolean hasUnsavedChanges() {
        return false;
    }

    @Install(to = "attributesDataGrid.edit", subject = "enabledRule")
    private boolean attributesDataGridEditEnabledRule() {
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

    protected void initMbeanSpans() {
        mbeanName.setText(messageBundle.formatMessage("mbean.name", getEditedEntity().getObjectName()));
        mbeanClassName.setText(messageBundle.formatMessage("mbean.className", getEditedEntity().getClassName()));
        mbeanDescription.setText(messageBundle.formatMessage("mbean.description", getEditedEntity().getDescription()));
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
