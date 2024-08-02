package io.jmix.fullcalendarflowui.component.data;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.fullcalendarflowui.kit.component.data.CalendarEventProvider;
import org.springframework.lang.Nullable;

public interface EntityCalendarEventProvider extends CalendarEventProvider {

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

    MetaClass getEntityMetaClass();

    @Nullable
    Class<?> getStartPropertyJavaType();

    @Nullable
    Class<?> getEndPropertyJavaType();
}
