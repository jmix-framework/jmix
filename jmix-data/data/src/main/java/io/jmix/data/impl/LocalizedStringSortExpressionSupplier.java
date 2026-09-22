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

package io.jmix.data.impl;

import io.jmix.core.annotation.Internal;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.data.impl.jpql.generator.LocalizedStringJpqlExpressionSupport;
import io.jmix.data.persistence.JpqlSortExpressionSupplier;
import io.jmix.data.persistence.SortExpressionContext;
import io.jmix.core.JmixOrder;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Sorts localized string properties by the text of the current user's locale. Declines everything else,
 * including {@code @Lob} properties, which keep the standard expression.
 * <p>
 * The order matches the one of {@code LocalizedStringConditionGenerator}, so that the two sides of the
 * mechanism are placed by the same rule. An application overrides the expression by declaring a supplier of
 * higher precedence.
 */
@Internal
@Order(JmixOrder.LOWEST_PRECEDENCE - 5)
@Component("data_LocalizedStringSortExpressionSupplier")
public class LocalizedStringSortExpressionSupplier implements JpqlSortExpressionSupplier {

    @Autowired
    protected LocalizedStringJpqlExpressionSupport expressionSupport;

    @Nullable
    @Override
    public String getDatatypeSortExpression(SortExpressionContext context) {
        MetaPropertyPath path = context.metaPropertyPath();
        if (!expressionSupport.isLocalizedString(path)) {
            return null;
        }

        return expressionSupport.buildResolvedValueExpression(path, "{E}." + path, expressionSupport.getCurrentLocale(), false);
    }
}
