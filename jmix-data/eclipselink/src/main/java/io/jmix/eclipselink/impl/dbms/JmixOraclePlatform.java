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
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.platform.database.Oracle10Platform;

import java.sql.Types;
import java.util.UUID;

public class JmixOraclePlatform extends Oracle10Platform implements UuidMappingInfo {

    @Override
    public Object convertObject(Object sourceObject, Class javaClass) throws ConversionException {
        if (sourceObject instanceof UUID && javaClass == String.class) {
            return String32UuidConverter.getInstance().uuidToString(sourceObject);
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
        return "varchar2(32)";
    }

    @Override
    public Converter getUuidConverter() {
        return String32UuidConverter.getInstance();
    }
}