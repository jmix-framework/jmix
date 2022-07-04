package ${project_rootPackage}.view.user;

import ${project_rootPackage}.entity.User;
import ${project_rootPackage}.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = UserListView.ROUTE, layout = MainView.class)
@UiController("${normalizedPrefix_underscore}User.list")
@UiDescriptor("user-list-view.xml")
@LookupComponent("usersTable")
@DialogMode(width = "800px", height = "600px")
public class UserListView extends StandardListView<User> {

    public static final String ROUTE = "users";
}