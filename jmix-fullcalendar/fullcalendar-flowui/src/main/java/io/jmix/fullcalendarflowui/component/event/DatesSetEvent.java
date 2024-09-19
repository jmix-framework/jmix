package io.jmix.fullcalendarflowui.component.event;

import com.vaadin.flow.component.ComponentEvent;
import io.jmix.fullcalendarflowui.component.FullCalendar;

import java.time.LocalDate;

/**
 * The event is fired after the calendar’s date range has been initially set or changed in some way
 * and the DOM of component has been updated.
 * <p>
 * The calendar’s dates can change any time the user does the following:
 * <ul>
 *     <li>
 *         navigates to next and previous;
 *     </li>
 *     <li>
 *         switches the display mode;
 *     </li>
 *     <li>
 *         clicks a navigation link (e.g. week or day if {@link FullCalendar#isNavigationLinksEnabled()} is
 *         {@code true}).
 *     </li>
 * </ul>
 * The dates can also change when the current-date is manipulated via the API, such as when
 * {@link FullCalendar#navigateToDate(LocalDate)} is called.
 */
public class DatesSetEvent extends ComponentEvent<FullCalendar> {

    protected final LocalDate startDate;

    protected final LocalDate endDate;

    protected final DisplayModeInfo displayModeInfo;

    public DatesSetEvent(FullCalendar fullCalendar, boolean fromClient,
                         LocalDate startDate,
                         LocalDate endDate,
                         DisplayModeInfo displayModeInfo) {
        super(fullCalendar, fromClient);

        this.startDate = startDate;
        this.endDate = endDate;

        this.displayModeInfo = displayModeInfo;
    }

    /**
     * @return the beginning of the range the calendar needs events for
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Note, this value is exclusive.
     *
     * @return the end of the range the calendar needs events for
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * @return information about current calendar's display mode
     */
    public DisplayModeInfo getDisplayModeInfo() {
        return displayModeInfo;
    }
}
