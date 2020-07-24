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

package io.jmix.ui.xml;

import io.jmix.ui.action.ActionType;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.component.ActionsHolder;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.compatibility.CubaFragmentAdapter;
import io.jmix.ui.screen.FrameOwner;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

@ActionType(DeclarativeAction.ID)
public class DeclarativeAction extends BaseAction {

    public static final String ID = "declarative";

    private Frame frame;
    private String methodName;

    public DeclarativeAction(String id) {
        super(id);
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void checkActionsHolder(@Nullable ActionsHolder holder) {
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
}
