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

import java.util.ArrayList;
import java.util.Collection;

/**
 * Class is a container for security row level policies.
 * <p>
 * Row-level policy restrict which data a user can read or modify.
 * <p>
 * Role objects may be created from different sources:
 * <ul>
 *     <li>from interfaces annotated with {@link io.jmix.security.role.annotation.RowLevelRole}</li>
 *     <li>from database Role entities</li>
 *     <li>created explicitly by the application</li>
 * </ul>
 */
public class RowLevelRole extends BaseRole {
    private Collection<RowLevelPolicy> rowLevelPolicies = new ArrayList<>();
    private Collection<RowLevelPolicy> allRowLevelPolicies;

    public Collection<RowLevelPolicy> getRowLevelPolicies() {
        return rowLevelPolicies;
    }

    public void setRowLevelPolicies(Collection<RowLevelPolicy> rowLevelPolicies) {
        this.rowLevelPolicies = rowLevelPolicies;
    }

    public Collection<RowLevelPolicy> getAllRowLevelPolicies() {
        return allRowLevelPolicies == null ? rowLevelPolicies : allRowLevelPolicies;
    }

    public void setAllRowLevelPolicies(Collection<RowLevelPolicy> allRowLevelPolicies) {
        this.allRowLevelPolicies = allRowLevelPolicies;
    }
}
