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

package io.jmix.dynattrflowui.view.categoryattr;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import io.jmix.core.*;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.dynattr.MsgBundleTools;
import io.jmix.dynattrflowui.impl.model.AttributeLocalizedEnumValue;
import io.jmix.dynattrflowui.view.localization.AttributeLocalizationComponent;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.editor.DataGridEditor;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.DataComponents;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@ViewController("dynat_AttributeEnumerationDetailView")
@ViewDescriptor("attribute-enumeration-detail-view.xml")
@DialogMode(width = "30em", resizable = true)
public class AttributeEnumerationDetailView extends StandardView {

    @Autowired
    protected CoreProperties coreProperties;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MsgBundleTools msgBundleTools;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected Messages messages;
    @Autowired
    protected DataComponents dataComponents;
    @Autowired
    protected MessageTools messageTools;

    @ViewComponent
    protected VerticalLayout localizationBox;
    @ViewComponent
    protected TypedTextField<String> valueField;
    @ViewComponent
    protected CollectionLoader<AttributeLocalizedEnumValue> localizedEnumValuesDl;
    @ViewComponent
    protected CollectionContainer<AttributeLocalizedEnumValue> localizedEnumValuesDc;
    @ViewComponent
    protected DataGrid<AttributeLocalizedEnumValue> localizedEnumValuesDataGrid;

    protected String enumeration;
    protected String enumerationLocales;
    protected AttributeLocalizationComponent localizationFragment;
    protected List<AttributeLocalizedEnumValue> localizedEnumValues = new ArrayList<>();

    public void setEnumeration(String enumeration) {
        this.enumeration = enumeration;
    }

    public void setEnumerationLocales(String enumerationLocales) {
        this.enumerationLocales = enumerationLocales;
    }

    public String getEnumeration() {
        return Strings.emptyToNull(localizedEnumValuesDc.getItems()
                .stream()
                .map(AttributeLocalizedEnumValue::getValue)
                .collect(Collectors.joining(",")));
    }

