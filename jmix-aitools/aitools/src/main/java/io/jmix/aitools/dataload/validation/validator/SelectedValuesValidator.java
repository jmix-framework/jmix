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

import java.util.List;

/**
 * Checks that every result column is a value rather than an entity instance, so the repair loop can rewrite a
 * query that selects an entity whole. An entity column would also carry attributes the current user may not read.
 */
@Component("aitls_SelectedValuesValidator")
public class SelectedValuesValidator implements JpqlResultValidator, Ordered {

    public static final String SELECTED_ENTITY_CODE = "selectedPath.entity";
    public static final String SELECTED_ENTITY_GUIDANCE = "Select the attributes you need, not the entity itself.";

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected QueryTransformerFactory queryTransformerFactory;

    @Override
    public List<JpqlValidationIssue> validate(GeneratedJpqlResult result) {
        QueryParser queryParser = JpqlValidatorSupport.getQueryParser(queryTransformerFactory, result.getJpql());
        if (queryParser == null) {
            return List.of();
        }

        try {
            return JpqlValidatorSupport.selectedEntityExpressions(queryParser, metadata).stream()
                    .map(expression -> new JpqlValidationIssue(SELECTED_ENTITY_CODE,
                            "Selected expression is an entity, not a value: " + expression,
                            SELECTED_ENTITY_GUIDANCE))
                    .toList();
        } catch (RuntimeException e) {
            return List.of();
        }
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 850;
    }
}
