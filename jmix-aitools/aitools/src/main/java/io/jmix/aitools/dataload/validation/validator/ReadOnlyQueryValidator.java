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

@Component("aitols_ReadOnlyQueryJpqlValidator")
public class ReadOnlyQueryValidator implements JpqlResultValidator, Ordered {

    @Override
    public List<JpqlValidationIssue> validate(GeneratedJpqlResult result) {
        List<JpqlValidationIssue> issues = new ArrayList<>();
        String jpql = result.getJpql();
        if (jpql == null || jpql.isBlank()) {
            return List.of();
        }

        String normalizedJpql = jpql.trim().toLowerCase(Locale.ROOT);
        if (!normalizedJpql.startsWith("select ")) {
            issues.add(new JpqlValidationIssue("jpql.notSelect", "Only read-only select JPQL is supported"));
        }

        if (containsWord(normalizedJpql, "update")
                || containsWord(normalizedJpql, "delete")
                || containsWord(normalizedJpql, "insert")) {
            issues.add(new JpqlValidationIssue("jpql.writeOperation", "Write JPQL operations are not allowed"));
        }
        return issues;
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 200;
    }
}
