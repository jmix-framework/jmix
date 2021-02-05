/*
 * Copyright 2020 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package io.jmix.hibernate.impl.types.date;

import org.hibernate.HibernateException;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JdbcTimestampTypeDescriptor;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Descriptor for {@link Timestamp} handling.
 *
 * @author Steve Ebersole
 */
public class JdbcDateTypeDescriptor extends JdbcTimestampTypeDescriptor {
    public static final JdbcDateTypeDescriptor INSTANCE = new JdbcDateTypeDescriptor();

    public JdbcDateTypeDescriptor() {
        super();
    }

    @Override
    public Date fromString(String string) {
        try {
            return new SimpleDateFormat(TIMESTAMP_FORMAT).parse(string);
        } catch (ParseException pe) {
            throw new HibernateException("could not parse timestamp string" + string, pe);
        }
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public <X> X unwrap(Date value, Class<X> type, WrapperOptions options) {
        if (value == null) {
            return null;
        }
        if (Timestamp.class.isAssignableFrom(type)) {
            final Timestamp rtn = Timestamp.class.isInstance(value)
                    ? (Timestamp) value
                    : new Timestamp(value.getTime());
            return (X) rtn;
        }
        if (java.sql.Date.class.isAssignableFrom(type)) {
            final java.sql.Date rtn = java.sql.Date.class.isInstance(value)
                    ? (java.sql.Date) value
                    : new java.sql.Date(value.getTime());
            return (X) rtn;
        }
        if (java.sql.Time.class.isAssignableFrom(type)) {
            final java.sql.Time rtn = java.sql.Time.class.isInstance(value)
                    ? (java.sql.Time) value
                    : new java.sql.Time(value.getTime());
            return (X) rtn;
        }
        if (Date.class.isAssignableFrom(type)) {
            return (X) value;
        }
        if (Calendar.class.isAssignableFrom(type)) {
            final GregorianCalendar cal = new GregorianCalendar();
            cal.setTimeInMillis(value.getTime());
            return (X) cal;
        }
        if (Long.class.isAssignableFrom(type)) {
            return (X) Long.valueOf(value.getTime());
        }
        throw unknownUnwrap(type);
    }

    @Override
    public <X> Date wrap(X value, WrapperOptions options) {
        if (value == null) {
            return null;
        }
        if (Timestamp.class.isInstance(value)) {
            return new Date(((Timestamp) value).getTime());
        }

        if (Long.class.isInstance(value)) {
            return new Date((Long) value);
        }

        if (Calendar.class.isInstance(value)) {
            return new Date(((Calendar) value).getTimeInMillis());
        }

        if (Date.class.isInstance(value)) {
            return new Date(((Date) value).getTime());
        }

        throw unknownWrap(value.getClass());
    }
}
