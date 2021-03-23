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

package io.jmix.dashboardsui.screen.parameter.fragment;

import io.jmix.core.MetadataTools;
import io.jmix.dashboards.model.parameter.type.EnumParameterValue;
import io.jmix.dashboards.model.parameter.type.ParameterValue;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;

@UiController("dshbrd_EnumValue.fragment")
@UiDescriptor("enum-value-fragment.xml")
public class EnumValueFragment extends ScreenFragment implements ValueFragment {
    @Autowired
    protected ComboBox<Class> enumClassComboBox;

    @Autowired
    private MetadataTools metadataTools;

    @Subscribe
    public void onInit(InitEvent event) {
        MapScreenOptions options = (MapScreenOptions) event.getOptions();
        Map<String, Object> params = options.getParams();

        loadEnumClasses();
        selectIfExist((EnumParameterValue) params.get(VALUE));
    }

    @Override
    public ParameterValue getValue() {
        Class value = enumClassComboBox.getValue();
        return new EnumParameterValue(value == null ? null : value.getName());
    }

    protected void loadEnumClasses() {
        List<Class> allEnums = new ArrayList<>(metadataTools.getAllEnums());
        enumClassComboBox.setOptionsList(allEnums);
    }

    protected void selectIfExist(EnumParameterValue enumValue) {
        if (enumValue == null || isBlank(enumValue.getValue())) {
            return;
        }

        String className = enumValue.getValue();

        enumClassComboBox.getOptions().getOptions()
                .filter(clazz -> className.equals(clazz.getName()))
                .findFirst()
                .ifPresent(enumClass -> enumClassComboBox.setValue(enumClass));

    }
}
