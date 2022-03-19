/*
 * Copyright 2020 Haulmont.
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
package com.haulmont.cuba.core.app;

import com.haulmont.cuba.core.config.Config;
import com.haulmont.cuba.core.config.Property;
import com.haulmont.cuba.core.config.Source;
import com.haulmont.cuba.core.config.SourceType;
import com.haulmont.cuba.core.config.defaults.DefaultBoolean;
import com.haulmont.cuba.core.config.defaults.DefaultInt;
import com.haulmont.cuba.core.config.defaults.DefaultLong;
import com.haulmont.cuba.core.config.defaults.DefaultString;

/**
 * Configuration parameters interface used by the CORE layer.
 */
@Source(type = SourceType.APP)
public interface SchedulingConfig extends Config {

    /**
     * @return Scheduled tasks execution control.
     */
    @Property("cuba.schedulingActive")
    @Source(type = SourceType.DATABASE)
    @DefaultBoolean(false)
    boolean getSchedulingActive();

    void setSchedulingActive(boolean value);

    /**
     * @return Scheduled tasks execution control.
     */
    @Property("cuba.schedulingInterval")
    @Source(type = SourceType.DATABASE)
    @DefaultLong(1000)
    long getSchedulingInterval();

    void setSchedulingInterval(long value);

    /**
     * @return Maximum size of thread pool which is used to process scheduled tasks
     */
    @Property("cuba.schedulingThreadPoolSize")
    @DefaultInt(10)
    int getSchedulingThreadPoolSize();

    void setSchedulingThreadPoolSize(int value);

    /**
     * @return This web application host name. Makes sense for CORE and WEB modules.
     */
    @Property("cuba.webHostName")
    @DefaultString("localhost")
    String getWebHostName();

    /**
     * @return This web application port. Makes sense for CORE and WEB modules.
     */
    @Property("cuba.webPort")
    @DefaultString("8080")
    String getWebPort();

    /**
     * @return This web application context name. Makes sense for CORE and WEB modules.
     */
    @Property("cuba.webContextName")
    @DefaultString("cuba")
    String getWebContextName();

    /**
     * Automatic testing mode indication.
     *
     * @return true if in test mode
     */
    @Property("cuba.testMode")
    @DefaultBoolean(false)
    boolean getTestMode();

}