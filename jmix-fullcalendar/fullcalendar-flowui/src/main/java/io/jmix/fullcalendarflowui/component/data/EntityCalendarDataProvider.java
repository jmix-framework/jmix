package io.jmix.fullcalendarflowui.component.data;

import io.jmix.core.metamodel.model.MetaClass;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * Base interface for data providers that works with entities. It contains entity properties
 * names to get according values for {@link CalendarEvent}.
 */
public interface EntityCalendarDataProvider extends CalendarDataProvider {

    /**
     * @return a group ID property or {@code null} if not set
     */
    @Nullable
    String getGroupIdProperty();

    /**
     * Sets a group ID property.
     *
     * @param groupIdProperty group ID property of entity
     * @see CalendarEvent#getGroupId()
     */
    void setGroupIdProperty(@Nullable String groupIdProperty);

    /**
     * @return an all-day property or {@code null} if not set
     */
    @Nullable
    String getAllDayProperty();

    /**
     * Sets an all-day property.
     *
     * @param allDayProperty all-day property of entity
     * @see CalendarEvent#getAllDay()
     */
    void setAllDayProperty(@Nullable String allDayProperty);

    /**
     * @return a start date-time property or {@code null} if not set
     */
    @Nullable
    String getStartDateTimeProperty();

    /**
     * Sets a start date-time property.
     *
     * @param startProperty start date-time property of entity
     * @see CalendarEvent#getStartDateTime()
     */
    void setStartDateTimeProperty(@Nullable String startProperty);

    /**
     * @return an end date-time property or {@code null} if not set
     */
    @Nullable
    String getEndDateTimeProperty();

    /**
     * Sets an end date-time property.
     *
     * @param endProperty end date-time property of entity
     * @see CalendarEvent#getEndDateTime()
     */
    void setEndDateTimeProperty(@Nullable String endProperty);

    /**
     * @return a title property or {@code null} if not set
     */
    @Nullable
    String getTitleProperty();

    /**
     * Sets a title property.
     *
     * @param titleProperty title property of entity
     * @see CalendarEvent#getTitle()
     */
    void setTitleProperty(@Nullable String titleProperty);

    /**
     * @return a description property or {@code null} if not set
     */
    @Nullable
    String getDescriptionProperty();

    /**
     * Sets a description property.
     *
     * @param descriptionProperty description property of entity
     * @see CalendarEvent#getDescription()
     */
    void setDescriptionProperty(@Nullable String descriptionProperty);

    /**
     * @return an interactive property or {@code null} if not set
     */
    @Nullable
    String getInteractiveProperty();

    /**
     * Sets an interactive property.
     *
     * @param interactiveProperty interactive property of entity
     * @see CalendarEvent#getInteractive()
     */
    void setInteractiveProperty(@Nullable String interactiveProperty);

    /**
     * @return a class names property or {@code null} if not set
     */
    @Nullable
    String getClassNamesProperty();

    /**
     * Sets a class names property.
     *
     * @param classNamesProperty class names property of entity
     * @see CalendarEvent#getClassNames()
     */
    void setClassNamesProperty(@Nullable String classNamesProperty);

    /**
     * @return a start editable property or {@code null} if not set
     */
    @Nullable
    String getStartEditableProperty();

    /**
     * Sets a start editable property.
     *
     * @param startEditableProperty start editable property of entity
     * @see CalendarEvent#getStartEditable()
     */
    void setStartEditableProperty(@Nullable String startEditableProperty);

    /**
     * @return a duration editable property or {@code null} if not set
     */
    @Nullable
    String getDurationEditableProperty();

    /**
     * Sets a duration editable property.
     *
     * @param durationEditableProperty duration editable property of entity
     * @see CalendarEvent#getDurationEditable()
     */
    void setDurationEditableProperty(@Nullable String durationEditableProperty);

    /**
     * @return a display property or {@code null} if not set
     */
    @Nullable
    String getDisplayProperty();

    /**
     * Sets a display property.
     *
     * @param displayProperty display property of entity
     * @see CalendarEvent#getDisplay()
     */
    void setDisplayProperty(@Nullable String displayProperty);

    /**
     * @return an overlap property or {@code null} if not set
     */
    @Nullable
    String getOverlapProperty();

