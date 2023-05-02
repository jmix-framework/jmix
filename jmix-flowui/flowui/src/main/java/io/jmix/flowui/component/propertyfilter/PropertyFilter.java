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

package io.jmix.flowui.component.propertyfilter;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.filter.SingleFilterComponentBase;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButton;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButtonVariant;
import io.jmix.flowui.model.DataLoader;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;

import static com.google.common.base.Preconditions.checkState;
import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * A UI component used for filtering entities returned by the {@link DataLoader}.
 * The component is related to entity property and can automatically render proper
 * layout for setting a condition value. In general case a component layout
 * contains a label with entity property name, operation label or selector
 * (=, contains, &#62;, etc.) and a field for editing a property value.
 *
 * @param <V> value type
 */
public class PropertyFilter<V> extends SingleFilterComponentBase<V> {

    protected static final String PROPERTY_FILTER_CLASS_NAME = "jmix-property-filter";

    protected SingleFilterSupport singleFilterSupport;
    protected PropertyFilterSupport propertyFilterSupport;

    protected DropdownButton operationSelector;

    protected Operation operation;
    protected boolean operationEditable = false;
    protected boolean operationTextVisible = true;

    @Override
    protected void autowireDependencies() {
        super.autowireDependencies();

        singleFilterSupport = applicationContext.getBean(SingleFilterSupport.class);
        propertyFilterSupport = applicationContext.getBean(PropertyFilterSupport.class);
    }

    @Override
    protected void initRootComponent(HorizontalLayout root) {
        super.initRootComponent(root);
        root.setClassName(PROPERTY_FILTER_CLASS_NAME);
    }

    @Override
    public String getInnerComponentPrefix() {
        return propertyFilterSupport.getPropertyFilterPrefix(getId(), Strings.nullToEmpty(getProperty()));
    }

    protected DropdownButton createOperationSelector() {
        DropdownButton operationSelector = uiComponents.create(DropdownButton.class);
        operationSelector.setId(getInnerComponentPrefix() + "operationSelector");
        operationSelector.setDropdownIndicatorVisible(false);
        operationSelector.addThemeVariants(DropdownButtonVariant.LUMO_ICON);

        initOperationSelectorActions(operationSelector);

        return operationSelector;
    }

    protected void initOperationSelectorActions(@Nullable DropdownButton operationSelector) {
        //noinspection ConstantConditions
        if (operationSelector != null
                && operationSelector.getItems().isEmpty()
                && dataLoader != null
                && getProperty() != null) {
            MetaClass metaClass = dataLoader.getContainer().getEntityMetaClass();

            for (Operation operation : propertyFilterSupport.getAvailableOperations(metaClass, getProperty())) {
                OperationChangeAction action = new OperationChangeAction(operation, this::setOperationInternal);
                action.setText(getOperationText(operation));
                operationSelector.addItem(operation.name(), action);
            }

            if (operation != null) {
                operationSelector.setText(getOperationText(operation));
            }
        }
    }

    protected String getOperationText(Operation operation) {
        return propertyFilterSupport.getOperationText(operation);
    }

    @Override
    public void setDataLoader(DataLoader dataLoader) {
        super.setDataLoader(dataLoader);

        initOperationSelectorActions(operationSelector);
        setLabelInternal(labelText);
    }

    /**
     * @return related entity property name
     */
    @Nullable
    public String getProperty() {
        return getQueryCondition().getProperty();
    }

    /**
     * Sets related entity property name.
     *
     * @param property entity property name
     */
    public void setProperty(String property) {
        checkState(getProperty() == null, "Property has already been initialized");
        checkNotNullArgument(property);

        getQueryCondition().setProperty(property);

        if (label != null) {
            label.setId(getInnerComponentPrefix() + "label");
        }

        initOperationSelectorActions(operationSelector);
        setLabelInternal(labelText);
    }

    /**
     * @return a filtering operation
     */
    public Operation getOperation() {
        return operation;
    }

    /**
     * Sets a filtering operation.
     *
     * @param operation a filtering operation
     */
    public void setOperation(Operation operation) {
        setOperationInternal(operation, false);
    }

    protected void setOperationInternal(Operation operation, boolean fromClient) {
        checkNotNullArgument(operation);

        if (this.operation == operation) {
            return;
        }

        getQueryCondition().setOperation(propertyFilterSupport.toPropertyConditionOperation(operation));

        if (operationSelector != null) {
            operationSelector.setText(getOperationText(operation));
        }

        if (this.valueComponent != null) {
            if (this.operation == null || this.operation.getType() != operation.getType()) {
                this.valueComponent.clear();
            }

            if (!isConditionModificationDelegated()) {
                apply();
            }
        }

        if (this.operation == null
                || this.operation.getType() != operation.getType()) {
            if (dataLoader != null && getProperty() != null) {
                MetaClass metaClass = dataLoader.getContainer().getEntityMetaClass();
                //noinspection unchecked
                HasValueAndElement<?, V> newValueComponent = singleFilterSupport.generateValueComponent(metaClass,
                        getProperty(), operation);
                setValueComponent(newValueComponent);
            }
        }

        Operation prevOperation = this.operation != null ? this.operation : operation;
        this.operation = operation;

        setLabelInternal(labelText);

        if (fromClient && operationSelector != null) {
            operationSelector.focus();
        }

        OperationChangeEvent<?> operationChangeEvent =
                new OperationChangeEvent<>(this, operation, prevOperation, fromClient);
        getEventBus().fireEvent(operationChangeEvent);
    }

    @Override
    public String getParameterName() {
        return getQueryCondition().getParameterName();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void setParameterName(String parameterName) {
        checkState(getParameterName() == null, "Parameter name has already been initialized");
        checkNotNullArgument(parameterName);

        getQueryCondition().setParameterName(parameterName);
    }

    @Override
    protected PropertyCondition createQueryCondition() {
        return new PropertyCondition();
    }

    /**
     * @return a {@link PropertyCondition} related to the current property filter
     */
    @Override
    public PropertyCondition getQueryCondition() {
        return (PropertyCondition) queryCondition;
    }

    @Override
    protected void updateQueryCondition(@Nullable V newValue) {
        getQueryCondition().setParameterValue(newValue);
    }

    /**
     * Adds a listener that is invoked when the {@code operation} property changes.
     *
     * @param listener a listener to add
     * @return a registration object for removing an event listener
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Registration addOperationChangeListener(ComponentEventListener<OperationChangeEvent<V>> listener) {
        return getEventBus().addListener(OperationChangeEvent.class, ((ComponentEventListener) listener));
    }

    /**
     * @return whether an operation selector is visible.
     */
    public boolean isOperationEditable() {
        return operationEditable;
    }

    /**
     * Sets whether an operation selector is visible.
     *
     * @param operationEditable whether an operation selector is visible
     */
    public void setOperationEditable(boolean operationEditable) {
        if (this.operationEditable != operationEditable) {
            this.operationEditable = operationEditable;

            removeOperationSelector();

            if (operationEditable) {
                operationSelector = createOperationSelector();
                root.addComponentAtIndex(labelPosition == LabelPosition.TOP ? 0 : 1, operationSelector);
            }

            setLabelInternal(labelText);
        }
    }

    /**
     * @return whether to show operation text
     */
    public boolean isOperationTextVisible() {
        return operationTextVisible;
    }

    /**
     * Sets whether to show operation text.
     *
     * @param operationLabelVisible whether to show operation text
     */
    public void setOperationTextVisible(boolean operationLabelVisible) {
        if (this.operationTextVisible != operationLabelVisible) {
            this.operationTextVisible = operationLabelVisible;

            setLabelInternal(labelText);
        }
    }

    protected void removeOperationSelector() {
        if (operationSelector != null) {
            root.remove(operationSelector);
            operationSelector = null;
        }
    }

    @Override
    protected void setLabelInternal(@Nullable String label) {
        String newLabelText;
        // TODO: gg, needs testing
        if (dataLoader == null
                || operation == null
                || getProperty() == null
                || !Strings.isNullOrEmpty(labelText)) {
            newLabelText = labelText;
        } else {
            MetaClass metaClass = dataLoader.getContainer().getEntityMetaClass();
            newLabelText = propertyFilterSupport.getPropertyFilterCaption(metaClass, getProperty(),
                    operation, operationTextVisible && !operationEditable);
        }

        super.setLabelInternal(newLabelText);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);

        if (operationSelector != null) {
            operationSelector.setEnabled(!readOnly);
        }
    }

    @Override
    public void setTabIndex(int tabIndex) {
        super.setTabIndex(tabIndex);

        if (operationSelector != null) {
            operationSelector.setTabIndex(tabIndex);
        }
    }

    protected static class OperationChangeAction extends BaseAction {

        protected Operation operation;
        protected BiConsumer<Operation, Boolean> handler;

        public OperationChangeAction(Operation operation, BiConsumer<Operation, Boolean> handler) {
            super(operation.name());

            this.operation = operation;
            this.handler = handler;
        }

        @Override
        public void actionPerform(Component component) {
            handler.accept(operation, true);
        }
    }

    /**
     * Operation representing corresponding filtering condition.
     */
    public enum Operation {
        EQUAL(Type.VALUE),
        NOT_EQUAL(Type.VALUE),
        GREATER(Type.VALUE),
        GREATER_OR_EQUAL(Type.VALUE),
        LESS(Type.VALUE),
        LESS_OR_EQUAL(Type.VALUE),
        CONTAINS(Type.VALUE),
        NOT_CONTAINS(Type.VALUE),
        STARTS_WITH(Type.VALUE),
        ENDS_WITH(Type.VALUE),
        IS_SET(Type.UNARY)/*,
        IS_NOT_SET(Type.UNARY),
        IN_LIST(Type.LIST),
        NOT_IN_LIST(Type.LIST),
        DATE_INTERVAL(Type.INTERVAL)*/;

        private final Type type;

        Operation(Type type) {
            this.type = type;
        }

        public Type getType() {
            return type;
        }

        /**
         * Operation type representing the required field type for editing
         * a property value with the given operation.
         */
        public enum Type {

            /**
             * Requires a field suitable for editing a property value, e.g.
             * {@link TypedTextField} for String, {@link JmixComboBox} for enum.
             */
            VALUE,

            /**
             * Requires a field suitable for choosing unary value, e.g. true/false, YES/NO.
             */
            UNARY,

            /**
             * Requires a field suitable for selecting multiple values of
             * the same type as the property value.
             */
            LIST,

            /**
             * Requires a field suitable for selecting a range of values of
             * the same type as the property value.
             */
            INTERVAL
        }
    }

    /**
     * Event sent when the {@code operation} property is changed.
     */
    public static class OperationChangeEvent<V> extends ComponentEvent<PropertyFilter<V>> {

        protected final Operation newOperation;
        protected final Operation prevOperation;

        public OperationChangeEvent(PropertyFilter<V> source,
                                    Operation newOperation, Operation prevOperation,
                                    boolean fromClient) {
            super(source, fromClient);
            this.prevOperation = prevOperation;
            this.newOperation = newOperation;
        }

        /**
         * @return new operation value
         */
        public Operation getNewOperation() {
            return newOperation;
        }

        /**
         * @return previous operation value
         */
        public Operation getPreviousOperation() {
            return prevOperation;
        }
    }
}
