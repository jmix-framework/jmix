/*
 * Copyright 2024 Haulmont.
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

package io.jmix.restds.exception;

import org.springframework.web.client.ResourceAccessException;

/**
 * Thrown when REST API cannot be accessed.
 */
public class RestDataStoreAccessException extends RuntimeException {

    private final String dataStoreName;

    public RestDataStoreAccessException(String dataStoreName, ResourceAccessException cause) {
        super("Cannot access '" + dataStoreName + "' REST API", cause);
        this.dataStoreName = dataStoreName;
    }

    /**
     * Returns name of the data store that cannot be accessed.
     */
    public String getDataStoreName() {
        return dataStoreName;
    }
}
