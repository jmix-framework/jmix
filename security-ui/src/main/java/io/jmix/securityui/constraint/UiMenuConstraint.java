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
import io.jmix.ui.WindowConfig;
import io.jmix.ui.context.UiMenuContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component(UiMenuConstraint.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UiMenuConstraint implements AccessConstraint<UiMenuContext> {
    public static final String NAME = "sec_UiMenuConstraint";

    protected UiSecureOperations uiOperations;
    protected UiPolicyStore policyStore;
    protected WindowConfig windowConfig;

    @Autowired
    public void setUiOperations(UiSecureOperations uiOperations) {
        this.uiOperations = uiOperations;
    }

    @Autowired
    public void setPolicyStore(UiPolicyStore policyStore) {
        this.policyStore = policyStore;
    }

    @Autowired
    public void setWindowConfig(WindowConfig windowConfig) {
        this.windowConfig = windowConfig;
    }

    @Override
    public Class<UiMenuContext> getContextType() {
        return UiMenuContext.class;
    }

    @Override
    public void applyTo(UiMenuContext context) {
        if (uiOperations.isMenuItemPermitted(context.getMenuItemId(), policyStore)) {
            if (windowConfig.hasWindow(context.getMenuItemId())
                    && !uiOperations.isScreenPermitted(context.getMenuItemId(), policyStore)) {
                context.setDenied();
            }
        } else {
            context.setDenied();
        }
    }
}
