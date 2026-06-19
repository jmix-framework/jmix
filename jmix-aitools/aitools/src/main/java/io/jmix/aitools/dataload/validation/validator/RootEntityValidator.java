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
import io.jmix.data.QueryParser;
import io.jmix.data.QueryTransformerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Checks that the query's root entity is non-blank and known to the introspected domain model.
 */
@Component("aitls_RootEntityValidator")
public class RootEntityValidator implements JpqlResultValidator, Ordered {

    public static final String ROOT_ENTITY_UNKNOWN_CODE = "rootEntity.unknown";
    public static final String ROOT_ENTITY_UNKNOWN_GUIDANCE = "Use only entity names that are present in the provided" +
            " schema.";

    @Autowired
    protected JpaDomainModelIntrospector modelIntrospector;
    @Autowired(required = false)
    protected QueryTransformerFactory queryTransformerFactory;

    @Override
    public List<JpqlValidationIssue> validate(GeneratedJpqlResult result) {
        QueryParser queryParser = JpqlValidatorSupport.getQueryParser(queryTransformerFactory, result.getJpql());
        if (queryParser == null) {
            return List.of();
        }

        try {
            String rootEntityName = queryParser.getEntityName();
            if (rootEntityName.isBlank()) {
                return List.of(new JpqlValidationIssue("rootEntity.blank", "Root entity name is blank"));
            }

            if (!modelIntrospector.containsEntity(rootEntityName)) {
                return List.of(new JpqlValidationIssue(ROOT_ENTITY_UNKNOWN_CODE,
                        "Unknown root entity: " + rootEntityName, ROOT_ENTITY_UNKNOWN_GUIDANCE));
            }
        } catch (RuntimeException e) {
            return List.of();
        }

        return List.of();
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 800;
    }
}
