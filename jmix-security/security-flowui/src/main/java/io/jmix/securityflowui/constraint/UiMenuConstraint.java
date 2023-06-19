/*
 * Copyright 2022 Haulmont.
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

package io.jmix.securityflowui.constraint;

import io.jmix.core.constraint.AccessConstraint;
import io.jmix.flowui.accesscontext.UiMenuContext;
import io.jmix.flowui.menu.MenuItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("sec_UiMenuConstraint")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UiMenuConstraint implements AccessConstraint<UiMenuContext> {

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
    public Class<UiMenuContext> getContextType() {
        return UiMenuContext.class;
    }

    @Override
    public void applyTo(UiMenuContext context) {
        MenuItem menuItem = context.getMenuItem();
        if (!uiOperations.isMenuItemPermitted(menuItem.getId(), policyStore)) {
            if (!hasPermittedChild(menuItem)) {
                context.setDenied();
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    protected boolean hasPermittedChild(MenuItem menuItem) {
        if (menuItem.getChildren() != null && !menuItem.getChildren().isEmpty()) {
            for (MenuItem child : menuItem.getChildren()) {
                if (uiOperations.isMenuItemPermitted(child.getId(), policyStore)) {
                    return true;
                } else {
                    if (hasPermittedChild(child)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
