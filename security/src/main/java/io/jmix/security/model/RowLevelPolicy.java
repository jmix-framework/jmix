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

import io.jmix.core.Entity;

import java.util.function.Predicate;

/**
 * Defines a constraint that restricts data a user can read or modify. There are two row-level policy types: in-memory
 * and JPQL.
 */
public class RowLevelPolicy {

    public static final String TYPE_IN_MEMORY = "inMemory";
    public static final String TYPE_JPQL = "jpql";

    private String entityName;

    private String action;

    private Predicate<? extends Entity> predicate;

    private String where;

    private String join;

    private String type;

    public RowLevelPolicy(String entityName, String where) {
        this.entityName = entityName;
        this.where = where;
        this.action = RowLevelPolicyAction.READ.getId();
        this.type = TYPE_JPQL;
    }

    public RowLevelPolicy(String entityName, String where, String join) {
        this.entityName = entityName;
        this.where = where;
        this.join = join;
        this.type = TYPE_JPQL;
        this.action = RowLevelPolicyAction.READ.getId();
    }

    public RowLevelPolicy(String entityName, String action, Predicate<? extends Entity> predicate) {
        this.entityName = entityName;
        this.action = action;
        this.predicate = predicate;
        this.type = TYPE_IN_MEMORY;
    }

    /**
     * Returns a name of the associated entity
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * Returns entity CRUD operation. See {@link RowLevelPolicyAction}
     *
     * @return entity CRUD operation
     */
    public String getAction() {
        return action;
    }

    /**
     * Returns a predicate for in-memory row-level policy
     * @return a predicate
     */
    public Predicate<? extends Entity> getPredicate() {
        return predicate;
    }

    /**
     * Returns "where" clause for JPQL policy
     * @return JPQL "where" clause
     */
    public String getWhere() {
        return where;
    }

    /**
     * Returns "join" clause for JPQL policy
     * @return JPQL "joine" clause
     */
    public String getJoin() {
        return join;
    }

    /**
     * Returns row-level policy type. It may be in-memory or JPQL policy
     * @return row-level policy type
     */
    public String getType() {
        return type;
    }
}
