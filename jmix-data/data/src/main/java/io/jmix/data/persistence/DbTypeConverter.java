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

package io.jmix.data.persistence;

import org.springframework.lang.Nullable;
import java.sql.ResultSet;

/**
 * Interface defining methods to convert data between Java objects and JDBC params and results depending on the current
 * DBMS type.
 * <br> The main goal is to convert dates and UUID.
 */
public interface DbTypeConverter {

    /**
     * Convert a JDBC ResultSet column value to a value appropriate for an entity attribute.
     *
     * @param resultSet JDBC ResultSet
     * @param column    ResultSet column number, starting from 1
     * @return corresponding value for an entity attribute
     */
    @Nullable
    Object getJavaObject(ResultSet resultSet, int column);

    /**
     * Convert an entity attribute value to a value appropriate for a JDBC parameter.
     *
     * @param value an entity attribute value
     * @return corresponding value for a JDBC parameter
     */
    Object getSqlObject(Object value);

    /**
     * Get a JDBC type corresponding to an entity attribute type.
     *
     * @param javaClass entity attribute type
     * @return corresponding JDBC type
     * @see java.sql.Types
     */
    int getSqlType(Class<?> javaClass);

    /**
     * Provides information about {@code jmix.data.dbmsType} and {@code jmix.data.dbmsVersion} this bean designed for. Version can be empty.
     */
    String getTypeAndVersion();
}