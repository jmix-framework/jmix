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

import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.dynattr.MsgBundleTools;
import io.jmix.dynattrflowui.impl.model.AttributeLocalizedValue;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ViewController("dynat_AttributeLocalizationFragment")
@ViewDescriptor("attribute-localization-fragment.xml")
public class AttributeLocalizationFragment extends StandardView {

    protected static final String NAME_PROPERTY = "name";
    protected static final String DESCRIPTION_PROPERTY = "description";

    @Autowired
    protected CoreProperties coreProperties;
    @Autowired
    protected MsgBundleTools msgBundleTools;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected Messages messages;
    @Autowired
    protected MessageTools messageTools;

    @ViewComponent
    protected CollectionLoader<AttributeLocalizedValue> localizedValuesDl;
    @ViewComponent
    protected CollectionContainer<AttributeLocalizedValue> localizedValuesDc;
    @ViewComponent
    protected DataGrid<AttributeLocalizedValue> localizedValuesDataGrid;

    protected List<AttributeLocalizedValue> localizedValues = new ArrayList<>();

    protected boolean descriptionColumnVisible = false;

    protected boolean isEnabled;

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public void setNameMsgBundle(@Nullable String nameMsgBundle) {
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
        setupFieldsLock();
    }

    @Install(to = "localizedValuesDl", target = Target.DATA_LOADER)
    protected List<AttributeLocalizedValue> localizedValuesDlLoadDelegate(LoadContext<AttributeLocalizedValue> loadContext) {
        return localizedValues;
    }

//  todo  @Install(to = "localizedValuesDataGrid.language", subject = "columnGenerator")
//    protected String localizedValuesDataGridLanguageColumnGenerator(DataGrid.ColumnGeneratorEvent<AttributeLocalizedValue> event) {
//        AttributeLocalizedValue localizedValue = event.getItem();
//        return localizedValue.getLanguage() + "|" + localizedValue.getLocale();
//    }

    @Install(to = "localizedValuesDataGrid", subject = "rowDescriptionProvider")
    protected String localizedValuesDataGridRowDescriptionProvider(AttributeLocalizedValue localizedValue) {
        return messages.getMessage(AttributeLocalizationFragment.class, "localizedValuesDataGrid.columnDescription");
    }

    protected void initLocalizedValuesDataGrid() {
        localizedValuesDataGrid.getColumnByKey(DESCRIPTION_PROPERTY).setVisible(descriptionColumnVisible);
    }

    protected void setupFieldsLock() {
        localizedValuesDataGrid.setEnabled(this.isEnabled);
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
}
