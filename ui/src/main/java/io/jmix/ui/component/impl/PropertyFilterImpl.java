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
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.HorizontalLayout;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.*;
import io.jmix.ui.component.factory.PropertyFilterComponentGenerationContext;
import io.jmix.ui.model.DataLoader;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class PropertyFilterImpl<V> extends AbstractComponent<HorizontalLayout> implements PropertyFilter<V> {

    private DataLoader dataLoader;
    private boolean operationCaptionVisible = true;
    private CaptionPosition captionPosition = CaptionPosition.LEFT;
    private String caption;
    private String captionWidth;
    private boolean autoApply;

    private UiComponents uiComponents;
    private ObjectProvider<UiComponentsGenerator> uiComponentsGeneratorProvider;
    private HasValue<V> valueComponent;

    private PropertyCondition propertyCondition;

    public PropertyFilterImpl() {
        component = new HorizontalLayout();
        propertyCondition = new PropertyCondition();
    }

    @Autowired
    private void setUiComponents(UiComponents uiComponents) {
        this.uiComponents = uiComponents;
    }

    @Autowired
    private void setUiComponentsGeneratorProvider(ObjectProvider<UiComponentsGenerator> uiComponentsGeneratorProvider) {
        this.uiComponentsGeneratorProvider = uiComponentsGeneratorProvider;
    }

    @Override
    public DataLoader getDataLoader() {
        return dataLoader;
    }

    @Override
    public void setDataLoader(DataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    @Override
    public String getProperty() {
        return propertyCondition.getProperty();
    }

    @Override
    public void setProperty(String property) {
        this.propertyCondition.setProperty(property);
    }

    @Override
    public String getOperation() {
        return propertyCondition.getOperation();
    }

    @Override
    public void setOperation(String operation) {
        this.propertyCondition.setOperation(operation);
    }

    @Override
    public String getParameterName() {
        return this.propertyCondition.getParameterName();
    }

    @Override
    public void setParameterName(String parameterName) {
        this.propertyCondition.setParameterName(parameterName);
    }

    @Override
    public CaptionPosition getCaptionPosition() {
        return captionPosition;
    }

    @Override
    public void setCaptionPosition(CaptionPosition captionPosition) {
        this.captionPosition = captionPosition;
    }

    @Override
    public boolean isOperationCaptionVisible() {
        return operationCaptionVisible;
    }

    @Override
    public void setOperationCaptionVisible(boolean operationCaptionVisible) {
        this.operationCaptionVisible = operationCaptionVisible;
    }

    @Override
    public PropertyCondition getPropertyCondition() {
        return propertyCondition;
    }

    @Override
    public void setPropertyCondition(PropertyCondition propertyCondition) {
        this.propertyCondition = propertyCondition;
    }

    @Override
    public void createLayout() {
        BoxLayout rootLayout;
        if (captionPosition == CaptionPosition.LEFT) {
            rootLayout = uiComponents.create(HBoxLayout.NAME);
            rootLayout.setSpacing(true);
        } else {
            rootLayout = uiComponents.create(VBoxLayout.NAME);
        }

        Label<String> captionLabel = uiComponents.create(Label.NAME);
        captionLabel.setValue(caption);
        if (!Strings.isNullOrEmpty(captionWidth)) {
            captionLabel.setWidth(captionWidth);
        }
        captionLabel.setAlignment(Alignment.MIDDLE_LEFT);
        rootLayout.add(captionLabel);

        //create a valueComponent if it hasn't been set explicitly
        if (valueComponent == null) {
            UiComponentsGenerator uiComponentsGenerator = uiComponentsGeneratorProvider.getObject();
            ComponentGenerationContext context = new PropertyFilterComponentGenerationContext(dataLoader.getContainer().getEntityMetaClass(), propertyCondition);
            context.setTargetClass(PropertyFilter.class);
            Component generatedComponent = uiComponentsGenerator.generate(context);
            if (!(generatedComponent instanceof HasValue)) {
                throw new RuntimeException("Generated component must be an instance of HasValue. Component class is "
                        + generatedComponent.getClass().getName());
            }
            valueComponent = (HasValue<V>) generatedComponent;
        }

        if (valueComponent instanceof BelongToFrame) {
            ((BelongToFrame) valueComponent).setFrame(this.getFrame());
        }

        valueComponent.addValueChangeListener(valueChangeEvent -> {
            V value = valueComponent.getValue();
            propertyCondition.setParameterValue(value);
            if (autoApply) {
                dataLoader.load();
            }
            ValueChangeEvent<V> event = new ValueChangeEvent<>(this,
                    valueChangeEvent.getPrevValue(),
                    valueChangeEvent.getValue(),
                    false);
            publish(ValueChangeEvent.class, event);
        });
        rootLayout.add(valueComponent);
        //todo get rid of unnecessary nested components
        AbstractLayout vRootLayout = rootLayout.unwrap(AbstractLayout.class);
        component.addComponent(vRootLayout);
        rootLayout.setWidth("100%");
        if (captionPosition == CaptionPosition.LEFT) {
            rootLayout.expand(valueComponent);
        }
    }

    @Nullable
    @Override
    public V getValue() {
        return valueComponent.getValue();
    }

    @Override
    public void setValue(@Nullable V value) {
        valueComponent.setValue(value);
    }

    @Override
    public HasValue<V> getValueComponent() {
        return valueComponent;
    }

    @Override
    public void setValueComponent(HasValue<V> valueComponent) {
        this.valueComponent = valueComponent;
    }

    @Override
    public Subscription addValueChangeListener(Consumer<ValueChangeEvent<V>> listener) {
        return getEventHub().subscribe(ValueChangeEvent.class, (Consumer) listener);
    }

    @Override
    public void setCaption(@Nullable String caption) {
        this.caption = caption;
    }

    @Nullable
    @Override
    public String getCaption() {
        return caption;
    }

    @Override
    public String getCaptionWidth() {
        return captionWidth;
    }

    @Override
    public void setCaptionWidth(String captionWidth) {
        this.captionWidth = captionWidth;
    }

    @Override
    public boolean isAutoApply() {
        return autoApply;
    }

    @Override
    public void setAutoApply(boolean autoApply) {
        this.autoApply = autoApply;
    }
}
