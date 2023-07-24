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

package io.jmix.flowui.sys;

import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import io.jmix.core.AccessManager;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.flowui.accesscontext.UiMenuContext;
import io.jmix.flowui.accesscontext.UiShowViewContext;
import io.jmix.flowui.menu.MenuItem;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewInfo;
import io.jmix.flowui.view.ViewRegistry;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Class checks UI access permission.
 */
@Component("flowui_UiAccessChecker")
public class UiAccessChecker {

    protected AccessAnnotationChecker accessAnnotationChecker;
    protected AccessManager accessManager;
    protected ViewRegistry viewRegistry;

    public UiAccessChecker(@Nullable AccessAnnotationChecker accessAnnotationChecker,
                           AccessManager accessManager,
                           ViewRegistry viewRegistry) {
        this.accessAnnotationChecker = accessAnnotationChecker;
        this.accessManager = accessManager;
        this.viewRegistry = viewRegistry;
    }

    /**
     * Firstly {@link AccessAnnotationChecker} checks annotation constraints, if menu item is not permitted then
     * {@link AccessManager} checks granted permissions.
     *
     * @param menuItem menu item to check
     * @return {@code true} if menu item is permitted
     */
    public boolean isMenuPermitted(MenuItem menuItem) {
        Class<? extends View<?>> controllerClass = getControllerClass(menuItem);
        boolean hasAccess = accessAnnotationChecker != null && accessAnnotationChecker.hasAccess(controllerClass);

        return hasAccess || isMenuItemHasSecurityPermission(menuItem);
    }

    /**
     * Firstly {@link AccessAnnotationChecker} checks annotation constraints, if view is not permitted then
     * {@link AccessManager} checks granted permissions.
     *
     * @param target class to check
     * @return {@code true} if view is permitted
     */
    public boolean isViewPermitted(Class<?> target) {
        boolean hasAccess = accessAnnotationChecker != null && accessAnnotationChecker.hasAccess(target);

        if (!hasAccess && isSupportedView(target)) {
            hasAccess = isViewHasSecurityPermission(target);
        }

        return hasAccess;
    }

    /**
     * Checks for access to the view.
     * Throws exception if no access.
     *
     * @param target class to check
     * @throws AccessDeniedException if the user does not have access to the view
     */
    public void checkViewPermitted(Class<?> target) {
        if (!isViewPermitted(target)) {
            throw new AccessDeniedException("view", target.getName());
        }
    }

    protected Class<? extends View<?>> getControllerClass(MenuItem menuItem) {
        ViewInfo viewInfo = viewRegistry.getViewInfo(menuItem.getView());
        return viewInfo.getControllerClass();
    }

    protected boolean isSupportedView(Class<?> targetView) {
        return View.class.isAssignableFrom(targetView)
                && targetView.getAnnotation(ViewController.class) != null;
    }

    protected boolean isViewHasSecurityPermission(Class<?> target) {
        String viewId = ViewDescriptorUtils.getInferredViewId(target);
        UiShowViewContext context = new UiShowViewContext(viewId);
        accessManager.applyRegisteredConstraints(context);
        return context.isPermitted();
    }

    protected boolean isMenuItemHasSecurityPermission(MenuItem menuItem) {
        UiMenuContext menuItemContext = new UiMenuContext(menuItem);
        accessManager.applyRegisteredConstraints(menuItemContext);
        return menuItemContext.isPermitted();
    }
}
