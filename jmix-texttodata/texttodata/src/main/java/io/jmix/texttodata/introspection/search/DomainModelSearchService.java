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

package io.jmix.texttodata.introspection.search;

import io.jmix.texttodata.introspection.model.EntityDescriptor;
import io.jmix.texttodata.introspection.model.EntityPropertyDescriptor;
import io.jmix.texttodata.introspection.model.EnumPropertyDescriptor;
import io.jmix.texttodata.introspection.model.EnumValueDescriptor;
import io.jmix.texttodata.introspection.model.RelationPropertyDescriptor;
import io.jmix.texttodata.introspection.registry.DomainModelRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

@Component("textdt_DomainModelSearchService")
public class DomainModelSearchService {

    protected static final Pattern TOKEN_SPLIT_PATTERN = Pattern.compile("[^\\p{IsAlphabetic}\\p{IsDigit}_]+");
    protected static final int DEFAULT_LIMIT = 10;

    @Autowired
    protected DomainModelRegistry domainModelRegistry;

    public List<DomainModelSearchCandidate> search(String text) {
        return search(text, DEFAULT_LIMIT);
    }

    public List<DomainModelSearchCandidate> search(String text, int limit) {
        List<String> tokens = tokenize(text);
        if (tokens.isEmpty() || limit <= 0) {
            return List.of();
        }

        List<DomainModelSearchCandidate> candidates = new ArrayList<>();
        for (EntityDescriptor entityDescriptor : domainModelRegistry.getEntityDescriptors()) {
            DomainModelSearchCandidate candidate = scoreEntity(entityDescriptor, tokens);
            if (candidate != null) {
                candidates.add(candidate);
            }
        }

        candidates.sort(Comparator
                .comparingInt(DomainModelSearchCandidate::getScore).reversed()
                .thenComparing(candidate -> candidate.getEntity().getName()));

        if (candidates.size() <= limit) {
            return Collections.unmodifiableList(candidates);
        }
        return Collections.unmodifiableList(candidates.subList(0, limit));
    }

    protected DomainModelSearchCandidate scoreEntity(EntityDescriptor entityDescriptor, List<String> tokens) {
        int score = 0;
        Set<String> matchedBy = new LinkedHashSet<>();

        for (String token : tokens) {
            score += scoreValue(token, entityDescriptor.getName(), 100, 50, "entityName", matchedBy);
            score += scoreValues(token, entityDescriptor.getLocalizedNames(), 90, 45, "entityCaption", matchedBy);

            for (EntityPropertyDescriptor propertyDescriptor : entityDescriptor.getProperties()) {
                score += scoreValue(token, propertyDescriptor.getName(), 60, 30,
                        "property:" + propertyDescriptor.getName(), matchedBy);
                score += scoreValues(token, propertyDescriptor.getLocalizedNames(), 55, 25,
                        "propertyCaption:" + propertyDescriptor.getName(), matchedBy);
                score += scoreValue(token, propertyDescriptor.getComment(), 35, 15,
                        "propertyComment:" + propertyDescriptor.getName(), matchedBy);

                if (propertyDescriptor instanceof RelationPropertyDescriptor relationPropertyDescriptor) {
                    score += scoreValue(token, relationPropertyDescriptor.getTargetEntityName(), 40, 20,
                            "relationTarget:" + propertyDescriptor.getName(), matchedBy);
                    score += scoreValues(token, relationPropertyDescriptor.getTargetEntityLocalizedNames(), 35, 15,
                            "relationTargetCaption:" + propertyDescriptor.getName(), matchedBy);
                }

                if (propertyDescriptor instanceof EnumPropertyDescriptor enumPropertyDescriptor) {
                    score += scoreValue(token, enumPropertyDescriptor.getEnumType().getName(), 30, 15,
                            "enumType:" + propertyDescriptor.getName(), matchedBy);
                    for (EnumValueDescriptor enumValueDescriptor : enumPropertyDescriptor.getEnumType().getConstants().values()) {
                        score += scoreValue(token, enumValueDescriptor.getName(), 45, 25,
                                "enumValue:" + propertyDescriptor.getName(), matchedBy);
                        score += scoreValues(token, enumValueDescriptor.getLocalizedName(), 40, 20,
                                "enumValueCaption:" + propertyDescriptor.getName(), matchedBy);
                    }
                }
            }
        }

        if (score == 0) {
            return null;
        }
        return new DomainModelSearchCandidate(entityDescriptor, score, List.copyOf(matchedBy));
    }

    protected int scoreValues(String token, Collection<String> values, int exactWeight, int containsWeight,
                              String matchLabel, Set<String> matchedBy) {
        int score = 0;
        for (String value : values) {
            score += scoreValue(token, value, exactWeight, containsWeight, matchLabel, matchedBy);
        }
        return score;
    }

    protected int scoreValue(String token, String value, int exactWeight, int containsWeight,
                             String matchLabel, Set<String> matchedBy) {
        if (value == null || value.isBlank()) {
            return 0;
        }

        String normalizedValue = normalize(value);
        if (normalizedValue.isEmpty()) {
            return 0;
        }

        if (normalizedValue.equals(token)) {
            matchedBy.add(matchLabel);
            return exactWeight;
        }

        if (normalizedValue.contains(token)) {
            matchedBy.add(matchLabel);
            return containsWeight;
        }

        return 0;
    }

    protected List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        List<String> tokens = new ArrayList<>();
        for (String token : TOKEN_SPLIT_PATTERN.split(text.toLowerCase(Locale.ROOT))) {
            if (!token.isBlank()) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    protected String normalize(String value) {
        return value.toLowerCase(Locale.ROOT);
    }
}
