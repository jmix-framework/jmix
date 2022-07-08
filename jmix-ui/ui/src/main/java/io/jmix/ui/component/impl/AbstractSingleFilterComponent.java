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
import io.jmix.core.annotation.Internal;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.ui.UiComponentProperties;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.*;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.model.BaseCollectionLoader;
import io.jmix.ui.model.DataLoader;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkState;
import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public abstract class AbstractSingleFilterComponent<V> extends CompositeComponent<HBoxLayout>
        implements SingleFilterComponent<V>, CompositeWithHtmlCaption, CompositeWithHtmlDescription {

    protected UiComponents uiComponents;

    protected Label<String> captionLabel;
    protected HasValue<V> valueComponent;

    protected DataLoader dataLoader;
    protected boolean autoApply;
    protected Condition queryCondition;

    protected boolean captionVisible = true;

    @Internal
    protected boolean conditionModificationDelegated = false;

    protected String caption;
    protected String captionWidth;
    protected String icon;

    protected CaptionPosition captionPosition = CaptionPosition.LEFT;

    public AbstractSingleFilterComponent() {
        addCreateListener(this::onCreate);
    }

    @Autowired
    public void setUiComponents(UiComponents uiComponents) {
        this.uiComponents = uiComponents;
    }

    @Autowired
    public void setUiComponentProperties(UiComponentProperties componentProperties) {
        this.autoApply = componentProperties.isFilterAutoApply();
    }

    protected void onCreate(CreateEvent createEvent) {
        root = createRootComponent();
        queryCondition = createQueryCondition();
        initRootComponent(root);
        updateCaptionLayout();
    }

    protected HBoxLayout createRootComponent() {
        return uiComponents.create(HBoxLayout.class);
    }

    protected abstract Condition createQueryCondition();

    protected void initRootComponent(HBoxLayout root) {
        root.setSpacing(true);
    }

    protected Label<String> createCaptionLabel() {
        Label<String> label = uiComponents.create(Label.TYPE_DEFAULT);
        label.setId(getInnerComponentPrefix() + "captionLabel");
        label.setAlignment(Alignment.MIDDLE_LEFT);
        label.setWidth(captionWidth);
        label.setValue(caption);
        label.setIcon(icon);
        label.setHtmlEnabled(isCaptionAsHtml());
        return label;
    }

    public abstract String getInnerComponentPrefix();

    @Override
    public DataLoader getDataLoader() {
        return dataLoader;
    }

    @Override
    public void setDataLoader(DataLoader dataLoader) {
        checkState(this.dataLoader == null, "DataLoader has already been initialized");
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
    public CaptionPosition getCaptionPosition() {
        return captionPosition;
    }

    @Override
    public void setCaptionPosition(CaptionPosition captionPosition) {
        if (this.captionPosition != captionPosition) {
            this.captionPosition = captionPosition;
            updateCaptionLayout();
            updateChildAlignment();
            updateCaption(caption);
        }
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

    protected void updateChildAlignment() {
        if (valueComponent != null) {
            valueComponent.setAlignment(getChildAlignment());
        }
    }

    protected Alignment getChildAlignment() {
        return captionPosition == CaptionPosition.LEFT
                ? Alignment.MIDDLE_LEFT
                : Alignment.TOP_LEFT;
    }

    @Nullable
    @Override
    public String getCaption() {
        return caption;
    }

    @Override
    public void setCaption(@Nullable String caption) {
        if (!Objects.equals(this.caption, caption)) {
            this.caption = caption;
            updateCaption(caption);
        }
    }

    protected void updateCaption(@Nullable String caption) {
        if (captionPosition == CaptionPosition.TOP) {
            root.setCaption(captionVisible ? caption : null);
        } else {
            captionLabel.setValue(captionVisible ? caption : null);
            captionLabel.setVisible(captionVisible || !Strings.isNullOrEmpty(icon));
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
    public boolean isCaptionVisible() {
        return captionVisible;
    }

    @Override
    public void setCaptionVisible(boolean captionVisible) {
        if (this.captionVisible != captionVisible) {
            this.captionVisible = captionVisible;

            updateCaption(caption);
        }
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
        valueComponent.setId(getInnerComponentPrefix() + "valueComponent");

        valueComponent.addValueChangeListener(this::onValueChanged);
        valueComponent.setAlignment(getChildAlignment());

        if (getWidth() > 0) {
            root.expand(valueComponent);
        }
    }

    protected void onValueChanged(ValueChangeEvent<V> valueChangeEvent) {
        updateQueryCondition(valueChangeEvent.getValue());

        if (valueChangeEvent.isUserOriginated()) {
            apply();
        }
    }

    @Override
    public void apply() {
        if (dataLoader != null) {
            setupLoaderFirstResult();
            if (autoApply) dataLoader.load();
        }
    }

    protected abstract void updateQueryCondition(@Nullable V newValue);

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

    protected void checkValueComponentState() {
        checkState(valueComponent != null, "Value component isn't set");
    }

    @Override
    public Subscription addValueChangeListener(Consumer<ValueChangeEvent<V>> listener) {
        checkValueComponentState();
        return valueComponent.addValueChangeListener(listener);
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
        } else if (captionLabel != null) {
            captionLabel.setIcon(icon);
        }

        updateCaption(caption);
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
    }

    @Nullable
    @Override
    public String getContextHelpText() {
        return valueComponent instanceof HasContextHelp
                ? ((HasContextHelp) valueComponent).getContextHelpText()
                : null;
    }

    @Override
    public void setContextHelpText(@Nullable String contextHelpText) {
        if (valueComponent instanceof HasContextHelp) {
            ((HasContextHelp) valueComponent).setContextHelpText(contextHelpText);
        }
    }

    @Override
    public boolean isContextHelpTextHtmlEnabled() {
        return valueComponent instanceof HasContextHelp
                && ((HasContextHelp) valueComponent).isContextHelpTextHtmlEnabled();
    }

    @Override
    public void setContextHelpTextHtmlEnabled(boolean enabled) {
        if (valueComponent instanceof HasContextHelp) {
            ((HasContextHelp) valueComponent).setContextHelpTextHtmlEnabled(enabled);
        }
    }

    @Nullable
    @Override
    public Consumer<ContextHelpIconClickEvent> getContextHelpIconClickHandler() {
        return valueComponent instanceof HasContextHelp
                ? ((HasContextHelp) valueComponent).getContextHelpIconClickHandler()
                : null;
    }

    @Override
    public void setContextHelpIconClickHandler(@Nullable Consumer<ContextHelpIconClickEvent> handler) {
        if (valueComponent instanceof HasContextHelp) {
            ((HasContextHelp) valueComponent).setContextHelpIconClickHandler(handler);
        }
    }

    @Override
    public boolean isRequired() {
        return valueComponent instanceof Requirable
                && ((Requirable) valueComponent).isRequired();
    }

    @Override
    public void setRequired(boolean required) {
        if (valueComponent instanceof Requirable) {
            ((Requirable) valueComponent).setRequired(required);
        }
    }

    @Nullable
    @Override
    public String getRequiredMessage() {
        return valueComponent instanceof Requirable
                ? ((Requirable) valueComponent).getRequiredMessage()
                : null;
    }

    @Override
    public void setRequiredMessage(@Nullable String msg) {
        if (valueComponent instanceof Requirable) {
            ((Requirable) valueComponent).setRequiredMessage(msg);
        }
    }

    @Override
    public boolean isValid() {
        return valueComponent instanceof Validatable
                && ((Validatable) valueComponent).isValid();
    }

    @Override
    public void validate() throws ValidationException {
        if (valueComponent instanceof Validatable) {
            ((Validatable) valueComponent).validate();
        }
    }

    protected void setupLoaderFirstResult() {
        if (dataLoader instanceof BaseCollectionLoader) {
            ((BaseCollectionLoader) dataLoader).setFirstResult(0);
        }
    }
}
