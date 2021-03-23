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
import io.jmix.ui.WindowParam;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.VBoxLayout;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import static io.jmix.dashboards.model.parameter.ParameterType.*;


@UiController("dshbrd_Parameter.edit")
@UiDescriptor("parameter-edit.xml")
@EditedEntityContainer("parameterDc")
public class ParameterEdit extends StandardEditor<Parameter> {

    protected final static String ITEM = "item";

    @Autowired
    protected InstanceContainer<Parameter> parameterDc;
    @Autowired
    protected ComboBox<ParameterType> typeLookup;
    @Autowired
    protected VBoxLayout valueBox;
    @Autowired
    protected Fragments fragments;

    @WindowParam(name = ITEM)
    protected Parameter parameter;

    protected ValueFragment valueFragment;

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        if (parameter != null) {
            parameterDc.setItem(parameter);
        }
        initParameter();
        typeLookup.addValueChangeListener(e -> parameterTypeChanged(e.getValue()));
    }

    @Subscribe(target = Target.DATA_CONTEXT)
    protected void preCommit(DataContext.PreCommitEvent event) {
        ParameterValue parameterValue = valueFragment == null ? null : valueFragment.getValue();
        getEditedEntity().setParameterValue(parameterValue);
    }

    protected void initParameter() {
        ParameterValue parameterValue = parameterDc.getItem().getParameterValue();
        valueBox.removeAll();
        if (parameterValue instanceof EntityParameterValue) {
            typeLookup.setValue(ENTITY);
            valueFragment = openEntityValueFragment((EntityParameterValue) parameterValue);
        } else if (parameterValue instanceof ListEntitiesParameterValue) {
            typeLookup.setValue(LIST_ENTITY);
            valueFragment = openEntitiesListValueFragment((ListEntitiesParameterValue) parameterValue);
        } else if (parameterValue instanceof EnumParameterValue) {
            typeLookup.setValue(ENUM);
            valueFragment = openEnumValueFragment((EnumParameterValue) parameterValue);
        } else if (parameterValue instanceof DateParameterValue) {
            typeLookup.setValue(DATE);
            valueFragment = openSimpleValueFragment(DATE, parameterValue);
        } else if (parameterValue instanceof DateTimeParameterValue) {
            typeLookup.setValue(DATETIME);
            valueFragment = openSimpleValueFragment(DATETIME, parameterValue);
        } else if (parameterValue instanceof TimeParameterValue) {
            typeLookup.setValue(TIME);
            valueFragment = openSimpleValueFragment(TIME, parameterValue);
        } else if (parameterValue instanceof UuidParameterValue) {
            typeLookup.setValue(UUID);
            valueFragment = openSimpleValueFragment(UUID, parameterValue);
        } else if (parameterValue instanceof IntegerParameterValue) {
            typeLookup.setValue(INTEGER);
            valueFragment = openSimpleValueFragment(INTEGER, parameterValue);
        } else if (parameterValue instanceof LongParameterValue) {
            typeLookup.setValue(LONG);
            valueFragment = openSimpleValueFragment(LONG, parameterValue);
        } else if (parameterValue instanceof StringParameterValue) {
            typeLookup.setValue(STRING);
            valueFragment = openSimpleValueFragment(STRING, parameterValue);
        } else if (parameterValue instanceof DecimalParameterValue) {
            typeLookup.setValue(DECIMAL);
            valueFragment = openSimpleValueFragment(DECIMAL, parameterValue);
        } else if (parameterValue instanceof BooleanParameterValue) {
            typeLookup.setValue(BOOLEAN);
            valueFragment = openSimpleValueFragment(BOOLEAN, parameterValue);
        } else {
            typeLookup.setValue(null);
        }
    }

    protected void parameterTypeChanged(ParameterType type) {
        valueBox.removeAll();
        switch (type) {
            case LIST_ENTITY:
                valueFragment = openEntitiesListValueFragment(new ListEntitiesParameterValue());
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

    protected SimpleValueFragment openSimpleValueFragment(ParameterType type, ParameterValue parameterValue) {
        SimpleValueFragment fragment = (SimpleValueFragment) fragments.create(
                this,
                SimpleValueFragment.class,
                new MapScreenOptions(ParamsMap.of(ValueFragment.VALUE_TYPE, type, ValueFragment.VALUE, parameterValue))
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

    protected EntitiesListValueFragment openEntitiesListValueFragment(ListEntitiesParameterValue value) {
        EntitiesListValueFragment fragment = (EntitiesListValueFragment) fragments.create(
                this,
                EntitiesListValueFragment.class,
                new MapScreenOptions(ParamsMap.of(ValueFragment.VALUE, value))
        ).init();
        valueBox.add(fragment.getFragment());
        return fragment;
    }
}