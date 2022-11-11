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

package com.haulmont.cuba.gui.xml;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.gui.components.compatibility.LegacyFragmentAdapter;
import com.haulmont.cuba.security.entity.ConstraintOperationType;
import io.jmix.core.AccessManager;
import io.jmix.core.Metadata;
import io.jmix.core.accesscontext.InMemoryCrudEntityContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.EntityOp;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.ListAction;
import io.jmix.ui.component.ActionsHolder;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Frame;
import io.jmix.ui.screen.FrameOwner;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

//TODO: access manager
public class DeclarativeTrackingAction extends ListAction implements Action.HasTarget, Action.SecuredAction,
        Action.HasSecurityConstraint {

    protected Frame frame;
    protected String methodName;
    protected EntityOp constraintEntityOp;
    protected Metadata metadata = AppBeans.get(Metadata.class);
    protected AccessManager accessManager = AppBeans.get(AccessManager.class);
    protected ApplicationContext applicationContext = AppContext.getApplicationContext();

    public DeclarativeTrackingAction(String id, String caption, String description, String icon, @Nullable String enable,
                                     @Nullable String visible, String methodName, @Nullable String shortcut, ActionsHolder holder) {
        super(id, shortcut);
        this.caption = caption;
        this.description = description;
        this.icon = icon;

        setEnabled(enable == null || Boolean.parseBoolean(enable));
        setVisible(visible == null || Boolean.parseBoolean(visible));

        this.methodName = methodName;
        checkActionsHolder(holder);
    }

    protected void checkActionsHolder(ActionsHolder holder) {
        if (holder instanceof Frame) {
            frame = (Frame) holder;
        } else if (holder instanceof Component.BelongToFrame) {
            frame = ((Component.BelongToFrame) holder).getFrame();
        } else {
            throw new IllegalStateException(String.format("Component %s can't contain DeclarativeAction", holder));
        }
    }

    @Override
    public void actionPerform(Component component) {
        if (StringUtils.isEmpty(methodName)) {
            return;
        }

        FrameOwner controller = frame.getFrameOwner();
        if (controller instanceof LegacyFragmentAdapter) {
            controller = ((LegacyFragmentAdapter) controller).getRealScreen();
        }

        Method method;
        try {
            method = controller.getClass().getMethod(methodName, Component.class);
        } catch (NoSuchMethodException e) {
            try {
                method = controller.getClass().getMethod(methodName);
            } catch (NoSuchMethodException e1) {
                throw new IllegalStateException(String.format("No suitable methods named %s for action %s", methodName, id));
            }
        }

        try {
            if (method.getParameterCount() == 1) {
                method.invoke(controller, component);
            } else {
                method.invoke(controller);
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception on action handling", e);
        }
    }

    @Nullable
    @Override
    public String getCaption() {
        return caption;
    }

    @Override
    protected boolean isApplicable() {
        return target != null && !target.getSelected().isEmpty();
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
