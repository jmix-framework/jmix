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

package io.jmix.dynattrui.screen.categoryattr;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.jmix.core.AccessManager;
import io.jmix.core.CoreProperties;
import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.dynattr.MsgBundleTools;
import io.jmix.dynattrui.impl.model.AttributeLocalizedEnumValue;
import io.jmix.dynattrui.screen.localization.AttributeLocalizationFragment;
import io.jmix.ui.Fragments;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.component.*;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@UiController("dynat_AttributeEnumerationScreen")
@UiDescriptor("attribute-enumeration-screen.xml")
@DialogMode(forceDialog = true)
public class AttributeEnumerationScreen extends Screen {

    @Autowired
    protected Fragments fragments;
    @Autowired
    protected CoreProperties coreProperties;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MsgBundleTools msgBundleTools;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected Icons icons;

    @Autowired
    protected VBoxLayout localizationBox;
    @Autowired
    protected TextField<String> valueField;
    @Autowired
    protected CollectionLoader<AttributeLocalizedEnumValue> localizedEnumValuesDl;
    @Autowired
    protected CollectionContainer<AttributeLocalizedEnumValue> localizedEnumValuesDc;

    protected String enumeration;
    protected String enumerationLocales;
    protected AttributeLocalizationFragment localizationFragment;
    protected List<AttributeLocalizedEnumValue> localizedEnumValues = new ArrayList<>();
    @Autowired
    private AccessManager accessManager;

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
                .filter(enumValue -> enumValue.getLocalizedValues() != null)
                .map(AttributeLocalizedEnumValue::getLocalizedValues)
                .collect(Collectors.joining()));
    }

    @Subscribe
    protected void onAfterShow(AfterShowEvent event) {
        initLocalizedEnumValuesDataGrid();
        initLocalizationFragment();
        localizedEnumValuesDl.load();
    }

    @Install(to = "localizedEnumValuesDl", target = Target.DATA_LOADER)
    protected List<AttributeLocalizedEnumValue> localizedEnumValuesDlLoadDelegate(LoadContext<AttributeLocalizedEnumValue> loadContext) {
        return localizedEnumValues;
    }

    @Subscribe("valueField")
    protected void onValueFieldEnterPress(TextInputField.EnterPressEvent event) {
        addEnumerationValue((String) event.getSource().getValue());
    }

    @Subscribe("localizedEnumValuesDataGrid.add")
    protected void onLocalizedEnumValuesDataGridAdd(Action.ActionPerformedEvent event) {
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

    @Install(to = "localizedEnumValuesDataGrid.removeItem", subject = "columnGenerator")
    protected LinkButton localizedEnumValuesDataGridRemoveItemColumnGenerator(DataGrid.ColumnGeneratorEvent<AttributeLocalizedEnumValue> event) {
        LinkButton linkButton = uiComponents.create(LinkButton.class);
        Action removeAction = new BaseAction("remove_item_" + event.getItem().getValue())
                .withHandler(actionPerformedEvent -> {
                    localizedEnumValues.remove(event.getItem());
                    localizedEnumValuesDl.load();
                })
                .withIcon(icons.get(JmixIcon.REMOVE));
        linkButton.setAction(removeAction);
        return linkButton;
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
                localizationFragment.getFragment().setEnabled(true);
            } else {
                localizationFragment.setNameMsgBundle(null);
                localizationFragment.getFragment().setEnabled(false);
            }
        }
    }

    @Subscribe("commitBtn")
    protected void onCommitBtnClick(Button.ClickEvent event) {
        if (localizationFragment != null) {
            AttributeLocalizedEnumValue lastSelectedValue = localizedEnumValuesDc.getItemOrNull();
            if (lastSelectedValue != null) {
                String localizedValues = msgBundleTools.convertMsgBundleToEnumMsgBundle(lastSelectedValue.getValue(), localizationFragment.getNameMsgBundle());
                lastSelectedValue.setLocalizedValues(localizedValues);
            }
        }

        close(StandardOutcome.COMMIT);
    }

    @Subscribe("cancelBtn")
    protected void onCancelBtnClick(Button.ClickEvent event) {
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

            localizationFragment = fragments.create(this, AttributeLocalizationFragment.class);
            localizationFragment.setEnabled(crudEntityContext.isUpdatePermitted());

            Fragment fragment = localizationFragment.getFragment();
            fragment.setWidth(Component.FULL_SIZE);
            fragment.setHeight(Component.FULL_SIZE);
            fragment.setEnabled(false);
            localizationBox.add(fragment);
            localizationBox.expand(fragment);
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
