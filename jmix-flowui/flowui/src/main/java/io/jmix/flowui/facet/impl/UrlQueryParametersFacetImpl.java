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

import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.facet.UrlQueryParametersFacet;
import io.jmix.flowui.facet.urlqueryparameters.HasInitialState;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.view.navigation.RouteSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class UrlQueryParametersFacetImpl extends AbstractFacet implements UrlQueryParametersFacet {

    private static final Logger log = LoggerFactory.getLogger(UrlQueryParametersFacetImpl.class);

    protected final RouteSupport routeSupport;

    protected List<Binder> binders = new ArrayList<>();
    protected Registration queryParametersChangeRegistration;
    protected Registration initialComponentsStateRegistration;
    protected Registration postReadyRegistration;
    protected Registration pushQueryParametersRegistration;

    protected QueryParameters initialQueryParameters;
    protected QueryParameters postponedQueryParameters;
    protected boolean ownerReady = false;

    public UrlQueryParametersFacetImpl(RouteSupport routeSupport) {
        this.routeSupport = routeSupport;
    }

    @Override
    public void setOwner(@Nullable View<?> owner) {
        super.setOwner(owner);

        if (queryParametersChangeRegistration != null) {
            queryParametersChangeRegistration.remove();
            queryParametersChangeRegistration = null;
        }

        if (initialComponentsStateRegistration != null) {
            initialComponentsStateRegistration.remove();
            initialComponentsStateRegistration = null;
        }

        if (postReadyRegistration != null) {
            postReadyRegistration.remove();
            postReadyRegistration = null;
        }

        if (owner != null && !UiComponentUtils.isComponentAttachedToDialog(owner)) {
            queryParametersChangeRegistration = ViewControllerUtils
                    .addQueryParametersChangeListener(owner, this::onViewQueryParametersChanged);
            initialComponentsStateRegistration = ViewControllerUtils
                    .addRestoreComponentsStateEventListener(owner, this::onRestoreInitialComponentsState);
            postReadyRegistration = ViewControllerUtils.addPostReadyListener(owner, this::onPostReady);
        }
    }

    protected void onViewQueryParametersChanged(View.QueryParametersChangeEvent event) {
        if (UiComponentUtils.isComponentAttachedToDialog(owner)) {
            return;
        }

        log.debug("Updating binders state: {}", queryParametersString(event.getQueryParameters()));

        for (Binder binder : binders) {
            binder.updateState(event.getQueryParameters());
        }
    }

    protected void onRestoreInitialComponentsState(View.RestoreComponentsStateEvent event) {
        if (UiComponentUtils.isComponentAttachedToDialog(owner)) {
            return;
        }

        log.debug("Restoring initial components state");

        ownerReady = false;
        initialQueryParameters = null;

        for (Binder binder : binders) {
            if (binder instanceof HasInitialState hasInitialState) {
                hasInitialState.applyInitialState();
            }
        }
    }

    protected void onPostReady(View.PostReadyEvent event) {
        if (UiComponentUtils.isComponentAttachedToDialog(owner)) {
            return;
        }

        log.debug("Applying initial QueryParameters: {}", queryParametersString(initialQueryParameters));

        if (initialQueryParameters != null) {
            QueryParameters queryParametersToAdd = initialQueryParameters;
            owner.getUI().ifPresent(ui -> {
                        ui.beforeClientResponse(owner, __ -> {
                            // must be executed immediately before the client responds,
                            // otherwise the server-side location will be the previous one
                            Location location = routeSupport.getActiveViewLocation(ui);

                            QueryParameters queryParameters = routeSupport.mergeQueryParameters(
                                    location.getQueryParameters(),
                                    queryParametersToAdd
                            );

                            Location newLocation = new Location(location.getPath(), queryParameters);
                            ui.getPage().getHistory().replaceState(null, newLocation);
                        });
                    }
            );
        }

        initialQueryParameters = null;
        ownerReady = true;
    }

    @Override
    public void registerBinder(Binder binder) {
        Preconditions.checkNotNullArgument(binder);

        binder.addUrlQueryParametersChangeListener(this::onComponentQueryParametersChanged);
        binders.add(binder);
    }

    protected void onComponentQueryParametersChanged(UrlQueryParametersChangeEvent event) {
        if (owner == null || UiComponentUtils.isComponentAttachedToDialog(owner)) {
            return;
        }

        if (!ownerReady) {
            if (initialQueryParameters == null) {
                initialQueryParameters = QueryParameters.empty();
            }

            log.debug("Collecting initial QueryParameters; added: {}; result: {}",
                    queryParametersString(event.getQueryParameters()),
                    queryParametersString(initialQueryParameters));

            initialQueryParameters = routeSupport.mergeQueryParameters(
                    initialQueryParameters,
                    event.getQueryParameters()
            );
        } else {
            if (postponedQueryParameters == null) {
                postponedQueryParameters = QueryParameters.empty();
            }

            log.debug("Collecting postponed QueryParameters; added: {}; previous: {}",
                    queryParametersString(event.getQueryParameters()),
                    queryParametersString(postponedQueryParameters));

            postponedQueryParameters = routeSupport.mergeQueryParameters(
                    postponedQueryParameters,
                    event.getQueryParameters()
            );

            if (pushQueryParametersRegistration != null) {
                return;
            }

            owner.getUI().ifPresent(ui ->
                    pushQueryParametersRegistration = ui.beforeClientResponse(owner, __ ->
                            routeSupport.fetchCurrentLocation(ui, location -> {
                                QueryParameters queryParameters = routeSupport.mergeQueryParameters(
                                        location.getQueryParameters(),
                                        postponedQueryParameters
                                );

                                log.debug("Mering postponed QueryParameters; added: {}; result: {}",
                                        queryParametersString(postponedQueryParameters),
                                        queryParametersString(queryParameters));

                                postponedQueryParameters = null;
                                pushQueryParametersRegistration = null;

                                routeSupport.setQueryParameters(ui, queryParameters);
                            })
                    )
            );
        }
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

    protected String queryParametersString(@Nullable QueryParameters queryParameters) {
        String queryString = queryParameters != null ? queryParameters.getQueryString() : "";
        return queryString.isEmpty() ? "<empty>" : queryString;
    }
}
