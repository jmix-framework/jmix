/*
 * Copyright 2020 Haulmont.
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
import io.jmix.core.common.event.Subscription;

import javax.annotation.Nullable;
import java.util.EventObject;
import java.util.function.Consumer;

/**
 * Generic UI component designed to select and display a value of any type.
 * Consists of the text field and the set of buttons defined by actions.
 */
public interface ValuePicker<V> extends Field<V>, HasFormatter<V>,
        ActionsHolder, Buffered, Component.Focusable, SupportsUserAction<V> {

    String NAME = "valuePicker";

    TypeToken<ValuePicker<String>> TYPE_STRING = new TypeToken<ValuePicker<String>>(){};

    static <T> TypeToken<ValuePicker<T>> of(Class<T> valueClass) {
        return new TypeToken<ValuePicker<T>>() {};
    }

    /**
     * @return whether a user can input the value manually
     */
    boolean isFieldEditable();

    /**
     * Sets whether a user can input the value manually. {@code false} by default.
     * Doesn't set entered value to the model. To handle user input,
     * the {@link FieldValueChangeEvent} listener must be used.
     *
     * @param editable {@code true} to enable manual input, {@code false} otherwise
     * @see #addFieldValueChangeListener(Consumer)
     */
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
     * The event that is fired when a user inputs value manually.
     *
     * @param <V> field value type
     * @see #setFieldEditable(boolean)
     */
    class FieldValueChangeEvent<V> extends EventObject {

        protected final String text;
        protected final V prevValue;

        public FieldValueChangeEvent(ValuePicker<V> source, String text, @Nullable V prevValue) {
            super(source);
            this.text = text;
            this.prevValue = prevValue;
        }

        @SuppressWarnings("unchecked")
        @Override
        public ValuePicker<V> getSource() {
            return (ValuePicker<V>) super.getSource();
        }

        /**
         * @return entered text
         */
        public String getText() {
            return text;
        }

        /**
         * @return the previous value
         */
        @Nullable
        public V getPrevValue() {
            return prevValue;
        }
    }

    /**
     * Interface to be implemented by actions intended to be used by ValuePicker.
     */
    interface ValuePickerAction {

        String PROP_EDITABLE = "editable";

        /**
         * Sets a {@link ValuePicker} instance associated with this action.
         *
         * @param valuePicker a {@link ValuePicker} instance to set
         */
        void setPicker(@Nullable ValuePicker valuePicker);

        /**
         * Called by {@link ValuePicker} to inform about its editable state.
         *
         * @param editable a {@link ValuePicker} editable state
         */
        void editableChanged(boolean editable);

        /**
         * @return whether this action is editable
         */
        boolean isEditable();
    }
}
