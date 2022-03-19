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

package io.jmix.ui.component.impl;

import com.google.common.base.Strings;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.HBoxLayout;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.PopupButton;
import io.jmix.ui.component.PropertyFilter;
import io.jmix.ui.component.propertyfilter.PropertyFilterSupport;
import io.jmix.ui.component.propertyfilter.SingleFilterSupport;
import io.jmix.ui.model.DataLoader;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkState;
import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;
import static io.jmix.ui.theme.ThemeClassNames.POPUP_BUTTON_NO_POPUP_INDICATOR;

public class PropertyFilterImpl<V> extends AbstractSingleFilterComponent<V> implements PropertyFilter<V> {

    protected static final String PROPERTY_FILTER_STYLENAME = "jmix-property-filter";

    protected PropertyFilterSupport propertyFilterSupport;
    protected SingleFilterSupport singleFilterSupport;

    protected PopupButton operationSelector;

    protected Operation operation;
    protected boolean operationEditable = false;
    protected boolean operationCaptionVisible = true;

    @Autowired
    public void setPropertyFilterSupport(PropertyFilterSupport propertyFilterSupport) {
        this.propertyFilterSupport = propertyFilterSupport;
    }

    @Autowired
    public void setSingleFilterSupport(SingleFilterSupport singleFilterSupport) {
        this.singleFilterSupport = singleFilterSupport;
    }

    @Override
    protected void initRootComponent(HBoxLayout root) {
        super.initRootComponent(root);

        root.unwrap(com.vaadin.ui.Component.class)
                .setPrimaryStyleName(PROPERTY_FILTER_STYLENAME);
    }

    @Override
    public String getInnerComponentPrefix() {
        return propertyFilterSupport.getPropertyFilterPrefix(getId(), getProperty());
    }

    protected PopupButton createOperationSelector() {
        PopupButton operationSelector = uiComponents.create(PopupButton.class);
        operationSelector.setId(getInnerComponentPrefix() + "operationSelector");
        operationSelector.addStyleName(POPUP_BUTTON_NO_POPUP_INDICATOR);
        operationSelector.setAlignment(getChildAlignment());

        initOperationSelectorActions(operationSelector);

        return operationSelector;
    }

    protected void initOperationSelectorActions(@Nullable PopupButton operationSelector) {
        //noinspection ConstantConditions
        if (operationSelector != null
                && operationSelector.getActions().isEmpty()
                && dataLoader != null
                && getProperty() != null) {
            MetaClass metaClass = dataLoader.getContainer().getEntityMetaClass();

            for (Operation operation : propertyFilterSupport.getAvailableOperations(metaClass, getProperty())) {
                OperationChangeAction action = new OperationChangeAction(operation, this::setOperation);
                action.setCaption(propertyFilterSupport.getOperationCaption(operation));
                operationSelector.addAction(action);
            }

            if (operation != null) {
                operationSelector.setCaption(getOperationCaption(operation));
            }
        }
    }

    protected String getOperationCaption(Operation operation) {
        return propertyFilterSupport.getOperationCaption(operation);
    }

    @Override
    public void setDataLoader(DataLoader dataLoader) {
        super.setDataLoader(dataLoader);

        initOperationSelectorActions(operationSelector);
        updateCaption(caption);
    }

