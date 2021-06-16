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

package io.jmix.ui.navigation.accessfilter;

import io.jmix.core.JmixOrder;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.app.navigation.notfoundwindow.NotFoundScreen;
import io.jmix.ui.navigation.NavigationFilter;
import io.jmix.ui.navigation.NavigationState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("ui_JmixNotFoundScreenFilter")
@Order(JmixOrder.LOWEST_PRECEDENCE - 10)
public class JmixNotFoundScreenFilter implements NavigationFilter {

    @Autowired
    protected WindowConfig windowConfig;

    @Override
    public AccessCheckResult allowed(NavigationState fromState, NavigationState toState) {
        String notFoundScreenRoute = windowConfig.findRoute(NotFoundScreen.ID);

        return Objects.equals(notFoundScreenRoute, toState.getNestedRoute())
                ? AccessCheckResult.rejected()
                : AccessCheckResult.allowed();
    }
}
