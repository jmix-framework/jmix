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

package io.jmix.reports.yarg.util.properties;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DefaultPropertiesLoader implements PropertiesLoader {

    public static final String DEFAULT_PROPERTIES_PATH = "./reporting.properties";

    protected String propertiesPath = DEFAULT_PROPERTIES_PATH;
    protected Properties properties;
    protected final Object lock = new Object();

    public DefaultPropertiesLoader() {
    }

    public DefaultPropertiesLoader(String propertiesPath) {
        this.propertiesPath = propertiesPath;
    }

    public Properties load() throws IOException {
        synchronized (lock) {
            if (properties == null) {
                properties = new Properties();
                properties.load(new FileInputStream(propertiesPath));
                properties.putAll(System.getProperties());
                return properties;
            } else {
                return properties;
            }
        }
    }
}
