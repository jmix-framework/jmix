package io.jmix.fullcalendarflowui.component.data;

import com.vaadin.flow.data.provider.AbstractDataProvider;
import io.jmix.core.common.util.Preconditions;
import io.jmix.fullcalendarflowui.kit.component.data.CalendarEvent;
import io.jmix.fullcalendarflowui.kit.component.data.EventProviderUtils;
import org.springframework.lang.Nullable;

import java.util.Objects;

/**
 *
 * @param <F> type of filter object
 */
public abstract class AbstractEntityCalendarItems<F> extends AbstractDataProvider<CalendarEvent, F>
        implements EntityCalendarEventProvider {

    protected String id;

    protected String groupIdProperty;
    protected String allDayProperty;
    protected String startDateTimeProperty;
    protected String endDateTimeProperty;
    protected String titleProperty;
    protected String descriptionProperty;
    protected String classNamesProperty;
    protected String startEditableProperty;
    protected String durationEditableProperty;
    protected String displayProperty;
    protected String overlapProperty;
    protected String constraintProperty;
    protected String backgroundColorProperty;
    protected String borderColorProperty;
    protected String textColorProperty;

    public AbstractEntityCalendarItems() {
        this(EventProviderUtils.generateId());
    }

    public AbstractEntityCalendarItems(String id) {
        Preconditions.checkNotEmptyString(id);
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getGroupIdProperty() {
        return groupIdProperty;
    }

    @Override
    public void setGroupIdProperty(@Nullable String groupIdProperty) {
        this.groupIdProperty = groupIdProperty;
    }

    @Override
    public String getAllDayProperty() {
        return allDayProperty;
    }

    @Override
    public void setAllDayProperty(@Nullable String allDayProperty) {
        this.allDayProperty = allDayProperty;
    }

    @Override
    public String getStartDateTimeProperty() {
        return startDateTimeProperty;
    }

    @Override
    public void setStartDateTimeProperty(@Nullable String startDateTimeProperty) {
        this.startDateTimeProperty = startDateTimeProperty;
    }

    @Override
    public String getEndDateTimeProperty() {
        return endDateTimeProperty;
    }

    @Override
    public void setEndDateTimeProperty(@Nullable String endDateTimeProperty) {
        this.endDateTimeProperty = endDateTimeProperty;
    }

    @Override
    public String getTitleProperty() {
        return titleProperty;
    }

    @Override
    public void setTitleProperty(@Nullable String titleProperty) {
        this.titleProperty = titleProperty;
    }

    @Override
    public String getDescriptionProperty() {
        return descriptionProperty;
    }

    @Override
    public void setDescriptionProperty(@Nullable String descriptionProperty) {
        this.descriptionProperty = descriptionProperty;
    }

    @Override
    public String getClassNamesProperty() {
        return classNamesProperty;
    }

    @Override
    public void setClassNamesProperty(@Nullable String classNamesProperty) {
        this.classNamesProperty = classNamesProperty;
    }

    @Override
    public String getStartEditableProperty() {
        return startEditableProperty;
    }

    @Override
    public void setStartEditableProperty(@Nullable String startEditableProperty) {
        this.startEditableProperty = startEditableProperty;
    }

    @Override
    public String getDurationEditableProperty() {
        return durationEditableProperty;
    }

    @Override
    public void setDurationEditableProperty(@Nullable String durationEditableProperty) {
        this.durationEditableProperty = durationEditableProperty;
    }

    @Override
    public String getDisplayProperty() {
        return displayProperty;
    }

    @Override
    public void setDisplayProperty(@Nullable String displayProperty) {
        this.displayProperty = displayProperty;
    }

    @Override
    public String getOverlapProperty() {
        return overlapProperty;
    }

    @Override
    public void setOverlapProperty(@Nullable String overlapProperty) {
        this.overlapProperty = overlapProperty;
    }

    @Override
    public String getConstraintProperty() {
        return constraintProperty;
    }

    @Override
    public void setConstraintProperty(@Nullable String constraintProperty) {
        this.constraintProperty = constraintProperty;
    }

    @Override
    public String getBackgroundColorProperty() {
        return backgroundColorProperty;
    }

    @Override
    public void setBackgroundColorProperty(@Nullable String backgroundColorProperty) {
        this.backgroundColorProperty = backgroundColorProperty;
    }

    @Override
    public String getBorderColorProperty() {
        return borderColorProperty;
    }

    @Override
    public void setBorderColorProperty(@Nullable String borderColorProperty) {
        this.borderColorProperty = borderColorProperty;
    }

    @Override
    public String getTextColorProperty() {
        return textColorProperty;
    }

    @Override
    public void setTextColorProperty(@Nullable String textColorProperty) {
        this.textColorProperty = textColorProperty;
    }

    protected boolean isEventPropertyChanged(String property) {
        return Objects.equals(groupIdProperty, property)
                || Objects.equals(allDayProperty, property)
                || Objects.equals(startDateTimeProperty, property)
                || Objects.equals(endDateTimeProperty, property)
                || Objects.equals(titleProperty, property)
                || Objects.equals(descriptionProperty, property)
                || Objects.equals(classNamesProperty, property)
                || Objects.equals(startEditableProperty, property)
                || Objects.equals(durationEditableProperty, property)
                || Objects.equals(displayProperty, property)
                || Objects.equals(overlapProperty, property)
                || Objects.equals(constraintProperty, property)
                || Objects.equals(backgroundColorProperty, property)
                || Objects.equals(borderColorProperty, property)
                || Objects.equals(textColorProperty, property);
    }
}
