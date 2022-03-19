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

package index_definition;

public class AnnotatedIndexDefinitionProcessorTestCase {

    private final String name;
    private final Class<?> indexDefinitionClass;
    private final String expectedEntityName;
    private final String expectedIndexName;
    private final Class<?> expectedEntityClass;
    private final String pathToFileWithExpectedMapping;

    AnnotatedIndexDefinitionProcessorTestCase(Builder builder) {
        this(
                builder.name,
                builder.indexDefinitionClass,
                builder.expectedEntityName,
                builder.expectedIndexName,
                builder.expectedEntityClass,
                builder.pathToFileWithExpectedMapping
        );
    }

    private AnnotatedIndexDefinitionProcessorTestCase(String name,
                                                      Class<?> indexDefinitionClass,
                                                      String expectedEntityName,
                                                      String expectedIndexName,
                                                      Class<?> expectedEntityClass,
                                                      String pathToFileWithExpectedMapping) {
        this.name = name;
        this.indexDefinitionClass = indexDefinitionClass;
        this.expectedEntityName = expectedEntityName;
        this.expectedIndexName = expectedIndexName;
        this.expectedEntityClass = expectedEntityClass;
        this.pathToFileWithExpectedMapping = pathToFileWithExpectedMapping;
    }

    @Override
    public String toString() {
        return name;
    }

    static Builder builder(String testCaseName) {
        return new Builder(testCaseName);
    }

    Class<?> getIndexDefinitionClass() {
        return indexDefinitionClass;
    }

    String getExpectedEntityName() {
        return expectedEntityName;
    }

    String getExpectedIndexName() {
        return expectedIndexName;
    }

    Class<?> getExpectedEntityClass() {
        return expectedEntityClass;
    }

    String getPathToFileWithExpectedMapping() {
        return pathToFileWithExpectedMapping;
    }

    static class Builder {
        private final String name;
        private Class<?> indexDefinitionClass;
        private String expectedEntityName;
        private String expectedIndexName;
        private Class<?> expectedEntityClass;
        private String pathToFileWithExpectedMapping;

        private Builder(String name) {
            this.name = name;
        }

        Builder indexDefinitionClass(Class<?> indexDefinitionClass) {
            this.indexDefinitionClass = indexDefinitionClass;
            return this;
        }

        Builder expectedEntityName(String expectedEntityName) {
            this.expectedEntityName = expectedEntityName;
            return this;
        }

        Builder expectedIndexName(String expectedIndexName) {
            this.expectedIndexName = expectedIndexName;
            return this;
        }

        Builder expectedEntityClass(Class<?> expectedEntityClass) {
            this.expectedEntityClass = expectedEntityClass;
            return this;
        }

        Builder pathToFileWithExpectedMapping(String pathToFileWithExpectedMapping) {
            this.pathToFileWithExpectedMapping = pathToFileWithExpectedMapping;
            return this;
        }

        AnnotatedIndexDefinitionProcessorTestCase build() {
            return new AnnotatedIndexDefinitionProcessorTestCase(this);
        }
    }
}
