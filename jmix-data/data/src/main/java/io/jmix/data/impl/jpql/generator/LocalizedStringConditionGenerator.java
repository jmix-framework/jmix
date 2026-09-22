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

package io.jmix.data.impl.jpql.generator;

import io.jmix.core.JmixOrder;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.core.querycondition.PropertyConditionUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

/**
 * Generates conditions on localized string properties against the text of the current user's locale instead of
 * the stored string. Joins are inherited; only the where clause and the parameter preparation differ.
 */
@NullMarked
@Component("data_LocalizedStringConditionGenerator")
@Order(JmixOrder.LOWEST_PRECEDENCE - 5)
public class LocalizedStringConditionGenerator extends PropertyConditionGenerator {

    protected static final Set<String> UNSUPPORTED_OPERATIONS = Set.of(
            PropertyCondition.Operation.IN_INTERVAL,
            PropertyCondition.Operation.DATE_EQUALS,
            PropertyCondition.Operation.IS_COLLECTION_EMPTY,
            PropertyCondition.Operation.MEMBER_OF_COLLECTION,
            PropertyCondition.Operation.NOT_MEMBER_OF_COLLECTION);

    protected LocalizedStringJpqlExpressionSupport expressionSupport;

    @Autowired
    public LocalizedStringConditionGenerator(MetadataTools metadataTools, Metadata metadata,
                                             LocalizedStringJpqlExpressionSupport expressionSupport) {
        super(metadataTools, metadata);
        this.expressionSupport = expressionSupport;
    }

    @Override
    public boolean supports(ConditionGenerationContext context) {
        MetaPropertyPath path = resolvePath(context);
        return path != null && expressionSupport.isLocalizedString(path);
    }

    /**
     * Resolves the property path, needed for the store of the column, and the alias the inherited join
     * generation produced, then builds the where clause against the resolved text.
     */
    @Override
    public String generateWhere(ConditionGenerationContext context) {
        PropertyCondition propertyCondition = (PropertyCondition) context.getCondition();
        if (propertyCondition == null) {
            return "";
        }
        MetaPropertyPath path = resolvePath(context);
        Preconditions.checkNotNullArgument(path, "Property path '%s' is not resolved in '%s'",
                propertyCondition.getProperty(), context.getEntityName());

        String joinAlias = context.getJoinAlias();
        String joinProperty = context.getJoinProperty();
        MetaClass joinMetaClass = context.getJoinMetaClass();

        String alias;
        String property;

        if (joinAlias != null && joinProperty != null && joinMetaClass != null) {
            alias = joinAlias;
            property = getProperty(joinProperty, joinMetaClass.getName());
        } else {
            alias = context.getEntityAlias();
            property = getProperty(propertyCondition.getProperty(), context.getEntityName());
        }
        String where = generateLocalizedWhere(propertyCondition, path, alias, property);

        if (context.getCollectionPath() != null) {
            // A path crossing a to-many association is wrapped in a self-contained subquery, the same way the
            // standard generator does it.
            where = String.format("exists (select 1 from %s where %s member of %s and %s)",
                    context.getCollectionFrom(),
                    context.getCollectionAlias(),
                    context.getCollectionPath(),
                    where);

            if (isMatchingEmptyCollection(propertyCondition)) {
                where = String.format("(%s is empty or %s)", context.getCollectionPath(), where);
            }
        }

        return where;
    }

    @Nullable
    protected MetaPropertyPath resolvePath(ConditionGenerationContext context) {
        if (!(context.getCondition() instanceof PropertyCondition condition) || context.getEntityName() == null) {
            return null;
        }

        MetaClass metaClass = metadata.findClass(context.getEntityName());
        return metaClass == null
                ? null
                : metadataTools.resolveMetaPropertyPathOrNull(metaClass, condition.getProperty());
    }

    protected String generateLocalizedWhere(PropertyCondition propertyCondition, MetaPropertyPath path,
                                            String alias, String property) {
        String operation = propertyCondition.getOperation();
        String column = alias + "." + property;
        // Checked before the unary branch, because IS_COLLECTION_EMPTY is a unary operation as well.
        if (UNSUPPORTED_OPERATIONS.contains(operation)) {
            throw new UnsupportedOperationException(String.format(
                    "Operation '%s' is not supported for the localized string property '%s'", operation, property));
        }

        if (PropertyConditionUtils.isUnaryOperation(propertyCondition)) {
            // IS_SET checks the column itself.
            return String.format("%s %s", column, PropertyConditionUtils.getJpqlOperation(propertyCondition));
        }

        boolean caseInsensitive = PropertyConditionUtils.isCaseInsensitiveOperation(propertyCondition);
        // The expression itself is lower-cased for like-operations, see the builder, so no outer lower(...).
        String expression = expressionSupport.buildResolvedValueExpression(path, column,
                expressionSupport.getCurrentLocale(), caseInsensitive);
        String where = String.format("%s %s :%s%s",
                expression,
                PropertyConditionUtils.getJpqlOperation(propertyCondition),
                propertyCondition.getParameterName(),
                getLikeEscapeClause(propertyCondition));

        if (dataProperties.isIncludeNullClauseInNotConditions() && isNegativeComparison(operation)) {
            where = String.format("(%s or %s is null)", where, expression);
        }

        return where;
    }

    @Nullable
    @Override
    public Object generateParameterValue(@Nullable Condition condition, @Nullable Object parameterValue,
                                         @Nullable String entityName) {
        if (condition instanceof PropertyCondition propertyCondition
                && parameterValue instanceof String value
                && PropertyConditionUtils.isCaseInsensitiveOperation(propertyCondition)) {
            // Lower() is applied inside the expression, so no (?i) marker: the value is lower-cased here.
            String lower = value.toLowerCase(Locale.ROOT);

            return switch (propertyCondition.getOperation()) {
                case PropertyCondition.Operation.STARTS_WITH -> lower + "%";
                case PropertyCondition.Operation.ENDS_WITH -> "%" + lower;
                default -> "%" + lower + "%";
            };
        }

        return super.generateParameterValue(condition, parameterValue, entityName);
    }
}
