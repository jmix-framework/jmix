/*
 * Copyright 2021 Haulmont.
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

package com.haulmont.cuba.gui.app.core.appproperties;

import com.haulmont.chile.core.datatypes.DatatypeRegistry;
import com.haulmont.cuba.core.app.ConfigStorageService;
import com.haulmont.cuba.core.config.AppPropertyEntity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.Datasource;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.impl.BooleanDatatype;
import io.jmix.ui.WindowParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Controller of the {@code appproperties-edit.xml} screen
 */
public class AppPropertiesEdit extends AbstractWindow {

    private static final Logger log = LoggerFactory.getLogger(AppPropertiesEdit.class);

    @WindowParam
    private AppPropertyEntity item;

    @Autowired
    private Datasource<AppPropertyEntity> appPropertyDs;

    @Autowired
    private Label cannotEditValueLabel;

    @Autowired
    private Metadata metadata;

    @Autowired
    private FieldGroup fieldGroup;

    @Autowired
    private UiComponents uiComponents;

    @Autowired
    private UserSessionSource userSessionSource;

    @Autowired
    protected DatatypeRegistry datatypeRegistry;

    @Named("fieldGroup.displayedDefaultValue")
    protected TextField displayedDefaultValueField;

    @Override
    public void init(Map<String, Object> params) {
        cannotEditValueLabel.setVisible(item.getOverridden());

        Datatype datatype = item.getEnumValues() != null ?
                datatypeRegistry.getNN(String.class) : datatypeRegistry.get(item.getDataTypeName());

        fieldGroup.addCustomField("currentValue", (datasource, propertyId) -> {
            if (item.getOverridden()) {
                TextField<String> textField = uiComponents.create(TextField.NAME);
                textField.setValue(item.getDisplayedCurrentValue());
                textField.setEditable(false);
                return textField;
            }
            if (item.getEnumValues() != null) {
                return createLookupField(Arrays.asList(item.getEnumValues().split(",")), item.getCurrentValue());
            } else {
                if (datatype instanceof BooleanDatatype) {
                    return createLookupField(Arrays.asList(Boolean.TRUE.toString(), Boolean.FALSE.toString()), item.getCurrentValue());
                } else {
                    if (Boolean.TRUE.equals(item.getSecret())) {
                        PasswordField passwordField = uiComponents.create(PasswordField.class);
                        passwordField.setValue(item.getCurrentValue());
                        passwordField.addValueChangeListener(e -> {
                            appPropertyDs.getItem().setCurrentValue(e.getValue());
                        });
                        return passwordField;
                    } else {
                        io.jmix.ui.component.TextField<Object> textField = uiComponents.create(TextField.NAME);
                        textField.setValue(item.getCurrentValue());

                        try {
                            Object value = datatype.parse(item.getCurrentValue(), userSessionSource.getLocale());
                            textField.setDatatype(datatype);
                            if (value != null) {
                                item.setCurrentValue(value.toString());
                            }
                        } catch (ParseException e) {
                            // do not assign datatype then
                            log.trace("Localized parsing by datatype cannot be used for value {}", item.getCurrentValue());
                        }

                        textField.addValueChangeListener(e -> {
                            appPropertyDs.getItem().setCurrentValue(e.getValue() == null ? null : e.getValue().toString());
                        });
                        return textField;
                    }
                }
            }
        });

        Function<String, String> defaultValueFormatter = (value) -> {
            if (datatype instanceof BooleanDatatype) {
                return value;
            }

            try {
                Object parsedDefaultValue = datatype.parse(value);
                return datatype.format(parsedDefaultValue, userSessionSource.getLocale());
            } catch (ParseException e) {
                log.trace("Localized parsing by datatype cannot be used for value {}", value, e);
            }
            return value;
        };
        displayedDefaultValueField.setFormatter(defaultValueFormatter);

        appPropertyDs.setItem(metadata.getTools().copy(item));
    }

    private LookupField<String> createLookupField(List<String> values, String currentValue) {
        LookupField<String> lookupField = uiComponents.create(LookupField.NAME);
        lookupField.setOptionsList(values);
        lookupField.setValue(currentValue);
        lookupField.addValueChangeListener(e -> {
            appPropertyDs.getItem().setCurrentValue(e.getValue());
        });
        return lookupField;
    }

    public void ok() {
        AppPropertyEntity appPropertyEntity = appPropertyDs.getItem();

        // Save property through the client-side cache to ensure it is updated in the cache immediately
        ConfigStorageService configStorageService = AppBeans.get(ConfigStorageService.class);
//        ConfigStorageService configStorageService = ((ConfigurationClientImpl) configuration).getConfigStorageService();
        configStorageService.setDbProperty(appPropertyEntity.getName(), appPropertyEntity.getCurrentValue());

        close(COMMIT_ACTION_ID);
    }

    public void cancel() {
        close(CLOSE_ACTION_ID);
    }
}
