/*
 * Copyright 2026 Haulmont.
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

package io.jmix.aitools.dataload.validation.validator;

import io.jmix.aitools.dataload.introspection.JpaDomainModelIntrospector;
import io.jmix.aitools.dataload.execution.GeneratedJpqlResult;
import io.jmix.aitools.dataload.validation.JpqlResultValidator;
import io.jmix.aitools.dataload.validation.JpqlValidationIssue;
import io.jmix.core.JmixOrder;
import io.jmix.core.Metadata;
import io.jmix.data.QueryParser;
import io.jmix.data.QueryTransformerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks that every entity referenced by the query is known to the introspected domain model. An entity named
 * anywhere in the text counts, including places the JPQL parser does not resolve to an entity, such as a
 * {@code TYPE()} comparison.
 */
@Component("aitls_UsedEntitiesValidator")
public class UsedEntitiesValidator implements JpqlResultValidator, Ordered {

    public static final String USED_ENTITY_UNKNOWN_CODE = "usedEntity.unknown";
    public static final String USED_ENTITY_UNKNOWN_GUIDANCE = "Use only entity names that are present in the provided" +
            " schema.";

    // A word that is not part of a path (`e.name`) or a parameter (`:name`).
    private static final Pattern STANDALONE_WORD_PATTERN = Pattern.compile("(?<![.:\\w$])[A-Za-z_$][\\w$]*");

    @Autowired
    protected JpaDomainModelIntrospector modelIntrospector;
    @Autowired
    protected Metadata metadata;
    @Autowired(required = false)
    protected QueryTransformerFactory queryTransformerFactory;

    @Override
    public List<JpqlValidationIssue> validate(GeneratedJpqlResult result) {
        Set<String> usedEntities = new LinkedHashSet<>();
        QueryParser queryParser = JpqlValidatorSupport.getQueryParser(queryTransformerFactory, result.getJpql());
        if (queryParser != null) {
            try {
                usedEntities.addAll(queryParser.getAllEntityNames());
            } catch (RuntimeException e) {
                // Not resolvable by the parser: the entity names written in the text are still checked below.
            }
        }
        // The parser leaves out entity type literals (`type(e) = X`, `type(e) in (X, Y)`): an entity excluded from
        // the model must not get into a query through them.
        usedEntities.addAll(mentionedEntityNames(result.getJpql()));

        List<JpqlValidationIssue> issues = new ArrayList<>();
        for (String usedEntity : usedEntities) {
            if (!modelIntrospector.containsEntity(usedEntity)) {
                issues.add(new JpqlValidationIssue(USED_ENTITY_UNKNOWN_CODE,
                        "Unknown used entity: " + usedEntity, USED_ENTITY_UNKNOWN_GUIDANCE));
            }
        }
        return issues;
    }

    protected Set<String> mentionedEntityNames(String jpql) {
        Set<String> entityNames = new LinkedHashSet<>();
        Matcher matcher = STANDALONE_WORD_PATTERN.matcher(JpqlValidatorSupport.stripStringLiterals(jpql));
        while (matcher.find()) {
            String word = matcher.group();
            if (metadata.findClass(word) != null) {
                entityNames.add(word);
            }
        }
        return entityNames;
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 1000;
    }
}
