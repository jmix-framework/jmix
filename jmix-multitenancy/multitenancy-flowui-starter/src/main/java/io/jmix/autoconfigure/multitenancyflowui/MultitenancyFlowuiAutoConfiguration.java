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

package io.jmix.autoconfigure.multitenancyflowui;

import io.jmix.multitenancy.core.TenantProvider;
import io.jmix.multitenancyflowui.MultitenancyFlowuiConfiguration;
import io.jmix.multitenancyflowui.impl.SameTenantUserSubstitutionCandidatePredicate;
import io.jmix.securityflowui.util.UserSubstitutionCandidatePredicate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({MultitenancyFlowuiConfiguration.class})
public class MultitenancyFlowuiAutoConfiguration {

    @Bean("mten_SameTenantUserSubstitutionCandidatePredicate")
    @ConditionalOnClass(name = "io.jmix.securityflowui.util.UserSubstitutionCandidatePredicate")
    public UserSubstitutionCandidatePredicate sameTenantUserSubstitutionCandidatePredicate(TenantProvider tenantProvider) {
        return new SameTenantUserSubstitutionCandidatePredicate(tenantProvider);
    }
}
