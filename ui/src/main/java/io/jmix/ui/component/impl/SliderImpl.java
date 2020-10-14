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


import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.datatype.impl.IntegerDatatype;
import io.jmix.core.metamodel.datatype.impl.LongDatatype;
import io.jmix.ui.component.Slider;
import io.jmix.ui.component.data.ConversionException;
import io.jmix.ui.component.data.DataAwareComponentsTools;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.widget.JmixSlider;
import org.springframework.util.NumberUtils;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;

public class SliderImpl<V extends Number> extends AbstractField<JmixSlider<V>, V, V> implements Slider<V> {

    protected Datatype<V> datatype;

    protected DataAwareComponentsTools dataAwareComponentsTools;

    public SliderImpl() {
        component = createComponent();

        attachValueChangeListener(component);
    }

    protected JmixSlider<V> createComponent() {
        return new JmixSlider<>();
    }

    @Autowired
    public void setDataAwareComponentsTools(DataAwareComponentsTools dataAwareComponentsTools) {
        this.dataAwareComponentsTools = dataAwareComponentsTools;
    }

    @Override
    public void setMin(V min) {
        component.setMin(convertToDouble(min));
    }

    @Override
    public V getMin() {
        return convertFromDouble(component.getMin());
    }

    @Override
    public void setMax(V max) {
        component.setMax(convertToDouble(max));
    }

    @Override
    public V getMax() {
        return convertFromDouble(component.getMax());
    }

    @Override
    public void setResolution(int resolution) {
        if (resolution > 0
                && (datatype instanceof IntegerDatatype || datatype instanceof LongDatatype)) {
            throw new IllegalArgumentException(
                    String.format("Slider cannot have resolution for datatype: '%s'", datatype));
        }

        component.setResolution(resolution);
    }

    @Override
    public int getResolution() {
        return component.getResolution();
    }

    @Override
    public void setUpdateValueOnClick(boolean updateValueOnClick) {
        component.setUpdateValueOnClick(updateValueOnClick);
    }

    @Override
    public boolean isUpdateValueOnCLick() {
        return component.isUpdateValueOnClick();
    }

    @Override
    public Orientation getOrientation() {
        return WrapperUtils.fromVaadinSliderOrientation(component.getOrientation());
    }

    @Override
    public void setOrientation(Orientation orientation) {
        component.setOrientation(WrapperUtils.toVaadinSliderOrientation(orientation));
    }

    @Nullable
    @Override
    public Datatype<V> getDatatype() {
        if (datatype == null) {
            datatype = loadDatatype();
        }
        return datatype;
    }

    @Override
    public void setDatatype(Datatype<V> datatype) {
        dataAwareComponentsTools.checkValueSourceDatatypeMismatch(datatype, getValueSource());

        this.datatype = datatype;
    }

    @SuppressWarnings("unchecked")
    protected Datatype<V> loadDatatype() {
        if (valueBinding != null
                && valueBinding.getSource() instanceof EntityValueSource) {
            EntityValueSource entityValueSource = (EntityValueSource) valueBinding.getSource();
            return entityValueSource.getMetaPropertyPath()
                    .getRange()
                    .asDatatype();
        }

        DatatypeRegistry datatypeRegistry = applicationContext.getBean(DatatypeRegistry.class);
        return (Datatype<V>) datatypeRegistry.get(Double.class);
    }

    @Override
    protected void attachValueChangeListener(JmixSlider<V> component) {
        component.getInternalComponent()
                .addValueChangeListener(event ->
                        componentValueChanged(event.getOldValue(), event.getValue(), event.isUserOriginated()));
    }

    protected void componentValueChanged(Double prevComponentValue, Double newComponentValue, boolean isUserOriginated) {
        V prevValue = convertFromDouble(prevComponentValue);
        V newValue = convertFromDouble(newComponentValue);

        componentValueChanged(prevValue, newValue, isUserOriginated);
    }

    @SuppressWarnings("unchecked")
    protected V convertFromDouble(Double componentValue) throws ConversionException {
        Datatype<V> datatype = getDatatype();
        return datatype != null
                ? (V) NumberUtils.convertNumberToTargetClass(componentValue, datatype.getJavaClass())
                : (V) componentValue;
    }

    protected Double convertToDouble(V value) {
        return value.doubleValue();
    }
}
