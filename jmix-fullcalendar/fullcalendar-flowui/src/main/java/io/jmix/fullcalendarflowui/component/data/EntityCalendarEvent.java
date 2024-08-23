package io.jmix.fullcalendarflowui.component.data;

import com.google.common.base.Strings;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.fullcalendar.DaysOfWeek;
import io.jmix.fullcalendar.Display;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.jmix.fullcalendarflowui.kit.component.CalendarDateTimeTransformations.*;

public class EntityCalendarEvent<E> implements CalendarEvent {
    private static final Logger log = LoggerFactory.getLogger(EntityCalendarEvent.class);

    protected final E entity;
    protected final Object id;
    protected AbstractEntityEventProvider<?> eventProvider;

    public EntityCalendarEvent(E entity, AbstractEntityEventProvider<?> eventProvider) {
        Preconditions.checkNotNullArgument(entity);
        Preconditions.checkNotNullArgument(eventProvider);
        this.entity = entity;
        this.eventProvider = eventProvider;

        id = EntityValues.getId(entity);
        if (id == null) {
            throw new IllegalStateException(getClass().getSimpleName() + " contains an " + entity
                    + " entity with null ID");
        }
    }

    public E getEntity() {
        return entity;
    }

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public Object getGroupId() {
        return getValue(eventProvider.getGroupIdProperty());
    }

    @Override
    public Boolean getAllDay() {
        return getValue(eventProvider.getAllDayProperty());
    }

    @Override
    public void setAllDay(@Nullable Boolean allDay) {
        EntityValues.setValue(entity, eventProvider.getAllDayProperty(), allDay);
    }

    @Override
    public LocalDateTime getStartDateTime() {
        Object value = getValue(eventProvider.getStartDateTimeProperty());

        return value != null
                ? (LocalDateTime) transformToType(value, LocalDateTime.class, null)
                : null;
    }

    @Override
    public void setStartDateTime(@Nullable LocalDateTime start) {
        Class<?> propertyJavaType = eventProvider.getStartPropertyJavaType();
        if (propertyJavaType == null) {
            log.warn("Cannot set start date since no Java type specified");
            return;
        }
        Object transformed = transformToType(start, propertyJavaType, null);

        EntityValues.setValue(entity, eventProvider.getStartDateTimeProperty(), transformed);
    }

    @Override
    public LocalDateTime getEndDateTime() {
        Object value = getValue(eventProvider.getEndDateTimeProperty());

        return value != null
                ? (LocalDateTime) transformToType(value, LocalDateTime.class, null)
                : null;
    }

    @Override
    public void setEndDateTime(@Nullable LocalDateTime end) {
        Class<?> propertyJavaType = eventProvider.getEndPropertyJavaType();
        if (propertyJavaType == null) {
            log.warn("Cannot set end date since no Java type specified");
            return;
        }
        Object transformed = transformToType(end, propertyJavaType, null);

        EntityValues.setValue(entity, eventProvider.getEndDateTimeProperty(), transformed);
    }

    @Override
    public String getTitle() {
        return getValue(eventProvider.getTitleProperty());
    }

    @Override
    public String getDescription() {
        return getValue(eventProvider.getDescriptionProperty());
    }

    @Override
    public Boolean getInteractive() {
        return getValue(eventProvider.getInteractiveProperty());
    }

    @Override
    public String getClassNames() {
        return getValue(eventProvider.getClassNamesProperty());
    }

    @Override
    public Boolean getStartEditable() {
        return getValue(eventProvider.getStartEditableProperty());
    }

    @Nullable
    @Override
    public Boolean getDurationEditable() {
        return getValue(eventProvider.getDurationEditableProperty());
    }

    @Override
    public Display getDisplay() {
        return getValue(eventProvider.getDisplayProperty());
    }

    @Override
    public Boolean getOverlap() {
        return getValue(eventProvider.getOverlapProperty());
    }

    @Nullable
    @Override
    public Object getConstraint() {
        return getValue(eventProvider.getConstraintProperty());
    }

    @Override
    public String getBackgroundColor() {
        return getValue(eventProvider.getBackgroundColorProperty());
    }

    @Override
    public String getBorderColor() {
        return getValue(eventProvider.getBorderColorProperty());
    }

    @Override
    public String getTextColor() {
        return getValue(eventProvider.getTextColorProperty());
    }

    @Override
    public Map<String, Object> getAdditionalProperties() {
        List<String> additionalProperties = eventProvider.getAdditionalProperties();
        if (CollectionUtils.isEmpty(eventProvider.getAdditionalProperties())) {
            return Map.of();
        }

        @SuppressWarnings("DataFlowIssue")
        Map<String, Object> properties = additionalProperties.stream()
                .filter(p -> getValue(p) != null)
                .collect(Collectors.toMap(Function.identity(), this::getValue)); // getValue() cannot return null as we filter by non-null values

        return properties;
    }

    @Override
    public DaysOfWeek getRecurringDaysOfWeek() {
        return getValue(eventProvider.getRecurringDaysOfWeekProperty());
    }

    @Override
    public LocalDate getRecurringStarDate() {
        Object value = getValue(eventProvider.getRecurringStartDateProperty());

        return value != null
                ? (LocalDate) transformToType(value, LocalDate.class, null)
                : null;
    }

    @Override
    public LocalDate getRecurringEndDate() {
        Object value = getValue(eventProvider.getRecurringEndDateProperty());

        return value != null
                ? (LocalDate) transformToType(value, LocalDate.class, null)
                : null;
    }

    @Override
    public LocalTime getRecurringStarTime() {
        Object value = getValue(eventProvider.getRecurringStartTimeProperty());

        return value != null ? transformToLocalTime(value) : null;
    }

    @Override
    public LocalTime getRecurringEndTime() {
        Object value = getValue(eventProvider.getRecurringEndTimeProperty());

        return value != null ? transformToLocalTime(value) : null;
    }

    @Nullable
    protected <T> T getValue(@Nullable String property) {
        return !Strings.isNullOrEmpty(property)
                ? EntityValues.getValueEx(entity, property)
                : null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof EntityCalendarEvent<?> ce) {
            return id.equals(ce.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
