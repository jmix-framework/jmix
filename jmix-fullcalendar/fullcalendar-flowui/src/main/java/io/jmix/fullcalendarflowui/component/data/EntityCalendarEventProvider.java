package io.jmix.fullcalendarflowui.component.data;

import io.jmix.core.metamodel.model.MetaClass;
import org.springframework.lang.Nullable;

import java.util.List;

public interface EntityCalendarEventProvider extends BaseCalendarEventProvider {

    @Nullable
    String getGroupIdProperty();

    void setGroupIdProperty(@Nullable String groupIdProperty);

    @Nullable
    String getAllDayProperty();

    void setAllDayProperty(@Nullable String allDayProperty);

    @Nullable
    String getStartDateTimeProperty();

    void setStartDateTimeProperty(@Nullable String startProperty);

    @Nullable
    String getEndDateTimeProperty();

    void setEndDateTimeProperty(@Nullable String endProperty);

    @Nullable
    String getTitleProperty();

    void setTitleProperty(@Nullable String titleProperty);

    @Nullable
    String getDescriptionProperty();

    void setDescriptionProperty(@Nullable String descriptionProperty);

    @Nullable
    String getInteractiveProperty();

    void setInteractiveProperty(@Nullable String interactiveProperty);

    @Nullable
    String getClassNamesProperty();

    void setClassNamesProperty(@Nullable String classNamesProperty);

    @Nullable
    String getStartEditableProperty();

    void setStartEditableProperty(@Nullable String startEditableProperty);

    @Nullable
    String getDurationEditableProperty();

    void setDurationEditableProperty(@Nullable String durationEditableProperty);

    @Nullable
    String getDisplayProperty();

    void setDisplayProperty(@Nullable String displayProperty);

    @Nullable
    String getOverlapProperty();

    void setOverlapProperty(@Nullable String overlapProperty);

    @Nullable
    String getConstraintProperty();

    void setConstraintProperty(@Nullable String constraintProperty);

    @Nullable
    String getBackgroundColorProperty();

    void setBackgroundColorProperty(@Nullable String backgroundColorProperty);

    @Nullable
    String getBorderColorProperty();

    void setBorderColorProperty(@Nullable String borderColorProperty);

    @Nullable
    String getTextColorProperty();

    void setTextColorProperty(@Nullable String textColorProperty);

    List<String> getAdditionalProperties();

    void setAdditionalProperties(@Nullable List<String> additionalProperties);

    @Nullable
    String getRecurringDaysOfWeekProperty();

    void setRecurringDaysOfWeekProperty(@Nullable String recurringDaysOfWeekProperty);

    @Nullable
    String getRecurringStartDateProperty();

    void setRecurringStartDateProperty(@Nullable String recurringStartDateProperty);

    @Nullable
    String getRecurringEndDateProperty();

    void setRecurringEndDateProperty(@Nullable String recurringEndDateProperty);

    @Nullable
    String getRecurringStartTimeProperty();

    void setRecurringStartTimeProperty(@Nullable String recurringStartTimeProperty);

    @Nullable
    String getRecurringEndTimeProperty();

    void setRecurringEndTimeProperty(@Nullable String recurringEndTimeProperty);

    MetaClass getEntityMetaClass();

    @Nullable
    Class<?> getStartPropertyJavaType();

    @Nullable
    Class<?> getEndPropertyJavaType();
}
