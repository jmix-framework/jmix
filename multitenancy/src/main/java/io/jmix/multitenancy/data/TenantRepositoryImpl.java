/*
 * Copyright 2021 Haulmont.
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

package io.jmix.multitenancy.data;

import io.jmix.core.UnconstrainedDataManager;
import io.jmix.multitenancy.entity.Tenant;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;

@Component("mten_TenantRepository")
public class TenantRepositoryImpl implements TenantRepository {

    private final UnconstrainedDataManager dataManager;

    public TenantRepositoryImpl(UnconstrainedDataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Nullable
    @Override
    public Tenant findByTenantId(String tenantId) {
        return dataManager.load(Tenant.class)
                .query("select t from mten_Tenant t where t.tenantId = :tenantId")
                .parameter("tenantId", tenantId)
                .optional()
                .orElse(null);
    }

    @Override
    public List<Tenant> findAll() {
        return dataManager.load(Tenant.class)
                .query("select t from mten_Tenant t")
                .list();
    }
}
