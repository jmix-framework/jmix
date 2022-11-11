/*
 * Copyright 2019 Haulmont.
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

import com.vaadin.data.HasValue;
import com.vaadin.server.ErrorMessage;
import com.vaadin.server.UserError;
import com.vaadin.shared.ui.datefield.DateResolution;
import com.vaadin.ui.AbstractComponent;
import io.jmix.core.DateTimeTransformations;
import io.jmix.core.Messages;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.App;
import io.jmix.ui.AppUI;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.BindingState;
import io.jmix.ui.component.data.ConversionException;
import io.jmix.ui.component.data.DataAwareComponentsTools;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.component.validation.Validator;
import io.jmix.ui.sys.TestIdManager;
import io.jmix.ui.theme.ThemeConstants;
import io.jmix.ui.widget.JmixCssActionsLayout;
import io.jmix.ui.widget.JmixDateField;
import io.jmix.ui.widget.JmixTimeFieldWrapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.time.*;
import java.util.*;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;
import static io.jmix.ui.component.impl.WrapperUtils.fromVaadinTimeMode;
import static io.jmix.ui.component.impl.WrapperUtils.toVaadinTimeMode;

public class DateFieldImpl<V extends Comparable<V>>
        extends AbstractViewComponent<JmixCssActionsLayout, LocalDateTime, V>
        implements DateField<V>, InitializingBean {

    public static final String DATEFIELD_WITH_TIME_STYLENAME = "jmix-datefield-withtime";

    protected static final int VALIDATORS_LIST_INITIAL_CAPACITY = 2;

    protected DateTimeTransformations dateTimeTransformations;

    protected List<Validator<V>> validators; // lazily initialized list

    protected Resolution resolution;
    protected ZoneId zoneId;
    protected Datatype<V> datatype;
    protected V rangeStart;
    protected V rangeEnd;

    protected boolean updatingInstance;

    protected JmixDateField dateField;
    protected JmixTimeFieldWrapper timeField;

    protected String dateTimeFormat;

    protected boolean editable = true;
    protected boolean required = false;

    protected ThemeConstants theme;

    protected Subscription parentEditableChangeSubscription;
    protected Subscription valueSourceStateChangeSubscription;

    protected DataAwareComponentsTools dataAwareComponentsTools;

    public DateFieldImpl() {
        component = createComponent();
        component.setPrimaryStyleName("jmix-datefield-layout");

        if (App.isBound()) {
            theme = App.getInstance().getThemeConstants();
        }

        dateField = createDateField();
        initDateField(dateField);
        timeField = createTimeField();
        initTimeField(timeField);

        setWidthAuto();

        dateField.addValueChangeListener(this::componentValueChanged);
        timeField.addValueChangeListener(this::componentValueChanged);

        updateLayout();
    }

    protected JmixCssActionsLayout createComponent() {
        return new JmixCssActionsLayout();
    }

    protected JmixDateField createDateField() {
        return new JmixDateField();
    }

    protected void initDateField(JmixDateField dateField) {
        dateField.setCaptionManagedByLayout(false);
    }

    protected JmixTimeFieldWrapper createTimeField() {
        return new JmixTimeFieldWrapper();
    }

    protected void initTimeField(JmixTimeFieldWrapper timeField) {
        timeField.setCaptionManagedByLayout(false);
    }

    @Autowired
    public void setDataAwareComponentsTools(DataAwareComponentsTools dataAwareComponentsTools) {
        this.dataAwareComponentsTools = dataAwareComponentsTools;
    }

    @Autowired
    public void setDateTimeTransformations(DateTimeTransformations dateTimeTransformations) {
        this.dateTimeTransformations = dateTimeTransformations;
    }

    @Override
    public void afterPropertiesSet() {
        CurrentAuthentication currentAuthentication = applicationContext.getBean(CurrentAuthentication.class);
        Locale locale = currentAuthentication.getLocale();

        FormatStringsRegistry formatStringsRegistry = applicationContext.getBean(FormatStringsRegistry.class);

        dateField.setDateFormat(formatStringsRegistry.getFormatStrings(locale).getDateFormat());
        dateField.setResolution(DateResolution.DAY);

        timeField.setTimeFormat(formatStringsRegistry.getFormatStrings(locale).getTimeFormat());

        AppUI ui = AppUI.getCurrent();
        if (ui != null && ui.isTestMode()) {
            timeField.setJTestId("timepart");
            dateField.setJTestId("datepart");
        }
    }

    protected void componentValueChanged(HasValue.ValueChangeEvent<?> e) {
        if (e.isUserOriginated()) {
            V value;

            try {
                value = constructModelValue();

                if (!checkRange(value, true)) {
                    return;
                }

                LocalDateTime presentationValue = convertToPresentation(value);
                setValueToPresentation(presentationValue);
            } catch (ConversionException ce) {
                LoggerFactory.getLogger(DateFieldImpl.class)
                        .trace("Unable to convert presentation value to model", ce);

                setValidationError(ce.getLocalizedMessage());
                return;
            }

            V oldValue = internalValue;
            internalValue = value;

            if (!fieldValueEquals(value, oldValue)) {
                ValueChangeEvent<V> event = new ValueChangeEvent<>(this, oldValue, value, true);
                publish(ValueChangeEvent.class, event);
            }
        }
    }

    @Nullable
    @Override
    public Resolution getResolution() {
        return resolution;
    }

    @Override
    public void setResolution(Resolution resolution) {
        this.resolution = resolution;
        setResolutionInternal(resolution);
        updateLayout();
    }

    protected void setResolutionInternal(Resolution resolution) {
        dateField.setResolution(WrapperUtils.convertDateTimeResolution(resolution));

        if (resolution.ordinal() < Resolution.DAY.ordinal()) {
            timeField.setResolution(WrapperUtils.toVaadinTimeResolution(resolution));
        } else {
            // Set time field value to zero in case of resolution without time.
            // If we don't set value to zero then after changing resolution back to
            // resolution with time, we will get some value in time field
            timeField.setValue(null);
        }
    }

    @Nullable
    @Override
    public Datatype<V> getDatatype() {
        return datatype;
    }

    @Override
    public void setDatatype(Datatype<V> datatype) {
        dataAwareComponentsTools.checkValueSourceDatatypeMismatch(datatype, getValueSource());

        this.datatype = datatype;
    }

    @Override
    public void setRangeStart(@Nullable V value) {
        this.rangeStart = value;
        dateField.setRangeStart(value == null ? null : convertToLocalDateTime(value, zoneId).toLocalDate());
    }

    @Nullable
    @Override
    public V getRangeStart() {
        return rangeStart;
    }

    @Override
    public void setRangeEnd(@Nullable V value) {
        this.rangeEnd = value;
        dateField.setRangeEnd(value == null ? null : convertToLocalDateTime(value, zoneId).toLocalDate());
    }

    @Nullable
    @Override
    public V getRangeEnd() {
        return rangeEnd;
    }

    protected boolean checkRange(@Nullable V value, boolean handleError) {
        if (updatingInstance) {
            return true;
        }

        if (value != null) {
            V rangeStart = getRangeStart();
            if (rangeStart != null && rangeStart.compareTo(value) > 0) {
                if (handleError) {
                    handleDateOutOfRange(value);
                }
                return false;
            }

            V rangeEnd = getRangeEnd();
            if (rangeEnd != null && rangeEnd.compareTo(value) < 0) {
                if (handleError) {
                    handleDateOutOfRange(value);
                }
                return false;
            }
        }

        return true;
    }

    protected void handleDateOutOfRange(V value) {
        if (getFrame() != null) {
            Messages messages = applicationContext.getBean(Messages.class);
            Notifications notifications = ComponentsHelper.getScreenContext(this).getNotifications();

            notifications.create()
                    .withCaption(messages.getMessage("datePicker.dateOutOfRangeMessage"))
                    .withType(Notifications.NotificationType.TRAY)
                    .show();
        }

        setValueToPresentation(convertToLocalDateTime(value, zoneId));
    }

    @Nullable
    @Override
    public String getDateFormat() {
        return dateTimeFormat;
    }

    @Override
    public void setDateFormat(String dateFormat) {
        Preconditions.checkNotNullArgument(dateFormat);

        dateTimeFormat = dateFormat;

        StringBuilder date = new StringBuilder(dateFormat);
        StringBuilder time = new StringBuilder(dateFormat);
        int timeStartPos = findTimeStartPos(dateFormat);
        if (timeStartPos >= 0) {
            time.delete(0, timeStartPos);
            date.delete(timeStartPos, dateFormat.length());
            timeField.setTimeFormat(StringUtils.trimToEmpty(time.toString()));
        }
        dateField.setDateFormat(StringUtils.trimToEmpty(date.toString()));

        updateLayout();
    }

    @Nullable
    @Override
    public TimeZone getTimeZone() {
        return getZoneId() != null ? TimeZone.getTimeZone(getZoneId()) : null;
    }

    @Override
    public void setTimeZone(@Nullable TimeZone timeZone) {
        setZoneId(timeZone == null ? null : timeZone.toZoneId());
    }

    @Nullable
    @Override
    public ZoneId getZoneId() {
        return zoneId;
    }

    @Override
    public void setAutofill(boolean autofill) {
        dateField.setAutofill(autofill);
    }

    @Override
    public boolean isAutofill() {
        return dateField.isAutofill();
    }

    @Override
    public void setZoneId(@Nullable ZoneId zoneId) {
        ZoneId prevZoneId = this.zoneId;
        V value = getValue();
        this.zoneId = zoneId;
        dateField.setZoneId(zoneId);
        if (value != null && !Objects.equals(prevZoneId, zoneId)) {
            setValueToPresentation(convertToPresentation(value));
        }
    }

    protected void updateLayout() {
        component.removeAllComponents();
        component.addComponent(dateField);

        boolean timeFieldAllowedByResolution = resolution != null
                && resolution.ordinal() < Resolution.DAY.ordinal();
        boolean timeFieldAllowedByDateFormat = resolution == null
                && dateTimeFormat != null
                && findTimeStartPos(dateTimeFormat) >= 0;

        if ((resolution == null && dateTimeFormat == null)
                || timeFieldAllowedByResolution
                || timeFieldAllowedByDateFormat) {
            component.addComponent(timeField);
            component.addStyleName(DATEFIELD_WITH_TIME_STYLENAME);
        } else {
            component.removeStyleName("jmix-datefield-withtime");
        }
    }

    protected int findTimeStartPos(String dateTimeFormat) {
        List<Integer> positions = new ArrayList<>();

        char[] signs = new char[]{'H', 'h', 'm', 's'};
        for (char sign : signs) {
            int pos = dateTimeFormat.indexOf(sign);
            if (pos > -1) {
                positions.add(pos);
            }
        }
        return positions.isEmpty() ? -1 : Collections.min(positions);
    }

    @Override
    protected void setValueToPresentation(@Nullable LocalDateTime value) {
        updatingInstance = true;
        try {
            if (value == null) {
                dateField.setValue(null);
                timeField.setValue(null);
            } else {
                dateField.setValue(value.toLocalDate());
                timeField.setValue(value.toLocalTime());
            }
        } finally {
            updatingInstance = false;
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected V constructModelValue() {
        LocalDate dateValue = dateField.getValue();
        if (dateValue == null) {
            return null;
        }

        LocalTime timeValue = timeField.getValue() != null
                ? timeField.getValue()
                : LocalTime.MIDNIGHT;

        LocalDateTime localDateTime = LocalDateTime.of(dateValue, timeValue);

        ValueSource<V> valueSource = getValueSource();
        if (valueSource instanceof EntityValueSource) {
            MetaProperty metaProperty = ((EntityValueSource) valueSource).getMetaPropertyPath().getMetaProperty();
            return (V) convertFromLocalDateTime(localDateTime, zoneId,
                    metaProperty.getRange().asDatatype().getJavaClass());
        }

        return (V) convertFromLocalDateTime(localDateTime, zoneId,
                datatype == null ? Date.class : datatype.getJavaClass());
    }

    @Nullable
    @Override
    protected LocalDateTime convertToPresentation(@Nullable V modelValue) throws ConversionException {
        if (modelValue == null) {
            return null;
        }
        return convertToLocalDateTime(modelValue, zoneId);
    }

    protected LocalDateTime convertToLocalDateTime(Object date, @Nullable ZoneId zoneId) {
        Preconditions.checkNotNullArgument(date);
        ZonedDateTime zonedDateTime = dateTimeTransformations.transformToZDT(date);
        if (dateTimeTransformations.isDateTypeSupportsTimeZones(date.getClass())) {
            zonedDateTime = zonedDateTime.withZoneSameInstant(zoneId != null ? zoneId : ZoneId.systemDefault());
        }
        return zonedDateTime.toLocalDateTime();
    }

    protected Object convertFromLocalDateTime(LocalDateTime localDateTime, @Nullable ZoneId fromZoneId, Class javaType) {
        if (fromZoneId == null || !dateTimeTransformations.isDateTypeSupportsTimeZones(javaType)) {
            fromZoneId = ZoneId.systemDefault();
        }
        ZonedDateTime zonedDateTime = localDateTime.atZone(fromZoneId);
        return dateTimeTransformations.transformFromZDT(zonedDateTime, javaType);
    }

    @Override
    public void setDescription(@Nullable String description) {
        super.setDescription(description);
        dateField.setDescription(description);
        timeField.setDescription(description);
    }

    @Override
    public void commit() {
        if (valueBinding != null) {
            valueBinding.write();
        }
    }

    @Override
    public void discard() {
        if (valueBinding != null) {
            valueBinding.discard();
        }
    }

    @Override
    public boolean isBuffered() {
        return valueBinding != null
                && valueBinding.isBuffered();
    }

    @Override
    public void setBuffered(boolean buffered) {
        if (valueBinding != null) {
            valueBinding.setBuffered(buffered);
        }
    }

    @Override
    public boolean isModified() {
        return valueBinding != null
                && valueBinding.isModified();
    }

    @Override
    public void setDebugId(@Nullable String id) {
        super.setDebugId(id);

        if (id != null && AppUI.getCurrent() != null) {
            TestIdManager testIdManager = AppUI.getCurrent().getTestIdManager();
            timeField.setId(testIdManager.getTestId(id + "_time"));
            dateField.setId(testIdManager.getTestId(id + "_date"));
        }
    }

    @Override
    protected void valueBindingConnected(ValueSource<V> valueSource) {
        super.valueBindingConnected(valueSource);

        if (valueSource instanceof EntityValueSource) {
            EntityValueSource entityValueSource = (EntityValueSource) valueSource;
            DataAwareComponentsTools dataAwareComponentsTools = applicationContext.getBean(DataAwareComponentsTools.class);
            dataAwareComponentsTools.setupDateFormat(this, entityValueSource);
            dataAwareComponentsTools.setupZoneId(this, entityValueSource);

            if (valueSourceStateChangeSubscription != null) {
                valueSourceStateChangeSubscription.remove();
            }

            // setup dateRange after valueSource is activated and value is set because
            // Vaadin dateField rejects value if it is not in range
            valueSourceStateChangeSubscription = valueSource.addStateChangeListener(event -> {
                if (event.getState() == BindingState.ACTIVE) {
                    dataAwareComponentsTools.setupDateRange(this, entityValueSource);
                }
            });
        }
    }

    @Override
    public void setParent(@Nullable Component parent) {
        if (this.parent instanceof EditableChangeNotifier
                && parentEditableChangeSubscription != null) {
            parentEditableChangeSubscription.remove();
            parentEditableChangeSubscription = null;
        }

        super.setParent(parent);

        if (parent instanceof EditableChangeNotifier) {
            parentEditableChangeSubscription =
                    ((EditableChangeNotifier) parent).addEditableChangeListener(this::onParentEditableChange);

            Editable parentEditable = (Editable) parent;
            if (!parentEditable.isEditable()) {
                setEditableToComponent(false);
            }
        }
    }

    protected void onParentEditableChange(EditableChangeNotifier.EditableChangeEvent event) {
        boolean parentEditable = event.getSource().isEditable();
        boolean finalEditable = parentEditable && editable;
        setEditableToComponent(finalEditable);
    }

    @Override
    public boolean isEditable() {
        return editable;
    }

    @Override
    public void setEditable(boolean editable) {
        if (this.editable == editable) {
            return;
        }

        this.editable = editable;

        boolean parentEditable = true;
        if (parent instanceof ChildEditableController) {
            parentEditable = ((ChildEditableController) parent).isEditable();
        }
        boolean finalEditable = parentEditable && editable;

        setEditableToComponent(finalEditable);
    }

    protected void setEditableToComponent(boolean editable) {
        timeField.setReadOnly(!editable);
        dateField.setReadOnly(!editable);

        updateRequiredIndicator();
    }

    @Override
    public void focus() {
        dateField.focus();
    }

    @Override
    public int getTabIndex() {
        return dateField.getTabIndex();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        dateField.setTabIndex(tabIndex);
        timeField.setTabIndex(tabIndex);
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Override
    public void setRequired(boolean required) {
        if (this.required == required) {
            return;
        }

        this.required = required;

        setupComponentErrorProvider(required, component);
        setupComponentErrorProvider(required, dateField);
        setupComponentErrorProvider(required, timeField);

        updateRequiredIndicator();
    }

    private void updateRequiredIndicator() {
        boolean isRequiredIndicatorVisible = isRequired() && isEditable();
        // Set requiredIndicatorVisible to a component
        // in order to show required indicator
        component.setRequiredIndicatorVisible(isRequiredIndicatorVisible);
    }

    protected void setupComponentErrorProvider(boolean required, AbstractComponent component) {
        if (required) {
            component.setComponentErrorProvider(this::getErrorMessage);
        } else {
            component.setComponentErrorProvider(null);
        }
    }

    @Nullable
    protected ErrorMessage getErrorMessage() {
        return (isEditableWithParent() && isRequired() && isEmpty())
                ? new UserError(getRequiredMessage())
                : null;
    }

    @Nullable
    @Override
    public String getRequiredMessage() {
        return dateField.getRequiredError();
    }

    @Override
    public void setRequiredMessage(@Nullable String msg) {
        dateField.setRequiredError(msg);
        timeField.setRequiredError(msg);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addValidator(Validator<? super V> validator) {
        if (validators == null) {
            validators = new ArrayList<>(VALIDATORS_LIST_INITIAL_CAPACITY);
        }
        if (!validators.contains(validator)) {
            validators.add((Validator<V>) validator);
        }
    }

    @Override
    public void removeValidator(Validator<V> validator) {
        if (validators != null) {
            validators.remove(validator);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Validator<V>> getValidators() {
        if (validators == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableCollection(validators);
    }

    @Override
    public boolean isValid() {
        try {
            validate();
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }

    @Override
    public void validate() throws ValidationException {
        if (hasValidationError()) {
            setValidationError(null);
        }

        if (!isVisibleRecursive() || !isEditableWithParent() || !isEnabledRecursive()) {
            return;
        }

        Messages messages = applicationContext.getBean(Messages.class);

        V value = constructModelValue();
        if (!checkRange(value, false)) {
            LoggerFactory.getLogger(DateFieldImpl.class)
                    .trace("DateField value is out of range");
            String dateOutOfRangeMessage = messages.getMessage("datePicker.dateOutOfRangeMessage");
            setValidationError(dateOutOfRangeMessage);
            throw new ValidationException(dateOutOfRangeMessage);
        }

        if (isEmpty() && isRequired()) {
            String requiredMessage = getRequiredMessage();
            if (requiredMessage == null) {
                requiredMessage = messages.getMessage("validationFail.defaultRequiredMessage");
            }
            throw new RequiredValueMissingException(requiredMessage, this);
        }

        value = getValue();
        triggerValidators(value);
    }

    protected void triggerValidators(V value) throws ValidationFailedException {
        if (validators != null) {
            try {
                for (Validator<V> validator : validators) {
                    validator.accept(value);
                }
            } catch (ValidationException e) {
                setValidationError(e.getDetailsMessage());

                throw new ValidationFailedException(e.getDetailsMessage(), this, e);
            }
        }
    }

    @Override
    protected boolean hasValidationError() {
        return dateField.getComponentError() instanceof UserError;
    }

    @Override
    protected void setValidationError(@Nullable String errorMessage) {
        if (errorMessage == null) {
            dateField.setComponentError(null);
            timeField.setComponentError(null);
        } else {
            UserError userError = new UserError(errorMessage);
            dateField.setComponentError(userError);
            timeField.setComponentError(userError);
        }
    }

    @Override
    public void setTimeMode(TimeField.TimeMode timeMode) {
        checkNotNullArgument("Time mode cannot be null");

        timeField.setTimeMode(toVaadinTimeMode(timeMode));
    }

    @Nullable
    @Override
    public TimeField.TimeMode getTimeMode() {
        return fromVaadinTimeMode(timeField.getTimeMode());
    }
}
