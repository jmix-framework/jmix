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

import jakarta.annotation.Nullable;
import java.util.Map;

/**
 * Interface defining methods of getting some DBMS-specific values. It must be implemented for each supported DBMS type
 * and version.
 *
 * @see DbmsSpecifics
 */
public interface DbmsFeatures {

    /**
     * @return JPA implementation properties to set in persistence.xml
     */
    Map<String, String> getJpaParameters();

    /**
     * @return name of data type storing date and time
     */
    String getTimeStampType();

    /**
     * @return name of class representing UUID in JDBC driver, or null if no special class required
     */
    @Nullable
    String getUuidTypeClassName();

    /**
     * @return statement to issue for setting the current transaction timeout, or null if not required.
     * <p>The statement text should contain %d placeholder that will be replaced by timeout value in milliseconds.
     */
    @Nullable
    String getTransactionTimeoutStatement();

    /**
     * @return  regexp to extract a unique constraint name from an exception message
     */
    String getUniqueConstraintViolationPattern();

    /**
     * @return default sort order of null values
     */
    boolean isNullsLastSorting();

    /**
     * @return true if the DBMS supports equality check and sorting for LOB columns
     */
    boolean supportsLobSortingAndFiltering();

    /**
     * @return true if the DBMS supports paging only with order by
     */
    default boolean useOrderByForPaging() {
        return false;
    }

    /**
     * @return maximum number of values that can be used in the "IN" operator in a query.
     * {@code null} is returned if there is no any limit
     */
    @Nullable
    default Integer getMaxIdsBatchSize() {
        return null;
    }


    /**
     * Provides information about {@code jmix.data.dbmsType} and {@code jmix.data.dbmsVersion} this bean designed for. Version can be empty.
     */
    String getTypeAndVersion();

}
