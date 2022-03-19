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

import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.component.EntityPicker;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.widget.JmixPickerField;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;

public class EntityPickerImpl<V> extends ValuePickerImpl<V> implements EntityPicker<V> {

    protected Metadata metadata;

    protected MetaClass metaClass;

    public EntityPickerImpl() {
    }

    protected JmixPickerField<V> createComponent() {
        return new JmixPickerField<>();
    }

    @Override
    public void setValue(@Nullable V value) {
        checkValueType(value);
        super.setValue(value);
    }

    @Override
    public void setValueFromUser(@Nullable V value) {
        checkValueType(value);
        super.setValueFromUser(value);
    }

    protected void checkValueType(@Nullable V value) {
        if (value != null) {
            MetaClass metaClass = getMetaClass();
            if (metaClass == null) {
                throw new IllegalStateException("Neither metaClass nor valueSource is set for PickerField");
            }

            Class<?> fieldClass = metaClass.getJavaClass();
            Class<?> valueClass = value.getClass();
            if (!fieldClass.isAssignableFrom(valueClass)) {
                throw new IllegalArgumentException(
                        String.format("Could not set value with class %s to field with class %s",
                                fieldClass.getCanonicalName(),
                                valueClass.getCanonicalName())
                );
            }
        }
    }

    @Autowired
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Nullable
    @Override
    public MetaClass getMetaClass() {
        ValueSource<V> valueSource = getValueSource();
        if (valueSource instanceof EntityValueSource) {
            MetaProperty metaProperty = ((EntityValueSource) valueSource).getMetaPropertyPath().getMetaProperty();
            return metaProperty.getRange().asClass();
        } else {
            return metaClass;
        }
    }

    @Override
    public void setMetaClass(@Nullable MetaClass metaClass) {
        ValueSource<V> valueSource = getValueSource();
        if (valueSource != null) {
            throw new IllegalStateException("ValueSource is not null");
        }
        this.metaClass = metaClass;
    }

    @Override
    public void setLookupSelectHandler(Consumer<Collection<V>> selectHandler) {
        // do nothing
    }

    @Override
    public Collection<V> getLookupSelectedItems() {
        V value = getValue();
        if (value == null) {
            return Collections.emptyList();
        }

        return Collections.singleton(value);
    }
}
