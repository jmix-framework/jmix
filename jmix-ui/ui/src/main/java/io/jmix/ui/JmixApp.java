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
import io.jmix.core.AccessManager;
import io.jmix.core.security.SecurityContextHelper;
import io.jmix.ui.accesscontext.UiShowScreenContext;
import io.jmix.ui.util.OperationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("ui_App")
@VaadinSessionScope
public class JmixApp extends App {

    private static final Logger log = LoggerFactory.getLogger(JmixApp.class);

    @Autowired
    protected AccessManager accessManager;

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

            String initialScreenId = uiProperties.getInitialScreenId();
            if (Strings.isNullOrEmpty(initialScreenId)) {
                return screenId;
            }

            if (!windowConfig.hasWindow(initialScreenId)) {
                log.info("Initial screen '{}' is not found", initialScreenId);
                return screenId;
            }

            UiShowScreenContext context = new UiShowScreenContext(initialScreenId);
            accessManager.applyRegisteredConstraints(context);
            if (!context.isPermitted()) {
                log.info("Initial screen '{}' is not permitted", initialScreenId);
                return screenId;
            }

            return initialScreenId;
        } else {
            return uiProperties.getMainScreenId();
        }
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
