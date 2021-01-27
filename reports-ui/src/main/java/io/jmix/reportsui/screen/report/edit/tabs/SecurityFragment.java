package io.jmix.reportsui.screen.report.edit.tabs;

import io.jmix.core.LoadContext;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportScreen;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import io.jmix.security.model.Role;
import io.jmix.security.role.RoleRepository;
import io.jmix.securityui.model.RoleModel;
import io.jmix.securityui.model.RoleModelConverter;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.EntityComboBox;
import io.jmix.ui.component.Table;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionPropertyContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import io.jmix.ui.sys.ScreensHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

@UiController("report_ReportEditSecurity.fragment")
@UiDescriptor("security.xml")
public class SecurityFragment extends ScreenFragment {

    @Autowired
    protected InstanceContainer<Report> reportDc;

    @Autowired
    protected CollectionPropertyContainer<RoleModel> rolesDc;

    @Autowired
    protected CollectionPropertyContainer<ReportScreen> reportScreensDc;

    @Autowired
    protected CollectionContainer<RoleModel> roleModelsDc;

    @Autowired
    protected Table<RoleModel> rolesTable;

    @Autowired
    protected Table<ReportScreen> screenTable;

    @Autowired
    protected EntityComboBox<RoleModel> rolesField;

    @Autowired
    protected ComboBox<String> screenIdField;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected SecureOperations secureOperations;

    @Autowired
    protected PolicyStore policyStore;

    @Autowired
    protected ScreensHelper screensHelper;

    @Autowired
    protected WindowConfig windowConfig;

    @Autowired
    protected Messages messages;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    protected RoleModelConverter roleModelConverter;

    @Install(to = "roleModelsDl", target = Target.DATA_LOADER)
    protected List<RoleModel> roleModelsDlLoadDelegate(LoadContext<RoleModel> loadContext) {
        Collection<Role> roles = roleRepository.getAllRoles();
        return roles.stream()
                .map(roleModelConverter::createRoleModel)
                .sorted(Comparator.comparing(RoleModel::getName))
                .collect(Collectors.toList());

    }

    @Subscribe
    protected void onInit(InitEvent event) {
        List<WindowInfo> windowInfoCollection = new ArrayList<>(windowConfig.getWindows());
        // sort by screenId
        screensHelper.sortWindowInfos(windowInfoCollection);

        Map<String, String> screens = new LinkedHashMap<>();
        for (WindowInfo windowInfo : windowInfoCollection) {
            String id = windowInfo.getId();
            String menuId = "menu-config." + id;
            String localeMsg = messages.getMessage(menuId);
            String title = menuId.equals(localeMsg) ? id : id + " ( " + localeMsg + " )";
            screens.put(title, id);
        }
        screenIdField.setOptionsMap(screens);
    }

    @Subscribe
    protected void onAfterInit(AfterInitEvent event) {
        reloadRoles();
    }

    private void reloadRoles() {
        Collection<Role> roles = roleRepository.getAllRoles();
        List<RoleModel> roleModels = roles.stream()
                .map(roleModelConverter::createRoleModel)
                .sorted(Comparator.comparing(RoleModel::getName))
                .collect(Collectors.toList());
        roleModelsDc.getMutableItems().clear();
        roleModelsDc.getMutableItems().addAll(roleModels);
    }

    @Install(to = "rolesTable.exclude", subject = "enabledRule")
    protected boolean rolesTableExcludeEnabledRule() {
        return isUpdatePermitted();
    }

    @Install(to = "rolesTable.add", subject = "enabledRule")
    protected boolean rolesTableAddEnabledRule() {
        return isUpdatePermitted();
    }

    @Subscribe("rolesTable.add")
    protected void onRolesTableAdd(Action.ActionPerformedEvent event) {
        if (rolesField.getValue() != null && !rolesDc.containsItem(roleModelsDc.getItem())) {
            rolesDc.getMutableItems().add(rolesField.getValue());
        }
    }

    @Subscribe("screenTable.add")
    public void onScreenTableAdd(Action.ActionPerformedEvent event) {
        if (screenIdField.getValue() != null) {
            String screenId = screenIdField.getValue();

            boolean exists = false;
            for (ReportScreen item : reportScreensDc.getItems()) {
                if (screenId.equalsIgnoreCase(item.getScreenId())) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                ReportScreen reportScreen = metadata.create(ReportScreen.class);
                reportScreen.setReport(reportDc.getItem());
                reportScreen.setScreenId(screenId);
                reportScreensDc.getMutableItems().add(reportScreen);
            }
        }
    }

    protected boolean isUpdatePermitted() {
        return secureOperations.isEntityUpdatePermitted(metadata.getClass(Report.class), policyStore);
    }
}
