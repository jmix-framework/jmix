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

package io.jmix.simplesecurityflowui.access;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterListener;
import com.vaadin.flow.router.NotFoundException;
import io.jmix.core.AccessManager;
import io.jmix.flowui.accesscontext.UiShowViewContext;
import io.jmix.flowui.sys.ViewDescriptorUtils;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewController;
import org.springframework.stereotype.Component;

/**
 * Class is responsible for checking view permissions when a view is being opened.
 */
@Component("simsec_SimpleSecurityViewAccessChecker")
public class SimpleSecurityViewAccessChecker implements BeforeEnterListener {

    private final AccessManager accessManager;

    public SimpleSecurityViewAccessChecker(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        Class<?> viewClass = beforeEnterEvent.getNavigationTarget();
        if (!isViewClassSupported(viewClass)) return;
        String viewId = ViewDescriptorUtils.getInferredViewId(viewClass);
        UiShowViewContext context = new UiShowViewContext(viewId);
        accessManager.applyRegisteredConstraints(context);
        if (context.isPermitted()) {
            return;
        }
        beforeEnterEvent.rerouteToError(NotFoundException.class, "Access denied to view " + viewId);
    }

    protected boolean isViewClassSupported(Class<?> targetView) {
        return View.class.isAssignableFrom(targetView)
                && targetView.getAnnotation(ViewController.class) != null;
    }

}
