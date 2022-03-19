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
package com.haulmont.cuba.core.global.filter;

import com.haulmont.cuba.core.global.TemplateHelper;
import io.jmix.data.QueryTransformer;
import io.jmix.data.QueryTransformerFactory;
import io.jmix.core.querycondition.JpqlCondition;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component("cuba_QueryFilter")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class QueryFilter extends FilterParser implements Serializable {

    protected static final boolean ENABLE_SESSION_PARAMS = true;

    @Autowired
    protected QueryTransformerFactory queryTransformerFactory;

    @Autowired
    protected FilterJpqlGenerator filterJpqlGenerator;

    protected QueryFilter(Condition condition) {
        super(condition);
    }

    protected QueryFilter(Element element) {
        super(element);
    }

    public static QueryFilter merge(@Nullable QueryFilter src1, @Nullable QueryFilter src2) {
        if (src1 == null || src2 == null)
            throw new IllegalArgumentException("Source query filter is null");

        Condition root = new LogicalCondition("root", LogicalOp.AND);
        root.getConditions().add(src1.getRoot());
        root.getConditions().add(src2.getRoot());

        return new QueryFilter(root);
    }

    public Collection<ParameterInfo> getCompiledParameters() {
        return root.getCompiledParameters();
    }

    public String processQuery(String query, Map<String, Object> paramValues) {
        Set<String> params = new HashSet<>();
        for (Map.Entry<String, Object> entry : paramValues.entrySet()) {
            if (paramValueIsOk(entry.getValue()))
                params.add(entry.getKey());
        }

        query = TemplateHelper.processTemplate(query, paramValues);

        if (isActual(root, params)) {
            Condition refined = refine(root, params);
            if (refined != null) {
                QueryTransformer transformer = queryTransformerFactory.transformer(query);
                String where = filterJpqlGenerator.generateJpql(refined);

                if (!StringUtils.isBlank(where)) {
                    Set<String> joins = refined.getJoins();
                    if (!joins.isEmpty()) {
                        joins.forEach(transformer::addJoin);
                    }
                    transformer.addWhere(where);
                }
                return transformer.getResult();
            }
        }
        return query;
    }

    protected boolean paramValueIsOk(@Nullable Object value) {
        if (value instanceof String)
            return !StringUtils.isBlank((String) value);
        else return value != null;
    }

    @Nullable
    protected Condition refine(Condition src, Set<String> params) {
        Condition copy = src.copy();
        List<Condition> list = new ArrayList<>();
        for (Condition condition : src.getConditions()) {
            if (isActual(condition, params)) {
                Condition refined = refine(condition, params);
                if (refined != null && !(refined instanceof LogicalCondition && refined.getConditions().isEmpty()))
                    list.add(refined);
            }
        }
        if (copy instanceof LogicalCondition && list.isEmpty()) {
            return null;
        }
        copy.setConditions(list.isEmpty() ? Collections.EMPTY_LIST : list);
        return copy;
    }

    protected boolean isActual(Condition condition, Set<String> params) {
        Set<ParameterInfo> declaredParams = condition.getCompiledParameters();

        if (declaredParams.isEmpty())
            return true;
        if (ENABLE_SESSION_PARAMS) {
            Predicate<ParameterInfo> paramHasValue = paramInfo -> params.contains(paramInfo.getName());
            if (condition.getConditions().isEmpty()) {
                // for leaf condition all parameters must have values
                return declaredParams.stream().allMatch(paramHasValue);
            } else {
                // for branch conditions at least some parameters must have values
                return declaredParams.stream().anyMatch(paramHasValue);
            }
        } else {
            // Return true only if declared params have values and there is at least one non-session parameter among them.
            // This is necessary to exclude generic filter conditions that contain only session parameters. Otherwise
            // there is no way to handle exclusion. Unfortunately this imposes the restriction on custom filters design:
            // condition with session-only parameters must be avoided, they must be coded as part of main query body or as
            // part of another condition.
            boolean found = false;
            for (ParameterInfo paramInfo : declaredParams) {
                if (params.contains(paramInfo.getName())) {
                    found = found || !paramInfo.getType().equals(ParameterInfo.Type.SESSION);
                }
            }
            return found;
        }
    }

    @Nullable
    public io.jmix.core.querycondition.Condition toQueryCondition(Set<String> parameters) {
        Condition condition = actualizeForQueryConditions(root, parameters);
        return condition == null ? null : createQueryCondition(condition);
    }

    public Set<String> getActualizedQueryParameterNames(Set<String> parameters) {
        Condition condition = actualizeForQueryConditions(root, parameters);
        return condition == null ? Collections.emptySet() : condition.getQueryParameters().stream()
                .map(ParameterInfo::getName)
                .collect(Collectors.toSet());
    }

    @Nullable
    protected Condition actualizeForQueryConditions(Condition src, Set<String> parameters) {
        Condition copy = src.copy();
        List<Condition> list = new ArrayList<>();
        for (Condition condition : src.getConditions()) {
            if (condition instanceof Clause) {
                Clause clause = (Clause) condition;
                if (clause.getType() == ConditionType.CUSTOM ||
                        clause.getType() == ConditionType.PROPERTY && clause.getOperator().isUnary()) {
                    Predicate<ParameterInfo> paramHasValue = paramInfo -> parameters.contains(paramInfo.getName())
                            || parameters.contains(paramInfo.getPath().replace(".", "_"));
                    if (clause.getInputParameters().stream().allMatch(paramHasValue)) {
                        list.add(clause);
                    }
                } else {
                    list.add(clause);
                }
            } else {
                list.add(actualizeForQueryConditions(condition, parameters));
            }
        }
        if (copy instanceof LogicalCondition && list.isEmpty()) {
            return null;
        }
        copy.setConditions(list.isEmpty() ? Collections.emptyList() : list);
        return copy;
    }

    protected io.jmix.core.querycondition.Condition createQueryCondition(Condition condition) {
        io.jmix.core.querycondition.Condition result;
        if (condition instanceof LogicalCondition) {
            LogicalCondition logicalCondition = (LogicalCondition) condition;
            if (logicalCondition.getOperation() == LogicalOp.AND) {
                result = new io.jmix.core.querycondition.LogicalCondition(io.jmix.core.querycondition.LogicalCondition.Type.AND);
            } else if (logicalCondition.getOperation() == LogicalOp.OR) {
                result = new io.jmix.core.querycondition.LogicalCondition(io.jmix.core.querycondition.LogicalCondition.Type.OR);
            } else {
                throw new UnsupportedOperationException("Operation is not supported: " + logicalCondition.getOperation());
            }
            for (Condition nestedCondition : logicalCondition.getConditions()) {
                ((io.jmix.core.querycondition.LogicalCondition) result).add(createQueryCondition(nestedCondition));
            }
        } else if (condition instanceof Clause) {
            Clause clause = (Clause) condition;
            result = JpqlCondition.create(clause.getContent(),
                    clause.getJoins().isEmpty() ? null : clause.getJoins().iterator().next());
        } else {
            throw new UnsupportedOperationException("Condition is not supported: " + condition);
        }
        return result;
    }
}
