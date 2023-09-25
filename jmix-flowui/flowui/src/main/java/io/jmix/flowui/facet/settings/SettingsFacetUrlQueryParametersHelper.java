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

package io.jmix.flowui.facet.settings;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.QueryParameters;
import io.jmix.core.annotation.Internal;
import io.jmix.flowui.component.PaginationComponent;
import io.jmix.flowui.facet.SettingsFacet;
import io.jmix.flowui.facet.UrlQueryParametersFacet;
import io.jmix.flowui.facet.urlqueryparameters.AbstractUrlQueryParametersBinder;
import io.jmix.flowui.facet.urlqueryparameters.PaginationUrlQueryParametersBinder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Provides methods for collaboration between {@link SettingsFacet} and {@link UrlQueryParametersFacet}.
 */
@Internal
@org.springframework.stereotype.Component("ui_SettingsFacetUrlQueryParametersHelper")
public class SettingsFacetUrlQueryParametersHelper {

    /**
     * @param queryParameters URL query parameters
     * @param binder          binder
     * @return {@code true} if URL query parameters contain specific parameters for the
     * {@link UrlQueryParametersFacet.Binder}
     */
    public boolean containsParametersForBinder(QueryParameters queryParameters, UrlQueryParametersFacet.Binder binder) {
        if (binder instanceof PaginationUrlQueryParametersBinder paginationBinder) {
            return containsParametersForPagination(queryParameters.getParameters(), paginationBinder);
        }
        return false;
    }

    /**
     * @param binder URL query parameters binder
     * @return component that associated with the provided binder
     */
    public Optional<Component> getComponentFromBinder(UrlQueryParametersFacet.Binder binder) {
        if (binder instanceof AbstractUrlQueryParametersBinder abstractBinder) {
            return Optional.ofNullable(abstractBinder.getComponent());
        }
        return Optional.empty();
    }

    /**
     * Checks only {@link PaginationUrlQueryParametersBinder#getMaxResultsParam()} since {@link PaginationComponent}
     * supports 'maxResult' in settings.
     *
     * @param parameters       query parameters map
     * @param paginationBinder pagination URL query binder
     * @return {@code true} if parameters map contains 'maxResult' parameter
     */
    protected boolean containsParametersForPagination(Map<String, List<String>> parameters,
                                                      PaginationUrlQueryParametersBinder paginationBinder) {
        return parameters.containsKey(paginationBinder.getMaxResultsParam());
    }
}
