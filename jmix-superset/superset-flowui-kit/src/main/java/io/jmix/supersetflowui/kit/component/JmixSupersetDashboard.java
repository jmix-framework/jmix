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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.internal.ExecutionContext;
import com.vaadin.flow.internal.StateTree;

@Tag("jmix-superset-dashboard")
@NpmPackage(value = "@superset-ui/embedded-sdk", version = "0.1.0-alpha.10")
@JsModule("./src/superset-dashboard/jmix-superset-dashboard.js")
public class JmixSupersetDashboard extends Component implements HasSize, HasStyle {

    private static final String PROPERTY_GUEST_TOKEN = "guestToken";
    private static final String PROPERTY_EMBEDDED_ID = "embeddedId";
    private static final String PROPERTY_SUPERSET_DOMAIN = "supersetDomain";
    private static final String PROPERTY_TAB_VISIBILITY = "tabVisibility";
    private static final String PROPERTY_TITLE_VISIBILITY = "titleVisibility";
    private static final String PROPERTY_CHART_CONTROLS_VISIBILITY = "chartControlsVisibility";
    //    private static final String PROPERTY_FILTERS_VISIBILITY = "filtersVisibility";
    private static final String PROPERTY_FILTERS_EXPANDED = "filtersExpanded";

    protected String guestToken;
    protected String supersetDomain;

    protected StateTree.ExecutionRegistration updateDashboardExecution;

    public String getGuestToken() {
        return getElement().getProperty(PROPERTY_GUEST_TOKEN, guestToken);
    }

    /**
     * Sets "geust" token from Superset
     *
     * @param guestToken guest token
     */
    public void setGuestToken(String guestToken) {
        this.guestToken = guestToken;
    }

    public String getEmbeddedId() {
        return getElement().getProperty(PROPERTY_EMBEDDED_ID);
    }

    public void setEmbeddedId(String embeddedId) {
        getElement().setProperty(PROPERTY_EMBEDDED_ID, embeddedId);
    }

    public String getSupersetDomain() {
        return getElement().getProperty(PROPERTY_SUPERSET_DOMAIN, supersetDomain);
    }

    public void setSupersetDomain(String supersetDomain) {
        this.supersetDomain = supersetDomain;
    }

    public Boolean getTabVisibility() {
        return getElement().getProperty(PROPERTY_TAB_VISIBILITY, Boolean.FALSE);
    }

    public void setTabVisibility(Boolean tabVisibility) {
        getElement().setProperty(PROPERTY_TAB_VISIBILITY, tabVisibility);
    }

    public Boolean getTitleVisibility() {
        return getElement().getProperty(PROPERTY_TITLE_VISIBILITY, Boolean.FALSE);
    }

    public void setTitleVisibility(Boolean titleVisibility) {
        getElement().setProperty(PROPERTY_TITLE_VISIBILITY, titleVisibility);
    }

    public Boolean getChartControlsVisibility() {
        return getElement().getProperty(PROPERTY_CHART_CONTROLS_VISIBILITY, Boolean.FALSE);
    }

    public void setChartControlsVisibility(Boolean chartControlsVisibility) {
        getElement().setProperty(PROPERTY_CHART_CONTROLS_VISIBILITY, chartControlsVisibility);
    }

    // todo rp it seems the configuration of superset server is needed, for now it ignores parameter in URL
/*    public Boolean getFiltersVisibility() {
        return getElement().getProperty(PROPERTY_FILTERS_VISIBILITY, Boolean.FALSE);
    }
    public void setFiltersVisibility(Boolean filtersVisibility) {
        getElement().setProperty(PROPERTY_FILTERS_VISIBILITY, filtersVisibility);
    }*/

    public void setFiltersExpanded(Boolean filtersExpanded) {
        getElement().setProperty(PROPERTY_FILTERS_EXPANDED, filtersExpanded);
    }

    public Boolean getFiltersExpanded() {
        return getElement().getProperty(PROPERTY_FILTERS_EXPANDED, Boolean.TRUE);
    }

    protected void setGuestTokenInternal(String guestToken) {
        getElement().setProperty(PROPERTY_GUEST_TOKEN, guestToken);

        // todo rp update token if embedded ID is changed?
    }

    protected void setSupersetDomainInternal(String supersetDomain) {
        getElement().setProperty(PROPERTY_SUPERSET_DOMAIN, supersetDomain);
    }

    protected void requestUpdateDashboard() {
        // Do not call if it's still updating
        if (updateDashboardExecution != null) {
            return;
        }
        getUI().ifPresent(ui ->
                updateDashboardExecution = ui.beforeClientResponse(this, this::updateDashboard));
    }

    protected void updateDashboard(ExecutionContext context) {
        getElement().callJsFunction("updateDashboard");
    }
}
