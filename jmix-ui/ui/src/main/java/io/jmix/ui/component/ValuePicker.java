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

import io.jmix.core.common.event.Subscription;
import io.jmix.ui.meta.CanvasBehaviour;
import io.jmix.ui.meta.PropertiesConstraint;
import io.jmix.ui.meta.PropertiesGroup;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;
import org.springframework.core.ParameterizedTypeReference;

import javax.annotation.Nullable;
import java.util.EventObject;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Generic UI component designed to select and display a value of any type.
 * Consists of the text field and the set of buttons defined by actions.
 */
@StudioComponent(
        caption = "ValuePicker",
        category = "Components",
        xmlElement = "valuePicker",
        icon = "io/jmix/ui/icon/component/valuePicker.svg",
        canvasBehaviour = CanvasBehaviour.VALUE_PICKER,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/value-picker.html"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "property", type = PropertyType.PROPERTY_PATH_REF, typeParameter = "V"),
                @StudioProperty(name = "dataContainer", type = PropertyType.DATACONTAINER_REF)
        },
        groups = {
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "property"})
        }
)
public interface ValuePicker<V> extends Field<V>, HasFormatter<V>,
        ActionsHolder, Buffered, Component.Focusable, SupportsUserAction<V>, HasInputPrompt {

    String NAME = "valuePicker";

    ParameterizedTypeReference<ValuePicker<String>> TYPE_STRING =
            new ParameterizedTypeReference<ValuePicker<String>>() {
            };

    static <T> ParameterizedTypeReference<ValuePicker<T>> of(Class<T> valueClass) {
        return new ParameterizedTypeReference<ValuePicker<T>>() {
        };
    }

    /**
     * @return an icon provider or {code null} if not set
     */
    @Nullable
    Function<? super V, String> getFieldIconProvider();

    /**
     * Sets a function that provides an icon for the field.
     *
     * @param iconProvider icon provider to set
     */
    void setFieldIconProvider(@Nullable Function<? super V, String> iconProvider);

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
    @StudioProperty(name = "fieldEditable", defaultValue = "false")
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
