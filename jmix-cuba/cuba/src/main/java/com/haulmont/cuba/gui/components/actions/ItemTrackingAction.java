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
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.gui.components.ListComponent;
import com.haulmont.cuba.security.entity.ConstraintOperationType;
import io.jmix.core.AccessManager;
import io.jmix.core.Metadata;
import io.jmix.core.accesscontext.InMemoryCrudEntityContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.EntityOp;
import io.jmix.ui.action.Action;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nullable;

public class ItemTrackingAction extends ListAction
        implements Action.HasSecurityConstraint {

    protected EntityOp constraintEntityOp;
    protected Security security = AppBeans.get(Security.class);
    protected Metadata metadata = AppBeans.get(Metadata.class);
    protected AccessManager accessManager = AppBeans.get(AccessManager.class);
    protected ApplicationContext applicationContext = AppContext.getApplicationContext();

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
    protected boolean isPermitted() {
        if (target == null) {
            return false;
        }

        Object singleSelected = target.getSingleSelected();
        if (singleSelected == null) {
            return false;
        }

        if (constraintEntityOp != null) {
            MetaClass metaClass = metadata.getClass(singleSelected);
            InMemoryCrudEntityContext context = new InMemoryCrudEntityContext(metaClass, applicationContext);
            accessManager.applyRegisteredConstraints(context);

            if (constraintEntityOp == EntityOp.CREATE) {
                return context.isCreatePermitted(singleSelected);
            } else if (constraintEntityOp == EntityOp.READ) {
                return context.isReadPermitted(singleSelected);
            } else if (constraintEntityOp == EntityOp.UPDATE) {
                return context.isUpdatePermitted(singleSelected);
            } else if (constraintEntityOp == EntityOp.DELETE) {
                return context.isDeletePermitted(singleSelected);
            } else {
                return false;
            }
        }

        return super.isPermitted();
    }

    public void setConstraintOperationType(ConstraintOperationType constraintOperationType) {
        if (constraintOperationType == null) {
            setConstraintEntityOp(null);
        } else {
            setConstraintEntityOp(EntityOp.fromId(constraintOperationType.getId()));
        }
    }

    @Nullable
    public ConstraintOperationType getConstraintOperationType() {
        return constraintEntityOp == null ? null : ConstraintOperationType.fromId(constraintEntityOp.getId());
    }

    @Override
    public void setConstraintEntityOp(@Nullable EntityOp entityOp) {
        this.constraintEntityOp = entityOp;
    }

    @Nullable
    @Override
    public EntityOp getConstraintEntityOp() {
        return constraintEntityOp;
    }
}
