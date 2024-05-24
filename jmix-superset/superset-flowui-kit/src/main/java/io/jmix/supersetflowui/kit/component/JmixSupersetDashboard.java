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

    private static final String PROPERTY_GUEST_TOKEN_INTERNAL = "_guestToken";
    private static final String PROPERTY_URL_INTERNAL = "_url";

    /**
     * @return guest token or {@code null} if not set
     */
    public String getGuestToken() {
        return getElement().getProperty(PROPERTY_GUEST_TOKEN);
    }

    /**
     * Sets guest token to perform a dashboard request. It is not required to set custom
     * guest token since component gets the token internally using Superset access token.
     * <p>
     * Note, if custom guest token is set, you should manually handle the expiration time and
     * set new guest token to the component.
     *
     * @param guestToken guest token
     */
    public void setGuestToken(String guestToken) {
        getElement().setProperty(PROPERTY_GUEST_TOKEN, guestToken);
    }

    /**
     * @return dashboard embedded ID or {@code null} if not set
     */
    public String getEmbeddedId() {
        return getElement().getProperty(PROPERTY_EMBEDDED_ID);
    }

    /**
     * Sets an embedded dashboard ID. This ID can be taken from dashboard if Superset has {@code EMBEDDED_SUPERSET}
     * feature flag.
     *
     * @param embeddedId a dashboard embedded ID
     */
    public void setEmbeddedId(String embeddedId) {
        getElement().setProperty(PROPERTY_EMBEDDED_ID, embeddedId);
    }

    /**
     * @return URL where Superset is deployed or {@code null} if not set
     */
    public String getUrl() {
        return getElement().getProperty(PROPERTY_URL);
    }

    /**
     * Sets a URL where Superset is deployed. If not set, will be used URL from application property.
     *
     * @param url URL to set
     */
    public void setUrl(String url) {
        getElement().setProperty(PROPERTY_URL, url);
    }

    public boolean isTitleVisible() {
        return getElement().getProperty(PROPERTY_TITLE_VISIBLE, Boolean.FALSE);
    }

    public void setTitleVisible(boolean titleVisible) {
        getElement().setProperty(PROPERTY_TITLE_VISIBLE, titleVisible);
    }

    public boolean isChartControlsVisible() {
        return getElement().getProperty(PROPERTY_CHART_CONTROLS_VISIBLE, Boolean.FALSE);
    }

    public void setChartControlsVisible(boolean chartControlsVisible) {
        getElement().setProperty(PROPERTY_CHART_CONTROLS_VISIBLE, chartControlsVisible);
    }

    public void setFiltersExpanded(boolean filtersExpanded) {
        getElement().setProperty(PROPERTY_FILTERS_EXPANDED, filtersExpanded);
    }

    public boolean isFiltersExpanded() {
        return getElement().getProperty(PROPERTY_FILTERS_EXPANDED, Boolean.FALSE);
    }

    protected void setUrlInternal(String url) {
        getElement().setProperty(PROPERTY_URL_INTERNAL, url);
    }

    protected void setGuestTokenInternal(String guestToken) {
        getElement().setProperty(PROPERTY_GUEST_TOKEN_INTERNAL, guestToken);
    }

    @ClientCallable
    protected void refreshGuestToken() {
        // implemented by inheritors
    }
}
