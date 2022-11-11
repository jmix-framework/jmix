/*
 * Copyright 2019 Haulmont.
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
import org.eclipse.persistence.platform.database.HSQLPlatform;

import java.sql.Types;

public class JmixHSQLPlatform extends HSQLPlatform implements UuidMappingInfo {
    @Override
    public boolean supportsNestingOuterJoins() {
        //nested joins supports in hsqldb from version 1.9
        //https://sourceforge.net/p/hsqldb/feature-requests/206/
        return true;
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
        return "varchar(36)";
    }

    @Override
    public Converter getUuidConverter() {
        return String36UuidConverter.getInstance();
    }
}
