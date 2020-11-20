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

import io.jmix.core.common.event.Subscription;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.component.*;
import io.jmix.ui.component.propertyfilter.PropertyFilterSupport;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.model.DataLoader;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkState;
import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;
import static io.jmix.ui.theme.HaloTheme.POPUP_BUTTON_NO_POPUP_INDICATOR;

public class PropertyFilterImpl<V> extends CompositeComponent<HBoxLayout> implements PropertyFilter<V>,
        CompositeWithHtmlCaption, CompositeWithHtmlDescription {

    protected static final String PROPERTY_FILTER_STYLENAME = "jmix-property-filter";

    protected UiComponents uiComponents;
    protected PropertyFilterSupport propertyFilterSupport;

    protected Label<String> captionLabel;
    protected PopupButton operationSelector;
    protected HasValue<V> valueComponent;

    protected DataLoader dataLoader;

    protected String caption;
    protected String captionWidth;
    protected String icon;

    protected CaptionPosition captionPosition = CaptionPosition.LEFT;
    protected PropertyCondition propertyCondition = new PropertyCondition();

    protected boolean autoApply;

    protected Operation operation;
    protected boolean operationEditable = false;

    public PropertyFilterImpl() {
        addCreateListener(this::onCreate);
    }

    @Autowired
    public void setUiComponents(UiComponents uiComponents) {
        this.uiComponents = uiComponents;
    }

    @Autowired
    public void setPropertyFilterSupport(PropertyFilterSupport propertyFilterSupport) {
        this.propertyFilterSupport = propertyFilterSupport;
    }

    protected void onCreate(CreateEvent createEvent) {
        root = createRootComponent();
        initRootComponent(root);
        updateCaptionLayout();
    }

    protected HBoxLayout createRootComponent() {
        return uiComponents.create(HBoxLayout.class);
    }

    protected void initRootComponent(HBoxLayout root) {
        root.unwrap(com.vaadin.ui.Component.class)
                .setPrimaryStyleName(PROPERTY_FILTER_STYLENAME);
        root.setSpacing(true);
    }

    protected void updateCaptionLayout() {
        if (captionPosition == CaptionPosition.LEFT) {
            root.setCaption(null);
            root.setIcon(null);

            captionLabel = createCaptionLabel();
            root.add(captionLabel, 0);
        } else {
            root.remove(captionLabel);
            captionLabel = null;
            root.setCaption(caption);
            root.setIcon(icon);
        }
    }

    protected Label<String> createCaptionLabel() {
        Label<String> label = uiComponents.create(Label.TYPE_DEFAULT);

        label.setAlignment(Alignment.MIDDLE_LEFT);
        label.setWidth(captionWidth);
        label.setValue(caption);
        label.setIcon(icon);
        label.setHtmlEnabled(isCaptionAsHtml());

        return label;
    }

    protected PopupButton createOperationSelector() {
        PopupButton operationSelector = uiComponents.create(PopupButton.class);
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
    public DataLoader getDataLoader() {
        return dataLoader;
    }

    @Override
    public void setDataLoader(DataLoader dataLoader) {
        checkState(this.dataLoader == null, "DataLoader has already been initialized");
        checkNotNullArgument(dataLoader);

        this.dataLoader = dataLoader;

        Condition rootCondition = dataLoader.getCondition();
        if (rootCondition == null) {
            rootCondition = new LogicalCondition(LogicalCondition.Type.AND);
            dataLoader.setCondition(rootCondition);
        }

        if (rootCondition instanceof LogicalCondition) {
            ((LogicalCondition) rootCondition).add(propertyCondition);
        }

        initOperationSelectorActions(operationSelector);
    }

    @Override
    public String getProperty() {
        return propertyCondition.getProperty();
    }

    @Override
    public void setProperty(String property) {
        //noinspection ConstantConditions
        checkState(this.propertyCondition.getProperty() == null, "Property has already been initialized");
        checkNotNullArgument(property);

        this.propertyCondition.setProperty(property);

        initOperationSelectorActions(operationSelector);
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

    protected void setOperationInternal(Operation operation) {
        propertyCondition.setOperation(propertyFilterSupport.toPropertyConditionOperation(operation));

        if (operationSelector != null) {
            operationSelector.setCaption(getOperationCaption(operation));
        }

        if (this.valueComponent != null) {
            this.valueComponent.setValue(null);
        }

        if (this.operation == null
                || this.operation.getType() != operation.getType()) {
            //noinspection ConstantConditions
            if (dataLoader != null && getProperty() != null) {
                MetaClass metaClass = dataLoader.getContainer().getEntityMetaClass();
                HasValue newValueComponent = propertyFilterSupport.generateValueField(metaClass, getProperty(), operation);
                setValueComponent(newValueComponent);
            }
        }

        this.operation = operation;
    }

    @Override
    public String getParameterName() {
        return this.propertyCondition.getParameterName();
    }

    @Override
    public void setParameterName(String parameterName) {
        //noinspection ConstantConditions
        checkState(this.propertyCondition.getParameterName() == null,
                "Parameter name has already been initialized");
        checkNotNullArgument(parameterName);

        this.propertyCondition.setParameterName(parameterName);
    }

    @Override
    public CaptionPosition getCaptionPosition() {
        return captionPosition;
    }

    @Override
    public void setCaptionPosition(CaptionPosition captionPosition) {
        if (this.captionPosition != captionPosition) {
            this.captionPosition = captionPosition;
            updateCaptionLayout();
            updateChildAlignment();
        }
    }

    protected void updateChildAlignment() {
        if (valueComponent != null) {
            valueComponent.setAlignment(getChildAlignment());
        }

        if (operationSelector != null) {
            operationSelector.setAlignment(getChildAlignment());
        }
    }

    protected Alignment getChildAlignment() {
        return captionPosition == CaptionPosition.LEFT
                ? Alignment.MIDDLE_LEFT
                : Alignment.TOP_LEFT;
    }

    public PropertyCondition getPropertyCondition() {
        return propertyCondition;
    }

    @Nullable
    @Override
    public V getValue() {
        checkValueComponentState();
        return valueComponent.getValue();
    }

    @Override
    public void setValue(@Nullable V value) {
        checkValueComponentState();
        valueComponent.setValue(value);
    }

    @Override
    public HasValue<V> getValueComponent() {
        return valueComponent;
    }

    @Override
    public void setValueComponent(HasValue<V> valueComponent) {
        checkNotNullArgument(valueComponent);

        if (this.valueComponent != null) {
            root.remove(this.valueComponent);
        }

        this.valueComponent = valueComponent;
        root.add(valueComponent);

        initValueComponent(valueComponent);
    }

    protected void initValueComponent(HasValue<V> valueComponent) {
        valueComponent.addValueChangeListener(this::onValueChanged);
        valueComponent.setAlignment(getChildAlignment());

        if (getWidth() > 0) {
            root.expand(valueComponent);
        }
    }

    protected void onValueChanged(ValueChangeEvent<V> valueChangeEvent) {
        V value = valueChangeEvent.getValue();
        propertyCondition.setParameterValue(value);

        if (autoApply) {
            dataLoader.load();
        }

        ValueChangeEvent<V> event = new ValueChangeEvent<>(this,
                valueChangeEvent.getPrevValue(),
                valueChangeEvent.getValue(),
                valueChangeEvent.isUserOriginated());
        publish(ValueChangeEvent.class, event);
    }

    @Override
    public Subscription addValueChangeListener(Consumer<ValueChangeEvent<V>> listener) {
        return getEventHub().subscribe(ValueChangeEvent.class, (Consumer) listener);
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
        }
    }

    protected void removeOperationSelector() {
        if (operationSelector != null) {
            root.remove(operationSelector);
            operationSelector = null;
        }
    }

    @Nullable
    @Override
    public String getCaption() {
        return caption;
    }

    @Override
    public void setCaption(@Nullable String caption) {
        this.caption = caption;
        updateCaption();
    }

    protected void updateCaption() {
        if (captionPosition == CaptionPosition.TOP) {
            root.setCaption(caption);
        } else {
            captionLabel.setValue(caption);
        }
    }

    @Override
    public void setCaptionAsHtml(boolean captionAsHtml) {
        root.setCaptionAsHtml(captionAsHtml);

        if (captionLabel != null) {
            captionLabel.setHtmlEnabled(captionAsHtml);
        }
    }

    @Override
    public float getCaptionWidth() {
        return captionLabel != null ? captionLabel.getWidth() : AUTO_SIZE_PX;
    }

    @Override
    public SizeUnit getCaptionWidthSizeUnit() {
        return captionLabel != null ? captionLabel.getWidthSizeUnit() : SizeUnit.PIXELS;
    }

    @Override
    public void setCaptionWidth(String captionWidth) {
        this.captionWidth = captionWidth;

        if (captionLabel != null) {
            captionLabel.setWidth(captionWidth);
        }
    }

    @Override
    public boolean isAutoApply() {
        return autoApply;
    }

    @Override
    public void setAutoApply(boolean autoApply) {
        this.autoApply = autoApply;
    }

    @Override
    public void setWidth(@Nullable String width) {
        super.setWidth(width);

        if (valueComponent != null) {
            if (Component.AUTO_SIZE.equals(width) || width == null) {
                root.resetExpanded();
                valueComponent.setWidthAuto();
            } else {
                root.expand(valueComponent);
            }
        }
    }

    @Nullable
    @Override
    public String getIcon() {
        return icon;
    }

    @Override
    public void setIcon(@Nullable String icon) {
        this.icon = icon;
        updateIcon();
    }

    protected void updateIcon() {
        if (captionPosition == CaptionPosition.TOP) {
            root.setIcon(icon);
        } else {
            captionLabel.setIcon(icon);
        }
    }

    @Override
    public void setIconFromSet(@Nullable Icons.Icon icon) {
        String iconName = getIconName(icon);
        setIcon(iconName);
    }

    @Nullable
    protected String getIconName(@Nullable Icons.Icon icon) {
        return applicationContext.getBean(Icons.class).get(icon);
    }

    @Override
    public boolean isEditable() {
        return valueComponent instanceof Editable
                && ((Editable) valueComponent).isEditable();
    }

    @Override
    public void setEditable(boolean editable) {
        if (valueComponent instanceof Editable) {
            ((Editable) valueComponent).setEditable(editable);
        }

        if (operationSelector != null) {
            operationSelector.setEnabled(editable);
        }
    }

    @Override
    public void focus() {
        if (valueComponent instanceof Focusable) {
            ((Focusable) valueComponent).focus();
        }
    }

    @Override
    public int getTabIndex() {
        return valueComponent instanceof Focusable
                ? ((Focusable) valueComponent).getTabIndex()
                : 0;
    }

    @Override
    public void setTabIndex(int tabIndex) {
        if (valueComponent instanceof Focusable) {
            ((Focusable) valueComponent).setTabIndex(tabIndex);
        }

        if (operationSelector != null) {
            operationSelector.setTabIndex(tabIndex);
        }
    }

    protected void checkValueComponentState() {
        checkState(valueComponent != null, "Value component isn't set");
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
