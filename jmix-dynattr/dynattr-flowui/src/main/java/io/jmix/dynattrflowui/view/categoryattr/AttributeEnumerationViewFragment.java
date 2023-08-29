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
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.core.AccessManager;
import io.jmix.core.CoreProperties;
import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.dynattr.MsgBundleTools;
import io.jmix.dynattrflowui.impl.model.AttributeLocalizedEnumValue;
import io.jmix.dynattrflowui.view.localization.AttributeLocalizationViewFragment;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.Views;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ViewController("dynat_AttributeEnumerationViewFragment")
@ViewDescriptor("attribute-enumeration-view-fragment.xml")
@DialogMode() //todo forceDialog = true
public class AttributeEnumerationViewFragment extends StandardView {

    @Autowired
    protected CoreProperties coreProperties;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MsgBundleTools msgBundleTools;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    private AccessManager accessManager;
    @Autowired
    private Views views;

    @ViewComponent
    protected VerticalLayout localizationBox;
    @ViewComponent
    protected TypedTextField<String> valueField;
    @ViewComponent
    protected CollectionLoader<AttributeLocalizedEnumValue> localizedEnumValuesDl;
    @ViewComponent
    protected CollectionContainer<AttributeLocalizedEnumValue> localizedEnumValuesDc;

    protected String enumeration;
    protected String enumerationLocales;
    protected AttributeLocalizationViewFragment localizationFragment;
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

//   @Install(to = "localizedEnumValuesDataGrid.removeItem", subject = "columnGenerator")
//    protected JmixButton localizedEnumValuesDataGridRemoveItemColumnGenerator(DataGrid.ColumnGeneratorEvent<AttributeLocalizedEnumValue> event) {
//        JmixButton linkButton = uiComponents.create(JmixButton.class);
//        Action removeAction = new BaseAction("remove_item_" + event.getItem().getValue())
//                .withHandler(actionPerformedEvent -> {
//                    localizedEnumValues.remove(event.getItem());
//                    localizedEnumValuesDl.load();
//                })
//                .withIcon(VaadinIcon.CLIPBOARD_CROSS.create());
//        linkButton.setAction(removeAction);
//        return linkButton;
//    }

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

            localizationFragment = views.create(AttributeLocalizationViewFragment.class);
            localizationFragment.setEnabled(crudEntityContext.isUpdatePermitted());

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
