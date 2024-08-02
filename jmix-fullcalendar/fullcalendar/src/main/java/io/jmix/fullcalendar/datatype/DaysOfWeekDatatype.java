package io.jmix.fullcalendar.datatype;

import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.fullcalendar.DaysOfWeek;

import java.text.ParseException;
import java.util.Locale;

public class DaysOfWeekDatatype implements Datatype<DaysOfWeek> {

    @Override
    public String format(Object value) {
        return "";
    }

    @Override
    public String format(Object value, Locale locale) {
        return "";
    }

    @Override
    public DaysOfWeek parse(String value) throws ParseException {
        return null;
    }

    @Override
    public DaysOfWeek parse(String value, Locale locale) throws ParseException {
        return null;
    }
}
