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

package io.jmix.dynattrflowui.view.localization;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import io.jmix.core.CoreProperties;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.entity.EntityValues;
import io.jmix.dynattr.MsgBundleTools;
import io.jmix.dynattrflowui.impl.model.AttributeLocalizedValue;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.data.grid.ContainerDataGridItems;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.DataComponents;
import io.jmix.flowui.model.DataContext;

import javax.annotation.Nullable;
import java.util.*;

public class AttributeLocalizationComponent extends Composite<VerticalLayout> {

    protected static final String NAME_PROPERTY = "name";
    protected static final String DESCRIPTION_PROPERTY = "description";
    protected static final String LANG_PROPERTY = "language";

    protected final CoreProperties coreProperties;
    protected final MsgBundleTools msgBundleTools;
    protected final Metadata metadata;
    protected final Messages messages;
    protected final MessageTools messageTools;
    protected final UiComponents uiComponents;
    protected final DataComponents dataComponents;
    protected final DataContext dataContext;

    protected CollectionLoader<AttributeLocalizedValue> localizedValuesDl;
    protected CollectionContainer<AttributeLocalizedValue> localizedValuesDc;
    protected Grid<AttributeLocalizedValue> localizedValuesDataGrid;

    protected List<AttributeLocalizedValue> localizedValues = new ArrayList<>();

    protected boolean isEnabled = true;

    public AttributeLocalizationComponent(CoreProperties coreProperties,
                                          MsgBundleTools msgBundleTools,
                                          Metadata metadata,
                                          Messages messages,
                                          MessageTools messageTools,
                                          UiComponents uiComponents,
                                          DataComponents dataComponents,
                                          DataContext dataContext) {
        this.coreProperties = coreProperties;
        this.msgBundleTools = msgBundleTools;
        this.metadata = metadata;
        this.messages = messages;
        this.messageTools = messageTools;
        this.uiComponents = uiComponents;
        this.dataComponents = dataComponents;
        this.dataContext = dataContext;

        initData();
        initComponentUi();
        loadLocalizedValues();
        setupFieldsLock();
    }

    @Override
    protected VerticalLayout initContent() {
        VerticalLayout content = super.initContent();
        content.add(localizedValuesDataGrid);
        content.setMargin(false);
        content.setPadding(false);
        return content;
    }

    private void initData() {
        localizedValuesDc = this.dataComponents.createCollectionContainer(AttributeLocalizedValue.class);
        localizedValuesDl = this.dataComponents.createCollectionLoader();
        localizedValuesDc.addItemChangeListener(e -> Optional.ofNullable(e.getItem()).ifPresent(val -> dataContext.setModified(val, true)));
        localizedValuesDl.setLoadDelegate(e -> localizedValues);
        localizedValuesDl.setContainer(localizedValuesDc);
    }

