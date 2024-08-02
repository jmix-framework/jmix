package io.jmix.fullcalendar;

import java.io.Serializable;
import java.util.List;

public class DaysOfWeek implements Serializable {

    public final List<DayOfWeek> daysOfWeek;

    public DaysOfWeek(List<DayOfWeek> daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public List<DayOfWeek> getDaysOfWeek() {
        return daysOfWeek;
    }
}
