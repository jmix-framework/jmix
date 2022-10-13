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

import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.flowui.view.StandardDetailView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static QueryParameters combineQueryParameters(QueryParameters... parameters) {
        Map<String, List<String>> combinedParams = new HashMap<>();

        for (QueryParameters queryParameters : parameters) {
            combinedParams.putAll(queryParameters.getParameters());
        }

        return new QueryParameters(combinedParams);
    }

    public static QueryParameters addQueryParameters(QueryParameters queryParameters, String name, String value) {
        Map<String, List<String>> resultParams = new HashMap<>();
        resultParams.put(name, List.of(value));
        resultParams.putAll(queryParameters.getParameters());

        return new QueryParameters(resultParams);
    }
}
