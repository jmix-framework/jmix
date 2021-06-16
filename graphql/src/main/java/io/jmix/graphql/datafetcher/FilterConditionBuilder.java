package io.jmix.graphql.datafetcher;

import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.graphql.schema.Types;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static io.jmix.graphql.schema.Types.FilterOperation.*;

@Component("gql_FilterConditionBuilder")
public class FilterConditionBuilder {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public List<Condition> buildCollectionOfConditions(String path, Collection<Map<String, Object>> filter) {
        log.debug("buildCollectionOfConditions: for path {}", path);
        List<Condition> result = new ArrayList<>();

        filter.forEach(condItem -> {

            if (condItem.isEmpty()) return;

            // todo need check - used only first element of map
            Map.Entry<String, Object> condEntry = condItem.entrySet().iterator().next();
            String condPath = condEntry.getKey();
            Object condition = condEntry.getValue();
            Types.ConditionUnionType conditionUnionType = Types.ConditionUnionType.find(condPath);
            if (conditionUnionType != null) {
                // aggregate nested conditions
                List<Condition> conditions = buildCollectionOfConditions(path, (Collection<Map<String, Object>>) condition);
                switch (conditionUnionType) {
                    case AND:
                        result.add(LogicalCondition.and(conditions.toArray(new Condition[0])));
                        return;
                    case OR:
                        result.add(LogicalCondition.or(conditions.toArray(new Condition[0])));
                        return;
                }
            }

            String newPath = StringUtils.isEmpty(path) ? condPath : path + "." + condPath;
            if (Collection.class.isAssignableFrom(condition.getClass())
                    && !IN_LIST.getId().equals(condPath)
                    && !NOT_IN_LIST.getId().equals(condPath)) {
                result.addAll(buildCollectionOfConditions(newPath, (Collection<Map<String, Object>>) condition));
                return;
            }

            //ignore value from request because of specific filters
            if (IS_NULL.getId().equals(condPath) && condition instanceof Boolean) {
                condition = BooleanUtils.negate((Boolean) condition);
            }
            log.debug("buildPropertyCondition: {} {} {}", path, condPath, condition);
            Types.FilterOperation operation = Types.FilterOperation.fromId(condPath);
            result.add(PropertyCondition.createWithValue(path, operation.getJmixOperation(), condition));
        });
        return result;
    }

}
