package io.jmix.fullcalendar;

import io.jmix.core.common.util.Preconditions;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.*;

/**
 * The class represents container for {@link DayOfWeek}. It can be used in entities as a property type.
 */
public class DaysOfWeek implements Serializable {

    public final Set<DayOfWeek> daysOfWeek;

    public DaysOfWeek(Set<DayOfWeek> daysOfWeek) {
        Preconditions.checkNotNullArgument(daysOfWeek);
        this.daysOfWeek = daysOfWeek;
    }

    public Set<DayOfWeek> getDaysOfWeek() {
        return daysOfWeek;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return (obj instanceof DaysOfWeek dObj)
                && CollectionUtils.isEqualCollection(dObj.daysOfWeek, daysOfWeek);
    }

    @Override
    public int hashCode() {
        return Objects.hash((daysOfWeek.toArray()));
    }
}
