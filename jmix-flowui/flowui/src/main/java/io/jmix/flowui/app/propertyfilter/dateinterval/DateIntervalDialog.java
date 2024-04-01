/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.app.propertyfilter.dateinterval;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.app.propertyfilter.dateinterval.component.LastIntervalField;
import io.jmix.flowui.app.propertyfilter.dateinterval.component.NextIntervalField;
import io.jmix.flowui.app.propertyfilter.dateinterval.model.BaseDateInterval;
import io.jmix.flowui.app.propertyfilter.dateinterval.model.BaseDateInterval.Type;
import io.jmix.flowui.app.propertyfilter.dateinterval.model.DateInterval;
import io.jmix.flowui.app.propertyfilter.dateinterval.model.DateInterval.TimeUnit;
import io.jmix.flowui.app.propertyfilter.dateinterval.model.predefined.PredefinedDateInterval;
import io.jmix.flowui.app.propertyfilter.dateinterval.model.predefined.PredefinedDateIntervalRegistry;
import io.jmix.flowui.component.checkboxgroup.JmixCheckboxGroup;
import io.jmix.flowui.component.radiobuttongroup.JmixRadioButtonGroup;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.sql.Time;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.util.*;

@ViewController("flowui_DateIntervalDialog")
@ViewDescriptor("date-interval-dialog.xml")
@DialogMode(width = "50em")
public class DateIntervalDialog extends StandardView {

    @Autowired
    protected Messages messages;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected ViewValidation viewValidation;
    @Autowired
    protected PredefinedDateIntervalRegistry intervalFactory;

    @ViewComponent
    protected JmixRadioButtonGroup<Type> typeRadioButtonGroup;
    @ViewComponent
    protected HorizontalLayout contentBox;
    @ViewComponent
    protected JmixSelect<PredefinedDateInterval> predefinedIntervalsSelect;

    protected LastIntervalField lastIntervalField;
    protected NextIntervalField nextIntervalField;

    protected Map<Type, Component> componentVisibilityMap = new HashMap<>();

    protected BaseDateInterval value;
    protected MetaPropertyPath mpp;

    @Subscribe
    protected void onInit(InitEvent event) {
        initComponents();
    }

    protected void initComponents() {
        initTypeRadioButtonGroup();
        initNextLastIntervalField();
        initPredefinedIntervalsSelect();
        initComponentVisibilityMap();
    }

    @Subscribe
    protected void onReady(ReadyEvent event) {
        filterOptionsByPropertyType();
        setupInitialValues();
    }

    protected void initTypeRadioButtonGroup() {
        Map<BaseDateInterval.Type, String> localizationMap =
                getLocalizedEnumMap(Arrays.asList(Type.LAST, Type.NEXT, Type.PREDEFINED));

        ComponentUtils.setItemsMap(typeRadioButtonGroup, localizationMap);
    }

    protected void initNextLastIntervalField() {
        lastIntervalField = uiComponents.create(LastIntervalField.class);
        nextIntervalField = uiComponents.create(NextIntervalField.class);

        Map<TimeUnit, String> localizationMap = getLocalizedEnumMap(TimeUnit.class);
        lastIntervalField.setTimeUnitItemsMap(localizationMap);
        lastIntervalField.setWidthFull();

        nextIntervalField.setTimeUnitItemsMap(localizationMap);
        nextIntervalField.setWidthFull();

        contentBox.add(lastIntervalField, nextIntervalField);
    }

    protected void initPredefinedIntervalsSelect() {
        Map<PredefinedDateInterval, String> localizationMap = intervalFactory.getAllPredefinedIntervals().stream()
                .collect(LinkedHashMap::new, (map, item) -> map.put(item, item.getLocalizedName()), Map::putAll);

        ComponentUtils.setItemsMap(predefinedIntervalsSelect, localizationMap);
    }

    protected void initComponentVisibilityMap() {
        componentVisibilityMap.put(Type.LAST, lastIntervalField);
        componentVisibilityMap.put(Type.NEXT, nextIntervalField);
        componentVisibilityMap.put(Type.PREDEFINED, predefinedIntervalsSelect);
    }

    /**
     * @return date value or {@code null} if value is not set
     */
    @Nullable
    public BaseDateInterval getValue() {
        return value;
    }

    /**
     * Sets value that will be applied when {@link BeforeShowEvent} is fired.
     *
     * @param value date interval
     */
    public void setValue(@Nullable BaseDateInterval value) {
        this.value = value;
    }

