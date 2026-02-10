package io.jmix.fullcalendarflowui.component.event;

import com.vaadin.flow.component.ComponentEvent;
import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.kit.component.event.MouseEventDetails;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

/**
 * The event is fired when a date/time selection is made.
 * <p>
 * Selection mode can be enabled by {@link FullCalendar#setSelectionEnabled(boolean)}.
 */
public class SelectEvent extends ComponentEvent<FullCalendar> {

    protected final LocalDateTime startDateTime;

    protected final LocalDateTime endDateTime;

    protected final boolean allDay;

    protected final MouseEventDetails mouseEventDetails;

    protected final DisplayModeInfo displayModeInfo;

    public SelectEvent(FullCalendar source,
                       boolean fromClient,
                       @Nullable MouseEventDetails mouseEventDetails,
                       LocalDateTime startDateTime,
                       LocalDateTime endDateTime,
                       boolean allDay,
                       DisplayModeInfo displayModeInfo) {
        super(source, fromClient);

        this.mouseEventDetails = mouseEventDetails;

        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.allDay = allDay;
        this.displayModeInfo = displayModeInfo;
    }

    /**
     * Returns date-time as is from component without transformation. It means that value corresponds component's
     * TimeZone.
     *
     * @return start date-time of selection
     */
    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    /**
     * Returns date-time as is from component without transformation. It means that value corresponds component's
     * TimeZone.
     * <p>
     * Note that end date-time is exclusive. For instance, if the selection is all-day and the last date value is
     * {@code 2024-10-15}, the value of this property will be equal to {@code 2024-10-16}.
     *
     * @return end date-time of selection
     */
    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    /**
     * @return whether the selection happened on all-day cells
     */
    public boolean isAllDay() {
        return allDay;
    }

    /**
     * @return information about current calendar's display mode
     */
    public DisplayModeInfo getDisplayModeInfo() {
        return displayModeInfo;
    }

    /**
     * @return information about mouse click or {@code null} if selection is performed by component methods like
     * {@link FullCalendar#select(LocalDateTime, LocalDateTime)}
     */
    @Nullable
    public MouseEventDetails getMouseEventDetails() {
        return mouseEventDetails;
    }
}
