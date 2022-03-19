/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.component.impl;

import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.DateTimeTransformations;
import io.jmix.core.Messages;
import io.jmix.ui.component.DatePicker;
import io.jmix.ui.component.data.ConversionException;
import io.jmix.ui.component.data.DataAwareComponentsTools;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.component.data.ValueSource;
import com.vaadin.shared.ui.datefield.DateResolution;
import com.vaadin.ui.InlineDateField;
import io.jmix.ui.widget.JmixInlineDateField;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.time.*;
import java.util.Date;

public class DatePickerImpl<V> extends AbstractField<InlineDateField, LocalDate, V> implements DatePicker<V> {

    protected DateTimeTransformations dateTimeTransformations;

    protected Resolution resolution = Resolution.DAY;
    protected Datatype<V> datatype;
    protected V rangeStart;
    protected V rangeEnd;

    protected DataAwareComponentsTools dataAwareComponentsTools;

    public DatePickerImpl() {
        this.component = createComponent();

        attachValueChangeListener(component);
    }

    protected InlineDateField createComponent() {
        return new JmixInlineDateField();
    }

    @Autowired
    public void setDataAwareComponentsTools(DataAwareComponentsTools dataAwareComponentsTools) {
        this.dataAwareComponentsTools = dataAwareComponentsTools;
    }

    @Autowired
    public void setDateTimeTransformations(DateTimeTransformations dateTimeTransformations) {
        this.dateTimeTransformations = dateTimeTransformations;
    }

    @Autowired
    public void setMessages(Messages messages) {
        component.setDateOutOfRangeMessage(messages.getMessage("datePicker.dateOutOfRangeMessage"));
    }

    @Override
    public Resolution getResolution() {
        return resolution;
    }

    @Override
    public void setResolution(Resolution resolution) {
        Preconditions.checkNotNullArgument(resolution);

        this.resolution = resolution;
        DateResolution vResolution = WrapperUtils.convertDateResolution(resolution);
        component.setResolution(vResolution);
    }

    @Override
    protected void valueBindingConnected(ValueSource<V> valueSource) {
        super.valueBindingConnected(valueSource);

        if (valueSource instanceof EntityValueSource) {
            EntityValueSource entityValueSource = (EntityValueSource) valueSource;
            DataAwareComponentsTools dataAwareComponentsTools = applicationContext.getBean(DataAwareComponentsTools.class);
            dataAwareComponentsTools.setupDateRange(this, entityValueSource);
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    @Override
    protected V convertToModel(@Nullable LocalDate componentRawValue) throws ConversionException {
        if (componentRawValue == null) {
            return null;
        }

        LocalDateTime localDateTime = LocalDateTime.of(componentRawValue, LocalTime.MIDNIGHT);

        ValueSource<V> valueSource = getValueSource();
        if (valueSource instanceof EntityValueSource) {
            MetaProperty metaProperty = ((EntityValueSource) valueSource).getMetaPropertyPath().getMetaProperty();
            return (V) convertFromLocalDateTime(localDateTime, metaProperty.getRange().asDatatype().getJavaClass());
        }
        return (V) convertFromLocalDateTime(localDateTime, datatype == null ? Date.class : datatype.getJavaClass());
    }

    @Nullable
    @Override
    protected LocalDate convertToPresentation(@Nullable V modelValue) throws ConversionException {
        if (modelValue == null) {
            return null;
        }
        return convertToLocalDateTime(modelValue).toLocalDate();
    }

    protected LocalDateTime convertToLocalDateTime(Object date) {
        Preconditions.checkNotNullArgument(date);
        ZonedDateTime zonedDateTime = dateTimeTransformations.transformToZDT(date);
        return zonedDateTime.toLocalDateTime();

    }

    protected Object convertFromLocalDateTime(LocalDateTime localDateTime, Class javaType) {
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
        return dateTimeTransformations.transformFromZDT(zonedDateTime, javaType);
    }

    @Nullable
    @Override
    public V getRangeStart() {
        return rangeStart;
    }

    @Override
    public void setRangeStart(@Nullable V value) {
        this.rangeStart = value;
        component.setRangeStart(value == null ? null : convertToLocalDateTime(rangeStart).toLocalDate());
    }

    @Nullable
    @Override
    public V getRangeEnd() {
        return rangeEnd;
    }

    @Override
    public void setRangeEnd(@Nullable V value) {
        this.rangeEnd = value;
        component.setRangeEnd(value == null ? null : convertToLocalDateTime(rangeEnd).toLocalDate());
    }

    @Override
    public void focus() {
        component.focus();
    }

    @Override
    public int getTabIndex() {
        return component.getTabIndex();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        component.setTabIndex(tabIndex);
    }

    @Nullable
    @Override
    public Datatype<V> getDatatype() {
        return datatype;
    }

    @Override
    public void setDatatype(Datatype<V> datatype) {
        dataAwareComponentsTools.checkValueSourceDatatypeMismatch(datatype, getValueSource());

        this.datatype = datatype;
    }

    @Override
    public void commit() {
        super.commit();
    }

    @Override
    public void discard() {
        super.discard();
    }

    @Override
    public boolean isBuffered() {
        return super.isBuffered();
    }

    @Override
    public void setBuffered(boolean buffered) {
        super.setBuffered(buffered);
    }

    @Override
    public boolean isModified() {
        return super.isModified();
    }
}
