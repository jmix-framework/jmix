/*
 * Copyright 2019 Haulmont.
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

package com.haulmont.cuba.gui.components.actions;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Security;
import com.haulmont.cuba.gui.components.ListComponent;
import io.jmix.core.security.ConstraintOperationType;
import io.jmix.ui.action.Action;

import javax.annotation.Nullable;

public class ItemTrackingAction extends ListAction
        implements Action.HasSecurityConstraint {

    protected ConstraintOperationType constraintOperationType;
    protected String constraintCode;

    protected Security security = AppBeans.get(Security.NAME);

    public ItemTrackingAction(String id) {
        super(id);
    }

    public ItemTrackingAction(String id, @Nullable String shortcut) {
        super(id, shortcut);
    }

    public ItemTrackingAction(ListComponent target, String id) {
        super(id, null);

        this.target = target;
    }

    @Override
    public void setConstraintOperationType(ConstraintOperationType constraintOperationType) {
        this.constraintOperationType = constraintOperationType;
    }

    @Override
    public ConstraintOperationType getConstraintOperationType() {
        return constraintOperationType;
    }

    @Override
    public void setConstraintCode(String constraintCode) {
        this.constraintCode = constraintCode;
    }

    @Override
    public String getConstraintCode() {
        return constraintCode;
    }
}
