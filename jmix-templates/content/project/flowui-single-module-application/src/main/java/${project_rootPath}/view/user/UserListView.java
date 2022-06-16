package ${project_rootPackage}.view.user;

import ${project_rootPackage}.entity.User;
import ${project_rootPackage}.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.screen.*;

@Route(value = UserListView.ROUTE, layout = MainView.class)
@UiController("${normalizedPrefix_underscore}User.browse")
@UiDescriptor("user-list-view.xml")
@LookupComponent("usersTable")
@DialogMode(width = "800px", height = "600px")
public class UserListView extends StandardLookup<User> {

    public static final String ROUTE = "users";
}