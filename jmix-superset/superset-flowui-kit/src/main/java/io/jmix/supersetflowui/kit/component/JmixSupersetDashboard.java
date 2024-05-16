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
import com.vaadin.flow.internal.ExecutionContext;
import com.vaadin.flow.internal.StateTree;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import elemental.json.impl.JreJsonFactory;

@Tag("jmix-superset-dashboard")
@NpmPackage(value = "@superset-ui/embedded-sdk", version = "0.1.0-alpha.10")
@JsModule("./src/superset-dashboard/jmix-superset-dashboard.js")
public class JmixSupersetDashboard extends Component implements HasSize, HasStyle {

    private static final String PROPERTY_GUEST_TOKEN = "guestToken";
    private static final String PROPERTY_EMBEDDED_ID = "embeddedId";
    private static final String PROPERTY_URL = "url";
    private static final String PROPERTY_TAB_VISIBILITY = "tabVisibility";
    private static final String PROPERTY_TITLE_VISIBILITY = "titleVisibility";
    private static final String PROPERTY_CHART_CONTROLS_VISIBILITY = "chartControlsVisibility";
    //    private static final String PROPERTY_FILTERS_VISIBILITY = "filtersVisibility";
    private static final String PROPERTY_FILTERS_EXPANDED = "filtersExpanded";

    private static final String PROPERTY_ACCESS_TOKEN = "_accessToken";
    private static final String PROPERTY_URL_INTERNAL = "_domain";
    private static final String PROPERTY_USER_INFO = "_userInfo";
    private static final String PROPERTY_DATASET_CONSTRAINS = "_datasetConstraints";

    protected StateTree.ExecutionRegistration updateComponentExecution;

    /**
     * @return guest token or {@code null} if not set
     */
    public String getGuestToken() {
        return getElement().getProperty(PROPERTY_GUEST_TOKEN);
    }

    /**
     * Sets guest token to perform a dashboard request. It is not required to set custom
     * guest token since component gets the guest token internally using Superset access token.
     * <p>
     * Note, if custom guest token is set, you should manually handle the expiration time and
     * set new guest token to the component.
     *
     * @param guestToken guest token
     */
    public void setGuestToken(String guestToken) {
        getElement().setProperty(PROPERTY_GUEST_TOKEN, guestToken);
    }

    public String getEmbeddedId() {
        return getElement().getProperty(PROPERTY_EMBEDDED_ID);
    }

    public void setEmbeddedId(String embeddedId) {
        getElement().setProperty(PROPERTY_EMBEDDED_ID, embeddedId);
    }

    public String getUrl() {
        return getElement().getProperty(PROPERTY_URL);
    }

    public void setUrl(String url) {
        getElement().setProperty(PROPERTY_URL, url);
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

    protected String getUrlInternal() {
        return getElement().getProperty(PROPERTY_URL_INTERNAL);
    }

    protected void setUrlInternal(String url) {
        getElement().setProperty(PROPERTY_URL_INTERNAL, url);
    }

    protected String getAccessToken() {
        return getElement().getProperty(PROPERTY_ACCESS_TOKEN);
    }

    protected void setAccessToken(String accessToken) {
        getElement().setProperty(PROPERTY_ACCESS_TOKEN, accessToken);
    }

    protected void setUserInfo(String username) {
        JsonObject json = new JreJsonFactory().createObject();
        json.put("username", username);
        getElement().setPropertyJson(PROPERTY_USER_INFO, json);
    }

    protected void setDatasetConstraints(JsonValue dataConstraints) {
        getElement().setPropertyJson(PROPERTY_DATASET_CONSTRAINS, dataConstraints);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        requestEmbedComponent();
    }

    protected void requestEmbedComponent() {
        // Do not call if it's still updating
        if (updateComponentExecution != null) {
            return;
        }

        getUI().ifPresent(ui ->
                updateComponentExecution = ui.beforeClientResponse(this, this::updateDashboard));
    }

    protected void updateDashboard(ExecutionContext context) {
        getElement().callJsFunction("updateDashboard");
    }
}
