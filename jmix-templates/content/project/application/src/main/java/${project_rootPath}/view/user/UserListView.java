package ${project_rootPackage}.view.user;

import ${project_rootPackage}.entity.User;
import ${project_rootPackage}.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "users", layout = MainView.class)
@ViewController("${normalizedPrefix_underscore}User.list")
@ViewDescriptor("user-list-view.xml")
@LookupComponent("usersDataGrid")
@DialogMode(width = "50em")
public class UserListView extends StandardListView<User> {
}