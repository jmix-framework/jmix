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

package io.jmix.core;

import javax.annotation.Nullable;
import javax.persistence.LockModeType;

/**
 * Base interface for load contexts used in {@link DataManager}.
 */
public interface DataLoadContext {

    /**
     * @param queryString query string. Only named parameters are supported.
     * @return  query definition object
     */
    DataLoadContextQuery setQueryString(String queryString);

    /**
     * @param lockMode lock mode to be used when executing query
     */
    void setLockMode(LockModeType lockMode);

    /**
     * @return lock mode to be used when executing query
     */
    @Nullable
    LockModeType getLockMode();
}