    /**
     * Sets an overlap property.
     *
     * @param overlapProperty overlap property of entity
     * @see CalendarEvent#getOverlap()
     */
    void setOverlapProperty(@Nullable String overlapProperty);

    /**
     * @return a constraint property or {@code null} if not set
     */
    @Nullable
    String getConstraintProperty();

    /**
     * Sets a constraint property.
     *
     * @param constraintProperty constraint property of entity
     * @see CalendarEvent#getConstraint()
     */
    void setConstraintProperty(@Nullable String constraintProperty);

    /**
     * @return a background color property or {@code null} if not set
     */
    @Nullable
    String getBackgroundColorProperty();

    /**
     * Sets a background color property.
     *
     * @param backgroundColorProperty background color property of entity
     * @see CalendarEvent#getBackgroundColor()
     */
    void setBackgroundColorProperty(@Nullable String backgroundColorProperty);

    /**
     * @return a border color property or {@code null} if not set
     */
    @Nullable
    String getBorderColorProperty();

    /**
     * Sets a border color property.
     *
     * @param borderColorProperty border color property of entity
     * @see CalendarEvent#getBorderColor()
     */
    void setBorderColorProperty(@Nullable String borderColorProperty);

    /**
     * @return a text color property or {@code null} if not set
     */
    @Nullable
    String getTextColorProperty();

    /**
     * Sets a text color property.
     *
     * @param textColorProperty text color property of entity
     * @see CalendarEvent#getTextColor()
     */
    void setTextColorProperty(@Nullable String textColorProperty);

    /**
     * @return list of additional properties of entity
     */
    List<String> getAdditionalProperties();

    /**
     * Sets a list of additional properties.
     *
     * @param additionalProperties a list of additional properties of entity
     * @see CalendarEvent#getAdditionalProperties()
     */
    void setAdditionalProperties(@Nullable List<String> additionalProperties);

    /**
     * @return a recurring days of week property or {@code null} if not set
     */
    @Nullable
    String getRecurringDaysOfWeekProperty();

    /**
     * Sets a recurring days of week property.
     *
     * @param recurringDaysOfWeekProperty a recurring days of week property of entity
     * @see CalendarEvent#getRecurringDaysOfWeek()
     */
    void setRecurringDaysOfWeekProperty(@Nullable String recurringDaysOfWeekProperty);

    /**
     * @return a recurring start date property or {@code null} if not set
     */
    @Nullable
    String getRecurringStartDateProperty();

    /**
     * Sets a recurring start date property.
     *
     * @param recurringStartDateProperty a recurring start date property of entity
     * @see CalendarEvent#getRecurringStartDate()
     */
    void setRecurringStartDateProperty(@Nullable String recurringStartDateProperty);

    /**
     * @return a recurring end date property or {@code null} if not set
     */
    @Nullable
    String getRecurringEndDateProperty();

    /**
     * Sets a recurring end date property.
     *
     * @param recurringEndDateProperty a recurring end date property of entity
     * @see CalendarEvent#getRecurringEndDate()
     */
    void setRecurringEndDateProperty(@Nullable String recurringEndDateProperty);

    /**
     * @return a recurring start time property or {@code null} if not set
     */
    @Nullable
    String getRecurringStartTimeProperty();

    /**
     * Sets a recurring start time property.
     *
     * @param recurringStartTimeProperty a recurring start time property of entity
     * @see CalendarEvent#getRecurringStartTime()
     */
    void setRecurringStartTimeProperty(@Nullable String recurringStartTimeProperty);

    /**
     * @return a recurring end time property or {@code null} if not set
     */
    @Nullable
    String getRecurringEndTimeProperty();

    /**
     * Sets a recurring end time property.
     *
     * @param recurringEndTimeProperty a recurring end time property of entity
     * @see CalendarEvent#getRecurringEndTime()
     */
    void setRecurringEndTimeProperty(@Nullable String recurringEndTimeProperty);

    /**
     * @return entity meta class
     */
    MetaClass getEntityMetaClass();

    /**
     * @return java type of start date-time property or {@code null} if property is not specified
     */
    @Nullable
    Class<?> getStartPropertyJavaType();

    /**
     * @return java type of end date-time property or {@code null} if property is not specified
     */
    @Nullable
    Class<?> getEndPropertyJavaType();
}
