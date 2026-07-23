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

package io.jmix.aitools.dataload.execution;

import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.datatype.EnumClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.data.QueryParser;
import io.jmix.data.QueryTransformerFactory;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Replaces enum values in the fetched rows of a {@link JpqlExecutionResult} with their localized
 * captions, so a conversational response shows human-readable labels instead of raw enum ids or
 * constant names.
 */
@Component("aitls_EnumCaptionResultLocalizer")
public class EnumCaptionResultLocalizer {

    private static final Logger log = LoggerFactory.getLogger(EnumCaptionResultLocalizer.class);

    @Autowired
    protected Messages messages;
    @Autowired(required = false)
    protected Metadata metadata;
    @Autowired(required = false)
    protected QueryTransformerFactory queryTransformerFactory;

    /**
     * Returns a copy of the given result with enum values in its rows replaced by their localized
     * captions (resolved for the current user locale). All other fields and non-enum values are
     * preserved.
     *
     * @param result           execution result whose rows may contain enum values
     * @param resultProperties selected column names in select-clause order, used to map columns back
     *                         to their enum type
     * @return a localized copy of the result
     */
    public JpqlExecutionResult localize(JpqlExecutionResult result, List<String> resultProperties) {
        Preconditions.checkNotNullArgument(result, "result is null");

        List<Map<String, Object>> rows = result.getRows();
        if (rows.isEmpty()) {
            return result;
        }

        Map<String, Class<? extends Enum<?>>> enumColumns =
                resolveEnumColumns(result.getGeneratedJpqlResult().getJpql(), resultProperties);

        List<Map<String, Object>> localizedRows = localizeRows(rows, enumColumns);

        return new JpqlExecutionResult(
                result.getGeneratedJpqlResult(),
                result.getValidationResult(),
                localizedRows,
                result.getMaxResults(),
                result.getFirstResult(),
                result.isHasMore(),
                result.isRepaired(),
                result.isExecuted(),
                result.getExecutionError()
        );
    }

    protected Map<String, Class<? extends Enum<?>>> resolveEnumColumns(String jpql, List<String> resultProperties) {
        if (metadata == null || queryTransformerFactory == null || resultProperties.isEmpty()) {
            return Map.of();
        }

        Map<String, Class<? extends Enum<?>>> enumColumns = new LinkedHashMap<>();
        try {
            QueryParser parser = queryTransformerFactory.parser(jpql);
            int index = 0;
            for (QueryParser.QueryPath path : parser.getQueryPaths()) {
                if (!path.isSelectedPath()) {
                    continue;
                }
                if (index < resultProperties.size()) {
                    Class<? extends Enum<?>> enumType = resolveEnumType(path);
                    if (enumType != null) {
                        enumColumns.put(resultProperties.get(index), enumType);
                    }
                }
                index++;
            }
        } catch (RuntimeException e) {
            // Parsing is best-effort: on any failure fall back to formatting only actual enum
            // instances, leaving raw values untouched.
            log.debug("Cannot resolve enum columns for query [{}]", jpql, e);
            return Map.of();
        }
        return enumColumns;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected Class<? extends Enum<?>> resolveEnumType(QueryParser.QueryPath path) {
        MetaPropertyPath propertyPath = metadata.getClass(path.getEntityName()).getPropertyPath(path.getPropertyPath());
        if (propertyPath == null) {
            return null;
        }
        MetaProperty metaProperty = propertyPath.getMetaProperty();
        if (metaProperty.getType() != MetaProperty.Type.ENUM) {
            return null;
        }
        Class<?> javaType = metaProperty.getJavaType();
        if (!javaType.isEnum()) {
            return null;
        }
        return (Class<? extends Enum<?>>) javaType;
    }

    protected List<Map<String, Object>> localizeRows(List<Map<String, Object>> rows,
                                                     Map<String, Class<? extends Enum<?>>> enumColumns) {
        List<Map<String, Object>> localizedRows = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            Map<String, Object> localizedRow = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                localizedRow.put(entry.getKey(), localizeValue(entry.getValue(), enumColumns.get(entry.getKey())));
            }
            localizedRows.add(Map.copyOf(localizedRow));
        }
        return List.copyOf(localizedRows);
    }

    protected Object localizeValue(Object value, @Nullable Class<? extends Enum<?>> enumType) {
        if (value instanceof Enum<?> enumValue) {
            return messages.getMessage(enumValue);
        }
        if (enumType != null) {
            Enum<?> constant = findConstantByStoredValue(enumType, value);
            if (constant != null) {
                return messages.getMessage(constant);
            }
        }
        return value;
    }

    /**
     * Finds the enum constant whose stored representation matches the given value: the {@link EnumClass}
     * id for Jmix enums, otherwise the constant name or ordinal for plain enums.
     *
     * @param enumType    enum type to search
     * @param storedValue raw value fetched for the column
     * @return the matching constant, or {@code null} if none matches
     */
    @Nullable
    protected Enum<?> findConstantByStoredValue(Class<? extends Enum<?>> enumType, Object storedValue) {
        for (Enum<?> constant : enumType.getEnumConstants()) {
            if (constant instanceof EnumClass<?> enumClass) {
                Object id = enumClass.getId();
                if (Objects.equals(id, storedValue)
                        || Objects.equals(String.valueOf(id), String.valueOf(storedValue))) {
                    return constant;
                }
            } else {
                if (constant.name().equals(storedValue)
                        || (storedValue instanceof Number number && number.intValue() == constant.ordinal())) {
                    return constant;
                }
            }
        }
        return null;
    }
}