    private void initComponentUi() {
        localizedValuesDataGrid = new Grid<>(AttributeLocalizedValue.class, false);
        localizedValuesDataGrid.setDataProvider(new ContainerDataGridItems<>(localizedValuesDc));

        Grid.Column<AttributeLocalizedValue> languageColumn = localizedValuesDataGrid.addColumn(LANG_PROPERTY);
        languageColumn.setHeader(messageTools.getPropertyCaption(metadata.getClass(AttributeLocalizedValue.class), LANG_PROPERTY));
        languageColumn.setRenderer(new ComponentRenderer<>(item -> new Text(item.getLanguage() + "|" + item.getLocale())));

        Grid.Column<AttributeLocalizedValue> nameCol = localizedValuesDataGrid.addColumn(NAME_PROPERTY);
        nameCol.setHeader(messageTools.getPropertyCaption(metadata.getClass(AttributeLocalizedValue.class), NAME_PROPERTY));

        Grid.Column<AttributeLocalizedValue> descriptionCol = localizedValuesDataGrid.addColumn(DESCRIPTION_PROPERTY);
        descriptionCol.setHeader(messageTools.getPropertyCaption(metadata.getClass(AttributeLocalizedValue.class), DESCRIPTION_PROPERTY));

        Editor<AttributeLocalizedValue> editor = localizedValuesDataGrid.getEditor();
        editor.addSaveListener(e -> {
            dataContext.setModified(e.getItem(), true);
        });

        Grid.Column<AttributeLocalizedValue> editColumn = localizedValuesDataGrid.addComponentColumn(attributeLocalizedValue -> {
                    Button editButton = new Button(messages.getMessage("actions.Edit"));
                    editButton.setEnabled(true);
                    editButton.addClickListener(e -> {
                        if (editor.isOpen()) {
                            editor.cancel();
                        }
                        localizedValuesDataGrid.getEditor().editItem(attributeLocalizedValue);
                    });
                    editButton.setEnabled(true);
                    return editButton;
                })
                .setFlexGrow(0);

        Binder<AttributeLocalizedValue> binder = new Binder<>(AttributeLocalizedValue.class);
        editor.setBinder(binder);
        editor.setBuffered(true);

        TextField nameField = new TextField();
        nameField.setWidthFull();

        binder.forField(nameField).bind(AttributeLocalizedValue::getName, AttributeLocalizedValue::setName);
        nameCol.setEditorComponent(nameField);

        TextField descriptionField = new TextField();
        descriptionField.setWidthFull();
        binder.forField(descriptionField)
                .bind(AttributeLocalizedValue::getDescription, AttributeLocalizedValue::setDescription);
        descriptionCol.setEditorComponent(descriptionField);


        Button saveButton = new Button(VaadinIcon.CHECK.create(), e -> editor.save());
        Button cancelButton = new Button(VaadinIcon.CLOSE.create(), e -> editor.cancel());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);
        saveButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SUCCESS);
        HorizontalLayout actions = new HorizontalLayout(saveButton, cancelButton);
        actions.setPadding(false);
        editColumn.setEditorComponent(actions);
    }

    protected void loadLocalizedValues() {
        for (Locale locale : coreProperties.getAvailableLocales()) {
            String localeCode = locale.toLanguageTag();
            AttributeLocalizedValue localizedValue = getAttributeLocalizedValue(localeCode);
            if (localizedValue == null) {
                localizedValue = createAttributeLocalizedValue(localeCode);
            }

            if (localizedValue.getLanguage() == null) {
                localizedValue.setLanguage(messageTools.getLocaleDisplayName(locale));
            }
        }
        localizedValuesDl.load();
    }

    protected void addAttributeLocalizedValue(String locale, String propertyName, String value) {
        AttributeLocalizedValue localizedValue = getAttributeLocalizedValue(locale);
        if (localizedValue == null) {
            localizedValue = createAttributeLocalizedValue(locale);
        }
        EntityValues.setValue(localizedValue, propertyName, value);
    }

    protected AttributeLocalizedValue getAttributeLocalizedValue(String locale) {
        return localizedValues.stream()
                .filter(localizedValue -> Objects.equals(locale, localizedValue.getLocale()))
                .findFirst()
                .orElse(null);
    }

    protected AttributeLocalizedValue createAttributeLocalizedValue(String locale) {
        AttributeLocalizedValue localizedValue = metadata.create(AttributeLocalizedValue.class);
        localizedValue.setLocale(locale);
        localizedValues.add(localizedValue);
        return localizedValue;
    }

    protected void setMsgBundle(@Nullable String msgBundle, String propertyName) {
        if (msgBundle != null) {
            Map<String, String> msgBundleValues = msgBundleTools.getMsgBundleValues(msgBundle);
            for (Map.Entry<String, String> entry : msgBundleValues.entrySet()) {
                addAttributeLocalizedValue(entry.getKey(), propertyName, entry.getValue());
            }
        }
    }

    protected String getMsgBundle(String propertyName) {
        Properties properties = new Properties();

        for (AttributeLocalizedValue localizedValue : localizedValuesDc.getItems()) {
            String value = EntityValues.getValue(localizedValue, propertyName);
            if (value != null) {
                properties.put(localizedValue.getLocale(), value);
            }
        }

        return msgBundleTools.getMsgBundle(properties);
    }

    protected void setupFieldsLock() {
        localizedValuesDataGrid.setEnabled(this.isEnabled);
    }

    public void setNameMsgBundle(@Nullable String nameMsgBundle) {
        localizedValues.clear();
        loadLocalizedValues();
        setMsgBundle(nameMsgBundle, NAME_PROPERTY);
        localizedValuesDataGrid.getDataProvider().refreshAll();
    }

    public void setDescriptionMsgBundle(String descriptionMsgBundle) {
        setMsgBundle(descriptionMsgBundle, DESCRIPTION_PROPERTY);
        localizedValuesDl.load();
    }

    public void removeDescriptionColumn() {
        localizedValuesDataGrid.removeColumn(localizedValuesDataGrid.getColumnByKey(DESCRIPTION_PROPERTY));
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public String getNameMsgBundle() {
        return getMsgBundle(NAME_PROPERTY);
    }

    public String getDescriptionMsgBundle() {
        return getMsgBundle(DESCRIPTION_PROPERTY);
    }

}
