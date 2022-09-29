/*
 * Copyright 2022 Haulmont.
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

import io.jmix.core.UuidProvider;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.sessions.Session;

public class String36UuidConverter implements Converter {

    private final static String36UuidConverter INSTANCE = new String36UuidConverter();

    public static String36UuidConverter getInstance() {
        return INSTANCE;
    }

    @Override
    public Object convertObjectValueToDataValue(Object objectValue, Session session) {
        return objectValue != null ? objectValue.toString() : null;
    }

    @Override
    public Object convertDataValueToObjectValue(Object dataValue, Session session) {
        try {
            return dataValue instanceof String ? UuidProvider.fromString((String) dataValue) : dataValue;
        } catch (Exception e) {
            throw new RuntimeException("Error creating UUID from database value '" + dataValue + "'", e);
        }
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public void initialize(DatabaseMapping mapping, Session session) {
    }
}