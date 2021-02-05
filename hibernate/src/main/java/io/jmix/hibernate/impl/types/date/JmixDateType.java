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

package io.jmix.hibernate.impl.types.date;

import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.LiteralType;
import org.hibernate.type.StringType;
import org.hibernate.type.VersionType;
import org.hibernate.type.descriptor.sql.TimestampTypeDescriptor;

import java.util.Comparator;
import java.util.Date;

public class JmixDateType
        extends AbstractSingleColumnStandardBasicType<Date>
        implements VersionType<Date>, LiteralType<Date> {

    public static final JmixDateType INSTANCE = new JmixDateType();

    public JmixDateType() {
        super(TimestampTypeDescriptor.INSTANCE, JdbcDateTypeDescriptor.INSTANCE);
    }

    @Override
    public String getName() {
        return "jmix-date";
    }

    @Override
    public String[] getRegistrationKeys() {
        return new String[]{getName(), java.util.Date.class.getName()};
    }

    @Override
    public Date next(Date current, SharedSessionContractImplementor session) {
        return seed(session);
    }

    @Override
    public Date seed(SharedSessionContractImplementor session) {
        return new Date(System.currentTimeMillis());
    }

    @Override
    public Comparator<Date> getComparator() {
        return getJavaTypeDescriptor().getComparator();
    }

    @Override
    public String objectToSQLString(Date value, Dialect dialect) throws Exception {
        return StringType.INSTANCE.objectToSQLString(value.toString(), dialect);
    }

    @Override
    public Date fromStringValue(String xml) throws HibernateException {
        return fromString(xml);
    }
}
