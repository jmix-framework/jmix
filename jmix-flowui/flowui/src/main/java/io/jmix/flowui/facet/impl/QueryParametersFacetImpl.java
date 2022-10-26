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

package io.jmix.flowui.facet.impl;

import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.facet.QueryParametersFacet;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.view.navigation.RouteSupport;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class QueryParametersFacetImpl extends AbstractFacet implements QueryParametersFacet {

    protected RouteSupport routeSupport;

    protected List<Binder> binders = new ArrayList<>();
    protected Registration queryParametersChangeRegistration;

    public QueryParametersFacetImpl(RouteSupport routeSupport) {
        this.routeSupport = routeSupport;
    }

    @Override
    public void setOwner(@Nullable View<?> owner) {
        super.setOwner(owner);

        if (queryParametersChangeRegistration != null) {
            queryParametersChangeRegistration.remove();
            queryParametersChangeRegistration = null;
        }

        if (owner != null) {
            queryParametersChangeRegistration =
                    ViewControllerUtils.addQueryParametersChangeListener(owner, this::onViewQueryParametersChanged);
        }
    }

    protected void onViewQueryParametersChanged(View.QueryParametersChangeEvent event) {
        if (UiComponentUtils.isComponentAttachedToDialog(owner)) {
            return;
        }

        for (Binder binder : binders) {
            binder.updateState(event.getQueryParameters());
        }
    }

    @Override
    public void registerBinder(Binder binder) {
        Preconditions.checkNotNullArgument(binder);

        binder.addQueryParametersChangeListener(this::onComponentQueryParametersChanged);
        binders.add(binder);
    }

    protected void onComponentQueryParametersChanged(QueryParametersChangeEvent event) {
        if (owner == null || UiComponentUtils.isComponentAttachedToDialog(owner)) {
            return;
        }

        owner.getUI().ifPresent(ui ->
                routeSupport.fetchCurrentLocation(ui, location -> {
                    QueryParameters queryParameters = routeSupport.mergeQueryParameters(
                            location.getQueryParameters(),
                            event.getQueryParameters()
                    );

                    routeSupport.setQueryParameters(ui, queryParameters);
                }));
    }

    @Override
    public List<Binder> getBinders() {
        return Collections.unmodifiableList(binders);
    }

    @Nullable
    @Override
    public Object getSubPart(String name) {
        return binders.stream()
                .filter(binder ->
                        Objects.equals(binder.getId(), name))
                .findAny()
                .orElse(null);
    }
}
