package io.jmix.fullcalendarflowui.component.event;

import com.vaadin.flow.component.ComponentEvent;
import io.jmix.fullcalendarflowui.component.FullCalendar;
import io.jmix.fullcalendarflowui.kit.component.event.MouseEventDetails;
import org.springframework.lang.Nullable;

/**
 * The event is fired when the current selection is cleared. A selection might be cleared for a number of reasons:
 * <ul>
 *     <li>
 *         The user clicks away from the current selection (does’t happen when {@link FullCalendar#isUnselectAuto()}
 *         returns {@code false}).
 *     </li>
 *     <li>
 *         The user makes a new selection. The unselect event will be fired before the new selection occurs.
 *     </li>
 *     <li>
 *         The user moves forward or backward in the current calendar's display mode, or switches to an other mode.
 *     </li>
 *     <li>
 *         The unselect method is called through the {@link FullCalendar#unselect()} method.
 *     </li>
 * </ul>
 */
public class UnselectEvent extends ComponentEvent<FullCalendar> {

    protected final DisplayModeInfo displayModeInfo;

    protected final MouseEventDetails mouseEventDetails;

    public UnselectEvent(FullCalendar source, boolean fromClient,
                         DisplayModeInfo displayModeInfo,
                         @Nullable MouseEventDetails mouseEventDetails) {
        super(source, fromClient);

        this.displayModeInfo = displayModeInfo;
        this.mouseEventDetails = mouseEventDetails;
    }

    /**
     * @return information about current calendar's display mode
     */
    public DisplayModeInfo getDisplayModeInfo() {
        return displayModeInfo;
    }

    /**
     * @return information about mouse click or {@code null} if unselect has been triggered via the
     * {@link FullCalendar#unselect()} method
     */
    @Nullable
    public MouseEventDetails getMouseEventDetails() {
        return mouseEventDetails;
    }
}