    /**
     * @return meta property path of entity's property for Date Interval
     */
    @Nullable
    public MetaPropertyPath getMetaPropertyPath() {
        return mpp;
    }

    /**
     * Sets meta property path of entity's property for Date Interval.
     *
     * @param metaPropertyPath meta property path
     */
    public void setMetaPropertyPath(@Nullable MetaPropertyPath metaPropertyPath) {
        this.mpp = metaPropertyPath;
    }

    protected void filterOptionsByPropertyType() {
        if (isOptionsFilterable()) {
            Map<TimeUnit, String> localizedTimeUnitMap = getLocalizedEnumMap(List.of(TimeUnit.HOUR, TimeUnit.MINUTE));
            lastIntervalField.setTimeUnitItemsMap(localizedTimeUnitMap);
            nextIntervalField.setTimeUnitItemsMap(localizedTimeUnitMap);

            List<Type> availableTypes = Arrays.asList(Type.LAST, Type.NEXT);
            ComponentUtils.setItemsMap(typeRadioButtonGroup, getLocalizedEnumMap(availableTypes));
        }
    }

    protected boolean isOptionsFilterable() {
        // If property is Dynamic Attribute, meta property path can be null.
        // DynAttr contains only Date or DateTime attributes.
        if (mpp == null) {
            return false;
        }

        Range range = mpp.getRange();
        if (!range.isDatatype()) {
            throw new IllegalStateException("Value is not a simple type");
        }

        Class<?> javaClass = range.asDatatype().getJavaClass();
        List<Class<?>> timeClasses = List.of(Time.class, LocalTime.class, OffsetTime.class);
        return timeClasses.contains(javaClass);
    }

    protected void setupInitialValues() {
        if (isValueEmpty()) {
            return;
        }

        Type initialValue = value.getType();
        typeRadioButtonGroup.setValue(initialValue);

        if (Type.PREDEFINED == value.getType()
                && value instanceof PredefinedDateInterval predefinedDateIntervalValue) {
            predefinedIntervalsSelect.setValue(predefinedDateIntervalValue);

        } else if (value instanceof DateInterval dateIntervalValue) {

            if (Type.LAST == value.getType()) {
                lastIntervalField.setValue(dateIntervalValue);
            } else {
                nextIntervalField.setValue(dateIntervalValue);
            }
        }
    }

    protected boolean isValueEmpty() {
        if (value == null) {
            Type initialValueType = Objects.requireNonNull(typeRadioButtonGroup.getGenericDataView().getItems())
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No items for date interval initial value"));
            typeRadioButtonGroup.setValue(initialValueType);

            return true;
        }

        return false;
    }

    @Subscribe("typeRadioButtonGroup")
    protected void onTypeRadioButtonGroupValueChange(ComponentValueChangeEvent<JmixCheckboxGroup<Type>, Type> event) {
        if (event.getValue() != null) {
            componentVisibilityMap.values().forEach(component -> component.setVisible(false));
            componentVisibilityMap.get(event.getValue()).setVisible(true);
        }
    }

    @Subscribe("saveAndCloseBtn")
    protected void onSaveAndCloseBtnClick(ClickEvent<JmixButton> event) {
        Type type = typeRadioButtonGroup.getValue();

        ValidationErrors validationErrors = viewValidation.validateUiComponent(componentVisibilityMap.get(type));
        if (!validationErrors.isEmpty()) {
            viewValidation.showValidationErrors(validationErrors);
            viewValidation.focusProblemComponent(validationErrors);
            return;
        }

        updateValueByType(type);

        close(StandardOutcome.SAVE);
    }

    protected void updateValueByType(Type type) {
        if (Type.PREDEFINED == type) {
            value = predefinedIntervalsSelect.getValue();
        } else if (Type.LAST == type) {
            value = lastIntervalField.getValue();
        } else if (Type.NEXT == type) {
            value = nextIntervalField.getValue();
        }
    }

    @SuppressWarnings("rawtypes")
    protected <T extends Enum> Map<T, String> getLocalizedEnumMap(Class<T> enumClass) {
        return getLocalizedEnumMap(Arrays.asList(enumClass.getEnumConstants()));
    }

    @SuppressWarnings("rawtypes")
    protected <T extends Enum> Map<T, String> getLocalizedEnumMap(List<T> values) {
        return values.stream()
                .collect(LinkedHashMap::new, (map, item) -> map.put(item, messages.getMessage(item)), Map::putAll);
    }
}
