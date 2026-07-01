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
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Checks that no {@code AS} alias in the query uses a reserved word. EclipseLink rejects reserved
 * words used as identification or result variables at parse time, so such an alias fails at
 * execution even though it passes the more lenient parser behind {@link JpqlSyntaxValidator}.
 * <p>
 * The checked words mirror the reserved-word list documented in the {@code aitls_executeQuery} tool
 * description; the two must be kept in sync.
 */
@Component("aitls_ReservedWordAliasValidator")
public class ReservedWordAliasValidator implements JpqlResultValidator, Ordered {

    public static final String RESERVED_ALIAS_CODE = "jpql.reservedAlias";
    public static final String RESERVED_ALIAS_GUIDANCE = "Do not use JPQL reserved words as AS aliases. Rename each" +
            " flagged alias to a non-reserved word (for example, use entityId instead of id) and keep" +
            " resultProperties in sync with the new aliases.";

    /**
     * Reserved words that must not be used as aliases. Mirrors the list documented in the
     * {@code aitls_executeQuery} tool description.
     */
    protected static final Set<String> RESERVED_ALIASES = Set.of(
            "id", "position", "user", "order", "table", "group", "where", "select", "from", "join",
            "left", "right", "inner", "outer", "on", "and", "or", "not", "in", "exists", "between", "like",
            "is", "null", "true", "false", "count", "sum", "avg", "max", "min", "distinct", "all", "any",
            "some", "union", "except", "intersect", "case", "when", "then", "else", "end", "new", "constructor",
            "size", "index", "key", "value", "entry", "type", "treat", "current_date", "current_time",
            "current_timestamp", "local", "date", "time", "timestamp", "year", "month", "day", "hour", "minute",
            "second");

    @Override
    public List<JpqlValidationIssue> validate(GeneratedJpqlResult result) {
        String jpql = result.getJpql();
        if (jpql == null || jpql.isBlank()) {
            return List.of();
        }

        List<JpqlValidationIssue> issues = new ArrayList<>();
        Set<String> reported = new LinkedHashSet<>();
        for (String alias : JpqlValidatorSupport.extractAliases(jpql)) {
            String normalized = alias.toLowerCase(Locale.ROOT);
            if (RESERVED_ALIASES.contains(normalized) && reported.add(normalized)) {
                issues.add(new JpqlValidationIssue(RESERVED_ALIAS_CODE,
                        "Reserved word used as alias: " + alias, RESERVED_ALIAS_GUIDANCE));
            }
        }
        return issues;
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 800;
    }
}
