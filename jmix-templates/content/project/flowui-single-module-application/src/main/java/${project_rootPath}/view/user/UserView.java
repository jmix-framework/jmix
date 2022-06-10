package ${project_rootPackage}.view.user;

import ${project_rootPackage}.entity.User;
import ${project_rootPackage}.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.screen.LookupComponent;
import io.jmix.flowui.screen.StandardLookup;
import io.jmix.flowui.screen.UiController;
import io.jmix.flowui.screen.UiDescriptor;

@Route(value = UserView.ROUTE, layout = MainView.class)
@UiController("${normalizedPrefix_underscore}User.browse")
@UiDescriptor("user-view.xml")
@LookupComponent("usersTable")
public class UserView extends StandardLookup<User> {

    public static final String ROUTE = "users";
}