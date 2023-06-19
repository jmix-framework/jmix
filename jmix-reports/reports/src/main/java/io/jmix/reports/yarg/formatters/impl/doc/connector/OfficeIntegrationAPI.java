/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.jmix.reports.yarg.formatters.impl.doc.connector;

import io.jmix.reports.yarg.formatters.impl.doc.connector.NoFreePortsException;
import io.jmix.reports.yarg.formatters.impl.doc.connector.OfficeTask;

public interface OfficeIntegrationAPI {

    int DEFAULT_RETRY_COUNT = 2;
    int DEFAULT_RETRY_INTERVAL = 1000;
    int DEFAULT_TIMEOUT = 60;
    int DEFAULT_CONNECTION_TIMEOUT = 15;

    String getTemporaryDirPath();

    Integer getTimeoutInSeconds();

    int getCountOfRetry();

    int getRetryIntervalMs();

    Boolean isDisplayDeviceAvailable();

    void runTaskWithTimeout(OfficeTask officeTask, int timeoutInSeconds) throws NoFreePortsException;
}
