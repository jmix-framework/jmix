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

import org.eclipse.persistence.mappings.converters.Converter;

import java.util.UUID;

public interface UuidMappingInfo {

    int getUuidSqlType();

    Class<?> getUuidType();

    String getUuidColumnDefinition();

    Converter getUuidConverter();

    /**
     * Sometimes for some complex queries Eclipselink does not process collection parameter elements with converters.
     * It only appends them to query using limited set of conversion checks instead.
     * E.g. in org.eclipse.persistence.internal.databaseaccess.DatabasePlatform#setParameterValueInDatabaseCall(..)
     * or in org.eclipse.persistence.internal.databaseaccess.DatabasePlatform#printValuelist(..)
     * for case jmix-framework/jmix#1073
     * <p>
     * We have to convert UUID manually in such cases for several database types.
     *
     * @return converted to db type UUID parameter or the same parameter for other types
     */
    default Object convertToDataValueIfUUID(Object parameter) {
        if (parameter instanceof UUID) {
            parameter = getUuidConverter().convertObjectValueToDataValue(parameter, null);
        }
        return parameter;
    }
}
