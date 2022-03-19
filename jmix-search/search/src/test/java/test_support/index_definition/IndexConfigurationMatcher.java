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
import io.jmix.search.index.IndexConfiguration;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class IndexConfigurationMatcher extends TypeSafeMatcher<IndexConfiguration> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final String entityName;
    private final String indexName;
    private final Class<?> entityClass;
    private final JsonNode mapping;

    private IndexConfigurationMatcher(String entityName, String indexName, Class<?> entityClass, JsonNode mapping) {
        this.entityName = entityName;
        this.indexName = indexName;
        this.entityClass = entityClass;
        this.mapping = mapping;
    }

    @Override
    protected boolean matchesSafely(IndexConfiguration indexConfiguration) {
        JsonNode actualMapping = objectMapper.convertValue(indexConfiguration.getMapping(), JsonNode.class);

        return this.entityName.equals(indexConfiguration.getEntityName())
                && this.indexName.equals(indexConfiguration.getIndexName())
                && this.entityClass.equals(indexConfiguration.getEntityClass())
                && mapping.equals(actualMapping);
    }

    @Override
    public void describeTo(Description description) {
        String message = String.format("EntityName=%s, EntityClass=%s, IndexName=%s, Mapping=%s",
                entityName, entityClass.getName(), indexName, mapping);
        description.appendText(message);
    }

    @Override
    protected void describeMismatchSafely(IndexConfiguration item, Description mismatchDescription) {
        JsonNode actualMapping = objectMapper.convertValue(item.getMapping(), JsonNode.class);
        String message = String.format("EntityName=%s, EntityClass=%s, IndexName=%s, Mapping=%s",
                item.getEntityName(), item.getEntityClass().getName(), item.getIndexName(), actualMapping);
        mismatchDescription.appendText(message);
    }

    public static Matcher<IndexConfiguration> configureWith(String entityName, String indexName, Class<?> entityClass, JsonNode mapping) {
        return new IndexConfigurationMatcher(entityName, indexName, entityClass, mapping);
    }
}
