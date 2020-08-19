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
package io.jmix.ui.component;

import com.google.common.reflect.TypeToken;
import io.jmix.core.JmixEntity;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.metamodel.model.MetaClass;

import javax.annotation.Nullable;
import java.util.EventObject;
import java.util.function.Consumer;

/**
 * Generic UI component designed to select and display an entity instance.
 * Consists of the text field and the set of buttons defined by actions.
 *
 * @see EntityComboBox
 */
public interface EntityPicker<V extends JmixEntity> extends ValuePicker<V>,
        LookupComponent, HasOptionCaptionProvider<V>, HasOptionIconProvider<V> {

    String NAME = "entityPicker";

    static <T extends JmixEntity> TypeToken<EntityPicker<T>> of(Class<T> valueClass) {
        return new TypeToken<EntityPicker<T>>() {};
    }

    @Nullable
    MetaClass getMetaClass();
    void setMetaClass(@Nullable MetaClass metaClass);

    interface EntityPickerAction extends ValuePickerAction {

        void setEntityPicker(@Nullable EntityPicker valuePicker);

        @Override
        default void setPicker(@Nullable ValuePicker valuePicker) {
            if (!(valuePicker instanceof EntityPicker)) {
                throw new IllegalArgumentException("Incorrect component type. Must be " +
                        "'EntityPicker' or its inheritors");
            }
            setEntityPicker(((EntityPicker) valuePicker));
        }
    }
}
