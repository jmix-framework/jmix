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

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface MultiValueSelectView<E> {

    void setMultiValueSelectContext(MultiValueSelectContext<E> context);

    List<E> getValue();

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

        @SuppressWarnings("DataFlowIssue")
        public MultiValueSelectContext() {
            super(null, null);
        }

        @Nullable
        public String getEntityName() {
            return entityName;
        }

        public void setEntityName(@Nullable String entityName) {
            this.entityName = entityName;
        }

        @Nullable
        public Class<? extends Enum<?>> getEnumClass() {
            return enumClass;
        }

        public void setEnumClass(@Nullable Class<? extends Enum<?>> enumClass) {
            this.enumClass = enumClass;
        }

        @Nullable
        public Class<?> getJavaClass() {
            return javaClass;
        }

        public void setJavaClass(@Nullable Class<?> javaClass) {
            this.javaClass = javaClass;
        }

        public boolean isReadOnly() {
            return readOnly;
        }

        public void setReadOnly(boolean readOnly) {
            this.readOnly = readOnly;
        }

        public Collection<E> getInitialValues() {
            return initialValues != null
                    ? Collections.unmodifiableCollection(initialValues)
                    : Collections.emptyList();
        }

        public void setInitialValues(@Nullable Collection<E> initialValues) {
            this.initialValues = initialValues;
        }

        public String getLookupViewId() {
            return lookupViewId;
        }

        public void setLookupViewId(String lookupViewId) {
            this.lookupViewId = lookupViewId;
        }

        public boolean isUseComboBox() {
            return useComboBox;
        }

        public void setUseComboBox(boolean useComboBox) {
            this.useComboBox = useComboBox;
        }

        @Nullable
        public ItemLabelGenerator<E> getItemLabelGenerator() {
            return itemLabelGenerator;
        }

        public void setItemLabelGenerator(@Nullable ItemLabelGenerator<E> itemLabelGenerator) {
            this.itemLabelGenerator = itemLabelGenerator;
        }

        public List<Validator<E>> getValidators() {
            return validators != null
                    ? Collections.unmodifiableList(validators)
                    : Collections.emptyList();
        }

        public void setValidators(@Nullable List<Validator<E>> validators) {
            this.validators = validators;
        }

        public void addValidator(Validator<E> validator) {
            if (validators == null) {
                validators = new ArrayList<>();
            }

            validators.add(validator);
        }

        @Nullable
        public TimeZone getTimeZone() {
            return timeZone;
        }

        public void setTimeZone(@Nullable TimeZone timeZone) {
            this.timeZone = timeZone;
        }

        public Predicate<E> getValueExistsHandler() {
            return valueExistsHandler;
        }

        public void setValueExistsHandler(Predicate<E> valueExistsHandler) {
            this.valueExistsHandler = valueExistsHandler;
        }

        public Consumer<E> getAddItemToLayoutHandler() {
            return addItemToLayoutHandler;
        }

        public void setAddItemToLayoutHandler(Consumer<E> addItemToLayoutHandler) {
            this.addItemToLayoutHandler = addItemToLayoutHandler;
        }

        public Consumer<ActionPerformedEvent> getEntityPickerActionPerformedEventHandler() {
            return entityPickerActionPerformedEventHandler;
        }

        public void setEntityPickerActionPerformedEventHandler(
                Consumer<ActionPerformedEvent> entityPickerActionPerformedEventHandler) {
            this.entityPickerActionPerformedEventHandler = entityPickerActionPerformedEventHandler;
        }

        public Consumer<HasValue<?, E>> getAddValueInternalHandler() {
            return addValueInternalHandler;
        }

        public void setAddValueInternalHandler(Consumer<HasValue<?, E>> addValueInternalHandler) {
            this.addValueInternalHandler = addValueInternalHandler;
        }
    }
}
