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

package io.jmix.ui;

import com.google.common.base.Strings;
import com.vaadin.spring.annotation.VaadinSessionScope;
import io.jmix.core.security.SecurityContextHelper;
import io.jmix.ui.util.OperationResult;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("ui_App")
@VaadinSessionScope
public class JmixApp extends App {

    @Override
    public void loginOnStart() {
        initializeUi();
    }

    @Override
    protected String routeTopLevelWindowId() {
        if (isAnonymousAuthentication()) {
            String screenId = uiProperties.getLoginScreenId();
            if (!windowConfig.hasWindow(screenId)) {
                screenId = uiProperties.getMainScreenId();
            }
            return screenId;
        } else {
            return uiProperties.getMainScreenId();
        }
    }

    @Override
    public OperationResult logout() {
        closeWindowsInternal(true);
        clearSettingsCache();

        String contextPath = servletContext == null ? null : servletContext.getContextPath();
        String logoutPath = Strings.isNullOrEmpty(contextPath) ? "/logout" : contextPath + "/logout";

        AppUI.getCurrent().getPage().setLocation(logoutPath);

        return OperationResult.success();
    }

    protected void initializeUi() {
        AppUI currentUi = AppUI.getCurrent();
        if (currentUi != null) {
            createTopLevelWindow(currentUi);
        }
    }

    private boolean isAnonymousAuthentication() {
        Authentication authentication = SecurityContextHelper.getAuthentication();
        return authentication == null ||
                authentication instanceof AnonymousAuthenticationToken;
    }
}
