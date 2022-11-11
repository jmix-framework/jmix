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

public class String32UuidConverter implements Converter {

    private final static String32UuidConverter INSTANCE = new String32UuidConverter();

    public static String32UuidConverter getInstance() {
        return INSTANCE;
    }

    public String uuidToString(Object uuid) {
        return uuid != null ? uuid.toString().replace("-", "") : null;
    }

    @Override
    public Object convertObjectValueToDataValue(Object objectValue, Session session) {
        return uuidToString(objectValue);
    }

    @Override
    public Object convertDataValueToObjectValue(Object dataValue, Session session) {
        try {
            if (dataValue instanceof String) {
                StringBuilder sb = new StringBuilder((String) dataValue);
                sb.insert(8, '-');
                sb.insert(13, '-');
                sb.insert(18, '-');
                sb.insert(23, '-');
                return UuidProvider.fromString(sb.toString());
            } else {
                return dataValue;
            }
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