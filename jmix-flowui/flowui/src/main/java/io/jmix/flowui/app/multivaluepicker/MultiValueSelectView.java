/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.app.multivaluepicker;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.ItemLabelGenerator;
import io.jmix.flowui.component.ComponentGenerationContext;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.kit.action.ActionPerformedEvent;

import org.springframework.lang.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Interface for a multi-value select view component.
 * Represents the contract for components that support multi-value selection,
 * allowing selection of multiple values  and providing customization
 * through the {@link MultiValueSelectContext}.
 *
 * @param <E> the type of elements handled by the multi-value select view
 */
public interface MultiValueSelectView<E> {

    /**
     * Sets the context for generating multi-value select components.
     *
     * @param context the {@link MultiValueSelectContext} containing parameters and handlers
     *                required for configuring and managing the multi-value select component
     */
    void setMultiValueSelectContext(MultiValueSelectContext<E> context);

    /**
     * Returns the selected values in the current multi-value select component.
     *
     * @return a list of selected values
     */
    List<E> getValue();

    /**
     * Context class providing parameters required for generating multi-value select components.
     * Extends {@link ComponentGenerationContext} to include additional features specific 
     * to multi-value selection scenarios.
     *
     * @param <E> the type of elements to be included in the multi-value select component
     */
    class MultiValueSelectContext<E> extends ComponentGenerationContext {

        protected String entityName;
        protected Class<? extends Enum<?>> enumClass;
        protected Class<?> javaClass;

        protected boolean readOnly = false;

        protected Collection<E> initialValues;

        protected String lookupViewId;

        protected boolean useComboBox = false;

        protected Predicate<E> valueExistsHandler;
        protected Consumer<E> addItemToLayoutHandler;
        protected Consumer<ActionPerformedEvent> entityPickerActionPerformedEventHandler;
        protected Consumer<HasValue<?, E>> addValueInternalHandler;

        protected ItemLabelGenerator<E> itemLabelGenerator;
        protected List<Validator<E>> validators;

        protected TimeZone timeZone;

        public MultiValueSelectContext() {
            super(null, null);
        }

        /**
         * Returns the entity name used in this context.
         *
         * @return the entity name or null if not set
         */
        @Nullable
        public String getEntityName() {
            return entityName;
        }

        /**
         * Sets the entity name for this context.
         *
         * @param entityName the entity name to set
         */
        public void setEntityName(@Nullable String entityName) {
            this.entityName = entityName;
        }

        /**
         * Returns the enum class used in this context.
         *
         * @return the enum class or null if not set
         */
        @Nullable
        public Class<? extends Enum<?>> getEnumClass() {
            return enumClass;
        }

        /**
         * Sets the enum class for this context.
         *
         * @param enumClass the enum class to set
         */
        public void setEnumClass(@Nullable Class<? extends Enum<?>> enumClass) {
            this.enumClass = enumClass;
        }

        /**
         * Returns the value type class used in this context.
         *
         * @return the class or {@code null} if not set
         */
        @Nullable
        public Class<?> getJavaClass() {
            return javaClass;
        }

        /**
         * Sets the value type class for this context.
         *
         * @param javaClass the class to set
         */
        public void setJavaClass(@Nullable Class<?> javaClass) {
            this.javaClass = javaClass;
        }

        /**
         * Returns whether this context is read-only.
         *
         * @return true if the context is read-only, false otherwise
         */
        public boolean isReadOnly() {
            return readOnly;
        }

        /**
         * Sets the read-only state of this context.
         *
         * @param readOnly true to make the context read-only, false otherwise
         */
        public void setReadOnly(boolean readOnly) {
            this.readOnly = readOnly;
        }

        /**
         * Returns an unmodifiable collection of initial values.
         *
         * @return unmodifiable collection of initial values or empty list if no values are set
         */
        public Collection<E> getInitialValues() {
            return initialValues != null
                    ? Collections.unmodifiableCollection(initialValues)
                    : Collections.emptyList();
        }

        /**
         * Sets the initial values for this context.
         *
         * @param initialValues the collection of initial values to set
         */
        public void setInitialValues(@Nullable Collection<E> initialValues) {
            this.initialValues = initialValues;
        }

        /**
         * Returns the lookup view identifier.
         *
         * @return the lookup view identifier
         */
        public String getLookupViewId() {
            return lookupViewId;
        }

