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

package io.jmix.jmxconsole;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * JMX console configuration interface
 */
@ConfigurationProperties(prefix = "jmix.jmxconsole")
public class JmxConsoleProperties {

    /**
     * Timeout (in seconds) for MBean operation invoked in JMX console
     */
    int jmxConsoleMBeanOperationTimeoutSec;


    public JmxConsoleProperties(@DefaultValue("600") int jmxConsoleMBeanOperationTimeoutSec) {
        this.jmxConsoleMBeanOperationTimeoutSec = jmxConsoleMBeanOperationTimeoutSec;
    }

    /**
     * @see #jmxConsoleMBeanOperationTimeoutSec
     */
    public int getJmxConsoleMBeanOperationTimeoutSec() {
        return jmxConsoleMBeanOperationTimeoutSec;
    }
}
