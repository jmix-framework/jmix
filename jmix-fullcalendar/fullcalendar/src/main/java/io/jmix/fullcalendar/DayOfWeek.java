package io.jmix.fullcalendar;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum DayOfWeek implements EnumClass<Integer> {

    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(3),
    THURSDAY(4),
    FRIDAY(5),
    SATURDAY(6),
    SUNDAY(0);

    private final Integer id;

    DayOfWeek(Integer id) {
        this.id = id;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Nullable
    public static DayOfWeek fromId(Integer id) {
        for (DayOfWeek at : DayOfWeek.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }

    public static DayOfWeek fromDayOfWeek(java.time.DayOfWeek dayOfWeek) {
        DayOfWeek result = SUNDAY;
        switch (dayOfWeek) {
            case MONDAY -> result = MONDAY;
            case TUESDAY -> result = TUESDAY;
            case WEDNESDAY -> result = WEDNESDAY;
            case THURSDAY -> result = THURSDAY;
            case FRIDAY -> result = FRIDAY;
            case SATURDAY -> result = SATURDAY;
        }
        return result;

    }
}
