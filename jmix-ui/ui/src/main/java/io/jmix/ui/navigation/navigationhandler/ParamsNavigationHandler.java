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

package io.jmix.ui.navigation.navigationhandler;

import io.jmix.core.JmixOrder;
import io.jmix.ui.AppUI;
import io.jmix.ui.component.impl.WindowImpl;
import io.jmix.ui.navigation.NavigationHandler;
import io.jmix.ui.navigation.NavigationState;
import io.jmix.ui.navigation.UrlChangeHandler;
import io.jmix.ui.navigation.UrlParamsChangedEvent;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiControllerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component("ui_ParamsNavigationHandler")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Order(JmixOrder.LOWEST_PRECEDENCE - 20)
public class ParamsNavigationHandler implements NavigationHandler {

    private static final Logger log = LoggerFactory.getLogger(ParamsNavigationHandler.class);

    @Override
    public boolean doHandle(NavigationState requestedState, AppUI ui) {
        UrlChangeHandler urlChangeHandler = ui.getUrlChangeHandler();
        if (urlChangeHandler.isEmptyState(requestedState)) {
            return false;
        }

        Screen screen = urlChangeHandler.getActiveScreen();
        if (screen == null) {
            log.debug("Unable to find a screen for state: '{}", requestedState);
            return false;
        }

        Map<String, String> params = requestedState.getParams() != null
                ? requestedState.getParams()
                : Collections.emptyMap();

        WindowImpl window = (WindowImpl) screen.getWindow();
        NavigationState resolvedState = window.getResolvedState();

        if (resolvedState == null
                || params.equals(resolvedState.getParams())) {
            return false;
        }

        NavigationState newState = new NavigationState(
                resolvedState.getRoot(),
                resolvedState.getStateMark(),
                resolvedState.getNestedRoute(),
                params);
        window.setResolvedState(newState);

        UiControllerUtils.fireEvent(screen, UrlParamsChangedEvent.class,
                new UrlParamsChangedEvent(screen, params));

        return true;
    }
}
