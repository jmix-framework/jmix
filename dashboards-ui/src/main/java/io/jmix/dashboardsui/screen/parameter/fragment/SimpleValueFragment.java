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

import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.dashboards.model.parameter.ParameterType;
import io.jmix.dashboards.model.parameter.type.*;
import io.jmix.ui.component.*;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@UiController("dshbrd_SimpleValue.fragment")
@UiDescriptor("simple-value-fragment.xml")
public class SimpleValueFragment extends ScreenFragment implements ValueFragment {
    @Autowired
    protected TextField<Integer> intField;
    @Autowired
    protected TextField<BigDecimal> decimalField;
    @Autowired
    protected TextField<Long> longField;
    @Autowired
    protected TextField<UUID> uuidField;
    @Autowired
    protected TextField<String> textField;
    @Autowired
    protected DateField<Date> dateField;
    @Autowired
    protected TimeField<Date> timeField;
    @Autowired
    protected CheckBox checkBox;
    @Autowired
    protected HBoxLayout hBox;
    @Autowired
    protected DatatypeRegistry datatypeRegistry;

    protected ParameterType type;

    @Subscribe
    public void onInit(InitEvent event) {
        MapScreenOptions options = (MapScreenOptions) event.getOptions();
        Map<String, Object> params = options.getParams();

        type = (ParameterType) params.get(VALUE_TYPE);
        ParameterValue parameterValue = (ParameterValue) params.get(VALUE);
        showField(parameterValue);
    }

    @Override
    public ParameterValue getValue() {
        switch (type) {
            case DATETIME:
                return new DateTimeParameterValue(dateField.getValue());
            case TIME:
                return new TimeParameterValue(timeField.getValue());
            case DATE:
                return new DateParameterValue(dateField.getValue());
            case DECIMAL:
                return new DecimalParameterValue(decimalField.getValue());
            case INTEGER:
                return new IntegerParameterValue(intField.getValue());
            case LONG:
                return new LongParameterValue(longField.getValue());
            case STRING:
                return new StringParameterValue(textField.getValue());
            case UUID:
                return new UuidParameterValue(uuidField.getValue());
            case BOOLEAN:
                return new BooleanParameterValue(checkBox.getValue());
            default:
                return null;
        }
    }

    protected void showField(@Nullable ParameterValue parameterValue) {
        switch (type) {
            case DATETIME:
                setDateTime((DateTimeParameterValue) parameterValue);
                break;
            case TIME:
                setTime((TimeParameterValue) parameterValue);
                break;
            case DATE:
                setDate((DateParameterValue) parameterValue);
                break;
            case DECIMAL:
                setDecimal((DecimalParameterValue) parameterValue);
                break;
            case INTEGER:
                setInteger((IntegerParameterValue) parameterValue);
                break;
            case LONG:
                setLong((LongParameterValue) parameterValue);
                break;
            case STRING:
                setString((StringParameterValue) parameterValue);
                break;
            case UUID:
                setUUID((UuidParameterValue) parameterValue);
                break;
            case BOOLEAN:
                setBoolean((BooleanParameterValue) parameterValue);
                break;
            default:
                break;
        }
    }

    protected void setDateTime(@Nullable DateTimeParameterValue value) {
        dateField.setValue(value == null ? null : value.getValue());
        initDateField("dd/MM/yyyy HH:mm");
    }

    protected void setDate(@Nullable DateParameterValue value) {
        dateField.setValue(value == null ? null : value.getValue());
        initDateField("dd/MM/yyyy");
    }

    protected void initDateField(String format) {
        dateField.setDateFormat(format);
        dateField.setVisible(true);
    }

    protected void setTime(@Nullable TimeParameterValue value) {
        timeField.setValue(value == null ? null : value.getValue());
        timeField.setVisible(true);
    }

    protected void setDecimal(@Nullable DecimalParameterValue value) {
        decimalField.setValue(value == null ? null : value.getValue());
        decimalField.setVisible(true);
    }

    protected void setInteger(@Nullable IntegerParameterValue value) {
        intField.setValue(value == null ? null : value.getValue());
        intField.setVisible(true);
    }

    protected void setLong(@Nullable LongParameterValue value) {
        longField.setValue(value == null ? null : value.getValue());
        longField.setVisible(true);
    }

    protected void setString(@Nullable StringParameterValue value) {
        textField.setValue(value == null ? null : value.getValue());
        textField.setVisible(true);
    }

    protected void setUUID(@Nullable UuidParameterValue value) {
        uuidField.setValue(value == null ? null : value.getValue());
        uuidField.setVisible(true);
    }

    protected void setBoolean(@Nullable BooleanParameterValue value) {
        checkBox.setValue(value == null ? null : value.getValue());
        checkBox.setVisible(true);
    }
}
