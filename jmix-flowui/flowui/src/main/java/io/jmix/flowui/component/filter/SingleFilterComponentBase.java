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

package io.jmix.flowui.component.filter;

import com.google.common.base.Strings;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.dom.PropertyChangeEvent;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.annotation.Internal;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.flowui.UiComponentProperties;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.*;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.model.BaseCollectionLoader;
import io.jmix.flowui.model.DataLoader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import org.springframework.lang.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkState;
import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public abstract class SingleFilterComponentBase<V> extends CustomField<V>
        implements SingleFilterComponent<V>, SupportsLabelPosition, SupportsValidation<V>, HasRequired, HasTooltip,
        ApplicationContextAware, InitializingBean {

    protected static final String FILTER_LABEL_CLASS_NAME = "filter-label";

    protected ApplicationContext applicationContext;
    protected UiComponents uiComponents;

    protected HasValueAndElement<?, V> valueComponent;

    protected DataLoader dataLoader;
    protected boolean autoApply;
    protected Condition queryCondition;

    @Internal
    protected boolean conditionModificationDelegated = false;

    protected HorizontalLayout root;

    protected Label label;
    protected String labelText;
    protected String labelWidth;
    protected boolean labelVisible = true;

    protected LabelPosition labelPosition = LabelPosition.ASIDE;

    @Internal
    protected Consumer<String> labelDelegate;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        autowireDependencies();
        initComponent();
    }

    protected void autowireDependencies() {
        uiComponents = applicationContext.getBean(UiComponents.class);
    }

    protected void initComponent() {
        this.autoApply = applicationContext.getBean(UiComponentProperties.class).isFilterAutoApply();

        root = createRootComponent();
        initRootComponent(root);
        add(root);

        queryCondition = createQueryCondition();

        updateLabelLayout();
        addValueChangeListener(this::onFilterValueChanged);
    }

    protected void onFilterValueChanged(ComponentValueChangeEvent<CustomField<V>, V> event) {
        updateQueryCondition(event.getValue());
        apply();
    }

    protected HorizontalLayout createRootComponent() {
        return uiComponents.create(HorizontalLayout.class);
    }

    protected void initRootComponent(HorizontalLayout root) {
        root.setSpacing(false);
        root.getThemeList().add("spacing-s");
    }

    public HorizontalLayout getRoot() {
        return root;
    }

    @Override
    protected V generateModelValue() {
        checkValueComponentState();
        //noinspection DataFlowIssue
        return UiComponentUtils.getValue(valueComponent);
    }

    @Override
    protected void setPresentationValue(V newPresentationValue) {
        checkValueComponentState();
        UiComponentUtils.setValue(valueComponent, newPresentationValue);
    }

    protected abstract Condition createQueryCondition();

    protected abstract void updateQueryCondition(@Nullable V newValue);

    @Override
    public DataLoader getDataLoader() {
        return dataLoader;
    }

    @Override
    public void setDataLoader(DataLoader dataLoader) {
        checkState(this.dataLoader == null, DataLoader.class.getSimpleName() + " has already been initialized");
        checkNotNullArgument(dataLoader);

        this.dataLoader = dataLoader;

        if (!isConditionModificationDelegated()) {
            updateDataLoaderCondition();
        }
    }

    protected void updateDataLoaderCondition() {
        Condition rootCondition = dataLoader.getCondition();
        if (rootCondition == null) {
            rootCondition = LogicalCondition.and();
            dataLoader.setCondition(rootCondition);
        }

        if (!(rootCondition instanceof LogicalCondition)) {
            rootCondition = LogicalCondition.and().add(rootCondition);
            dataLoader.setCondition(rootCondition);
        }

        ((LogicalCondition) rootCondition).add(queryCondition);
    }

    @Override
    public boolean isAutoApply() {
        return autoApply;
    }

    @Override
    public void setAutoApply(boolean autoApply) {
        this.autoApply = autoApply;
    }

    @Internal
    @Override
    public boolean isConditionModificationDelegated() {
        return conditionModificationDelegated;
    }

    @Internal
    @Override
    public void setConditionModificationDelegated(boolean conditionModificationDelegated) {
        this.conditionModificationDelegated = conditionModificationDelegated;
    }

    @Override
    public Condition getQueryCondition() {
        return queryCondition;
    }

    @Override
    public void apply() {
        if (dataLoader != null) {
            setupLoaderFirstResult();
            if (autoApply) {
                dataLoader.load();
            }
        }
    }

    protected void setupLoaderFirstResult() {
        if (dataLoader instanceof BaseCollectionLoader) {
            ((BaseCollectionLoader) dataLoader).setFirstResult(0);
        }
    }

    @Nullable
    @Override
    public String getLabel() {
        return labelText;
    }

    @Override
    public void setLabel(@Nullable String label) {
        if (!Objects.equals(this.labelText, label)) {
            this.labelText = label;
            setLabelInternal(label);
        }
    }

    protected void setLabelInternal(@Nullable String labelText) {
        if (labelDelegate != null) {
            labelDelegate.accept(labelText);
        } else if (labelPosition == LabelPosition.TOP) {
            super.setLabel(labelVisible ? labelText : null);
        } else {
            this.label.setText(labelVisible ? labelText : null);
            this.label.setVisible(labelVisible);
        }
    }

    @Internal
    public void setLabelDelegate(Consumer<String> labelDelegate) {
        this.labelDelegate = labelDelegate;

        updateLabelLayout();
    }

    @Override
    public LabelPosition getLabelPosition() {
        return labelPosition;
    }

    @Override
    public void setLabelPosition(LabelPosition labelPosition) {
        if (this.labelPosition != labelPosition) {
            this.labelPosition = labelPosition;

            updateLabelLayout();
            updateChildAlignment();
        }
    }

    protected void updateChildAlignment() {
        root.setAlignItems(labelPosition == LabelPosition.ASIDE
                ? FlexComponent.Alignment.CENTER
                : FlexComponent.Alignment.START);
    }

    protected void updateLabelLayout() {
        if (labelDelegate != null) {
            if (label != null) {
                root.remove(label);
                label = null;
            }
            super.setLabel(null);
        } else if (labelPosition == LabelPosition.ASIDE) {
            super.setLabel(null);
            label = createLabel();
            root.addComponentAsFirst(label);
        } else {
            root.remove(label);
            label = null;
        }

        setLabelInternal(labelText);
    }

    protected Label createLabel() {
        Label label = uiComponents.create(Label.class);
        label.setId(getInnerComponentPrefix() + "label");
        label.setWidth(labelWidth);
        label.setClassName(FILTER_LABEL_CLASS_NAME);
        return label;
    }

    public abstract String getInnerComponentPrefix();

    @Override
    @Nullable
    public String getLabelWidth() {
        return label != null ? label.getWidth() : null;
    }

    @Override
    public void setLabelWidth(@Nullable String labelWidth) {
        this.labelWidth = labelWidth;

        if (label != null) {
            label.setWidth(labelWidth);
        }
    }

    @Override
    public boolean isLabelVisible() {
        return labelVisible;
    }

    @Override
    public void setLabelVisible(boolean labelVisible) {
        if (this.labelVisible != labelVisible) {
            this.labelVisible = labelVisible;

            setLabelInternal(labelText);
        }
    }

    @Override
    public void setWidth(String width) {
        super.setWidth(width);

        if (valueComponent != null) {
            if (Strings.isNullOrEmpty(width)) {
                // Same as remove expand
                root.setFlexGrow(0.0, valueComponent);
            } else {
                root.expand(((Component) valueComponent));
            }
        }
    }

    public HasValueAndElement<?, V> getValueComponent() {
        return valueComponent;
    }

    public void setValueComponent(HasValueAndElement<?, V> valueComponent) {
        checkNotNullArgument(valueComponent);

        if (this.valueComponent != null) {
            root.remove(((Component) this.valueComponent));
        }

        this.valueComponent = valueComponent;
        root.add(((Component) valueComponent));

        initValueComponent(valueComponent);

        if (label != null) {
            label.setFor(((Component) valueComponent));
        }
    }

    protected void initValueComponent(HasValueAndElement<?, V> valueComponent) {
        ((Component) valueComponent).setId(getInnerComponentPrefix() + "valueComponent");

        if (valueComponent instanceof SupportsTypedValue) {
            //noinspection unchecked
            ((SupportsTypedValue<?, ?, V, ?>) valueComponent).addTypedValueChangeListener(this::onFieldValueChanged);
        } else {
            valueComponent.addValueChangeListener(this::onFieldValueChanged);
        }

        if (valueComponent instanceof SupportsStatusChangeHandler) {
            ((SupportsStatusChangeHandler<?>) valueComponent).setStatusChangeHandler(this::onFieldStatusChanged);
        }

        valueComponent.getElement().addPropertyChangeListener("invalid", this::onFieldInvalidChanged);

        String width = getWidth();
        if (!Strings.isNullOrEmpty(width)
                && Unit.getSize(width) > 0) {
            root.expand(((Component) valueComponent));
        }
    }

    protected void onFieldValueChanged(ValueChangeEvent<V> valueChangeEvent) {
        setModelValue(valueChangeEvent.getValue(), valueChangeEvent.isFromClient());
    }

    protected void onFieldStatusChanged(SupportsStatusChangeHandler.StatusContext<?> statusContext) {
        setErrorMessage(statusContext.getDescription());
    }

    protected void onFieldInvalidChanged(PropertyChangeEvent event) {
        if (event.getValue() instanceof Boolean) {
            super.setInvalid(((Boolean) event.getValue()));
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        if (valueComponent != null) {
            valueComponent.setReadOnly(readOnly);
        }
    }

    @Override
    public boolean isInvalid() {
        return valueComponent instanceof HasValidation
                ? ((HasValidation) valueComponent).isInvalid()
                : super.isInvalid();
    }

    @Override
    public void setInvalid(boolean invalid) {
        super.setInvalid(invalid);

        if (valueComponent instanceof SupportsValidation) {
            ((SupportsValidation<?>) valueComponent).setInvalid(invalid);
        }
    }

    @Nullable
    @Override
    public String getRequiredMessage() {
        return valueComponent instanceof HasRequired
                ? ((HasRequired) valueComponent).getRequiredMessage()
                : null;
    }

    @Override
    public void setRequiredMessage(@Nullable String requiredMessage) {
        if (valueComponent instanceof HasRequired) {
            ((HasRequired) valueComponent).setRequiredMessage(requiredMessage);
        }
    }

    @Override
    public void setRequired(boolean required) {
        HasRequired.super.setRequired(required);

        if (valueComponent instanceof HasRequired) {
            ((HasRequired) valueComponent).setRequired(required);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Registration addValidator(Validator<? super V> validator) {
        if (valueComponent instanceof SupportsValidation) {
            return ((SupportsValidation<V>) valueComponent).addValidator(validator);
        }

        throw new IllegalStateException("Value component doesn't support validation");
    }

    @Override
    public void executeValidators() throws ValidationException {
        if (valueComponent instanceof SupportsValidation) {
            ((SupportsValidation<?>) valueComponent).executeValidators();
        }
    }

    @Override
    public void setTabIndex(int tabIndex) {
        super.setTabIndex(tabIndex);

        if (valueComponent instanceof Focusable) {
            ((Focusable<?>) valueComponent).setTabIndex(tabIndex);
        }
    }

    @Override
    public void focus() {
        if (valueComponent instanceof Focusable) {
            ((Focusable<?>) valueComponent).focus();
        } else {
            super.focus();
        }
    }

    @Override
    public void blur() {
        if (valueComponent instanceof Focusable) {
            ((Focusable<?>) valueComponent).blur();
        } else {
            super.blur();
        }
    }

    protected void checkValueComponentState() {
        checkState(valueComponent != null, "Value component isn't set");
    }
}
