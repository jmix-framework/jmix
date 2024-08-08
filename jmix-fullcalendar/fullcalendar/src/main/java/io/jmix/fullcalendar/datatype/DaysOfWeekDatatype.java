package io.jmix.fullcalendar.datatype;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.annotation.DatatypeDef;
import io.jmix.core.metamodel.annotation.Ddl;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.fullcalendar.DayOfWeek;
import io.jmix.fullcalendar.DaysOfWeek;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.text.ParseException;
import java.time.temporal.WeekFields;
import java.util.*;

@DatatypeDef(id = "daysOfWeek", javaClass = DaysOfWeek.class, defaultForClass = true, value = "fclndr_DaysOfWeek")
@Ddl("varchar(200)")
public class DaysOfWeekDatatype implements Datatype<DaysOfWeek> {

    protected Messages messages;

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Override
    public String format(@Nullable Object value) {
        return format(value, Locale.ENGLISH);
    }

    @Override
    public String format(@Nullable Object value, Locale locale) {
        if (value instanceof DaysOfWeek daysOfWeek) {
            List<DayOfWeek> days = sortDaysOfWeek(daysOfWeek.getDaysOfWeek(), locale);

            return Joiner.on(", ")
                    .appendTo(new StringBuilder(),
                            days.stream().map((d) -> messages.getMessage(d, locale)).toList())
                    .toString();
        }
        return "";
    }

    @Override
    public DaysOfWeek parse(@Nullable String value) throws ParseException {
        return parse(value, Locale.ENGLISH);
    }

    @Override
    public DaysOfWeek parse(@Nullable String value, Locale locale) throws ParseException {
        if (value == null) {
            return null;
        }
        if (value.trim().isEmpty()) {
            return new DaysOfWeek(Set.of());
        }
        List<String> localizedDaysOfWeek = Splitter.on(", ")
                .omitEmptyStrings()
                .trimResults()
                .splitToList(value);

        List<DayOfWeek> allDaysOfWeek = Arrays.stream(DayOfWeek.values()).toList();
        Set<DayOfWeek> resultDaysOfWeek = new HashSet<>(localizedDaysOfWeek.size());

        for (String localizedDay : localizedDaysOfWeek) {
            allDaysOfWeek.stream()
                    .filter(d -> messages.getMessage(d, locale).equals(localizedDay))
                    .findFirst()
                    .ifPresent(resultDaysOfWeek::add);
        }
        return new DaysOfWeek(resultDaysOfWeek);
    }

    protected List<DayOfWeek> sortDaysOfWeek(Collection<DayOfWeek> value, Locale locale) {
        DayOfWeek firstDay = DayOfWeek.fromDayOfWeek(WeekFields.of(locale).getFirstDayOfWeek());

        return DaysOfWeekDatatypeUtils.sortByFirstDay(value, firstDay);
    }
}
