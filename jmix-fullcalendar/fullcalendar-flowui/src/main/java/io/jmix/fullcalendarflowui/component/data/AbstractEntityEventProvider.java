package io.jmix.fullcalendarflowui.component.data;

import com.vaadin.flow.data.provider.AbstractDataProvider;
import io.jmix.core.common.util.Preconditions;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 *
 * @param <F> type of filter object
 */
public abstract class AbstractEntityEventProvider<F> extends AbstractDataProvider<CalendarEvent, F>
        implements EntityCalendarEventProvider {

    protected String id;

    protected String groupIdProperty;
    protected String allDayProperty;
    protected String startDateTimeProperty;
    protected String endDateTimeProperty;
    protected String titleProperty;
    protected String descriptionProperty;
    protected String interactiveProperty;
    protected String classNamesProperty;
    protected String startEditableProperty;
    protected String durationEditableProperty;
    protected String displayProperty;
    protected String overlapProperty;
    protected String constraintProperty;
    protected String backgroundColorProperty;
    protected String borderColorProperty;
    protected String textColorProperty;

    protected List<String> additionalProperties;

    protected String recurringDaysOfWeekProperty;
    protected String recurringStartDateProperty;
    protected String recurringEndDateProperty;
    protected String recurringStartTimeProperty;
    protected String recurringEndTimeProperty;

    public AbstractEntityEventProvider() {
        this(EventProviderUtils.generateId());
    }

    public AbstractEntityEventProvider(String id) {
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
    public String getInteractiveProperty() {
        return interactiveProperty;
    }

    @Override
    public void setInteractiveProperty(@Nullable String interactiveProperty) {
        this.interactiveProperty = interactiveProperty;
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

    @Override
    public List<String> getAdditionalProperties() {
        return CollectionUtils.isEmpty(additionalProperties) ? Collections.emptyList() : additionalProperties;
    }

    @Override
    public void setAdditionalProperties(@Nullable List<String> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    @Override
    public String getRecurringDaysOfWeekProperty() {
        return recurringDaysOfWeekProperty;
    }

    @Override
    public void setRecurringDaysOfWeekProperty(@Nullable String recurringDaysOfWeekProperty) {
        this.recurringDaysOfWeekProperty = recurringDaysOfWeekProperty;
    }

    @Override
    public String getRecurringStartDateProperty() {
        return recurringStartDateProperty;
    }

    @Override
    public void setRecurringStartDateProperty(@Nullable String recurringStartDateProperty) {
        this.recurringStartDateProperty = recurringStartDateProperty;
    }

    @Override
    public String getRecurringEndDateProperty() {
        return recurringEndDateProperty;
    }

    @Override
    public void setRecurringEndDateProperty(@Nullable String recurringEndDateProperty) {
        this.recurringEndDateProperty = recurringEndDateProperty;
    }

    @Override
    public String getRecurringStartTimeProperty() {
        return recurringStartTimeProperty;
    }

    @Override
    public void setRecurringStartTimeProperty(@Nullable String recurringStartTimeProperty) {
        this.recurringStartTimeProperty = recurringStartTimeProperty;
    }

    @Override
    public String getRecurringEndTimeProperty() {
        return recurringEndTimeProperty;
    }

    @Override
    public void setRecurringEndTimeProperty(@Nullable String recurringEndTimeProperty) {
        this.recurringEndTimeProperty = recurringEndTimeProperty;
    }

    protected boolean isAdditionalProperty(String property) {
        return CollectionUtils.isNotEmpty(additionalProperties)
                && additionalProperties.contains(property);
    }

    protected boolean isEventPropertyChanged(String property) {
        boolean isProperty = Objects.equals(groupIdProperty, property)
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
                || Objects.equals(textColorProperty, property)
                || Objects.equals(recurringDaysOfWeekProperty, property)
                || Objects.equals(recurringStartDateProperty, property)
                || Objects.equals(recurringEndDateProperty, property)
                || Objects.equals(recurringStartTimeProperty, property)
                || Objects.equals(recurringEndTimeProperty, property);
        return isProperty || isAdditionalProperty(property);
    }
}
