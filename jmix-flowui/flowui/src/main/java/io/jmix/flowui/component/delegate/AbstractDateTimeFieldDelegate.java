/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.component.delegate;

import com.vaadin.flow.component.AbstractField;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import io.jmix.flowui.data.EntityValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.component.validation.DateTimeRangeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

public abstract class AbstractDateTimeFieldDelegate<C extends AbstractField<?, V>, T extends Comparable, V>
        extends AbstractFieldDelegate<C, T, V> {

    private static final Logger log = LoggerFactory.getLogger(AbstractDateTimeFieldDelegate.class);

    protected DateTimeRangeValidator<T> dateTimeRangeValidator;

    protected V minDate;
    protected V maxDate;

    public AbstractDateTimeFieldDelegate(C component) {
        super(component);
    }

    @Nullable
    public V getMin() {
        return minDate;
    }

    public void setMin(@Nullable V date) {
        minDate = date;

        updateDateTimeValidator();
    }

    @Nullable
    public V getMax() {
        return maxDate;
    }

    public void setMax(@Nullable V max) {
        maxDate = max;

        updateDateTimeValidator();
    }

    public Class<?> getValueType() {
        if (getValueSource() instanceof EntityValueSource) {
            MetaPropertyPath propertyPath = ((EntityValueSource<?, V>) getValueSource()).getMetaPropertyPath();
            MetaProperty metaProperty = propertyPath.getMetaProperty();
            Range range = metaProperty.getRange();
            if (range.isDatatype()) {
                return range.asDatatype().getJavaClass();
            } else {
                throw new IllegalStateException("Property has a non-simple type");
            }
        } else if (getDatatype() != null) {
            return getDatatype().getJavaClass();
        }

        return java.util.Date.class;
    }

    @Override
    public void setDatatype(@Nullable Datatype<T> datatype) {
        super.setDatatype(datatype);

        updateDateTimeValidator();
    }

    @Override
    public void setValueSource(@Nullable ValueSource<T> valueSource) {
        super.setValueSource(valueSource);

        updateDateTimeValidator();
    }

    @SuppressWarnings("unchecked")
    protected void updateDateTimeValidator() {
        if (dateTimeRangeValidator == null) {
            dateTimeRangeValidator = applicationContext.getBean(DateTimeRangeValidator.class);
            addValidator(dateTimeRangeValidator);
        }

        if (toModelConverter != null) {
            dateTimeRangeValidator.setMin(toModelConverter.apply(minDate));
            dateTimeRangeValidator.setMax(toModelConverter.apply(maxDate));
        } else {
            log.debug("Min and max values are not set to the '{}' because the converter to model type is null",
                    DateTimeRangeValidator.class.getName());
        }
    }
}
