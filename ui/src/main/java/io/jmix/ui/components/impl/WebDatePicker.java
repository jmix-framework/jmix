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

package io.jmix.ui.components.impl;

import io.jmix.core.commons.util.Preconditions;
import io.jmix.core.metamodel.datatypes.Datatype;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.DateTimeTransformations;
import io.jmix.core.Messages;
import io.jmix.ui.components.DatePicker;
import io.jmix.ui.components.data.ConversionException;
import io.jmix.ui.components.data.DataAwareComponentsTools;
import io.jmix.ui.components.data.meta.EntityValueSource;
import io.jmix.ui.components.data.ValueSource;
import io.jmix.ui.widgets.CubaDatePicker;
import com.vaadin.shared.ui.datefield.DateResolution;
import com.vaadin.ui.InlineDateField;
import io.jmix.ui.widgets.CubaDatePicker;

import javax.inject.Inject;
import java.time.*;
import java.util.Date;

public class WebDatePicker<V> extends WebV8AbstractField<InlineDateField, LocalDate, V> implements DatePicker<V> {

    protected DateTimeTransformations dateTimeTransformations;

    protected Resolution resolution = Resolution.DAY;
    protected Datatype<V> datatype;
    protected V rangeStart;
    protected V rangeEnd;

    protected DataAwareComponentsTools dataAwareComponentsTools;

    public WebDatePicker() {
        this.component = new CubaDatePicker();

        attachValueChangeListener(component);
    }

    @Inject
    public void setDataAwareComponentsTools(DataAwareComponentsTools dataAwareComponentsTools) {
        this.dataAwareComponentsTools = dataAwareComponentsTools;
    }

    @Inject
    public void setDateTimeTransformations(DateTimeTransformations dateTimeTransformations) {
        this.dateTimeTransformations = dateTimeTransformations;
    }

    @Inject
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
        DateResolution vResolution = WebWrapperUtils.convertDateResolution(resolution);
        component.setResolution(vResolution);
    }

    @Override
    protected void valueBindingConnected(ValueSource<V> valueSource) {
        super.valueBindingConnected(valueSource);

        if (valueSource instanceof EntityValueSource) {
            EntityValueSource entityValueSource = (EntityValueSource) valueSource;
            DataAwareComponentsTools dataAwareComponentsTools = beanLocator.get(DataAwareComponentsTools.class);
            dataAwareComponentsTools.setupDateRange(this, entityValueSource);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected V convertToModel(LocalDate componentRawValue) throws ConversionException {
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

    @Override
    protected LocalDate convertToPresentation(V modelValue) throws ConversionException {
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

    @Override
    public V getRangeStart() {
        return rangeStart;
    }

    @Override
    public void setRangeStart(V value) {
        this.rangeStart = value;
        component.setRangeStart(value == null ? null : convertToLocalDateTime(rangeStart).toLocalDate());
    }

    @Override
    public V getRangeEnd() {
        return rangeEnd;
    }

    @Override
    public void setRangeEnd(V value) {
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
