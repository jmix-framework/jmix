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

package io.jmix.flowui.view.navigation;

import com.vaadin.flow.router.RouteParameters;

import java.util.HashMap;

public final class NavigationUtils {

    private NavigationUtils() {
    }

    public static RouteParameters generateRouteParameters(ViewNavigator navigator, String param, String value) {
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put(param, value);

        if (navigator.getRouteParameters().isPresent()) {
            RouteParameters routeParameters = navigator.getRouteParameters().get();
            for (String name : routeParameters.getParameterNames()) {
                //noinspection OptionalGetWithoutIsPresent
                paramsMap.put(name, routeParameters.get(name).get());
            }
        }

        return new RouteParameters(paramsMap);
    }
}
