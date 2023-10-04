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

package io.jmix.simplesecurityflowui.constraint;

import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import io.jmix.core.constraint.AccessConstraint;
import io.jmix.flowui.accesscontext.UiMenuContext;
import io.jmix.flowui.menu.MenuItem;
import io.jmix.flowui.view.ViewInfo;
import io.jmix.flowui.view.ViewRegistry;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

public class UiMenuConstraint implements AccessConstraint<UiMenuContext> {

    protected ViewRegistry viewRegistry;

    protected AccessAnnotationChecker accessAnnotationChecker;

    public UiMenuConstraint(ViewRegistry viewRegistry, AccessAnnotationChecker accessAnnotationChecker) {
        this.viewRegistry = viewRegistry;
        this.accessAnnotationChecker = accessAnnotationChecker;
    }

    @Override
    public Class<UiMenuContext> getContextType() {
        return UiMenuContext.class;
    }

    @Override
    public void applyTo(UiMenuContext context) {
        MenuItem menuItem = context.getMenuItem();
        ViewInfo viewInfo = viewRegistry.getViewInfo(menuItem.getView());
        if (!accessAnnotationChecker.hasAccess(viewInfo.getControllerClass())) {
            context.setDenied();
        }
    }
}
