package supersetflowui.kit.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

@Tag("jmix-superset-dashboard")
@NpmPackage(value = "@superset-ui/embedded-sdk", version = "0.1.0-alpha.10")
@JsModule("./src/component/superset-dashboard/jmix-superset-dashboard.js")
public class JmixSupersetDashboard extends Component implements HasSize, HasStyle {

    private static final String PROPERTY_TOKEN = "token";
    private static final String PROPERTY_DASHBOARD_ID = "dashboardId";
    private static final String PROPERTY_SUPERSET_DOMAIN = "supersetDomain";
    private static final String PROPERTY_TAB_VISIBILITY = "tabVisibility";
    private static final String PROPERTY_TITLE_VISIBILITY = "titleVisibility";
    private static final String PROPERTY_CHART_CONTROLS_VISIBILITY = "chartControlsVisibility";
    //    private static final String PROPERTY_FILTERS_VISIBILITY = "filtersVisibility";
    private static final String PROPERTY_FILTERS_EXPANDED = "filtersExpanded";

    public String getToken() {
        return getElement().getProperty(PROPERTY_TOKEN);
    }

    /**
     * Sets "geust" token from Superset
     *
     * @param token guest token
     */
    public void setToken(String token) {
        getElement().setProperty(PROPERTY_TOKEN, token);
    }

    public String getDashboardId() {
        return getElement().getProperty(PROPERTY_DASHBOARD_ID);
    }

    public void setDashboardId(String dashboardId) {
        getElement().setProperty(PROPERTY_DASHBOARD_ID, dashboardId);
    }

    public String getSupersetDomain() {
        return getElement().getProperty(PROPERTY_SUPERSET_DOMAIN);
    }

    public void setSupersetDomain(String supersetDomain) {
        getElement().setProperty(PROPERTY_SUPERSET_DOMAIN, supersetDomain);
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
}
