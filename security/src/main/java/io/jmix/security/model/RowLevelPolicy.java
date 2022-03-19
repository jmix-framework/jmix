/*
 * Copyright 2020 Haulmont.
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

package io.jmix.security.model;

import org.springframework.context.ApplicationContext;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines a constraint that restricts data a user can read or modify. There are two row-level policy types: in-memory
 * and JPQL.
 */
public class RowLevelPolicy implements Serializable {
    private static final long serialVersionUID = -2332931381828513664L;

    private String entityName;

    private RowLevelPolicyAction action;

    private RowLevelBiPredicate<Object, ApplicationContext> biPredicate;

    private String whereClause;

    private String joinClause;

    private String script;

    private RowLevelPolicyType type;

    private Map<String, String> customProperties = new HashMap<>();

    public RowLevelPolicy(String entityName, String whereClause, @Nullable String joinClause) {
        this(entityName, whereClause, joinClause, Collections.emptyMap());
    }

    public RowLevelPolicy(String entityName, String whereClause, @Nullable String joinClause,
                          Map<String, String> customProperties) {
        this.entityName = entityName;
        this.whereClause = whereClause;
        this.joinClause = joinClause;
        this.type = RowLevelPolicyType.JPQL;
        this.action = RowLevelPolicyAction.READ;
        this.customProperties = customProperties;
    }

    public RowLevelPolicy(String entityName, RowLevelPolicyAction action, String script, RowLevelBiPredicate<Object, ApplicationContext> biPredicate,
                          Map<String, String> customProperties) {
        this.entityName = entityName;
        this.action = action;
        this.script = script;
        this.biPredicate = biPredicate;
        this.type = RowLevelPolicyType.PREDICATE;
        this.customProperties = customProperties;
    }

    public RowLevelPolicy(String entityName, RowLevelPolicyAction action, RowLevelBiPredicate<Object, ApplicationContext> biPredicate,
                          Map<String, String> customProperties) {
        this.entityName = entityName;
        this.action = action;
        this.biPredicate = biPredicate;
        this.type = RowLevelPolicyType.PREDICATE;
        this.customProperties = customProperties;
    }

    /**
     * Returns a name of the associated entity
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * Returns entity CRUD operation
     *
     * @return entity CRUD operation
     */
    public RowLevelPolicyAction getAction() {
        return action;
    }

    /**
     * Returns a predicate for in-memory row-level policy
     *
     * @return a predicate
     */
    @Nullable
    public RowLevelBiPredicate<Object, ApplicationContext> getBiPredicate() {
        return biPredicate;
    }

    /**
     * Returns a script for predicate for in-memory row-level policy
     *
     * @return a script
     */
    @Nullable
    public String getScript() {
        return script;
    }

    /**
     * Returns "where" clause for JPQL policy
     *
     * @return JPQL "where" clause
     */
    public String getWhereClause() {
        return whereClause;
    }

    /**
     * Returns "join" clause for JPQL policy
     *
     * @return JPQL "join" clause
     */
    public String getJoinClause() {
        return joinClause;
    }

    /**
     * Returns row-level policy type. It may be in-memory predicate or JPQL policy
     *
     * @return row-level policy type
     */
    public RowLevelPolicyType getType() {
        return type;
    }

    public Map<String, String> getCustomProperties() {
        return customProperties;
    }
}
