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

import io.jmix.core.Metadata;
import io.jmix.dynattr.DynAttrMetadata;

import java.util.List;

public class AnnotatedIndexDefinitionProcessorTestCase {

    private final String name;
    private final Class<?> indexDefinitionClass;
    private final String expectedEntityName;
    private final Class<?> expectedEntityClass;
    private final String pathToFileWithExpectedMapping;
    private final String expectedIndexNamePattern;
    private final AttributeMocker dynamicAttributesMocker;

    AnnotatedIndexDefinitionProcessorTestCase(Builder builder) {
        this(
                builder.name,
                builder.indexDefinitionClass,
                builder.expectedEntityName,
                builder.expectedEntityClass,
                builder.pathToFileWithExpectedMapping,
                builder.expectedIndexNamePattern,
                builder.dynamicAttributesMocker
        );
    }

    private AnnotatedIndexDefinitionProcessorTestCase(String name,
                                                      Class<?> indexDefinitionClass,
                                                      String expectedEntityName,
                                                      Class<?> expectedEntityClass,
                                                      String pathToFileWithExpectedMapping,
                                                      String expectedIndexNamePattern,
                                                      AttributeMocker dynamicAttributesMocker) {
        this.name = name;
        this.indexDefinitionClass = indexDefinitionClass;
        this.expectedEntityName = expectedEntityName;
        this.expectedEntityClass = expectedEntityClass;
        this.pathToFileWithExpectedMapping = pathToFileWithExpectedMapping;
        this.expectedIndexNamePattern = expectedIndexNamePattern;
        this.dynamicAttributesMocker = dynamicAttributesMocker;
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

    Class<?> getExpectedEntityClass() {
        return expectedEntityClass;
    }

    String getPathToFileWithExpectedMapping() {
        return pathToFileWithExpectedMapping;
    }

    public String getExpectedIndexNamePattern() {
        return expectedIndexNamePattern;
    }

    public AttributeMocker getDynAttrMetadataConsumer() {
        return dynamicAttributesMocker;
    }

    static class Builder {
        private final String name;
        private Class<?> indexDefinitionClass;
        private String expectedEntityName;
        private Class<?> expectedEntityClass;
        private String pathToFileWithExpectedMapping;
        private String expectedIndexNamePattern;
        private AttributeMocker dynamicAttributesMocker = (dynAttrMetadata, metadata) -> {};

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

        /**
         * The pattern an entity declares for itself in {@code @JmixEntitySearchIndex(indexName = "...")}.
         * <p>
         * Left out, it expects none - which is what almost every definition has. A definition that does declare
         * one and forgets to say so here fails, because the matcher compares the expectation as it stands.
         */
        Builder expectedIndexNamePattern(String expectedIndexNamePattern) {
            this.expectedIndexNamePattern = expectedIndexNamePattern;
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

        Builder dynamicAttributes(AttributeMocker dynamicAttributesMocker) {
            this.dynamicAttributesMocker = dynamicAttributesMocker;
            return this;
        }

        AnnotatedIndexDefinitionProcessorTestCase build() {
            return new AnnotatedIndexDefinitionProcessorTestCase(this);
        }
    }

    @FunctionalInterface
    public interface AttributeMocker {
        void addMocks(DynAttrMetadata dynAttrMetadata, Metadata metadata);
    }
}
