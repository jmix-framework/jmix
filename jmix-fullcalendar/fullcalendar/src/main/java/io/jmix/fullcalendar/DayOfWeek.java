package io.jmix.fullcalendar;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

/**
 * The enum represents day of week with ids that corresponds the order in full calendar component.
 */
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

    /**
     * @param id the ID of enum instance
     * @return enum instance or {@code null} if there is no enum with the provided ID
     */
    @Nullable
    public static DayOfWeek fromId(Integer id) {
        for (DayOfWeek at : DayOfWeek.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }

    /**
     * Returns day of week from {@link java.time.DayOfWeek}
     *
     * @param dayOfWeek dayOfWeek to convert
     * @return day of week
     */
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
