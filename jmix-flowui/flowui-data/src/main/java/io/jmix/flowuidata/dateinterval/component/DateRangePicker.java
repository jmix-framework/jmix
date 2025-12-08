/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowuidata.dateinterval.component;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.jmix.core.DateTimeTransformations;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.SupportsStatusChangeHandler.StatusContext;
import io.jmix.flowui.component.SupportsTypedValue.TypedValueChangeEvent;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowuidata.dateinterval.model.CustomDateInterval;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;

import java.time.LocalDate;

/**
 * Picker component for selecting an arbitrary date range. Uses {@link CustomDateInterval} as a model.
 * {@link LocalDate} is used as a presentation value.
 */
public class DateRangePicker extends CustomField<CustomDateInterval>
        implements SupportsValidation<CustomDateInterval>, ApplicationContextAware, InitializingBean {

    protected HorizontalLayout root;

    protected TypedDatePicker<LocalDate> startDatePicker;
    protected TypedDatePicker<LocalDate> endDatePicker;

    protected UiComponents uiComponents;
    protected Messages messages;
    protected DatatypeRegistry datatypeRegistry;
    protected DateTimeTransformations transformations;

    protected ApplicationContext applicationContext;

    protected MetaPropertyPath metaPropertyPath;
    protected Datatype<?> datatype;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        autowireDependencies();
        initComponent();
        updateInvalidState();
    }

    protected void autowireDependencies() {
        uiComponents = applicationContext.getBean(UiComponents.class);
        messages = applicationContext.getBean(Messages.class);
        datatypeRegistry = applicationContext.getBean(DatatypeRegistry.class);
        transformations = applicationContext.getBean(DateTimeTransformations.class);
    }

    protected void initComponent() {
        initRoot();
        initDatePickers();
    }

    protected void initRoot() {
        root = uiComponents.create(HorizontalLayout.class);
        root.setPadding(false);
        root.setSpacing(false);

        root.addClassName(LumoUtility.Gap.Column.XSMALL);
        root.setWrap(true);
        root.setAlignItems(FlexComponent.Alignment.BASELINE);

        add(root);
    }

    @SuppressWarnings("unchecked")
    protected void initDatePickers() {
        startDatePicker = uiComponents.create(TypedDatePicker.class);
        startDatePicker.setPlaceholder(messages.getMessage(getClass(), "DateRangePicker.startDatePicker.placeholder"));
        initDatePicker(startDatePicker);

        endDatePicker = uiComponents.create(TypedDatePicker.class);
        endDatePicker.setPlaceholder(messages.getMessage(getClass(), "DateRangePicker.endDatePicker.placeholder"));
        initDatePicker(endDatePicker);

        root.add(startDatePicker, endDatePicker);
        root.setFlexGrow(1, startDatePicker, endDatePicker);
    }

    @SuppressWarnings("DataFlowIssue")
    protected void onStartDateValueChange(TypedValueChangeEvent<TypedDatePicker<LocalDate>, LocalDate> event) {
        if (startDatePicker.equals(event.getSource())) {
            endDatePicker.setMin(event.getValue() != null ? event.getValue().plusDays(1) : null);
        } else if (endDatePicker.equals(event.getSource())) {
            startDatePicker.setMax(event.getValue() != null ? event.getValue().minusDays(1) : null);
        }
    }

    protected void initDatePicker(TypedDatePicker<LocalDate> datePicker) {
        datePicker.setDatatype(datatypeRegistry.get(LocalDate.class));

        datePicker.addTypedValueChangeListener(this::onStartDateValueChange);
        datePicker.setStatusChangeHandler(this::onValidationStatusChange);
        datePicker.addValidationStatusChangeListener(__ -> updateInvalidState());
        datePicker.addClientValidatedEventListener(__ -> updateInvalidState());

        datePicker.setRequiredMessage(messages.getMessage(getClass(), "DateRangePicker.requiredMessage"));
        datePicker.setRequired(true);
    }

    @Override
    protected CustomDateInterval generateModelValue() {
        return new CustomDateInterval(
                metaPropertyPath.getFirstPropertyName(),
                datatype.getId(),
                convertValueToModel(startDatePicker.getTypedValue()),
                convertValueToModel(endDatePicker.getTypedValue())
        );
    }

    @Override
    protected void setPresentationValue(CustomDateInterval newPresentationValue) {
        LocalDate start = convertValueToPresentation(newPresentationValue.getStart());
        startDatePicker.setTypedValue(start);

        LocalDate endDate = convertValueToPresentation(newPresentationValue.getEnd());
        endDatePicker.setTypedValue(endDate);

        updateInvalidState();
    }

    @Override
    public Registration addValidator(Validator<? super CustomDateInterval> validator) {
        throw new UnsupportedOperationException("%s has a predefined validators".formatted(getClass().getSimpleName()));
    }

    protected void onValidationStatusChange(StatusContext<TypedDatePicker<LocalDate>> context) {
        setErrorMessage(context.getDescription());
    }

    protected void updateInvalidState() {
        setInvalid(startDatePicker.isInvalid() || endDatePicker.isInvalid());
    }

    @Override
    public void executeValidators() throws ValidationException {
        startDatePicker.executeValidators();
        endDatePicker.executeValidators();
    }

    @Override
    public void setInvalid(boolean invalid) {
        super.setInvalid(invalid);
    }

    /**
     * Sets the {@link MetaPropertyPath} for the date range picker component. Updates the datatype.
     *
     * @param metaPropertyPath {@link MetaPropertyPath} to set
     */
    public void setMetaPropertyPath(MetaPropertyPath metaPropertyPath) {
        this.metaPropertyPath = metaPropertyPath;
        this.datatype = metaPropertyPath.getRange().asDatatype();
    }

    @Nullable
    protected Object convertValueToModel(@Nullable LocalDate value) {
        return value != null
                ? transformations.transformToType(value, datatype.getJavaClass(), null)
                : null;
    }

    protected LocalDate convertValueToPresentation(Object value) {
        return (LocalDate) transformations.transformToType(value, LocalDate.class, null);
    }
}