        /**
         * Sets the lookup view identifier.
         *
         * @param lookupViewId the lookup view identifier to set
         */
        public void setLookupViewId(String lookupViewId) {
            this.lookupViewId = lookupViewId;
        }

        /**
         * Returns whether to use combo box for selection.
         *
         * @return true if combo box should be used, false otherwise
         */
        public boolean isUseComboBox() {
            return useComboBox;
        }

        /**
         * Sets whether to use combo box for selection.
         *
         * @param useComboBox true to use combo box, false otherwise
         */
        public void setUseComboBox(boolean useComboBox) {
            this.useComboBox = useComboBox;
        }

        /**
         * Returns the item label generator used for generating labels.
         *
         * @return the item label generator or null if not set
         */
        @Nullable
        public ItemLabelGenerator<E> getItemLabelGenerator() {
            return itemLabelGenerator;
        }

        /**
         * Sets the item label generator for this context.
         *
         * @param itemLabelGenerator the item label generator to set
         */
        public void setItemLabelGenerator(@Nullable ItemLabelGenerator<E> itemLabelGenerator) {
            this.itemLabelGenerator = itemLabelGenerator;
        }

        /**
         * Returns an unmodifiable list of validators.
         *
         * @return unmodifiable list of validators or empty list if no validators are set
         */
        public List<Validator<E>> getValidators() {
            return validators != null
                    ? Collections.unmodifiableList(validators)
                    : Collections.emptyList();
        }

        /**
         * Sets the list of validators for this context.
         *
         * @param validators the list of validators to set
         */
        public void setValidators(@Nullable List<Validator<E>> validators) {
            this.validators = validators;
        }

        /**
         * Adds a validator to the list of validators.
         *
         * @param validator the validator to add
         */
        public void addValidator(Validator<E> validator) {
            if (validators == null) {
                validators = new ArrayList<>();
            }

            validators.add(validator);
        }

        /**
         * Returns the time zone used in this context.
         *
         * @return the time zone or null if not set
         */
        @Nullable
        public TimeZone getTimeZone() {
            return timeZone;
        }

        /**
         * Sets the time zone for this context.
         *
         * @param timeZone the time zone to set
         */
        public void setTimeZone(@Nullable TimeZone timeZone) {
            this.timeZone = timeZone;
        }

        /**
         * Returns the handler that checks if a value exists.
         *
         * @return the value exists handler
         */
        public Predicate<E> getValueExistsHandler() {
            return valueExistsHandler;
        }

        /**
         * Sets the handler that checks if a value exists.
         *
         * @param valueExistsHandler the value exists handler to set
         */
        public void setValueExistsHandler(Predicate<E> valueExistsHandler) {
            this.valueExistsHandler = valueExistsHandler;
        }

        /**
         * Returns the handler that adds items to the layout.
         *
         * @return the add item to layout handler
         */
        public Consumer<E> getAddItemToLayoutHandler() {
            return addItemToLayoutHandler;
        }

        /**
         * Sets the handler that adds items to the layout.
         *
         * @param addItemToLayoutHandler the add item to layout handler to set
         */
        public void setAddItemToLayoutHandler(Consumer<E> addItemToLayoutHandler) {
            this.addItemToLayoutHandler = addItemToLayoutHandler;
        }

        /**
         * Returns the handler for entity picker action performed events.
         *
         * @return the entity picker action performed event handler
         */
        public Consumer<ActionPerformedEvent> getEntityPickerActionPerformedEventHandler() {
            return entityPickerActionPerformedEventHandler;
        }

        /**
         * Sets the handler for entity picker action performed events.
         *
         * @param entityPickerActionPerformedEventHandler the entity picker action performed event handler to set
         */
        public void setEntityPickerActionPerformedEventHandler(
                Consumer<ActionPerformedEvent> entityPickerActionPerformedEventHandler) {
            this.entityPickerActionPerformedEventHandler = entityPickerActionPerformedEventHandler;
        }

        /**
         * Returns the handler that adds values internally.
         *
         * @return the add value internal handler
         */
        public Consumer<HasValue<?, E>> getAddValueInternalHandler() {
            return addValueInternalHandler;
        }

        /**
         * Sets the handler that adds values internally.
         *
         * @param addValueInternalHandler the add value internal handler to set
         */
        public void setAddValueInternalHandler(Consumer<HasValue<?, E>> addValueInternalHandler) {
            this.addValueInternalHandler = addValueInternalHandler;
        }
    }
}
