/*
 * Copyright 2025 Haulmont.
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

package io.jmix.search.index;

import io.jmix.core.annotation.Internal;
import io.jmix.search.SearchProperties;
import io.jmix.search.exception.IndexDefinitionRejectedException;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@Internal
@Component("search_IndexNameGenerator")
public class StandardIndexNameGenerator implements IndexNameGenerator {

    protected static final String ENTITY_NAME_PLACEHOLDER = "{entityName}";
    protected static final String TENANT_ID_PLACEHOLDER = "{tenantId}";

    protected static final String TENANTLESS_PATTERN_PROPERTY = "jmix.search.tenantless-index-name-pattern";
    protected static final String TENANT_PATTERN_PROPERTY = "jmix.search.tenant-index-name-pattern";

    protected static final String SAMPLE_ENTITY_NAME = "sample_Entity";
    protected static final String SAMPLE_TENANT_ID = "sampleTenant";

    /**
     * Search engines don't accept these characters in index names.
     */
    protected static final Pattern FORBIDDEN_NAME_CHARS = Pattern.compile("[\\\\/*?\"<>|,#: ]");

    /**
     * Search engines don't accept index names starting with these characters.
     */
    protected static final String FORBIDDEN_NAME_START = "-_+";

    protected static final int MAX_INDEX_NAME_LENGTH = 255;
    @Autowired
    protected SearchProperties searchProperties;

    protected String tenantlessPattern;
    protected String tenantPattern;

    @PostConstruct
    protected void init() {
        tenantlessPattern = getTenantlessPattern(searchProperties);
        tenantPattern = getTenantPattern(searchProperties);
    }

    /**
     * Returns the configured tenantless pattern or, if it is not set, builds the pattern from the deprecated
     * index name prefix, so that index names of an existing application remain the same after upgrade.
     */
    @SuppressWarnings("removal")
    private String getTenantlessPattern(SearchProperties searchProperties) {
        String pattern = searchProperties.getTenantlessIndexNamePattern();
        if (StringUtils.isBlank(pattern)) {
            pattern = searchProperties.getSearchIndexNamePrefix() + ENTITY_NAME_PLACEHOLDER;
        }
        validatePattern(pattern, TENANTLESS_PATTERN_PROPERTY, false);
        return pattern;
    }

    /**
     * Returns the configured tenant pattern or, if it is not set, builds the pattern from the deprecated
     * index name prefix, keeping tenant index names consistent with tenantless ones.
     */
    @SuppressWarnings("removal")
    private String getTenantPattern(SearchProperties searchProperties) {
        String pattern = searchProperties.getTenantIndexNamePattern();
        if (StringUtils.isBlank(pattern)) {
            pattern = searchProperties.getSearchIndexNamePrefix()
                    + ENTITY_NAME_PLACEHOLDER + "_" + TENANT_ID_PLACEHOLDER;
        }
        validatePattern(pattern, TENANT_PATTERN_PROPERTY, true);
        return pattern;
    }

    /**
     * Checks that the pattern is able to produce a valid index name for every entity and tenant.
     */
    protected void validatePattern(String pattern, String propertyName, boolean tenantSpecific) {
        if (!pattern.contains(ENTITY_NAME_PLACEHOLDER)) {
            throw new IllegalArgumentException(String.format(
                    "Property '%s': index name pattern must contain %s", propertyName, ENTITY_NAME_PLACEHOLDER));
        }
        if (tenantSpecific && !pattern.contains(TENANT_ID_PLACEHOLDER)) {
            throw new IllegalArgumentException(String.format(
                    "Property '%s': tenant index name pattern must contain %s", propertyName, TENANT_ID_PLACEHOLDER));
        }
        if (!tenantSpecific && pattern.contains(TENANT_ID_PLACEHOLDER)) {
            throw new IllegalArgumentException(String.format(
                    "Property '%s': tenantless index name pattern must not contain %s",
                    propertyName, TENANT_ID_PLACEHOLDER));
        }
        String sample = pattern.replace(ENTITY_NAME_PLACEHOLDER, SAMPLE_ENTITY_NAME)
                .replace(TENANT_ID_PLACEHOLDER, SAMPLE_TENANT_ID);
        try {
            validateIndexName(toIndexName(sample));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format(
                    "Property '%s': index name pattern is not applicable. %s", propertyName, e.getMessage()), e);
        }
    }

    @Override
    public String generateIndexName(IndexConfiguration configuration, @Nullable String tenantId) {
        String entityName = configuration.getEntityName();
        if (StringUtils.isBlank(entityName)) {
            throw new IllegalArgumentException("Entity name must not be blank or null.");
        }
        String pattern = resolvePattern(configuration, tenantId);
        String indexName = toIndexName(pattern
                .replace(ENTITY_NAME_PLACEHOLDER, entityName)
                .replace(TENANT_ID_PLACEHOLDER, tenantId == null ? "" : tenantId));
        try {
            validateIndexName(indexName);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format(
                    "Unable to generate the index name of entity '%s'%s. %s",
                    entityName, tenantId == null ? "" : " and tenant '" + tenantId + "'", e.getMessage()), e);
        }
        return indexName;
    }

    /**
     * The pattern an entity declares for itself wins over the application-wide one: it is an explicit decision
     * about this entity, while the property is a default for everything that made no decision.
     */
    protected String resolvePattern(IndexConfiguration configuration, @Nullable String tenantId) {
        String declaredPattern = configuration.getIndexNamePattern();
        if (StringUtils.isNotBlank(declaredPattern)) {
            return declaredPattern;
        }
        return tenantId == null ? tenantlessPattern : tenantPattern;
    }

    /**
     * Rejects the definition that declared the pattern, not the application: the pattern of one entity is one
     * entity's business, unlike the application-wide patterns checked by {@link #validatePattern}.
     */
    @Override
    public void validateEntityIndexNamePattern(String pattern, String source, boolean splitByTenants) {
        if (StringUtils.isBlank(pattern)) {
            throw new IndexDefinitionRejectedException(
                    String.format("%s: index name pattern must not be blank", source));
        }
        if (splitByTenants && !pattern.contains(TENANT_ID_PLACEHOLDER)) {
            throw new IndexDefinitionRejectedException(String.format(
                    "%s: the data of the entity is stored in a separate index per tenant, so its index name pattern"
                            + " must contain %s", source, TENANT_ID_PLACEHOLDER));
        }
        if (!splitByTenants && pattern.contains(TENANT_ID_PLACEHOLDER)) {
            throw new IndexDefinitionRejectedException(String.format(
                    "%s: the data of the entity is stored in a single index, so its index name pattern must not"
                            + " contain %s", source, TENANT_ID_PLACEHOLDER));
        }
        String sample = pattern.replace(ENTITY_NAME_PLACEHOLDER, SAMPLE_ENTITY_NAME)
                .replace(TENANT_ID_PLACEHOLDER, SAMPLE_TENANT_ID);
        try {
            validateIndexName(toIndexName(sample));
        } catch (IllegalArgumentException e) {
            throw new IndexDefinitionRejectedException(String.format(
                    "%s: index name pattern is not applicable. %s", source, e.getMessage()));
        }
    }

    /**
     * Search engines reject index names containing uppercase characters, so a generated name is always lowercased.
     * <p>
     * The locale of the JVM is used, as it was before index names were computed here. Pinning a locale would be
     * the better rule - the Turkish one lowercases {@code I} to a dotless {@code i}, so the same entity gets two
     * different index names on two machines - but it would rename the indexes of every application running under
     * such a locale, and that is not this task's to do.
     */
    @SuppressWarnings("StringToUpperCaseOrToLowerCaseWithoutLocale")
    protected String toIndexName(String value) {
        return value.toLowerCase();
    }

    /**
     * The limit is set in bytes, but encoding the name is avoided in the common case: a UTF-8 byte length is never
     * less than the number of characters and never exceeds three bytes per character.
     */
    protected boolean isTooLong(String indexName) {
        int length = indexName.length();
        if (length > MAX_INDEX_NAME_LENGTH) {
            return true;
        }
        if (length * 3 <= MAX_INDEX_NAME_LENGTH) {
            return false;
        }
        return indexName.getBytes(StandardCharsets.UTF_8).length > MAX_INDEX_NAME_LENGTH;
    }

    /**
     * Checks the generated name against the index naming rules of the search engines. Unlike the pattern, the name
     * contains values taken from the application data, so it can't be validated by configuration checks alone.
     */
    protected void validateIndexName(String indexName) {
        if (indexName.isEmpty()) {
            throw new IllegalArgumentException("Index name is empty");
        }
        if (FORBIDDEN_NAME_CHARS.matcher(indexName).find()) {
            throw new IllegalArgumentException(String.format(
                    "Index name '%s' contains characters that are not allowed: %s",
                    indexName, "\\ / * ? \" < > | , # : and space"));
        }
        if (FORBIDDEN_NAME_START.indexOf(indexName.charAt(0)) >= 0) {
            throw new IllegalArgumentException(String.format(
                    "Index name '%s' starts with a character that is not allowed: one of '%s'",
                    indexName, FORBIDDEN_NAME_START));
        }
        if (".".equals(indexName) || "..".equals(indexName)) {
            throw new IllegalArgumentException(String.format("Index name '%s' is not allowed", indexName));
        }
        if (isTooLong(indexName)) {
            throw new IllegalArgumentException(String.format(
                    "Index name '%s' is longer than %d bytes", indexName, MAX_INDEX_NAME_LENGTH));
        }
    }
}
