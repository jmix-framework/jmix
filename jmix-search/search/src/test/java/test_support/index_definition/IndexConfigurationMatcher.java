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

package test_support.index_definition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;
import io.jmix.search.index.IndexConfiguration;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Custom matcher for verifying properties of {@link IndexConfiguration} instances.
 * This matcher ensures that the provided {@link IndexConfiguration} instance matches
 * the expected configuration, including entity name, index name, entity class, and mapping.
 *
 * <p>This class extends {@link TypeSafeMatcher} to perform type-safe comparisons
 * of {@link IndexConfiguration} objects.</p>
 */
public class IndexConfigurationMatcher extends TypeSafeMatcher<IndexConfiguration> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final String entityName;
    private final Class<?> entityClass;
    private final JsonNode mapping;
    private final String indexNamePattern;

    private IndexConfigurationMatcher(String entityName, Class<?> entityClass, JsonNode mapping,
                                      String indexNamePattern) {
        this.entityName = entityName;
        this.entityClass = entityClass;
        this.mapping = mapping;
        this.indexNamePattern = indexNamePattern;
    }

    @Override
    protected boolean matchesSafely(IndexConfiguration indexConfiguration) {
        JsonNode actualMapping = objectMapper.convertValue(indexConfiguration.getMapping(), JsonNode.class);

        return this.entityName.equals(indexConfiguration.getEntityName())
                && this.entityClass.equals(indexConfiguration.getEntityClass())
                && mapping.equals(actualMapping)
                && Objects.equals(this.indexNamePattern, indexConfiguration.getIndexNamePattern());
    }

    @Override
    public void describeTo(Description description) {
        String message = String.format("EntityName=%s, EntityClass=%s, IndexNamePattern=%s, Mapping=%s",
                entityName, entityClass.getName(), indexNamePattern, mapping);
        description.appendText(message);
    }

    @Override
    protected void describeMismatchSafely(IndexConfiguration item, Description mismatchDescription) {
        JsonNode actualMapping = objectMapper.convertValue(item.getMapping(), JsonNode.class);
        String message = String.format("EntityName=%s, EntityClass=%s, IndexNamePattern=%s, Mapping=%s",
                item.getEntityName(), item.getEntityClass().getName(), item.getIndexNamePattern(), actualMapping);
        mismatchDescription.appendText(message);
    }

    public static Matcher<IndexConfiguration> configureWith(String entityName, Class<?> entityClass,
                                                           JsonNode mapping, String indexNamePattern) {
        return new IndexConfigurationMatcher(entityName, entityClass, mapping, indexNamePattern);
    }
}
