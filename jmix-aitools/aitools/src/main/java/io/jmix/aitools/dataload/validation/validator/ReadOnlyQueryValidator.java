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

import io.jmix.core.JmixOrder;
import io.jmix.aitools.dataload.execution.GeneratedJpqlResult;
import io.jmix.aitools.dataload.validation.JpqlValidationIssue;
import io.jmix.aitools.dataload.validation.JpqlResultValidator;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static io.jmix.aitools.dataload.validation.validator.JpqlValidatorUtils.containsWord;

/**
 * Checks that the query is a read-only select, rejecting non-select queries and write operations
 * (update, delete, insert).
 */
@Component("aitols_ReadOnlyQueryJpqlValidator")
public class ReadOnlyQueryValidator implements JpqlResultValidator, Ordered {

    public static final String JPQL_NOT_SELECT_CODE = "jpql.notSelect";
    public static final String JPQL_NOT_SELECT_GUIDANCE = "Return a read-only select JPQL query only.";

    public static final String JPQL_WRITE_OPERATION_CODE = "jpql.writeOperation";
    public static final String JPQL_WRITE_OPERATION_GUIDANCE = "Return a read-only select JPQL query only.";

    @Override
    public List<JpqlValidationIssue> validate(GeneratedJpqlResult result) {
        List<JpqlValidationIssue> issues = new ArrayList<>();
        String jpql = result.getJpql();
        if (jpql == null || jpql.isBlank()) {
            return List.of();
        }

        String normalizedJpql = jpql.trim().toLowerCase(Locale.ROOT);
        if (!normalizedJpql.startsWith("select ")) {
            issues.add(new JpqlValidationIssue(JPQL_NOT_SELECT_CODE, "Only read-only select JPQL is supported",
                    JPQL_NOT_SELECT_GUIDANCE));
        }

        if (containsWord(normalizedJpql, "update")
                || containsWord(normalizedJpql, "delete")
                || containsWord(normalizedJpql, "insert")) {
            issues.add(new JpqlValidationIssue(JPQL_WRITE_OPERATION_CODE, "Write JPQL operations are not allowed",
                    JPQL_WRITE_OPERATION_GUIDANCE));
        }
        return issues;
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 200;
    }
}
