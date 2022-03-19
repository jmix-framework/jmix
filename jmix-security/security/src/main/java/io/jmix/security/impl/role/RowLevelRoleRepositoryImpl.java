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

package io.jmix.security.impl.role;

import io.jmix.security.model.RowLevelPolicy;
import io.jmix.security.model.RowLevelRole;
import io.jmix.security.role.RoleProvider;
import io.jmix.security.role.RowLevelRoleProvider;
import io.jmix.security.role.RowLevelRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;

@Component("sec_RowLevelRoleRepository")
public class RowLevelRoleRepositoryImpl extends BaseRoleRepository<RowLevelRole>
        implements RowLevelRoleRepository {

    private Collection<RowLevelRoleProvider> roleProviders;

    @Autowired
    public void setRoleProviders(Collection<RowLevelRoleProvider> roleProviders) {
        this.roleProviders = roleProviders;
    }

    @Override
    protected Collection<? extends RoleProvider<RowLevelRole>> getRoleProviders() {
        return roleProviders;
    }

    @Override
    protected void mergeChildRoleState(RowLevelRole role, RowLevelRole childRole) {
        Collection<RowLevelPolicy> allPolicies = new ArrayList<>(role.getAllRowLevelPolicies());
        allPolicies.addAll(childRole.getAllRowLevelPolicies());
        role.setAllRowLevelPolicies(allPolicies);
    }
}
