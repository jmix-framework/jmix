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

package io.jmix.eclipselink.impl.dbms;

import org.eclipse.persistence.exceptions.ConversionException;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.platform.database.MariaDBPlatform;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

/**
 * MariaDB platform for databases with {@code char(36)} ID columns. {@code char(36)} id columns are used by:
 * <ul>
 *     <li>MariaDB 10.6 or earlier - UUID column type is not yet supported</li>
 *     <li>old databases which have been created with Liquibase 4.25 or earlier (Jmix 2.4.0 or earlier)</li>
 * </ul>
 */
public class JmixMariaDBPlatform extends MariaDBPlatform implements UuidMappingInfo {

    @Override
    public void setParameterValueInDatabaseCall(Object parameter,
                                                PreparedStatement statement,
                                                int index,
                                                AbstractSession session) throws SQLException {

        if (parameter instanceof UUID uuid)
            parameter = getUuidConverter().convertObjectValueToDataValue(uuid, session);


        super.setParameterValueInDatabaseCall(parameter, statement, index, session);
    }

    @Override
    public Object convertObject(Object sourceObject, Class javaClass) throws ConversionException {
        if (sourceObject instanceof UUID uuid && javaClass == String.class) {
            return getUuidConverter().convertObjectValueToDataValue(uuid, null);
        }
        return super.convertObject(sourceObject, javaClass);
    }

    @Override
    public int getUuidSqlType() {
        return Types.VARCHAR;
    }

    @Override
    public Class<?> getUuidType() {
        return String.class;
    }

    @Override
    public String getUuidColumnDefinition() {
        return "varchar(32)";
    }

    @Override
    public Converter getUuidConverter() {
        return String32UuidConverter.getInstance();
    }
}