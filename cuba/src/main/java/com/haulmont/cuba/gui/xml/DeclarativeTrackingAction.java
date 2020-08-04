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

import io.jmix.core.JmixEntity;
import io.jmix.core.security.ConstraintOperationType;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.ListAction;
import io.jmix.ui.component.ActionsHolder;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.compatibility.CubaFragmentAdapter;
import io.jmix.ui.screen.FrameOwner;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

//TODO: access manager
public class DeclarativeTrackingAction extends ListAction implements Action.HasTarget, Action.SecuredAction,
        Action.HasSecurityConstraint {

    protected Frame frame;
    protected String methodName;

    protected ConstraintOperationType constraintOperationType;
    protected String constraintCode;

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
        if (controller instanceof CubaFragmentAdapter) {
            controller = ((CubaFragmentAdapter) controller).getRealScreen();
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

        JmixEntity singleSelected = target.getSingleSelected();
        if (singleSelected == null) {
            return false;
        }

        //TODO: access manager

//
//        if (constraintOperationType != null) {
//            boolean isPermitted;
//            if (constraintCode != null) {
//                isPermitted = security.isPermitted(singleSelected, constraintCode);
//            } else {
//                isPermitted = security.isPermitted(singleSelected, constraintOperationType);
//            }
//            if (!isPermitted) {
//                return false;
//            }
//        }

        return super.isPermitted();
    }

    @Nullable
    @Override
    public ConstraintOperationType getConstraintOperationType() {
        return constraintOperationType;
    }

    @Override
    public void setConstraintOperationType(@Nullable ConstraintOperationType constraintOperationType) {
        this.constraintOperationType = constraintOperationType;
    }

    @Nullable
    @Override
    public String getConstraintCode() {
        return constraintCode;
    }

    @Override
    public void setConstraintCode(@Nullable String constraintCode) {
        this.constraintCode = constraintCode;
    }
}
