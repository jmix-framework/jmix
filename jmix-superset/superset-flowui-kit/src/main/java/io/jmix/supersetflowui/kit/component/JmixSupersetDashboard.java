/*
 * Copyright 2024 Haulmont.
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

package io.jmix.supersetflowui.kit.component;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

@Tag("jmix-superset-dashboard")
@NpmPackage(value = "@superset-ui/embedded-sdk", version = "0.1.0-alpha.10")
@JsModule("./src/superset-dashboard/jmix-superset-dashboard.js")
public class JmixSupersetDashboard extends Component implements HasSize, HasStyle {

    private static final String PROPERTY_GUEST_TOKEN = "guestToken";
    private static final String PROPERTY_EMBEDDED_ID = "embeddedId";
    private static final String PROPERTY_URL = "url";
    private static final String PROPERTY_TITLE_VISIBLE = "titleVisible";
    private static final String PROPERTY_CHART_CONTROLS_VISIBLE = "chartControlsVisible";
    private static final String PROPERTY_FILTERS_EXPANDED = "filtersExpanded";

    /**
     * @return dashboard embedded ID or {@code null} if not set
     */
    public String getEmbeddedId() {
        return getElement().getProperty(PROPERTY_EMBEDDED_ID);
    }

    /**
     * Sets an embedded dashboard ID. This ID can be taken from dashboard if Superset has {@code EMBEDDED_SUPERSET}
     * feature flag. Without an embedded ID, the component won't start the request for fetching guest token.
     * <p>
     * Note that every time the embedded ID is set to the component, it will start a process of fetching a guest
     * token and handling its expiration.
     *
     * @param embeddedId a dashboard embedded ID
     */
    public void setEmbeddedId(String embeddedId) {
        getElement().setProperty(PROPERTY_EMBEDDED_ID, embeddedId);
    }

    /**
     * @return {@code true} if dashboard title is visible
     */
    public boolean isTitleVisible() {
        return getElement().getProperty(PROPERTY_TITLE_VISIBLE, Boolean.FALSE);
    }

    /**
     * Sets whether the dashboard title should be visible. The default value is {@code false}.
     *
     * @param titleVisible title visible option
     */
    public void setTitleVisible(boolean titleVisible) {
        getElement().setProperty(PROPERTY_TITLE_VISIBLE, titleVisible);
    }

    public boolean isChartControlsVisible() {
        return getElement().getProperty(PROPERTY_CHART_CONTROLS_VISIBLE, Boolean.FALSE);
    }

    /**
     * Sets whether the chart's kebab menu should be visible. The default value is {@code false}.
     *
     * @param chartControlsVisible chart control visible option
     */
    public void setChartControlsVisible(boolean chartControlsVisible) {
        getElement().setProperty(PROPERTY_CHART_CONTROLS_VISIBLE, chartControlsVisible);
    }

    /**
     * Sets whether the filter's bar should be expanded. The default value is {@code false}.
     *
     * @param filtersExpanded filters expanded option
     */
    public void setFiltersExpanded(boolean filtersExpanded) {
        getElement().setProperty(PROPERTY_FILTERS_EXPANDED, filtersExpanded);
    }

    /**
     * @return {@code true} if filter's bar is expanded
     */
    public boolean isFiltersExpanded() {
        return getElement().getProperty(PROPERTY_FILTERS_EXPANDED, Boolean.FALSE);
    }

    protected void setGuestTokenInternal(String guestToken) {
        getElement().setProperty(PROPERTY_GUEST_TOKEN, guestToken);
    }

    protected void setUrlInternal(String url) {
        getElement().setProperty(PROPERTY_URL, url);
    }

    @ClientCallable
    protected void fetchGuestToken() {
        // implemented by inheritors
    }
}