    public String getEnumerationLocales() {
        return Strings.emptyToNull(localizedEnumValuesDc.getItems()
                .stream()
                .map(AttributeLocalizedEnumValue::getLocalizedValues)
                .filter(Objects::nonNull)
                .collect(Collectors.joining()));
    }

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        initLocalizedEnumValuesDataGrid();
        initLocalizationFragment();
        localizedEnumValuesDl.load();
    }

    @Install(to = "localizedEnumValuesDl", target = Target.DATA_LOADER)
    protected List<AttributeLocalizedEnumValue> localizedEnumValuesDlLoadDelegate(LoadContext<AttributeLocalizedEnumValue> loadContext) {
        return localizedEnumValues;
    }

    @Subscribe("valueField")
    protected void onValueFieldEnterPress(HasValue.ValueChangeEvent<String> event) {
        addEnumerationValue(event.getValue());
    }

    @Subscribe("localizedEnumValuesDataGrid.add")
    protected void onLocalizedEnumValuesDataGridAdd(ActionPerformedEvent event) {
        addEnumerationValue(valueField.getValue());
        valueField.focus();
    }

    protected void addEnumerationValue(String value) {
        if (!Strings.isNullOrEmpty(value) && !valueExists(value)) {
            AttributeLocalizedEnumValue localizedEnumValue = createAttributeLocalizedEnumValue(value);
            localizedEnumValues.add(localizedEnumValue);
            localizedEnumValuesDl.load();
            valueField.clear();
        }
    }

    @Supply(to = "localizedEnumValuesDataGrid.bufferedEditorColumn", subject = "editorComponent")
    protected Component localizedEnumValuesDataGridEditorComponent() {
        DataGridEditor<AttributeLocalizedEnumValue> editor = localizedEnumValuesDataGrid.getEditor();

        JmixButton saveButton = createEditorSaveButton(editor);
        JmixButton cancelButton = createEditorCancelButton(editor);

        HorizontalLayout editorButtonsLayout = createEditorButtonsLayout();
        editorButtonsLayout.add(saveButton, cancelButton);

        return editorButtonsLayout;
    }

    protected JmixButton createEditorSaveButton(DataGridEditor<AttributeLocalizedEnumValue> editor) {
        JmixButton saveButton = uiComponents.create(JmixButton.class);
        saveButton.setIcon(VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        saveButton.addClickListener(__ -> editor.save());

        return saveButton;
    }

    protected JmixButton createEditorCancelButton(DataGridEditor<AttributeLocalizedEnumValue> editor) {
        JmixButton cancelButton = uiComponents.create(JmixButton.class);
        cancelButton.setIcon(VaadinIcon.CLOSE.create());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        cancelButton.addClickListener(__ -> editor.cancel());

        return cancelButton;
    }

    @Supply(to = "localizedEnumValuesDataGrid.bufferedEditorColumn", subject = "renderer")
    protected Renderer<AttributeLocalizedEnumValue> localizedEnumValuesDataGridEditorRenderer() {
        return new ComponentRenderer<>(this::createEditorColumnRenderer);
    }

    protected Component createEditorColumnRenderer(AttributeLocalizedEnumValue currentItem) {
        DataGridEditor<AttributeLocalizedEnumValue> editor = localizedEnumValuesDataGrid.getEditor();

        JmixButton editButton = createEditorEditButton(editor, currentItem);
        JmixButton removeButton = createEditorRemoveButton(currentItem);

        HorizontalLayout editorButtonsLayout = createEditorButtonsLayout();
        editorButtonsLayout.add(editButton, removeButton);

        return editorButtonsLayout;
    }

    protected JmixButton createEditorEditButton(DataGridEditor<AttributeLocalizedEnumValue> editor,
                                                AttributeLocalizedEnumValue currentItem) {
        JmixButton editorButton = uiComponents.create(JmixButton.class);
        editorButton.setIcon(VaadinIcon.PENCIL.create());

        editorButton.addClickListener(__ -> {
            if (editor.isOpen()) {
                editor.cancel();
            }
            editor.editItem(currentItem);
        });

        return editorButton;
    }

    protected JmixButton createEditorRemoveButton(AttributeLocalizedEnumValue currentItem) {
        JmixButton removeButton = uiComponents.create(JmixButton.class);
        removeButton.setIcon(VaadinIcon.TRASH.create());
        removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        removeButton.addClickListener(__ -> {
            localizedEnumValues.remove(currentItem);
            localizedEnumValuesDl.load();
        });

        return removeButton;
    }

    protected HorizontalLayout createEditorButtonsLayout() {
        HorizontalLayout horizontalLayout = uiComponents.create(HorizontalLayout.class);
        horizontalLayout.setPadding(false);
        return horizontalLayout;
    }

    protected void gradeRemoveItemColumnUpdater(JmixButton button, AttributeLocalizedEnumValue customer) {
        Action removeAction = new BaseAction("remove_item_" + customer.getValue())
                .withHandler(actionPerformedEvent -> {
                    localizedEnumValues.remove(customer);
                    localizedEnumValuesDl.load();
                })
                .withVariant(ActionVariant.DANGER)
                .withIcon(VaadinIcon.TRASH.create());
        button.setAction(removeAction);
    }

    @Subscribe(id = "localizedEnumValuesDc", target = Target.DATA_CONTAINER)
    protected void onLocalizedEnumValuesDcItemChange(InstanceContainer.ItemChangeEvent<AttributeLocalizedEnumValue> event) {
        AttributeLocalizedEnumValue prevLocalizedEnumValue = event.getPrevItem();
        if (prevLocalizedEnumValue != null && localizationFragment != null) {
            String localizedValues = msgBundleTools.convertMsgBundleToEnumMsgBundle(prevLocalizedEnumValue.getValue(), localizationFragment.getNameMsgBundle());
            prevLocalizedEnumValue.setLocalizedValues(localizedValues);
        }
        if (localizationFragment != null) {
            AttributeLocalizedEnumValue localizedEnumValue = event.getItem();
            if (localizedEnumValue != null) {
                String localizedValues = msgBundleTools.convertEnumMsgBundleToMsgBundle(localizedEnumValue.getLocalizedValues());
                localizationFragment.setNameMsgBundle(localizedValues);
                localizationFragment.setEnabled(true);
            } else {
                localizationFragment.setNameMsgBundle(null);
                localizationFragment.setEnabled(false);
            }
        }
    }

    @Subscribe("commitBtn")
    protected void onCommitBtnClick(ClickEvent<Button> event) {
        if (localizationFragment != null) {
            AttributeLocalizedEnumValue lastSelectedValue = localizedEnumValuesDc.getItemOrNull();
            if (lastSelectedValue != null) {
                String localizedValues = msgBundleTools.convertMsgBundleToEnumMsgBundle(lastSelectedValue.getValue(), localizationFragment.getNameMsgBundle());
                lastSelectedValue.setLocalizedValues(localizedValues);
            }
        }

        close(StandardOutcome.SAVE);
    }

    @Subscribe("cancelBtn")
    protected void onCancelBtnClick(ClickEvent<Button> event) {
        close(StandardOutcome.CLOSE);
    }

    protected void initLocalizedEnumValuesDataGrid() {
        if (!Strings.isNullOrEmpty(enumeration)) {
            List<String> enumValues = Lists.newArrayList(Splitter.on(",").omitEmptyStrings().split(enumeration));
            Map<String, String> enumMsgBundleValues = msgBundleTools.getEnumMsgBundleValues(enumerationLocales);

            for (String enumValue : enumValues) {
                AttributeLocalizedEnumValue localizedEnumValue = createAttributeLocalizedEnumValue(enumValue);
                localizedEnumValue.setLocalizedValues(enumMsgBundleValues.get(localizedEnumValue.getValue()));
                localizedEnumValues.add(localizedEnumValue);
            }
        }
    }

    protected void initLocalizationFragment() {
        if (coreProperties.getAvailableLocales().size() > 1) {
            localizationBox.setVisible(true);

            CrudEntityContext crudEntityContext = new CrudEntityContext(localizedEnumValuesDc.getEntityMetaClass());
            accessManager.applyRegisteredConstraints(crudEntityContext);

            localizationFragment = new AttributeLocalizationComponent(coreProperties,
                    msgBundleTools,
                    metadata,
                    messages,
                    messageTools,
                    uiComponents,
                    dataComponents,
                    getViewData().getDataContext());
            localizationFragment.setEnabled(crudEntityContext.isUpdatePermitted());
            localizationFragment.removeDescriptionColumn();

            localizationFragment.setEnabled(false);
            localizationBox.add(localizationFragment);
            localizationBox.expand(localizationFragment);
        }
    }

    protected AttributeLocalizedEnumValue createAttributeLocalizedEnumValue(String value) {
        AttributeLocalizedEnumValue localizedEnumValue = metadata.create(AttributeLocalizedEnumValue.class);
        localizedEnumValue.setValue(value);
        return localizedEnumValue;
    }

    protected boolean valueExists(String value) {
        return localizedEnumValues.stream()
                .anyMatch(localizedEnumValue -> Objects.equals(value, localizedEnumValue.getValue()));
    }

    protected AttributeLocalizedEnumValue getLocalizedEnumValue(AttributeLocalizedEnumValue enumValue) {
        return localizedEnumValues.stream()
                .filter(localizedEnumValue -> Objects.equals(localizedEnumValue, enumValue))
                .findFirst()
                .orElse(null);
    }
}
