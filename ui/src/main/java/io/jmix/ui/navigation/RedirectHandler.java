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

package io.jmix.ui.navigation;

import io.jmix.core.common.util.Preconditions;
import io.jmix.ui.AppUI;
import io.jmix.ui.UiProperties;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.app.navigation.notfoundwindow.NotFoundScreen;
import io.jmix.ui.component.RootWindow;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Component("ui_RedirectHandler")
@Scope("prototype")
public class RedirectHandler {

    private static final Logger log = LoggerFactory.getLogger(RedirectHandler.class);

    protected static final String REDIRECT_PARAM = "redirectTo";

    @Autowired
    protected UiProperties uiProperties;

    @Autowired
    protected WindowConfig windowConfig;

    protected AppUI ui;

    protected NavigationState redirect;

    public RedirectHandler(AppUI ui) {
        this.ui = ui;
    }

    public void schedule(NavigationState redirect) {
        UrlHandlingMode urlHandlingMode = uiProperties.getUrlHandlingMode();
        if (UrlHandlingMode.URL_ROUTES != urlHandlingMode) {
            log.debug("RedirectHandler is disabled for {} URL handling mode", urlHandlingMode);
            return;
        }

        Preconditions.checkNotNullArgument(redirect);

        RouteDefinition notFoundScreenRouteDef = windowConfig.getWindowInfo(NotFoundScreen.ID).getRouteDefinition();
        if (Objects.equals(notFoundScreenRouteDef.getPath(), redirect.getNestedRoute())) {
            return;
        }

        this.redirect = redirect;

        String nestedRoute = redirect.getNestedRoute();
        if (StringUtils.isEmpty(nestedRoute)) {
            return;
        }

        Map<String, String> params = new LinkedHashMap<>();
        params.put(REDIRECT_PARAM, nestedRoute);

        if (redirect.getParams() != null) {
            params.putAll(redirect.getParams());
        }

        RootWindow rootWindow = ui.getTopLevelWindow();
        if (rootWindow != null) {
            ui.getUrlRouting().replaceState(rootWindow.getFrameOwner(), params);
        }
    }

    public boolean scheduled() {
        return redirect != null;
    }

    public void redirect() {
        UrlHandlingMode urlHandlingMode = uiProperties.getUrlHandlingMode();
        if (UrlHandlingMode.URL_ROUTES != urlHandlingMode) {
            log.debug("RedirectHandler is disabled for {} URL handling mode", urlHandlingMode);
            return;
        }

        String nestedRoute = redirect.getNestedRoute();
        Map<String, String> params = redirect.getParams();

        String redirectTarget = null;

        if (StringUtils.isNotEmpty(nestedRoute)) {
            redirectTarget = nestedRoute;
        } else if (MapUtils.isNotEmpty(params) && params.containsKey(REDIRECT_PARAM)) {
            redirectTarget = params.remove(REDIRECT_PARAM);
        }

        if (StringUtils.isEmpty(redirectTarget)) {
            return;
        }

        NavigationState currentState = ui.getUrlRouting().getState();
        NavigationState newState = new NavigationState(currentState.getRoot(), "", redirectTarget, params);

        ui.getUrlChangeHandler().getScreenNavigator()
                .handleScreenNavigation(newState);

        redirect = null;
    }
}
