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
 * Generic UI component designed to select and display an entity instance. Consists of the text field and the set of buttons
 * defined by actions.
 *
 * @see EntityComboBox
 */
public interface EntityPicker<V extends JmixEntity> extends Field<V>, ActionsHolder, Buffered,
        LookupComponent, Component.Focusable, HasOptionCaptionProvider<V>, SupportsUserAction<V>,
        HasOptionIconProvider<V> {

    String NAME = "entityPicker";

    static <T extends JmixEntity> TypeToken<EntityPicker<T>> of(Class<T> valueClass) {
        return new TypeToken<EntityPicker<T>>() {};
    }

    MetaClass getMetaClass();
    void setMetaClass(MetaClass metaClass);

    void setFieldEditable(boolean editable);

    /**
     * Adds a listener that will be fired in case field is editable.
     *
     * @param listener a listener to add
     * @return a {@link Subscription} object
     * @see #setFieldEditable(boolean)
     */
    Subscription addFieldValueChangeListener(Consumer<FieldValueChangeEvent<V>> listener);

    /**
     * The event is fired when a user inputs value manually.
     * <p>
     * Field editing can be enabled via {@link #setFieldEditable(boolean)}.
     *
     * @param <V> field value type
     */
    class FieldValueChangeEvent<V extends JmixEntity> extends EventObject {

        protected final String text;
        protected final V prevValue;

        public FieldValueChangeEvent(EntityPicker<V> source, String text, V prevValue) {
            super(source);
            this.text = text;
            this.prevValue = prevValue;
        }

        @SuppressWarnings("unchecked")
        @Override
        public EntityPicker<V> getSource() {
            return (EntityPicker<V>) super.getSource();
        }

        public String getText() {
            return text;
        }

        public V getPrevValue() {
            return prevValue;
        }
    }

    interface EntityPickerAction {
        String PROP_EDITABLE = "editable";

        void setEntityPicker(@Nullable EntityPicker entityPicker);

        void editableChanged(boolean editable);

        boolean isEditable();
    }
}
