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

package io.jmix.securityui.constraint;

import io.jmix.core.constraint.AccessConstraint;
import io.jmix.ui.accesscontext.UiShowScreenContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("sec_UiShowScreenConstraint")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UiShowScreenConstraint implements AccessConstraint<UiShowScreenContext> {

    protected UiSecureOperations uiOperations;
    protected UiPolicyStore policyStore;

    @Autowired
    public void setUiOperations(UiSecureOperations uiOperations) {
        this.uiOperations = uiOperations;
    }

    @Autowired
    public void setPolicyStore(UiPolicyStore policyStore) {
        this.policyStore = policyStore;
    }

    @Override
    public Class<UiShowScreenContext> getContextType() {
        return UiShowScreenContext.class;
    }

    @Override
    public void applyTo(UiShowScreenContext context) {
        if (!uiOperations.isScreenPermitted(context.getScreenId(), policyStore)) {
            context.setDenied();
        }
    }
}
