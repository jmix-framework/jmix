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

package io.jmix.securityflowui.model;

import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import java.util.Collection;

/**
 * Non-persistent entity used to display roles in UI
 */
@JmixEntity(name = "sec_RowLevelRoleModel")
public class RowLevelRoleModel extends BaseRoleModel {

    @Composition
    @JmixProperty
    private Collection<RowLevelPolicyModel> rowLevelPolicies;

    public Collection<RowLevelPolicyModel> getRowLevelPolicies() {
        return rowLevelPolicies;
    }

    public void setRowLevelPolicies(Collection<RowLevelPolicyModel> rowLevelPolicies) {
        this.rowLevelPolicies = rowLevelPolicies;
    }
}
