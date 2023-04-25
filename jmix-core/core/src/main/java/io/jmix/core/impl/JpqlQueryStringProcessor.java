/*
 * Copyright 2019 Haulmont.
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

package io.jmix.core.impl;

import com.google.common.base.Strings;
import io.jmix.core.JmixOrder;
import io.jmix.core.Metadata;
import io.jmix.core.QueryStringProcessor;
import io.jmix.core.metamodel.model.MetaClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link QueryStringProcessor} implementation that provides support for abbreviated JPQL queries.
 */
@Component("core_JpqlQueryStringProcessor")
@Order(JmixOrder.HIGHEST_PRECEDENCE + 100)
public class JpqlQueryStringProcessor implements QueryStringProcessor {

    public static final Pattern START_PATTERN = Pattern.compile("^(\\w+)\\s");

    @Autowired
    private Metadata metadata;

    public String process(String queryString, Class<?> entityClass) {
        if (Strings.isNullOrEmpty(queryString)) {
            return queryString;
        }
        if (entityClass.getAnnotation(jakarta.persistence.Entity.class) != null) {
            return processJpaQuery(queryString, entityClass);
        } else {
            return queryString;
        }
    }

    protected String processJpaQuery(String queryString, Class<?> entityClass) {
        MetaClass metaClass = metadata.getClass(entityClass);
        String entityName = metaClass.getName();

        String query = queryString.trim();
        Matcher startMatcher = START_PATTERN.matcher(query);
        if (startMatcher.find()) {
            String startToken = startMatcher.group(1);
            // select, from, where, order by
            if ("select".equalsIgnoreCase(startToken)) {
                return query;
            }
            if ("from".equalsIgnoreCase(startToken)) {
                Pattern entityPattern = Pattern.compile(entityName.replace("$", "\\$") + "\\s+(\\w+)");
                Matcher entityMatcher = entityPattern.matcher(query);
                if (entityMatcher.find()) {
                    String alias = entityMatcher.group(1);
                    return "select " + alias + " " + query;
                } else {
                    throw new RuntimeException(String.format(
                            "Cannot find alias for entity %s in query '%s'", entityName, query));
                }
            }
            if ("where".equalsIgnoreCase(startToken) || "order".equalsIgnoreCase(startToken)) {
                return "select e from " + entityName + " e " + query;
            }
        } else {
            // property condition
            return "select e from " + entityName + " e where " + query;
        }
        throw new RuntimeException(String.format(
                "Unable to process query '%s'.\n" +
                        "Query string must start from 'select', 'from', 'where', 'order by', or be a property condition like 'e.property = :param'.",
                query));
    }
}
