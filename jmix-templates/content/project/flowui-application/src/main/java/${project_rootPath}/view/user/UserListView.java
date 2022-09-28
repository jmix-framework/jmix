package ${project_rootPackage}.view.user;

import ${project_rootPackage}.entity.User;
import ${project_rootPackage}.view.main.MainView;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.view.*;
import io.jmix.securityflowui.action.ChangePasswordAction;
import io.jmix.securityflowui.action.ResetPasswordAction;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "users", layout = MainView.class)
@ViewController("${normalizedPrefix_underscore}User.list")
@ViewDescriptor("user-list-view.xml")
@LookupComponent("usersTable")
@DialogMode(width = "50em", height = "37.5em")
public class UserListView extends StandardListView<User> {

    @ViewComponent
    protected HorizontalLayout buttonsPanel;
    @ViewComponent
    protected DataGrid<User> usersTable;

    @Autowired
    protected MessageBundle messageBundle;
    @Autowired
    protected UiComponents uiComponents;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        createPasswordDropdownButton();
    }

    protected void createPasswordDropdownButton() {
        MenuBar additionActionMenuBar = uiComponents.create(MenuBar.class);

        buttonsPanel.add(additionActionMenuBar);

        Action changePasswordAction = usersTable.getAction(ChangePasswordAction.ID);
        Action resetPasswordAction = usersTable.getAction(ResetPasswordAction.ID);

        MenuItem item = additionActionMenuBar.addItem(VaadinIcon.COG.create());
        item.add(messageBundle.getMessage("additionalMenuBar"));

        SubMenu subMenu = item.getSubMenu();

        if (changePasswordAction != null) {
            subMenu.addItem(changePasswordAction.getText(),
                    clickEvent -> changePasswordAction.actionPerform(usersTable));
        }

        if (resetPasswordAction != null) {
            subMenu.addItem(resetPasswordAction.getText(),
                    clickEvent -> resetPasswordAction.actionPerform(usersTable));
        }

        item.addClickListener(clickEvent ->
                subMenu.getItems().forEach(menuItem -> menuItem.setEnabled(!usersTable.getSelectedItems().isEmpty()))
        );

        additionActionMenuBar.setVisible(!subMenu.getItems().isEmpty());
    }
}