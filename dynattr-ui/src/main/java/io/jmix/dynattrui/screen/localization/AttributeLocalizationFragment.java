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

package io.jmix.dynattrui.screen.localization;

import io.jmix.core.CoreProperties;
import io.jmix.core.LoadContext;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.entity.EntityValues;
import io.jmix.dynattrui.MsgBundleTools;
import io.jmix.dynattrui.impl.model.AttributeLocalizedValue;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.Install;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.Target;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

@UiController("jmix_AttributeLocalizationFragment")
@UiDescriptor("attribute-localization-fragment.xml")
public class AttributeLocalizationFragment extends ScreenFragment {

    protected static final String NAME_PROPERTY = "name";
    protected static final String DESCRIPTION_PROPERTY = "description";

    @Inject
    protected CoreProperties coreProperties;
    @Inject
    protected MsgBundleTools msgBundleTools;
    @Inject
    protected Metadata metadata;
    @Inject
    protected Messages messages;

    @Inject
    protected CollectionLoader<AttributeLocalizedValue> localizedValuesDl;
    @Inject
    protected CollectionContainer<AttributeLocalizedValue> localizedValuesDc;
    @Inject
    protected DataGrid<AttributeLocalizedValue> localizedValuesDataGrid;

    protected List<AttributeLocalizedValue> localizedValues = new ArrayList<>();

    protected boolean descriptionColumnVisible = false;

    public void setNameMsgBundle(String nameMsgBundle) {
        localizedValues.clear();
        loadLocalizedValues();
        setMsgBundle(nameMsgBundle, NAME_PROPERTY);
        localizedValuesDl.load();
    }

    public void setDescriptionMsgBundle(String descriptionMsgBundle) {
        setMsgBundle(descriptionMsgBundle, DESCRIPTION_PROPERTY);
        descriptionColumnVisible = true;
    }

    public String getNameMsgBundle() {
        return getMsgBundle(NAME_PROPERTY);
    }

    public String getDescriptionMsgBundle() {
        return getMsgBundle(DESCRIPTION_PROPERTY);
    }

    @Subscribe
    protected void onInit(InitEvent event) {
        loadLocalizedValues();
        initLocalizedValuesDataGrid();
    }

    @Install(to = "localizedValuesDl", target = Target.DATA_LOADER)
    protected List<AttributeLocalizedValue> localizedValuesDlLoadDelegate(LoadContext<AttributeLocalizedValue> loadContext) {
        return localizedValues;
    }

    @Install(to = "localizedValuesDataGrid.language", subject = "columnGenerator")
    protected String localizedValuesDataGridLanguageColumnGenerator(DataGrid.ColumnGeneratorEvent<AttributeLocalizedValue> event) {
        AttributeLocalizedValue localizedValue = event.getItem();
        return localizedValue.getLanguage() + "|" + localizedValue.getLocale();
    }

    @Install(to = "localizedValuesDataGrid", subject = "rowDescriptionProvider")
    protected String localizedValuesDataGridRowDescriptionProvider(AttributeLocalizedValue localizedValue) {
        return messages.getMessage(AttributeLocalizationFragment.class, "localizedValuesDataGrid.columnDescription");
    }

    protected void initLocalizedValuesDataGrid() {
        localizedValuesDataGrid.getColumn(DESCRIPTION_PROPERTY).setVisible(descriptionColumnVisible);
    }

    protected void loadLocalizedValues() {
        for (Map.Entry<String, Locale> entry : coreProperties.getAvailableLocales().entrySet()) {
            String locale = entry.getValue().toLanguageTag();
            AttributeLocalizedValue localizedValue = getAttributeLocalizedValue(locale);
            if (localizedValue == null) {
                localizedValue = createAttributeLocalizedValue(locale);
            }

            if (localizedValue.getLanguage() == null) {
                localizedValue.setLanguage(entry.getKey());
            }
        }
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

    protected void setMsgBundle(String msgBundle, String propertyName) {
        Map<String, String> msgBundleValues = msgBundleTools.getMsgBundleValues(msgBundle);
        for (Map.Entry<String, String> entry : msgBundleValues.entrySet()) {
            addAttributeLocalizedValue(entry.getKey(), propertyName, entry.getValue());
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
}
