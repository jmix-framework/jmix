package io.jmix.graphql.datafetcher;

import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.graphql.schema.FilterTypesBuilder;
import io.jmix.graphql.schema.Types;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class FilterConditionBuilder {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * build condition composed with 'AND' operator from graphql conditions stored in Collection
     * @param graphqlConditions collection of graphql format conditions
     * @return LogicalCondition that compose all entries with AND
     */
    public LogicalCondition buildCollectionOfConditions(String path, Collection<?> graphqlConditions) {
        log.debug("buildCollectionOfConditions: for path '{}'", path);

        List<Condition> andConditions = new ArrayList<>();
        List<Condition> orConditions = new ArrayList<>();

        graphqlConditions.forEach(filterCond -> {

            if (filterCond == null || !Map.class.isAssignableFrom(filterCond.getClass())) {
                throw new UnsupportedOperationException(String.format(
                        "expected class of graphql filter condition object is java.util.Map, but actual is %s",
                        filterCond == null ? null : filterCond.getClass()));
            }

            ((Map<String, Object>) filterCond).forEach((key, value) -> {
                FilterTypesBuilder.ConditionUnionType ct = FilterTypesBuilder.ConditionUnionType.find(key);

                if (ct != null) {
                    // step into union condition
                    Map<String, Object> condition = ((Iterable<Map<String, Object>>) value).iterator().next();
                    Map.Entry<String, Object> conditionEntry = condition.entrySet().iterator().next();
                    key = conditionEntry.getKey();
                    value = conditionEntry.getValue();

                    switch (ct) {
                        case AND:
                            andConditions.add(buildPropertyCondition(path, key, value));
                            break;
                        case OR:
                            orConditions.add(buildPropertyCondition(path, key, value));
                            break;
//                        case NOT:
//                            break;
                    }
                } else {
                    andConditions.add(buildPropertyCondition(path, key, value));
                }
            });
        });

        return LogicalCondition.or(orConditions.toArray(new Condition[0]))
                .add(LogicalCondition.and(andConditions.toArray(new Condition[0])));
    }

    protected Condition buildPropertyCondition(String path, String key, Object value) {
        Class<?> valueClass = value.getClass();

        String newPath = StringUtils.isBlank(path) ? key : path + "." + key;
        if (Collection.class.isAssignableFrom(valueClass)) {
            return buildCollectionOfConditions(newPath, (Collection<?>) value);
        }

        // todo looks like this never call
        if (Map.class.isAssignableFrom(valueClass)) {
            // todo need to check and support
            throw new IllegalStateException("can't create condition for valueClass " + valueClass);
        }

        log.debug("buildPropertyCondition: {} {} {}", path, key, value);
        Types.FilterOperation operation = Types.FilterOperation.valueOf(key);
        return PropertyCondition.createWithValue(path, operation.getJmixOperation(), value);
    }

}
