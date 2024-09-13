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

import static io.jmix.fullcalendarflowui.kit.component.CalendarDateTimeUtils.*;

/**
 * Calendar event that wraps an entity.
 *
 * @param <E> entity type
 * @see ContainerCalendarDataProvider
 * @see EntityCalendarDataRetriever
 */
public class EntityCalendarEvent<E> implements CalendarEvent {

    private static final Logger log = LoggerFactory.getLogger(EntityCalendarEvent.class);

    protected final E entity;
    protected final Object id;
    protected AbstractEntityCalendarDataProvider<?> dataProvider;

    public EntityCalendarEvent(E entity, AbstractEntityCalendarDataProvider<?> dataProvider) {
        Preconditions.checkNotNullArgument(entity);
        Preconditions.checkNotNullArgument(dataProvider);
        this.entity = entity;
        this.dataProvider = dataProvider;

        id = EntityValues.getId(entity);
        if (id == null) {
            throw new IllegalStateException(getClass().getSimpleName() + " contains an " + entity
                    + " entity with null ID");
        }
    }

    /**
     * @return a wrapped entity
     */
    public E getEntity() {
        return entity;
    }

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public Object getGroupId() {
        return getValue(dataProvider.getGroupIdProperty());
    }

    @Override
    public Boolean getAllDay() {
        return getValue(dataProvider.getAllDayProperty());
    }

    @Override
    public void setAllDay(@Nullable Boolean allDay) {
        EntityValues.setValue(entity, dataProvider.getAllDayProperty(), allDay);
    }

    @Override
    public LocalDateTime getStartDateTime() {
        Object value = getValue(dataProvider.getStartDateTimeProperty());

        return value != null
                ? (LocalDateTime) transformToType(value, LocalDateTime.class, null)
                : null;
    }

    @Override
    public void setStartDateTime(@Nullable LocalDateTime start) {
        Class<?> propertyJavaType = dataProvider.getStartPropertyJavaType();
        if (propertyJavaType == null) {
            log.warn("Cannot set start date since no Java type specified");
            return;
        }
        Object transformed = transformToType(start, propertyJavaType, null);

        EntityValues.setValue(entity, dataProvider.getStartDateTimeProperty(), transformed);
    }

    @Override
    public LocalDateTime getEndDateTime() {
        Object value = getValue(dataProvider.getEndDateTimeProperty());

        return value != null
                ? (LocalDateTime) transformToType(value, LocalDateTime.class, null)
                : null;
    }

    @Override
    public void setEndDateTime(@Nullable LocalDateTime end) {
        Class<?> propertyJavaType = dataProvider.getEndPropertyJavaType();
        if (propertyJavaType == null) {
            log.warn("Cannot set end date since no Java type specified");
            return;
        }
        Object transformed = transformToType(end, propertyJavaType, null);

        EntityValues.setValue(entity, dataProvider.getEndDateTimeProperty(), transformed);
    }

    @Override
    public String getTitle() {
        return getValue(dataProvider.getTitleProperty());
    }

    @Override
    public String getDescription() {
        return getValue(dataProvider.getDescriptionProperty());
    }

    @Override
    public Boolean getInteractive() {
        return getValue(dataProvider.getInteractiveProperty());
    }

    @Override
    public String getClassNames() {
        return getValue(dataProvider.getClassNamesProperty());
    }

    @Override
    public Boolean getStartEditable() {
        return getValue(dataProvider.getStartEditableProperty());
    }

    @Nullable
    @Override
    public Boolean getDurationEditable() {
        return getValue(dataProvider.getDurationEditableProperty());
    }

    @Override
    public Display getDisplay() {
        return getValue(dataProvider.getDisplayProperty());
    }

    @Override
    public Boolean getOverlap() {
        return getValue(dataProvider.getOverlapProperty());
    }

    @Nullable
    @Override
    public Object getConstraint() {
        return getValue(dataProvider.getConstraintProperty());
    }

    @Override
    public String getBackgroundColor() {
        return getValue(dataProvider.getBackgroundColorProperty());
    }

    @Override
    public String getBorderColor() {
        return getValue(dataProvider.getBorderColorProperty());
    }

    @Override
    public String getTextColor() {
        return getValue(dataProvider.getTextColorProperty());
    }

    @Override
    public Map<String, Object> getAdditionalProperties() {
        List<String> additionalProperties = dataProvider.getAdditionalProperties();
        if (CollectionUtils.isEmpty(dataProvider.getAdditionalProperties())) {
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
        return getValue(dataProvider.getRecurringDaysOfWeekProperty());
    }

    @Override
    public LocalDate getRecurringStartDate() {
        Object value = getValue(dataProvider.getRecurringStartDateProperty());

        return value != null
                ? (LocalDate) transformToType(value, LocalDate.class, null)
                : null;
    }

    @Override
    public LocalDate getRecurringEndDate() {
        Object value = getValue(dataProvider.getRecurringEndDateProperty());

        return value != null
                ? (LocalDate) transformToType(value, LocalDate.class, null)
                : null;
    }

    @Override
    public LocalTime getRecurringStartTime() {
        Object value = getValue(dataProvider.getRecurringStartTimeProperty());

        return value != null ? transformToLocalTime(value) : null;
    }

    @Override
    public LocalTime getRecurringEndTime() {
        Object value = getValue(dataProvider.getRecurringEndTimeProperty());

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
