/*
 * Copyright 2025 Haulmont.
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


import org.eclipse.persistence.mappings.converters.Converter;

import java.sql.Types;
import java.util.UUID;

/**
 * Platform for MariaDB 10.7+ with UUID column type support.
 * Use this platform for new databases only. For existing databases with already generated CHAR(36) columns, use {@link JmixMariaDBPlatform} instead.
 */
public class JmixMariaDB107Platform extends JmixMariaDBPlatform {

    @Override
    public int getUuidSqlType() {
        return Types.OTHER;
    }

    @Override
    public Class<?> getUuidType() {
        return UUID.class;
    }

    @Override
    public String getUuidColumnDefinition() {
        return "UUID";
    }

    @Override
    public Converter getUuidConverter() {
        return String36UuidConverter.getInstance();
    }
}