    @Override
    public String getProperty() {
        return getQueryCondition().getProperty();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void setProperty(String property) {
        checkState(getProperty() == null, "Property has already been initialized");
        checkNotNullArgument(property);

        getQueryCondition().setProperty(property);

        if (captionLabel != null) {
            captionLabel.setId(getInnerComponentPrefix() + "captionLabel");
        }

        initOperationSelectorActions(operationSelector);
        updateCaption(caption);
    }

    @Override
    public Operation getOperation() {
        return operation;
    }

    @Override
    public void setOperation(Operation operation) {
        checkNotNullArgument(operation);

        if (this.operation != operation) {
            setOperationInternal(operation);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked", "ConstantConditions"})
    protected void setOperationInternal(Operation operation) {
        getQueryCondition().setOperation(propertyFilterSupport.toPropertyConditionOperation(operation));

        if (operationSelector != null) {
            operationSelector.setCaption(getOperationCaption(operation));
        }

        if (this.valueComponent != null) {
            if (this.operation == null || this.operation.getType() != operation.getType()) {
                this.valueComponent.setValue(null);
            }

            if (!isConditionModificationDelegated()) {
                apply();
            }
        }

        if (this.operation == null
                || this.operation.getType() != operation.getType()) {
            if (dataLoader != null && getProperty() != null) {
                MetaClass metaClass = dataLoader.getContainer().getEntityMetaClass();
                HasValue newValueComponent = singleFilterSupport.generateValueComponent(metaClass,
                        getProperty(), operation);
                setValueComponent(newValueComponent);
            }
        }

        Operation prevOperation = this.operation != null ? this.operation : operation;
        this.operation = operation;

        updateCaption(caption);

        OperationChangeEvent operationChangeEvent = new OperationChangeEvent(this, operation, prevOperation);
        publish(OperationChangeEvent.class, operationChangeEvent);
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
    protected void updateChildAlignment() {
        super.updateChildAlignment();

        if (operationSelector != null) {
            operationSelector.setAlignment(getChildAlignment());
        }
    }

    @Override
    protected PropertyCondition createQueryCondition() {
        return new PropertyCondition();
    }

    @Override
    public PropertyCondition getQueryCondition() {
        return (PropertyCondition) queryCondition;
    }

    @Override
    protected void updateQueryCondition(@Nullable V newValue) {
        getQueryCondition().setParameterValue(newValue);
    }

    @Override
    public Subscription addOperationChangeListener(Consumer<OperationChangeEvent> listener) {
        return getEventHub().subscribe(OperationChangeEvent.class, listener);
    }

    @Override
    public boolean isOperationEditable() {
        return operationEditable;
    }

    @Override
    public void setOperationEditable(boolean operationEditable) {
        if (this.operationEditable != operationEditable) {
            this.operationEditable = operationEditable;

            removeOperationSelector();

            if (operationEditable) {
                operationSelector = createOperationSelector();
                root.add(operationSelector, captionPosition == CaptionPosition.TOP ? 0 : 1);
            }

            updateCaption(caption);
        }
    }

    @Override
    public boolean isOperationCaptionVisible() {
        return operationCaptionVisible;
    }

    @Override
    public void setOperationCaptionVisible(boolean operationCaptionVisible) {
        if (this.operationCaptionVisible != operationCaptionVisible) {
            this.operationCaptionVisible = operationCaptionVisible;

            updateCaption(caption);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void updateCaption(@Nullable String caption) {
        String newCaption;
        if (dataLoader == null || operation == null
                || getProperty() == null || !Strings.isNullOrEmpty(caption)) {
            newCaption = caption;
        } else {
            MetaClass metaClass = dataLoader.getContainer().getEntityMetaClass();
            newCaption = propertyFilterSupport.getPropertyFilterCaption(metaClass, getProperty(), operation,
                    operationCaptionVisible && !operationEditable);
        }

        super.updateCaption(newCaption);
    }

    protected void removeOperationSelector() {
        if (operationSelector != null) {
            root.remove(operationSelector);
            operationSelector = null;
        }
    }

    @Override
    public void setTabIndex(int tabIndex) {
        super.setTabIndex(tabIndex);

        if (operationSelector != null) {
            operationSelector.setTabIndex(tabIndex);
        }
    }

    protected static class OperationChangeAction extends AbstractAction {

        protected Operation operation;
        protected Consumer<Operation> handler;

        public OperationChangeAction(Operation operation, Consumer<Operation> handler) {
            super(operation.name());

            this.operation = operation;
            this.handler = handler;
        }

        @Override
        public void actionPerform(Component component) {
            handler.accept(operation);
        }
    }
}
