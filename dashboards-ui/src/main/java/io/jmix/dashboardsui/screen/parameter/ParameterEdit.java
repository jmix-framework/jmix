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
package io.jmix.dashboardsui.screen.parameter;

import io.jmix.core.common.util.ParamsMap;
import io.jmix.dashboards.model.parameter.Parameter;
import io.jmix.dashboards.model.parameter.ParameterType;
import io.jmix.dashboards.model.parameter.type.*;
import io.jmix.dashboardsui.screen.parameter.fragment.*;
import io.jmix.ui.Fragments;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.VBoxLayout;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Map;

import static io.jmix.dashboards.model.parameter.ParameterType.*;


@UiController("dshbrd_Parameter.edit")
@UiDescriptor("parameter-edit.xml")
@EditedEntityContainer("parameterDc")
public class ParameterEdit extends StandardEditor<Parameter> {

    @Autowired
    protected InstanceContainer<Parameter> parameterDc;
    @Autowired
    protected ComboBox<ParameterType> typeComboBox;
    @Autowired
    protected VBoxLayout valueBox;
    @Autowired
    protected Fragments fragments;

    protected ValueFragment valueFragment;

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        initParameter();
        typeComboBox.addValueChangeListener(e -> parameterTypeChanged(e.getValue()));
    }

    @Subscribe(target = Target.DATA_CONTEXT)
    protected void preCommit(DataContext.PreCommitEvent event) {
        ParameterValue parameterValue = valueFragment == null ? null : valueFragment.getValue();
        getEditedEntity().setValue(parameterValue);
    }

    protected void initParameter() {
        ParameterValue parameterValue = parameterDc.getItem().getValue();
        valueBox.removeAll();
        if (parameterValue instanceof EntityParameterValue) {
            typeComboBox.setValue(ENTITY);
            valueFragment = openEntityValueFragment((EntityParameterValue) parameterValue);
        } else if (parameterValue instanceof EntityListParameterValue) {
            typeComboBox.setValue(ENTITY_LIST);
            valueFragment = openEntitiesListValueFragment((EntityListParameterValue) parameterValue);
        } else if (parameterValue instanceof EnumParameterValue) {
            typeComboBox.setValue(ENUM);
            valueFragment = openEnumValueFragment((EnumParameterValue) parameterValue);
        } else if (parameterValue instanceof DateParameterValue) {
            typeComboBox.setValue(DATE);
            valueFragment = openSimpleValueFragment(DATE, parameterValue);
        } else if (parameterValue instanceof DateTimeParameterValue) {
            typeComboBox.setValue(DATETIME);
            valueFragment = openSimpleValueFragment(DATETIME, parameterValue);
        } else if (parameterValue instanceof TimeParameterValue) {
            typeComboBox.setValue(TIME);
            valueFragment = openSimpleValueFragment(TIME, parameterValue);
        } else if (parameterValue instanceof UuidParameterValue) {
            typeComboBox.setValue(UUID);
            valueFragment = openSimpleValueFragment(UUID, parameterValue);
        } else if (parameterValue instanceof IntegerParameterValue) {
            typeComboBox.setValue(INTEGER);
            valueFragment = openSimpleValueFragment(INTEGER, parameterValue);
        } else if (parameterValue instanceof LongParameterValue) {
            typeComboBox.setValue(LONG);
            valueFragment = openSimpleValueFragment(LONG, parameterValue);
        } else if (parameterValue instanceof StringParameterValue) {
            typeComboBox.setValue(STRING);
            valueFragment = openSimpleValueFragment(STRING, parameterValue);
        } else if (parameterValue instanceof DecimalParameterValue) {
            typeComboBox.setValue(DECIMAL);
            valueFragment = openSimpleValueFragment(DECIMAL, parameterValue);
        } else if (parameterValue instanceof BooleanParameterValue) {
            typeComboBox.setValue(BOOLEAN);
            valueFragment = openSimpleValueFragment(BOOLEAN, parameterValue);
        } else {
            typeComboBox.setValue(null);
        }
    }

    protected void parameterTypeChanged(ParameterType type) {
        valueBox.removeAll();
        switch (type) {
            case ENTITY_LIST:
                valueFragment = openEntitiesListValueFragment(new EntityListParameterValue());
                break;
            case ENTITY:
                valueFragment = openEntityValueFragment(new EntityParameterValue());
                break;
            case ENUM:
                valueFragment = openEnumValueFragment(new EnumParameterValue());
                break;
            case DATETIME:
            case TIME:
            case DATE:
            case DECIMAL:
            case INTEGER:
            case LONG:
            case STRING:
            case BOOLEAN:
            case UUID:
                valueFragment = openSimpleValueFragment(type, null);
                break;
            default:
                valueFragment = null;
                valueBox.removeAll();
                break;
        }
    }

    protected SimpleValueFragment openSimpleValueFragment(ParameterType type, @Nullable ParameterValue parameterValue) {
        Map<String, Object> params = ParamsMap.of()
                .pair(ValueFragment.VALUE_TYPE, type)
                .pair(ValueFragment.VALUE, parameterValue)
                .create();

        SimpleValueFragment fragment = (SimpleValueFragment) fragments.create(
                this,
                SimpleValueFragment.class,
                new MapScreenOptions(params)
        ).init();
        valueBox.add(fragment.getFragment());
        return fragment;
    }

    protected EnumValueFragment openEnumValueFragment(EnumParameterValue value) {
        EnumValueFragment fragment = (EnumValueFragment) fragments.create(
                this,
                EnumValueFragment.class,
                new MapScreenOptions(ParamsMap.of(ValueFragment.VALUE, value))
        ).init();
        valueBox.add(fragment.getFragment());
        return fragment;
    }

    protected EntityValueFragment openEntityValueFragment(EntityParameterValue value) {
        EntityValueFragment fragment = (EntityValueFragment) fragments.create(
                this,
                EntityValueFragment.class,
                new MapScreenOptions(ParamsMap.of(ValueFragment.VALUE, value))
        ).init();
        valueBox.add(fragment.getFragment());
        return fragment;
    }

    protected EntityListValueFragment openEntitiesListValueFragment(EntityListParameterValue value) {
        EntityListValueFragment fragment = (EntityListValueFragment) fragments.create(
                this,
                EntityListValueFragment.class,
                new MapScreenOptions(ParamsMap.of(ValueFragment.VALUE, value))
        ).init();
        valueBox.add(fragment.getFragment());
        return fragment;
    }
}