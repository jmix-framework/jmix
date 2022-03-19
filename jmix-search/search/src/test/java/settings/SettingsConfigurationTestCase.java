/*
 * Copyright 2021 Haulmont.
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

package settings;

public class SettingsConfigurationTestCase {

    private final String name;
    private final Class<?> indexDefinitionClass;
    private final String pathToFileWithExpectedSettings;

    SettingsConfigurationTestCase(Builder builder) {
        this(builder.name, builder.indexDefinitionClass, builder.pathToFileWithExpectedSettings);
    }

    private SettingsConfigurationTestCase(String name, Class<?> indexDefinitionClass, String pathToFileWithExpectedSettings) {
        this.name = name;
        this.indexDefinitionClass = indexDefinitionClass;
        this.pathToFileWithExpectedSettings = pathToFileWithExpectedSettings;
    }

    public String getName() {
        return name;
    }

    public Class<?> getIndexDefinitionClass() {
        return indexDefinitionClass;
    }

    public String getPathToFileWithExpectedSettings() {
        return pathToFileWithExpectedSettings;
    }

    static Builder builder(String testCaseName) {
        return new Builder(testCaseName);
    }

    static class Builder {
        private final String name;
        private Class<?> indexDefinitionClass;
        private String pathToFileWithExpectedSettings;

        private Builder(String name) {
            this.name = name;
        }

        SettingsConfigurationTestCase.Builder indexDefinitionClass(Class<?> indexDefinitionClass) {
            this.indexDefinitionClass = indexDefinitionClass;
            return this;
        }

        SettingsConfigurationTestCase.Builder pathToFileWithExpectedSettings(String pathToFileWithExpectedSettings) {
            this.pathToFileWithExpectedSettings = pathToFileWithExpectedSettings;
            return this;
        }

        SettingsConfigurationTestCase build() {
            return new SettingsConfigurationTestCase(this);
        }
    }
}
